using System.Collections;
using System.Collections.Generic;
using UnityEditorInternal;
using UnityEngine;

public class Cannonball : MonoBehaviour
{
    private float xVel, yVel, launchAngle, maxBounces = 5, waterAvgHeight;
    private readonly float gravity = 9.8f, coefOfRest = 0.6f;
    private bool shootingFromTheLeft, hasLanded = false;

    private GameObject balloonManager;
    private BalloonSpawner balloonManagerScript;
    private List<GameObject> balloonList;

    private GameObject cannonManager;
    private Cannons cannonScript;
    private List<GameObject> cannonballList;

    private Vector3[] mountainPoints, leftSidePoints, rightSidePoints, waterPoints;

    private void Start()
    {   
        //get the cannons script
        cannonManager = GameObject.Find("Cannons Manager");
        cannonScript = cannonManager.GetComponent<Cannons>();
        cannonballList = cannonScript.cannonballList;

        //get launch angle
        if (shootingFromTheLeft = cannonScript.getActiveCannon() == true) launchAngle = cannonScript.getCurRotleft();
        else launchAngle = cannonScript.getCurRotRight();

        //decomposing the launch speed into it's x and y components
        xVel = cannonScript.getLaunchVelocity() * Mathf.Cos(launchAngle);
        yVel = cannonScript.getLaunchVelocity() * Mathf.Sin(launchAngle);

        //Velocity's sign depends on which cannon we are firing from
        if (shootingFromTheLeft == false) xVel *= -1;

        //get terrain data points
        GameObject mountains = GameObject.Find("Mountain range");
        Terrain terrainScript = mountains.GetComponent<Terrain>();
        mountainPoints = terrainScript.mountainPoints;

        leftSidePoints = terrainScript.leftSidePoints;
        rightSidePoints = terrainScript.rightSidePoints;

        waterPoints = terrainScript.waterPoints;
        waterAvgHeight = terrainScript.getWaterAvgHeight();

        //Getting the balloon manager script
        balloonManager = GameObject.Find("Balloon Spawner");
        balloonManagerScript = balloonManager.GetComponent<BalloonSpawner>();
    }

    private void Update()
    {
        if (hasLanded==false) Motion();

        TerrainCollisionDetection();

        StopMotion();

        CheckOutOfBounds();

        BalloonCollisionDetection();
    }

    //calculates the projectile motion of the cannonball
    private void Motion()
    {
        //xVel is constant
        transform.Translate(transform.right * xVel * Time.deltaTime);

        //yVel is affected by gravity
        yVel = yVel - gravity * Time.deltaTime;
        transform.Translate(transform.up * yVel * Time.deltaTime);
    }

    //detects collision with the terrain and water
    private void TerrainCollisionDetection()
    {
        Vector3 cannonBallPos = this.gameObject.GetComponent<Renderer>().bounds.center;
        float radius = this.gameObject.GetComponent<Renderer>().bounds.extents.x;

        //collision response calculates velocity after impact, reflection and direction of object push

        //detects collision with the mountain range
        for (int i=0; i<mountainPoints.Length; ++i)
        {
            float x = mountainPoints[i].x, y = mountainPoints[i].y;

            if ((cannonBallPos.x + radius >= x && cannonBallPos.x- radius < x) && (cannonBallPos.y + radius >= y && cannonBallPos.y - radius < y))
            {                      
                int xDir=1;
                if ((i >= 0 && i < 32) || (i >= 96 && i < 128))
                {
                    if (xVel > 0)   //going from left ro right
                    {
                        xVel *= (-1) * coefOfRest; xDir = -1;
                    }
                    else
                    {
                        xVel *= (1) * coefOfRest; xDir = -1;
                    }
                } else if ((i >= 32 && i < 64) || (i >= 128 && i <= 160))
                {
                    if (xVel > 0)   //going from left ro right
                    {
                        xVel *= (1) * coefOfRest; xDir = 1;
                    }
                    else
                    {
                        xVel *= (-1) * coefOfRest; xDir = 1;
                    }
                }
                else
                {
                    if (xVel > 0)   //going from left ro right
                    {
                        xVel *= (1) * coefOfRest; xDir = 1;
                    }
                    else
                    {
                        xVel *= (1) * coefOfRest; xDir = -1;
                    }
                }
                
                //y velocity always changes direction on bounce
                yVel *= -coefOfRest;

                if (maxBounces > 0)
                {
                    //apply a small position change to avoid detecting the same collsion many times
                    transform.position += new Vector3(0.1f * xDir, 0.1f);
                    maxBounces--;
                }
                break;
            }

        }

        //detects collision with the left side ground
        float x0 = leftSidePoints[1].x, x1 = leftSidePoints[0].x;
        if ((cannonBallPos.x + radius >= x0 && cannonBallPos.x - radius <= x1) 
            && (cannonBallPos.y - radius <= -2f))
        {
            int xDir;
            if (xVel > 0)   //going from left ro right
            {
                xVel *= (1) * coefOfRest; xDir = 1;
            }
            else
            {
                xVel *= (1) * coefOfRest; xDir = -1;
            }

            yVel *= -coefOfRest;
            
            if (maxBounces > 0)
            {
                transform.position += new Vector3(0.1f * xDir, 0.1f);
                maxBounces--;
            }
        }

        //detects collsion with the right side ground
        x0 = rightSidePoints[0].x; x1 = rightSidePoints[1].x;
        if ((cannonBallPos.x + radius >= x0 && cannonBallPos.x - radius <= x1)
            && (cannonBallPos.y - radius <= -2f))
        {
            int xDir;
            if (xVel > 0)   //going from left ro right
            {
                xVel *= (1) * coefOfRest; xDir = 1;
            }
            else
            {
                xVel *= (1) * coefOfRest; xDir = -1;
            }
            
            yVel *= -coefOfRest;
            
            if (maxBounces > 0)
            {
                transform.position += new Vector3(0.1f * xDir, 0.1f);
                maxBounces--;
            }
        }

        //detects if ball went into the water
        x0 = waterPoints[0].x; x1 = waterPoints[waterPoints.Length-1].x;
        if ((cannonBallPos.x > x0 && cannonBallPos.x < x1)
            && (cannonBallPos.y - radius < waterAvgHeight))
        {
            cannonballList.Remove(gameObject);
            Destroy(gameObject);
        }

    }

    //if applicable stop the cannonball and time despawn
    private void StopMotion()
    {
        Vector3 pos = this.gameObject.GetComponent<Renderer>().bounds.center;
        if (Mathf.Abs(xVel) < 0.05f && Mathf.Abs(yVel) < 0.05f && pos.y < -1.8f && maxBounces == 0)
        {
            hasLanded = true;
            yVel = 0; xVel = 0;

            cannonballList.Remove(gameObject);
            Destroy(gameObject, 1.5f);
        }
    }

    /*Check if a cannonball has gone outside the camera bounds
     *Doesn't apply to the vertical axis to allow cannonballs 
     *to fall down */
    private void CheckOutOfBounds()
    {
        Vector3 cannonBallPos = this.gameObject.GetComponent<Renderer>().bounds.center;
        if (cannonBallPos.x < -15 || cannonBallPos.x > 15)
        {
            cannonballList.Remove(gameObject);
            Destroy(gameObject);
        }
    }

    /*
     * Detects a collision with a balloon.
     * If positive sends a signal to the 
     * specific balloon so it can handle it.
     */
    private void BalloonCollisionDetection()
    {
        //update the balloon list
        balloonList = balloonManagerScript.balloonList;
        Balloon balloonScript;

        //current position and radius of cannonball 
        Vector3 cannonBallPos = this.gameObject.GetComponent<Renderer>().bounds.center;
        float radius = this.gameObject.GetComponent<Renderer>().bounds.extents.x;

        //we test with every balloon that is active on the scene
        for (int i = balloonList.Count - 1; i >= 0; i--)
        {
            //serves to signal a hit on a balloon string only once
            bool hitDetected = false;

            //get a specific balloon script and renderer
            LineRenderer balloonOutline = balloonList[i].GetComponent<LineRenderer>();
            balloonScript = balloonList[i].GetComponent<Balloon>();
            GameObject balloonRef = balloonList[i];

            //Do the right 2 quadrants of the cannon ball intersect the balloon 
            if ((cannonBallPos.x + radius >= balloonOutline.GetPosition(4).x) && 
                (cannonBallPos.x + radius <= balloonOutline.GetPosition(2).x))
            {
                //Do the top 2 quadrants of the cannon ball intersect the balloon
                if ((cannonBallPos.y + radius <= balloonOutline.GetPosition(3).y) &&
                (cannonBallPos.y + radius >= balloonOutline.GetPosition(0).y))
                {
                    balloonList.RemoveAt(i);
                    Destroy(balloonRef);
                    continue;
                } 
                //Do the bottom 2 quadrants of the cannon ball intersect the balloon
                else if ((cannonBallPos.y - radius <= balloonOutline.GetPosition(3).y) &&
                (cannonBallPos.y - radius >= balloonOutline.GetPosition(0).y))
                {
                    balloonList.RemoveAt(i);
                    Destroy(balloonRef);
                    continue;
                }
            }
            //Do the left 2 quadrants of the cannon ball intersect the balloon 
            else if ((cannonBallPos.x - radius >= balloonOutline.GetPosition(4).x) &&
                (cannonBallPos.x - radius <= balloonOutline.GetPosition(2).x))
            {
                //Do the top 2 quadrants of the cannon ball intersect the balloon
                if ((cannonBallPos.y + radius <= balloonOutline.GetPosition(3).y) &&
                (cannonBallPos.y + radius >= balloonOutline.GetPosition(0).y))
                {
                    balloonList.RemoveAt(i);
                    Destroy(balloonRef);
                    continue;
                }
                //Do the bottom 2 quadrants of the cannon ball intersect the balloon
                else if ((cannonBallPos.y - radius <= balloonOutline.GetPosition(3).y) &&
                (cannonBallPos.y - radius >= balloonOutline.GetPosition(0).y))
                {
                    balloonList.RemoveAt(i);
                    Destroy(balloonRef);
                    continue;
                }
            }
            //Does the cannonball intersect the string
            else {
                for (int j = 7; j < 11; j++)
                {
                    float x0 = balloonOutline.GetPosition(j-1).x, y0 = balloonOutline.GetPosition(j-1).y,
                    xf = balloonOutline.GetPosition(j).x, yf = balloonOutline.GetPosition(j).y;

                    //test if a vertex of the string is inside the cannonball
                    if (((xf <= cannonBallPos.x + radius && x0 >= cannonBallPos.x - radius) || 
                    (x0 <= cannonBallPos.x + radius && xf >= cannonBallPos.x - radius)) &&
                        ((y0 >= cannonBallPos.y - radius && yf <= cannonBallPos.y + radius) ||
                        (yf >= cannonBallPos.y - radius && y0 <= cannonBallPos.y + radius)) && hitDetected == false)
                    {
                        balloonScript.signalHit(true, xVel, yVel);  //transmit hit signal and data to the balloon
                        hitDetected = true; //we only detect collision once for a balloon in a single frame
                        break;
                    }
                    else //extra check to see is the string is in the cannonball
                    {   
                        if ((xf >= cannonBallPos.x - radius && xf <= cannonBallPos.x + radius) &&
                        y0 >= cannonBallPos.y && yf <= cannonBallPos.y)
                        {
                            balloonScript.signalHit(true, xVel, yVel);  //transmit hit signal and data to the balloon
                            hitDetected = true; //we only detect collision once for a balloon in a single frame
                            break;
                        }  
                    }
                }
            }
        }
    }

}
