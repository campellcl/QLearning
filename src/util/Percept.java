package util;
import java.util.List;

/**
 * The percept comprises information about the game the player can currently
 * perceive.
 * 
 * @author Mitch Parry
 * @version 2016-04-25
 * 
 */
public class Percept
{
    public static final int NEIGHBORHOOD_SIZE = 5;
    private GridCell[][] neighborhood;
    private List<String> actions;
    private double gamma;
    private double score;

    /**
     * Constructor for a percept.
     * 
     * @param mdp
     *            the MDP.
     * @param current
     *            the current grid cell.
     * @param score
     *            the current score.
     */
    public Percept(MarkovDecisionProcess mdp, GridCell current, double score)
    {
        actions = mdp.getActions();
        gamma = mdp.getGamma();
        this.score = score;
        neighborhood = new GridCell[NEIGHBORHOOD_SIZE][NEIGHBORHOOD_SIZE];

        String name = current.name();
        int row = GridWorld.nameToRow(name) - NEIGHBORHOOD_SIZE / 2;
        int col = GridWorld.nameToCol(name) - NEIGHBORHOOD_SIZE / 2;

        for (int i = 0; i < NEIGHBORHOOD_SIZE; i++)
        {
            int r = row + i;
            for (int j = 0; j < NEIGHBORHOOD_SIZE; j++)
            {
                int c = col + j;
                String targetName = GridWorld.rowColToName(r, c);
                int index =
                    mdp.getStates().indexOf(new GridCell(targetName, 0));
                if (index >= 0)
                {
                    neighborhood[i][j] = mdp.getStates().get(index);
                }
            }
        }
    }

    /**
     * @return the possible actions.
     */
    public List<String> actions()
    {
        return actions;
    }

    /**
     * @return the neighborhood of grid cells.
     */
    public GridCell[][] neighborhood()
    {
        return neighborhood;
    }

    /**
     * @return the current grid cell in the neighborhood.
     */
    public GridCell current()
    {
        return neighborhood[NEIGHBORHOOD_SIZE / 2][NEIGHBORHOOD_SIZE / 2];
    }

    /**
     * @return gamma.
     */
    public double gamma()
    {
        return gamma;
    }

    /**
     * @return the score.
     */
    public double score()
    {
        return score;
    }
}
