import java.io.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;

/**
 * The SoundManager class manages sound playback. The SoundManager is a ThreadPool, with each thread playing back one
 * sound at a time. This allows the SoundManager to easily limit the number of simultaneous sounds being played.
 */
public class SoundManager extends ThreadPool {

    private AudioFormat playbackFormat;
    private ThreadLocal localLine;
    private ThreadLocal localBuffer;
    private Object pausedLock;
    private boolean paused;

    /**
     * Creates a new SoundManager with the specified maximum number of simultaneous sounds.
     *
     * @param playbackFormat the file format of the audio clips
     * @param maxSimultaneousSounds the maximum number of sounds that can play at once
     */
    public SoundManager(AudioFormat playbackFormat, int maxSimultaneousSounds) {
        super(maxSimultaneousSounds);
        this.playbackFormat = playbackFormat;
        localLine = new ThreadLocal();
        localBuffer = new ThreadLocal();
        pausedLock = new Object();
        //Notify threads in pool it's okay to start
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * @param playbackFormat the audio format of the sound
     * @return the maximum number of simultaneous sounds with the specified AudioFormat that the default mixer can play.
     */
    public static int getMaxSimultaneousSounds(AudioFormat playbackFormat)
    {
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, playbackFormat);
        Mixer mixer = AudioSystem.getMixer(null);
        int maxLines = mixer.getMaxLines(lineInfo);
        if(maxLines == AudioSystem.NOT_SPECIFIED) {
            maxLines = 32;
        }
        return maxLines;
        //return mixer.getMaxLines(lineInfo);
    }

    /**
     * Does any clean up before closing.
     */
    protected void cleanUp() {
        //Signal to unpause
        setPaused(false);
        //Close the mixer (stops any running sounds)
        Mixer mixer = AudioSystem.getMixer(null);
        if(mixer.isOpen()) {
            mixer.close();
        }
    }

    public void close() {
        cleanUp();
        super.close();
    }

    public void join() {
        cleanUp();
        super.join();
    }

    /**
     * Sets the paused state. Sounds may not pause immediately.
     *
     * @param paused a boolean to pause or unpause
     */
    public void setPaused(boolean paused) {
        if(this.paused != paused) {
            synchronized (pausedLock) {
                this.paused = paused;
                if(!paused) {
                    //Restart sounds
                    pausedLock.notifyAll();
                }
            }
        }
    }

    /**
     * @return the paused state
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Loads a sound from the file system.
     *
     * @param filename the name of the sound file
     * @return the sound sample or null if an error occurs
     */
    public SoundSample getSound(String filename) {
        return getSound(getAudioInputStream(filename));
    }

    /**
     * Loads a sound from an AudioInputStream.
     *
     * @param audioStream the audio stream of the sound
     * @return the sound sample
     */
    public SoundSample getSound(AudioInputStream audioStream) {
        if(audioStream == null) {
            return null;
        }

        //Get the number of bytes to read
        int length = (int) (audioStream.getFrameLength() * audioStream.getFormat().getFrameSize());

        //Read the entire stream
        byte[] samples = new byte[length];
        DataInputStream is = new DataInputStream(audioStream);
        try {
            is.readFully(samples);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        //Return the samples
        return new SoundSample(samples);
    }

    /**
     * Creates an AudioInputStream from a sound from the file system.
     *
     * @param filename the name of the sound file
     * @return the AudioInputStream in the correct format
     */
    public AudioInputStream getAudioInputStream(String filename) {
        try {
            //Open the source file
            AudioInputStream source = AudioSystem.getAudioInputStream(new File(filename));

            //Convert to playback format
            return AudioSystem.getAudioInputStream(playbackFormat, source);
        }
        catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Plays a sound.
     *
     * @param sound the sound to play
     * @return
     */
    public InputStream play(SoundSample sound) {
        return play(sound, null, false);
    }

    /**
     * Plays a sound with an optional SoundFilter, and optionally looping. This method returns immediately.
     *
     * @param sound the sound to play
     * @param filter the optional filter to play over the sound
     * @param loop whether to loop the sound
     * @return
     */
    public InputStream play(SoundSample sound, SoundFilter filter, boolean loop) {
        InputStream is;
        if(sound != null) {
            if(loop) {
                is = new LoopingByteInputStream(sound.getSamples());
            }
            else {
                is = new ByteArrayInputStream(sound.getSamples());
            }

            return play(is, filter);
        }
        return null;
    }

    /**
     * Plays a sound from an InputStream. THis method returns immediately.
     *
     * @param is the InputStream from which to play the sound
     * @return
     */
    public InputStream play(InputStream is) {
        return play(is, null);
    }

    /**
     * Plays a sound from an InputStream with an optional sound filter. This method returns immediately.
     *
     * @param is the InputStream from which to play the sound
     * @param filter the optional filter to play over the sound
     * @return
     */
    public InputStream play(InputStream is, SoundFilter filter) {
        if(is != null) {
            if(filter != null) {
                is = new FilteredSoundStream(is, filter);
            }
            runTask(new SoundPlayer(is));
        }
        return is;
    }

    /**
     * Signals that a PooledThread has started. Creates the Thread's line and buffer.
     */
    protected void threadStarted() {
        //Wait for the SoundManager constructor to finish
        synchronized (this) {
            try {
                wait();
            }
            catch (InterruptedException ex) { }
        }

        //Use a short, 100ms buffer for filters that change in real time
        int bufferSize = playbackFormat.getFrameSize() * Math.round(playbackFormat.getSampleRate() / 10);

        //Create, open and start the line
        SourceDataLine line;
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, playbackFormat);
        try {
            line = (SourceDataLine)AudioSystem.getLine(lineInfo);
            line.open(playbackFormat, bufferSize);
        }
        catch (LineUnavailableException ex) {
            //The line is unavailable - signal to end this thread
            Thread.currentThread().interrupt();
            return;
        }

        line.start();

        //Create the buffer
        byte[] buffer = new byte[bufferSize];

        //Set this thread's locals
        localLine.set(line);
        localBuffer.set(buffer);
    }

    /**
     * Signals that a PooledThread has stopped. Drains and closes the thread's Line.
     */
    protected void threadStopped() {
        SourceDataLine line = (SourceDataLine) localLine.get();
        if(line != null) {
            line.drain();
            line.close();
        }
    }

    protected class SoundPlayer implements Runnable {

        private InputStream source;

        public SoundPlayer(InputStream source) {
            this.source = source;
        }

        public void run() {
            //Get line and buffer from ThreadLocals
            SourceDataLine line = (SourceDataLine) localLine.get();
            byte[] buffer = (byte[]) localBuffer.get();
            if (line == null || buffer == null) {
                //The line is unavailable
                return;
            }
            //Copy data to the line
            try {
                int numBytesRead = 0;
                while (numBytesRead != -1) {
                    //If paused, wait until unpaused
                    synchronized (pausedLock) {
                        if (paused) {
                            try {
                                pausedLock.wait();
                            } catch (InterruptedException ex) {
                                return;
                            }
                        }
                    }
                    //Copy data
                    numBytesRead = source.read(buffer, 0, buffer.length);
                    if (numBytesRead != -1) {
                        line.write(buffer, 0, numBytesRead);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
