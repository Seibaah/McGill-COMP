using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Cannons : MonoBehaviour
{

    private bool shootingFromTheLeft = true, up, down, left, right;
    private float curRotLeft = 0.0f, curRotRight = 0.0f, launchVel = 15f;
    private readonly float maxRot = 90.0f, minLaunchVel = 12f, maxLaunchVel = 18f;

    public GameObject nozzleLeft, nozzleRight, barrelLeft, barrelRight;
    public List<GameObject> cannonballList;
    public Sprite cannonballSprite;

    private void Update()
    {
        GetInput();
    }

    //test for input
    private void GetInput()
    {
        //switch active cannon
        if (Input.GetKeyDown(KeyCode.Tab))
        {
            if (shootingFromTheLeft == true) shootingFromTheLeft = false;
            else shootingFromTheLeft = true;
        }

        //fire active cannon
        if (Input.GetKeyDown(KeyCode.Space))
        {
            Shoot();
        }

        //update elevation of active cannon
        if ((up = Input.GetKey(KeyCode.UpArrow)) || (down = Input.GetKey(KeyCode.DownArrow)))
        {
            AdjustLaunchAngle(up, down);
        }

        //power control for firing the cannon
        if ((left = Input.GetKey(KeyCode.LeftArrow)) || (right = Input.GetKey(KeyCode.RightArrow)))
        {
            AdjustLaunchSpeed(left, right);
        }
    }

    //instantiates a cannonball upon firing
    private void Shoot()
    {
        GameObject projectile = new GameObject();

        //all the cannonballs are referenced in a list
        cannonballList.Add(projectile);

        //Spawns the projectile in the current cannon's nozzle
        if (shootingFromTheLeft == true) projectile.transform.position = nozzleLeft.transform.position;
        else projectile.transform.position = nozzleRight.transform.position;

        //set and scale cannonballSprite and attach script to the cannonball obj
        projectile.name = "Cannonball";
        projectile.transform.rotation = Quaternion.identity;
        SpriteRenderer renderer = projectile.AddComponent<SpriteRenderer>();
        renderer.sprite = cannonballSprite;
        projectile.transform.localScale = new Vector2(0.07f, 0.07f);
        Cannonball script = projectile.AddComponent<Cannonball>();
    }

    //adjusts the rotation of the cannon barrel for launch
    private void AdjustLaunchAngle(bool up, bool down)
    {
        //which cannon is selected
        if (shootingFromTheLeft == true)
        {
            //going up or down
            if (up == true)
            {   
                //limit the angle of the cannon rotation
                if (curRotLeft<maxRot)
                {
                    barrelLeft.transform.Rotate(0.0f, 0.0f, 0.1f, Space.Self);
                    curRotLeft += 0.1f;
                }
            }
            else
            {
                if (curRotLeft > 0.0f)
                {
                    barrelLeft.transform.Rotate(0.0f, 0.0f, -0.1f, Space.Self);
                    curRotLeft -= 0.1f;
                }
            }
        } 
        else
        {
            if (up == true)
            {
                if (curRotRight < maxRot)
                {
                    barrelRight.transform.Rotate(0.0f, 0.0f, -0.1f, Space.Self);
                    curRotRight += 0.1f;
                }
            }
            else
            {
                if (curRotRight > 0.0f)
                {
                    barrelRight.transform.Rotate(0.0f, 0.0f, +0.1f, Space.Self);
                    curRotRight -= 0.1f;
                }
            }
        }
    }

    //controls initial velocity of the cannonball
    private void AdjustLaunchSpeed(bool left, bool right)
    {
        if (left == true)
        {
            if (launchVel > minLaunchVel)
            {
                launchVel -= 0.005f;
            }
        }
        else {
            if (launchVel < maxLaunchVel)
            {
                launchVel += 0.005f;
            }
        }
        
    }

    //return launch angle of left cannon in radians
    public float getCurRotleft()
    {
        return curRotLeft * 3.14f / 180f;
    }

    //return launch angle of right cannon in radians
    public float getCurRotRight()
    {
        return curRotRight * 3.14f / 180f;
    }

    //return launch velocity
    public float getLaunchVelocity()
    {
        return launchVel;
    }

    //return bool that indicates which cannon is active
    public bool getActiveCannon()
    {
        return shootingFromTheLeft;
    }

    //displays current launch velocity
    private void OnGUI()
    {
        GUI.color = new Color(1, 0, 0, 1);

        GUI.Label(new Rect(10, 10, 300, 50), "Launch Velocity " + System.Math.Round(launchVel, 2) + "u/s");

        if (shootingFromTheLeft == true)
        {
            GUI.Label(new Rect(10, 500, 300, 50), "Left cannon active");
        } else GUI.Label(new Rect(10, 500, 300, 50), "Right cannon active");
    }
}
