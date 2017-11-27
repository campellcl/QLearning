package util;

import java.util.Scanner;

import skeleton.MyQLearner;

/**
 * The game class uses an MDP to explore a GridWorld.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public class Game
{
    private MarkovDecisionProcess mdp;
    private Player player;

    /**
     * Constructor initializes the MDP and player.
     * 
     * @param mdp
     *            the MDP
     * @param player
     *            the player
     */
    public Game(MarkovDecisionProcess mdp, Player player)
    {
        this.mdp = mdp;
        this.player = player;
    }

    /**
     * Plays the game by repeatedly querying the player for an action until it
     * reaches a terminal state, accumulating points along the way.
     * 
     * @return the final score for the game.
     */
    public double play()
    {
        GridCell current = mdp.getCurrent();
        double score = current.reward();
        int t = 1;
        while (true)
        {
            String action = player.play(new Percept(mdp, current, score));
            if (mdp.getCurrent().isTerminal())
            {
                break;
            }
            if (!mdp.getActions().contains(action))
            {
                continue;
            }
            mdp.takeAction(action);
            current = mdp.getCurrent();
            score += Math.pow(mdp.getGamma(), t) * current.reward();
            t++;
        }
        return score;
    }

    /**
     * This is the main game program with a human player. Run this to try out a
     * random GridWorld game.
     * 
     * @param args
     *            not used.
     */
    public static void main(String[] args)
    {
        final int NUM_TRIALS = 1000000;
        //final int NUM_TRIALS = 1000;
    	final int DISPLAY_EVERY = 1000;
        //final int DISPLAY_EVERY = 100;
        String world =
            GridWorld.createRandomGridWorld(10, 10, 0, 2, 10, 1, 1.0, 1L);
        MarkovDecisionProcess mdp =
            new MarkovDecisionProcess(new Scanner(world), 1L);

        GridWorld.display(mdp, null);
        GridCell start = mdp.getCurrent();
        
        double Ne = 100.0;
        double Rplus = 0.0;
        QLearner player = new MyQLearner("Q-Learner");
        // Player player = new HumanPlayer("Human");

        double score = 0.0;
        for (int i = 1; i <= NUM_TRIALS; i++)
        {
        	//System.out.printf("Playing Game: %d\n", i);
            mdp.setCurrent(start);
            Game game = new Game(mdp, player);
            score += (game.play() - score) / i;
            if (i % DISPLAY_EVERY == 0)
            {
                // player.displayStatMaps(mdp);
                System.out.printf("\t\t\t%.1f%%: Recent Average Score: %.2f\n",
                    100.0 * i / NUM_TRIALS, score);
            }
        }
        player.displayStats(mdp);
    }
}
