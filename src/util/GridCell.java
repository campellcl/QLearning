package util;

/**
 * Represents a state in the MDP.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public class GridCell implements Cloneable
{
    private double reward;
    private String name;
    private boolean terminal;
    private boolean start;

    /**
     * Constructor takes a name and a reward.
     * 
     * @param n
     *            the name
     * @param r
     *            the reward
     */
    public GridCell(String n, double r)
    {
        name = n;
        reward = r;
    }

    /**
     * @return the reward for this state.
     */
    public double reward()
    {
        return reward;
    }

    /**
     * @return the name for this state.
     */
    public String name()
    {
        return name;
    }

    /**
     * Sets this state to be a terminal state.
     */
    public void setTerminal()
    {
        terminal = true;
    }

    /**
     * Sets this state to be the start state.
     */
    public void setStart()
    {
        start = true;
    }

    @Override
    protected Object clone()
    {
        // TODO Auto-generated method stub
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * @return true if this state is a terminal state.
     */
    public boolean isTerminal()
    {
        return terminal;
    }

    /**
     * @return true if this state is the start state.
     */
    public boolean isStart()
    {
        return start;
    }

    @Override
    public String toString()
    {
        return name + "(" + reward + ")";
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        GridCell other = (GridCell) obj;
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    /**
     * @return is this cell is a goal?
     */
    public boolean isGoal()
    {
        return reward == GridWorld.R_GOAL;
    }

    /**
     * @return is this cell is a hole?
     */
    public boolean isHole()
    {
        return reward == GridWorld.R_HOLE;
    }

    /**
     * @return is this cell is normal?
     */
    public boolean isNormal()
    {
        return reward == GridWorld.R_CELL;
    }

}
