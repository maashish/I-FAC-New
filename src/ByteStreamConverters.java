import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ByteStreamConverters {

    // using DataOutputStream
    public static byte[] intArrayToByteArray(final int[] integerArray) throws IOException {
        int index;
        byte byteArray[] = null;
        ByteArrayOutputStream bos;
        DataOutputStream dos;

        System.out.println("From intArrayToByteArray, integerArray.length: " + integerArray.length);

        for (index = 0; index < integerArray.length; index++) {
            bos = new ByteArrayOutputStream();
            dos = new DataOutputStream(bos);
            dos.writeInt(integerArray[index]);
            dos.flush();
            System.arraycopy(byteArray, (index * 4), bos.toByteArray(), 0, 4);
            bos.flush();
        }
        System.out.println("From intArrayToByteArray, byteArray.length: " + byteArray.length);
        return byteArray;
    }

    // converting back with data output stream
    public static int[] byteArrayToIntArray(final byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = null;
        DataInputStream in = null;
        int index, index1, tempIntArray[] = new int[1], integerArray[] = null;
        byte tempByteArray[] = null;

        System.out.println("From intArrayToByteArray, byteArray.length: " + byteArray.length);
        for (index = 0, index1 = 0; index < byteArray.length; index += 4, index1++) {
            System.arraycopy(tempByteArray, 0, byteArray, index, 4);
            bis = new ByteArrayInputStream(tempByteArray);
            in = new DataInputStream(bis);
            tempIntArray[0] = in.readInt();
            System.arraycopy(integerArray, index1, tempIntArray, 0, 1);
        }
        System.out.println("From intArrayToByteArray, integerArray.length: " + integerArray.length);
        return integerArray;
    }

    // using DataOutputStream
    public static byte[] charArrayToByteArray(final char[] charArray) throws IOException {
        int index;
        byte byteArray[] = null;
        ByteArrayOutputStream bos;
        DataOutputStream dos;

        System.out.println("From charArrayToByteArray, charArray.length: " + charArray.length);

        for (index = 0; index < charArray.length; index++) {
            bos = new ByteArrayOutputStream();
            dos = new DataOutputStream(bos);
            dos.writeChar(charArray[index]);
            dos.flush();
            System.arraycopy(byteArray, (index * 2), bos.toByteArray(), 0, 2);
            bos.flush();
        }
        System.out.println("From charArrayToByteArray, byteArray.length: " + byteArray.length);
        return byteArray;
    }

    // converting back with data output stream
    public static char[] byteArrayToCharArray(final byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = null;
        DataInputStream in = null;
        int index, index1;
        char tempCharArray[] = new char[1], charArray[] = null;
        byte tempByteArray[] = null;

        System.out.println("From byteArrayToCharArray, byteArray.length: " + byteArray.length);
        for (index = 0, index1 = 0; index < byteArray.length; index += 2, index1++) {
            System.arraycopy(tempByteArray, 0, byteArray, index, 2);
            bis = new ByteArrayInputStream(tempByteArray);
            in = new DataInputStream(bis);
            tempCharArray[0] = in.readChar();
            System.arraycopy(charArray, index1, tempCharArray, 0, 1);
        }
        System.out.println("From byteArrayToCharArray, charArray.length: " + charArray.length);
        return charArray;
    }

    // using DataOutputStream
    /*public static byte[] shortArrayToByteArray(final short[] shortArray) throws IOException {
        int index;
        byte byteArray[] = new byte[shortArray.length * 2], tempByteArray[];
        ByteArrayOutputStream bos;
        DataOutputStream dos;

        System.out.println("From shortArrayToByteArray, shortArray.length: " + shortArray.length);

        for (index = 0; index < shortArray.length; index++) {
            bos = new ByteArrayOutputStream();
            dos = new DataOutputStream(bos);
            dos.writeShort(shortArray[index]);
            dos.flush();
            dos.close();
            bos.close();

            tempByteArray = bos.toByteArray();
            System.arraycopy(byteArray, (index * 2), tempByteArray, 0, 2);
            System.out.println("From shortArrayToByteArray, shortArray[index]: " + shortArray[index]);
            System.out.println("From shortArrayToByteArray, bos.toByteArray(): " + tempByteArray[0] + " " + tempByteArray[1]);
        }
        System.out.println("From shortArrayToByteArray, byteArray.length: " + byteArray.length);
        return byteArray;
    }*/

    public static byte[] shortArrayToByteArray(final short[] shortArray) throws IOException {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;
        int arrayLength = shortArray.length;
        byte byteArray[] = null;
        Short []shortArrayObject = new Short [arrayLength];;

        int index, index1;
        /*for (short e : shortArray) {
            shortArrayObject[index] = e;
            index++;
        }

        List tempList = Arrays.asList(shortArrayObject);*/

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(shortArray);
        oos.flush();
        oos.close();
        bos.close();
        byteArray = bos.toByteArray();

        //System.out.println("From shortArrayToByteArray, shortArray.length: " + shortArray.length);
        //System.out.println("From shortArrayToByteArray, byteArray.length: " + byteArray.length);

        /*for (index = 0; index < shortArray.length; index++) {
            System.out.println("From shortArrayToByteArray, shortArray[index]: " + shortArray[index]);
            System.out.println("From shortArrayToByteArray, bos.toByteArray(): " + byteArray[index * 2] + " " + byteArray[(index * 2) + 1]);
        }*/
        
        return byteArray;
    }

    public static byte[] floatArrayToByteArray(final float[] floatArray) throws IOException {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;
        int arrayLength = floatArray.length;
        byte byteArray[] = null;
        float []floatArrayObject = new float [arrayLength];;

        int index, index1;
        /*for (float e : floatArray) {
            floatArrayObject[index] = e;
            index++;
        }

        List tempList = Arrays.asList(floatArrayObject);*/

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        oos.writeObject(floatArray);
        oos.flush();
        oos.close();
        bos.close();
        byteArray = bos.toByteArray();

        //System.out.println("From floatArrayToByteArray, floatArray.length: " + floatArray.length);
        //System.out.println("From floatArrayToByteArray, byteArray.length: " + byteArray.length);

        /*for (index = 0; index < floatArray.length; index++) {
            System.out.println("From floatArrayToByteArray, floatArray[index]: " + floatArray[index]);
            System.out.println("From floatArrayToByteArray, bos.toByteArray(): " + byteArray[index * 2] + " " + byteArray[(index * 2) + 1]);
        }*/

        return byteArray;
    }

    // converting back with data output stream
    /*public static short[] byteArrayToShortArray(final byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = null;
        DataInputStream in = null;
        int index, index1, arrayLength = (int) Math.ceil(byteArray.length / 2);
        short tempShortArray[] = new short[1], shortArray[] = new short [arrayLength];
        byte tempByteArray[] = new byte[2];

        System.out.println("From byteArrayToShortArray, byteArray.length: " + byteArray.length);
        for (index = 0, index1 = 0; index < byteArray.length; index += 2, index1++) {
            System.arraycopy(tempByteArray, 0, byteArray, index, 2);
            bis = new ByteArrayInputStream(tempByteArray);
            in = new DataInputStream(bis);
            tempShortArray[0] = in.readShort();
            System.arraycopy(shortArray, index1, tempShortArray, 0, 1);
            System.out.println("From byteArrayToShortArray, byteArray[index]: " + byteArray[index] + " " + byteArray[index+1]);
            System.out.println("From byteArrayToShortArray, shortArray[index1]: " + shortArray[index1]);
        }
        System.out.println("From byteArrayToShortArray, shortArray.length: " + shortArray.length);
        return shortArray;
    }*/

    // converting back with data output stream
    public static short[] byteArrayToShortArray(final byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = null;
        ObjectInputStream in = null;
        List <Short> tempList = null;
        int index, index1, arrayLength = (int) Math.ceil(byteArray.length / 2);
        short tempShortArray[] = new short[1], shortArray[] = null;
        Short [] shortArrayObject = null;
        byte tempByteArray[] = new byte[2];

        bis = new ByteArrayInputStream(byteArray);
        in = new ObjectInputStream(bis);
        try {
            shortArray = (short[]) in.readObject();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }

        /*System.out.println("From byteArrayToShortArray, tempList.size(): " + tempList.size());
        System.out.println("From byteArrayToShortArray, tempList: " + tempList);
        shortArrayObject = tempList.toArray(new Short[0]);
        System.out.println("From byteArrayToShortArray, shortArrayObject.length: " + shortArrayObject.length);

        index = 0;
        for (Short e : shortArrayObject) {
            shortArray[index] = e.shortValue();
            index++;
        }*/

        //System.out.println("From byteArrayToShortArray, byteArray.length: " + byteArray.length);
        //System.out.println("From byteArrayToShortArray, shortArray.length: " + shortArray.length);
        /*for (index = 0, index1 = 0; index < byteArray.length; index += 2, index1++) {

            System.out.println("From byteArrayToShortArray, byteArray[index]: " + byteArray[index] + " " + byteArray[index+1]);
            System.out.println("From byteArrayToShortArray, shortArray[index1]: " + shortArray[index1]);
        }*/
        
        return shortArray;
    }

    public static float[] byteArrayToFloatArray(final byte[] byteArray) throws IOException {
        ByteArrayInputStream bis = null;
        ObjectInputStream in = null;
        List <Float> tempList = null;
        int index, index1, arrayLength = (int) Math.ceil(byteArray.length / 2);
        float tempfloatArray[] = new float[1], floatArray[] = null;
        float [] floatArrayObject = null;
        byte tempByteArray[] = new byte[2];

        bis = new ByteArrayInputStream(byteArray);
        in = new ObjectInputStream(bis);
        try {
            floatArray = (float[]) in.readObject();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }

        /*System.out.println("From byteArrayTofloatArray, tempList.size(): " + tempList.size());
        System.out.println("From byteArrayTofloatArray, tempList: " + tempList);
        floatArrayObject = tempList.toArray(new float[0]);
        System.out.println("From byteArrayTofloatArray, floatArrayObject.length: " + floatArrayObject.length);

        index = 0;
        for (float e : floatArrayObject) {
            floatArray[index] = e.floatValue();
            index++;
        }*/

        //System.out.println("From byteArrayTofloatArray, byteArray.length: " + byteArray.length);
        //System.out.println("From byteArrayTofloatArray, floatArray.length: " + floatArray.length);
        /*for (index = 0, index1 = 0; index < byteArray.length; index += 2, index1++) {

            System.out.println("From byteArrayTofloatArray, byteArray[index]: " + byteArray[index] + " " + byteArray[index+1]);
            System.out.println("From byteArrayTofloatArray, floatArray[index1]: " + floatArray[index1]);
        }*/

        return floatArray;
    }
}
