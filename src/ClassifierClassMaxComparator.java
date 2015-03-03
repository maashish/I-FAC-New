
import java.util.Comparator;
import java.util.HashMap;

public class ClassifierClassMaxComparator implements Comparator {

    private HashMap ruleIG;

    public ClassifierClassMaxComparator(HashMap ruleIG) {
        this.ruleIG = ruleIG;
    }

    public int compare(Object rule1, Object rule2) {
        double IG1, IG2;

        //if (((String) rule1).contains(antecedent) && ((String) rule2).contains(antecedent)) {

            IG1 = (Double) ruleIG.get(rule1);
            IG2 = (Double) ruleIG.get(rule2);

            if (IG1 > IG2) {
                return 1;
            } else if (IG1 < IG2) {
                return -1;
            }
        //}
        return 0; //meaning IG1 == IG2
    }
}
