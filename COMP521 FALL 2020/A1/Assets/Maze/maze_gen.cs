using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using UnityEngine;

public class maze_gen : MonoBehaviour
{
    public int gridRows, gridColumns;
    public GameObject mazeSpawner;
    public int[,] completeMazeMap = new int[11,11];

    protected maze_walls[,] mazeWallsMap = new maze_walls[5, 5];                       
    protected maze_platforms[,] mazePlatformsMap = new maze_platforms[5, 5];

    private int entranceColumnCoordinate, exitColumnCoordinate;
    private bool hasFoundTheExit = false;
    private Stack<int[]> mazeSolutionStack = new Stack<int[]>();

    private void Start()
    {
        int entranceColumn = SpawnPartialMaze();
        PrimsMazeGen();
        ConnectPathToMaze(entranceColumn);
        FindMazeSolution();
        MarkMazeSolution();
    }

    //spawns a partial maze (i.e. a maze where all the platforms are surrounded by walls in every direction
    private int SpawnPartialMaze()
    {
        //entrance and exit are chosen randomly 
        int mazeEntranceColumn = UnityEngine.Random.Range(0, gridColumns), mazeExitColumn = UnityEngine.Random.Range(0, gridRows);

        for (int x = 0; x < gridRows; ++x)
        {
            for (int z = 0; z < gridColumns; ++z)
            {   
                //2d array to keep track of each platform
                mazePlatformsMap[x, z] = ScriptableObject.CreateInstance<maze_platforms>();

                //Spawns the platform in game
                SpawnPlatform(mazePlatformsMap[x, z], 3.1f * x, 0f, 3.1f * z, 0.5f, -0.5f, 0.5f, 0f, 1f, 1f, 1f, "Ground", "Destructible", -1);

                //spawn walls
                mazeWallsMap[x, z] = ScriptableObject.CreateInstance<maze_walls>();

                /*each entry in mazeWallsMap contains references to the walls in coordinate x, z
                if you pay attention you'll see each coordinate has unassigned wall references as 
                I couldn't make them share a wall. I preferred this over having walls stacked on
                top of each other at the same place.*/

                //spawn a rightwall only in the first column, all the columns get a left wall
                if (z == 0)
                {
                    mazeWallsMap[x, z].wall2 = SpawnWalls(3.1f * x, 1, (3.1f * z) - 1.5f, 2f, 2f, -0.5f, 0.5f, 0.5f, 0.5f, 1, "WallRight");
                }
                mazeWallsMap[x, z].wall1 = SpawnWalls(3.1f * x, 1, (3.1f * z) + 1.5f, 2f, 2f, -0.5f, 0.5f, 0.5f, 0.5f, 1, "WallLeft");

                //spawn a downwall only in the first row, all rows get an upper wall. Don't spawn a downwall in the entrance cell. Don't spawn a wall in the exit cell.
                if (z != mazeEntranceColumn && x == 0)
                {
                    mazeWallsMap[x, z].wall4 = SpawnWalls((3.1f * x) - 1.5f, 1, 3.1f * z, -0.5f, 2f, 2f, 0.5f, 0.5f, 0.5f, 1, "WallDown");
                }
                if (z == mazeExitColumn && x == gridRows - 1)
                {
                    continue;
                }
                mazeWallsMap[x, z].wall3 = SpawnWalls((3.1f * x) + 1.5f, 1, 3.1f * z, -0.5f, 2f, 2f, 0.5f, 0.5f, 0.5f, 1, "WallUp");

                //building the full maze matrix representation for the pathfinding algorithm
                completeMazeMap[2 * x + 1, 2 * z + 1] = 1;
                //Mark the entrance and exit in the full map
                if (z == mazeEntranceColumn && x == 0)
                {
                    completeMazeMap[0, 2 * z + 1] = 2;
                }
                if (z == mazeExitColumn)
                {
                    completeMazeMap[10, 2 * z + 1] = 2;
                }
            }
        }
        return mazeEntranceColumn;
    }

    //Spawns a platform and sets some of its parameters
    private void SpawnPlatform(maze_platforms cell, float posX, float posY, float posZ, float scaleX, float scaleY, float scaleZ, 
        float r, float g, float b, float a, string layer, string tag, int name)
    {
        Vector3 spawnCoordinates = new Vector3(posX, posY, posZ) + mazeSpawner.transform.localPosition;
        cell.platform = GameObject.CreatePrimitive(PrimitiveType.Cube);
        cell.platform.transform.SetParent(mazeSpawner.transform, true);

        cell.platform.transform.localScale += new Vector3(scaleX, scaleY, scaleZ);
        cell.platform.transform.position = spawnCoordinates;

        cell.platform.GetComponent<MeshRenderer>().material.color = new Color(r, g, b, a);
        cell.platform.GetComponent<MeshFilter>().mesh.RecalculateNormals();

        cell.platform.layer = LayerMask.NameToLayer(layer);
        cell.platform.tag = tag;
        cell.platform.name = (name).ToString();
    }

    //Spawns a wall and sets some of its parameters
    private GameObject SpawnWalls(float posX, float posY, float posZ, float scaleX, float scaleY, float scaleZ,
        float r, float g, float b, float a, string layer)
    {
        GameObject wall = new GameObject();

        Vector3 spawnCoordinates = new Vector3(posX, posY, posZ) + mazeSpawner.transform.localPosition;
        wall = GameObject.CreatePrimitive(PrimitiveType.Cube);
        wall.transform.SetParent(mazeSpawner.transform, true);

        wall.transform.localScale += new Vector3(scaleX, scaleY, scaleZ);
        wall.transform.position = spawnCoordinates;

        wall.GetComponent<MeshRenderer>().material.color = new Color(r, g, b, a);
        wall.GetComponent<MeshFilter>().mesh.RecalculateNormals();

        wall.layer = LayerMask.NameToLayer(layer);

        return wall;
    }

    //Uses Prim's Algorithm to generate a perfect maze
    private void PrimsMazeGen()
    {
        int unvisitedCells = gridRows * gridColumns;
        int[,] mazeCells = new int[gridRows, gridColumns], frontierCells = new int[gridRows, gridColumns];
        ArrayList frontierList = new ArrayList();

        //Pick a random starting cell and add it to the maze
        int cellX = UnityEngine.Random.Range(0, gridRows), cellZ = UnityEngine.Random.Range(0, gridColumns);
        mazeCells[cellX, cellZ] = 1;
        unvisitedCells--;

        //Add the starting cell frontier nodes to the frontier map, ignore return value
        ConnectFrontierCellToMaze(mazeCells, frontierCells, frontierList, cellX, cellZ);

        //Keep adding frontier nodes to the maze and updating the frontier map until there are no more unvisited cells
        while (unvisitedCells > 0)
        {
            //count the number of frontier cells
            int frontierListSize = frontierList.Count;

            //Choose a random cell in the frontier and get its coordinates
            int frontierCellSelected = UnityEngine.Random.Range(0, frontierListSize);
            int[] coordSelection = (int[])frontierList[frontierCellSelected];
            int x = coordSelection[0], z = coordSelection[1];

            //remove the coordinate from the frontier list and cellmap
            frontierList.RemoveAt(frontierCellSelected);
            frontierListSize = frontierList.Count;
            frontierCells[x, z] = 0;

            //add frontier to maze
            mazeCells[x, z] = 1;

            //directional list that keeps track of directions that lead from the selected frontier cell to the maze
            ArrayList directionsToMaze = new ArrayList();

            //Calculate the ways you can add the new future maze cell to the maze
            directionsToMaze = ConnectFrontierCellToMaze(mazeCells, frontierCells, frontierList, x, z);

            //Select how the new cell will be connected to the maze
            int directionsToMazeCount = directionsToMaze.Count;   //1-4
            int pathSelectedIndex = UnityEngine.Random.Range(0, directionsToMazeCount);   //1-4 paths maps to 0-3 index
            int wallTargetDeletion = (int)directionsToMaze[pathSelectedIndex];

            //Destroy the respective wall
            WallDeletion(wallTargetDeletion, x, z);

            --unvisitedCells;
        }

    }

    /*Calculates the directions in which you can go to connect a cell to the maze. 
    Returns an arrayList containing the possible directions to connect to the maze*/
    private ArrayList ConnectFrontierCellToMaze(int [,] mazeCells, int[,] frontierCells, ArrayList frontierList, int x, int z)
    {
        //directional list that keeps track of directions that lead from the selected frontier cell to the maze
        ArrayList directionsToMaze = new ArrayList();

        //Add cells to the fontier list and map if they are within maze bounds and if they aren't already either in the maze or the frontier.
        if (x - 1 >= 0)
        {
            if (mazeCells[x - 1, z] != 1 && frontierCells[x - 1, z] != 1)  //if the cell isnt already in the maze or frontier add it to the fontier
            {
                int[] pair = new int[2];
                pair[0] = x - 1; pair[1] = z;
                frontierList.Add(pair);
                frontierCells[x - 1, z] = 1;
            }
            else if (mazeCells[x - 1, z] == 1)
            {
                directionsToMaze.Add(4); //4 represents left
            }
        }
        if (x + 1 < gridRows)
        {
            if (mazeCells[x + 1, z] != 1 && frontierCells[x + 1, z] != 1)
            {
                int[] pair = new int[2];
                pair[0] = x + 1; pair[1] = z;
                frontierList.Add(pair);
                frontierCells[x + 1, z] = 1;
            }
            else if (mazeCells[x + 1, z] == 1)
            {
                directionsToMaze.Add(2); //2 represents right
            }
        }
        if (z - 1 >= 0)
        {
            if (mazeCells[x, z - 1] != 1 && frontierCells[x, z - 1] != 1)
            {
                int[] pair = new int[2];
                pair[0] = x; pair[1] = z - 1;
                frontierList.Add(pair);
                frontierCells[x, z - 1] = 1;
            }
            else if (mazeCells[x, z - 1] == 1)
            {
                directionsToMaze.Add(3); //3 represents down
            }
        }
        if (z + 1 < gridColumns)
        {
            if (mazeCells[x, z + 1] != 1 && frontierCells[x, z + 1] != 1)
            {
                int[] pair = new int[2];
                pair[0] = x; pair[1] = z + 1;
                frontierList.Add(pair);
                frontierCells[x, z + 1] = 1;
            }
            else if (mazeCells[x, z + 1] == 1)
            {
                directionsToMaze.Add(1); //1 represents up
            }
        }

        return directionsToMaze;
    }

    //Deletes a referenced wall at a given cell. Updates the relevant maps.
    private void WallDeletion(int wallTargetDeletion, int x, int z)
    {
        if (wallTargetDeletion == 1)  //cell in maze above the frontier
        {
            Destroy(mazeWallsMap[x, z].wall1);
            completeMazeMap[2 * x + 1, 2 * z + 2] = 2;
        }
        else if (wallTargetDeletion == 2)
        {
            Destroy(mazeWallsMap[x, z].wall3);
            completeMazeMap[2 * x + 2, 2 * z + 1] = 2;
        }
        else if (wallTargetDeletion == 3)
        {
            Destroy(mazeWallsMap[x, z - 1].wall1);
            completeMazeMap[2 * x + 1, 2 * z] = 2;
        }
        else if (wallTargetDeletion == 4)
        {
            Destroy(mazeWallsMap[x - 1, z].wall3);
            completeMazeMap[2 * x, 2 * z + 1] = 2;
        }
    }

    //attaches the maze's entrance at the end of the path by adjusting the maze Spawner position
    private void ConnectPathToMaze(int z)
    {
        float deltaZ;
        switch (z)
        {
            case 0:
                deltaZ = 6.2f; break;
            case 1:
                deltaZ = 3.1f; break;
            case 2:
                deltaZ = 0; break;
            case 3:
                deltaZ = -3.1f; break;
            case 4:
                deltaZ = -6.2f; break;
            default:
                deltaZ = 0; break;
        }
        mazeSpawner.transform.position += new Vector3(0, 0, deltaZ);
    }

    //Finds what platforms constitute the unique minimal solution to the maze
    void FindMazeSolution()
    {
        for (int z = 0; z < 11; ++z)
        {
            int val = completeMazeMap[0, z];
            if (val == 2) { entranceColumnCoordinate = z; }
        }
        for (int z = 0; z < 11; ++z)
        {
            int val = completeMazeMap[10, z];
            if (val == 2) { exitColumnCoordinate = z; }
        }

        //call rec pathfinding method
        hasFoundTheExit = MazeFill(1, entranceColumnCoordinate, 1);
    }

    /*Recursive pathfinding method that explores all the maze's ramifications
    and returns true when it has reached the exit coordinate. Before returning
    true the current position is saved onto a stack. The result is a stack of 
    pairs of coordinates from the entrance to the exit. The algorithm keeps track
    of where it comes so it doesn't backtrack to where it has already been to.
    Completion guaranteed since we have a perfect maze (i.e. no loops) with a 
    guaranteed and known entrance and exit.*/
    bool MazeFill(int x, int z, int prevPos)
    {
        int[] coord = new int[2];
        if (x == 9 && z == exitColumnCoordinate)
        {
            coord[0] = (x - 1) / 2; coord[1] = (z - 1) / 2;
            mazeSolutionStack.Push(coord);
            return true;
        }
        if (x + 1 < 11)
        {
            if (completeMazeMap[x + 1, z] == 2 && prevPos != 3)
            {
                if (MazeFill(x + 2, z, 1) == true)
                {
                    coord[0] = (x - 1) / 2; coord[1] = (z - 1) / 2;
                    mazeSolutionStack.Push(coord);
                    return true;
                }
            }
        }
        if (x - 1 >= 0)
        {
            if (completeMazeMap[x - 1, z] == 2 && prevPos != 1)
            {
                if (MazeFill(x - 2, z, 3) == true)
                {
                    coord[0] = (x - 1) / 2; coord[1] = (z - 1) / 2;
                    mazeSolutionStack.Push(coord);
                    return true;
                }
            }
        }
        if (z + 1 < 11)
        {
            if (completeMazeMap[x, z + 1] == 2 && prevPos != 2)
            {
                if (MazeFill(x, z + 2, 4) == true)
                {
                    coord[0] = (x - 1) / 2; coord[1] = (z - 1) / 2;
                    mazeSolutionStack.Push(coord);
                    return true;
                }
            }
        }
        if (z - 1 >= 0)
        {
            if (completeMazeMap[x, z - 1] == 2 && prevPos != 4)
            {
                if (MazeFill(x, z - 2, 2) == true)
                {
                    coord[0] = (x - 1) / 2; coord[1] = (z - 1) / 2;
                    mazeSolutionStack.Push(coord);
                    return true;
                }
            }
        }
        
        return false;
    }

    /*Unwind the stack of the solution coordinates. Updates the in game objects layer for tracking purposes
     and names them from 0 to n to have them as an ordered sequence. Allows to know if the player can or can't 
    complete the maze after shooting a platform. Non solution platforms are named -1 at spawn.*/
    void MarkMazeSolution()
    {
        int stackSize = mazeSolutionStack.Count;
        for (int i = 0; i < stackSize; ++i)
        {
            int[] arr = mazeSolutionStack.Pop();
            mazePlatformsMap[arr[0], arr[1]].platform.layer = LayerMask.NameToLayer("Solution");

            //name the solution platforms in sequential order from 0 to n
            mazePlatformsMap[arr[0], arr[1]].platform.name = i.ToString();
        }
    }
}
