import java.util.*;
import java.io.File;
 
public class FordFulkerson {
 
	public static ArrayList<Integer> pathDFS(Integer source, Integer destination, WGraph graph){
		ArrayList<Integer> path = new ArrayList<Integer>();
		
		//YOUR CODE GOES HERE
		path.add(source);
		Integer cur = source;
		
		ArrayList<Integer> deadEnd = new ArrayList<Integer>();
		
		//loop until you reach the destination
		while(!path.contains(destination)) {
			int flag=0;//flag let us know if we reached a dead end node
			
			//try to add a valid edge from the adj list to the path
			for (Edge e : graph.getEdges()) {
				if(e.nodes[0]==cur) {
					//to be valid the edge needs to have capacity, not form a loop and not be tagged as a dead end
					if (e.weight!=0 && !path.contains(e.nodes[1]) && !deadEnd.contains(e.nodes[1])) {
						path.add(e.nodes[1]);
						cur = e.nodes[1];
						flag=1;
						break;
					}
				}
			}
			
			if (flag==0) {
				//if reached a dead end move back up the path and try different nodes
				if (path.size()>1) {
					path.remove(cur);
					deadEnd.add(cur);
					cur = path.get(path.size()-1);
					continue;
				}
				//if here then root has been reached, no more paths available
				else break;
			} 
			else if (flag==1) {
				//we successfully added a node to our path
				if (cur==destination) {
					//if it's the destination then we are done
					break;
				}
			}
			
		}
		
		return path;
	}
 
	//adds backwards edges to g based on path p.
	private static WGraph UpdateGraph(WGraph g, ArrayList<Integer> p, int b) {
		Integer source = p.get(0);
		Collections.reverse(p);
		
		for (int i=0; i<p.size()-1; i++) {
			Edge e = new Edge(p.get(i), p.get(i+1), b);
			Edge e2 = g.getEdge(p.get(i), p.get(i+1));
			if (e2==null) {
				g.addEdge(e);
			}
			else {
				e2.weight+=b;
			}
		}
		
		Collections.reverse(p);
		
		return g;
	}
	
	
	public static String fordfulkerson(WGraph graph){
		String answer="";
		int maxFlow = 0;
		
		/* YOUR CODE GOES HERE		*/
		WGraph g = new WGraph(graph);
		WGraph g2 = new WGraph(graph);
		for (Edge e : g2.getEdges()) {
			e.weight=0;
		}
 
		//ford fulkerson loop
		while(true) {
			
			ArrayList<Integer> p = pathDFS(g.getSource(), g.getDestination(), g);
			if (!p.contains(g.getDestination())) {
				//this only happens when no more augmenting paths can be found
				break;
			}
			
			//find the flow of the augmenting path
			int flow = findBottleneck(p, g);
			if (flow<0) flow*=-1;
			
			g = UpdateGraph(g, p, -flow);
			
			//update flow in graph
			for (int i = 0; i < p.size() - 1; i++) {
                if (g.getEdge(p.get(i), p.get(i + 1)).weight>=0) {
                	g.getEdge(p.get(i), p.get(i + 1)).weight-= flow;
                }
                else {
                	g.getEdge(p.get(i), p.get(i + 1)).weight+= flow;
                }
            }
			
			maxFlow+=flow;
		}
		
		//g is a residual graph, it needs to be partitioned to make the final max flow graph
		WGraph flowG=SignedSubsetOfGraph(g, -1);
		WGraph freeG=SignedSubsetOfGraph(g, 1);
		WGraph finalG=ConvergeGraphs(flowG, freeG, g2);
	
		answer += maxFlow + "\n" + finalG.toString();	
		return answer;
	}
	
	//returns bottleneck on a path
	private static int findBottleneck(ArrayList<Integer> p, WGraph g) {
		int min = Integer.MAX_VALUE;
		
		for (int i=0; i<p.size()-1; i++) {
			for (Edge e : g.getEdges()) {
				
				if (e.nodes[0]==p.get(i) && e.nodes[1]==p.get(i+1)) {
					int w=e.weight;
					if (w<0) {
						w*=-1;
					}
					if (w<min) {
						min=w;
					}
				}
			}
		}
		
		return min;
	}
	
	//creates a subset graph that includes either the positive+0 or negative edges
	private static WGraph SignedSubsetOfGraph(WGraph g, int sign) {
		WGraph gSub = new WGraph();
		for (Edge e : g.getEdges()) {
			if (sign==-1 && e.weight<0) {
				Edge e2 = new Edge(e.nodes[1], e.nodes[0], -1*e.weight);
				gSub.addEdge(e2);
			}
			else if (sign==1 && e.weight>=0) {
				gSub.addEdge(e);
			}
		}
		
		return gSub;
	}
 
	//builds the max flow graph from the flow graph and unused capacity graph
	private static WGraph ConvergeGraphs(WGraph flowG, WGraph freeG, WGraph emptyG) {
		
		for(Edge e : emptyG.getEdges()) {
			int u=e.nodes[0], v=e.nodes[1];
			Edge flowEdge = flowG.getEdge(u, v);
			Edge freeEdge = freeG.getEdge(u, v);
			
			if (flowEdge==null) {
				e.weight=0;
			}
			else {
				e.weight=flowEdge.weight;
			}
		}
		
		return emptyG;
	}
	
	 public static void main(String[] args){
		 String file = args[0];
		 File f = new File(file);
		 WGraph g = new WGraph(file);
	         System.out.println(fordfulkerson(g));
	 }
}