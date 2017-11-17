package util;


/**
 * Abstract player class.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public abstract class Player
{
    private String name;

    /**
     * Constructor takes the name.
     * 
     * @param name
     *            the name of the player.
     */
    public Player(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Player [name=" + name + "]";
    }

    /**
     * Returns the desired action for the current state of the MDP.
     * 
     * @param percept
     *            the percept.
     * @return the desired action.
     */
    public abstract String play(Percept percept);
}
