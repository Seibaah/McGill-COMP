using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Terrain : MonoBehaviour
{
    private float waterAvgHeight = 0.0f;
    private float[] mountainFunction, waterFunction;
    public LineRenderer mountainLine, groundLeft, groundRight, waterLine;
    public Vector3[] mountainPoints, leftSidePoints, rightSidePoints, waterPoints;

    private void Start()
    {
        PerlinNoise();

        //initialize the structs for the segments of each terrain section
        mountainPoints = new Vector3[161]; leftSidePoints = new Vector3[2]; 
        rightSidePoints = new Vector3[2]; waterPoints = new Vector3[32];

        //save the respective segments data in our data structs
        for (int i = 0; i < mountainPoints.Length; i++)
        {
            mountainPoints[i] = mountainLine.GetPosition(i);
            mountainPoints[i].y += mountainFunction[i];
            mountainLine.SetPosition(i, mountainPoints[i]);
        }

        for (int i=0; i<waterPoints.Length; i++)
        {
            waterPoints[i] = waterLine.GetPosition(i);
            waterPoints[i].y += waterFunction[i];
            waterLine.SetPosition(i, waterPoints[i]);

            waterAvgHeight += waterPoints[i].y;
        }

        waterAvgHeight /= waterPoints.Length;
        groundLeft.GetPositions(leftSidePoints);
        groundRight.GetPositions(rightSidePoints);
    }

    //computes perlin for the mountains and water
    private void PerlinNoise()
    {
        mountainFunction = new float[mountainLine.positionCount];
        Noise(mountainFunction, 0.5f, 2);
        Noise(mountainFunction, 0.25f, 1);

        waterFunction = new float[waterLine.positionCount];
        Noise(waterFunction, 0.25f, 2);
        Noise(waterFunction, 0.125f, 1);
    }

    //computes an octave and saves the function points in the arg array
    private void Noise(float[] points, float amp, int period)
    {
        for (int i = 0; i < points.Length; i+=period)
        {
            float val = (Random.value * amp) - (amp/2);
            points[i] += val;
        }
    }

    //getter for the avg height of the water line. For simplified water collision
    public float getWaterAvgHeight()
    {
        return waterAvgHeight;
    }
}
