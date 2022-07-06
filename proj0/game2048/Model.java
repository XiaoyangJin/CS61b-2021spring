package game2048;

import java.util.Formatter;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    //column is ｜，row is ——
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }//useful to return to a specific element

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

//        for(int i = 0; i < board.size(); i++){
//            for(int j = 0; j < board.size(); j++){
//                Tile t= board.tile(i,j);
//                if(board.tile(i,j) != null){
//                    board.move(i,3,t);//move everything up
//                    changed = true;
//                    score += 7;
//                }
//            }
//        }

        //move up only
        //can move up if the space above it is empty, or it can move up one if the space above it has the same value as itself
        //when iterating over rows, it is safe to iterate starting from row 3 down

//        board.setViewingPerspective(side);
//        int size = board.size();
//        for (int c = 0; c < size; c++) {
//            HashSet<Integer> mergedRows = new HashSet();
//            for (int r = size - 1; r >= 0; r--) {
//                Tile currentTile = board.tile(c, r);
//                if (currentTile == null) {
//                    continue;
//                }
//                int distanceToTop = size - r;
//                int moveToRow = size - 1;
//                for (int i = 1; i < distanceToTop; i++) {
//                    if (board.tile(c, r+i) != null) {
//                        if (currentTile.value() == board.tile(c, r+i).value() && !mergedRows.contains(r+i)) {
//                            mergedRows.add(r+i);
//                            moveToRow = r+i;
//                        }
//                        else moveToRow = r + i - 1;
//                    } else if (r == size - 2) {
//                        moveToRow = size - 1;
//                    }
//                }
//                if (moveToRow != r) {
//                    if (board.tile(c, moveToRow) != null) {
//                        score += 2 * currentTile.value();
//                    }
//                    board.move(c, moveToRow, currentTile);
//                    changed = true;
//                }
//            }
//        }


        Set<Tile> changedTiles = new HashSet<>();
        board.setViewingPerspective(side);
        for (int row = board.size() - 2; row >= 0; row--) {
            for (int col = 0; col < board.size(); col++) {
                Tile tile = board.tile(col, row);
                if (tile == null) {
                    continue;
                }
                VTile vTile = new VTile(new Coordinate(col, row), tile);
                boolean isChangedAfterMove = moveTileUp(vTile, changedTiles);
                if (isChangedAfterMove) {
                    changed = true;
                }
            }
        }
        board.setViewingPerspective(Side.NORTH);

        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }
    //new code for 2021 edition
    private boolean moveTileUp(VTile vTile, Set<Tile> changedTiles) {
        int targetRow;
        VTile nearest = findNearestTileAbove(vTile);
        if (nearest == null) {
            targetRow = board.size() - 1;
        } else if (vTile.actualTile.value() == nearest.actualTile.value() && !changedTiles.contains(nearest.actualTile)) {
            targetRow = nearest.row;
        } else if (nearest.row == vTile.row + 1) {
            return false;
        } else {
            targetRow = nearest.row - 1;
        }
        boolean isMerged = board.move(vTile.col, targetRow, vTile.actualTile);
        if (isMerged) {
            score += vTile.actualTile.next().value();
            changedTiles.add(board.tile(vTile.col, targetRow));
        }
        return true;
    }

    private VTile findNearestTileAbove(VTile vTile) {
        for (int row = vTile.row + 1; row < board.size(); row++) {
            Tile actualTile = board.tile(vTile.col, row);
            if (actualTile != null) {
                return new VTile(new Coordinate(vTile.col, row), actualTile);
            }
        }
        return null;
    }

    private static class VTile {
        int col;
        int row;
        Tile actualTile;
        public VTile(Coordinate c, Tile t) {
            col = c.col;
            row = c.row;
            actualTile = t;
        }
    }

    private static class Coordinate {
        int col;
        int row;
        public Coordinate(int c, int r) {
            col = c;
            row = r;
        }
        public static Coordinate[] of(int[][] values) {
            Coordinate[] coordinates = new Coordinate[values.length];
            for (int i = 0; i < values.length; i++) {
                int[] coordinate = values[i];
                coordinates[i] = new Coordinate(coordinate[0], coordinate[1]);
            }
            return coordinates;
        }
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for(int i = 0; i < 4; i++){ //4 <-> b.size()
            for(int j = 0; j < 4; j++){
                if(b.tile(i,j) == null){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(b.tile(i,j) != null && b.tile(i,j).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        //emptySpaceExists
        for(int i = 0; i < 4; i++){ //4 <-> b.size()
            for(int j = 0; j < 4; j++){
                if(Model.emptySpaceExists(b)){
                    return true;
                    //b.tile(i,j) != null &&
                }
            }
        }

        //test left or right
//        for(int j = 0; j < 4; j++){ //4 <-> b.size()
//            for(int i = 1; i < 2; i++){
//                if(b.tile(i,j) == null){
//                    continue;
//                } else if(b.tile(i,j).value() == b.tile(i-1,j).value() || b.tile(i,j).value() == b.tile(i+1,j).value()){
//                    return true;
//                }
//            }
//        }

        //test up or down
//        for(int i = 0; i < 4; i++){ //4 <-> b.size()
//            for(int j = 1; j < 2; j++){
//                if(b.tile(i,j) == null){
//                    continue;
//                } else if(b.tile(i,j).value() == b.tile(i,j-1).value() || b.tile(i,j).value() == b.tile(i,j+1).value()){
//                    return true;
//                }
//            }
//        }

        for(int i = 0; i < 4; i++){ //4 <-> b.size()
            for(int j = 0; j < 4; j++){
                if(b.tile(i,j) != null && i == 3 && j == 3){
                    continue;
                }

                if(j==3){
                    if(b.tile(i,j).value() == b.tile(i+1,j).value()){
                        return true;
                    }
                    continue;
                }

                if(i==3){
                    if(b.tile(i,j).value() == b.tile(i,j+1).value()){
                        return true;
                    }
                    continue;
                }

                if(b.tile(i,j) != null && i != 3 && j != 3){
                    if(b.tile(i,j).value() == b.tile(i+1,j).value()){
                        return true;
                    } else if(b.tile(i,j).value() == b.tile(i,j+1).value()){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
