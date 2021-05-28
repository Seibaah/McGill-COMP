import java.util.*;
 
public class BellmanFord{
 
    private int[] distances = null;
    private int[] predecessors = null;
    private int source;
 
    class BellmanFordException extends Exception{
        public BellmanFordException(String str){
            super(str);
        }
    }
 
    class NegativeWeightException extends BellmanFordException{
        public NegativeWeightException(String str){
            super(str);
        }
    }
 
    class PathDoesNotExistException extends BellmanFordException{
        public PathDoesNotExistException(String str){
            super(str);
        }
    }
 
    BellmanFord(WGraph g, int source) throws NegativeWeightException{
        /* Constructor, input a graph and a source
         * Computes the Bellman Ford algorithm to populate the
         * attributes 
         *  distances - at position "n" the distance of node "n" to the source is kept
         *  predecessors - at position "n" the predecessor of node "n" on the path
         *                 to the source is kept
         *  source - the source node
         *
         *  If the node is not reachable from the source, the
         *  distance value must be Integer.MAX_VALUE
         */
    	
    	int nbOfNodes = g.getNbNodes();
    	
    	this.distances = new int[nbOfNodes];
    	this.predecessors = new int [nbOfNodes];
    	this.source=source;
    	
    	//set distance to every node but the source to unknown
    	for (int i=0; i<nbOfNodes; i++) {
    		distances[i] = Integer.MAX_VALUE;
    		predecessors[i] = -1;
    	}
    	distances[source]=0;
    	
    	//edge relaxing loop
    	for (int i = 1; i<nbOfNodes-1; i++) {
    		for (Edge e : g.getEdges()) {
    			int u = e.nodes[0];
    			int v = e.nodes[1];
    			int w = e.weight;
    			
    			if (distances[u] != Integer.MAX_VALUE && distances[v] > distances[u] + w) {
    				distances[v] = distances[u] + w;
    				predecessors[v] = u;
    			}
    		}
    	}
    	
    	//verify there isn't a negative weight cycle
    	for (Edge e : g.getEdges()) {
    		int u = e.nodes[0];
			int v = e.nodes[1];
			int w = e.weight;
			
			if (distances[u] != Integer.MAX_VALUE && distances[v] > distances[u] + w) {
				throw new NegativeWeightException("There is a negative weight cycle in the graph");
			}
    	}
    	
    } 
 
    public int[] shortestPath(int destination) throws PathDoesNotExistException{
        /*Returns the list of nodes along the shortest path from 
         * the object source to the input destination
         * If not path exists an Error is thrown
         */
    	
    	//verify the destination is reachable
    	if (distances[destination] == Integer.MAX_VALUE) {
    		throw new PathDoesNotExistException("No path leads to the destination");
    	}
    	
    	//trace back the predecessors from the destination
    	ArrayList<Integer> path = new ArrayList<Integer>();
    	path.add(destination);
    	int curr = destination;
    	
    	while (curr != source) {
    		curr = predecessors[curr];
    		path.add(curr);
    	}
    	
    	//reverse the obtained path and convert to array
    	Collections.reverse(path);
    	int[] p = new int[path.size()];
 
    	for (int i=0; i<path.size(); i++) {
    		p[i]=path.get(i);
    	}
    	
    	return p;
    }
 
    public void printPath(int destination){
        /*Print the path in the format s->n1->n2->destination
         *if the path exists, else catch the Error and 
         *prints it
         */
        try {
            int[] path = this.shortestPath(destination);
            for (int i = 0; i < path.length; i++){
                int next = path[i];
                if (next == destination){
                    System.out.println(destination);
                }
                else {
                    System.out.print(next + "-->");
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
 
    public static void main(String[] args){
 
        String file = args[0];
        WGraph g = new WGraph(file);
        try{
            BellmanFord bf = new BellmanFord(g, g.getSource());
            bf.printPath(g.getDestination());
        }
        catch (Exception e){
            System.out.println(e);
        }
 
   } 
}