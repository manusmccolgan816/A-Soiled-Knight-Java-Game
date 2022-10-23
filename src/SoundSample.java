/**
 The SoundSample class is a container for sound samples. The sound
 samples are format-agnostic and are stored as a byte array.
 */
public class SoundSample {

    private byte[] samples;

    /**
     * Create a new SoundSample object with the specified array of bytes.
     *
     * @param samples The sound sample array
     */
    public SoundSample(byte[] samples) {
        this.samples = samples;
    }

    /**
     * @return the sound sample as a byte array
     */
    public byte[] getSamples() {
        return samples;
    }
}
