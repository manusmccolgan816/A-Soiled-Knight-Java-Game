import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * The FilteredSoundStream class is a FilterInputStream that applies a SoundFilter to the underlying input stream.
 */
public class FilteredSoundStream extends FilterInputStream {

    private static final int REMAINING_SIZE_UNKNOWN = -1;

    private SoundFilter soundFilter;
    private int remainingSize;

    /**
     * Creates a new FilteredSoundStream object with the specified InputStream and SoundFilter.
     *
     * @param in
     * @param soundFilter
     */
    public FilteredSoundStream(InputStream in, SoundFilter soundFilter)
    {
        super(in);
        this.soundFilter = soundFilter;
        remainingSize = REMAINING_SIZE_UNKNOWN;
    }

    public int read(byte[] samples, int offset, int length) throws IOException {
        //Read and filter the sound samples in the stream
        int bytesRead = super.read(samples, offset, length);
        if(bytesRead > 0) {
            soundFilter.filter(samples, offset, bytesRead);
            return bytesRead;
        }

        //If there are no remaining bytes in the sound stream, check if the filter has any remaining bytes ("echoes")
        if(remainingSize == REMAINING_SIZE_UNKNOWN) {
            remainingSize = soundFilter.getRemainingSize();
            //Round down to nearest multiple of 4 (typical frame size)
            remainingSize = remainingSize / 4 * 4;
        }
        if(remainingSize > 0) {
            length = Math.min(length, remainingSize);

            //Clear the buffer
            for(int i = offset; i < offset + length; i++) {
                samples[i] = 0;
            }

            //Filter the remaining bytes
            soundFilter.filter(samples, offset, length);
            remainingSize -= length;

            return length;
        }
        else {
            //End of stream
            return -1;
        }
    }
}
