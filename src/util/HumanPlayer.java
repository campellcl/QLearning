package util;

import java.util.List;
import java.util.Scanner;

/**
 * A sample human player so that the game can be played manually.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public class HumanPlayer extends Player
{

    public static Scanner scan = new Scanner(System.in);

    /**
     * Constructor simply takes a name for the player.
     * 
     * @param name
     *            the name of the player.
     */
    public HumanPlayer(String name)
    {
        super(name);
    }

    /**
     * Display the Grid World with statistics from Q[s,a], N[s,a], and
     * Utility[s,a].
     * 
     * @param mdp
     *            the MDP.
     */
    public void displayStatMaps(MarkovDecisionProcess mdp)
    {

    }

    /**
     * Display stats for the player.
     * 
     * @param mdp
     *            the MDP.
     */
    public void displayStats(MarkovDecisionProcess mdp)
    {

    }

    /**
     * Returns the desired action for the current state of the MDP.
     * 
     * @param percept
     *            The percept.
     * @return the next desired action.
     */
    public String play(Percept percept)
    {
        // return the index of the action that you want to take in the current
        // state
        List<String> actions = percept.actions();
        int index = -1;
        String command = null;
        GridWorld.display(percept.neighborhood(),
            percept.current());
        System.out.printf("Current score: %.1f\n", percept.score());
        while (index < 0)
        {
            System.out.println(actions);
            System.out.println("Enter choice: ");
            command = scan.next().toUpperCase();
            index = actions.indexOf(command);
            if (index < 0 && command.toLowerCase().equals("quit"))
            {
                break;
            }
        }
        return command;
    }

}
