package sound;

/**
 * An abstract class designed to filter sound samples. Since SoundFilters may use internal buffering of samples, a new
 * sound.SoundFilter object should be created for every sound played. However, SoundFilters can be reused after they are
 * finished by calling the reset() method.
 * Assimes all samples are 16 bit, signed, little-endian format.
 */
public abstract class SoundFilter {

    /**
     * Resets the sound filter. Does nothing by default.
     */
    public void reset() {
        //Do nothing
    }

    /**
     * Gets the remaining size, in bytes, that this filter plays after the sound is finished. An example would be an
     * echo that plays longer than its original sound. Returns 0 by default.
     * @return
     */
    public int getRemainingSize() {
        return 0;
    }

    /**
     * Filters an array of samples. Samples should be in 16 bit, signed, little-endian format.
     * @param samples
     */
    public void filter(byte[] samples) {
        filter(samples, 0, samples.length);
    }

    /**
     * Filters an array of samples. Samples should be in 16 but, signed, little-endian format. This method should be
     * implemented by subclasses. Note that the samples offset and length are number of bytes, not samples.
     *
     * @param samples
     * @param offset
     * @param length
     */
    public abstract void filter(byte[] samples, int offset, int length);

    /**
     * Convenience method for getting a 16 bit sample from a byte array. Samples should be in 16 bit, signed,
     * little-endian format.
     *
     * @param buffer the array of sound data
     * @param position the index in the buffer to be returned
     * @return the sample from the requested position
     */
    public static short getSample(byte[] buffer, int position) {
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
    public static void setSample(byte[] buffer, int position, short sample) {
        buffer[position] = (byte) (sample & 0xff);
        buffer[position+1] = (byte) ((sample >> 8) & 0xff);
    }
}
