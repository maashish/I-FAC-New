
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class AccuracyCalculateFromFile {

    public static void main(String[] args) {
        //new AccuracyCalculateFromFile().accuracyCalculateFromFile();
        new AccuracyCalculateFromFile().calculateAUCMAP(args[0]);
    }

    private void calculateAUCMAP(String fileName) {
        BufferedReader bufReader = null;
        TreeMap<String, Double> classAUCMap = new TreeMap();
        HashMap<String, Double> classMAPMap = new HashMap();
        HashMap<String, Integer> classIndexMap = new HashMap();
        String line, className;
        StringTokenizer st = null;
        double AUC, MAP;
        int index = 0;
        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ":");
                st.nextToken();
                className = st.nextToken().trim();

                line = bufReader.readLine();
                st = new StringTokenizer(line, ":");
                st.nextToken();
                AUC = Double.parseDouble(st.nextToken().trim());

                line = bufReader.readLine();
                st = new StringTokenizer(line, ":");
                st.nextToken();
                MAP = Double.parseDouble(st.nextToken().trim());

                if (classAUCMap.containsKey(className)) {
                    AUC += classAUCMap.get(className);
                    MAP += classMAPMap.get(className);
                    index = classIndexMap.get(className);
                    classAUCMap.remove(className);
                    classMAPMap.remove(className);
                    classIndexMap.remove(className);
                } else {
                    index = 0;
                }
                index++;
                classAUCMap.put(className, AUC);
                classMAPMap.put(className, MAP);
                classIndexMap.put(className, index);
            }
            bufReader.close();
            PrintWriter pw = new PrintWriter(new FileWriter(fileName + ".ConsolidatedResults.txt", false), true);
            pw.println("Class\tAUC\tMAP");
            for (String keyClassName : classAUCMap.keySet()) {
                System.out.println((keyClassName + "," + classAUCMap.get(keyClassName) + "," + (double) classIndexMap.get(keyClassName)) + "," + (classMAPMap.get(keyClassName)));
                pw.println((keyClassName + "\t" + classAUCMap.get(keyClassName) / (double) classIndexMap.get(keyClassName)) + "\t" + (classMAPMap.get(keyClassName) / (double) classIndexMap.get(keyClassName)));
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    private void accuracyCalculateFromFile() {
        BufferedReader bufReader = null;
        String valueString;
        float accuracyTotal = 0;
        int index;
        try {
            bufReader = new BufferedReader(new FileReader("accuracyNumberOverall.txt"));
            for (index = 0; true; index++) {
                valueString = bufReader.readLine();
                if (valueString == null) {
                    break;
                }
                accuracyTotal += Float.parseFloat(valueString);
            }
            bufReader.close();
            PrintWriter pw = new PrintWriter(new FileWriter("accuracyNumberOverall.txt", true), true);
            pw.println("OVERALL Accuracy: " + (accuracyTotal / (float) index));
            pw.close();
            System.out.println("OVERALL Accuracy: " + (accuracyTotal / (float) index));
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
}
