import javax.sound.midi.*;
import java.io.File;

public class PlayMIDI {

    private Sequence score;
    private Sequencer seq;

    public void play(String filename) throws Exception {
        //Get a reference to the MIDI data stored in the file
        score = MidiSystem.getSequence(new File(filename));
        //Get a reference to the sequencer that will play it
        seq = MidiSystem.getSequencer();

        //Ensure the song loops endlessly
        seq.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);

        //Open the sequencer and play the sequence (score)
        seq.open();
        seq.setSequence(score);
        seq.start();
    }

    public void stop() {
        seq.stop();
    }

    public void resume() {
        seq.start();
    }
}
