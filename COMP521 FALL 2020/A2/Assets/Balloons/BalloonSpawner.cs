using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SocialPlatforms;

public class BalloonSpawner : MonoBehaviour
{
    public List<GameObject> balloonList;
    public float windDirectionalMultiplier, windForce;
 
    private void Start()
    {
        //create coroutines to spawn balloons and change wind based on a timer
        StartCoroutine(SpawnTimer());
        StartCoroutine(WindTimer());
    }

    //balloon spawn timer
    IEnumerator SpawnTimer()
    {
        while (true)
        {
            SpawnBalloons();
            yield return new WaitForSecondsRealtime(1.0f);
        }
    }

    //wind reroll timer
    IEnumerator WindTimer()
    {
        while (true)
        {
            CalculateWindDirection();
            yield return new WaitForSeconds(2.0f);
        }
    }

    private void Update()
    {
        CheckOutOfBounds();
    }

    //spawn a ballon
    private void SpawnBalloons()
    {
        //create ballon game obj and attach a balloon script to it
        GameObject balloon = new GameObject();
        balloon.transform.name = "Balloon";
        balloon.AddComponent<Balloon>();

        //add the new balloon to the ref list
        balloonList.Add(balloon);
    }

    //calculate a new wind direction and intensity
    private void CalculateWindDirection()
    {
        windDirectionalMultiplier = Random.Range(-2f, 2f); 
        windForce = Random.Range(5f, 10f);
    }

    //despawns de baloon if it is out of the camera fov
    private void CheckOutOfBounds()
    {
        for (int i = balloonList.Count - 1; i >= 0; i--)
        {
            LineRenderer line = balloonList[i].GetComponent<LineRenderer>();
            GameObject balloonRef = balloonList[i];
            if (line.GetPosition(1).x < -14)
            {
                balloonList.RemoveAt(i);
                Destroy(balloonRef);
            }
            if (line.GetPosition(5).x > 14)
            {
                balloonList.RemoveAt(i);
                Destroy(balloonRef);
            }
            if (line.GetPosition(10).y > 8.5f)
            {
                balloonList.RemoveAt(i);
                Destroy(balloonRef);
            }
        }
    }

    //displays current launch velocity
    private void OnGUI()
    {
        GUI.color = new Color(1, 0, 0, 1);
        GUI.Label(new Rect(10, 30, 150, 50), "Wind: " + System.Math.Round(windDirectionalMultiplier * windForce, 2) + " u/s");
    }
}
