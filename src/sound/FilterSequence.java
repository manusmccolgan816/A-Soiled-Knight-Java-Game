package sound;

/**
 The sound.FilterSequence class is a sound.SoundFilter that combines
 several SoundFilters at once.
 */
public class FilterSequence extends SoundFilter {

    private SoundFilter[] filters;

    /**
     Creates a new sound.FilterSequence object with the specified
     array of SoundFilters. The samples run through each
     sound.SoundFilter in the order of this array.
     */
    public FilterSequence(SoundFilter[] filters) {
        this.filters = filters;
    }


    /**
     Returns the maximum remaining size of all SoundFilters
     in this sound.FilterSequence.
     */
    public int getRemainingSize() {
        int max = 0;
        for (SoundFilter filter : filters) {
            max = Math.max(max, filter.getRemainingSize());
        }
        return max;
    }


    /**
     Resets each sound.SoundFilter in this sound.FilterSequence.
     */
    public void reset() {
        for (SoundFilter filter : filters) {
            filter.reset();
        }
    }


    /**
     Filters the sound sample through each sound.SoundFilter in this
     sound.FilterSequence.
     */
    public void filter(byte[] samples, int offset, int length) {
        for (SoundFilter filter : filters) {
            filter.filter(samples, offset, length);
        }
    }
}