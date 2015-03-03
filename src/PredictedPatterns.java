
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class PredictedPatterns {

    public static void main(String[] args) {
        FARMORBzip2ByteArraySubPartitionZipSparseRapLeaf farmor = null;
        farmor = new FARMORBzip2ByteArraySubPartitionZipSparseRapLeaf();
        farmor.generateAssociationRules(0, "GLOBAL", "ARMParameters.txt", false);
        farmor.generateAssociationRulesSecondPhase(0, "GLOBAL");
        new PredictedPatterns().sortItemsets("frequentItemsets_0_GLOBAL.txt", "fuzzyTrainingData0.txt");
    }

    public void sortItemsets(String fileName, String trainingFileName) {
        String rule, line;
        BufferedReader bufReader = null;
        PrintWriter pw = null;
        StringTokenizer st = null;
        ArrayList rules = null;
        HashMap rulesIG = null;
        double support;
        int previousLength = 0, trainFileLength = 0;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("PredictedPatterns.txt", false), true);
            rules = new ArrayList();
            rulesIG = new HashMap();
            
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                rule = st.nextToken();
                rules.add(rule);
                support = Double.parseDouble(st.nextToken());
                rulesIG.put(rule, support);
            }
            Collections.sort(rules, new ClassifierComparator(rulesIG));
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(trainingFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                trainFileLength++;
            }
            bufReader.close();

            for(int index = 0; index < rules.size(); index++) {
                rule = (String) rules.get(index);
                st = new StringTokenizer(rule, ",");
                if(index != 0 && previousLength != st.countTokens()) {
                    pw.println();
                }
                pw.println(rule + "\t\t" + (Double) rulesIG.get(rule) / (double) trainFileLength);
                previousLength = st.countTokens();
            }
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }
}
