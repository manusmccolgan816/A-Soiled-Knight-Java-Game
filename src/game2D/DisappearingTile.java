package game2D;

public class DisappearingTile extends Tile {

    public static final int TIME_TO_BREAK = 2000;
    private long stoodOnTimer;

    /**
     * Create an instance of a tile
     *
     * @param c The character associated with this tile
     * @param x The x tile coordinate in pixels
     * @param y The y tile coordinate in pixels
     */
    public DisappearingTile(char c, int x, int y) {
        super(c, x, y);
    }

    public void startStoodOnTimer() {
        stoodOnTimer = System.nanoTime();
    }

    public long getStoodOnTimer() {
        return stoodOnTimer;
    }

    public void setStoodOnTimer(long stoodOnTimer) {
        this.stoodOnTimer = stoodOnTimer;
    }
}





