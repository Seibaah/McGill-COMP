using System.Collections;
using System.Collections.Generic;
using System.Runtime.InteropServices.ComTypes;
using UnityEngine;
using UnityEngine.U2D;

public class Balloon : MonoBehaviour
{
    public LineRenderer line;

    private int stringAngle;
    private float deltaX, windForce, windDirectionalMultiplier, windXVector, collisionSlope;
    private readonly float constraint=1.0f, stringConstraint = 0.3f;
    private List<float[]> stringHits = new List<float[]>();
    private bool hitOnString = false, hitCountdownActive = false, 
        collisionOnString = false, collisionCountdownActive = false;

    private GameObject balloonSpawner;
    private BalloonSpawner script;

    private GameObject terrain;
    private Terrain terrainScript;
    private Vector3[] mountainPoints;

    private void Start()
    {
        //Create and add a line of 11 vertices, set width and color
        line = gameObject.AddComponent<LineRenderer>();
        line.positionCount = 11;
        line.startWidth = 0.1f; line.endWidth = 0.1f;
        line.startColor = new Color(1, 0, 1, 1); line.endColor = new Color(1, 0, 1, 1);

        //random spawn delta
        deltaX = Random.Range(-3f, 3f);

        //manipulate the line to create the balloon shape
        CreateBalloonShape();
        stringAngle = 270;

        //get a ref to the balloon spawner
        balloonSpawner = GameObject.Find("Balloon Spawner");
        script = balloonSpawner.GetComponent<BalloonSpawner>();

        //get the mountain position points
        terrainScript = GameObject.Find("Mountain range").GetComponent<Terrain>();
        mountainPoints = terrainScript.mountainPoints;
    }

    private void Update()
    {
        Motion();

        WindChange();

        if (line.GetPosition(3).y > mountainPoints[32].y)
        {
            WindOnBody();
            RestoreBalloonShape();
            WindOnString();
        }

        /*
         * if there is a cannonball hit on the string a response is calculated
         * and a timer goes off that keep the position for
         * a some time to allow the player to see it
         */
        if (hitOnString == true)
        {
            CannonballHitOnString();
            if (hitCountdownActive == false)
            {
                StartCoroutine(ResetHitFlag());
            }
        }

        /*
         * if there is a mountain collision with the string a response is calculated
         * and a timer goes off that keep the position for
         * a some time to allow the player to see it
         */
        MountainHitOnBalloon();
        if (collisionOnString == true)
        {
            PushStringAway();
            if (collisionCountdownActive == false)
            {
                StartCoroutine(ResetCollisionFlag());
            }
        }
    }

    //creates the balloon shape
    private void CreateBalloonShape()
    {
        //balloon vertices
        line.SetPosition(0, new Vector3(0.0f + deltaX, 0.0f - 2.5f));
        line.SetPosition(1, new Vector3(0.5f + deltaX, 0.25f - 2.5f));
        line.SetPosition(2, new Vector3(0.5f + deltaX, 0.75f - 2.5f));
        line.SetPosition(3, new Vector3(0.0f + deltaX, 1.0f - 2.5f));
        line.SetPosition(4, new Vector3(-0.5f + deltaX, 0.75f - 2.5f));
        line.SetPosition(5, new Vector3(-0.5f + deltaX, 0.25f - 2.5f));
        line.SetPosition(6, new Vector3(0.0f + deltaX, 0.0f - 2.5f));

        //string vertices
        line.SetPosition(7, new Vector3(0.0f + deltaX, -0.3f - 2.5f));
        line.SetPosition(8, new Vector3(0.0f + deltaX, -0.6f - 2.5f));
        line.SetPosition(9, new Vector3(0.0f + deltaX, -0.9f - 2.5f));
        line.SetPosition(10, new Vector3(0.0f + deltaX, -1.2f - 2.5f));
    }

    //apply the vetical motion of the balloon
    private void Motion()
    {
        for (int i = 0; i < 11; i++)
        {
            //apply on the top vertices the effect of rise
            if (i == 2 || i == 3 || i == 4)
            {
                line.SetPosition(i, line.GetPosition(i) + new Vector3(0.0f, 1.25f) * Time.deltaTime);
            }
        }
        for (int i = 0; i < 11; i++)
        {
            //apply on the balloon constraints and the string
            if (i == 1)
            {
                float dist = Vector3.Distance(line.GetPosition(i), line.GetPosition(2));
                if (dist >= constraint/2)
                {
                    line.SetPosition(i, line.GetPosition(i) - new Vector3(0.0f, (constraint /2 - dist)));
                }
            }
            else if(i == 5)
            {
                float dist = Vector3.Distance(line.GetPosition(i), line.GetPosition(4));
                if (dist >= constraint / 2)
                {
                    line.SetPosition(i, line.GetPosition(i) - new Vector3(0.0f, (constraint / 2 - dist)));
                }
            }
            else if (i == 0 || i == 6)
            {
                float dist = Vector3.Distance(line.GetPosition(i), line.GetPosition(3));
                if (dist >= constraint)
                {
                    line.SetPosition(i, line.GetPosition(i) - new Vector3(0.0f, (constraint - dist)));
                }
            }
            else if (i > 6)
            {
                float dist = Vector3.Distance(line.GetPosition(i), line.GetPosition(i - 1));
                if (dist >= stringConstraint)
                {
                    line.SetPosition(i, line.GetPosition(i) - new Vector3(0.0f, (stringConstraint - dist)));
                }
            }
        }
    }

    //update wind factors
    private void WindChange()
    {
        windDirectionalMultiplier = script.windDirectionalMultiplier;
        windForce = script.windForce;
        windXVector = windForce * windDirectionalMultiplier;
    }

    //apply the wind effect on the ballon body
    private void WindOnBody()
    {
        for (int i = 0; i < 11; i++)
        {
            if (windDirectionalMultiplier < 0)
            {
                //wind coming form the right only is applied to the vertices on the right above the mountain top
                if (((i >= 0) && i <= 3) || i == 6)
                {
                    if (line.GetPosition(i).y > mountainPoints[32].y)
                    {
                        line.SetPosition(i, line.GetPosition(i) + new Vector3(windForce * windDirectionalMultiplier, 0.0f) * Time.deltaTime);
                    }
                }
            } 
            else if (windDirectionalMultiplier > 0)  //same logic as above but applied to the other side vertices
            {
                if (((i >= 3) && i <= 6) || i == 0)
                {
                    if (line.GetPosition(i).y > mountainPoints[32].y)
                    {
                        line.SetPosition(i, line.GetPosition(i) + new Vector3(windForce * windDirectionalMultiplier, 0.0f) * Time.deltaTime);
                    }
                }
            }
        }
        
    }

    //deform the string in accordance to the wind
    private void WindOnString()
    {
        for (int i = 7; i < 11; i++)
        {
            float xPrev = line.GetPosition(i - 1).x, yPrev = line.GetPosition(i - 1).y;

            //the angle of the trailing string depends on the wind intensity and direction
            if (windDirectionalMultiplier > 0)
            {
                if (Mathf.Abs(windXVector) <= 7.5)
                {
                    DeformString(240, -5);
                }
                else
                {
                    DeformString(225, -5);
                }
            }
            else if (windDirectionalMultiplier < 0)
            {
                if (Mathf.Abs(windXVector) <= 7.5)
                {
                    DeformString(300, 5);
                }
                else
                {
                    DeformString(315, 5);
                }
            } else
            {
                DeformString(270, 0);
            }

        }
    }

    //apply a cannonball hit on the string vertices
    private void CannonballHitOnString()
    {
        float Vx, Vy;
        float[] hitData;

        for (int i = 0; i < stringHits.Count; i++)
        {
            hitData = stringHits[i];
            Vx = hitData[0];
            Vy = hitData[1];

            /*
             * What follows is a 40 case decision on where to move the string
             * based on the cannonball x and y velocity and the current position of the string.
             * An even more accurate version would take into account magnitude of velocity 
             * to decide the angle of rotation of the string nodes. A slight curve is applied.
             * See PDF for details.
             */
    
            //If string is in the 1st or 4th quadrant of the balloon local xy axis
            if ((stringAngle >= 0 && stringAngle <= 90) || (stringAngle <= 360 && stringAngle >= 270))
            {
                if (Vy < 0) //if the cannonball that hit is going down 
                {
                    if (Vx > 0)     //if the cannonball that hit is going to the right (#1 in pdf diagram)
                    {
                        if (stringAngle == 45)
                        {
                            DeformString(300, -5);
                        }
                        else if (stringAngle == 10 || stringAngle == 0)
                        {
                            DeformString(240, -5);
                        }
                        else if (stringAngle == 330)
                        {
                            DeformString(210, -5);
                        }
                        else if (stringAngle == 315)
                        {
                            DeformString(225, -5);
                        }
                        else if (stringAngle == 300)
                        {
                            DeformString(330, 5);
                        }
                        else if (stringAngle == 270)
                        {

                            DeformString(300, 5);
                        }
                    }
                    else     //if the cannonball that hit is going to the left (#2 in pdf diagram)
                    {
                        if (stringAngle == 45)
                        {
                            DeformString(240, 5);
                        }
                        else if (stringAngle == 10 || stringAngle == 0)
                        {
                            DeformString(210, -5);
                        }
                        else if (stringAngle == 330)
                        {
                            DeformString(170, -5);
                        }
                        else if (stringAngle == 315)
                        {
                            DeformString(210, -5);
                        }
                        else if (stringAngle == 300)
                        {
                            DeformString(170, 5);
                        }
                        else if (stringAngle == 270)
                        {
                            DeformString(240, -5);
                        }
                    }
                }
                else    //if the cannonball that hit is going up 
                {
                    if (Vx > 0)     //if the cannonball that hit is going to the right (#3 in pdf diagram)
                    {
                        if (stringAngle == 10 || stringAngle == 0)
                        {
                            CurveString(45);
                        }
                        else if (stringAngle == 330)
                        {
                            CurveString(45);
                        }
                        else if (stringAngle == 315)
                        {
                            CurveString(45);
                        }
                        else if (stringAngle == 300)
                        {
                            CurveString(45);
                        }
                        else if (stringAngle == 270)
                        {
                            CurveString(45);
                        }
                    }
                    else     //if the cannonball that hit is going to the left (#4 in pdf diagram)
                    {
                        if (stringAngle == 10 || stringAngle == 0)
                        {
                            CurveString(45);
                        }
                        else if (stringAngle == 330)
                        {
                            CurveString(45);
                        }
                        else if (stringAngle == 315)
                        {
                            DeformString(170, -5);
                        }
                        else if (stringAngle == 300)
                        {
                            CurveString(135);
                        }
                        else if (stringAngle == 270)
                        {
                            CurveString(135);
                        }
                    }
                }
            }
            else    //If string is in the 2nd or 3rd quadrant of the balloon local xy axis
            {
                if (Vy < 0) //if the cannonball that hit is going down 
                {
                    if (Vx > 0)     //if the cannonball that hit is going to the right (#1 in pdf diagram)
                    {
                        if (stringAngle == 135)
                        {
                            DeformString(300, -5);
                        }
                        else if (stringAngle == 170 || stringAngle == 180)
                        {
                            DeformString(330, 5);
                        }
                        else if (stringAngle == 210)
                        {
                            DeformString(10, 5);
                        }
                        else if (stringAngle == 225)
                        {
                            DeformString(330, 5);
                        }
                        else if (stringAngle == 240)
                        {
                            DeformString(10, -5);
                        }
                    }
                    else     //if the cannonball that hit is going to the left (#2 in pdf diagram)
                    {
                        if (stringAngle == 135)
                        {
                            DeformString(240, 5);
                        }
                        else if (stringAngle == 170 || stringAngle == 180)
                        {
                            DeformString(300, 5);
                        }
                        else if (stringAngle == 210)
                        {
                            DeformString(330, 5);
                        }
                        else if (stringAngle == 225)
                        {
                            DeformString(315, 5);
                        }
                        else if (stringAngle == 240)
                        {
                            DeformString(210, -5);
                        }
                    }
                }
                else    //if the cannonball that hit is going up 
                {
                    if (Vx > 0)     //if the cannonball that hit is going to the right (#3 in pdf diagram)
                    {
                        if (stringAngle == 170 || stringAngle == 180)
                        {
                            CurveString(135);
                        }
                        else if (stringAngle == 210)
                        {
                            CurveString(135);
                        }
                        else if (stringAngle == 225)
                        {
                            DeformString(10, 5);
                        }
                        else if (stringAngle == 240)
                        {
                            CurveString(45);
                        }
                    }
                    else     //if the cannonball that hit is going to the left (#4 in pdf diagram)
                    {
                        if (stringAngle == 170 || stringAngle == 180)
                        {
                            CurveString(135);
                        }
                        else if (stringAngle == 210)
                        {
                            CurveString(135);
                        }
                        else if (stringAngle == 225)
                        {
                            CurveString(135);
                        }
                        else if (stringAngle == 240)
                        {
                            CurveString(135);
                        }
                    }
                }
            }
        }
    }
     
    //Alters the string shape using an absolute angle in degrees
    private void DeformString(int angle, int deltaTeta)
    {
        if (angle >= 360)
        {
            angle -= 360;
        } 
        else if (angle < 0)
        {
            angle += 360;
        }

        stringAngle = angle;     
        for (int i = 7; i < 11; i++)
        {
            float teta = (angle+(deltaTeta*(i-7))) * 3.14f / 180f;
            float dx = stringConstraint * Mathf.Cos(teta), dy = stringConstraint * Mathf.Sin(teta);

            float xPrev = line.GetPosition(i - 1).x, yPrev = line.GetPosition(i - 1).y;
            line.SetPosition(i, new Vector3(xPrev + dx, yPrev + dy));
        }
    }

    //Special alteration of the string. Uses absolute angle in degrees
    private void CurveString(int angle)
    {
        stringAngle = angle;
        float alfa1, alfa2;
        if (angle == 45)
        {
            alfa1 = 10; alfa2 = 70;
        }
        else
        {
            alfa1 = 170; alfa2 = 110;
        }
        
        for (int i = 7; i < 11; i++)
        {
            if (i == 7 || i ==8)
            {
                float teta = alfa1 * 3.14f / 180f;
                float dx = stringConstraint * Mathf.Cos(teta), dy = stringConstraint * Mathf.Sin(teta);
                float xPrev = line.GetPosition(i - 1).x, yPrev = line.GetPosition(i - 1).y;
                line.SetPosition(i, new Vector3(xPrev + dx, yPrev + dy));
            }
            else
            {
                float teta = alfa2 * 3.14f / 180f;
                float dx = stringConstraint * Mathf.Cos(teta), dy = stringConstraint * Mathf.Sin(teta);
                float xPrev = line.GetPosition(i - 1).x, yPrev = line.GetPosition(i - 1).y;
                line.SetPosition(i, new Vector3(xPrev + dx, yPrev + dy));
            }
        }
    }

    //Signals a cannonball hit on the string has occurred and gets the collision details. Called by a cannonball
    public void signalHit(bool b, float Vx, float Vy)
    {
        float[] hitData = new float[2];
        hitOnString = b;
        hitData[0] = Vx;
        hitData[1] = Vy;

        stringHits.Add(hitData);
    }

    //detects if the string collides with a mountain
    private void MountainHitOnBalloon()
    {
        //detects collision with the mountain range
        for (int i = 0; i < 161; ++i)
        {
            float x = mountainPoints[i].x, y = mountainPoints[i].y;

            if ((i >= 0 && i < 32) || (i >= 96 && i < 128))
            {
                //detects if balloon is colliding with the mountain
                if (x <= line.GetPosition(1).x && x >= line.GetPosition(5).x &&
                    y <= line.GetPosition(2).y && y >= line.GetPosition(0).y)
                {
                    PushBalloonAway(-0.2f, 0.2f);
                }
                //detect collision between the string and the mountain
                if (collisionCountdownActive == false)
                {
                    for (int j = 7; j < 11; j++)
                    {
                        float x0 = line.GetPosition(j - 1).x, y0 = line.GetPosition(j - 1).y,
                        xf = line.GetPosition(j).x, yf = line.GetPosition(j).y;

                        //test if a mountain point inside the string
                        if (x <= x0 && x >= xf && y <= y0 && y >= yf)
                        {
                            collisionOnString = true;
                            collisionSlope = 1;
                        }
                    }
                }     
            }
            else if ((i >= 32 && i < 64) || (i >= 128 && i <= 160))
            {
                //detects if balloon is colliding with the mountain
                if (x <= line.GetPosition(1).x && x >= line.GetPosition(5).x &&
                    y <= line.GetPosition(4).y && y >= line.GetPosition(0).y)
                {
                    PushBalloonAway(0.2f, 0.2f);
                }
                //detect collision between the string and the mountain
                if (collisionCountdownActive == false)
                {
                    for (int j = 7; j < 11; j++)
                    {
                        float x0 = line.GetPosition(j - 1).x, y0 = line.GetPosition(j - 1).y,
                        xf = line.GetPosition(j).x, yf = line.GetPosition(j).y;

                        //test if a mountain point inside the string
                        if (x >= x0 && x <= xf && y <= y0 && y >= yf)
                        {
                            collisionOnString = true;
                            collisionSlope = -1;
                        }
                    }
                }
            }       
        }
    }

    //instantaneous push to the balloon in a given direction
    private void PushBalloonAway(float dx, float dy)
    {
       for (int i=0; i<11; i++)
        {
            line.SetPosition(i, new Vector3(line.GetPosition(i).x + dx, line.GetPosition(i).y + dy));
        }
    }

    //string's response to a collision with the mountain
    private void PushStringAway()
    {
        if (collisionSlope > 0)
        {
            DeformString(180, -5);
        } else
        {
            DeformString(0, 5);
        }

    }

    //Restore the balloon constraints after wind has been applied to the vertices above the mountain top
    private void RestoreBalloonShape()
    {
        float v3x = line.GetPosition(3).x;
        float v0x = line.GetPosition(0).x;
        if (v3x != v0x)
        {
            line.SetPosition(0, new Vector3(line.GetPosition(3).x, line.GetPosition(0).y));
            line.SetPosition(6, new Vector3(line.GetPosition(3).x, line.GetPosition(6).y));
        }

        if (windDirectionalMultiplier <= 0)
        {
            float v2x = line.GetPosition(2).x;
            if (Mathf.Abs(v2x - v3x) != 0.5f)
            {
                line.SetPosition(2, new Vector3(line.GetPosition(3).x + 0.5f, line.GetPosition(2).y));
            }

            float v1x = line.GetPosition(1).x;
            if (Mathf.Abs(v1x - v3x) != 0.5f)
            {
                line.SetPosition(1, new Vector3(line.GetPosition(3).x + 0.5f, line.GetPosition(1).y));
            }

            line.SetPosition(4, new Vector3(line.GetPosition(2).x - 1f, line.GetPosition(4).y));
            line.SetPosition(5, new Vector3(line.GetPosition(1).x - 1f, line.GetPosition(5).y));
        }
        else
        {
            float v4x = line.GetPosition(4).x;
            if (Mathf.Abs(v3x - v4x) != 0.5f)
            {
                line.SetPosition(4, new Vector3(line.GetPosition(3).x - 0.5f, line.GetPosition(4).y));
            }

            float v5x = line.GetPosition(5).x;
            if (Mathf.Abs(v3x - v5x) != 0.5f)
            {
                line.SetPosition(5, new Vector3(line.GetPosition(3).x - 0.5f, line.GetPosition(5).y));
            }

            line.SetPosition(2, new Vector3(line.GetPosition(4).x + 1f, line.GetPosition(2).y));
            line.SetPosition(1, new Vector3(line.GetPosition(5).x + 1f, line.GetPosition(1).y));
        }
    }

    /*
     * Coroutine that allows to keep the string cannonball hit position for a while
     * After timer passes wind position is restored
     */
    IEnumerator ResetHitFlag()
    {
        hitCountdownActive = true;
        yield return new WaitForSecondsRealtime(0.5f);
        hitOnString = false;
        hitCountdownActive = false;
        stringHits.Clear();
        yield break;
    }

    /*
     * Coroutine that allows to keep the string mountain hit position for a while
     * After timer passes wind position is restored
     */
    IEnumerator ResetCollisionFlag()
    {
        collisionCountdownActive = true;
        yield return new WaitForSecondsRealtime(0.3f);
        collisionCountdownActive = false;
        collisionOnString = false;
        collisionSlope = 0;
        yield break;
    }
}
