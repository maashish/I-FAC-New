import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class Sampler {

    public void sampleWithClasses(String inputFileName, String outputFileName, int reductionFactor) {
        BufferedReader bufferedReaderIn = null;
        String line = null, claSS;
        int lineNumber = 0, classCount, classIndex;
        PrintWriter printWriterOutPut = null;
        HashMap<String, HashSet<Integer>> classRandomNumbersSetMap = new HashMap();
        HashMap<String, Integer> classIndexMap = new HashMap();
        HashSet<Integer> randomNumbersSet = null;
        HashMap<String, Integer> classCountMap = new HashMap<String, Integer>();

        try {
            bufferedReaderIn = new BufferedReader(new FileReader(inputFileName));
            while (true) {
                line = bufferedReaderIn.readLine();
                if (line == null) {
                    break;
                }

                if (++lineNumber % 1000 == 0) {
                    System.out.println("lineNumber: " + lineNumber + Calendar.getInstance().getTime());
                }
                String lineArray[] = line.split("\t");
                //String classLine = lineArray[lineArray.length - 1];
                //claSS = classLine.split("^")[0];
                claSS = lineArray[3];

                if (classCountMap.containsKey(claSS)) {
                    classCount = classCountMap.get(claSS);
                    classCountMap.remove(claSS);
                    classCount++;
                } else {
                    classCount = 1;
                }
                classCountMap.put(claSS, classCount);
            }
            bufferedReaderIn.close();
            System.out.println("classCountMap: " + classCountMap);

            Iterator<String> it = classCountMap.keySet().iterator();
            while (it.hasNext()) {
                claSS = it.next();
                classCount = classCountMap.get(claSS);

                long numberOfRandomNumbers = Math.round((double) classCount / (double) reductionFactor);
                Random random = new Random();
                randomNumbersSet = new HashSet();
                while (randomNumbersSet.size() < numberOfRandomNumbers) {
                    randomNumbersSet.add(random.nextInt(classCount));
                }
                classRandomNumbersSetMap.put(claSS, randomNumbersSet);
                System.out.println("randomNumbersSet.size(): " + randomNumbersSet.size() + " for class: " + claSS);
            }

            bufferedReaderIn = new BufferedReader(new FileReader(inputFileName));
            printWriterOutPut = new PrintWriter(new FileWriter(outputFileName));
            lineNumber = 0;
            while (true) {
                line = bufferedReaderIn.readLine();
                if (line == null) {
                    break;
                }
                if (++lineNumber % 1000 == 0) {
                    System.out.println("lineNumberAgain: " + lineNumber + Calendar.getInstance().getTime());
                }
                String lineArray[] = line.split("\t");
                //String classLine = lineArray[lineArray.length - 1];
                //claSS = classLine.split("^")[0];
                claSS = lineArray[3];

                if (classIndexMap.containsKey(claSS)) {
                    classIndex = classIndexMap.get(claSS);
                    classIndexMap.remove(claSS);
                } else {
                    classIndex = 0;
                }
                randomNumbersSet = classRandomNumbersSetMap.get(claSS);
                if (randomNumbersSet.contains(classIndex)) {
                    printWriterOutPut.println(line);
                }
                classIndex++;
                classIndexMap.put(claSS, classIndex);
            }
            bufferedReaderIn.close();
            printWriterOutPut.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Sampler().sampleWithClasses(args[0], args[1], Integer.parseInt(args[2]));
    }
}
