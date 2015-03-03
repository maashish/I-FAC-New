
import java.util.Comparator;
import java.util.HashMap;

public class ClassifierComparatorImages implements Comparator {

    private HashMap ruleIG;

    public ClassifierComparatorImages(HashMap ruleIG) {
        this.ruleIG = ruleIG;
    }

    public int compare(Object rule1, Object rule2) {
        double IG1, IG2;

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
