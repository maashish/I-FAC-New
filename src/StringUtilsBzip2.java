import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
 
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

 
/**
 * Utility methods that compress and uncompress strings 
 */
public final class StringUtilsBzip2
{
    /** Default buffer size */
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    
    private StringUtilsBzip2() {}
 
    /**
     * Compress a string
     * @param uncompressed string
     * @return byte array containing compressed data
     * @throws IOException if the deflation fails
     */
    public static final byte [] compress(final byte [] uncompressedBytes) throws IOException
    {
        //byte [] compressed = null;
        Deflater compresser = new Deflater();
        compresser.setLevel(Deflater.BEST_SPEED);
        //compresser.setInput(uncompressedBytes);
        //compresser.finish();

        //compresser.deflate(compressed);
        //compresser.end();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream gzos = new DeflaterOutputStream(baos, compresser);
 
        //byte [] uncompressedBytes   = uncompressed.getBytes();
 
        gzos.write(uncompressedBytes, 0, uncompressedBytes.length);
        gzos.close();
 
        return baos.toByteArray();
        //return compressed; 
    }
 
    /**
     * Uncompress a previously compressed string;
     * this method is the inverse of the compress method.
     * @param byte array containing compressed data
     * @return uncompressed string
     * @throws IOException if the inflation fails
     */
    public static final byte [] uncompress(final byte [] compressed) throws IOException
    {
        Inflater decompresser = new Inflater();
        byte [] uncompressed = null;
 
        //try
        //{
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            InflaterInputStream  gzis = new InflaterInputStream(bais, decompresser);
            //decompresser.setInput(compressed);
            //decompresser.inflate(uncompressed);
            //decompresser.end();

            ByteArrayOutputStream baos  = new ByteArrayOutputStream();
            int numBytesRead            = 0;
            byte [] tempBytes           = new byte[DEFAULT_BUFFER_SIZE];
            while ((numBytesRead = gzis.read(tempBytes, 0, tempBytes.length)) != -1)
            {
                baos.write(tempBytes, 0, numBytesRead);
            }
            
            tempBytes = null;
 
            uncompressed = baos.toByteArray();
        //}
        //catch ( e)
        //{
       //     e.printStackTrace(System.err);
        //}
 
        return uncompressed;
    }
}