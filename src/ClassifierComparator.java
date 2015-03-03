
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ClassifierComparator implements Comparator {

    private HashMap ruleIG;

    public ClassifierComparator(HashMap ruleIG) {
        this.ruleIG = ruleIG;
    }

    public int compare(Object rule1, Object rule2) {
        double IG1, IG2;
        int commaCount1, commaCount2;
        StringTokenizer st;

        st = new StringTokenizer((String) rule1, ",");
        commaCount1 = st.countTokens();

        st = new StringTokenizer((String) rule2, ",");
        commaCount2 = st.countTokens();
        
        st = null;

        if (commaCount1 < commaCount2) {
            return 1;
        } else if (commaCount1 > commaCount2) {
            return -1;
        } else {
            IG1 = (Double) ruleIG.get(rule1);
            IG2 = (Double) ruleIG.get(rule2);

            if (IG1 < IG2) {
                return 1;
            } else if (IG1 > IG2) {
                return -1;
            } else {
                return 0; //meaning IG1 == IG2
            }
        }
    }
}
