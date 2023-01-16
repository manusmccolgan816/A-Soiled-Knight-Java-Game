import game2D.GameCore;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Controls screen displays the controls of the game.
 */
public class Controls extends GameCore implements MouseListener {

    private Image imgControlsBackground;
    private Image imgBack;

    private int backPosX;
    private int backPosY;

    private Game game;

    public Controls(Game game) {
        this.game = game;

        pack();

        imgControlsBackground = loadImage("images/ControlsScreenNoButtonsResized.png");
        imgBack = loadImage("images/BackButtonControlsResized.png");

        backPosX = 20 + getInsets().left;
        backPosY = 476 + getInsets().top;
    }

    @Override
    public void draw(Graphics2D g) {
        g.drawImage(imgControlsBackground, getInsets().left, getInsets().top, null);
        g.drawImage(imgBack, backPosX, backPosY, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        //If Back is clicked...
        if(mouseX >= backPosX && mouseX <= (backPosX + imgBack.getWidth(null)) && mouseY >= backPosY &&
                mouseY <= backPosY + imgBack.getHeight(null)) {
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
