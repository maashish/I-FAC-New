import java.util.ArrayList;
import java.util.StringTokenizer;

public class Utils {
    public static ArrayList convertStringToArrayList(String string, String delimiter) {
        StringTokenizer st = new StringTokenizer(string, delimiter);
        ArrayList stringAL = new ArrayList();
        while(st.hasMoreTokens()) {
            stringAL.add(st.nextToken());
        }
        return stringAL;
    }
}
