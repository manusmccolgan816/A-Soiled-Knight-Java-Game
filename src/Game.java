import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import game2D.*;
import sound.Filter3d;
import sound.PlayMIDI;
import sound.SoundManager;
import sound.SoundSample;

import javax.sound.sampled.AudioFormat;

/**
 * @author Manus McColgan
 */

public class Game extends GameCore implements MouseListener {

    //Dimensions are 4 times that of a Game Boy display
    public static final int SCREEN_WIDTH = 640; //160 * 4
    public static final int SCREEN_HEIGHT = 576; //144 * 4

    public STATE gameState;
    public static int CURRENTLEVEL = 0; //When this is 0, no level is being played

    private final float gravity = 0.002f;

    private boolean aPressed;
    private boolean dPressed;
    private boolean spacePressed;
    private boolean isPaused;
    private boolean isJustPaused;
    private boolean isJustResumed;

    //Determines whether player can jump based on whether the space key has been released since jumping
    private boolean canJump;
    private boolean isFacingRight;
    private boolean isRecovering;

    private long recoveryTimer;

    private Animation whiteKnightIdleRight;
    private Animation whiteKnightIdleLeft;
    private Animation whiteKnightBobbingRight;
    private Animation whiteKnightBobbingLeft;
    private Animation whiteKnightRecoveringIdleRight;
    private Animation whiteKnightRecoveringIdleLeft;
    private Animation blackKnightIdleRight;
    private Animation blackKnightIdleLeft;
    private Animation blackKnightBobbingRight;
    private Animation blackKnightBobbingLeft;
    private Animation blackPawnIdle;
    private Animation blackPawnBob;
    private Animation blackRookIdle;

    private Sprite whiteKnight = null;

    private final TileMap tmap = new TileMap();

    private Menu menu;
    private Controls controls;
    private HUD hud;
    private GameOver gameOver;
    private Completed completed;
    private Level level1;
    private Level level2;
    private Level level3;

    private PlayMIDI backgroundSong;

    //Uncompressed, 16 bit, mono, signed, little-endian format
    private static final AudioFormat PLAYBACK_FORMAT = new AudioFormat
            (44100, 16, 1, true, false);
    private SoundManager soundManager;
    private ArrayList<Filter3d> enemyJumpDistanceFilters;
    private SoundSample jumpSound;
    private SoundSample hitHurtSound;
    private SoundSample pickupHorseshoeSound;
    private SoundSample pauseSound;

    /**
     * The main method that creates an instance of our class and starts it running.
     *
     * @param args The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {
        Game gct = new Game();
        //Start in windowed mode with the given screen height and width
        gct.run(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    public Game() {
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null); //Positions frame in centre of screen
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        pack();
        //x is the amount of pixels hidden by the left and right borders of the window
        int x = getInsets().left + getInsets().right;
        //y is the amount of pixels hidden by the title bar and the bottom border of the window
        int y = getInsets().top + getInsets().bottom;

        //Now we set the size of the window as the desired size PLUS the amount of pixels hidden
        setPreferredSize(new Dimension(SCREEN_WIDTH + x, SCREEN_HEIGHT + y));

        setMaximumSize(new Dimension(SCREEN_WIDTH + x, SCREEN_HEIGHT + y));
        setMinimumSize(new Dimension(SCREEN_WIDTH + x, SCREEN_HEIGHT + y));

        initialiseMenu();
    }

    /**
     * Initialises the class by setting up variables, loading images, etc.
     */
    public void init() {
        whiteKnightIdleRight = new Animation();
        whiteKnightIdleLeft = new Animation();
        whiteKnightBobbingRight = new Animation();
        whiteKnightBobbingLeft = new Animation();
        whiteKnightRecoveringIdleRight = new Animation();
        whiteKnightRecoveringIdleLeft = new Animation();
        Image imgWhiteKnightIdleRight = loadImage("images/WhiteKnightResizedRight.png");
        Image imgWhiteKnightIdleLeft = loadImage("images/WhiteKnightResizedLeft.png");
        Image imgWhiteKnightBobbingRight = loadImage("images/WhiteKnightBobRight2.png");
        Image imgWhiteKnightBobbingLeft = loadImage("images/WhiteKnightBobLeft2.png");
        Image imgNoWhiteKnight = loadImage("images/NoWhiteKnight.png");
        whiteKnightIdleRight.addFrame(imgWhiteKnightIdleRight, 1000);
        whiteKnightIdleLeft.addFrame(imgWhiteKnightIdleLeft, 1000);
        whiteKnightBobbingRight.addFrame(imgWhiteKnightIdleRight, 100);
        whiteKnightBobbingRight.addFrame(imgWhiteKnightBobbingRight, 120);
        whiteKnightBobbingLeft.addFrame(imgWhiteKnightIdleLeft, 100);
        whiteKnightBobbingLeft.addFrame(imgWhiteKnightBobbingLeft, 120);
        whiteKnightRecoveringIdleRight.addFrame(imgNoWhiteKnight, 200);
        whiteKnightRecoveringIdleRight.addFrame(imgWhiteKnightIdleRight, 100);
        whiteKnightRecoveringIdleLeft.addFrame(imgNoWhiteKnight, 200);
        whiteKnightRecoveringIdleLeft.addFrame(imgWhiteKnightIdleLeft, 100);

        blackKnightIdleRight = new Animation();
        blackKnightIdleLeft = new Animation();
        blackKnightBobbingRight = new Animation();
        blackKnightBobbingLeft = new Animation();
        Image imgBlackKnightIdleRight = loadImage("images/BlackKnightResizedRight.png");
        Image imgBlackKnightIdleLeft = loadImage("images/BlackKnightResizedLeft.png");
        Image imgBlackKnightBobbingRight = loadImage("images/BlackKnightBobRight.png");
        Image imgBlackKnightBobbingLeft = loadImage("images/BlackKnightBobLeft.png");
        blackKnightIdleRight.addFrame(imgBlackKnightIdleRight, 1000);
        blackKnightIdleLeft.addFrame(imgBlackKnightIdleLeft, 1000);
        blackKnightBobbingRight.addFrame(imgBlackKnightIdleRight, 100);
        blackKnightBobbingRight.addFrame(imgBlackKnightBobbingRight, 120);
        blackKnightBobbingLeft.addFrame(imgBlackKnightIdleLeft, 100);
        blackKnightBobbingLeft.addFrame(imgBlackKnightBobbingLeft, 120);

        blackPawnIdle = new Animation();
        blackPawnBob = new Animation();
        Image imgBlackPawnIdle = loadImage("images/BlackPawnResized.png");
        Image imgBlackPawnBob = loadImage("images/BlackPawnBob.png");
        blackPawnIdle.addFrame(imgBlackPawnIdle, 1000);
        blackPawnBob.addFrame(imgBlackPawnIdle, 100);
        blackPawnBob.addFrame(imgBlackPawnBob, 120);

        blackRookIdle = new Animation();
        Image imgBlackRookIdle = loadImage("images/BlackRookResized.png");
        blackRookIdle.addFrame(imgBlackRookIdle, 1000);

        //Initialise the player with an animation
        whiteKnight = new Sprite(whiteKnightIdleRight);

        soundManager = new SoundManager(PLAYBACK_FORMAT, 6);
        jumpSound = soundManager.getSound("sounds/jump16bit.wav");
        hitHurtSound = soundManager.getSound("sounds/hitHurt16bit.wav");
        pickupHorseshoeSound = soundManager.getSound("sounds/pickupHorseshoe16bit.wav");
        pauseSound = soundManager.getSound("sounds/pause16bit.wav");

        initialiseGame();
        goToNextLevel();
    }

    /**
     * Starts up the menu, to be called to display menu screen
     */
    public void initialiseMenu() {
        gameState = STATE.Menu;
        CURRENTLEVEL = 0; //Progress is not saved, when 'Start' is clicked the player will start at level 1

        //Remove the mouse listeners which may still be running for other objects
        removeMouseListener(gameOver);
        removeMouseListener(controls);
        removeMouseListener(completed);

        menu = new Menu(this);

        //Add mouse listeners to menu screen
        addMouseListener(menu);
        addMouseMotionListener(menu);
    }

    /**
     * Called to display controls screen.
     */
    public void initialiseControls() {
        controls = new Controls(this);

        removeMouseListener(menu);
        removeMouseMotionListener(menu);

        gameState = STATE.Controls;

        addMouseListener(controls);
    }

    /**
     * This method can be called to restart a game multiple times,
     * whereas init() need only be called once at the beginning.
     */
    public void initialiseGame() {
        removeMouseListener(menu);
        removeMouseMotionListener(menu);

        //Set up the initial state of the player character
        whiteKnight.setVelocityX(0);
        whiteKnight.setVelocityY(0);
        whiteKnight.setAnimation(whiteKnightIdleRight);
        whiteKnight.show();

        aPressed = false;
        dPressed = false;
        spacePressed = false;
        isPaused = false;
        isJustPaused = false;
        isJustResumed = false;
        canJump = true;
        isFacingRight = true;
        isRecovering = false;

        recoveryTimer = 0;
    }

    /**
     * Called to display the game over screen.
     */
    public void initialiseGameOver() {
        backgroundSong.stop();

        gameState = STATE.GameOver;

        gameOver = new GameOver(this);

        //Add mouse listener to game over screen
        addMouseListener(gameOver);
    }

    /**
     * Called to display the game completed screen.
     */
    public void initialiseCompleted() {
        backgroundSong.stop();

        gameState = STATE.Completed;

        completed = new Completed(this);

        addMouseListener(completed);
    }

    /**
     * Draws the current state of the game.
     *
     * @param g The Graphics2D object to draw with
     */
    public void draw(Graphics2D g) {
        if(gameState == STATE.Menu) {
            menu.draw(g);
        }
        else if(gameState == STATE.Controls) {
            controls.draw(g);
        }
        else if(gameState == STATE.Game) {
            if(CURRENTLEVEL == 1) {
                level1.draw(g);
            }
            else if(CURRENTLEVEL == 2) {
                level2.draw(g);
            }
            else if(CURRENTLEVEL == 3) {
                level3.draw(g);
            }
            hud.draw(g);
        }
        else if(gameState == STATE.GameOver) {
            gameOver.draw(g);
        }
        else if(gameState == STATE.Completed) {
            completed.draw(g);
        }
    }

    /**
     * Updates sprites and checks for collisions.
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {
        if(gameState == STATE.Menu) {
            menu.update(elapsed);
        }
        else if(gameState == STATE.Game) {
            if(CURRENTLEVEL == 1) {
                level1.update(elapsed);
            }
            else if(CURRENTLEVEL == 2) {
                level2.update(elapsed);
            }
            else if(CURRENTLEVEL == 3) {
                level3.update(elapsed);
            }
        }
        else if (gameState == STATE.GameOver) {
            gameOver.update(elapsed);
        }
        else if(gameState == STATE.Completed) {
            completed.update(elapsed);
        }
    }

    /**
     * Returns true if s1's bounding box collides with s2's bounding box.
     *
     * @param s1    the first sprite
     * @param s2    the second sprite
     * @return      true if collision occurred
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2) {
        return ((s1.getX() + s1.getImage().getWidth(null) > s2.getX()) &&
                (s1.getX() < (s2.getX() + s2.getImage().getWidth(null))) &&
                ((s1.getY() + s1.getImage().getHeight(null) > s2.getY()) &&
                        (s1.getY() < s2.getY() + s2.getImage().getHeight(null))));
    }

    /**
     * Checks if a sprite is on the ground.
     *
     * @param s    The sprite to check
     * @param tmap The tilemap to check
     * @return     True if s is on a ground tile
     */
    public boolean isOnGround(Sprite s, TileMap tmap) {
        //Take a note of a sprite's current position
        float sx = s.getX();
        float sy = s.getY();

        //Find out how wide and how tall a tile is
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        //Gets the number of tiles across and down the bottom-left corner of s is, 6 pixels in to avoid side-on
        //collisions being detected
        int xtile = (int) ((sx + 6) / tileWidth);
        int ytile = (int) ((sy + s.getHeight()) / tileHeight);
        char ch = tmap.getTileChar(xtile, ytile);

        //If it's a ground tile
        if (ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') {
            return true;
        }

        //Gets the number of tiles across and down the bottom-right corner of s is, 6 pixels in to avoid side-on
        //collisions being detected
        xtile = (int) ((sx + s.getWidth() - 6) / tileWidth);
        ytile = (int) ((sy + s.getHeight()) / tileHeight);
        ch = tmap.getTileChar(xtile, ytile);

        //Return true if it is a ground tile, false if not
        return ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't';
    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'.
     *
     * @param s    The Sprite to check collisions for
     * @param tmap The tile map to check
     */
    public void checkTileCollision(Sprite s, TileMap tmap, ID id) {
        boolean reverseVelX = false;
        boolean turnToRight = false;

        //Take a note of s's current position
        float sx = s.getX();
        float sy = s.getY();

        //Find out how wide and how tall a tile is
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        //region Top-left collisions
        //Divide s???s x coordinate by the width of a tile to get the number of tiles across the x-axis that the sprite
        //is positioned at
        int xtile = (int) (sx / tileWidth);
        //The same applies to the y coordinate
        int ytile = (int) (sy / tileHeight);

        //What tile character is at the top left of the sprite s?
        char ch = tmap.getTileChar(xtile, ytile);

        if (ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') { //If it's a ground tile, handle it
            if(isOnGround(s, tmap) || sx >= tmap.getTileXC(xtile, ytile) + tileWidth - 6) {
                if(id == ID.Player)
                {
                    if(ch == 's') {
                        if(!isRecovering) {
                            soundManager.play(hitHurtSound);
                            hud.setHealth(hud.getHealth() - 1);

                            if(hud.getHealth() <= 0) {
                                //Handle death
                                initialiseGameOver();
                                return;
                            }
                            else {
                                isRecovering = true;
                                recoveryTimer = System.nanoTime();

                                if(isFacingRight)
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                                else
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                            }
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                }
                else {
                    reverseVelX = true;
                    turnToRight = true;
                }

                xtile = (int) (sx / tileWidth);
                ytile = (int) ((sy + s.getHeight()) / tileHeight);
                //Move s just out of the tile it moved into so there is no overlap
                s.setX(tmap.getTileXC(xtile, ytile) + tileWidth);
            }
            //If s is not on the ground (i.e. hitting a tile from underneath)...
            else {
                //Stop vertical movement
                s.setVelocityY(0);

                //Move s just out of the tile it moved into so there is no overlap
                s.setY(tmap.getTileYC(xtile, ytile) + s.getHeight() + 0.1f);
            }
        }
        //If s is the player and collided with a horse shoe
        else if(ch == 'e' && id == ID.Player) {
            soundManager.play(pickupHorseshoeSound);
            tmap.setTileChar('.', xtile, ytile); //Change the horse shoe to empty space
            hud.incrementHorseShoesCollected();

            if(hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                backgroundSong.stop();
                initialiseGame();
                goToNextLevel();
            }
        }
        //endregion

        //region Top-right collisions
        xtile = (int) ((sx + s.getWidth()) / tileWidth);
        ytile = (int) (sy / tileHeight);
        ch = tmap.getTileChar(xtile, ytile);

        if (ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') { //If it's a ground tile, handle it
            if(isOnGround(s, tmap) || sx + s.getWidth() <= tmap.getTileXC(xtile, ytile) + 6) {
                if(id == ID.Player)
                {
                    if(ch == 's') {
                        if(!isRecovering) {
                            soundManager.play(hitHurtSound);
                            hud.setHealth(hud.getHealth() - 1);

                            if(hud.getHealth() <= 0) {
                                //Handle death
                                initialiseGameOver();
                                return;
                            }
                            else {
                                isRecovering = true;
                                recoveryTimer = System.nanoTime();

                                if(isFacingRight)
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                                else
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                            }
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                }
                else {
                    reverseVelX = true;
                }

                xtile = (int) ((sx + s.getWidth())  / tileWidth);
                ytile = (int) ((sy + s.getHeight()) / tileHeight);
                //Move s just out of the tile it moved into so there is no overlap
                s.setX(tmap.getTileXC(xtile, ytile) - s.getWidth() - 0.1f);
            }
            //If s is not on the ground (i.e. hitting a tile from underneath)...
            else {
                //Stop vertical movement
                s.setVelocityY(0);

                //Move s just out of the tile it moved into so there is no overlap
                s.setY(tmap.getTileYC(xtile, ytile) + s.getHeight() + 0.1f);
            }
        }
        //If s is the player and collided with a horse shoe
        else if(ch == 'e' && id == ID.Player) {
            soundManager.play(pickupHorseshoeSound);
            tmap.setTileChar('.', xtile, ytile); //Change the horse shoe to empty space
            hud.incrementHorseShoesCollected();

            if(hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                backgroundSong.stop();
                initialiseGame();
                goToNextLevel();
            }
        }
        //endregion

        //Just checking the corners will not cover all possible collision areas if s is taller than a tile
        if(s.getHeight() > tileHeight) {
            //region Centre-left collisions
            //ytile is the middle y point of s so that it doesn't detect ground collisions
            xtile = (int) (sx / tileWidth);
            ytile = (int) ((sy + (s.getHeight() / 2)) / tileHeight);
            ch = tmap.getTileChar(xtile, ytile);

            if(ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') { //If it's a ground tile, handle it
                if(id == ID.Player)
                {
                    if(ch == 's') {
                        if(!isRecovering) {
                            soundManager.play(hitHurtSound);
                            hud.setHealth(hud.getHealth() - 1);

                            if(hud.getHealth() <= 0) {
                                //Handle death
                                initialiseGameOver();
                                return;
                            }
                            else {
                                isRecovering = true;
                                recoveryTimer = System.nanoTime();

                                if(isFacingRight)
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                                else
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                            }
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                }
                else {
                    reverseVelX = true;
                    turnToRight = true;
                }
                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tmap.getTileXC(xtile, ytile) + tileWidth);
            }
            //If s is the player and collided with a horse shoe
            else if(ch == 'e' && id == ID.Player) {
                soundManager.play(pickupHorseshoeSound);
                tmap.setTileChar('.', xtile, ytile); //Change the horse shoe to empty space
                hud.incrementHorseShoesCollected();

                if(hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                    backgroundSong.stop();
                    initialiseGame();
                    goToNextLevel();
                }
            }
            //endregion

            //region Centre-right collisions
            xtile = (int) ((sx + s.getWidth())  / tileWidth);
            ytile = (int) ((sy + s.getHeight() / 2) / tileHeight);
            ch = tmap.getTileChar(xtile, ytile);

            if(ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') { //If it's a ground tile, handle it
                if(id == ID.Player)
                {
                    if(ch == 's') {
                        if(!isRecovering) {
                            soundManager.play(hitHurtSound);
                            hud.setHealth(hud.getHealth() - 1);

                            if(hud.getHealth() <= 0) {
                                //Handle death
                                initialiseGameOver();
                                return;
                            }
                            else {
                                isRecovering = true;
                                recoveryTimer = System.nanoTime();

                                if(isFacingRight)
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                                else
                                    whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                            }
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                }
                else {
                    reverseVelX = true;
                }
                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tmap.getTileXC(xtile, ytile) - s.getWidth() - 0.1f);
            }
            //If s is the player and collided with a horse shoe
            else if(ch == 'e' && id == ID.Player) {
                soundManager.play(pickupHorseshoeSound);
                tmap.setTileChar('.', xtile, ytile); //Change the horse shoe to empty space
                hud.incrementHorseShoesCollected();

                if(hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                    backgroundSong.stop();
                    initialiseGame();
                    goToNextLevel();
                }
            }
            //endregion
        }

        //region Bottom-left collisions
        xtile = (int) (sx / tileWidth);
        ytile = (int) ((sy + s.getHeight()) / tileHeight);
        ch = tmap.getTileChar(xtile, ytile);

        if(ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') { //If it's a ground tile, handle it
            if(ch == 's' && id == ID.Player) {
                if(!isRecovering) {
                    soundManager.play(hitHurtSound);
                    hud.setHealth(hud.getHealth() - 1);

                    if(hud.getHealth() <= 0) {
                        //Handle death
                        initialiseGameOver();
                        return;
                    }
                    else {
                        isRecovering = true;
                        recoveryTimer = System.nanoTime();

                        if(isFacingRight)
                            whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                        else
                            whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                    }
                }
            }
            if(!isOnGround(s, tmap))
            {
                if(id == ID.Player)
                {
                    //Stop horizontal movement
                    s.setVelocityX(0);
                }
                else {
                    reverseVelX = true;
                    turnToRight = true;
                }

                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tmap.getTileXC(xtile, ytile) + tileWidth);
            }
        }
        //If s is the player and collided with a horse shoe
        else if(ch == 'e' && id == ID.Player) {
            soundManager.play(pickupHorseshoeSound);
            tmap.setTileChar('.', xtile, ytile); //Change the horse shoe to empty space
            hud.incrementHorseShoesCollected();

            if(hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                backgroundSong.stop();
                initialiseGame();
                goToNextLevel();
            }
        }
        //endregion

        //region Bottom-right collisions
        xtile = (int) ((sx + s.getWidth())  / tileWidth);
        ytile = (int) ((sy + s.getHeight()) / tileHeight);
        ch = tmap.getTileChar(xtile, ytile);

        if(ch != '.' && ch != 'e' && ch != 'b' && ch != 'm' && ch != 't') { //If it's a ground tile, handle it
            if(ch == 's' && id == ID.Player) {
                if(!isRecovering) {
                    soundManager.play(hitHurtSound);
                    hud.setHealth(hud.getHealth() - 1);

                    if(hud.getHealth() <= 0) {
                        //Handle death
                        initialiseGameOver();
                        return;
                    }
                    else {
                        isRecovering = true;
                        recoveryTimer = System.nanoTime();

                        if(isFacingRight)
                            whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                        else
                            whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                    }
                }
            }
            if(!isOnGround(s, tmap)) {
                if(id == ID.Player)
                {
                    //Stop horizontal movement
                    s.setVelocityX(0);
                }
                else {
                    reverseVelX = true;
                }

                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tmap.getTileXC(xtile, ytile) - s.getWidth() - 0.1f);
            }
        }
        //If s is the player and collided with a horse shoe
        else if(ch == 'e' && id == ID.Player) {
            soundManager.play(pickupHorseshoeSound);
            tmap.setTileChar('.', xtile, ytile); //Change the horse shoe to empty space
            hud.incrementHorseShoesCollected();

            if(hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                backgroundSong.stop();
                initialiseGame();
                goToNextLevel();
            }
        }
        //endregion

        if(reverseVelX) {
            s.setVelocityX(-s.getVelocityX()); //Reverse the sprite's movement

            if(id == ID.EnemyBlackKnight) {
                if(turnToRight) {
                    s.setAnimation(blackKnightBobbingRight);
                }
                else {
                    s.setAnimation(blackKnightBobbingLeft);
                }
            }
        }
    }

    public void goToNextLevel() {
        LinkedList<Sprite> sprites = new LinkedList<>();
        LinkedList<ID> spriteIDs = new LinkedList<>();

        //Game over screen mouse listener is removed if it is running
        removeMouseListener(gameOver);
        removeMouseListener(controls);

        //If there is no current level...
        if(CURRENTLEVEL == 0) {
            //Load the tile map
            tmap.loadMap("maps", "Level1Map.txt");

            //Load the background image
            Image imgBackground = loadImage("images/RollingHillsResized.png");

            //Initialise the HUD, giving the max health for the level and the number of horse shoes to collect
            hud = new HUD(4, 4, 0, countHorseShoesInMap(tmap));

            //Set the position of the player
            whiteKnight.setPosition(tmap.getTileXC(6, 0), tmap.getTileYC(0, 8));
            sprites.add(whiteKnight);
            spriteIDs.add(ID.Player);

            //Set the positions of the level's enemies
            Sprite blackPawn0 = new Sprite(blackPawnBob);
            blackPawn0.show();
            blackPawn0.setPosition(tmap.getTileXC(12, 0), tmap.getTileYC(0, 8));
            blackPawn0.setVelocityX(0.2f);
            sprites.add(blackPawn0);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackPawn1 = new Sprite(blackPawnBob);
            blackPawn1.show();
            blackPawn1.setPosition(tmap.getTileXC(40, 0), tmap.getTileYC(0, 7));
            blackPawn1.setVelocityX(0.2f);
            sprites.add(blackPawn1);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackKnight1 = new Sprite(blackKnightBobbingLeft);
            blackKnight1.show();
            blackKnight1.setPosition(tmap.getTileXC(53, 0), tmap.getTileYC(0, 7));
            sprites.add(blackKnight1);
            spriteIDs.add(ID.EnemyBlackKnight);

            Sprite blackKnight2 = new Sprite(blackKnightBobbingLeft);
            blackKnight2.show();
            blackKnight2.setPosition(tmap.getTileXC(89, 0), tmap.getTileYC(0, 7));
            sprites.add(blackKnight2);
            spriteIDs.add(ID.EnemyBlackKnight);

            //Play the background track
            backgroundSong = new PlayMIDI();
            try {
                backgroundSong.play("sounds/StoneTower.mid");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the array of 3d sound filters
            enemyJumpDistanceFilters = new ArrayList<>();
            //Go over every sprite in the level
            for(int i = 0; i < sprites.size(); i++) {
                if(spriteIDs.get(i) == ID.EnemyBlackKnight) {
                    //If the sprite is a blackKnight, it is added to the 3d sound filter list
                    enemyJumpDistanceFilters.add(new Filter3d(whiteKnight, sprites.get(i), SCREEN_WIDTH));
                }
                else {
                    //If the sprite is not a blackKnight, add null to the list as a filter will not be needed for them
                    enemyJumpDistanceFilters.add(null);
                }
            }

            level1 = new Level(sprites, spriteIDs, tmap, imgBackground);
            CURRENTLEVEL = 1;
        }
        else if(CURRENTLEVEL == 1) {
            //Load the tile map
            tmap.loadMap("maps", "Level2Map.txt");

            //Load the background image
            Image imgBackground = loadImage("images/RollingHillsResized.png");

            hud = new HUD(3, 3, 0, countHorseShoesInMap(tmap));

            whiteKnight.setPosition(tmap.getTileXC(4, 0), tmap.getTileYC(0, 17));
            sprites.add(whiteKnight);
            spriteIDs.add(ID.Player);

            Sprite blackRook1 = new Sprite(blackRookIdle);
            blackRook1.show();
            blackRook1.setPosition(tmap.getTileXC(34, 0), tmap.getTileYC(0, 13));
            sprites.add(blackRook1);
            spriteIDs.add(ID.EnemyBlackRook);

            Sprite blackPawn1 = new Sprite(blackPawnBob);
            blackPawn1.show();
            blackPawn1.setPosition(tmap.getTileXC(48, 0), tmap.getTileYC(0, 16));
            blackPawn1.setVelocityX(0.2f);
            sprites.add(blackPawn1);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackPawn2 = new Sprite(blackPawnBob);
            blackPawn2.show();
            blackPawn2.setPosition(tmap.getTileXC(51, 0), tmap.getTileYC(0, 16));
            blackPawn2.setVelocityX(0.2f);
            sprites.add(blackPawn2);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackKnight1 = new Sprite(blackKnightBobbingLeft);
            blackKnight1.show();
            blackKnight1.setPosition(tmap.getTileXC(19, 0), tmap.getTileYC(0, 4));
            sprites.add(blackKnight1);
            spriteIDs.add(ID.EnemyBlackKnight);

            Sprite blackPawn3 = new Sprite(blackPawnBob);
            blackPawn3.show();
            blackPawn3.setPosition(tmap.getTileXC(22, 0), tmap.getTileYC(0, 4));
            blackPawn3.setVelocityX(0.2f);
            sprites.add(blackPawn3);
            spriteIDs.add(ID.EnemyBlackPawn);

            backgroundSong = new PlayMIDI();
            try {
                backgroundSong.play("sounds/StoneTower.mid");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Initialise the array of 3d sound filters
            enemyJumpDistanceFilters = new ArrayList<>();
            //Go over every sprite in the level
            for(int i = 0; i < sprites.size(); i++) {
                if(spriteIDs.get(i) == ID.EnemyBlackKnight) {
                    //If the sprite is a blackKnight, it is added to the 3d sound filter list
                    enemyJumpDistanceFilters.add(new Filter3d(whiteKnight, sprites.get(i), SCREEN_WIDTH));
                }
                else {
                    //If the sprite is not a blackKnight, add null to the list as a filter will not be needed for them
                    enemyJumpDistanceFilters.add(null);
                }
            }

            level2 = new Level(sprites, spriteIDs, tmap, imgBackground);
            CURRENTLEVEL = 2;
        }
        else {
            initialiseCompleted();
            return;
        }
//        else if(CURRENTLEVEL == 2) {
//            //Load the tile maps
//            tmap.loadMap("maps", "Level3Map.txt");
//
//            //Load the background image
//            Image imgBackground = loadImage("images/RollingHillsResized.png");
//
//            hud = new HUD(3, 3, 0, countHorseShoesInMap(tmap));
//
//            whiteKnight.setPosition(tmap.getTileXC(4, 0), tmap.getTileYC(0, 17));
//
//            sprites.add(whiteKnight);
//            spriteIDs.add(ID.Player);
//
//            backgroundSong = new PlayMIDI();
//            try {
//                backgroundSong.play("sounds/StoneTower.mid");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            //Initialise the array of 3d sound filters
//            enemyJumpDistanceFilters = new ArrayList<>();
//            //Go over every sprite in the level
//            for(int i = 0; i < sprites.size(); i++) {
//                if(spriteIDs.get(i) == ID.EnemyBlackKnight) {
//                    //If the sprite is a blackKnight, it is added to the 3d sound filter list
//                    enemyJumpDistanceFilters.add(new Filter3d(whiteKnight, sprites.get(i), SCREEN_WIDTH));
//                }
//                else {
//                    //If the sprite is not a blackKnight, add null to the list as a filter will not be needed for them
//                    enemyJumpDistanceFilters.add(null);
//                }
//            }
//
//            level3 = new Level(sprites, spriteIDs, tmap, imgBackground);
//            CURRENTLEVEL = 3;
//        }
        gameState = STATE.Game;
    }

    /**
     * Override of the keyPressed event defined in GameCore to catch our own events.
     *
     * @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) {
        if(gameState != STATE.Game) return;

        int key = e.getKeyCode();

        if(key == KeyEvent.VK_P) {
            if(isPaused) {
                isPaused = false;
                isJustResumed = true;
            }
            else {
                isPaused = true;
                isJustPaused = true;
            }
        }

        if(isPaused) return;

        if(key == KeyEvent.VK_A) {
            aPressed = true;
        }
        if(key == KeyEvent.VK_D) {
            dPressed = true;
        }
        if(key == KeyEvent.VK_SPACE) {
            spacePressed = true;
        }

        if(aPressed && dPressed) {
            whiteKnight.setVelocityX(0);
        }
    }

    /**
     * Override of the keyReleased event defined in GameCore to catch our own events.
     *
     * @param e The event that has been generated
     */
    public void keyReleased(KeyEvent e) {
        if(gameState != STATE.Game) return;
        int key = e.getKeyCode();

        if(isPaused) return;

        if(key == KeyEvent.VK_A) {
            if(isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
            else whiteKnight.setAnimation(whiteKnightIdleLeft);
            aPressed = false;
        }
        if(key == KeyEvent.VK_D) {
            if(isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
            else whiteKnight.setAnimation(whiteKnightIdleRight);
            dPressed = false;
        }
        if(key == KeyEvent.VK_SPACE) {

            spacePressed = false;
            //The space key must be released before the player can jump again
            canJump = true;
        }

        if(!aPressed && !dPressed) {
            whiteKnight.setVelocityX(0);
        }
    }

    /**
     * Ensures that val is never less than min or greater than max.
     *
     * @param val the value to assess
     * @param min the minimum value
     * @param max the maximum value
     * @return the greater value, either min or the lesser value of max and val
     */
    public static float clamp(float val, float min, float max) {
        if(val >= max)
            return max;
        else if(val <= min)
            return min;
        else
            return val;
    }

    /**
     * Counts and returns the number of horse shoe tiles in the given tilemap.
     *
     * @param tmap the TileMap to check
     * @return the number of horse shoes in the tilemap
     */
    public int countHorseShoesInMap(TileMap tmap) {
        int mapWidth = tmap.getMapWidth();
        int mapHeight = tmap.getTileHeight();
        char tileChar;
        int numHorseShoes = 0;

        for(int i = 0; i < mapWidth; i++) {
            for(int j = 0; j < mapHeight; j++) {
                tileChar = tmap.getTileChar(i, j);

                //Increment numHorseShoes if a horse shoe is found
                if(tileChar == 'e')
                    numHorseShoes++;
            }
        }

        return numHorseShoes;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public HUD getHud() {
        return hud;
    }

    class Level extends GameCore {

        private final TileMap tmap;

        private LinkedList<Sprite> sprites;
        private LinkedList<ID> spriteIDs;

        private float[][] spriteVelocities;

        private long pauseTimer;
        /*
        lastPauseTimer is used to ensure that the game can be paused multiple times during the recovery process from
        taking damage without any of the time paused for being considered as part of the recovery time.
         */
        private long lastPauseTimer;

        private final Image imgBackground;
        private final int backgroundWidth;
        private float backgroundX, backgroundX2;
        private int previousXOffset;

        public Level(LinkedList<Sprite> sprites, LinkedList<ID> spriteIDs, TileMap tmap, Image imgBackground) {
            pack();

            this.sprites = sprites;
            this.spriteIDs = spriteIDs;
            this.tmap = tmap;
            this.imgBackground = imgBackground;

            backgroundWidth = imgBackground.getWidth(null);
            backgroundX = 0;
            backgroundX2 = imgBackground.getWidth(null);
            previousXOffset = 0;

            //Initialise a 2D array to store the x and y velocity of each sprite in the level to use when pausing and
            //resuming
            spriteVelocities = new float[2][sprites.size()];

            pauseTimer = 0;
            lastPauseTimer = 0;
        }

        @Override
        public void draw(Graphics2D g) {
            int xOffset = 0;
            int yOffset = 0;

            boolean screenScrolling;

            g.setColor(new Color(240, 246, 240)); //Sets the background colour
            g.fillRect(getInsets().left, getInsets().top, SCREEN_WIDTH, SCREEN_HEIGHT);

            for(int i = 0; i < sprites.size(); i++) {
                Sprite s = sprites.get(i);

                if(spriteIDs.get(i) == ID.Player) {
                    //Adjusts the x offset so that the player is to the left of the centre of the screen
                    xOffset = (int) (-s.getX()) + (SCREEN_WIDTH / 2) - s.getWidth();
                    //Ensures that the screen does not scroll past the bounds of the map
                    xOffset = (int) clamp(xOffset, -tmap.getPixelWidth() + SCREEN_WIDTH + getInsets().left,
                            getInsets().right);

                    //screenScrolling is false if the screen hasn't scrolled along the x-axis since the last run of
                    //Level.draw(), otherwise true
                    screenScrolling = xOffset != previousXOffset;

                    previousXOffset = xOffset;

                    //Adjusts the y offset so that the player is close to the centre of the screen
                    yOffset = (int) (-s.getY()) + (SCREEN_HEIGHT / 2) - s.getHeight();
                    //Ensures that the screen does not scroll past the bounds of the map
                    yOffset = (int) clamp(yOffset, -tmap.getPixelHeight() + SCREEN_HEIGHT + getInsets().top,
                            0);

                    //Apply offsets to whiteKnight
                    s.setOffsets(xOffset, yOffset);

                    //region Background
                    //Draw background images
                    g.drawImage(imgBackground, (int) backgroundX, 0, null);
                    g.drawImage(imgBackground, (int) backgroundX2, 0, null);

                    //If the screen is scrolling and the player is moving left...
                    if(screenScrolling && s.getVelocityX() < 0) {
                        //...move both background images to the right
                        backgroundX = backgroundX + 0.75f;
                        backgroundX2 = backgroundX2 + 0.75f;

                        //If the first background image is not visible and is to the right of the screen by over 10
                        //pixels...
                        if(backgroundX > SCREEN_WIDTH + 10) {
                            //...place it to the left of the second background image
                            backgroundX = backgroundX2 - backgroundWidth;
                        }
                        //If the second background image is not visible and is to the right of the screen by over 10
                        //pixels...
                        else if(backgroundX2 > SCREEN_WIDTH + 10) {
                            //...place it to the left of the first background image
                            backgroundX2 = backgroundX - backgroundWidth;
                        }
                    }
                    //If the screen is scrolling and the player is moving right...
                    else if(screenScrolling && s.getVelocityX() > 0) {
                        //...move both background images to the left
                        backgroundX = backgroundX - 1;
                        backgroundX2 = backgroundX2 - 1;

                        //If the first background image is not visible and is to the left of the screen...
                        if(backgroundX < -backgroundWidth) {
                            //...place it to the right of the second background image
                            backgroundX = backgroundX2 + backgroundWidth;
                        }
                        //If the second background image is not visible and is to the left of the screen...
                        else if(backgroundX2 < -backgroundWidth) {
                            //...place it to the right of the first background image
                            backgroundX2 = backgroundX + backgroundWidth;
                        }
                    }
                    //endregion

                    //Apply offsets to tile map and draw  it
                    tmap.draw(g, xOffset, yOffset);

                    //Draw the player
                    s.draw(g);
                }
                //If the sprite is not the player...
                else {
                    s.setOffsets(xOffset, yOffset);
                    s.draw(g);
                }
            }

            if(isPaused) {
                g.drawImage(loadImage("images/PauseScreenResized.png"), 0, 0, null);
            }
        }

        @Override
        public void update(long elapsed) {
            if(isPaused) {
                //If this is the first call of Level.update() since the pause button was pressed...
                if(isJustPaused) {
                    //If the game is being paused for the second (third, ... onward) time during the same recovery...
                    if(isRecovering && pauseTimer != 0) {
                        //Assign the previous length of time paused for to lastPauseTimer
                        lastPauseTimer = pauseTimer;
                    }
                    //'Start' the pause timer
                    pauseTimer = System.nanoTime();

                    soundManager.play(pauseSound);
                    backgroundSong.stop();

                    //Set whiteKnight's animation back to what it should be
                    if(aPressed) {
                        if(isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                        else whiteKnight.setAnimation(whiteKnightIdleLeft);
                    }
                    else if(dPressed) {
                        if(isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                        else whiteKnight.setAnimation(whiteKnightIdleRight);
                    }

                    aPressed = false;
                    dPressed = false;
                    spacePressed = false;
                    canJump = true;

                    for(int i = 0; i < sprites.size(); i++) {
                        //Store the x and y velocity of the sprite before stopping it
                        spriteVelocities[0][i] = sprites.get(i).getVelocityX();
                        spriteVelocities[1][i] = sprites.get(i).getVelocityY();

                        //Set the sprite's x and y velocity to 0
                        sprites.get(i).stop();

                        sprites.get(i).pauseAnimation();

                        isJustPaused = false;
                    }
                }
            }
            else {
                //If this is the first call of Level.update() since the resume button was pressed...
                if(isJustResumed) {
                    //Get the time paused for
                    pauseTimer = (System.nanoTime() - pauseTimer);
                    //Add the previous time(s) paused for to pauseTimer
                    pauseTimer += lastPauseTimer;

                    backgroundSong.resume();

                    for(int i = 0; i < sprites.size(); i++) {
                        if(spriteIDs.get(i) == ID.Player) {
                            sprites.get(i).setVelocity(0, spriteVelocities[1][i]);
                        }
                        else {
                            //Set the x and y velocity of the sprite to what they were before pausing
                            sprites.get(i).setVelocity(spriteVelocities[0][i], spriteVelocities[1][i]);
                        }

                        sprites.get(i).playAnimation();

                        isJustResumed = false;
                    }
                }
                for(int i = 0; i < sprites.size(); i++) {
                    Sprite s = sprites.get(i);

                    if(spriteIDs.get(i) == ID.Player) {
                        s.update(elapsed);

                        //region Gravity and ground collision
                        if(isOnGround(s, tmap) && s.getVelocityY() >= 0) {
                            //Stops whiteKnight from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of whiteKnight is
                            int ytile = (int) ((s.getY() + s.getHeight()) / tmap.getTileHeight());

                            //Moves whiteKnight just out of the tile it landed on so there is no overlap
                            s.setY(tmap.getTileYC(0, ytile) - s.getHeight());
                        }
                        else {
                            //Makes whiteKnight fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        //region Movement
                        //Handles left movement
                        if(aPressed && !dPressed) {
                            s.setAnimation(whiteKnightBobbingLeft);
                            isFacingRight = false;
                            if(isRecovering) s.setAnimation(whiteKnightRecoveringIdleLeft);
                            else {
                                if(isOnGround(s, tmap)) {
                                    s.setAnimation(whiteKnightBobbingLeft);
                                }
                                else {
                                    s.setAnimation(whiteKnightIdleLeft);
                                }
                            }

                            s.setVelocityX(-0.3f);
                        }
                        //Handles right movement
                        else if(!aPressed && dPressed) {
                            s.setAnimation(whiteKnightBobbingRight);
                            isFacingRight = true;
                            if(isRecovering) s.setAnimation(whiteKnightRecoveringIdleRight);
                            else {
                                if(isOnGround(s, tmap)) {
                                    s.setAnimation(whiteKnightBobbingRight);
                                }
                                else {
                                    s.setAnimation(whiteKnightIdleRight);
                                }
                            }

                            s.setVelocityX(0.3f);
                        }
                        //endregion

                        //region Jumping
                        if(spacePressed && isOnGround(s, tmap) && canJump) {
                            canJump = false;
                            //s jumps
                            s.setVelocityY(clamp((s.getVelocityY() + 0.1f) * -7.5f, -3, 0));

                            soundManager.play(jumpSound);
                        }
                        //endregion

                        checkTileCollision(s, tmap, ID.Player);
                    }
                    else if(spriteIDs.get(i) == ID.EnemyBlackKnight) {
                        Random rnd = new Random();

                        s.update(elapsed);

                        //region Gravity and ground collision
                        if(isOnGround(s, tmap) && s.getVelocityY() >= 0) {
                            //Stops s from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of s is
                            int ytile = (int) ((s.getY() + s.getHeight()) / tmap.getTileHeight());

                            //Moves s just out of the tile it landed on so there is no overlap
                            s.setY(tmap.getTileYC(0, ytile) - s.getHeight());

                            if(s.getAnimation() == blackKnightBobbingLeft || s.getAnimation() == blackKnightIdleLeft) {
                                s.setVelocityX(-0.2f);
                                s.setAnimation(blackKnightBobbingLeft);
                            }
                            else {
                                s.setVelocityX(0.2f);
                                s.setAnimation(blackKnightBobbingRight);
                            }
                        }
                        else {
                            //Makes s fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        //region Jumping
                        if(rnd.nextInt(200) == 1) {
                            if(isOnGround(s, tmap)) {
                                s.setVelocityY(Game.clamp((s.getVelocityY() + 0.1f) * -7.5f, -3, 0));
                                s.setVelocityX(0);
                                if(s.getAnimation() == blackKnightBobbingLeft || s.getAnimation() ==
                                        blackKnightIdleLeft)
                                    s.setAnimation(blackKnightIdleLeft);
                                else
                                    s.setAnimation(blackKnightIdleRight);

                                //Play the 3d sound filter for the given blackKnight
                                soundManager.play(jumpSound, enemyJumpDistanceFilters.get(i), false);
                            }
                        }
                        //endregion

                        checkTileCollision(s, tmap, spriteIDs.get(i));
                    }
                    else if(spriteIDs.get(i) == ID.EnemyBlackPawn) {
                        s.update(elapsed);

                        //region Gravity and ground collision
                        if(isOnGround(s, tmap) && s.getVelocityY() >= 0) {
                            //Stops s from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of s is
                            int ytile = (int) ((s.getY() + s.getHeight()) / tmap.getTileHeight());

                            //Moves s just out of the tile it landed on so there is no overlap
                            s.setY(tmap.getTileYC(0, ytile) - s.getHeight());
                        }
                        else {
                            //Makes s fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        checkTileCollision(s, tmap, spriteIDs.get(i));
                    }
                    else if(spriteIDs.get(i) == ID.EnemyBlackRook) {
                        s.update(elapsed);

                        //region Gravity and ground collision
                        if(isOnGround(s, tmap) && s.getVelocityY() >= 0) {
                            //Stops s from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of s is
                            int ytile = (int) ((s.getY() + s.getHeight()) / tmap.getTileHeight());

                            //Moves s just out of the tile it landed on so there is no overlap
                            s.setY(tmap.getTileYC(0, ytile) - s.getHeight());
                        }
                        else {
                            //Makes s fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        checkTileCollision(s, tmap, spriteIDs.get(i));

                        if(whiteKnight.getX() + whiteKnight.getWidth() / 2f > s.getX() + s.getWidth() / 2f) {
                            s.setVelocityX(0.075f);
                        }
                        else {
                            s.setVelocityX(-0.075f);
                        }
                    }

                    for(int j = 1; j < sprites.size(); j++) { //j initialised to 1 to avoid checking player sprite
                        Sprite s2 = sprites.get(j);

                        if(s2 != s) {
                            if(spriteIDs.get(i) == ID.Player) { //If s is the player sprite
                                //region Player on enemy collision
                                if(!isRecovering) {
                                    if(boundingBoxCollision(s, s2)) {
                                        soundManager.play(hitHurtSound);

                                        HUD h = getHud();

                                        h.setHealth(h.getHealth() - 1);

                                        if(h.getHealth() <= 0) {
                                            //Handle death
                                            initialiseGameOver();
                                            return;
                                        }
                                        else {
                                            isRecovering = true;
                                            recoveryTimer = System.nanoTime();

                                            if(isFacingRight)
                                                whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                                            else
                                                whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                                        }
                                    }
                                }
                                //endregion
                            }
                            else {
                                //region Enemy on enemy collision
                                if(boundingBoxCollision(s, s2)) {
                                    //If both sprites are going left
                                    if(s.getVelocityX() < 0 && s2.getVelocityX() < 0) {
                                        //If s is the rightmost sprite
                                        if(s.getX() > s2.getX()) {
                                            //Change the direction s is going
                                            s.setVelocityX(s.getVelocityX() * -1);
                                            //Move s so that there is no overlap
                                            s.setX(s2.getX() + s2.getWidth() + 1);
                                        }
                                        //If s2 is the rightmost sprite
                                        else {
                                            //Change the direction s2 is going
                                            s2.setVelocityX(s2.getVelocityX() * -1);
                                            //Move s2 so that there is no overlap
                                            s2.setX(s.getX() + s.getWidth() + 1);
                                        }
                                    }
                                    //If both sprites are going right
                                    else if(s.getVelocityX() > 0 && s2.getVelocityX() > 0) {
                                        //If s is the leftmost sprite
                                        if(s.getX() < s2.getX()) {
                                            //Change the direction s is going
                                            s.setVelocityX(s.getVelocityX() * -1);
                                            //Move s so there is no overlap
                                            s.setX(s2.getX() - s.getWidth() - 1);
                                        }
                                        //If s2 is the leftmost sprite
                                        else {
                                            //Change the direction s2 is going
                                            s2.setVelocityX(s2.getVelocityX() * -1);
                                            //Move s2 so there is no overlap
                                            s2.setX(s.getX() - s2.getWidth() - 1);
                                        }
                                    }
                                    //If s is higher off the ground than s2 (most likely if s is in the air and s2 is
                                    //not)
                                    else if(s.getY() + s.getHeight() > s2.getY() + s2.getHeight()) {
                                        //Change the direction s2 is going
                                        s2.setVelocityX(s2.getVelocityX() * -1);

                                        //If s2 is the rightmost sprite
                                        if(s2.getX() > s.getX()) {
                                            //Move s2 to the right of s so there is no overlap
                                            s2.setX(s.getX() + s.getWidth() + 1);
                                        }
                                        //If s2 is the leftmost sprite
                                        else {
                                            //Move s2 to the left of s so there is no overlap
                                            s2.setX(s.getX() - s2.getWidth() - 1);
                                        }
                                    }
                                    //If s2 is higher off the ground than s (most likely if s2 is in the air and s is
                                    //not)
                                    else if(s2.getY() + s2.getHeight() > s.getY() + s.getHeight()) {
                                        //Change the direction s is going
                                        s.setVelocityX(s.getVelocityX() * -1);

                                        //If s is the rightmost sprite
                                        if(s.getX() > s2.getX()) {
                                            //Move s to the right of s so there is no overlap
                                            s.setX(s2.getX() + s2.getWidth() + 1);
                                        }
                                        //If s is the leftmost sprite
                                        else {
                                            //Move s to the left of s so there is no overlap
                                            s.setX(s2.getX() - s.getWidth() - 1);
                                        }
                                    }
                                    //If sprites are going opposite directions, and they are on the same elevation
                                    else {
                                        //Change the direction both of the sprites are going
                                        s.setVelocityX(s.getVelocityX() * -1);
                                        s2.setVelocityX(s2.getVelocityX() * -1);

                                        //Move s so that it no longer overlaps with s2
                                        if(s.getX() < s2.getX()) {
                                            //Move s so there is no overlap
                                            s.setX(s2.getX() - s.getWidth() - 1);
                                        }
                                        else {
                                            //Move s2 so there is no overlap
                                            s2.setX(s.getX() - s2.getWidth() - 1);
                                        }
                                    }

                                    //If s is a blackKnight
                                    if(spriteIDs.get(i) == ID.EnemyBlackKnight) {
                                        if(s.getVelocityX() < 0) {
                                            s.setAnimation(blackKnightBobbingLeft);
                                        }
                                        else {
                                            s.setAnimation(blackKnightBobbingRight);
                                        }
                                    }
                                    //If s2 is a blackKnight
                                    if(spriteIDs.get(j) == ID.EnemyBlackKnight) {
                                        if(s2.getVelocityX() < 0) {
                                            s2.setAnimation(blackKnightBobbingLeft);
                                        }
                                        else {
                                            s2.setAnimation(blackKnightBobbingRight);
                                        }
                                    }
                                }
                                //endregion
                            }
                        }
                    }

                    long elapsedTimeSincePlayerEnemyCollision = (System.nanoTime() - recoveryTimer) / 1000000;

                    //If the player is recovering and at least 2 (not paused) seconds have passed since they started
                    //recovering...
                    if(isRecovering && elapsedTimeSincePlayerEnemyCollision > 2000 + (pauseTimer / 1000000)) {
                        //...stop recovery process
                        isRecovering = false;
                        recoveryTimer = 0;

                        pauseTimer = 0;
                        lastPauseTimer = 0;

                        //Change whiteKnight back to correct animation
                        if(isFacingRight) {
                            if(whiteKnight.getVelocityX() > 0) whiteKnight.setAnimation(whiteKnightBobbingRight);
                            else whiteKnight.setAnimation(whiteKnightIdleRight);
                        }
                        else {
                            if(whiteKnight.getVelocityX() < 0) whiteKnight.setAnimation(whiteKnightBobbingLeft);
                            else whiteKnight.setAnimation(whiteKnightIdleLeft);
                        }
                    }
                }
            }
        }
    }
}
