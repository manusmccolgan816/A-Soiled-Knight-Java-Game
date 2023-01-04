import game2D.Animation;
import game2D.GameCore;
import game2D.Sound;
import game2D.Sprite;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Menu extends GameCore implements MouseListener, MouseMotionListener {

    private final Animation titleIdle;
    private final Animation whiteKnightIdleRight;

    private final Sprite titleText;
    private final Sprite whiteKnight;

    private final Image imgTitleBackground;
    private final Image imgTitleText;
    private final Image imgStart;
    private final Image imgControls;
    private final Image imgWhiteKnightIdleRight;

    private final float startPosX;
    private final float startPosY;
    private final float controlsPosX;
    private final float controlsPosY;

    Sound titleSong;

    private final Game game;

    public Menu(Game game) {
        this.game = game;

        //Start the title theme
        titleSong = new Sound("sounds/ChessTitleThemeSecond.wav");
        titleSong.start();

        pack();
        imgTitleBackground = loadImage("images/TitleScreenResizedBackground.png");
        imgTitleText = loadImage("images/TitleTextResized.png");
        imgStart = loadImage("images/StartButtonResized.png");
        imgControls = loadImage("images/ControlsButtonResized.png");
        imgWhiteKnightIdleRight = loadImage("images/WhiteKnightResizedRight.png");

        //Set the positions of the buttons
        startPosX = 208 + getInsets().left;
        startPosY = 324 + getInsets().top;
        controlsPosX = 208 + getInsets().left;
        controlsPosY = 452 + getInsets().top;

        titleIdle = new Animation();
        titleIdle.addFrame(imgTitleText, 1000);
        whiteKnightIdleRight = new Animation();
        whiteKnightIdleRight.addFrame(imgWhiteKnightIdleRight, 1000);

        titleText = new Sprite(titleIdle);
        whiteKnight = new Sprite(whiteKnightIdleRight);

        titleText.setPosition(76 + getInsets().left, -153);
        titleText.setVelocityY(0.015f);
        titleText.show();
    }

    @Override
    public void draw(Graphics2D g) {
        g.drawImage(imgTitleBackground, getInsets().left, getInsets().top, imgTitleBackground.getWidth(null),
                imgTitleBackground.getHeight(null), null);
        g.drawImage(imgStart, (int) startPosX, (int) startPosY, imgStart.getWidth(null),
                imgStart.getHeight(null), null);
        g.drawImage(imgControls, (int) controlsPosX, (int) controlsPosY, imgControls.getWidth(null),
                imgControls.getHeight(null), null);
        titleText.draw(g);
        whiteKnight.draw(g);
    }

    @Override
    public void update(long elapsed) {
        if(titleText.getY() >= 76) {
            titleText.setVelocityY(0);
            titleText.setY(76);
        }

        titleText.update(elapsed);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        //If Start is clicked...
        if(mouseX >= startPosX && mouseX <= (startPosX + imgStart.getWidth(null)) && mouseY >= startPosY &&
                mouseY <= startPosY + imgStart.getHeight(null)) {
            titleSong.stopSong();
            game.init();
        }
        else if(mouseX >= controlsPosX && mouseX <= (controlsPosX + imgControls.getWidth(null)) && mouseY >=
                controlsPosY && mouseY <= controlsPosY + imgControls.getHeight(null)) {
            titleSong.stopSong();
            game.initialiseControls();
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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(e.getX() >= startPosX && e.getX() <= startPosX + imgStart.getWidth(null) &&
                e.getY() >= startPosY && e.getY() <= startPosY + imgStart.getHeight(null)) {
            whiteKnight.setPosition(139, 337);
            whiteKnight.show();
        }
        else if(e.getX() >= controlsPosX && e.getX() <= controlsPosX + imgStart.getWidth(null) &&
                e.getY() >= controlsPosY && e.getY() <= controlsPosY + imgStart.getHeight(null)) {
            whiteKnight.setPosition(139, 465);
            whiteKnight.show();
        }
        else {
            whiteKnight.hide();
        }
    }
}
