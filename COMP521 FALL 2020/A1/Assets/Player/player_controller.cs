using Packages.Rider.Editor.Util;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class player_controller : MonoBehaviour
{
    public int ammo = 0;
    public float speed, jumpHeight;
    public bool canPlay = true, hasWon;
    public GameObject mazeSpawner, pathSpawner;
    public CharacterController controller;
    public Transform groundCheck;
    public LayerMask groundMask, solutionMask;
    

    private int lastSolutionPlatformVisited = -1;
    private float gravity = -9.81f, groundDistance = 0.2f;
    private bool isGrounded, hasDestroyedSolution = false;
    private string nameOfCurrentlyStandingPlatform;
    private path_gen pathGenScript;
    private Vector3 velocity;

    private void Start()
    {
        pathGenScript = pathSpawner.GetComponent<path_gen>();
    }

    /*FPS camera script. Credits to Brackeys at https://www.youtube.com/watch?v=_QajrabyTJc 
     Slight modifications were made to interrupt user input on win or loss*/
    private void Update()
    {
        if (Physics.CheckSphere(groundCheck.position, groundDistance, groundMask) ||
            Physics.CheckSphere(groundCheck.position, groundDistance, solutionMask)) {
            isGrounded = true;
        }
        else isGrounded = false;

        UpdateMazeProgress();

        if (isGrounded && velocity.y < 0)
            {
                velocity.y = 0f;
            }

        //canPlay is true only when the player has either won or lost. Input will be blocked accross all relevant actions.
        if (canPlay == true)
        {
            // player movement - forward, backward, left, right
            float x = Input.GetAxis("Horizontal");
            float z = Input.GetAxis("Vertical");
            Vector3 move = transform.right * 1.2f * x + transform.forward * z * 1.2f;

            controller.Move(move * speed * Time.deltaTime);

            if (Input.GetButtonDown("Jump") && isGrounded)
            {
                velocity.y = Mathf.Sqrt(jumpHeight * -2 * gravity);
            }

            velocity.y += 1.5f * gravity * Time.deltaTime;

            controller.Move(velocity * Time.deltaTime);
        }        

        checkFall();

        checkAmmo();

        checkWin();

    }

    /*Updates the player progress through the maze by keeping what is the current solution platform
    they are on. If the player jumps to a non solution then nothing is updated and the last solution 
    platform they were on indicator is preserved. Uses ground collision and the sequence name of the
    solution.*/
    private void UpdateMazeProgress()
    {
        //keepTrack of all-time maze progress
        Collider[] hitColliders = Physics.OverlapSphere(groundCheck.position, groundDistance);
        for (int i = 0; i < hitColliders.Length; ++i)
        {
            if (hitColliders[i].gameObject.transform.parent == mazeSpawner.transform)
            {
                nameOfCurrentlyStandingPlatform = hitColliders[i].gameObject.transform.name;
                int numOfCurrentlyStandingPlatform = int.Parse(nameOfCurrentlyStandingPlatform);
                if (numOfCurrentlyStandingPlatform != -1)
                {
                    lastSolutionPlatformVisited = numOfCurrentlyStandingPlatform;
                }
            }
        }
    }

    /*Checks if the player picked up an ammo box by collision*/
    private void OnTriggerEnter(Collider other)
    {
        if (other.CompareTag("Ammunition"))
        {
            Destroy(other.gameObject);
            ++ammo; --pathGenScript.totalWorldAmmo;
        }
    }

    /*Checks if the player can still complete the maze after destroying a platform. We only need to use
    the player's maze progress data and compare it to the platform's name which is parsed from string to int.
    Will NOT activate a losing state if player strands himself in a platform surrounded by non solution platforms.
    Trigger relevant state if needed.*/
    public void checkPlatformDestruction(string name)
    {
        int numOfDestroyedPlatform = int.Parse(name);
        Collider[] hitColliders = Physics.OverlapSphere(groundCheck.position, groundDistance);
        for (int i = 0; i < hitColliders.Length; ++i)
        {
            if (hitColliders[i].gameObject.transform.parent == mazeSpawner.transform)
            {
                //get the name of the destroyed obj and convert it to int
                //string nameOfCurrentlyStandingPlatform = hitColliders[i].gameObject.transform.name;
                int numOfCurrentlyStandingPlatform = int.Parse(nameOfCurrentlyStandingPlatform);
                if ((numOfCurrentlyStandingPlatform <= numOfDestroyedPlatform && numOfCurrentlyStandingPlatform != -1) || lastSolutionPlatformVisited <= numOfDestroyedPlatform)
                {
                    canPlay = false; hasWon = false;
                }
                else if (numOfDestroyedPlatform >= 0) { hasDestroyedSolution = true; }
            } else if (lastSolutionPlatformVisited < numOfDestroyedPlatform)
            {
                canPlay = false; hasWon = false;
            }
            else if (numOfDestroyedPlatform >= 0) { hasDestroyedSolution = true; }
        }
    }

    //If the player is below a certain y then he fell and thus lost. Trigger relevant state if needed.
    private void checkFall()
    {
        if (gameObject.transform.position.y < -10)
        {
            canPlay = false; hasWon = false;
        }
    }

    //If the player ran out of ammo and there is no more ammo in the world then he lost. Trigger relevant state if needed.
    private void checkAmmo()
    {
        if (pathGenScript.totalWorldAmmo == 0 && ammo == 0 && hasDestroyedSolution == false)
        {
            canPlay = false; hasWon = false;
        }
    }

    //If the player completes the maze and destroys the solution path then he wins
    private void checkWin()
    {
        if (gameObject.transform.position.x >= 47.5 && hasDestroyedSolution == true)
        {
            canPlay = false; hasWon = true;
        }
    }
}
