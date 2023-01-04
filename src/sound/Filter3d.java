package sound;

import game2D.Sprite;

/**
 * The sound.Filter3d class is a sound.SoundFilter that creates a 3D sound effect. The sound is filtered so that it is quieter
 * the farther away the sound source is from the listener.
 */
public class Filter3d extends SoundFilter {

    //Number of samples to shift while changing the volume
    private static final int NUM_SHIFTING_SAMPLES = 500;

    private Sprite source;
    private Sprite listener;
    private final int maxDistance;
    private float lastVolume;

    /**
     * Creates a new sound.Filter3d object with the specified source and listener Sprites. The Sprite's position can be
     * changed while this filter is running.
     *
     * @param source the Sprite that makes the sound
     * @param listener the Sprite that listens to the sound
     * @param maxDistance the maximum distance from which the sound can be heard
     */
    public Filter3d(Sprite source, Sprite listener, int maxDistance) {
        this.source = source;
        this.listener = listener;
        this.maxDistance = maxDistance;
        this.lastVolume = 0.0f;
    }

    /**
     * Filters the sound so that it gets more quiet with distance.
     *
     * @param samples the sound sample in an array of bytes
     * @param offset
     * @param length
     */
    public void filter(byte[] samples, int offset, int length) {
        if(source == null) {
            //Do nothing to filter - return
            return;
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
        for(int i = offset; i < offset + length; i += 2) {
            float volume = newVolume;

            //Shift from the last volume to the new volume so that volume change isn't abrupt
            if (shift < NUM_SHIFTING_SAMPLES) {
                volume = lastVolume + (newVolume - lastVolume) * shift / NUM_SHIFTING_SAMPLES;
                shift++;
            }

            //Change the volume of the sample
            short oldSample = SoundFilter.getSample(samples, i);
            short newSample = (short)(oldSample * volume);
            SoundFilter.setSample(samples, i, newSample);
        }
        lastVolume = newVolume;
    }
}
