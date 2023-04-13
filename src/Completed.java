import game2D.GameCore;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// Student ID: 2816175

/**
 * The Completed screen is that which is displayed when the player
 * completes the game.
 */
public class Completed extends GameCore implements MouseListener {

    private final Game game;
    private final Image imgCompleted;
    private final Image imgQuit;
    private final float quitPosX;
    private final float quitPosY;

    public Completed(Game game) {
        this.game = game;
        imgCompleted = loadImage("images/EndScreenResized.png");
        imgQuit = loadImage("images/QuitButtonEndScreenResized.png");
        quitPosX = 236 + getInsets().left;
        quitPosY = 332 + getInsets().top;
    }

    public void update(long elapsed) {

    }

    @Override
    public void draw(Graphics2D g) {
        pack();
        g.drawImage(imgCompleted, getInsets().left, getInsets().top, null);
        g.drawImage(imgQuit, (int) quitPosX, (int) quitPosY, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        //If Quit is clicked...
        if(mouseX >= quitPosX && mouseX <= (quitPosX + imgQuit.getWidth(null)) && mouseY >= quitPosY &&
                mouseY <= (quitPosY + imgQuit.getHeight(null))) {
            //Return to the menu screen
            game.initialiseMenu();
        }
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
}
