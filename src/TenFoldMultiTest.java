
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

public class TenFoldMultiTest {

    public void generateParameters(BigDecimal startNumberOfRules, BigDecimal minNumberOfRules, BigDecimal maxNumberOfRules, BigDecimal stepSizeNumberOfRules, int maxRuleLength) {
        int index;
        BigDecimal numberOfRules;
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("multiTestParameters.txt", false), true);
            for (index = 1; index <= maxRuleLength; index++) {
                pw.println(startNumberOfRules + "," + index);
            }
            for (numberOfRules = minNumberOfRules; numberOfRules.compareTo(maxNumberOfRules) <= 0 ; numberOfRules = numberOfRules.add(stepSizeNumberOfRules)) {
                for (index = 1; index <= maxRuleLength; index++) {
                    pw.println(numberOfRules + "," + index);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TenFoldMultiTest().generateParameters(new BigDecimal(args[0]), new BigDecimal(args[1]), new BigDecimal(args[2]), new BigDecimal(args[3]), Integer.parseInt(args[4]));
    }

}
