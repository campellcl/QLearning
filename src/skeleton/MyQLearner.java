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
    private static final double Rplus = 0.0;
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
    	HashMap<String,Double> actionQUtilPair = this.q.get(state);
    	HashMap<String,Double> actionCountPair = this.n.get(state);
    	if (actionQUtilPair == null || actionCountPair == null) {
    		return Double.NEGATIVE_INFINITY;
    	}
    	// u is the currently estimated utility (QUtility) of state s'. 
    	double u = actionQUtilPair.get(action);
    	// TODO: u could be null (but doubles can't be). 
    	// n is the number of times the state-action pair [s',a'] has been encountered.
    	double n = actionCountPair.get(action);
    	if (actionQUtilPair.get(action) == null || n < NE) {
    		return Rplus;
    	}
    	return u;
        // return Double.NEGATIVE_INFINITY;
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
    		Pair<State, String> sPrimeAPrime = new Pair<State, String>(sPrime, aPrime);
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
    	HashMap<String,Double> Nsa = n.get(s);
    	// Q[s,a]
    	HashMap<String,Double> Qsa = q.get(s);
    	// Q[s',a']:
    	HashMap<String,Double> NsaPrime = n.get(sPrime);
    	// if TERMINAL?(s') then Q[s',None] <- r'
    	if (sPrime.isTerminal()) {
    		HashMap<String,Double> actionRewardPair = new HashMap<String,Double>();
    		actionRewardPair.put(null, rPrime);
    		this.q.put(s, actionRewardPair);
    	}
    	if (s != null) {
    		// s is not null, increment N[s,a]:
    		HashMap<String,Double> actionCountPair = this.n.get(s);
    		if (actionCountPair == null) {
    			// The previous state s was never encountered in the frequency table (add it with one observance):
    			actionCountPair = new HashMap<String,Double>();
    			actionCountPair.put(a, 0.0);
    		}
    		double numTimesVisited = actionCountPair.get(a);
    		// increment the observed count by one and reinsert into N:
    		actionCountPair.put(a, ++numTimesVisited);
    		this.n.put(s, actionCountPair);
    		// get Q[s,a]:
    		HashMap<String,Double> actionQUtilPair = this.q.get(s);
    		if (actionQUtilPair == null) {
    			// this Q[s,a] does not exist, init to zero:
    			actionQUtilPair = new HashMap<String,Double>();
    			actionQUtilPair.put(a, 0.0);
    		}
    		// calculate alpha*(N[s,a]) recall this impacts the temporal importance of observed Q utilities:
    		double updateConstraint = alpha*(numTimesVisited);
    		// compute for every action aPrime: Q[s',a'] - Q[s,a]
    		HashMap<String,Double> actionPrimeDeltaQUtilPair = new HashMap<String,Double>();
    		for (String aPrime : percept.actions()) {
    			// Q[s',a']-Q[s,a]:
    			HashMap<String,Double> aPrimeQUtilPair;
    			aPrimeQUtilPair = this.q.get(sPrime);
    			if (aPrimeQUtilPair == null) {
    				// Q[s',a'] does not exist yet, init to zero:
    				aPrimeQUtilPair = new HashMap<String,Double>();
    				aPrimeQUtilPair.put(aPrime, 0.0);
    			}
    			double aPrimeQUtil = aPrimeQUtilPair.get(aPrime);
    			actionPrimeDeltaQUtilPair.put(aPrime, (aPrimeQUtil - actionQUtilPair.get(a)));
    		}
    		// choose the action aPrime that maximizes Q[s',a'] - Q[s,a]:
    		double maxDeltaQUtil = Integer.MIN_VALUE;
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
    	// source: https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/learning/reinforcement/agent/QLearningAgent.java    	
    	a = argmaxAPrime(sPrime, percept.actions());
    	r = rPrime;
        return a;
    }

}
