import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
 
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;
 
/**
 * Utility methods that compress and uncompress strings 
 */
public final class StringUtils
{
    /** Default buffer size */
    private static final int DEFAULT_BUFFER_SIZE = 4096;
 
 
 
 
 
    /** Default ctor - private to prohibit instantiation */
    private StringUtils() {}
 
 
 
 
    /**
     * Compress a string
     * @param uncompressed string
     * @return byte array containing compressed data
     * @throws IOException if the deflation fails
     */
    public static final byte [] compress(final String uncompressed) throws IOException
    {
        ByteArrayOutputStream baos  = new ByteArrayOutputStream();
        GZIPOutputStream gzos       = new GZIPOutputStream(baos);
 
        byte [] uncompressedBytes   = uncompressed.getBytes();
 
        gzos.write(uncompressedBytes, 0, uncompressedBytes.length);
        gzos.close();
 
        return baos.toByteArray();
    }
 
 
 
 
    /**
     * Uncompress a previously compressed string;
     * this method is the inverse of the compress method.
     * @param byte array containing compressed data
     * @return uncompressed string
     * @throws IOException if the inflation fails
     */
    public static final String uncompress(final byte [] compressed) throws IOException
    {
        String uncompressed = "";
 
        try
        {
            ByteArrayInputStream bais   = new ByteArrayInputStream(compressed);
            GZIPInputStream gzis        = new GZIPInputStream(bais);
 
            ByteArrayOutputStream baos  = new ByteArrayOutputStream();
            int numBytesRead            = 0;
            byte [] tempBytes           = new byte[DEFAULT_BUFFER_SIZE];
            while ((numBytesRead = gzis.read(tempBytes, 0, tempBytes.length)) != -1)
            {
                baos.write(tempBytes, 0, numBytesRead);
            }
 
            uncompressed = new String(baos.toByteArray());
        }
        catch (ZipException e)
        {
            e.printStackTrace(System.err);
        }
 
        return uncompressed;
    }
}