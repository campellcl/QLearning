package util;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import skeleton.MyState;

/**
 * 
 * @author Mitch Parry
 * @version 2016-04-27
 *
 */
public abstract class QLearner extends Player
{
    protected HashMap<State, HashMap<String, Double>> q;
    protected HashMap<State, HashMap<String, Double>> n;
    private Random rand;

    /**
     * The constructor.
     * 
     * @param name
     *            the name
     */
    public QLearner(String name)
    {
        super(name);
        q = new HashMap<State, HashMap<String, Double>>();
        n = new HashMap<State, HashMap<String, Double>>();
        rand = new Random(1);
    }

    /**
     * @return Q[s,a]
     */
    public HashMap<State, HashMap<String, Double>> getQ()
    {
        return q;
    }

    /**
     * @return N[s,a]
     */
    public HashMap<State, HashMap<String, Double>> getN()
    {
        return n;
    }

    /**
     * map[state, action] = r.
     * 
     * @param map
     *            the map
     * @param s
     *            the state
     * @param a
     *            the action
     * @param newValue
     *            the new value
     */
    protected void putValue(HashMap<State, HashMap<String, Double>> map,
        State s,
        String a, double newValue)
    {
        if (!map.containsKey(s))
        {
            map.put(s, new HashMap<String, Double>());
        }
        map.get(s).put(a, newValue);
    }

    /**
     * map[state, action] += r.
     * 
     * @param map
     *            the map.
     * @param s
     *            the state.
     * @param a
     *            the action.
     * @param addedValue
     *            the new value.
     */
    protected void addValue(HashMap<State, HashMap<String, Double>> map,
        State s, String a, double addedValue)
    {
        if (!map.containsKey(s))
        {
            map.put(s, new HashMap<String, Double>());
        }
        double currentValue = 0.0;
        if (map.get(s).containsKey(a))
        {
            currentValue = map.get(s).get(a);
        }
        map.get(s).put(a, currentValue + addedValue);
    }

    /**
     * Return map[state, action].
     * 
     * @param map
     *            the map.
     * @param s
     *            the state.
     * @param a
     *            the action.
     * @return map[state, action]
     */
    protected double value(HashMap<State, HashMap<String, Double>> map,
        State s, String a)
    {
        double v = 0.0;
        if (map.containsKey(s) && map.get(s).containsKey(a))
        {
            v = map.get(s).get(a);
        }
        return v;
    }

    /**
     * Get the utility (value) of the state.
     * 
     * @param state
     *            the state
     * @param actions
     *            the list of actions
     * @return the max_a(utility(state,a))
     */
    protected double maxValue(State state, List<String> actions)
    {
        double maxValue = Double.NEGATIVE_INFINITY;
        for (String action : actions)
        {
            double v = value(q, state, action);
            if (v > maxValue)
            {
                maxValue = v;
            }
        }
        return maxValue;
    }

    /**
     * Return the action with maximum exploration function.
     * 
     * @param state
     *            the state
     * @param actions
     *            the list of actions
     * @return the action with highest exploration function, ties are broken randomly
     */
    protected String maxExplorationAction(State state, List<String> actions)
    {
        String maxAction = null;
        double maxF = Double.NEGATIVE_INFINITY;
        double ties = 1.0;
        for (String action : actions)
        {
            double f = explorationFunction(state, action);
            if (f == maxF)
            {
                ties += 1.0;
                if (rand.nextDouble() < (1.0 / ties)) {
                    maxF = f;
                    maxAction = action;
                }
            }
            else if (f > maxF)
            {
                maxF = f;
                maxAction = action;
                ties = 1.0;
            }
        }
        return maxAction;
    }

    /**
     * Get the action with maximum utility for this state.
     * 
     * @param state
     *            the state
     * @param actions
     *            the list of actions
     * @return the action with maximum utility in this state.
     */
    protected String maxAction(State state, List<String> actions)
    {
        String maxAction = null;
        double max = Double.NEGATIVE_INFINITY;
        for (String action : actions)
        {
            double qsa = value(q, state, action);
            if (qsa > max)
            {
                max = qsa;
                maxAction = action;
            }
        }
        return maxAction;
    }

    /**
     * Return utility, U[s], for each grid cell.
     * 
     * @param mdp
     *            the MDP.
     * @return U[s]
     */
    protected HashMap<GridCell, Double> getUtility(MarkovDecisionProcess mdp)
    {
        HashMap<GridCell, Double> value = new HashMap<GridCell, Double>();
        for (GridCell cell : mdp.getStates())
        {
            State state = new MyState(new Percept(mdp, cell, 0));
            value.put(cell, maxValue(state, mdp.getActions()));
        }
        return value;
    }

    /**
     * Return Pi[s] for each grid cell.
     * 
     * @param mdp
     *            the MDP
     * @return Pi[s]
     */
    protected HashMap<GridCell, String> getPolicy(MarkovDecisionProcess mdp)
    {
        HashMap<GridCell, String> value = new HashMap<GridCell, String>();
        for (GridCell cell : mdp.getStates())
        {
            MyState state = new MyState(new Percept(mdp, cell, 0));
            value.put(cell, maxAction(state, mdp.getActions()));
        }
        return value;
    }

    /**
     * Return N[s] for each grid cell.
     * 
     * @param mdp
     *            the MDP
     * @return N[s]
     */
    protected HashMap<GridCell, Double> getN(MarkovDecisionProcess mdp)
    {
        HashMap<GridCell, Double> value = new HashMap<GridCell, Double>();
        for (GridCell cell : mdp.getStates())
        {
            MyState state = new MyState(new Percept(mdp, cell, 0));
            String action = maxAction(state, mdp.getActions());
            value.put(cell, value(n, state, action));
        }
        return value;
    }

    /**
     * Returns U[s] for each state.
     * 
     * @param actions
     *            list of actions
     * @return U[s]
     */
    protected HashMap<State, Double> getUtility(List<String> actions)
    {
        HashMap<State, Double> value = new HashMap<State, Double>();
        for (State state : q.keySet())
        {
            value.put(state, this.maxValue(state, actions));
        }
        return value;
    }

    /**
     * Return policy, Pi[s] for each state.
     * 
     * @param actions
     *            list of actions
     * @return Pi[s]
     */
    protected HashMap<State, String> getPolicy(List<String> actions)
    {
        HashMap<State, String> value = new HashMap<State, String>();
        for (State state : q.keySet())
        {
            value.put(state, this.maxAction(state, actions));
        }
        return value;
    }

    /**
     * Display maps for the policy Pi[s], utility U[s], and N[s,a] for each grid
     * cell in the grid world.
     * 
     * @param mdp
     *            the MDP
     */
    public void displayStatMaps(MarkovDecisionProcess mdp)
    {
        System.out.println("Q-Player Current Policy:");
        GridWorld.display(mdp, getPolicy(mdp));
        System.out.println("Q-Player Current Utility:");
        GridWorld.display(mdp, getUtility(mdp));
        System.out.println("Q-Player States Visited:");
        GridWorld.display(mdp, getN(mdp));
    }

    /**
     * Display the statistics for Q[s,a] and N[s,a].
     * 
     * @param mdp
     *            the MDP
     */
    public void displayStats(MarkovDecisionProcess mdp)
    {
        HashMap<State, String> policy = getPolicy(mdp.getActions());
        HashMap<State, HashMap<String, Double>> q = getQ();
        HashMap<State, HashMap<String, Double>> n = getN();
        for (State s : policy.keySet())
        {
            for (String a : mdp.getActions())
            {
                System.out.printf("%s,%s: %.2f / %.0f\n", s, a,
                    value(q, s, a), value(n, s, a));
            }
            System.out.println("Take action " + policy.get(s));
            s.display();
        }
        System.out.printf("%d states\n\n", policy.size());
    }

    /**
     * The exploration function.
     * 
     * @param state
     *            the state
     * @param action
     *            the action
     * @return the exploration function.
     */
    protected abstract double explorationFunction(State state, String action);

}
