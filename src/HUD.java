import game2D.GameCore;

import java.awt.*;

// Student ID: 2816175

/**
 * The HUD displays the player's health, the current level and
 * the number of horse shoes collected.
 */
public class HUD extends GameCore {

    public static final int HUD_POS_X = 10;
    public static final int HUD_POS_Y = 32;
    private int health; //The player's health, represented by the number of filled hearts
    private int maxHealth; //The max number of hearts the player can have
    private int numHorseShoesCollected; //The number of horse shoes the player has collected in the current level
    private int numHorseShoes; //The total number of horse shoes in the current level

    private Game game;

    public HUD(Game game, int health, int maxHealth, int numHorseShoesCollected, int numHorseShoes) {
        this.game = game;
        this.health = health;
        this.maxHealth = maxHealth;
        this.numHorseShoesCollected = numHorseShoesCollected;
        this.numHorseShoes = numHorseShoes;
    }

    @Override
    public void draw(Graphics2D g) {
        //Displaying health info
        int heartsDrawn = 0;
        int nextHeartX = HUD_POS_X + 10;
        int nextHeartY = HUD_POS_Y + 14;

        while(heartsDrawn < health) {
           Image imgFilledHeart = loadImage("images/FilledHeart.png");

           //Draw filled heart
            g.drawImage(imgFilledHeart, nextHeartX, nextHeartY, null);
            heartsDrawn++;
            nextHeartX += imgFilledHeart.getWidth(null) + 10;
        }
        while(heartsDrawn < maxHealth) {
            Image imgEmptyHeart = loadImage("images/EmptyHeart.png");

            //Draw empty heart
            g.drawImage(imgEmptyHeart, nextHeartX, nextHeartY, null);
            heartsDrawn++;
            nextHeartX += imgEmptyHeart.getWidth(null) + 10;
        }

        g.setColor(ColourRepository.WHITE);
        g.fillRect(nextHeartX + 5, HUD_POS_Y + 5, 165, 50);

        //Display level number
        g.setColor(ColourRepository.BLACK);
        g.drawString("Level: " + Game.currentLevel, nextHeartX + 10, nextHeartY + 10);

        //Display horse shoe info
        g.drawString("Horse shoes collected: " + numHorseShoesCollected + "/" + numHorseShoes, nextHeartX + 10, nextHeartY + 30);

        if (Game.isDebugModeOn) {
            g.setColor(ColourRepository.WHITE);
            g.fillRect(nextHeartX + 5, HUD_POS_Y + 50, 165, 65);

            g.setColor(Color.RED);
            g.drawString("x: " + game.getWhiteKnight().getX(), nextHeartX + 10, nextHeartY + 50);
            g.drawString("y: " + game.getWhiteKnight().getY(), nextHeartX + 10, nextHeartY + 70);
            g.drawString("FPS: " + game.getFPS(), nextHeartX + 10, nextHeartY + 90);
        }
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getNumHorseShoesCollected() {
        return  numHorseShoesCollected;
    }

    public int getNumHorseShoes() {
        return numHorseShoes;
    }

    public void incrementHorseShoesCollected() {
        numHorseShoesCollected++;
    }
}
