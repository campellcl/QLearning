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
    private static final boolean DEBUG = true;
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
    	Double Qsa = this.value(q, state, action);
    	Double Nsa = this.value(n, state, action);
    	if (Qsa == null || Nsa < this.NE) {
    		return Rplus;
    	}
    	return Qsa;
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
    	double alpha = 1.0/NE;
    	// state s' is the current state:
    	State sPrime = new MyState(percept);
    	// reward r' is the current reward signal:
    	double rPrime = percept.score();
    	double gamma = percept.gamma();
    	// if TERMINAL?(s') then Q[s',None] <- r'
    	if (sPrime.isTerminal()) {
    		this.putValue(q, sPrime, null, rPrime);
    	}
    	if (s != null) {
    		// s is not null, increment N[s,a]:
    		this.addValue(n, s, a, 1.0);
    		// get Q[s,a]:
    		Double Q_sa = this.value(q, s, a);
    		// get Q[s',a']
    		Double QPrime_sa = this.value(q, sPrime, this.maxAction(sPrime, percept.actions()));
    		// get Q[s',a']-Q[s,a]:
    		Double deltaQUtil = QPrime_sa - Q_sa;
    		// update the deltaQUtil
    		deltaQUtil = (r + (gamma * deltaQUtil));
    		this.putValue(q, s, a, 
    				((alpha * this.value(n, s, a))*deltaQUtil));
  
    	}
    	s = sPrime;
    	a = this.maxExplorationAction(sPrime, percept.actions());
    	// source: https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/learning/reinforcement/agent/QLearningAgent.java    	
    	r = rPrime;
    	//System.out.println(System.identityHashCode(sPrime));
    	//System.out.printf("N: %s\n", this.n);
    	//System.out.printf("Q: %s\n", this.q);
    	//System.out.printf("Q: %s \n N: %s",this.q, this.n);
    	return a;
        
    }

}
