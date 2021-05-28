import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class q1 {
	static int inputLength=5000000;
	static char[] input;
	static ArrayList<Character> output;
	
	static Worker[] workers;
	static int optimisticThr;
	static ExecutorService executor;
	static ArrayList<FutureTask<Boolean>> results;
	
	static ArrayList<GenericWorker> mergers;
	static ArrayList<GenericWorker> _mergersCopy;
	static ArrayList<FutureTask<GenericWorker>> mergeResults;
	
	static DFA dfa;
	
	static int outputLastState=0;
	static ArrayList<Integer> nextStatesPriority;
	static ArrayList<Character> lastPartialWord;
	static boolean lastPartialWordIsAFloat=false;
	
    public static long t0, tf;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		//generate random string (seeded or not)
		if (args.length==2) GenerateRandomString(Integer.parseInt(args[1]));
		else GenerateRandomString(-1);
		
		//COMMENT IN TO PRINT INPUT
		/*for (int i=0; i<input.length; i++) {
			System.out.print(input[i]);
		}
		System.out.println();*/
		
		//initialization
		output =  new ArrayList<Character>();
		optimisticThr = Integer.parseInt(args[0]);
		
		workers = new Worker[optimisticThr+1];
		int partitionThr = input.length/workers.length;
		executor = Executors.newFixedThreadPool(optimisticThr+1);
		results = new ArrayList<FutureTask<Boolean>>();
		
		BuildDFA();
		//PrintDFA();
		
		//prepare thread pool to do parsing
		workers[0]=new Worker(0, 0, Type.normal);
		results.add(new FutureTask<Boolean>(workers[0], true));
		for (int i=1; i<optimisticThr+1; i++) {
			workers[i]=new Worker(i, i*partitionThr, Type.optimistic);
			results.add(new FutureTask<Boolean>(workers[i], true));
		}
		
		//start timer
		t0=System.currentTimeMillis();
		
		//run the threads
		for (int i=0; i<optimisticThr+1; i++) {
			executor.submit(results.get(i));
		}
		
		//wait for parsing to be done
		for (int i=0; i<results.size(); i++) {
			results.get(0).get();
		}
		
		//merge in parallel the parsings
		MergeBrain();
		
		executor.shutdown();
		
		//timer end
		tf=System.currentTimeMillis();
		
		System.out.println("Time: "+(tf-t0));

		//COMMENT IN TO PRINT OUTPUT
		/*for (int i=0; i<mergers.get(0).branchParses.get(0).size(); i++) {
			System.out.print(mergers.get(0).branchParses.get(0).get(i));
		}*/
	}
	
	/*
	 * Parallel merge that converges a parse tree until one continuous segment remains
	 */
	private static void MergeBrain() throws InterruptedException, ExecutionException {
		int n=0;
		
		//create merger workers from the workers
		mergers=new ArrayList<GenericWorker>();
		for (int i=0; i<workers.length; i++) {
			if (workers.length%2==1 && i==workers.length-1)	mergers.add(new MergeWorker(n++, workers[i]));
			else if (i%2==0) mergers.add(new MergeWorker(n++, workers[i], workers[i+1]));
		}
		
		//create futures array list for the convergence
		mergeResults = new ArrayList<FutureTask<GenericWorker>>();
		for (int i=0; i<mergers.size(); i++) {
			mergeResults.add(new FutureTask<GenericWorker>((Callable) mergers.get(i)));
		}
		
		//run the first level of the convergence tree 
		for (int i=0; i<mergeResults.size(); i++) {
			executor.submit(mergeResults.get(i));
		}
		
		//wait for 1st convergence level to be done
		for (int i=0; i<mergeResults.size(); i++) {
			mergeResults.get(0).get();
		}
		
		//start a new convergence level
		while (mergers.size()>1) {
			n=0;
			_mergersCopy = new ArrayList<GenericWorker>();
			for (int i=0; i<mergers.size(); i++) {
				if (i%2==0 && i!=mergers.size()-1) {
					_mergersCopy.add(new MergeWorker(n++, mergers.get(i), mergers.get(i+1)));
				}
				else if (i%2==0 && i==mergers.size()-1 && mergers.size()!=1) {
					_mergersCopy.add(new MergeWorker(n++, mergers.get(i)));
				}
			}
			
			//create futures array list for the convergence
			mergeResults = new ArrayList<FutureTask<GenericWorker>>();
			for (int i=0; i<_mergersCopy.size(); i++) {
				mergeResults.add(new FutureTask<GenericWorker>((Callable) _mergersCopy.get(i)));
			}
			
			//run the n-th level of the convergence tree 
			for (int i=0; i<mergeResults.size(); i++) {
				executor.submit(mergeResults.get(i));
			}
			
			//wait for n-th convergence level to be done
			GenericWorker _w = null;
			for (int i=0; i<mergeResults.size(); i++) {
				_w = mergeResults.get(0).get();
			}
			
			mergers=_mergersCopy;
		}
		
		//parse the last word of the string
		ArrayList<Character> _lastWord = mergers.get(0).boundaryWords.get(0);
		if (mergers.get(0).branchEndStates[0]!=4) {
			int _l=_lastWord.size(), _l2=_l;
			while (_l-- >0) mergers.get(0).branchParses.get(0).remove(mergers.get(0).branchParses.get(0).size()-1);
			while (_l2-- >0) mergers.get(0).branchParses.get(0).add('_');
		}
	}
	
	//generate a random char array
	private static void GenerateRandomString(int seed) {
		Random rand;
		if (seed!=-1) rand = new Random(seed);
		else rand = new Random();
		
		input = new char[inputLength];
		
		for (int i=0; i<inputLength; i++) {
			int n=rand.nextInt(11);
			if (n>=0 && n<=9) input[i]=Character.forDigit(n, 10);
			else if (n==10) input[i]='.';
			else input[i]='a';
		}
	}
	
	//call to build a float recognition dfa
	private static void BuildDFA() {
		dfa = new DFA();
		
		dfa.AddNewstate(0, StateType.reject);
		
		dfa.AddNewstate(1, StateType.reject);
		String _char = "0";
		dfa.states.get(0).AddTransition(0, 1, _char);
		
		dfa.AddNewstate(2, StateType.reject);
		String _char3 = "123456789";
		String _char4 = "0123456789";
		dfa.states.get(0).AddTransition(0, 2, _char3);
		dfa.states.get(2).AddTransition(2, 2, _char4);
		
		dfa.AddNewstate(3, StateType.reject);
		String _char2 = ".";
		dfa.states.get(1).AddTransition(1, 3, _char2);
		dfa.states.get(2).AddTransition(2, 3, _char2);
		
		dfa.AddNewstate(4, StateType.accept);
		dfa.states.get(3).AddTransition(3, 4, _char4);
		dfa.states.get(4).AddTransition(4, 4, _char4);	
	}

}

/*
 * special type of worker thread that specializes in merging all parse branches between 2 workers
 */
class MergeWorker extends GenericWorker implements Callable<MergeWorker>{
	public GenericWorker w1;
	public GenericWorker w2;
	public ArrayList<ArrayList<Character>> w1Branches; 
	public ArrayList<ArrayList<Character>> w2Branches;
	
	MergeWorker(int _id, GenericWorker _w1, GenericWorker _w2){
		id=_id;
		p0=_w1.p0; pf=_w2.pf;
		dfa=_w1.dfa;
		w1Branches=_w1.branchParses;		
		w2Branches=_w2.branchParses;		
		w1=_w1; w2=_w2;
		BranchesInit();
	}
	
	MergeWorker(int _id, GenericWorker w){
		id=_id;
		p0=w.p0; pf=w.pf;
		dfa=w.dfa;
		w1Branches=w.branchParses;
		w2Branches=new ArrayList<ArrayList<Character>>();
		w1=w;
		BranchesInit();
	}	

	//Initialize Data structures - called by constructor
	private void BranchesInit() {
		branchParses=new ArrayList<ArrayList<Character>>();
		boundaryWords=new ArrayList<ArrayList<Character>>();
		branchEndStates = new int[5];
		for (int i=0; i<5; i++) {
			branchParses.add(new ArrayList<Character>());
			boundaryWords.add(new ArrayList<Character>());
			branchEndStates[i]=0;
		}
	}

	/*
	 * take all non-empty branch parses in w1
	 * and produce all possible connections
	 * and save in abstract data structures
	 */
	@Override
	public MergeWorker call() {
		for (int i=0; i<w1.branchEndStates.length; i++) {
			if (w1Branches.get(i).isEmpty()==false) CombineBranches(i);
		}
		
		return this;
	}
	
	/*
	 * Combines the parse from worker 1 that started in state s0 and merges it with the appropriate
	 * parse branch from worker 2 to produce the longest valid float possible between their boundary
	 */
	private void CombineBranches(int s0) {
		
		//only 1 worker was passed
		if (w2Branches.isEmpty()==true) {
			branchParses=w1Branches;
			branchEndStates=w1.branchEndStates;
			boundaryWords=w1.boundaryWords;
			return;
		}
		
		int w1EndState=w1.branchEndStates[s0];
		/*
		 * Easy merge possible, combine or update last word data
		 */
		if (w2Branches.get(w1EndState).isEmpty()==false
				&& Character.compare(w2Branches.get(w1EndState).get(0), '_')!=0) {
			
			branchParses.get(s0).addAll(w1Branches.get(s0));
			branchParses.get(s0).addAll(w2Branches.get(w1EndState));
			
			if (w1EndState==0) {
				branchEndStates[0]=w2.branchEndStates[0];
				boundaryWords.get(0).addAll(w2.boundaryWords.get(0));
			}
			else {
				//partial word carries over a thread segment
				if (w2.p0==w2.pf-w2.boundaryWords.get(w1EndState).size()) {
					branchEndStates[s0]=w2.branchEndStates[w1EndState];
					boundaryWords.get(s0).addAll(w1.boundaryWords.get(s0));
					boundaryWords.get(s0).addAll(w2.boundaryWords.get(w1EndState));
				}
				//partial word is contained within segment
				else {
					branchEndStates[s0]=w2.branchEndStates[w1EndState];
					boundaryWords.get(s0).addAll(w2.boundaryWords.get(w1EndState));
				}
			}
			return;
		}
		
		/*
		 * DFA reached an illegal state at the boundary.
		 */
		branchParses.get(s0).addAll(w1Branches.get(s0));
		
		if (w1EndState!=4) {
			int _l=w1.boundaryWords.get(s0).size(), _l2=_l;
			while (_l-- >0) branchParses.get(s0).remove(branchParses.get(s0).size()-1);
			while (_l2-- >0) branchParses.get(s0).add('_');
		}
		
		branchParses.get(s0).addAll(w2Branches.get(0));
		
		branchEndStates[s0]=w2.branchEndStates[0];
		boundaryWords.get(s0).addAll(w2.boundaryWords.get(w1EndState));
	}
}

/*
 * Specialized worker thread that parses a sector of the input string.
 * Can be of 2 types: normal or optimistic
 */
class Worker extends GenericWorker implements Runnable{
	public Type t;
	
	public boolean foundFloat;
	
	Worker (int _id, int _p0, Type _t){
		id=_id; p0=_p0; pi=p0;
		if (id==q1.optimisticThr) pf=q1.input.length-1;
		else pf=p0+q1.input.length/q1.workers.length;
		if (id==q1.optimisticThr && pf!=q1.inputLength) pf++;
		foundFloat=false;
		t=_t;
		dfa=q1.dfa;
		branchParses=new ArrayList<ArrayList<Character>>();
		boundaryWords=new ArrayList<ArrayList<Character>>();
		branchEndStates = new int[5];
		for (int i=0; i<5; i++) {
			branchParses.add(new ArrayList<Character>());
			boundaryWords.add(new ArrayList<Character>());
			branchEndStates[i]=0;
		}
	}

	@Override
	public void run() {
		if (t==Type.normal) {
			SpecialParse(0);
		}
		else {
			ComputePossibleBranches();
		}
		//PrintBranches();
	}
	
	//produces the logical parsings depending on the first character of the segment
	private void ComputePossibleBranches() {
		if (Character.compare(q1.input[pi], 'a')==0) {
			//parse from 0
			SpecialParse(0);
		}
		else if (Character.compare(q1.input[pi], '.')==0) {
			//parse from 0, 1, 2
			SpecialParse(0);
			SpecialParse(1);
			SpecialParse(2);
		}
		else if (Character.compare(q1.input[pi], '0')==0) {
			//parse from 0, 2, 3, 4
			SpecialParse(0);
			SpecialParse(2);
			SpecialParse(3);
			SpecialParse(4);
		}
		else if (Character.toString(q1.input[pi]).matches("[1-9]")) {
			//parse from 0, 2, 3, 4
			SpecialParse(0);
			SpecialParse(2);
			SpecialParse(3);
			SpecialParse(4);
		}
	}
	
	/*
	 * runs the dfa from the specified and produces a parse branch
	 */
	private void SpecialParse(int s0) {
		curState=s0;
		int pk=pi;
		foundFloat=false;
		ArrayList<Character> word = new ArrayList<Character>();
		
		while (pk<pf && pk<q1.input.length) {
			//get the next state by running the dfa on the current character
			curState=dfa.states.get(curState).GetNewState(q1.input[pk]);
			
			//test if the current state is an accept state
			if (curState!=-1 && dfa.states.get(curState).t==StateType.accept) foundFloat=true;
			
			//curState = -1 means we reached a dead state
			if (curState==-1) {
				
				//if we are reading the first illegal char after a valid float then save the end of the float
				if (foundFloat==true) {
					word.clear();
				}
				
				//replace all character up the last validated number with an underscore
				int _rollbackLength=word.size();
				while (_rollbackLength-->0) {
					branchParses.get(s0).remove(branchParses.get(s0).size()-1);
				}
				while (word.size()>0) {
					branchParses.get(s0).add('_');
					word.remove(word.size()-1);
				}				
				if (Character.toString(q1.input[pk]).matches("[0-9]")==false) branchParses.get(s0).add('_');
				else {
					branchParses.get(s0).add(q1.input[pk]);
					word.add(q1.input[pk]);
				}
				
				//reset flags
				foundFloat=false;
				if (Character.compare(q1.input[pk], '0')==0) curState=1;
				else if (Character.toString(q1.input[pk]).matches("[1-9]")==true) curState=2;
				else curState=0;
			}
			else {
				//if we haven't reached a dead state add the char to the parse string
				branchParses.get(s0).add(q1.input[pk]);
				word.add(q1.input[pk]);
			}
			
			pk++;
		}
		
		//save the end state of the parse
		branchEndStates[s0]=curState;
		boundaryWords.set(s0, word);
	}	

}

/*
 * class that serves as the basis for the specific worker implementations
 */
class GenericWorker{
	public int id;
	
	public int p0;
	public int pf;
	public int pi;
	
	public DFA dfa;
	public int curState;
	public ArrayList<ArrayList<Character>> branchParses; 
	public int[] branchEndStates;
	public ArrayList<ArrayList<Character>> boundaryWords;
}

/*
 * class that allows to build and simulate a dfa
 */
class DFA {
	public ArrayList<DFAstate> states;
	
	DFA() {
		states = new ArrayList<DFAstate>();
	}
	
	public void AddNewstate(int _id, StateType _t) {
		states.add(new DFAstate(_id, _t));
	}
}

class DFAstate {
	public int stateID;
	public StateType t;
	
	public ArrayList<DFAtransition> transitions;
	
	DFAstate(int _id, StateType _t) {
		stateID=_id; t=_t;
		transitions = new ArrayList<DFAtransition>();
	}

	public void AddTransition(int _o, int _d, String _c) {
		transitions.add(new DFAtransition(_o, _d, _c));
	}
	
	public int GetNewState(char _c) {
		int n=-1;
		for (DFAtransition t : transitions) {
			n = t.GetNewState(_c);
			if (n!=-1) return n;
		}
		return -1;
	}
	
	public boolean IsAccept() {
		if (t==StateType.accept) return true;
		else return false;
	}
	
	public String toString() {
		return "State: "+stateID+"\nType: "+t; 
	}
}

class DFAtransition {
	public int ori;
	public int dest;
	public char[] chars;
	
	DFAtransition(int _o, int _d, String _c) {
		ori=_o; dest=_d;
		
		chars = _c.toCharArray();
	}
	
	public int GetNewState(char _c) {
		for (int i=0; i<chars.length; i++) {
			if (Character.compare(chars[i], _c)==0) return dest;
		}
		return -1;
	}
	
	public String toString() {
		return "Origin: "+ori+"\nDest: "+dest+"\nChars: "+String.valueOf(chars); 
	}
}

enum StateType{
	accept,
	reject
}

enum Type{
	optimistic,
	normal
}