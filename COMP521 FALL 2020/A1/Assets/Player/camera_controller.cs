using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class camera_controller : MonoBehaviour
{
    public float sensitivity;
    public GameObject player;
    public Transform playerBody;

    private int playerAmmo;
    private float xRotation = 0f;
    private bool canPlay;
    private player_controller playerScript;

    /*FPS camera script. Credits to Brackeys at https://www.youtube.com/watch?v=_QajrabyTJc */
    private void Start()
    {
        Cursor.lockState = CursorLockMode.Locked;
        playerScript = player.GetComponent<player_controller>();
        canPlay = playerScript.canPlay;
    }

    private void Update()
    {
        canPlay = playerScript.canPlay;
        if (canPlay == true)
        {
            float mouseX = Input.GetAxis("Mouse X") * sensitivity * Time.deltaTime;
            float mouseY = Input.GetAxis("Mouse Y") * sensitivity * Time.deltaTime;

            xRotation -= mouseY;
            xRotation = Mathf.Clamp(xRotation, -50f, 90f);

            transform.localRotation = Quaternion.Euler(xRotation, 0f, 0f);
            playerBody.Rotate(Vector3.up * mouseX);
        }
    }

    //Updates the GUI with the ammo count and, if triggered, the win/loss state
    private void OnGUI()
    {
        GUI.Label(new Rect(10, 10, 150, 50), "Ammo: " + playerScript.ammo);
        if (playerScript.canPlay == false)
        {
            GUI.color = new Color(1, 0, 0, 1);
            if (playerScript.hasWon == true) { GUI.Label(new Rect(100, 100, 150, 50), "You won!"); }
            else GUI.Label(new Rect(100, 100, 150, 50), "You lost...");
        }
    }
}
