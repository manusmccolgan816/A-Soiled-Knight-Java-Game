import game2D.GameCore;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class GameOver extends GameCore implements MouseListener {

    private Game game;
    private Image imgGameOverScreen;
    private Image imgRetry;
    private Image imgQuit;
    private float retryPosX;
    private float retryPosY;
    private float quitPosX;
    private float quitPosY;

    public GameOver(Game game) {
        this.game = game;
        imgGameOverScreen = loadImage("images/GameOverScreenResizedNoButtons.png");
        imgRetry = loadImage("images/RetryButtonGameOver.png");
        imgQuit = loadImage("images/QuitButtonGameOver.png");
        retryPosX = 192 + getInsets().left;
        retryPosY = 280 + getInsets().top;
        quitPosX = 192 + getInsets().left;
        quitPosY = 408 + getInsets().top;
    }

    public void update(long elapsed) {

    }

    @Override
    public void draw(Graphics2D g) {
        pack();
        g.drawImage(imgGameOverScreen, getInsets().left, getInsets().top, null);
        g.drawImage(imgRetry, (int) retryPosX, (int) retryPosY, null);
        g.drawImage(imgQuit, (int) quitPosX, (int) quitPosY, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        //If Retry is clicked...
        if(mouseX >= retryPosX && mouseX <= (retryPosX + imgRetry.getWidth(null)) && mouseY >= retryPosY &&
                mouseY <= (retryPosY + imgRetry.getHeight(null))) {
            //CURRENTLEVEL is decremented so that calling goToNextLevel() simply restarts the level the player died on
            Game.CURRENTLEVEL--;
            game.initialiseGame();
            game.goToNextLevel();
        }
        //If Quit is clicked...
        else if(mouseX >= quitPosX && mouseX <= (quitPosX + imgQuit.getWidth(null)) && mouseY >= quitPosY &&
                mouseY <= (quitPosY + imgQuit.getHeight(null))) {
            game.initialiseMenu();
            removeMouseListener(this);
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
