package sound;

import game2D.Sprite;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * A filter to reduce the volume of a sound with distance. If the maximum
 * distance is exceeded, no sound will be played.
 */
public class FadeWithDistanceFilter extends FilterInputStream {

    //Number of samples to shift while changing the volume
    private static final int NUM_SHIFTING_SAMPLES = 500;

    private Sprite source;
    private Sprite listener;
    private final int maxDistance;
    private float lastVolume;

    private static final Logger LOGGER = Logger.getLogger(FadeWithDistanceFilter.class.getName());

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    protected FadeWithDistanceFilter(InputStream in, Sprite source, Sprite listener, int maxDistance) {
        super(in);

        this.source = source;
        this.listener = listener;
        this.maxDistance = maxDistance;
        this.lastVolume = 0.0f;
    }

    /**
     * Convenience method for getting a 16 bit sample from a byte array. Samples should be in 16 bit, signed,
     * little-endian format.
     *
     * @param buffer the array of sound data
     * @param position the index in the buffer to be returned
     * @return the sample from the requested position
     */
    public short getSample(byte[] buffer, int position) {
        return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
    }

    /**
     * Convenience method for setting a 16 bit sample  in a byte array. Samples should be in 16 bit, signed,
     * little-endian format.
     *
     * @param buffer the array of sound data
     * @param position the index in the buffer in which to assign the sample
     * @param sample the new sample to assign to the requested position
     */
    public void setSample(byte[] buffer, int position, short sample) {
        buffer[position] = (byte) (sample & 0xff);
        buffer[position+1] = (byte) ((sample >> 8) & 0xff);
    }

    public int read(byte[] samples, int offset, int length) throws IOException {
        int bytesRead = super.read(samples,offset,length);
        if(source == null) {
            return length;
        }

        //Calculate the listener's distance from the sound source
        float dx = (source.getX() - listener.getX());
        float dy = (source.getY() - listener.getY());
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        //Set volume from 0 (no sound) to 1
        float newVolume = (maxDistance - distance) / maxDistance;
        if (newVolume <= 0) {
            newVolume = 0;
        }

        //Set the volume of the sample
        int shift = 0;
        for(int i = offset; i < bytesRead; i += 2) {
            float volume = newVolume;

            //Shift from the last volume to the new volume so that volume change isn't abrupt
            if (shift < NUM_SHIFTING_SAMPLES) {
                volume = lastVolume + (newVolume - lastVolume) * shift / NUM_SHIFTING_SAMPLES;
                shift++;
            }

            //Change the volume of the sample
            short oldSample = getSample(samples, i);
            short newSample = (short)(oldSample * volume);
            setSample(samples, i, newSample);
        }
        lastVolume = newVolume;
        //LOGGER.info("Volume is " + lastVolume);

        return length;
    }
}
