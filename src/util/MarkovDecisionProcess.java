package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Represents a Markov Decision Process.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public class MarkovDecisionProcess implements Cloneable
{
    private ArrayList<GridCell> states;
    private ArrayList<String> actions;
    private GridCell currentState;
    // state transition matrix
    private double[][][] transProbs;
    private double gamma;
    private double[][] actionUncertainty;
    private Random rand;

    /**
     * Constructor reads the parameters from a scanner.
     * 
     * @param scan
     *            the scanner.
     */
    public MarkovDecisionProcess(Scanner scan)
    {
        this.rand = new Random();
        initialize(scan);
    }

    /**
     * Constructor reads the parameters from a scanner.
     * 
     * @param scan
     *            the scanner.
     * @param seed
     *            the seed for random number generator
     */
    public MarkovDecisionProcess(Scanner scan, Long seed)
    {
        this.rand = new Random(seed);
        initialize(scan);
    }

    /**
     * Initialize the MDP with the scanner.
     * 
     * @param scan
     *            the scanner
     */
    public void initialize(Scanner scan)
    {
        // read and create the states with their rewards.
        readStates(scan);
        // read the actions and their transition probabilities
        readActions(scan);
        // Fill the state transition matrix
        readTransitions(scan);
        gamma = scan.nextDouble();
        currentState = states.get(states.indexOf(new GridCell(scan.next(), 0)));
    }

    /**
     * Helper method to read the transitions from the scanner.
     * 
     * @param scan
     *            the scanner
     */
    private void readTransitions(Scanner scan)
    {
        int numStates = states.size();
        int numActions = actions.size();
        transProbs = new double[numStates][numActions][numStates];
        int numTransitions = scan.nextInt();
        for (int i = 0; i < numTransitions; i++)
        {
            int s0 = states.indexOf(new GridCell(scan.next(), 0));
            int a = actions.indexOf(scan.next());
            int s1 = states.indexOf(new GridCell(scan.next(), 0));
            for (int j = 0; j < numActions; j++)
            {
                transProbs[s0][j][s1] += actionUncertainty[j][a];
            }
        }
    }

    /**
     * Helper method to read the actions from the scanner.
     * 
     * @param scan
     *            the scanner.
     */
    private void readActions(Scanner scan)
    {
        int numActions = scan.nextInt();
        actions = new ArrayList<String>(numActions);
        actionUncertainty = new double[numActions][numActions];
        for (int i = 0; i < numActions; i++)
        {
            actions.add(scan.next());
            for (int j = 0; j < numActions; j++)
            {
                actionUncertainty[i][j] = scan.nextDouble();
            }
        }
    }

    /**
     * Helper method to read the states.
     * 
     * @param scan
     *            the scanner to read.
     */
    private void readStates(Scanner scan)
    {
        int numStates = scan.nextInt();
        states = new ArrayList<GridCell>(numStates);
        for (int i = 0; i < numStates; i++)
        {
            states.add(new GridCell(scan.next(), scan.nextInt()));
        }
        // read the goal states
        while (!scan.hasNextInt())
        {
            GridCell s =
                states.get(states.indexOf(new GridCell(scan.next(), 0)));
            s.setTerminal();
        }
    }

    /**
     * Sets the current state.
     * 
     * @param s
     *            the current state.
     */
    public void setCurrent(GridCell s)
    {
        currentState = s;
    }

    /**
     * @return the current state.
     */
    public GridCell getCurrent()
    {
        return (GridCell) currentState.clone();
    }

    /**
     * @return all the states.
     */
    public List<GridCell> getStates()
    {
        ArrayList<GridCell> copy = new ArrayList<GridCell>(states.size());
        for (GridCell s : states)
        {
            copy.add((GridCell) s.clone());
        }
        return copy;
    }

    /**
     * @return all the actions.
     */
    public List<String> getActions()
    {
        ArrayList<String> copy = new ArrayList<String>(actions.size());
        for (String s : actions)
        {
            copy.add(s);
        }
        return copy;
    }

    /**
     * @return the discount rate.
     */
    public double getGamma()
    {
        return gamma;
    }

    /**
     * Performs the desired action.
     * 
     * @param action
     *            the action.
     */
    public void takeAction(String action)
    {
        // System.out.println("MDP: moving " + actions.get(actionIndex));
        double r = rand.nextDouble();
        double sum = 0.0;
        GridCell newState = null;
        for (int i = 0; i < states.size(); i++)
        {
            newState = states.get(i);
            sum += transProb(currentState, action, newState);
            if (sum >= r)
            {
                break;
            }
        }
        currentState = newState;
    }

    @Override
    protected Object clone()
    {
        MarkovDecisionProcess answer = null;
        try
        {
            answer = (MarkovDecisionProcess) super.clone();
            answer.states = new ArrayList<GridCell>(states.size());
            for (GridCell s : states)
            {
                answer.states.add((GridCell) s.clone());
            }
            answer.actions = new ArrayList<String>(actions);

            answer.transProbs = transProbs.clone();
            for (int i = 0; i < answer.transProbs.length; i++)
            {
                answer.transProbs[i] = transProbs[i].clone();
            }
            for (int i = 0; i < answer.transProbs.length; i++)
            {
                for (int j = 0; j < answer.transProbs[i].length; j++)
                {
                    answer.transProbs[i][j] = transProbs[i][j].clone();
                }
            }
        }
        catch (CloneNotSupportedException e)
        {
            System.exit(-1);
        }
        return answer;
    }

    /**
     * Returns P(s2|s1,a).
     * 
     * @param s1
     *            Initial state.
     * @param a
     *            Action performed.
     * @param s2
     *            Final state.
     * @return P(s2|s1,a)
     */
    public double transProb(GridCell s1, String a, GridCell s2)
    {
        return transProbs[states.indexOf(s1)][actions.indexOf(a)][states
            .indexOf(s2)];
    }

}
