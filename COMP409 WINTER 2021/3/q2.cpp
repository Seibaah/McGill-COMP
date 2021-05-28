#include <iostream>
#include <unordered_set>
#include <random>
#include <mutex>
#include <chrono>
#include <vector>

using namespace std;
using namespace std::chrono;

class Vertex {
public:
    int Id;
    int Color;
    vector<Vertex*> AdjList;

    Vertex() {
        Color = 0;
    };

    ~Vertex() {};

    inline bool operator==(Vertex _V2) { return !(Id == _V2.Id); }
};

class UndirectedGraph {
public:
    int N;
    int E;
    vector<Vertex*> Vertices;

    UndirectedGraph(int _N, int _E) {
        N = _N;
        E = _E;
        int i = 0;

        for (int i = 0; i < N; i++) {
            Vertex* V = new Vertex();
            Vertices.push_back(V);
        }
    };
};

void Assign(int Num, vector<Vertex*> ConflictingSets);
int DetectConflicts(int Num, vector<Vertex*> ConflictingSets, vector<Vertex*> NewConflictingSet);
mutex Mtx;

int main(int argc, char* argv[]) {
    
    int NumOfVertices, NumOfEdges, NumOfThreads;
    if (argc < 4) {
        NumOfVertices = 10; NumOfEdges = 33; NumOfThreads = 5;
    }
    else {
        NumOfVertices= strtol(argv[1], NULL, 10);
        NumOfEdges = strtol(argv[2], NULL, 10);
        NumOfThreads = strtol(argv[3], NULL, 10);
    }

    //create a graph 
    UndirectedGraph G (NumOfVertices, NumOfEdges);
    for (int i = 0; i < G.Vertices.size(); i++) {
        G.Vertices[i]->Id = i;
    }

    //random edge generation
    random_device Rd;
    mt19937 Gen(Rd());
    uniform_real_distribution<> Distribution(0, NumOfVertices);
    int EdgesLeft = NumOfEdges;
    while (EdgesLeft>0) {
        int V0 = Distribution(Gen), VF = Distribution(Gen);
        //self edges not allowed
        if (V0 == VF) continue;
        bool FoundDuplicate=false;
        for (int i = 0; i < G.Vertices[V0]->AdjList.size(); i++) {
            if (G.Vertices[V0]->AdjList[i]->Id == VF) {
                FoundDuplicate = true;
                break;
            }
        }
        //only one edge from u to v can exist at a time
        if (FoundDuplicate == false) {
            G.Vertices[V0]->AdjList.push_back(G.Vertices[VF]);
            G.Vertices[VF]->AdjList.push_back(G.Vertices[V0]);
            EdgesLeft--;
        }
    }

    //Start timer
    auto start = high_resolution_clock::now();

    //Gebremedhin and Mann inspired algorithm
    vector<Vertex*> ConflictingSets=G.Vertices;
    vector<Vertex*> NewConflictingSet;
    int Conflicting = NumOfVertices;
    while (Conflicting != 0) {
        Assign(NumOfThreads, ConflictingSets);
        Conflicting = DetectConflicts(NumOfThreads, ConflictingSets, NewConflictingSet);
        ConflictingSets = NewConflictingSet;
    }

    //End timer
    auto stop = high_resolution_clock::now();
    auto duration = duration_cast<microseconds>(stop - start);
    cout << "Time: "<< duration.count() << " microseconds" << endl;

    int MaxVertexDegree = INT_MIN, MaxColorUsed = INT_MIN;
    for (int i = 0; i < G.Vertices.size(); i++) {
        //cout << "Vertex " << G.Vertices[i]->Id <<" Color "<< G.Vertices[i]->Color<<endl;
        if (G.Vertices[i]->Color > MaxColorUsed) MaxColorUsed = G.Vertices[i]->Color;
        int Count = 0;
        for (int j = 0; j < G.Vertices[i]->AdjList.size(); j++) {
            //cout << G.Vertices[i]->AdjList[j]->Id << " ";
            Count++;
        }
        if (Count > MaxVertexDegree) MaxVertexDegree = Count;
        //cout << endl;
    }

    cout << "MaxVertexDegree: "<<MaxVertexDegree << endl;
    cout << "MaxColorUsed: " << MaxColorUsed << endl;
    
    return 0;
}

/*
* Assigns a new color to every conflicting vertex in the graph
*/
void Assign(int Num, vector<Vertex*> ConflictingSets) {

    //calculate thread partition size
    vector<int> Partitions;
    int PartitionSize = ConflictingSets.size() / Num;
    while (PartitionSize == 0 && Num>1) {
        PartitionSize = ConflictingSets.size() / --Num;
    }
    for (int i = 0; i < Num-1; i++) {
        Partitions.push_back(PartitionSize);
    }
    if (Num == 1) Partitions.push_back(ConflictingSets.size());
    else Partitions.push_back(ConflictingSets.size()-(PartitionSize*Num));

    /*
    * For every vertex in the subarray find the least magnitude available color to set it to
    */
    #pragma omp parallel for
    for (int i = 0; i < Num; i++) {
        for (int j = 0; j < Partitions[i]; j++) {
            unordered_set<int> Colors;
            for (Vertex *v : ConflictingSets[i * PartitionSize + j]->AdjList) {
                Colors.emplace(v->Color);
            }
            int MinFreeColor=0;
            while (true) {
                if (Colors.end() == Colors.find(MinFreeColor)) {
                    break;
                }
                MinFreeColor++;
            }
            ConflictingSets[i * PartitionSize + j]->Color = MinFreeColor;
        }
    }
}

/*
* Detects neighboring verices that have the same color and produces a new conflicting set
*/
int DetectConflicts(int Num, vector<Vertex*> ConflictingSets, vector<Vertex*> NewConflictingSet){

    //calculate thread partition size
    vector<int> Partitions;
    int PartitionSize = ConflictingSets.size() / Num;
    while (PartitionSize == 0 && Num > 1) {
        PartitionSize = ConflictingSets.size() / --Num;
    }
    for (int i = 0; i < Num - 1; i++) {
        Partitions.push_back(PartitionSize);
    }
    if (Num == 1) Partitions.push_back(ConflictingSets.size());
    else Partitions.push_back(ConflictingSets.size() - (PartitionSize * Num));
    
    /*
    * For each vertex traverse the adjacency list and find conflicts. If founf then add the current 
    * vertex to the new conflicting set
    */
    #pragma omp parallel for
    for (int i = 0; i < Num; i++) {
        for (int j = 0; j < Partitions[i]; j++) {
            bool FoundConflict = false;
            Vertex* V2;
            for (Vertex *v : ConflictingSets[i * PartitionSize + j]->AdjList) {
                if (v->Color == ConflictingSets[i * PartitionSize + j]->Color) {
                    FoundConflict = true;
                    V2 = v;
                }
            }

            //use of mutex to enforce ME while modifying the list
            if (FoundConflict == true) {
                Mtx.lock();
                NewConflictingSet.push_back(V2);
                Mtx.unlock();
            }
            FoundConflict = false;
        }
    }
    return NewConflictingSet.size();
}



