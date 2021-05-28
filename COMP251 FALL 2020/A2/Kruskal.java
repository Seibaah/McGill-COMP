import java.util.*;
 
public class Kruskal{
 
    public static WGraph kruskal(WGraph g){
    	
    	//sorting the edges in ascendant order
    	ArrayList<Edge> sortedEdges=g.listOfEdgesSorted();
    	
    	//store result in mst, subset to keep track of comparable nodes, i is increment 
    	WGraph mst= new WGraph();
    	
    	DisjointSets subset = new DisjointSets(g.getNbNodes());
    	int i=0;
    	
    	System.out.println(g.getNbNodes());
		
    	while(mst.getNbNodes()<g.getNbNodes()) {
    		//Grabbing an edge and its 2 connected nodes
    		Edge edge = sortedEdges.get(i++);
    		//Create a disjointSet and test if it forms a cycle within mst
    		
    		boolean condition=IsSafe(subset, edge);
    		//If the edge is safe add it to the mst
    		if (condition==true) {
    			mst.addEdge(edge);
    			int x=subset.find(edge.nodes[0]), y=subset.find(edge.nodes[1]);
    			subset.union(x, y);
    		}
    	}
 
    	return mst;
        /* Fill this method (The statement return null is here only to compile) */
    }
 
    public static Boolean IsSafe(DisjointSets p, Edge e){
    	
    	int x=p.find(e.nodes[0]), y=p.find(e.nodes[1]);
    	
    	if (x!=y) {
    		return true;
    	} 
    	else {
    		return false;
    	}
        /* Fill this method (The statement return 0 is here only to compile) */
    }
 
    public static void main(String[] args){
 
        String file = args[0];
        WGraph g = new WGraph(file);
        WGraph t = kruskal(g);
        System.out.println(t);
 
   } 
}
 