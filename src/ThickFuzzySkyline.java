
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

public class ThickFuzzySkyline {

    public void createInputFile(String inputFile) {

        ArrayList attributeAL = null;
        HashMap valueMembershipMapTemp = null;
        StringTokenizer st = null;
        BufferedReader bufReaderConfig = null, bufReader = null;
        PrintWriter pw = null;
        String line;
        int index = 0, lineNumber;
        float value;

        try {
            bufReaderConfig = new BufferedReader(new FileReader("thickSkylineConfig.txt"));
            attributeAL = new ArrayList();
            while (true) {
                line = bufReaderConfig.readLine();
                if (line == null) {
                    break;
                }
                bufReader = new BufferedReader(new FileReader(line));
                valueMembershipMapTemp = new HashMap();
                while (true) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    st = new StringTokenizer(line);
                    valueMembershipMapTemp.put(Float.parseFloat(st.nextToken()), Float.parseFloat(st.nextToken()));
                }
                attributeAL.add(valueMembershipMapTemp);
                bufReader.close();
            }
            bufReaderConfig.close();

            bufReader = new BufferedReader(new FileReader(inputFile));
            pw = new PrintWriter(new FileWriter("thickSkylineInput.txt", false), true);

            for (lineNumber = 0; true; lineNumber++) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                pw.print(lineNumber + "\t");
                for (index = 0; st.hasMoreTokens(); index++) {
                    value = Float.parseFloat(st.nextToken());
                    valueMembershipMapTemp = (HashMap) attributeAL.get(index);
                    System.out.print(value + "," + valueMembershipMapTemp.get(value) + "\t");
                    pw.print(valueMembershipMapTemp.get(value) + "\t");
                }
                pw.println();
                System.out.println();
            }
            bufReader.close();
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public void generateSkyLine(String inputFile, float delta, int windowSize) {

        ArrayList window = null, tupleTemp, tupleCurrent, windowLineNumbers = null;
        StringTokenizer st = null;
        BufferedReader bufReader = null;
        PrintWriter pw = null;
        File file = null, fileOld = null;
        String line;
        int index = 0, iteration, numberOfTuplesLeft, index1, lineNumber;
        float valueTemp, valueCurrent;
        boolean isDominated, isDominatingOneLessOrEqualOver, isDominating, isDominatedOneLessOrEqualOver;

        try {
            window = new ArrayList();
            windowLineNumbers = new ArrayList();
            for (iteration = 0, numberOfTuplesLeft = 0; true; iteration++) {
                if (iteration != 0 && numberOfTuplesLeft == 0) {
                    break;
                }
                numberOfTuplesLeft = 0;
                if (iteration != 0) {
                    file = new File("tempFileNew.txt");
                    fileOld = new File("tempFileOld.txt");
                    if (fileOld.exists()) {
                        System.out.println("fuzzyMemberships fileOld.delete(): " + fileOld.delete());
                    }
                    if (file.exists()) {
                        System.out.println("fuzzyMemberships file.renameTo(fileOld)(): " + file.renameTo(fileOld));
                    }
                    
                    bufReader = new BufferedReader(new FileReader("tempFileOld.txt"));
                } else {
                    bufReader = new BufferedReader(new FileReader(inputFile));
                }

                pw = new PrintWriter(new FileWriter("tempFileNew.txt", false), true);

                while (true) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }

                    st = new StringTokenizer(line);
                    tupleCurrent = new ArrayList();
                    lineNumber = Integer.parseInt(st.nextToken());
                    while (st.hasMoreTokens()) {
                        tupleCurrent.add(Float.parseFloat(st.nextToken()));
                    }
                    
                    isDominated = false;
                    for (index = 0; index < window.size(); index++) {
                        tupleTemp = (ArrayList) window.get(index);
                        isDominating = false;
                        isDominatedOneLessOrEqualOver = false;
                        isDominatingOneLessOrEqualOver = false;
                        for (index1 = 0; index1 < tupleTemp.size(); index1++) {
                            valueCurrent = (Float) tupleCurrent.get(index1);
                            valueTemp = (Float) tupleTemp.get(index1);
                            //System.out.println("valueTemp:" + valueTemp);
                            //System.out.println("valueCurrent:" + valueCurrent);

                            if ((valueCurrent + delta) <= valueTemp) {
                                if (isDominatedOneLessOrEqualOver) {
                                    isDominated = true;
                                    break;
                                } else {
                                    isDominatedOneLessOrEqualOver = true;
                                }
                            }
                            
                            if ((valueTemp + delta) <= valueCurrent) {
                                if (isDominatingOneLessOrEqualOver) {
                                    isDominating = true;
                                    break;
                                } else {
                                    isDominatingOneLessOrEqualOver = true;
                                }
                            }
                            
                        }
                        if (isDominating) {
                            window.remove(index);
                            windowLineNumbers.remove(index);
                            index--;
                        } else if (isDominated) {
                            break;
                        }
                    }

                    if (!isDominated && window.size() >= windowSize) {
                        pw.print(line);
                        numberOfTuplesLeft++;
                    } else if (!isDominated) {
                        window.add(tupleCurrent);
                        windowLineNumbers.add(lineNumber);
                    }
                    //System.out.println(window);
                }
                bufReader.close();
                pw.close();
                System.out.println(window);
            }
            pw = new PrintWriter(new FileWriter("thickSkylineOutput.txt", false), true);
            for(index = 0; index < window.size(); index++) {
                tupleTemp = (ArrayList) window.get(index);
                pw.print(windowLineNumbers.get(index) + "\t");
                for (index1 = 0; index1 < tupleTemp.size(); index1++) {
                    pw.print(tupleTemp.get(index1) + "\t");
                }
                pw.println();
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

     public void createOutputFile(String inputFile, String thickSkylineOutputFile) {

        StringTokenizer st = null;
        BufferedReader bufReaderThickSkyline = null, bufReader = null;
        PrintWriter pw = null, pw1 = null;
        String line;
        int index = -1, lineNumber;

        try {
            bufReader = new BufferedReader(new FileReader(inputFile));
            bufReaderThickSkyline = new BufferedReader(new FileReader(thickSkylineOutputFile));
            pw = new PrintWriter(new FileWriter("thickSkylineOutputActualValues.txt", false), true);
            pw1 = new PrintWriter(new FileWriter("thickNonSkylineOutputActualValues.txt", false), true);

            while(true) {
                line = bufReaderThickSkyline.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                lineNumber  = Integer.parseInt(st.nextToken());
                while(true) {
                    line = bufReader.readLine();
                    index++;
                    if(index == lineNumber) {
                        pw.println(line);
                        break;
                    }
                    else {
                        pw1.println(line);
                    }
                }
            }
       
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                pw1.println(line);
            }
            bufReader.close();
            bufReaderThickSkyline.close();
            pw.close();
            pw1.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

     public void generateKMeansPartioningInMemory(String fileName, int k, int vectorSize) {
        BufferedReader bufReader = null;
        float jNew = 0, jOld, difference;
        int clusterNumber[] = null;
        int iteration, index, i, j, minClusterNumber = -9999, clusterCenterTotal[] = null, randomNumber, indexTab;
        String line = null;
        StringTokenizer st = null;
        float clusterCenters[][] = null, tempDoubleArray[] = null, tempDoubleArray1[] = null, centresFloatArray[][] = null;
        float cosineDistance, x, minCosineDistance, minValue, maxValue, value, euclideanDistance, euclideanDistanceTotal;
        PrintWriter pw = null;
        File file = null;
        ArrayList lineAL = new ArrayList(), randomNumberAL = new ArrayList(), membershipsAL = new ArrayList();
        long startMs, endMs;
        Random random = new Random();

        try {
            iteration = -1;
            bufReader = new BufferedReader(new FileReader(fileName));
            minValue = Float.POSITIVE_INFINITY;
            maxValue = Float.NEGATIVE_INFINITY;
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                lineAL.add(line);
                st = new StringTokenizer(line);
                value = Float.parseFloat(st.nextToken());
                if(value < minValue) {
                    minValue = value;
                }
                else if(value > maxValue) {
                    maxValue = value;
                }
            }
            pw = new PrintWriter(new FileWriter("crispClusters" + k + ".txt", false), true);

            ////for fuzzy partitioning
            pw.println(minValue);
            pw.println(maxValue);
            pw.close();
            bufReader.close();

            /*clusterNumber = new int[lineAL.size()];
            clusterCenters = new float[k][vectorSize];
            clusterCenterTotal = new int[k];

            while (true) {
                jOld = jNew;
                jNew = 0;
                iteration++;
                // Calculate J - START

                startMs = System.currentTimeMillis();
                if (iteration != 0) {

                    for (i = 0; i < lineAL.size(); i++) {
                        line = (String) lineAL.get(i);

                        st = new StringTokenizer(line);
                        tempDoubleArray = new float[vectorSize];

                        for (index = 0; st.hasMoreTokens(); index++) {
                            tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                        }

                        for (j = 0; j < k; j++) {
                            cosineDistance = EuclideanDistanceARM.distance(tempDoubleArray, clusterCenters[j]);
                            jNew += cosineDistance;
                        }
                    }
                }

                difference = (float) Math.abs(jNew - jOld);
                System.out.println("Iteration, jOld, jNew, difference: " + iteration + "..." + jOld + "..." + jNew + "..." + difference + "..." + Calendar.getInstance().getTime());
                //if ((iteration > 15 && difference <= 1 || (iteration <= 15 && difference <= 0.001)) && jNew != 0) {
                if (iteration != 0) {
                    index = lineAL.size();
                    file = new File("crispMemberships" + k + ".txt");
                    if (file.exists()) {
                        System.out.println("crispMemberships file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter("crispMemberships" + k + ".txt", false), true);
                    for (i = 0; i < index; i++) {
                        pw.println(i + "\t" + clusterNumber[i]);
                    }
                    pw.close();
                    file = new File("crispClusters" + k + ".txt");
                    if (file.exists()) {
                        System.out.println("crispClusters file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter("crispClusters" + k + ".txt", false), true);
                    for (j = 0; j < k; j++) {
                        for (index = 0; index < vectorSize - 1; index++) {
                            pw.print(clusterCenters[j][index] + "\t");
                        }
                        pw.println(clusterCenters[j][index]);
                    }

                    ////for fuzzy partitioning
                    pw.println(minValue);
                    pw.println(maxValue);
                    pw.close();
                    if ((iteration > 15 && difference <= 100 || (iteration <= 15 && difference <= 1)) && jNew != 0) {
                        System.out.println("FCM END");
                        break;
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time J: " + (endMs - startMs));

                // Calculate J - END

                if (iteration == 0) {
                    while (randomNumberAL.size() < k) {
                        randomNumber = random.nextInt(lineAL.size());
                        if (!randomNumberAL.contains(randomNumber)) {
                            randomNumberAL.add(randomNumber);
                        }
                    }

                    for (j = 0; j < k; j++) {
                        randomNumber = (Integer) randomNumberAL.get(j);
                        System.out.println("randomNumber: " + randomNumber);
                        line = (String) lineAL.get(randomNumber);
                        st = new StringTokenizer(line);

                        tempDoubleArray1 = new float[vectorSize];
                        for (index = 0; st.hasMoreTokens(); index++) {
                            tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                        }
                        clusterCenters[j] = tempDoubleArray1;
                    }
                }

                // Calculate u - START

                startMs = System.currentTimeMillis();
                for (i = 0; i < lineAL.size(); i++) {
                    line = (String) lineAL.get(i);
                    st = new StringTokenizer(line);

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; st.hasMoreTokens(); index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }

                    minCosineDistance = Float.POSITIVE_INFINITY;
                    tempDoubleArray = new float[k];
                    for (j = 0; j < k; j++) {
                        cosineDistance = EuclideanDistanceARM.distance(tempDoubleArray1, clusterCenters[j]);
                        if (cosineDistance < minCosineDistance) {
                            minCosineDistance = cosineDistance;
                            minClusterNumber = j;
                        }
                    }
                    clusterNumber[i] = minClusterNumber;
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time u: " + (endMs - startMs));

                // Calculate C - START
                startMs = System.currentTimeMillis();

                clusterCenters = new float[k][vectorSize];
                clusterCenterTotal = new int[k];

                for (i = 0; i < lineAL.size(); i++) {
                    line = (String) lineAL.get(i);

                    j = clusterNumber[i];
                    clusterCenterTotal[j]++;

                    st = new StringTokenizer(line);
                    for (index = 0; index < vectorSize; index++) {
                        x = Float.parseFloat(st.nextToken());
                        clusterCenters[j][index] += x;
                    }
                }

                for (j = 0; j < k; j++) {
                    if (clusterCenterTotal[j] != 0) {
                        for (index = 0; index < vectorSize; index++) {
                            clusterCenters[j][index] = clusterCenters[j][index] / clusterCenterTotal[j];
                        }
                    } else {
                        System.out.println("clusterCenterTotal[j] is " + clusterCenterTotal[j] + ": " + j);
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time C: " + (endMs - startMs));
                // Calculate C - END
            }*/

            bufReader = new BufferedReader(new FileReader("crispClusters" + k + ".txt"));
            centresFloatArray = new float[k + 2][vectorSize];
            for (index = 0; true; index++) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, "\t");
                for (indexTab = 0; indexTab < vectorSize; indexTab++) {
                    centresFloatArray[index][indexTab] = Float.parseFloat(st.nextToken());
                }
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("fuzzyMembershipsInterpolated_" + k + ".txt", false), true);
            while (true) {
                //System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                tempDoubleArray = new float[vectorSize];
                for (index = 0; index < vectorSize; index++) {
                    tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                }

                membershipsAL.clear();
                 euclideanDistanceTotal = 0;
                //euclideanDistanceTotal = Float.NEGATIVE_INFINITY;

                for (index = 0; index < (k + 2); index++) {
                    euclideanDistance = EuclideanDistanceARM.distance(tempDoubleArray, centresFloatArray[index]);
                    membershipsAL.add(euclideanDistance);
                    euclideanDistanceTotal += euclideanDistance;
                    //if(euclideanDistance > euclideanDistanceTotal) {
                    //    euclideanDistanceTotal = euclideanDistance;
                    //}
                }
                pw.print(line + "\t");
                for (index = 0; index < (k + 1); index++) {
                    pw.print((1 - ((Float) membershipsAL.get(index) / (float) euclideanDistanceTotal)) + "\t");
                }
                pw.println((1 - ((Float) membershipsAL.get(index) / (float) euclideanDistanceTotal)));
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThickFuzzySkyline tfs = new ThickFuzzySkyline();
         if (Integer.parseInt(args[0]) == 1) {
            tfs.generateKMeansPartioningInMemory(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        } else if (Integer.parseInt(args[0]) == 2) {
            tfs.createInputFile(args[1]);
        } else if (Integer.parseInt(args[0]) == 3) {
            tfs.generateSkyLine(args[1], Float.parseFloat(args[2]), Integer.parseInt(args[3]));
        } else if (Integer.parseInt(args[0]) == 4) {
            tfs.createOutputFile(args[1], args[2]);
        }
    }
}
