using System;
using System.Collections;
using System.Collections.Generic;
using UnityEditor.SceneManagement;
using UnityEngine;

public class path_gen : MonoBehaviour
{
    public int gridX, gridZ, curX, curZ, endX, endZ, ammoSpawnRate, totalWorldAmmo;
    public float gridSpacingOffset = 1f;
    public GameObject roadParent, mazeSpawner, ammoBox;

    private Stack<int[]> path = new Stack<int[]>(), pathBackup = new Stack<int[]>();
    private Vector3 gridOrigin = Vector3.zero;

    private void Start()
    {
        CreatePath();
        SpawnPath();
        SetMazeSpawn();
    }

    /*Calculates the trajectory of the path. Initially based on Wilson's algorithm, it only
    goes in 3 directions (up, down, right) for completion guarantee. Stores the coordinates 
    in a stack while calculating and unwinds it when self intersecting. Since this can only
    happen in one axis the unwinding needed only goes as far as the last two steps. This
    was not the case initially as to why the process is overkill for the current directional limitations.*/
    private void CreatePath(){
        int[] coordPair = {curX, curZ};
        path.Push(coordPair);
        int prevDir=2;

        while (curX < endX)
        {
            int dir = UnityEngine.Random.Range(1, 4);

            if (dir == 1 && curZ < gridZ)
            {
                int[] pair = { curX, curZ + 1 };
                if (SearchStack(curX, curZ + 1))
                {
                    UndoPath(curX, curZ + 1);
                    int[] coords = path.Peek();
                    curX = coords[0]; curZ = coords[1];
                }
                else { path.Push(pair); ++curZ; prevDir = 1; }
            }
            else if (dir == 2 && curX < gridX)
            {
                int[] pair = { curX + 1, curZ };
                if (SearchStack(curX + 1, curZ))
                {
                    UndoPath(curX + 1, curZ);
                    int[] coords = path.Peek();
                    curX = coords[0]; curZ = coords[1];
                }
                else
                {
                    if (prevDir != 2 && curX < gridX-1)
                    {
                        path.Push(pair); ++curX;
                    }
                    int[] pair2 = { curX + 1, curZ };
                    path.Push(pair2); ++curX; prevDir = 2;
                }
            }
            else if (dir == 3 && curZ > 0)
            {
                int[] pair = { curX, curZ - 1 };
                if (SearchStack(curX, curZ - 1))
                {
                    UndoPath(curX, curZ - 1);
                    int[] coords = path.Peek();
                    curX = coords[0]; curZ = coords[1];
                }
                else { path.Push(pair); --curZ; prevDir = 3; }
            }
        }
        endZ = curZ;
    }

    /*Called when the path generation generates a new coordinate. Looks for path self intersection and 
    returns true if this happens. An alternative to overloading the stack.Contains as the latter doesn't
    compare array contents but rather array ids by default.*/
    private bool SearchStack(int x, int z)
    {
        int blocks = path.Count;

        while (blocks > 0)
        {
            int[] pair = path.Pop();
            pathBackup.Push(pair);
            if (x == pair[0] && z == pair[1])
            {
                RestorePath();
                return true;
            }
            --blocks;
        }
        RestorePath();
        return false;
    }

    /*Helper method that restores a stack to its original state after it has been searched through*/
    private void RestorePath()
    {
        int blocksTemp = pathBackup.Count;
        while (blocksTemp > 0)
        {
            path.Push(pathBackup.Pop());
            --blocksTemp;
        }
    }

    /*Called when the path generation self intersets. Bactracks the stack up to the self intersection point*/
    private void UndoPath(int x, int z){
        int[] coords=path.Peek();
        while(coords[0]!=x && coords[1]!=z){
            path.Pop();
        }
    }

    /*Spawns the path and the ammo Objects in game*/
    private void SpawnPath(){
        int blocks=path.Count;

        while (blocks>0){

            //unwind the stack to get the path blocks coordinates
            int[] coordPair = path.Pop();
            int x=coordPair[0], z=coordPair[1];

            //spawn path blocks
            SpawnCube(x * gridSpacingOffset, 0, z * gridSpacingOffset, 0f, 1f, 1f, 1f, "Ground");
            
            --blocks;

            //spawn ammo
            int ammoSpawnChance = UnityEngine.Random.Range(0, 11);
            if (ammoSpawnChance <= ammoSpawnRate && x!=0 && z!=0)
            {
                SpawnAmmo(x, z);
            }
        }
    }

    //Spawns a cube in game according to some arguments
    private void SpawnCube(float posX, float posY, float posZ, float r, float g, float b, float a, string layer)
    {
        GameObject cube = GameObject.CreatePrimitive(PrimitiveType.Cube);
        
        Vector3 spawnPosition = new Vector3(posX * gridSpacingOffset, posY, posZ * gridSpacingOffset) + gridOrigin;        
        cube.transform.SetParent(roadParent.transform, true);
        cube.transform.position = spawnPosition;

        cube.GetComponent<MeshRenderer>().material.color = new Color(r, g, b, a);
        cube.GetComponent<MeshFilter>().mesh.RecalculateNormals();

        cube.layer = LayerMask.NameToLayer("Ground");
    }
   
    //Spawns the ammo prefab in game. Updates world ammo count
    void SpawnAmmo(int x, int z)
    {
        ammo box = ScriptableObject.CreateInstance<ammo>();
        box.model = ammoBox;
        Instantiate(box.model, new Vector3(x, 1f, z), Quaternion.identity);
        ++totalWorldAmmo;
    }

    //Attaches th emaze spawn to the path
    void SetMazeSpawn()
    {
        mazeSpawner.transform.SetPositionAndRotation(new Vector3(gridX + 2.5f, 0, endZ - 6.2f), Quaternion.identity);
    }
}

  
