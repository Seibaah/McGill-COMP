using System.Collections;
using System.Collections.Generic;
using UnityEditor.SceneManagement;
using UnityEngine;

public class bullet : MonoBehaviour
{
    public Vector3 prevPos;
    public GameObject player;
    public Camera playerCam;

    private Vector3 forward;

    private void Start()
    {
        prevPos = transform.position;

        //bullet uses the camera direction at instanciation to define trajectory
        player = GameObject.Find("Player");
        playerCam = player.transform.GetChild(0).GetComponent<Camera>();
        forward = playerCam.transform.forward;        

        //bullet is destroyed after 2 seconds if it hasn't collided with anything
        Destroy(gameObject, 2f);
    }

    void Update()
    {
        /*discrete collision doesn't work so we use a reverse raycast to see what we have gone through since the last frame
         Credits to Ather Omar at https://www.youtube.com/watch?v=cAM6fE3Cnk8 */
        prevPos = transform.position;

        transform.Translate(forward * 10f * Time.deltaTime);
                
        RaycastHit[] hits = Physics.RaycastAll(new Ray(prevPos, (transform.position - prevPos).normalized), (transform.position - prevPos).magnitude);
        for (int i=0;i<hits.Length; i++)
        {
            if (hits[i].transform.CompareTag("Destructible"))
            {
                string name = hits[i].transform.name;
                hits[i].transform.DetachChildren();
                Destroy(hits[i].transform.gameObject);

                //check loss on platfrom destruction
                player.GetComponent<player_controller>().checkPlatformDestruction(name);

            } Destroy(gameObject);  //Destroys bullet regardless on what was collided with
        }

    }
}
