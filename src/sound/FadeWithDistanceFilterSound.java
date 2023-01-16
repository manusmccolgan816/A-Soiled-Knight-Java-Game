package sound;

import game2D.Sound;
import game2D.Sprite;

import javax.sound.sampled.*;
import java.io.File;

public class FadeWithDistanceFilterSound extends Sound {

    private final String filename;    // The name of the file to play
    private final Sprite source;
    private final Sprite listener;
    private final int maxDistance;

    public FadeWithDistanceFilterSound(String fname, Sprite source, Sprite listener, int maxDistance) {
        super(fname);

        this.filename = fname;
        this.source = source;
        this.listener = listener;
        this.maxDistance = maxDistance;
    }

    public void run() {
        try {
            File file = new File(filename);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();

            FadeWithDistanceFilter filtered = new FadeWithDistanceFilter(stream, source, listener, maxDistance);
            AudioInputStream fStream = new AudioInputStream(filtered, format, stream.getFrameLength());

            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(fStream);
            clip.start();
            Thread.sleep(100);
            while (clip.isRunning()) {
                Thread.sleep(100);
            }
            clip.close();
        } catch (Exception ignored) {
        }
    }
}
