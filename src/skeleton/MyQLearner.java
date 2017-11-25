package skeleton;

import util.GridCell;
import util.Percept;
import util.QLearner;
import util.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

/**
 * An agent that uses value iteration to play the game.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public class MyQLearner extends QLearner
{
    private static final boolean DEBUG = false;
    // NE is a fixed parameter for use in the method explorationFunction. 
    private static final double NE = 100.0;
    // Rplus (R+) is an optimistic estimate of the best possible reward obtainable in any state, which is used in the method explorationFunction.
    private static final double Rplus = Double.NEGATIVE_INFINITY;
    private State s;
    private String a;
    private double r;

    /**
     * The constructor takes the name.
     * 
     * @param name
     *            the name of the player.
     */
    public MyQLearner(String name)
    {
    	//TODO: Create custom HashMap class and implement getDefault()
        super(name);
        s = null;
        a = null;
        r = Double.NEGATIVE_INFINITY;
    }
    
    /**
     * explorationFunction - The exploration function f(u, n) determines how greed (preferences for high values of u)
     * 	is traded off against curiosity (preferences for actions that have not been tried often and have low n). The 
     * 	function f(u, n) should be increasing in u and decreasing in n. 
     * @source source - https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/learning/reinforcement/agent/QLearningAgent.java
     * @param state - The state s' for which the exploration value is to be calculated for. 
     * @param action - The action a' for which the exploration value is to be calculated for. 
     * @return explorationValue - The estimated utility for exploring state s' with action a'. 
     */
    @Override
    protected double explorationFunction(State state, String action)
    {
    	// The utility of being in state s' and executing action a' is Q[s',a']:
    	HashMap<String,Double> QsaPrime = this.q.get(state);
    	// The number of times the state action pair s','a has been encountered is N[s',a']
    	HashMap<String,Double> NsaPrime = this.n.get(state);
    	// u is the currently estimated utility (QUtility) of state s'. 
    	double u;
    	try {
    		u = QsaPrime.get(action);
    	} catch (NullPointerException npe) {
    		// u == null
    		return Rplus;
    	}
    	// n is the number of times the state-action pair [s',a'] has been encountered.
    	double n;
    	try {
    		n = NsaPrime.get(action);
    	} catch (NullPointerException npe) {
    		return Rplus;
    	}
    	// if n < NE:
    	if (n < NE) {
    		return Rplus;
    	} else {
    		return u;
    	}
    }
    
    /**
     * argmaxAPrime - Returns the action that maximizes the explorationValue (function f).
     * @param sPrime - The current state s'.
     * @param aPrimes - The list of actions available in the current state a'.
     * @return action - The action that maximizes the exploration function f(action in {a'}).
     */
    private String argmaxAPrime(State sPrime, List<String> aPrimes) {
    	String action = "N";
    	double maxExplorationValue = Double.NEGATIVE_INFINITY;
    	for (String aPrime : aPrimes) {
    		// get the count N[s',a']:
    		double explorationValue = explorationFunction(sPrime, aPrime);
    		if (explorationValue > maxExplorationValue) {
    			maxExplorationValue = explorationValue;
    			action = aPrime;
    		}
    	}
    	return action;
    }
    
    /**
     * Plays the game using a Q-Learning agent.
     * 
     * @param percept
     *            the percept.
     * @return the desired action.
     */
    public String play(Percept percept)
    {
    	//define the constant alpha (impacting the temporal importance of observed Q utilities):
    	//TODO Dynamically get the number of states in the world (it happens to be 100 in this example (10x10)):
    	double alpha = 1/NE;
    	// state s' is the current state:
    	State sPrime = new MyState(percept);
    	// reward r' is the current reward signal:
    	double rPrime = percept.score();
    	// N[s,a]
    	// HashMap<State,HashMap<String,Double>> Nsa = getN();
    	// N[s',a']
    	// HashMap<String,Double> NsaPrime = Nsa.get(sPrime);
    	// Q[s,a]
    	// HashMap<State,HashMap<String,Double>> Qsa = getQ();
    	// Q[s',a']:
    	// HashMap<String,Double> QsaPrime = Qsa.get(sPrime);
    	
    	// if TERMINAL?(s') then Q[s',None] <- r'
    	if (sPrime.isTerminal()) {
    		this.putValue(q, sPrime, null, rPrime);
    	}
    	if (s != null) {
    		// s is not null, increment N[s,a]:
    		HashMap<String,Double> actionFrequencyPair;
    		double frequency = Double.MIN_VALUE;
    		try {
    			actionFrequencyPair = this.n.get(s);
    			frequency = actionFrequencyPair.get(a);
    			// increment the observed count by one and reinsert into N:
    			this.putValue(n, s, a, ++frequency);
    		} catch (NullPointerException npe) {
    			// The previous state s was never encountered in the frequency table (add it with one observance):
    			this.putValue(n, s, a, 1.0);
    		}
    		// get Q[s,a]:
    		HashMap<String,Double> actionQUtilPair = null;
    		try {
    			actionQUtilPair = this.q.get(s);
    			if (actionQUtilPair == null) {
        			// Q[s,a] does not exist yet, add it:
        			this.putValue(q, s, a, 0.0);
        			actionQUtilPair = new HashMap<String,Double>();
        			actionQUtilPair.put(a, 0.0);
    			}
    		} catch (NullPointerException npe) {
    			// Q[s,a] does not exist yet, add it:
    			this.putValue(q, s, a, 0.0);
    			actionQUtilPair = new HashMap<String,Double>();
    			actionQUtilPair.put(a, 0.0);
    		}
    		// calculate alpha*(N[s,a]) recall this impacts the temporal importance of observed Q utilities:
    		double updateConstraint = alpha*(frequency);
    		// compute for every action aPrime: Q[s',a'] - Q[s,a]
    		HashMap<String,Double> actionPrimeDeltaQUtilPair = new HashMap<String,Double>();
    		for (String aPrime : percept.actions()) {
    			// Q[s',a']-Q[s,a]:
    			HashMap<String,Double> sPrimeQUtilPair;
    			double sPrimeAPrimeUtil;
    			try {
    				// get Q[s',*]
    				sPrimeQUtilPair = this.q.get(sPrime);
    				// get Q[s',a']
    				sPrimeAPrimeUtil = sPrimeQUtilPair.get(aPrime);
    			} catch (NullPointerException npe) {
    				// Q[s',a'] = (0.0)
    				this.putValue(q, sPrime, aPrime, 0.0);
    				sPrimeAPrimeUtil = 0.0;
    			}
    			// Compute Q[s',a']-Q[s,a]
    			double deltaQUtil = (sPrimeAPrimeUtil - actionQUtilPair.get(a));
    			actionPrimeDeltaQUtilPair.put(aPrime, deltaQUtil);
    		}
    		// choose the action aPrime that maximizes Q[s',a'] - Q[s,a]:
    		double maxDeltaQUtil = Double.MIN_VALUE;
    		String bestAPrime = null;
    		for (Map.Entry<String,Double> aPrimeDeltaQUtilPair : actionPrimeDeltaQUtilPair.entrySet()) {
    			if (aPrimeDeltaQUtilPair.getValue() > maxDeltaQUtil) {
    				maxDeltaQUtil = aPrimeDeltaQUtilPair.getValue();
    				bestAPrime = aPrimeDeltaQUtilPair.getKey();
    			}
    		}
    		// get the final Q-Util of the state:
    		double qUtil = r + (percept.gamma()*maxDeltaQUtil);
    		// Caclulate and update Q[s,a]
    		// first, build the [a,QUtil] pair:
    		HashMap<String,Double> updatedQ = this.q.get(s);
    		// Get the old q value Q[s,a]:
    		double oldQUtil = actionQUtilPair.get(a);
    		// second update this value with the new QUtil:
    		updatedQ.put(a, (oldQUtil + (updateConstraint * qUtil)));
    		// perform Q[s,a]<- ...
    		this.q.put(s, updatedQ);
    	}
    	s = sPrime;
    	a = this.maxExplorationAction(sPrime, percept.actions());
    	// source: https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/learning/reinforcement/agent/QLearningAgent.java    	
    	// a = argmaxAPrime(sPrime, percept.actions());
    	r = rPrime;
        return a;
    }

}
