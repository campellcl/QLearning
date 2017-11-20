package skeleton;

import util.GridCell;
import util.Percept;
import util.State;

/**
 * The state encodes sufficient information for your Q-Learner to choose a good
 * action.
 *
 * @author Mitch Parry
 * @version 2016-04-26
 */
public class MyState extends State
{
    private String north;
    private String east;
    private String south;
    private String west;
    private String current;

    /**
     * Use the percept information to create a state.
     * 
     * @param p
     *            the percept
     */
    public MyState(Percept p)
    {
        super(p);
        int c = Percept.NEIGHBORHOOD_SIZE / 2;
        GridCell[][] neighborhood = p.neighborhood();
        north = getType(neighborhood[c - 1][c]);
        east = getType(neighborhood[c][c + 1]);
        south = getType(neighborhood[c + 1][c]);
        west = getType(neighborhood[c][c - 1]);
        current = getType(neighborhood[c][c]);
        if (current != " ")
        {
            north = east = south = west = current;
        }
    }

    /**
     * Encode the type of a grid cell.
     * 
     * @param cell
     *            the grid cell.
     * @return an integer code for the cell type.
     */
    public String getType(GridCell cell)
    {
        if (cell == null)
        {
            return "X";
        }
        else if (cell.isGoal())
        {
            return "+";
        }
        else if (cell.isHole())
        {
            return "-";
        }
        else if (cell.isNormal())
        {
            return " ";
        }
        else
        {
            return "O";
        }
    }

    /**
     * @return true if the state is terminal.
     */
    public boolean isTerminal()
    {
        return terminal;
    }

    @Override
    public void display()
    {
        System.out.println("?|" + north + "|?");
        System.out.println(west + "|" + current + "|" + east);
        System.out.println("?|" + south + "|?");
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((current == null) ? 0 : current.hashCode());
        result = PRIME * result + ((east == null) ? 0 : east.hashCode());
        result = PRIME * result + ((north == null) ? 0 : north.hashCode());
        result = PRIME * result + ((south == null) ? 0 : south.hashCode());
        result = PRIME * result + ((west == null) ? 0 : west.hashCode());
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
        MyState other = (MyState) obj;
        if (current == null)
        {
            if (other.current != null)
            {
                return false;
            }
        }
        else if (!current.equals(other.current))
        {
            return false;
        }
        if (east == null)
        {
            if (other.east != null)
            {
                return false;
            }
        }
        else if (!east.equals(other.east))
        {
            return false;
        }
        if (north == null)
        {
            if (other.north != null)
            {
                return false;
            }
        }
        else if (!north.equals(other.north))
        {
            return false;
        }
        if (south == null)
        {
            if (other.south != null)
            {
                return false;
            }
        }
        else if (!south.equals(other.south))
        {
            return false;
        }
        if (west == null)
        {
            if (other.west != null)
            {
                return false;
            }
        }
        else if (!west.equals(other.west))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "" + north + "," + east + "," + south + "," + west + ","
            + current;
    }

}
