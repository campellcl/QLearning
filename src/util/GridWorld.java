package util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GridWorld class represents the game's grid world. It uses the state names of
 * the MDP to determine where each state is in the grid.
 * 
 * @author Mitch Parry
 * @version 2014-03-28
 * 
 */
public class GridWorld
{
    public static final int R_GOAL = 100;
    public static final int R_HOLE = -100;
    public static final int R_CELL = -3;
    private static int[][][] board;
    private static ArrayList<int[]> cells;
    private static ArrayList<int[]> openCells;
    private static ArrayList<int[]> goalCells;
    private static ArrayList<int[]> rockCells;
    private static ArrayList<int[]> tunnelCells;
    private static ArrayList<int[]> holeCells;
    private static ArrayList<int[]> startCells;
    private static Random rand = new Random();
    private static double[] directionUncertainty;
    private static char[] dir = {
        'N', 'E', 'S', 'W'
    };
    private static int w = 7;

    /**
     * Place types of cells in the grid.
     * 
     * @param rand
     *            the random number generator.
     * @param numGoals
     *            the number of goals.
     * @param numRocks
     *            the number of rocks.
     * @param numTunnels
     *            the number of tunnels.
     * @param numHoles
     *            the number of holes.
     */
    private static void mapLayout(Random rand, int numGoals, int numRocks,
        int numTunnels, int numHoles)
    {
        int numRows = board.length;
        int numCols = board[0].length;
        openCells = new ArrayList<int[]>(cells);

        // pick start cell (always southwest corner).
        startCells = new ArrayList<int[]>(1);
        startCells.add(board[numRows - 1][0]);
        openCells.remove(board[numRows - 1][0]);
        // pick goal cells
        // +100 always northeast corner.
        goalCells = new ArrayList<int[]>(numGoals);
        goalCells.add(board[0][numCols - 1]);
        openCells.remove(board[0][numCols - 1]);
        for (int i = 1; i < numGoals; i++)
        {
            goalCells.add(openCells.remove(rand.nextInt(openCells.size())));
        }
        // pick rocks
        rockCells = new ArrayList<int[]>(numRocks);
        for (int i = 0; i < numRocks; i++)
        {
            rockCells.add(openCells.remove(rand.nextInt(openCells.size())));
        }
        // pick tunnels
        tunnelCells = new ArrayList<int[]>(numTunnels);
        for (int i = 0; i < numTunnels; i++)
        {
            tunnelCells.add(openCells.remove(rand.nextInt(openCells.size())));
        }
        // pick holes
        holeCells = new ArrayList<int[]>(numHoles);
        for (int i = 0; i < numHoles; i++)
        {
            holeCells.add(openCells.remove(rand.nextInt(openCells.size())));
        }

    }

    /**
     * Helper method to create the cells in the board.
     * 
     * @param numRows
     *            the number of rows.
     * @param numCols
     *            the number of columns.
     */
    private static void createCells(int numRows, int numCols)
    {
        board = new int[numRows][numCols][];
        cells = new ArrayList<int[]>(numRows * numCols);
        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < numCols; j++)
            {
                int[] cell = {
                    i, j
                };
                cells.add(cell);
                board[i][j] = cell;
            }
        }
    }

    /**
     * Pick action uncertainty (same shape for every direction).
     */
    private static void sampleUncertainty()
    {
        directionUncertainty = new double[4];
        directionUncertainty[0] = (6.0 / 10.0) + (Math.random() * (4.0 / 10.0));
        directionUncertainty[1] = (1.0 - directionUncertainty[0]) / 2.0;
        directionUncertainty[3] = (1.0 - directionUncertainty[0]) / 2.0;
        directionUncertainty[2] = 0.0;

    }

    /**
     * @return the string representation of the states.
     */
    private static String statesToString()
    {
        // write states
        String s = cells.size() - tunnelCells.size() - rockCells.size() + "\n";
        for (int[] cell : cells)
        {
            if (!rockCells.contains(cell) && !tunnelCells.contains(cell))
            {
                if (openCells.contains(cell) || startCells.contains(cell))
                {
                    s += cellName(cell) + " " + R_CELL + "\n";
                }
                else if (holeCells.contains(cell))
                {
                    s += cellName(cell) + " " + R_HOLE + "\n";
                }
                else if (goalCells.contains(cell))
                {
                    s += cellName(cell) + " " + R_GOAL + "\n";
                }
            }
        }
        // write goal states
        for (int[] cell : holeCells)
        {
            s += cellName(cell) + " ";
        }
        for (int[] cell : goalCells)
        {
            s += cellName(cell) + " ";
        }
        return s + "\n";
    }

    /**
     * @return a string representation of the direction uncertainty.
     */
    private static String uncertaintyToString()
    {
        // write action uncertainty
        String output = dir.length + "\n";
        for (int k = 0; k < dir.length; k++)
        {
            output += dir[k] + " ";
            int j = (dir.length - k) % dir.length;
            for (int i = 0; i < directionUncertainty.length; i++)
            {
                output += directionUncertainty[j] + " ";
                j = (j + 1) % directionUncertainty.length;
            }
            output += "\n";
        }
        return output;
    }

    /**
     * Return a string representation of this path.
     * 
     * @param i
     *            row index
     * @param j
     *            column index
     * @param k
     *            action (direction) index
     * @param cell
     *            the cell
     * @return a string representation of this path.
     */
    private static String pathToString(int i, int j, int k, int[] cell)
    {
        int[] adjCell = getAdjacentCell(i, j, board, dir[k]);
        String output = cellName(cell) + " " + dir[k] + " ";
        if (adjCell == null || rockCells.contains(adjCell))
        {
            output += cellName(cell);
        }
        else if (tunnelCells.contains(adjCell))
        {
            int index = tunnelCells.indexOf(adjCell);
            int[] exit = tunnelCells.get((index + 1) % tunnelCells.size());
            int[] outCell = getAdjacentCell(exit[0], exit[1], board, dir[k]);
            if (outCell != null && !tunnelCells.contains(outCell)
                && !rockCells.contains(outCell))
            {
                output += cellName(outCell);
            }
            else
            {
                output += cellName(cell);
            }
        }
        else if (holeCells.contains(adjCell) || goalCells.contains(adjCell)
            || openCells.contains(adjCell) || startCells.contains(adjCell))
        {
            output += cellName(adjCell);
        }
        return output + "\n";
    }

    /**
     * @return a string representation of the paths.
     */
    private static String pathsToString()
    {
        String output =
            (openCells.size() + startCells.size()) * dir.length + "\n";
        for (int i = 0; i < board.length; i++)
        {
            for (int j = 0; j < board[i].length; j++)
            {
                int[] cell = board[i][j];
                if (openCells.contains(cell) || startCells.contains(cell))
                {
                    for (int k = 0; k < dir.length; k++)
                    {
                        output += pathToString(i, j, k, cell);
                    }
                }
            }
        }
        return output;

    }

    /**
     * Creates a random Grid World with the start state in the southwest corner
     * and at least one goal state in the northeast corner.
     * 
     * @param numRows
     *            the number of rows in the grid world.
     * @param numCols
     *            the number of columnss in the grid world.
     * @param numRocks
     *            the number of rocks in the grid world.
     * @param numTunnels
     *            the number of tunnels in the grid world.
     * @param numHoles
     *            the number of holes in the grid world.
     * @param numGoals
     *            the number of goals in the grid world.
     * @param gamma
     *            the discount factor for each move.
     * @param seed
     *            the random seed
     * @return the string representation of the grid world to be read by the MDP
     *         constructor.
     */
    public static String createRandomGridWorld(int numRows, int numCols,
        int numRocks, int numTunnels, int numHoles, int numGoals, double gamma,
        Long seed)
    {

        // check parameters.
        int numCells = numRows * numCols;
        if (numRocks + numTunnels + numHoles + numGoals >= numCells)
        {
            throw new IllegalArgumentException("Combined number of rocks, "
                + "tunnels, holes, and goals must be less than number of grid "
                + "locations.");
        }

        if (seed != null)
        {
            rand.setSeed(seed);
        }

        // create cells
        createCells(numRows, numCols);

        // Pick types of cells
        mapLayout(rand, numGoals, numRocks, numTunnels, numHoles);

        // pick action uncertainty (same shape for every direction)
        sampleUncertainty();

        // write states
        String output = statesToString();

        // write action uncertainty
        output += uncertaintyToString();

        // write paths
        output += pathsToString();
        // write gamma
        output += gamma + "\n";
        // write start state
        output += cellName(startCells.get(0));
        return output;
    }

    /**
     * Extracts the row index from the name of the state.
     * 
     * @param name
     *            the name of the state
     * @return the row index of the grid location for the state.
     */
    public static int nameToRow(String name)
    {
        final int NUM_LETTERS = 26;
        Pattern p = Pattern.compile("([a-z]+)[0-9]+");
        Matcher m = p.matcher(name);
        m.find();
        String rowString = m.group(1);
        int row = 0;
        int nchars = rowString.length();
        int mult = 1;
        for (int i = nchars - 1; i >= 0; i--)
        {
            row += (rowString.charAt(i) - 'a') * mult;
            mult *= NUM_LETTERS;
        }
        return row;
    }

    /**
     * Extracts the column index from the name of the state.
     * 
     * @param name
     *            the name of the state
     * @return the column index of the grid location for the state.
     */
    public static int nameToCol(String name)
    {
        Pattern p = Pattern.compile("[a-z]+([0-9]+)");
        Matcher m = p.matcher(name);
        m.find();
        return Integer.parseInt(m.group(1)) - 1;
    }

    /**
     * Constructs the state name from its grid index.
     * 
     * @param row
     *            the row index of the state.
     * @param col
     *            the column index of the state
     * @return the name of the state
     */
    public static String rowColToName(int row, int col)
    {
        final int NUM_LETTERS = 26;
        String rowString = "";

        do
        {
            rowString = (char) ('a' + (row % NUM_LETTERS)) + rowString;
            row /= NUM_LETTERS;
        } while (row > 0);
        String colString = Integer.toString(col + 1);
        return rowString + colString;
    }

    /**
     * Constructs the name of the state from its grid location.
     * 
     * @param cell
     *            the coordinates of the grid location
     * @return the name of the state.
     */
    private static String cellName(int[] cell)
    {
        return rowColToName(cell[0], cell[1]);
    }

    /**
     * Returns the adjacent cell location in the GridWorld.
     * 
     * @param i
     *            the row index.
     * @param j
     *            the column index.
     * @param board
     *            the board of cells.
     * @param direction
     *            the direction (N, E, S, or W)
     * @return the cell location adjacent.
     */
    private static int[] getAdjacentCell(int i, int j, int[][][] board,
        char direction)
    {
        int numRows = board.length;
        int numCols = board[0].length;
        int[] adjacentCell = null;
        if (direction == 'N' && i > 0)
        {
            adjacentCell = board[i - 1][j];
        }
        else if (direction == 'E' && j < numCols - 1)
        {
            adjacentCell = board[i][j + 1];
        }
        else if (direction == 'S' && i < numRows - 1)
        {
            adjacentCell = board[i + 1][j];
        }
        else if (direction == 'W' && j > 0)
        {
            adjacentCell = board[i][j - 1];
        }
        return adjacentCell;
    }

    /**
     * Displays the grid world described by the states, marking the current
     * state with a :).
     * 
     * @param neighborhood
     *            the neighborhood of the current grid cell.
     * @param current
     *            the current grid cell
     */
    public static void display(GridCell[][] neighborhood, GridCell current)
    {
        display(neighborhood, current, (HashMap<GridCell, String>) null);
    }

    /**
     * Helper method to get the number of rows in the Grid World described by
     * these states. Uses the label of each state (a1...aN,b1...bN,...,z1...zN)
     * to find numRows
     * 
     * @param states
     *            the states.
     * @return the number of rows of the grid world represented.
     */
    private static int getRows(List<GridCell> states)
    {
        int numRows = 0;
        for (GridCell s : states)
        {
            int row = nameToRow(s.name());
            if (row + 1 > numRows)
            {
                numRows = row + 1;
            }
        }
        return numRows;
    }

    /**
     * Helper method to get the number of columns in the Grid World described by
     * these states. Uses the label of each state (a1...aN,b1...bN,...,z1...zN)
     * to find numCols
     * 
     * @param states
     *            the states.
     * @return the number of columns of the grid world represented.
     */
    private static int getCols(List<GridCell> states)
    {
        int numCols = 0;
        for (GridCell s : states)
        {
            int col = nameToCol(s.name());
            if (col + 1 > numCols)
            {
                numCols = col + 1;
            }
        }
        return numCols;
    }

    /**
     * Helper method generates string representation for this state.
     * 
     * @param s
     *            the state to convert to a string.
     * @param current
     *            the current state.
     * @param labels
     *            the labels to print in the cell for the grid cell.
     * @return the string representation.
     */
    private static String stateToString(GridCell s, GridCell current,
        HashMap<GridCell, ?> labels)
    {
        int w = 7;
        String str = "";
        if (s == null)
        {
            str += String.format("%" + w + "s", "XXXX");
        }
        else if (labels == null && s.equals(current))
        {
            str += String.format("%" + w + "s", " :) ");
        }
        else if (s.isTerminal())
        {
            str += String.format("%" + w + "d", (int) s.reward());
        }
        else if (labels != null)
        {
            str +=
                (labels.get(s) instanceof Double) ? String.format("%" + w
                    + ".0f",
                    (Double) labels.get(s))
                    : String.format("%" + w + "s", labels.get(s).toString());
        }
        else
        {
            str += String.format("%" + w + "s", "");
        }
        return str + "|";
    }

    /**
     * Helper method to return a string of dashes.
     * 
     * @param numCols
     *            the number of columns of dashes to return.
     * @return a string of dashes.
     */
    private static String dashes(int numCols)
    {
        String s = "|";
        for (int j = 0; j < numCols; j++)
        {
            for (int k = 0; k < w; k++)
            {
                s += "-";
            }
            s += "|";
        }
        s += "\n";
        return s;
    }

    /**
     * Converts the GridWorld to a string based on the states, current state,
     * and labels.
     * 
     * @param neighborhood
     *            the neighborhood of the current grid cell..
     * @param current
     *            the current grid cell.
     * @param labels
     *            Labels for each grid cell.
     * @return a string representation of the world.
     */
    public static String toString(GridCell[][] neighborhood, GridCell current,
        HashMap<GridCell, ?> labels)
    {
        String str = "";
        int numRows = neighborhood.length;
        int numCols = neighborhood[0].length;
        str += dashes(numCols);
        for (int i = 0; i < numRows; i++)
        {
            str += "|";
            for (int j = 0; j < numCols; j++)
            {
                GridCell s = neighborhood[i][j];
                str += stateToString(s, current, labels);
            }
            str += "\n";
            str += dashes(numCols);
        }
        return str;
    }

    /**
     * Displays the grid world described by the states, marking the current
     * state with a :).
     * 
     * @param neighborhood
     *            the neighborhood of the current grid cell.
     * @param current
     *            the current grid cell.
     * @param labels
     *            the labels to print in each grid cell.
     */
    public static void display(GridCell[][] neighborhood, GridCell current,
        HashMap<GridCell, ?> labels)
    {
        System.out.println(toString(neighborhood, current, labels));
    }

    /**
     * Converts the GridWorld to a string based on the states, current state,
     * and labels.
     * 
     * @param mdp
     *            The MDP.
     * @param labels
     *            Labels for each grid cell in the MDP.
     */
    public static void display(MarkovDecisionProcess mdp,
        HashMap<GridCell, ?> labels)
    {
        List<GridCell> states = mdp.getStates();
        GridCell current = mdp.getCurrent();
        String str = "";
        int numRows = getRows(states);
        int numCols = getCols(states);
        str += dashes(numCols);

        for (int i = 0; i < numRows; i++)
        {
            str += "|";
            for (int j = 0; j < numCols; j++)
            {
                int index = states.indexOf(new GridCell(rowColToName(i, j), 0));
                GridCell s = (index >= 0) ? states.get(index) : null;
                str += stateToString(s, current, labels);
            }
            str += "\n";
            str += dashes(numCols);
        }
        System.out.println(str);
    }
}
