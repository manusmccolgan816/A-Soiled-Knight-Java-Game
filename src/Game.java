import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import game2D.*;
import sound.*;

public class Game extends GameCore implements MouseListener {

    //Dimensions are 4 times that of a Game Boy display
    public static final int SCREEN_WIDTH = 640; //160 * 4
    public static final int SCREEN_HEIGHT = 576; //144 * 4

    public STATE gameState;
    public static int currentLevel = 0; //When this is 0, no level is being played
    public static boolean isDebugModeOn = false;

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
    private boolean isHappyBlockActivated;

    private long recoveryTimer;
    private long happyBlockTimer;

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

    private final TileMap tMap = new TileMap();
    public static final char horseShoeChar = 'e';
    public static final char heartChar = 'h';
    public static final ArrayList<Character> backgroundChars = new ArrayList<>(
            Arrays.asList('.', horseShoeChar, heartChar, 'b', 'm', 't')
    );
    public static final ArrayList<Character> damageChars = new ArrayList<>(
            Arrays.asList('s')
    );
    public static final char happyBlockChar = ')';
    public static final char sadBlockChar = '(';

    private Menu menu;
    private Controls controls;
    private HUD hud;
    private GameOver gameOver;
    private Completed completed;
    private Level level1;
    private Level level2;

    private PlayMIDI backgroundSong;

    private static final String jumpSoundFilepath = "sounds/jump16bit.wav";
    private static final String hitHurtSoundFilepath = "sounds/hitHurt16bit.wav";
    private static final String pickupHorseshoeSoundFilepath = "sounds/pickupHorseshoe16bit.wav";
    private static final String healthUpSoundFilepath = "sounds/healthUp16bit.wav";
    private static final String pauseSoundFilepath = "sounds/pause16bit.wav";

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

        initialiseGame();
        goToNextLevel();
    }

    /**
     * Starts up the menu, to be called to display menu screen.
     */
    public void initialiseMenu() {
        gameState = STATE.Menu;
        currentLevel = 0; //Progress is not saved, when 'Start' is clicked the player will start at level 1

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
        isHappyBlockActivated = false;

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
        if (gameState == STATE.Menu) {
            menu.draw(g);
        } else if (gameState == STATE.Controls) {
            controls.draw(g);
        } else if (gameState == STATE.Game) {
            if (currentLevel == 1) {
                level1.draw(g);
            } else if (currentLevel == 2) {
                level2.draw(g);
            }
            hud.draw(g);
        } else if (gameState == STATE.GameOver) {
            gameOver.draw(g);
        } else if (gameState == STATE.Completed) {
            completed.draw(g);
        }
    }

    /**
     * Updates sprites and checks for collisions.
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {
        if (gameState == STATE.Menu) {
            menu.update(elapsed);
        } else if (gameState == STATE.Game) {
            if (currentLevel == 1) {
                level1.update(elapsed);
            } else if (currentLevel == 2) {
                level2.update(elapsed);
            }
        } else if (gameState == STATE.GameOver) {
            gameOver.update(elapsed);
        } else if (gameState == STATE.Completed) {
            completed.update(elapsed);
        }
    }

    /**
     * Returns true if s1's bounding box collides with s2's bounding box.
     *
     * @param s1 the first sprite
     * @param s2 the second sprite
     * @return true if collision occurred
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
     * @param tMap The tilemap to check
     * @return true if s is on a ground tile
     */
    public boolean isOnGround(Sprite s, TileMap tMap) {
        //Take a note of a sprite's current position
        float sx = s.getX();
        float sy = s.getY();

        //Find out how wide and how tall a tile is
        float tileWidth = tMap.getTileWidth();
        float tileHeight = tMap.getTileHeight();

        //Gets the number of tiles across and down the bottom-left corner of s is, 6 pixels in to avoid side-on
        //collisions being detected
        int xTile = (int) ((sx + 6) / tileWidth);
        int yTile = (int) ((sy + s.getHeight()) / tileHeight);
        char ch = tMap.getTileChar(xTile, yTile);

        //If it's a ground tile
        if (!backgroundChars.contains(ch)) {
            return true;
        }

        //Gets the number of tiles across and down the bottom-right corner of s is, 6 pixels in to avoid side-on
        //collisions being detected
        xTile = (int) ((sx + s.getWidth() - 6) / tileWidth);
        yTile = (int) ((sy + s.getHeight()) / tileHeight);
        ch = tMap.getTileChar(xTile, yTile);

        //Return true if it is a ground tile, false if not
        return !backgroundChars.contains(ch);
    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite s.
     *
     * @param s    The Sprite to check collisions for
     * @param tMap The tile map to check
     */
    public void handleTileCollision(Sprite s, TileMap tMap, ID id) {
        boolean reverseVelX = false;
        boolean turnToRight = false;

        //Take a note of s's current position
        float sx = s.getX();
        float sy = s.getY();

        //Find out how wide and how tall a tile is
        float tileWidth = tMap.getTileWidth();
        float tileHeight = tMap.getTileHeight();

        //region Top-left collisions
        //Divide s’s x coordinate by the width of a tile to get the number of tiles across the x-axis that the sprite
        //is positioned at
        int xTile = (int) (sx / tileWidth);
        //The same applies to the y coordinate
        int yTile = (int) (sy / tileHeight);

        //The tile character at the top left of sprite s
        char ch = tMap.getTileChar(xTile, yTile);

        if (!backgroundChars.contains(ch)) {
            if (isOnGround(s, tMap) || sx >= tMap.getTileXC(xTile, yTile) + tileWidth - 6) {
                if (id == ID.Player) {
                    if (damageChars.contains(ch)) {
                        //If the player isn't recovering...
                        if (!isRecovering) {
                            //If the player died, return
                            if (handleDamageCollision()) return;
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                } else {
                    reverseVelX = true;
                    turnToRight = true;
                }

                xTile = (int) (sx / tileWidth);
                yTile = (int) ((sy + s.getHeight()) / tileHeight);
                //Move s just out of the tile it moved into so there is no overlap
                s.setX(tMap.getTileXC(xTile, yTile) + tileWidth);
            }
            //If s is not on the ground (i.e. hitting a tile from underneath)...
            else {
                //Stop vertical movement
                s.setVelocityY(0);

                //Move s just out of the tile it moved into so there is no overlap
                s.setY(tMap.getTileYC(xTile, yTile) + s.getHeight() + 0.1f);
            }
        }
        //If s is the player and collided with a horse shoe or heart
        else {
            checkHandleHorseShoeCollision(tMap, id, xTile, yTile, ch);
            checkHandleHeartCollision(tMap, id, xTile, yTile, ch);
        }
        //endregion

        //region Top-right collisions
        xTile = (int) ((sx + s.getWidth()) / tileWidth);
        yTile = (int) (sy / tileHeight);
        ch = tMap.getTileChar(xTile, yTile);

        if (!backgroundChars.contains(ch)) {
            if (isOnGround(s, tMap) || sx + s.getWidth() <= tMap.getTileXC(xTile, yTile) + 6) {
                if (id == ID.Player) {
                    if (damageChars.contains(ch)) {
                        //If the player isn't recovering...
                        if (!isRecovering) {
                            //If the player died, return
                            if (handleDamageCollision()) return;
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                } else {
                    reverseVelX = true;
                }

                xTile = (int) ((sx + s.getWidth()) / tileWidth);
                yTile = (int) ((sy + s.getHeight()) / tileHeight);
                //Move s just out of the tile it moved into so there is no overlap
                s.setX(tMap.getTileXC(xTile, yTile) - s.getWidth() - 0.1f);
            }
            //If s is not on the ground (i.e. hitting a tile from underneath)...
            else {
                //Stop vertical movement
                s.setVelocityY(0);

                //Move s just out of the tile it moved into so there is no overlap
                s.setY(tMap.getTileYC(xTile, yTile) + s.getHeight() + 0.1f);
            }
        }
        //If s is the player and collided with a horse shoe or heart
        else {
            checkHandleHorseShoeCollision(tMap, id, xTile, yTile, ch);
            checkHandleHeartCollision(tMap, id, xTile, yTile, ch);
        }
        //endregion

        //Just checking the corners will not cover all possible collision areas if s is taller than a tile
        if (s.getHeight() > tileHeight) {
            //region Centre-left collisions
            //yTile is the middle y point of s so that it doesn't detect ground collisions
            xTile = (int) (sx / tileWidth);
            yTile = (int) ((sy + (s.getHeight() / 2)) / tileHeight);
            ch = tMap.getTileChar(xTile, yTile);

            if (!backgroundChars.contains(ch)) {
                if (id == ID.Player) {
                    if (damageChars.contains(ch)) {
                        //If the player isn't recovering...
                        if (!isRecovering) {
                            //If the player died, return
                            if (handleDamageCollision()) return;
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                } else {
                    reverseVelX = true;
                    turnToRight = true;
                }
                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tMap.getTileXC(xTile, yTile) + tileWidth);
            }
            //If s is the player and collided with a horse shoe or heart
            else {
                checkHandleHorseShoeCollision(tMap, id, xTile, yTile, ch);
                checkHandleHeartCollision(tMap, id, xTile, yTile, ch);
            }
            //endregion

            //region Centre-right collisions
            xTile = (int) ((sx + s.getWidth()) / tileWidth);
            yTile = (int) ((sy + s.getHeight() / 2) / tileHeight);
            ch = tMap.getTileChar(xTile, yTile);

            if (!backgroundChars.contains(ch)) {
                if (id == ID.Player) {
                    if (damageChars.contains(ch)) {
                        if (!isRecovering) {
                            //If the player died, return
                            if (handleDamageCollision()) return;
                        }
                    }

                    //Stop horizontal movement
                    s.setVelocityX(0);
                } else {
                    reverseVelX = true;
                }
                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tMap.getTileXC(xTile, yTile) - s.getWidth() - 0.1f);
            }
            //If s is the player and collided with a horse shoe or heart
            else {
                checkHandleHorseShoeCollision(tMap, id, xTile, yTile, ch);
                checkHandleHeartCollision(tMap, id, xTile, yTile, ch);
            }
            //endregion
        }

        //region Bottom-left collisions
        xTile = (int) (sx / tileWidth);
        yTile = (int) ((sy + s.getHeight()) / tileHeight);
        ch = tMap.getTileChar(xTile, yTile);

        if (!backgroundChars.contains(ch)) {
            if (damageChars.contains(ch) && id == ID.Player) {
                if (!isRecovering) {
                    //If the player died, return
                    if (handleDamageCollision()) return;
                }
            }
            if (!isOnGround(s, tMap)) {
                if (id == ID.Player) {
                    //Stop horizontal movement
                    s.setVelocityX(0);
                } else {
                    reverseVelX = true;
                    turnToRight = true;
                }

                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tMap.getTileXC(xTile, yTile) + tileWidth);
            }
        }
        //If s is the player and collided with a horse shoe or heart
        else {
            checkHandleHorseShoeCollision(tMap, id, xTile, yTile, ch);
            checkHandleHeartCollision(tMap, id, xTile, yTile, ch);
        }
        //endregion

        //region Bottom-right collisions
        xTile = (int) ((sx + s.getWidth()) / tileWidth);
        yTile = (int) ((sy + s.getHeight()) / tileHeight);
        ch = tMap.getTileChar(xTile, yTile);

        if (!backgroundChars.contains(ch)) {
            if (damageChars.contains(ch) && id == ID.Player) {
                if (!isRecovering) {
                    //If the player died, return
                    if (handleDamageCollision()) return;
                }
            }
            if (!isOnGround(s, tMap)) {
                if (id == ID.Player) {
                    //Stop horizontal movement
                    s.setVelocityX(0);
                } else {
                    reverseVelX = true;
                }

                //Moves s just out of the tile it moved into so there is no overlap
                s.setX(tMap.getTileXC(xTile, yTile) - s.getWidth() - 0.1f);
            }
        }
        //If s is the player and collided with a horse shoe or heart
        else {
            checkHandleHorseShoeCollision(tMap, id, xTile, yTile, ch);
            checkHandleHeartCollision(tMap, id, xTile, yTile, ch);
        }
        //endregion

        if (reverseVelX) {
            s.setVelocityX(-s.getVelocityX()); //Reverse the sprite's movement

            if (id == ID.EnemyBlackKnight) {
                if (turnToRight) {
                    s.setAnimation(blackKnightBobbingRight);
                } else {
                    s.setAnimation(blackKnightBobbingLeft);
                }
            }
        }
    }

    private void checkHandleHorseShoeCollision(TileMap tMap, ID id, int xTile, int yTile, char ch) {
        if (ch == horseShoeChar && id == ID.Player) {
            new Sound(pickupHorseshoeSoundFilepath).start();

            tMap.setTileChar('.', xTile, yTile); //Change the horse shoe to empty space
            hud.incrementHorseShoesCollected();

            if (hud.getNumHorseShoesCollected() == hud.getNumHorseShoes()) {
                backgroundSong.stop();
                initialiseGame();
                goToNextLevel();
            }
        }
    }

    private void checkHandleHeartCollision(TileMap tMap, ID id, int xTile, int yTile, char ch) {
        if (ch == heartChar && id == ID.Player) {
            new Sound(healthUpSoundFilepath).start();

            tMap.setTileChar('.', xTile, yTile);

            int health = hud.getHealth();
            if (health < hud.getMaxHealth()) {
                hud.setHealth(health + 1);
            }
        }
    }

    /**
     * Handles the player taking damage.
     *
     * @return true if the player died, false if not
     */
    private boolean handleDamageCollision() {
        new Sound(hitHurtSoundFilepath).start(); //Play the take damage sound

        hud.setHealth(hud.getHealth() - 1); //Lower the player's health by 1

        //If the player is dead...
        if (hud.getHealth() <= 0) {
            //Handle death
            initialiseGameOver();
            return true;
        }
        //If the player is still alive...
        else {
            //Start the recovery process
            isRecovering = true;
            recoveryTimer = System.nanoTime();

            if (isFacingRight)
                whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
            else
                whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
        }
        return false;
    }

    /**
     * Loads the next level, setting up sprites, tilemap, background etc.
     * If there is no next level, the completed screen is displayed.
     */
    public void goToNextLevel() {
        LinkedList<Sprite> sprites = new LinkedList<>();
        LinkedList<ID> spriteIDs = new LinkedList<>();

        //Game over screen mouse listener is removed if it is running
        removeMouseListener(gameOver);
        removeMouseListener(controls);

        //If there is no current level...
        if (currentLevel == 0) {
            //Load the tile map
            tMap.loadMap("maps", "Level1Map.txt");

            //Load the background image
            Image imgBackground = loadImage("images/RollingHillsResized.png");

            //Initialise the HUD, giving the max health for the level and the number of horse shoes to collect
            hud = new HUD(this,4, 4, 0, countHorseShoesInMap(tMap));

            //Set the position of the player
            whiteKnight.setPosition(tMap.getTileXC(6, 0), tMap.getTileYC(0, 8));
            sprites.add(whiteKnight);
            spriteIDs.add(ID.Player);

            //Set the positions of the level's enemies
            Sprite blackPawn0 = new Sprite(blackPawnBob);
            blackPawn0.show();
            blackPawn0.setPosition(tMap.getTileXC(12, 0), tMap.getTileYC(0, 8));
            blackPawn0.setVelocityX(0.2f);
            sprites.add(blackPawn0);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackPawn1 = new Sprite(blackPawnBob);
            blackPawn1.show();
            blackPawn1.setPosition(tMap.getTileXC(40, 0), tMap.getTileYC(0, 7));
            blackPawn1.setVelocityX(0.2f);
            sprites.add(blackPawn1);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackKnight1 = new Sprite(blackKnightBobbingLeft);
            blackKnight1.show();
            blackKnight1.setPosition(tMap.getTileXC(53, 0), tMap.getTileYC(0, 7));
            sprites.add(blackKnight1);
            spriteIDs.add(ID.EnemyBlackKnight);

            Sprite blackKnight2 = new Sprite(blackKnightBobbingLeft);
            blackKnight2.show();
            blackKnight2.setPosition(tMap.getTileXC(89, 0), tMap.getTileYC(0, 7));
            sprites.add(blackKnight2);
            spriteIDs.add(ID.EnemyBlackKnight);

            //Play the background track
            backgroundSong = new PlayMIDI();
            try {
                backgroundSong.play("sounds/StoneTower.mid");
            } catch (Exception e) {
                e.printStackTrace();
            }

            level1 = new Level(sprites, spriteIDs, tMap, imgBackground);
            currentLevel = 1;
        } else if (currentLevel == 1) {
            //Load the tile map
            tMap.loadMap("maps", "Level2Map.txt");

            //Load the background image
            Image imgBackground = loadImage("images/RollingHillsResized.png");

            hud = new HUD(this,4, 4, 0, countHorseShoesInMap(tMap));

            whiteKnight.setPosition(tMap.getTileXC(4, 0), tMap.getTileYC(0, 17));
            sprites.add(whiteKnight);
            spriteIDs.add(ID.Player);

            Sprite blackKnight1 = new Sprite(blackKnightBobbingLeft);
            blackKnight1.show();
            blackKnight1.setPosition(tMap.getTileXC(19, 0), tMap.getTileYC(0, 4));
            sprites.add(blackKnight1);
            spriteIDs.add(ID.EnemyBlackKnight);

            Sprite blackPawn3 = new Sprite(blackPawnBob);
            blackPawn3.show();
            blackPawn3.setPosition(tMap.getTileXC(22, 0), tMap.getTileYC(0, 4));
            blackPawn3.setVelocityX(0.2f);
            sprites.add(blackPawn3);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackRook1 = new Sprite(blackRookIdle);
            blackRook1.show();
            blackRook1.setPosition(tMap.getTileXC(34, 0), tMap.getTileYC(0, 13));
            sprites.add(blackRook1);
            spriteIDs.add(ID.EnemyBlackRook);

            Sprite blackPawn1 = new Sprite(blackPawnBob);
            blackPawn1.show();
            blackPawn1.setPosition(tMap.getTileXC(48, 0), tMap.getTileYC(0, 16));
            blackPawn1.setVelocityX(0.2f);
            sprites.add(blackPawn1);
            spriteIDs.add(ID.EnemyBlackPawn);

            Sprite blackPawn2 = new Sprite(blackPawnBob);
            blackPawn2.show();
            blackPawn2.setPosition(tMap.getTileXC(51, 0), tMap.getTileYC(0, 16));
            blackPawn2.setVelocityX(0.2f);
            sprites.add(blackPawn2);
            spriteIDs.add(ID.EnemyBlackPawn);

            backgroundSong = new PlayMIDI();
            try {
                backgroundSong.play("sounds/StoneTower.mid");
            } catch (Exception e) {
                e.printStackTrace();
            }

            level2 = new Level(sprites, spriteIDs, tMap, imgBackground);
            currentLevel = 2;
        } else {
            initialiseCompleted();
            return;
        }
        gameState = STATE.Game;
    }

    /**
     * Override of the keyPressed event defined in GameCore to catch our own events.
     *
     * @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) {
        if (gameState != STATE.Game) return;

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_P) {
            if (isPaused) {
                isPaused = false;
                isJustResumed = true;
            } else {
                isPaused = true;
                isJustPaused = true;
            }
        }
        else if (key == KeyEvent.VK_M) {
            isDebugModeOn = !isDebugModeOn;
        }

        if (isPaused) return;

        if (key == KeyEvent.VK_A) {
            aPressed = true;
        }
        if (key == KeyEvent.VK_D) {
            dPressed = true;
        }
        if (key == KeyEvent.VK_SPACE) {
            spacePressed = true;
        }

        if (aPressed && dPressed) {
            whiteKnight.setVelocityX(0);
        }
    }

    /**
     * Override of the keyReleased event defined in GameCore to catch our own events.
     *
     * @param e The event that has been generated
     */
    public void keyReleased(KeyEvent e) {
        if (gameState != STATE.Game) return;
        int key = e.getKeyCode();

        if (isPaused) return;

        if (key == KeyEvent.VK_A) {
            if (isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
            else whiteKnight.setAnimation(whiteKnightIdleLeft);
            aPressed = false;
        }
        if (key == KeyEvent.VK_D) {
            if (isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
            else whiteKnight.setAnimation(whiteKnightIdleRight);
            dPressed = false;
        }
        if (key == KeyEvent.VK_SPACE) {

            spacePressed = false;
            //The space key must be released before the player can jump again
            canJump = true;
        }

        if (!aPressed && !dPressed) {
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
        if (val >= max)
            return max;
        else return Math.max(val, min);
    }

    /**
     * Counts and returns the number of horse shoe tiles in the given tilemap.
     *
     * @param tMap the TileMap to check
     * @return the number of horse shoes in the tilemap
     */
    public int countHorseShoesInMap(TileMap tMap) {
        int mapWidth = tMap.getMapWidth();
        int mapHeight = tMap.getTileHeight();
        char tileChar;
        int numHorseShoes = 0;

        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                tileChar = tMap.getTileChar(i, j);

                //Increment numHorseShoes if a horse shoe is found
                if (tileChar == horseShoeChar)
                    numHorseShoes++;
            }
        }

        return numHorseShoes;
    }

    public Sprite getWhiteKnight() {
        return whiteKnight;
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

    class Level extends GameCore {

        private final TileMap tMap;

        private final LinkedList<Sprite> sprites;
        private final LinkedList<ID> spriteIDs;

        private final float[][] spriteVelocities;

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

        public Level(LinkedList<Sprite> sprites, LinkedList<ID> spriteIDs, TileMap tMap, Image imgBackground) {
            pack();

            this.sprites = sprites;
            this.spriteIDs = spriteIDs;
            this.tMap = tMap;
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

            g.setColor(ColourRepository.WHITE); //Sets the background colour
            g.fillRect(getInsets().left, getInsets().top, SCREEN_WIDTH, SCREEN_HEIGHT);

            for (int i = 0; i < sprites.size(); i++) {
                Sprite s = sprites.get(i);

                if (spriteIDs.get(i) == ID.Player) {
                    //Adjusts the x offset so that the player is to the left of the centre of the screen
                    xOffset = (int) (-s.getX()) + (SCREEN_WIDTH / 2) - s.getWidth();
                    //Ensures that the screen does not scroll past the bounds of the map
                    xOffset = (int) clamp(xOffset, -tMap.getPixelWidth() + SCREEN_WIDTH + getInsets().left,
                            getInsets().right);

                    //screenScrolling is false if the screen hasn't scrolled along the x-axis since the last run of
                    //Level.draw(), otherwise true
                    screenScrolling = xOffset != previousXOffset;

                    previousXOffset = xOffset;

                    //Adjusts the y offset so that the player is close to the centre of the screen
                    yOffset = (int) (-s.getY()) + (SCREEN_HEIGHT / 2) - s.getHeight();
                    //Ensures that the screen does not scroll past the bounds of the map
                    yOffset = (int) clamp(yOffset, -tMap.getPixelHeight() + SCREEN_HEIGHT + getInsets().top,
                            0);

                    //Apply offsets to whiteKnight
                    s.setOffsets(xOffset, yOffset);

                    //region Background
                    //Draw background images
                    g.drawImage(imgBackground, (int) backgroundX, 0, null);
                    g.drawImage(imgBackground, (int) backgroundX2, 0, null);

                    //If the screen is scrolling and the player is moving left...
                    if (screenScrolling && s.getVelocityX() < 0) {
                        //...move both background images to the right
                        backgroundX = backgroundX + 0.75f;
                        backgroundX2 = backgroundX2 + 0.75f;

                        //If the first background image is not visible and is to the right of the screen by over 10
                        //pixels...
                        if (backgroundX > SCREEN_WIDTH + 10) {
                            //...place it to the left of the second background image
                            backgroundX = backgroundX2 - backgroundWidth;
                        }
                        //If the second background image is not visible and is to the right of the screen by over 10
                        //pixels...
                        else if (backgroundX2 > SCREEN_WIDTH + 10) {
                            //...place it to the left of the first background image
                            backgroundX2 = backgroundX - backgroundWidth;
                        }
                    }
                    //If the screen is scrolling and the player is moving right...
                    else if (screenScrolling && s.getVelocityX() > 0) {
                        //...move both background images to the left
                        backgroundX = backgroundX - 1;
                        backgroundX2 = backgroundX2 - 1;

                        //If the first background image is not visible and is to the left of the screen...
                        if (backgroundX < -backgroundWidth) {
                            //...place it to the right of the second background image
                            backgroundX = backgroundX2 + backgroundWidth;
                        }
                        //If the second background image is not visible and is to the left of the screen...
                        else if (backgroundX2 < -backgroundWidth) {
                            //...place it to the right of the first background image
                            backgroundX2 = backgroundX + backgroundWidth;
                        }
                    }
                    //endregion

                    //Apply offsets to tile map and draw  it
                    tMap.draw(g, xOffset, yOffset);

                    //Draw the player
                    s.draw(g);
                }
                //If the sprite is not the player...
                else {
                    s.setOffsets(xOffset, yOffset);
                    s.draw(g);
                }
            }

            if (isPaused) {
                g.drawImage(loadImage("images/PauseScreenResized.png"), 0, 0, null);
            }
        }

        @Override
        public void update(long elapsed) {
            if (isPaused) {
                //If this is the first call of Level.update() since the pause button was pressed...
                if (isJustPaused) {
                    //If the game is being paused for the second (third, ... onward) time during the same recovery...
                    if (isRecovering && pauseTimer != 0) {
                        //Assign the previous length of time paused for to lastPauseTimer
                        lastPauseTimer = pauseTimer;
                    }
                    //Start the pause timer
                    pauseTimer = System.nanoTime();

                    new Sound(pauseSoundFilepath).start();
                    backgroundSong.stop();

                    //Set whiteKnight's animation back to what it should be
                    if (aPressed) {
                        if (isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleLeft);
                        else whiteKnight.setAnimation(whiteKnightIdleLeft);
                    } else if (dPressed) {
                        if (isRecovering) whiteKnight.setAnimation(whiteKnightRecoveringIdleRight);
                        else whiteKnight.setAnimation(whiteKnightIdleRight);
                    }

                    aPressed = false;
                    dPressed = false;
                    spacePressed = false;
                    canJump = true;

                    for (int i = 0; i < sprites.size(); i++) {
                        //Store the x and y velocity of the sprite before stopping it
                        spriteVelocities[0][i] = sprites.get(i).getVelocityX();
                        spriteVelocities[1][i] = sprites.get(i).getVelocityY();

                        //Set the sprite's x and y velocity to 0
                        sprites.get(i).stop();

                        sprites.get(i).pauseAnimation();

                        isJustPaused = false;
                    }
                }
            } else {
                //If this is the first call of Level.update() since the resume button was pressed...
                if (isJustResumed) {
                    //Get the time paused for
                    pauseTimer = (System.nanoTime() - pauseTimer);
                    //Add the previous time(s) paused for to pauseTimer
                    pauseTimer += lastPauseTimer;

                    backgroundSong.resume();

                    for (int i = 0; i < sprites.size(); i++) {
                        if (spriteIDs.get(i) == ID.Player) {
                            sprites.get(i).setVelocity(0, spriteVelocities[1][i]);
                        } else {
                            //Set the x and y velocity of the sprite to what they were before pausing
                            sprites.get(i).setVelocity(spriteVelocities[0][i], spriteVelocities[1][i]);
                        }

                        sprites.get(i).playAnimation();

                        isJustResumed = false;
                    }
                }
                for (int i = 0; i < sprites.size(); i++) {
                    Sprite s = sprites.get(i);

                    // Skip this sprite if it is far outside the viewable area
                    if (s.getX() > whiteKnight.getX() + SCREEN_WIDTH * 2
                            || s.getX() < whiteKnight.getX() - SCREEN_WIDTH * 2) {
                        continue;
                    }

                    if (spriteIDs.get(i) == ID.Player) {
                        s.update(elapsed);

                        //region Gravity and ground collision
                        if (isOnGround(s, tMap) && s.getVelocityY() >= 0) {
                            //Stops whiteKnight from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of whiteKnight is
                            int yTile = (int) ((s.getY() + s.getHeight()) / tMap.getTileHeight());

                            //Moves whiteKnight just out of the tile it landed on so there is no overlap
                            s.setY(tMap.getTileYC(0, yTile) - s.getHeight());
                        } else {
                            //Makes whiteKnight fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        //region Movement
                        //Handles left movement
                        if (aPressed && !dPressed) {
                            s.setAnimation(whiteKnightBobbingLeft);
                            isFacingRight = false;
                            if (isRecovering) s.setAnimation(whiteKnightRecoveringIdleLeft);
                            else {
                                if (isOnGround(s, tMap)) {
                                    s.setAnimation(whiteKnightBobbingLeft);
                                } else {
                                    s.setAnimation(whiteKnightIdleLeft);
                                }
                            }

                            s.setVelocityX(-0.3f);
                        }
                        //Handles right movement
                        else if (!aPressed && dPressed) {
                            s.setAnimation(whiteKnightBobbingRight);
                            isFacingRight = true;
                            if (isRecovering) s.setAnimation(whiteKnightRecoveringIdleRight);
                            else {
                                if (isOnGround(s, tMap)) {
                                    s.setAnimation(whiteKnightBobbingRight);
                                } else {
                                    s.setAnimation(whiteKnightIdleRight);
                                }
                            }

                            s.setVelocityX(0.3f);
                        }
                        //endregion

                        //region Jumping
                        if (spacePressed && isOnGround(s, tMap) && canJump) {
                            canJump = false;
                            //s jumps
                            s.setVelocityY(clamp((s.getVelocityY() + 0.1f) * -7.5f, -3, 0));

                            new Sound(jumpSoundFilepath).start();
                        }
                        //endregion

                        handleTileCollision(s, tMap, ID.Player);
                    } else if (spriteIDs.get(i) == ID.EnemyBlackKnight) {
                        Random rnd = new Random();

                        s.update(elapsed);

                        //region Gravity and ground collision
                        if (isOnGround(s, tMap) && s.getVelocityY() >= 0) {
                            //Stops s from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of s is
                            int yTile = (int) ((s.getY() + s.getHeight()) / tMap.getTileHeight());

                            //Moves s just out of the tile it landed on so there is no overlap
                            s.setY(tMap.getTileYC(0, yTile) - s.getHeight());

                            if (s.getAnimation() == blackKnightBobbingLeft || s.getAnimation() == blackKnightIdleLeft) {
                                s.setVelocityX(-0.2f);
                                s.setAnimation(blackKnightBobbingLeft);
                            } else {
                                s.setVelocityX(0.2f);
                                s.setAnimation(blackKnightBobbingRight);
                            }
                        } else {
                            //Makes s fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        //region Jumping
                        if (rnd.nextInt(200) == 1) {
                            if (isOnGround(s, tMap)) {
                                s.setVelocityY(Game.clamp((s.getVelocityY() + 0.1f) * -7.5f, -3, 0));
                                s.setVelocityX(0);
                                if (s.getAnimation() == blackKnightBobbingLeft || s.getAnimation() ==
                                        blackKnightIdleLeft)
                                    s.setAnimation(blackKnightIdleLeft);
                                else
                                    s.setAnimation(blackKnightIdleRight);

                                //Play the jump sound at a volume that depends on the proximity of the enemy to the
                                //player sprite (which may be silent)
                                new FadeWithDistanceFilterSound(jumpSoundFilepath, whiteKnight,
                                        sprites.get(i), SCREEN_WIDTH).start();

                            }
                        }
                        //endregion

                        handleTileCollision(s, tMap, spriteIDs.get(i));
                    } else if (spriteIDs.get(i) == ID.EnemyBlackPawn) {
                        s.update(elapsed);

                        //region Gravity and ground collision
                        if (isOnGround(s, tMap) && s.getVelocityY() >= 0) {
                            //Stops s from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of s is
                            int yTile = (int) ((s.getY() + s.getHeight()) / tMap.getTileHeight());

                            //Moves s just out of the tile it landed on so there is no overlap
                            s.setY(tMap.getTileYC(0, yTile) - s.getHeight());
                        } else {
                            //Makes s fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        handleTileCollision(s, tMap, spriteIDs.get(i));
                    } else if (spriteIDs.get(i) == ID.EnemyBlackRook) {
                        s.update(elapsed);

                        //region Gravity and ground collision
                        if (isOnGround(s, tMap) && s.getVelocityY() >= 0) {
                            //Stops s from falling
                            s.setVelocityY(0);

                            //Gets the number of tiles across and down the bottom-left corner of s is
                            int yTile = (int) ((s.getY() + s.getHeight()) / tMap.getTileHeight());

                            //Moves s just out of the tile it landed on so there is no overlap
                            s.setY(tMap.getTileYC(0, yTile) - s.getHeight());
                        } else {
                            //Makes s fall
                            s.setVelocityY(s.getVelocityY() + gravity * elapsed);
                        }
                        //endregion

                        handleTileCollision(s, tMap, spriteIDs.get(i));

                        if (whiteKnight.getX() + whiteKnight.getWidth() / 2f > s.getX() + s.getWidth() / 2f) {
                            s.setVelocityX(0.075f);
                        } else {
                            s.setVelocityX(-0.075f);
                        }
                    }

                    for (int j = 1; j < sprites.size(); j++) { //j initialised to 1 to avoid checking player sprite
                        Sprite s2 = sprites.get(j);

                        if (s2 != s) {
                            if (spriteIDs.get(i) == ID.Player) { //If s is the player sprite
                                //region Player on enemy collision
                                if (!isRecovering) {
                                    if (boundingBoxCollision(s, s2)) {
                                        //If the player died, return
                                        if (handleDamageCollision()) return;
                                    }
                                }
                                //endregion
                            } else {
                                //region Enemy on enemy collision
                                if (boundingBoxCollision(s, s2)) {
                                    //If both sprites are going left
                                    if (s.getVelocityX() < 0 && s2.getVelocityX() < 0) {
                                        //If s is the rightmost sprite
                                        if (s.getX() > s2.getX()) {
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
                                    else if (s.getVelocityX() > 0 && s2.getVelocityX() > 0) {
                                        //If s is the leftmost sprite
                                        if (s.getX() < s2.getX()) {
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
                                    else if (s.getY() + s.getHeight() > s2.getY() + s2.getHeight()) {
                                        //Change the direction s2 is going
                                        s2.setVelocityX(s2.getVelocityX() * -1);

                                        //If s2 is the rightmost sprite
                                        if (s2.getX() > s.getX()) {
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
                                    else if (s2.getY() + s2.getHeight() > s.getY() + s.getHeight()) {
                                        //Change the direction s is going
                                        s.setVelocityX(s.getVelocityX() * -1);

                                        //If s is the rightmost sprite
                                        if (s.getX() > s2.getX()) {
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
                                        if (s.getX() < s2.getX()) {
                                            //Move s so there is no overlap
                                            s.setX(s2.getX() - s.getWidth() - 1);
                                        } else {
                                            //Move s2 so there is no overlap
                                            s2.setX(s.getX() - s2.getWidth() - 1);
                                        }
                                    }

                                    //If s is a blackKnight
                                    if (spriteIDs.get(i) == ID.EnemyBlackKnight) {
                                        if (s.getVelocityX() < 0) {
                                            s.setAnimation(blackKnightBobbingLeft);
                                        } else {
                                            s.setAnimation(blackKnightBobbingRight);
                                        }
                                    }
                                    //If s2 is a blackKnight
                                    if (spriteIDs.get(j) == ID.EnemyBlackKnight) {
                                        if (s2.getVelocityX() < 0) {
                                            s2.setAnimation(blackKnightBobbingLeft);
                                        } else {
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
                    if (isRecovering && elapsedTimeSincePlayerEnemyCollision > 2000 + (pauseTimer / 1000000)) {
                        //...stop recovery process
                        isRecovering = false;
                        recoveryTimer = 0;

                        pauseTimer = 0;
                        lastPauseTimer = 0;

                        //Change whiteKnight back to correct animation
                        if (isFacingRight) {
                            if (whiteKnight.getVelocityX() > 0) whiteKnight.setAnimation(whiteKnightBobbingRight);
                            else whiteKnight.setAnimation(whiteKnightIdleRight);
                        } else {
                            if (whiteKnight.getVelocityX() < 0) whiteKnight.setAnimation(whiteKnightBobbingLeft);
                            else whiteKnight.setAnimation(whiteKnightIdleLeft);
                        }
                    }
                }
            }
        }
    }
}
