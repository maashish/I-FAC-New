
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

public class CalculateROC {

    class ValueComparator implements Comparator {

        Map base;

        public ValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if ((Double) base.get(a) < (Double) base.get(b)) {
                return 1;
            } else if ((Double) base.get(a) == (Double) base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public void calculateAUCMAP(double interval, String inputFileName, String groundtruthFileName, String outputFileName) {
        BufferedReader bufferedReader = null, bufferedReaderGroundtruth = null;
        HashMap positiveMap = new HashMap(), negativeMap = new HashMap();
        HashMap<Integer, Double> mapScore = new HashMap<Integer, Double>();
        HashMap<Integer, Integer> mapGroundtruth = new HashMap<Integer, Integer>();
        TreeMap<String, HashMap> classScoreMapMap = new TreeMap<String, HashMap>();
        HashMap<String, HashMap> classGroundtruthMapMap = new HashMap<String, HashMap>();
        ArrayList FPRAL = new ArrayList(), recallAL = new ArrayList();
        String line = null, token = null, lineGroundtruth = null, className = null;
        int testSetPositiveSize = 0, testSetNegativeSize = 0, remainder, numberOfItems, index, numberOfPositive = 0, numberOfNegative = 0, groundtruth;
        StringTokenizer st = null, st1 = null;
        double score, FPR, recall, base1, base2, altitude, area = 0, prevRecall = 0, prevFPR = 0, MAP, totalPrecision = 0;
        TreeSet treeSet = new TreeSet(Collections.reverseOrder());
        PrintWriter pw = null, pw1 = null, pw2 = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(inputFileName));
            bufferedReaderGroundtruth = new BufferedReader(new FileReader(groundtruthFileName));
            for (index = 0; true; index++) {
                line = bufferedReader.readLine();
                lineGroundtruth = bufferedReaderGroundtruth.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    st1 = new StringTokenizer(token, "=");
                    className = new String(st1.nextToken());
                    score = Double.parseDouble(st1.nextToken());
                    if (classScoreMapMap.containsKey(className)) {
                        mapScore = classScoreMapMap.get(className);
                    } else {
                        mapScore = new HashMap();
                        classScoreMapMap.put(className, mapScore);
                    }
                    mapScore.put(index, score);
                }
                st = new StringTokenizer(lineGroundtruth, ",");
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    st1 = new StringTokenizer(token, "=");
                    className = new String(st1.nextToken());
                    groundtruth = Integer.parseInt(st1.nextToken());
                    if (classGroundtruthMapMap.containsKey(className)) {
                        mapGroundtruth = classGroundtruthMapMap.get(className);
                    } else {
                        mapGroundtruth = new HashMap();
                        classGroundtruthMapMap.put(className, mapGroundtruth);
                    }
                    mapGroundtruth.put(index, groundtruth);
                }
            }
            bufferedReader.close();
            bufferedReaderGroundtruth.close();
            
            pw2 = new PrintWriter(new FileWriter(outputFileName));
            for (String keyClassName : classScoreMapMap.keySet()) {
                mapScore = classScoreMapMap.get(keyClassName);
                ValueComparator vc = new ValueComparator(mapScore);
                TreeMap<Integer, Double> sortedTreeMapScore = new TreeMap(vc);
                //sortedMapScore.clear();
                sortedTreeMapScore.putAll(mapScore);
                LinkedHashMap <Integer, Double> sortedMapScore = new LinkedHashMap<Integer, Double>();
                sortedMapScore.putAll(sortedTreeMapScore);
                mapGroundtruth = classGroundtruthMapMap.get(keyClassName);
                negativeMap.clear();
                positiveMap.clear();
                treeSet.clear();
                FPRAL.clear();
                recallAL.clear();
                testSetPositiveSize = 0;
                testSetNegativeSize = 0;
                totalPrecision = 0;
                area = 0;
                numberOfPositive = 0;
                numberOfNegative = 0;
                System.out.println("sortedMapScore: " + sortedMapScore);
                for (Integer indexLine : sortedMapScore.keySet()) {
                    System.out.println("indexLine, sortedMapScore.get(indexLine): " + indexLine + "," + sortedMapScore.get(indexLine));
                    score = sortedMapScore.get(indexLine);
                    groundtruth = mapGroundtruth.get(indexLine);
                    remainder = (int) Math.floor(score / interval);
                    System.out.println("score...remainder: " + score + "..." + remainder);

                    if (groundtruth == 0) {
                        if (negativeMap.containsKey(remainder)) {
                            numberOfItems = (Integer) negativeMap.get(remainder);
                            negativeMap.remove(remainder);
                            numberOfItems++;
                        } else {
                            numberOfItems = 1;
                        }
                        negativeMap.put(remainder, numberOfItems);
                        testSetNegativeSize++;
                    } else if (groundtruth == 1) {
                        if (positiveMap.containsKey(remainder)) {
                            numberOfItems = (Integer) positiveMap.get(remainder);
                            positiveMap.remove(remainder);
                            numberOfItems++;
                        } else {
                            numberOfItems = 1;
                        }
                        positiveMap.put(remainder, numberOfItems);
                        testSetPositiveSize++;
                        totalPrecision += (double) testSetPositiveSize / ((double) (testSetPositiveSize + testSetNegativeSize));
                    }
                }
                MAP = totalPrecision / (double) testSetPositiveSize;
                treeSet.addAll(negativeMap.keySet());
                treeSet.addAll(positiveMap.keySet());
                System.out.println("treeSet: " + treeSet);
                
                System.out.println("positiveMap: " + positiveMap);
                System.out.println("negativeMap: " + negativeMap);

                Iterator iterator = treeSet.iterator();

                pw = new PrintWriter(new FileWriter(keyClassName + "RPchart.txt"));
                pw1 = new PrintWriter(new FileWriter(keyClassName + "ROC.txt"));
                index = 1;
                pw.println("Reach\tPrecision");
                pw1.println("FPR\tRecall");
                while (iterator.hasNext()) {
                    remainder = (Integer) iterator.next();
                    if (positiveMap.containsKey(remainder)) {
                        numberOfPositive += (Integer) positiveMap.get(remainder);
                    }
                    if (negativeMap.containsKey(remainder)) {
                        numberOfNegative += (Integer) negativeMap.get(remainder);
                    }

                    recall = (double) numberOfPositive / (double) testSetPositiveSize;
                    FPR = (double) numberOfNegative / (double) testSetNegativeSize;

                    System.out.println(remainder + "\t" + interval + "\t" + (remainder * interval) + "\t" + FPR + "\t" + recall + "\t" + numberOfPositive + "\t" + (numberOfPositive + numberOfNegative) + "\t" + ((double) numberOfPositive / ((double) numberOfPositive + (double) numberOfNegative)));
                    //pw.println(((double) index / (double) treeSet.size()) + "\t" + ((double) numberOfPositive / ((double) numberOfPositive + (double) numberOfNegative)));
                    pw.println((((double) numberOfPositive + (double) numberOfNegative) / ((double) testSetPositiveSize + (double) testSetNegativeSize)) + "\t" + ((double) numberOfPositive / ((double) numberOfPositive + (double) numberOfNegative)));
                    pw1.println(FPR + "\t" + recall);
                    FPRAL.add(FPR);
                    recallAL.add(recall);
                    index++;
                }
                pw.close();
                pw1.close();

                if (((Double) FPRAL.get(0)) == 0) {
                    prevRecall = (Double) recallAL.get(0);
                    prevFPR = 0;
                } else {
                    prevRecall = 0;
                    prevFPR = 0;

                    base1 = prevRecall;
                    base2 = (Double) recallAL.get(0);
                    altitude = (Double) FPRAL.get(0) - prevFPR;

                    area += ((base1 + base2) * altitude) / 2;
                    //System.out.println("base1, base2, alt, area: " + base1 + "..." + base2 + "..." + altitude + "..." + area);

                    prevRecall = base2;
                    prevFPR = (Double) FPRAL.get(0);
                }

                for (index = 1; index < recallAL.size(); index++) {
                    base1 = prevRecall;
                    base2 = (Double) recallAL.get(index);
                    altitude = (Double) FPRAL.get(index) - prevFPR;
                    area += ((base1 + base2) * altitude) / 2;
                    //System.out.println("base1, base2, alt, area: " + base1 + "..." + base2 + "..." + altitude + "..." + area);
                    prevRecall = base2;
                    prevFPR = (Double) FPRAL.get(index);
                }
                if (((Double) FPRAL.get(index - 1)) != 1) {
                    base1 = prevRecall;
                    base2 = prevRecall;
                    altitude = 1 - prevFPR;
                    area += ((base1 + base2) * altitude) / 2;
                    //System.out.println("base1, base2, alt, area: " + base1 + "..." + base2 + "..." + altitude + "..." + area);
                }
                pw2.println("Class: " + keyClassName);
                pw2.println("Area under ROC: " + area);
                pw2.println("Mean Average Precision: " + MAP);
                System.out.println("Class: " + keyClassName);
                System.out.println("Area under ROC: " + area);
                System.out.println("Mean Average Precision: " + MAP);
            }
            pw2.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void calculateROC(double interval, String rpchartFileName, String rocFileName) {
        BufferedReader bufferedReader = null;
        HashMap positiveMap = new HashMap(), negativeMap = new HashMap();
        ArrayList tempAL = null, FPRAL = new ArrayList(), recallAL = new ArrayList();
        String line = null, token = null;
        int testSetPositiveSize = 0, testSetNegativeSize = 0, remainder, numberOfItems, index, numberOfPositive = 0, numberOfNegative = 0;
        StringTokenizer st = null, st1 = null;
        double value, FPR, recall, base1, base2, altitude, area = 0, prevRecall = 0, prevFPR = 0, MAP, totalPrecision = 0;
        TreeSet treeSet = new TreeSet(Collections.reverseOrder());
        PrintWriter pw = null, pw1 = null;

        try {

            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }

                st = new StringTokenizer(line);
                value = Float.parseFloat(st.nextToken());
                token = st.nextToken();
                remainder = (int) Math.floor(value / interval);

                if (token.equals("-1")) {
                    if (negativeMap.containsKey(remainder)) {
                        numberOfItems = (Integer) negativeMap.get(remainder);
                        negativeMap.remove(remainder);
                        numberOfItems++;
                    } else {
                        numberOfItems = 1;
                    }
                    negativeMap.put(remainder, numberOfItems);
                    testSetNegativeSize++;
                } else if (token.equals("1")) {
                    if (positiveMap.containsKey(remainder)) {
                        numberOfItems = (Integer) positiveMap.get(remainder);
                        positiveMap.remove(remainder);
                        numberOfItems++;
                    } else {
                        numberOfItems = 1;
                    }
                    positiveMap.put(remainder, numberOfItems);
                    testSetPositiveSize++;
                    totalPrecision += (double) testSetPositiveSize / ((double) (testSetPositiveSize + testSetNegativeSize));
                }
            }
            MAP = totalPrecision / (double) testSetPositiveSize;
            bufferedReader.close();

            //tempAL = new ArrayList(negativeMap.keySet());
            //Collections.sort(tempAL, Collections.reverseOrder());

            treeSet.addAll(negativeMap.keySet());
            treeSet.addAll(positiveMap.keySet());


            //System.out.println("positiveMap: " + positiveMap);

            //System.out.println("\n\negativeMap: " + negativeMap);
            //System.out.println("\n\treeSet: " + treeSet);

            //System.out.println("testSetPositiveSize, testSetNegativeSize: " + testSetPositiveSize + "..." + testSetNegativeSize);

            Iterator iterator = treeSet.iterator();

            //for(index = 0; index < tempAL.size(); index++) {

            pw = new PrintWriter(new FileWriter(rpchartFileName));
            pw1 = new PrintWriter(new FileWriter(rocFileName));
            index = 1;
            pw.println("Reach\tPrecision");
            pw1.println("FPR\tRecall");
            while (iterator.hasNext()) {
                remainder = (Integer) iterator.next();
                if (positiveMap.containsKey(remainder)) {
                    numberOfPositive += (Integer) positiveMap.get(remainder);
                }
                if (negativeMap.containsKey(remainder)) {
                    numberOfNegative += (Integer) negativeMap.get(remainder);
                }

                recall = (double) numberOfPositive / (double) testSetPositiveSize;
                FPR = (double) numberOfNegative / (double) testSetNegativeSize;

                System.out.println(remainder + "\t" + interval + "\t" + (remainder * interval) + "\t" + FPR + "\t" + recall + "\t" + numberOfPositive + "\t" + (numberOfPositive + numberOfNegative) + "\t" + ((double) numberOfPositive / ((double) numberOfPositive + (double) numberOfNegative)));
                //pw.println(((double) index / (double) treeSet.size()) + "\t" + ((double) numberOfPositive / ((double) numberOfPositive + (double) numberOfNegative)));
                pw.println((((double) numberOfPositive + (double) numberOfNegative) / ((double) testSetPositiveSize + (double) testSetNegativeSize)) + "\t" + ((double) numberOfPositive / ((double) numberOfPositive + (double) numberOfNegative)));
                pw1.println(FPR + "\t" + recall);
                FPRAL.add(FPR);
                recallAL.add(recall);
                index++;
            }

            pw.close();
            pw1.close();

            if (((Double) FPRAL.get(0)) == 0) {
                prevRecall = (Double) recallAL.get(0);
                prevFPR = 0;
            } else {
                prevRecall = 0;
                prevFPR = 0;

                base1 = prevRecall;
                base2 = (Double) recallAL.get(0);
                altitude = (Double) FPRAL.get(0) - prevFPR;

                area += ((base1 + base2) * altitude) / 2;
                //System.out.println("base1, base2, alt, area: " + base1 + "..." + base2 + "..." + altitude + "..." + area);

                prevRecall = base2;
                prevFPR = (Double) FPRAL.get(0);
            }

            for (index = 1; index < recallAL.size(); index++) {
                base1 = prevRecall;
                base2 = (Double) recallAL.get(index);
                altitude = (Double) FPRAL.get(index) - prevFPR;
                area += ((base1 + base2) * altitude) / 2;
                //System.out.println("base1, base2, alt, area: " + base1 + "..." + base2 + "..." + altitude + "..." + area);

                prevRecall = base2;
                prevFPR = (Double) FPRAL.get(index);
            }

            if (((Double) FPRAL.get(index - 1)) != 1) {
                base1 = prevRecall;
                base2 = prevRecall;
                altitude = 1 - prevFPR;
                area += ((base1 + base2) * altitude) / 2;
                //System.out.println("base1, base2, alt, area: " + base1 + "..." + base2 + "..." + altitude + "..." + area);
            }
            System.out.println("Area under ROC: " + area);
            System.out.println("Mean Average Precision: " + MAP);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CalculateROC cROC = new CalculateROC();
        //cROC.calculateROC(Double.parseDouble(args[0]), args[1], args[2]);
        cROC.calculateAUCMAP(Double.parseDouble(args[0]), args[1], args[2], args[3]);
    }
}
