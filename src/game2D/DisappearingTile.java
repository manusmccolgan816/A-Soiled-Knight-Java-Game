package game2D;

/**
 * This is a Tile that disappears after a being stood on for a period
 * of time and then reappears after another set period of time.
 */
public class DisappearingTile extends Tile {

    public static final int TIME_TO_BREAK = 1750;
    public static final int TIME_TO_REAPPEAR = 5000;

    private long stoodOnTimer;
    private long reloadTimer;
    private long pauseTimer;
    private long lastPauseTimer;

    /**
     * Create an instance of a tile
     *
     * @param c The character associated with this tile
     * @param x The x tile coordinate in pixels
     * @param y The y tile coordinate in pixels
     */
    public DisappearingTile(char c, int x, int y) {
        super(c, x, y);

        reloadTimer = 0;
        pauseTimer = 0;
        lastPauseTimer = 0;
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

    public void startReloadTimer() {
        reloadTimer = System.nanoTime();
    }

    public long getReloadTimer() {
        return reloadTimer;
    }

    public void setReloadTimer(long reloadTimer) {
        this.reloadTimer = reloadTimer;
    }

    public long getPauseTimer() {
        return pauseTimer;
    }

    public void setPauseTimer(long pauseTimer) {
        this.pauseTimer = pauseTimer;
    }

    public long getLastPauseTimer() {
        return lastPauseTimer;
    }

    public void setLastPauseTimer(long lastPauseTimer) {
        this.lastPauseTimer = lastPauseTimer;
    }
}





