
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

public class SURF {

    /*class ValueComparator implements Comparator {
    Map base;
    public ValueComparator(Map base) {
    this.base = base;
    }

    public int compare(Object a, Object b) {
    if ((Integer) base.get(a) < (Integer) base.get(b)) {
    return 1;
    } else if ((Integer) base.get(a) == (Integer) base.get(b)) {
    return 0;
    } else {
    return -1;
    }
    }
    }*/
    public void mapSampledToOriginal(String originalFileName, String sampleFileName) {
        BufferedReader bufReader = null;
        String line;
        int lineNumber, index, lineNumber1 = 0;
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        PrintWriter pw;
        try {
            bufReader = new BufferedReader(new FileReader(sampleFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                index = line.indexOf('\t');
                lineNumber = Integer.parseInt(line.substring(0, index));
                line = line.substring(index + 1);
                map.put(line, lineNumber);
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(originalFileName));
            pw = new PrintWriter(new FileWriter("sampledToOriginalMapping.txt", false), true);
            lineNumber1 = 0;
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber2: " + lineNumber1);
                }
                index = line.indexOf('\t');
                lineNumber = Integer.parseInt(line.substring(0, index));
                line = line.substring(index + 1);
                if (map.containsKey(line)) {
                    pw.println(lineNumber + "\t" + map.get(line));
                } else {
                    //pw.println(lineNumber + "\t-1");
                }
            }
            bufReader.close();
            pw.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void deDupe(String FCMFileName) {
        BufferedReader bufReader = null;
        String line;
        int lineNumber, index, lineNumber1 = 0;
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        //ValueComparator vc = new ValueComparator(map);
        //TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(vc);
        PrintWriter pw;

        try {
            bufReader = new BufferedReader(new FileReader(FCMFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                index = line.indexOf('\t');
                lineNumber = Integer.parseInt(line.substring(0, index));
                line = line.substring(index + 1);
                if (!map.containsKey(line)) {
                    map.put(line, lineNumber);
                }

                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
            }
            bufReader.close();
            //sortedMap.putAll(map);
            pw = new PrintWriter(new FileWriter("FCMTrainingUnique.txt", false), true);
            for (String key : map.keySet()) {
                pw.println(map.get(key) + "\t" + key);
            }
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void MIRConvertToFCMFormat(int numberOfImages, String annotationsFileName, String splitFileName) {
        BufferedReader bufReaderannotationsFile = null, bufReadersplitFile = null, bufReader = null;
        PrintWriter pw = null, pwGroundTruth = null;
        String line = null, decisionClass;
        StringTokenizer st = null, st1 = null;
        HashMap imageAnnotationMap = new HashMap(), imageSplitMap = new HashMap(), trainingWriterMap = new HashMap(), testingWriterMap = new HashMap(), trainingIndexMap = new HashMap(), testingIndexMap = new HashMap(), trainingGroundTruthWriterMap = new HashMap(), testingGroundTruthWriterMap = new HashMap();
        ArrayList tempAL, lineAL;
        int index, splitNumber, indexSplit, index1;
        File file = null;
        boolean isTest;

        try {
            bufReaderannotationsFile = new BufferedReader(new FileReader(annotationsFileName));
            while (true) {
                line = bufReaderannotationsFile.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                imageAnnotationMap.put(Integer.parseInt(st.nextToken()), st.nextToken());
            }
            bufReaderannotationsFile.close();
            System.out.println("imageAnnotationMap: " + imageAnnotationMap);
            bufReadersplitFile = new BufferedReader(new FileReader(splitFileName));
            while (true) {
                line = bufReadersplitFile.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                imageSplitMap.put(Integer.parseInt(st.nextToken()), st.nextToken());
            }
            bufReadersplitFile.close();

            line = (String) imageSplitMap.get(1);

            st = new StringTokenizer(line, ",");
            while (st.hasMoreTokens()) {
                splitNumber = Integer.parseInt(st.nextToken());
                file = new File("./" + splitNumber);
                file.mkdir();
                trainingWriterMap.put(splitNumber, new PrintWriter(new FileWriter("./" + splitNumber + "/FCMTraining.txt", false), true));
                testingWriterMap.put(splitNumber, new PrintWriter(new FileWriter("./" + splitNumber + "/FCMTesting.txt", false), true));
                trainingGroundTruthWriterMap.put(splitNumber, new PrintWriter(new FileWriter("./" + splitNumber + "/groundtruthTraining.txt", false), true));
                testingGroundTruthWriterMap.put(splitNumber, new PrintWriter(new FileWriter("./" + splitNumber + "/groundtruthTesting.txt", false), true));
                trainingIndexMap.put(splitNumber, 0);
                testingIndexMap.put(splitNumber, 0);
            }

            for (index = 1; index <= numberOfImages; index++) {
                if (!imageAnnotationMap.containsKey(index)) {
                    continue;
                }
                bufReader = new BufferedReader(new FileReader("im" + index + ".jpg.txt.fcm.txt"));
                lineAL = new ArrayList();
                while (true) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    lineAL.add(line);
                }
                System.out.println(index);
                st1 = new StringTokenizer((String) imageSplitMap.get(index), ",");
                while (st1.hasMoreTokens()) {
                    if (st1.countTokens() > 1) {
                        isTest = true;
                    } else {
                        isTest = false;
                    }
                    splitNumber = Integer.parseInt(st1.nextToken());
                    if (isTest) {
                        for (index1 = 0; index1 < lineAL.size(); index1++) {
                            line = (String) lineAL.get(index1);
                            pw = (PrintWriter) testingWriterMap.get(splitNumber);
                            pwGroundTruth = (PrintWriter) testingGroundTruthWriterMap.get(splitNumber);
                            indexSplit = (Integer) testingIndexMap.get(splitNumber);
                            pw.println(indexSplit + "\t" + line);
                            pwGroundTruth.println(imageAnnotationMap.get(index));
                            indexSplit++;
                            testingIndexMap.remove(splitNumber);
                            testingIndexMap.put(splitNumber, indexSplit);
                        }
                        pwGroundTruth = (PrintWriter) testingGroundTruthWriterMap.get(splitNumber);
                        pwGroundTruth.println("END OF DATA POINT");

                    } else {
                        st = new StringTokenizer((String) imageAnnotationMap.get(index), ",");
                        while (st.hasMoreTokens()) {
                            decisionClass = st.nextToken();
                            for (index1 = 0; index1 < lineAL.size(); index1++) {
                                line = (String) lineAL.get(index1);
                                pw = (PrintWriter) trainingWriterMap.get(splitNumber);
                                pwGroundTruth = (PrintWriter) trainingGroundTruthWriterMap.get(splitNumber);
                                indexSplit = (Integer) trainingIndexMap.get(splitNumber);
                                pw.println(indexSplit + "\t" + line);
                                pwGroundTruth.println(decisionClass);
                                indexSplit++;
                                trainingIndexMap.remove(splitNumber);
                                trainingIndexMap.put(splitNumber, indexSplit);
                            }
                        }
                    }
                }
            }

            tempAL = new ArrayList(trainingWriterMap.keySet());
            for (index = 0; index < tempAL.size(); index++) {
                splitNumber = (Integer) tempAL.get(index);
                pw = (PrintWriter) trainingWriterMap.get(splitNumber);
                pw.close();
                pw = (PrintWriter) testingWriterMap.get(splitNumber);
                pw.close();
                pw = (PrintWriter) trainingGroundTruthWriterMap.get(splitNumber);
                pw.close();
                pw = (PrintWriter) testingGroundTruthWriterMap.get(splitNumber);
                pw.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void MIRConvertToFCMFormatCopy(int numberOfImages, String annotationsFileName, String splitFileName) {
        BufferedReader bufReaderannotationsFile = null, bufReadersplitFile = null, bufReader = null;
        PrintWriter pw = null, pwGroundTruth = null;
        String line = null, decisionClass;
        StringTokenizer st = null, st1 = null;
        HashMap imageAnnotationMap = new HashMap(), imageSplitMap = new HashMap(), trainingGroundTruthWriterMap = new HashMap();
        ArrayList tempAL, lineAL;
        int index, splitNumber, indexSplit, index1;
        File file = null;
        boolean isTest;

        try {
            bufReaderannotationsFile = new BufferedReader(new FileReader(annotationsFileName));
            while (true) {
                line = bufReaderannotationsFile.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                imageAnnotationMap.put(Integer.parseInt(st.nextToken()), st.nextToken());
            }
            bufReaderannotationsFile.close();
            System.out.println("imageAnnotationMap: " + imageAnnotationMap);
            bufReadersplitFile = new BufferedReader(new FileReader(splitFileName));
            while (true) {
                line = bufReadersplitFile.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                imageSplitMap.put(Integer.parseInt(st.nextToken()), st.nextToken());
            }
            bufReadersplitFile.close();

            line = (String) imageSplitMap.get(1);

            st = new StringTokenizer(line, ",");
            while (st.hasMoreTokens()) {
                splitNumber = Integer.parseInt(st.nextToken());
                file = new File("./" + splitNumber);
                file.mkdir();
                trainingGroundTruthWriterMap.put(splitNumber, new PrintWriter(new FileWriter("./" + splitNumber + "/groundtruthTrainingWithEODP.txt", false), true));
            }

            for (index = 1; index <= numberOfImages; index++) {
                if (!imageAnnotationMap.containsKey(index)) {
                    continue;
                }
                bufReader = new BufferedReader(new FileReader("im" + index + ".jpg.txt.fcm.txt"));
                lineAL = new ArrayList();
                while (true) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    lineAL.add(line);
                }
                System.out.println(index);
                st1 = new StringTokenizer((String) imageSplitMap.get(index), ",");
                while (st1.hasMoreTokens()) {
                    if (st1.countTokens() > 1) {
                        isTest = true;
                    } else {
                        isTest = false;
                    }
                    splitNumber = Integer.parseInt(st1.nextToken());
                    if (isTest) {

                    } else {
                        st = new StringTokenizer((String) imageAnnotationMap.get(index), ",");
                        while (st.hasMoreTokens()) {
                            decisionClass = st.nextToken();
                            for (index1 = 0; index1 < lineAL.size(); index1++) {
                                //line = (String) lineAL.get(index1);
                                pwGroundTruth = (PrintWriter) trainingGroundTruthWriterMap.get(splitNumber);
                                pwGroundTruth.println(decisionClass);
                            }
                            pwGroundTruth = (PrintWriter) trainingGroundTruthWriterMap.get(splitNumber);
                            pwGroundTruth.println("END OF DATA POINT");
                        }
                    }
                }
            }

            tempAL = new ArrayList(trainingGroundTruthWriterMap.keySet());
            for (index = 0; index < tempAL.size(); index++) {
                splitNumber = (Integer) tempAL.get(index);
                pw = (PrintWriter) trainingGroundTruthWriterMap.get(splitNumber);
                pw.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void convertToFCMFormat(String fileName) {
        BufferedReader bufReader = null;
        PrintWriter pw1 = null;
        String line = null, imageName;
        int index, numberOfSIFTPoints, totalNumberOfSIFTPoints = 0, indexSpace, index1;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));

            pw1 = new PrintWriter(new FileWriter("vectorIndexMap.txt", false), true);

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                imageName = line;
                bufReader.readLine();
                numberOfSIFTPoints = Integer.parseInt(bufReader.readLine());
                for (index = 1; index <= numberOfSIFTPoints; index++) {
                    line = bufReader.readLine();

                    totalNumberOfSIFTPoints++;
                    for (index1 = 0; index1 < 6; index1++) {
                        indexSpace = line.indexOf(' ');
                        line = line.substring(indexSpace + 1);
                    }
                    line = line.replace(' ', '\t');
                    pw1.println(totalNumberOfSIFTPoints + "\t" + imageName + "\t" + line);
                }
            }
            bufReader.close();
            pw1.close();

            /*pw = new PrintWriter(new FileWriter("FCMInput.txt", false), true);
            bufReader = new BufferedReader(new FileReader("vectorIndexMap.txt"));

            pw.print(totalNumberOfSIFTPoints + "\t64\t");
            for (index = 1; index < 64; index++) {
            pw.print(index + "\t");
            }
            pw.print(index);
            pw.println();
            index = 0;

            while (true) {
            line = bufReader.readLine();
            if (line == null) {
            break;
            }
            index++;
            pw.println("x" + index + "\tx" + index + "\t" + line);
            }
            bufReader.close();
            pw.close(); */
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void convertToFCMFormatYLR(String fileName) {
        BufferedReader bufReader = null;
        PrintWriter pw1 = null;
        String line = null;
        int index, indexComma;
        StringTokenizer st = null, st1 = null;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));

            pw1 = new PrintWriter(new FileWriter("vectorIndexMap.txt", false), true);

            for (index = 638794; true; index++) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexComma = line.indexOf(',');
                line = line.substring(indexComma + 1);
                line = line.replace(',', '\t');
                pw1.print(index + "\t" + index + "\t" + line);
                pw1.println();

                /*st = new StringTokenizer(line);
                st.nextToken();
                st.nextToken();

                pw1.print(index + "\t" + index + "\t");
                
                while(st.hasMoreTokens()) {
                st1 = new StringTokenizer(st.nextToken(), ":");
                st1.nextToken();
                if(st.countTokens() > 0) {
                pw1.print(st1.nextToken() + "\t");
                }
                else {
                pw1.print(st1.nextToken());
                pw1.println();
                }
                }*/
            }
            bufReader.close();
            pw1.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /* public void createFuzzyDataset(String vectorIndexMapFileName, String vectorIndexMapReducedFileName, String membershipsFileName, String groundtruthFileName, int vectorSize, int k, int knn) {

    BufferedReader bufReader = null, bufReader1 = null;
    String line, lineNumber, imageName, membershipsLine;
    StringTokenizer st = null;
    HashMap groundtruthMap = new HashMap();
    HashMap membershipsMap = new HashMap();
    int indexTab,indexDot, index, index1;
    PrintWriter pw = null;
    float tempFloatArray[] = null, lineFloatArray[] = null;
    double tempDoubleArray[] = null;
    double minMaxCosineDistance = 0, cosineDistance, cosineSimilarityTotal;
    DenseVector dv, dv1;
    ArrayList vectorsAL = new ArrayList(), membershipsAL = new ArrayList();
    CosineSimilarity metric = new CosineSimilarity();

    try{
    bufReader = new BufferedReader(new FileReader(groundtruthFileName));
    while(true) {
    line = bufReader.readLine();
    if(line == null) {
    break;
    }

    st = new StringTokenizer(line, ",");
    groundtruthMap.put(st.nextToken(), st.nextToken());
    }
    bufReader.close();

    bufReader = new BufferedReader(new FileReader(membershipsFileName));
    while (true) {
    line = bufReader.readLine();
    if (line == null) {
    break;
    }
    indexTab = line.indexOf('\t');
    lineNumber = line.substring(0, indexTab);
    line = line.substring(indexTab + 1);
    membershipsMap.put(lineNumber, line);

    }

    bufReader.close();

    pw = new PrintWriter(new FileWriter("training_" + k + "_" + knn + ".txt", false), true);
    bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));

    while (true) {
    line = bufReader.readLine();
    if (line == null) {
    break;
    }
    indexTab = line.indexOf('\t');
    lineNumber = line.substring(0, indexTab);
    line = line.substring(indexTab + 1);
    indexTab = line.indexOf('\t');
    imageName = line.substring(0, indexTab);
    line = line.substring(indexTab + 1);
    indexDot = imageName.indexOf('.');
    imageName = imageName.substring(0, indexDot);

    if(!groundtruthMap.containsKey(imageName)) {
    System.out.println("Image not found: " + imageName);
    continue;
    }

    if(membershipsMap.containsKey(lineNumber)) {
    membershipsLine = (String) membershipsMap.get(lineNumber);
    st = new StringTokenizer(membershipsLine);
    System.out.println("Short cut: " + lineNumber);

    for(index = 0; st.countTokens() > 1; index++) {
    pw.print((index + 1) + "^" + st.nextToken() + ",");
    }
    pw.println((index + 1) + "^" + st.nextToken() + ",CLASS=" + groundtruthMap.get(imageName)+ "^1");
    continue;
    }

    st = new StringTokenizer(line);
    tempDoubleArray = new double[vectorSize];

    for (index = 0; st.hasMoreTokens(); index++) {
    tempDoubleArray[index] = Double.parseDouble(st.nextToken());
    }
    dv = new DenseVector(tempDoubleArray);

    vectorsAL.clear();
    membershipsAL.clear();
    bufReader1 = new BufferedReader(new FileReader(vectorIndexMapReducedFileName));

    while(true) {
    line  = bufReader1.readLine();
    if(line == null) {
    break;
    }
    indexTab = line.indexOf('\t');
    lineNumber = line.substring(0, indexTab);
    line = line.substring(indexTab + 1);
    indexTab = line.indexOf('\t');
    line = line.substring(indexTab + 1);
    st = new StringTokenizer(line);

    tempDoubleArray = new double[vectorSize];
    for (index = 0; st.hasMoreTokens(); index++) {
    tempDoubleArray[index] = Double.parseDouble(st.nextToken());
    }
    dv1 = new DenseVector(tempDoubleArray);
    cosineDistance = metric.distance(dv, dv1);
    tempDoubleArray = null;

    if (!membershipsAL.isEmpty()) {
    minMaxCosineDistance = (Double) Collections.max(membershipsAL);
    }

    tempFloatArray = new float[k];

    if (membershipsAL.size() < knn || minMaxCosineDistance > cosineDistance) {
    membershipsLine = (String) membershipsMap.get(lineNumber);
    st = new StringTokenizer(membershipsLine);
    for (index = 0; st.hasMoreTokens(); index++) {
    tempFloatArray[index] = Float.parseFloat(st.nextToken());
    }
    vectorsAL.add(tempFloatArray);
    membershipsAL.add(cosineDistance);
    tempFloatArray = null;
    }

    if (membershipsAL.size() >= knn) {
    while ((index = membershipsAL.indexOf(minMaxCosineDistance)) != -1) {
    membershipsAL.remove(index);
    vectorsAL.remove(index);
    }
    }
    }

    bufReader1.close();

    lineFloatArray = new float[k];
    cosineSimilarityTotal = 0;
    for(index1 = 0; index1 < membershipsAL.size(); index1++) {
    tempFloatArray = (float[]) vectorsAL.get(index1);
    cosineDistance = (Double) membershipsAL.get(index1);
    cosineSimilarityTotal += (1-cosineDistance);
    for(index = 0; index < k; index++) {
    lineFloatArray[index] += (1-cosineDistance) * tempFloatArray[index];
    }
    }
    tempFloatArray = null;

    //System.out.println(Arrays.toString(lineDoubleArray));

    for (index = 0; index < (k - 1); index++) {
    pw.print((index + 1) + "^" + (lineFloatArray[index] / (float) cosineSimilarityTotal) + ",");
    }
    pw.println((index + 1) + "^" + (lineFloatArray[index] / (float) cosineSimilarityTotal) + ",CLASS=" + groundtruthMap.get(imageName)+ "^1");
    lineFloatArray = null;
    }
    bufReader.close();

    } catch (IOException ioe) {
    ioe.printStackTrace();
    }
    }*/
    public void createFuzzyDatasetTrainingPartitionsFloat(String vectorIndexMapFileName, String vectorIndexMapReducedFileName, String membershipsFileName, String groundtruthFileName, int vectorSize, int k, int knn, int numberOfPartitions, boolean isTesting) {

        BufferedReader bufReader = null, bufReader1 = null;
        String line, lineNumber, lineNumber1, imageName, membershipsLine, imageNamePrev = "";
        StringTokenizer st = null;
        HashMap groundtruthMap = new HashMap();
        HashMap membershipsMap = new HashMap();
        HashMap partitionVectorsMap = new HashMap(), partitionsMembershipsMap = new HashMap(), partitionImageMap = new HashMap();
        int indexTab, indexDot, index, index1, datasetSize = 0, datasetSizeCutOff, indexPartition, iteration;
        PrintWriter pw = null, pw1 = null;
        float tempFloatArray[] = null, lineFloatArray[] = null;
        float tempDoubleArray[] = null, tempDoubleArray1[] = null;
        float minMaxCosineDistance = 0, cosineDistance, cosineSimilarityTotal;
        ArrayList vectorsAL, membershipsAL, lineAL = new ArrayList();

        try {
            bufReader = new BufferedReader(new FileReader(groundtruthFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                groundtruthMap.put(st.nextToken(), st.nextToken());
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(membershipsFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                membershipsMap.put(lineNumber, line);

            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                datasetSize++;
            }
            bufReader.close();

            datasetSizeCutOff = datasetSize / numberOfPartitions;
            System.out.println("datasetSize, numberOfPartitions, datasetSizeCutOff: " + datasetSize + "..." + numberOfPartitions + "..." + datasetSizeCutOff);
            pw = new PrintWriter(new FileWriter("training_" + k + "_" + knn + ".txt", false), true);
            pw1 = new PrintWriter(new FileWriter("trainingCosineSimlarity_" + k + "_" + knn + ".txt", false), true);
            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));

            for (indexPartition = 0; indexPartition < numberOfPartitions; indexPartition++) {

                System.out.println("indexPartition START: " + indexPartition + "..." + Calendar.getInstance().getTime());
                partitionVectorsMap.clear();
                partitionsMembershipsMap.clear();
                partitionImageMap.clear();
                lineAL.clear();
                System.gc();

                for (iteration = 0; true; iteration++) {
                    line = bufReader.readLine();

                    if (line == null || ((indexPartition < numberOfPartitions - 1) && (iteration > datasetSizeCutOff))) {
                        break;
                    }
                    lineAL.add(line);
                }

                bufReader1 = new BufferedReader(new FileReader(vectorIndexMapReducedFileName));

                while (true) {
                    System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                    line = bufReader1.readLine();
                    if (line == null) {
                        break;
                    }
                    indexTab = line.indexOf('\t');
                    lineNumber1 = line.substring(0, indexTab);
                    line = line.substring(indexTab + 1);
                    indexTab = line.indexOf('\t');
                    line = line.substring(indexTab + 1);
                    st = new StringTokenizer(line);

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; st.hasMoreTokens(); index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }

                    for (iteration = 0; iteration < lineAL.size(); iteration++) {
                        line = (String) lineAL.get(iteration);
                        indexTab = line.indexOf('\t');
                        lineNumber = line.substring(0, indexTab);
                        line = line.substring(indexTab + 1);
                        indexTab = line.indexOf('\t');
                        imageName = line.substring(0, indexTab);
                        line = line.substring(indexTab + 1);
                        ////indexDot = imageName.indexOf('.');
                        ////imageName = imageName.substring(0, indexDot);

                        if (!partitionImageMap.containsKey(iteration)) {
                            partitionImageMap.put(iteration, imageName);
                        }

                        if (!groundtruthMap.containsKey(imageName)) {
                            System.out.println("Image not found: " + imageName);
                            continue;
                        }

                        /*if (membershipsMap.containsKey(lineNumber)) {
                        if(!doNotProcessMap.containsKey(lineNumber)) {
                        doNotProcessMap.put(iteration, lineNumber);
                        }
                        System.out.println("iteration, lineNumber, vectorsAL.size(): " + iteration + " ... " + lineNumber + " do not process");
                        continue;
                        }*/

                        st = new StringTokenizer(line);
                        tempDoubleArray = new float[vectorSize];

                        for (index = 0; st.hasMoreTokens(); index++) {
                            tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                        }
                        cosineDistance = CosineSimilarityARM.distance(tempDoubleArray, tempDoubleArray1);

                        if (!partitionsMembershipsMap.containsKey(iteration)) {
                            membershipsAL = new ArrayList();
                            vectorsAL = new ArrayList();
                            partitionsMembershipsMap.put(iteration, membershipsAL);
                            partitionVectorsMap.put(iteration, vectorsAL);
                        } else {
                            membershipsAL = (ArrayList) partitionsMembershipsMap.get(iteration);
                            vectorsAL = (ArrayList) partitionVectorsMap.get(iteration);
                        }

                        if (!membershipsAL.isEmpty()) {
                            minMaxCosineDistance = (Float) Collections.max(membershipsAL);
                        }
                        tempFloatArray = new float[k];

                        if (membershipsAL.size() < knn || minMaxCosineDistance > cosineDistance) {
                            membershipsLine = (String) membershipsMap.get(lineNumber1);
                            //System.out.println("lineNumber, membershipsLine: " + lineNumber + "..." + membershipsLine);
                            st = new StringTokenizer(membershipsLine);
                            for (index = 0; st.hasMoreTokens(); index++) {
                                tempFloatArray[index] = Float.parseFloat(st.nextToken());
                            }
                            vectorsAL.add(tempFloatArray);
                            membershipsAL.add(cosineDistance);
                            tempFloatArray = null;
                        }

                        if (membershipsAL.size() > knn) {
                            while ((index = membershipsAL.indexOf(minMaxCosineDistance)) != -1) {
                                membershipsAL.remove(index);
                                vectorsAL.remove(index);
                            }
                        }

                        //System.out.println("iteration, membershipsAL.size(), vectorsAL.size(): " + iteration + " ... " + membershipsAL.size() + " ... " + vectorsAL.size());
                    }
                }
                bufReader1.close();

                System.out.println("partitionsMembershipsMap: " + partitionsMembershipsMap);

                for (iteration = 0; iteration < lineAL.size(); iteration++) {
                    //System.out.println("iteration: " + iteration);
                    imageName = (String) partitionImageMap.get(iteration);

                    if (!groundtruthMap.containsKey(imageName)) {
                        System.out.println("Image not found: " + imageName);
                        imageNamePrev = "";
                        continue;
                    }

                    if (isTesting && !imageNamePrev.equals("") && !imageName.equals(imageNamePrev)) {
                        pw.println("END OF DATA POINT");
                    }
                    imageNamePrev = imageName;

                    membershipsAL = (ArrayList) partitionsMembershipsMap.get(iteration);
                    vectorsAL = (ArrayList) partitionVectorsMap.get(iteration);

                    lineFloatArray = new float[k];
                    cosineSimilarityTotal = 0;
                    for (index1 = 0; index1 < membershipsAL.size(); index1++) {
                        tempFloatArray = (float[]) vectorsAL.get(index1);
                        cosineDistance = (Float) membershipsAL.get(index1);
                        System.out.println("iteration, cosineDistance, 1 - cosineDistance, membershipsAL.size(): " + iteration + "..." + cosineDistance + "..." + (1 - cosineDistance) + "..." + membershipsAL.size());
                        cosineSimilarityTotal += (1 - cosineDistance);
                        for (index = 0; index < k; index++) {
                            lineFloatArray[index] += (1 - cosineDistance) * tempFloatArray[index];
                        }
                    }
                    tempFloatArray = null;

                    //System.out.println(Arrays.toString(lineDoubleArray));

                    for (index = 0; index < (k - 1); index++) {
                        pw.print((index + 1) + "^" + (lineFloatArray[index] / (float) cosineSimilarityTotal) + ",");
                    }
                    pw.println((index + 1) + "^" + (lineFloatArray[index] / (float) cosineSimilarityTotal) + ",CLASS=" + groundtruthMap.get(imageName) + "^1");
                    pw1.println((cosineSimilarityTotal / knn) + "," + (cosineSimilarityTotal / membershipsAL.size()));
                    lineFloatArray = null;
                }

                System.out.println("indexPartition END: " + indexPartition + "..." + Calendar.getInstance().getTime());
            }
            if (isTesting) {
                pw.println("END OF DATA POINT");
            }
            bufReader.close();
            pw.close();
            pw1.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetTrainingClusterCentresFloat(String vectorIndexMapFileName, String centresFileName, String membershipsFileName, String groundtruthFileName, String sampledToOriginalMappingFileName, int vectorSize, int k, boolean isTest) {

        BufferedReader bufReader = null, bufReaderGroundTruth = null;
        String line, lineNumber, membershipsLine, token, lineNumberOriginal, groundtruth;
        StringTokenizer st = null;
        HashMap membershipsMap = new HashMap(), sampledToOriginalMap = new HashMap(), groundtruthMap = new HashMap();
        int indexTab, index, lineNumber1 = 0, indexComma;
        PrintWriter pw = null, pw1 = null;
        float tempDoubleArray[] = null, centresFloatArray[][] = null, cosineSimilarity;
        float cosineSimilarityTotal;
        ArrayList membershipsAL = new ArrayList(), lineAL = new ArrayList();

        try {
            bufReader = new BufferedReader(new FileReader(centresFileName));
            centresFloatArray = new float[k][vectorSize];
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

            bufReader = new BufferedReader(new FileReader(membershipsFileName));
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                membershipsMap.put(new String(lineNumber), new String(line));
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(sampledToOriginalMappingFileName));
            lineNumber1 = 0;
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber2: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                sampledToOriginalMap.put(new String(st.nextToken()), new String(st.nextToken()));
            }
            bufReader.close();

            lineNumber1 = 0;
            if (!isTest) {
                bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
                while (true) {
                    //System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                    if (lineNumber1++ % 10000 == 0) {
                        System.out.println("lineNumber3: " + lineNumber1);
                    }
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    indexTab = line.indexOf('\t');
                    lineNumberOriginal = line.substring(0, indexTab);
                    groundtruthMap.put(new String(lineNumberOriginal), null);
                }

                bufReaderGroundTruth = new BufferedReader(new FileReader(groundtruthFileName));
                lineNumber1 = 0;
                for (index = 0; true; index++) {
                    if (lineNumber1++ % 10000 == 0) {
                        System.out.println("lineNumber4: " + lineNumber1);
                    }
                    line = bufReaderGroundTruth.readLine();
                    if (line == null) {
                        break;
                    }
                    lineNumberOriginal = Integer.toString(index);
                    if (groundtruthMap.containsKey(lineNumberOriginal)) {
                        groundtruthMap.remove(lineNumberOriginal);
                        groundtruthMap.put(lineNumberOriginal, line);
                    }
                }
                bufReaderGroundTruth.close();
            }

            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            if (isTest) {
                bufReaderGroundTruth = new BufferedReader(new FileReader(groundtruthFileName));
            }
            pw = new PrintWriter(new FileWriter("training_clusterCentres" + k + ".txt", false), true);
            pw1 = new PrintWriter(new FileWriter("training_clusterCentres_cosineSimlarity_" + k + ".txt", false), true);
            lineNumber1 = 0;
            while (true) {
                //System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber5: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                indexTab = line.indexOf('\t');
                lineNumberOriginal = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                /*indexTab = line.indexOf('\t');
                imageName = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);*/

                if (isTest) {
                    groundtruth = bufReaderGroundTruth.readLine();
                } else {
                    groundtruth = (String) groundtruthMap.get(lineNumberOriginal);
                }
                if (isTest && groundtruth.equals("END OF DATA POINT")) {
                    pw.println("END OF DATA POINT");
                    pw1.println("END OF DATA POINT");
                    while (true) {
                        groundtruth = bufReaderGroundTruth.readLine();
                        if (!groundtruth.equals("END OF DATA POINT")) {
                            break;
                        }
                    }
                }
                groundtruth = groundtruth.replaceAll(",", "^1,CLASS=");
                lineNumber = (String) sampledToOriginalMap.get(lineNumberOriginal);
                if (membershipsMap.containsKey(lineNumber)) {
                    membershipsLine = (String) membershipsMap.get(lineNumber);
                    st = new StringTokenizer(membershipsLine);
                    System.out.println("Short cut: " + lineNumberOriginal + "..." + lineNumber);

                    for (index = 0; st.hasMoreTokens(); index++) {
                        token = st.nextToken();
                        pw.print((index + 1) + "^" + token + ",");
                    }
                    pw.println("CLASS=" + groundtruth + "^1");
                    pw1.println("O");
                    continue;
                }

                st = new StringTokenizer(line);
                tempDoubleArray = new float[vectorSize];
                for (index = 0; index < vectorSize; index++) {
                    tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                }
                membershipsAL.clear();
                cosineSimilarityTotal = 0;

                for (index = 0; index < k; index++) {
                    cosineSimilarity = CosineSimilarityARM.similarity(tempDoubleArray, centresFloatArray[index]);
                    membershipsAL.add(cosineSimilarity);
                    cosineSimilarityTotal += cosineSimilarity;
                }
                for (index = 0; index < (k - 1); index++) {
                    pw.print((index + 1) + "^" + ((Float) membershipsAL.get(index) / (float) cosineSimilarityTotal) + ",");
                }
                pw.println((index + 1) + "^" + ((Float) membershipsAL.get(index) / (float) cosineSimilarityTotal) + ",CLASS=" + groundtruth + "^1");
                pw1.println(cosineSimilarityTotal / membershipsAL.size());
            }
            bufReader.close();
            pw.close();
            pw1.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetTrainingCrispClusterCentresFloat(String vectorIndexMapFileName, String centresFileName, String membershipsFileName, String groundtruthFileName, String sampledToOriginalMappingFileName, int vectorSize, int k, boolean isTest) {

        BufferedReader bufReader = null, bufReaderGroundTruth = null;
        String line, lineNumber, lineNumberOriginal, groundtruth;
        StringTokenizer st = null;
        HashMap membershipsMap = new HashMap(), sampledToOriginalMap = new HashMap(), groundtruthMap = new HashMap();
        int indexTab, index, lineNumber1 = 0, indexMax;
        PrintWriter pw = null, pw1 = null;
        float tempDoubleArray[] = null, centresFloatArray[][] = null, cosineSimilarity;
        float cosineSimilarityMax;
        ArrayList membershipsAL = new ArrayList(), lineAL = new ArrayList();

        try {
            bufReader = new BufferedReader(new FileReader(centresFileName));
            centresFloatArray = new float[k][vectorSize];
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

            bufReader = new BufferedReader(new FileReader(membershipsFileName));
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                membershipsMap.put(new String(lineNumber), Integer.parseInt(line));
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(sampledToOriginalMappingFileName));
            lineNumber1 = 0;
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber2: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                sampledToOriginalMap.put(new String(st.nextToken()), new String(st.nextToken()));
            }
            bufReader.close();

            lineNumber1 = 0;
            if (!isTest) {
                bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
                while (true) {
                    //System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                    if (lineNumber1++ % 10000 == 0) {
                        System.out.println("lineNumber3: " + lineNumber1);
                    }
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    indexTab = line.indexOf('\t');
                    lineNumberOriginal = line.substring(0, indexTab);
                    groundtruthMap.put(new String(lineNumberOriginal), null);
                }

                bufReaderGroundTruth = new BufferedReader(new FileReader(groundtruthFileName));
                lineNumber1 = 0;
                for (index = 0; true; index++) {
                    if (lineNumber1++ % 10000 == 0) {
                        System.out.println("lineNumber4: " + lineNumber1);
                    }
                    line = bufReaderGroundTruth.readLine();
                    if (line == null) {
                        break;
                    }
                    lineNumberOriginal = Integer.toString(index);
                    if (groundtruthMap.containsKey(lineNumberOriginal)) {
                        groundtruthMap.remove(lineNumberOriginal);
                        groundtruthMap.put(lineNumberOriginal, line);
                    }
                }
                bufReaderGroundTruth.close();
            }

            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            if (isTest) {
                bufReaderGroundTruth = new BufferedReader(new FileReader(groundtruthFileName));
            }
            pw = new PrintWriter(new FileWriter("training_clusterCentres" + k + ".txt", false), true);
            pw1 = new PrintWriter(new FileWriter("training_clusterCentres_cosineSimlarity_" + k + ".txt", false), true);
            lineNumber1 = 0;
            while (true) {
                //System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber5: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                indexTab = line.indexOf('\t');
                lineNumberOriginal = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                /*indexTab = line.indexOf('\t');
                imageName = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);*/

                if (isTest) {
                    groundtruth = bufReaderGroundTruth.readLine();
                } else {
                    groundtruth = (String) groundtruthMap.get(lineNumberOriginal);
                }
                if (isTest && groundtruth.equals("END OF DATA POINT")) {
                    pw.println("END OF DATA POINT");
                    pw1.println("END OF DATA POINT");
                    while (true) {
                        groundtruth = bufReaderGroundTruth.readLine();
                        if (!groundtruth.equals("END OF DATA POINT")) {
                            break;
                        }
                    }
                }
                groundtruth = groundtruth.replaceAll(",", "^1,CLASS=");
                lineNumber = (String) sampledToOriginalMap.get(lineNumberOriginal);
                if (membershipsMap.containsKey(lineNumber)) {
                    System.out.println("Short cut: " + lineNumberOriginal + "..." + lineNumber);
                    pw.println(membershipsMap.get(lineNumber) + "^1,CLASS=" + groundtruth + "^1");
                    pw1.println("O");
                    continue;
                }

                st = new StringTokenizer(line);
                tempDoubleArray = new float[vectorSize];
                for (index = 0; index < vectorSize; index++) {
                    tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                }
                membershipsAL.clear();
                cosineSimilarityMax = 0;

                for (index = 0, indexMax = 0; index < k; index++) {
                    cosineSimilarity = CosineSimilarityARM.similarity(tempDoubleArray, centresFloatArray[index]);
                    if(cosineSimilarity > cosineSimilarityMax) {
                        cosineSimilarityMax = cosineSimilarity;
                        indexMax = index;
                    }
                }
                pw.println((indexMax + 1) + "^1,CLASS=" + groundtruth + "^1");
                pw1.println(cosineSimilarityMax);
            }
            bufReader.close();
            pw.close();
            pw1.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetFloat(String vectorIndexMapFileName, String vectorIndexMapReducedFileName, String membershipsFileName, String groundtruthFileName, String sampledToOriginalMappingFileName, int vectorSize, int k, int knn, boolean isTesting) {

        BufferedReader bufReader = null, bufReader1 = null;
        String line, lineNumber, lineNumberOriginal, imageName, membershipsLine, imageNamePrev = "", classLabel = null, token;
        StringTokenizer st = null;
        HashMap groundtruthMap = new HashMap();
        HashMap membershipsMap = new HashMap(), sampledToOriginalMap = new HashMap();
        int indexTab, index, index1, classLabelCount = 0, lineNumber1 = 0;
        PrintWriter pw = null, pw1 = null;
        float tempFloatArray[] = null, lineFloatArray[] = null;
        float tempDoubleArray[] = null, tempDoubleArray1[] = null;
        float minMaxCosineDistance = 0, cosineDistance, cosineSimilarityTotal, membershipSum;
        ArrayList vectorsAL = new ArrayList(), membershipsAL = new ArrayList(), lineMembershipAL = new ArrayList();
        boolean isFirstLine = true;

        try {
            bufReader = new BufferedReader(new FileReader(groundtruthFileName));
            for (index = 0; true; index++) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                //st = new StringTokenizer(line, ",");
                groundtruthMap.put(index, line);
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(membershipsFileName));
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                membershipsMap.put(lineNumber, line);
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(sampledToOriginalMappingFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                sampledToOriginalMap.put(st.nextToken(), st.nextToken());
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("training_" + k + "_" + knn + ".txt", false), true);
            pw1 = new PrintWriter(new FileWriter("training_bov_" + k + "_" + knn + ".txt", false), true);
            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            lineNumber1 = 0;

            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    if (isTesting) {
                        pw.println("END OF DATA POINT");
                    } else {
                        for (index = 0; index < k; index++) {
                            pw1.print((index + 1) + "^" + lineMembershipAL.get(index) + ",");
                        }
                        pw1.println(classLabel + "^" + classLabelCount);
                        isFirstLine = true;
                        lineMembershipAL.clear();
                    }
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumberOriginal = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                indexTab = line.indexOf('\t');
                imageName = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                ////indexDot = imageName.indexOf('.');
                ////imageName = imageName.substring(0, indexDot);

                /*if (!groundtruthMap.containsKey(imageName)) {
                System.out.println("Image not found: " + imageName);
                continue;
                }*/

                if (isTesting && !imageNamePrev.equals("") && !imageName.equals(imageNamePrev)) {
                    pw.println("END OF DATA POINT");
                } else if (!isTesting && !imageNamePrev.equals("") && !imageName.equals(imageNamePrev)) {
                    System.out.println("lineMembershipAL: " + lineMembershipAL);
                    for (index = 0; index < k; index++) {
                        pw1.print((index + 1) + "^" + lineMembershipAL.get(index) + ",");
                    }
                    pw1.println(classLabel + "^" + classLabelCount);
                    isFirstLine = true;
                    lineMembershipAL.clear();
                    classLabelCount = 0;
                }

                classLabelCount++;
                imageNamePrev = imageName;
                lineNumber = (String) sampledToOriginalMap.get(lineNumberOriginal);
                if (membershipsMap.containsKey(lineNumber)) {
                    membershipsLine = (String) membershipsMap.get(lineNumber);
                    st = new StringTokenizer(membershipsLine);
                    System.out.println("Short cut: " + lineNumberOriginal + "..." + lineNumber);

                    for (index = 0; st.hasMoreTokens(); index++) {
                        token = st.nextToken();
                        pw.print((index + 1) + "^" + token + ",");
                        if (!isFirstLine) {
                            membershipSum = (Float) lineMembershipAL.get(index);
                            lineMembershipAL.remove(index);
                        } else {
                            membershipSum = 0;
                        }
                        membershipSum += Float.parseFloat(token);
                        lineMembershipAL.add(index, membershipSum);
                    }
                    pw.println("CLASS=" + groundtruthMap.get(Integer.parseInt(lineNumberOriginal)) + "^1");
                    if (isFirstLine) {
                        classLabel = "CLASS=" + groundtruthMap.get(Integer.parseInt(lineNumberOriginal));
                        isFirstLine = false;
                    }
                    continue;
                }

                st = new StringTokenizer(line);
                tempDoubleArray = new float[vectorSize];

                for (index = 0; st.hasMoreTokens(); index++) {
                    tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                }
                ////dv = new DenseVector(tempDoubleArray);

                vectorsAL.clear();
                membershipsAL.clear();
                bufReader1 = new BufferedReader(new FileReader(vectorIndexMapReducedFileName));

                while (true) {
                    line = bufReader1.readLine();
                    if (line == null) {
                        break;
                    }
                    indexTab = line.indexOf('\t');
                    lineNumber = line.substring(0, indexTab);
                    line = line.substring(indexTab + 1);
                    indexTab = line.indexOf('\t');
                    line = line.substring(indexTab + 1);
                    st = new StringTokenizer(line);

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; st.hasMoreTokens(); index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }
                    ////dv1 = new DenseVector(tempDoubleArray);
                    ////cosineDistance = metric.distance(dv, dv1);
                    cosineDistance = CosineSimilarityARM.distance(tempDoubleArray, tempDoubleArray1);
                    tempDoubleArray1 = null;

                    if (!membershipsAL.isEmpty()) {
                        minMaxCosineDistance = (Float) Collections.max(membershipsAL);
                    }

                    tempFloatArray = new float[k];
                    if (membershipsAL.size() < knn || minMaxCosineDistance > cosineDistance) {
                        membershipsLine = (String) membershipsMap.get(lineNumber);
                        //System.out.println("lineNumber, membershipsLine: " + lineNumber + "..." + membershipsLine);
                        st = new StringTokenizer(membershipsLine);
                        for (index = 0; st.hasMoreTokens(); index++) {
                            tempFloatArray[index] = Float.parseFloat(st.nextToken());
                        }
                        vectorsAL.add(tempFloatArray);
                        membershipsAL.add(cosineDistance);
                        tempFloatArray = null;
                    }

                    if (membershipsAL.size() >= knn) {
                        while ((index = membershipsAL.indexOf(minMaxCosineDistance)) != -1) {
                            membershipsAL.remove(index);
                            vectorsAL.remove(index);
                        }
                    }
                }

                bufReader1.close();
                lineFloatArray = new float[k];
                cosineSimilarityTotal = 0;
                for (index1 = 0; index1 < membershipsAL.size(); index1++) {
                    tempFloatArray = (float[]) vectorsAL.get(index1);
                    cosineDistance = (Float) membershipsAL.get(index1);
                    cosineSimilarityTotal += (1 - cosineDistance);
                    for (index = 0; index < k; index++) {
                        lineFloatArray[index] += (1 - cosineDistance) * tempFloatArray[index];
                    }
                }
                tempFloatArray = null;
                //System.out.println(Arrays.toString(lineDoubleArray));
                for (index = 0; index < (k - 1); index++) {
                    pw.print((index + 1) + "^" + (lineFloatArray[index] / (float) cosineSimilarityTotal) + ",");

                }
                pw.println((index + 1) + "^" + (lineFloatArray[index] / (float) cosineSimilarityTotal) + ",CLASS=" + groundtruthMap.get(Integer.parseInt(lineNumberOriginal)) + "^1");
                lineFloatArray = null;
            }
            bufReader.close();
            pw.close();
            pw1.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetFloatYLR(String vectorIndexMapFileName, String YLRFile, String membershipsFileName, int vectorSize, int k) {

        BufferedReader bufReader = null, bufReader1 = null;
        String line, line1, lineNumber, membershipsLine, classLabelPrev = "", classLabel = null, token;
        StringTokenizer st = null;
        HashMap membershipsMap = new HashMap();
        int indexTab, index, relevance;
        PrintWriter pw = null, pw1 = null;
        float membershipSum, classLabelMembership;
        ArrayList lineMembershipAL = new ArrayList();
        boolean isFirstLine;

        try {
            bufReader = new BufferedReader(new FileReader(membershipsFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                membershipsMap.put(lineNumber, line);

            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("training_" + k + ".txt", false), true);
            pw1 = new PrintWriter(new FileWriter("training_bov_" + k + ".txt", false), true);
            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            bufReader1 = new BufferedReader(new FileReader(YLRFile));

            while (true) {
                line = bufReader.readLine();
                line1 = bufReader1.readLine();
                if (line == null) {
                    for (index = 0; index < k; index++) {
                        pw1.print((index + 1) + "^" + lineMembershipAL.get(index) + ",");
                    }
                    pw1.println("CLASS=" + classLabel + "^" + lineMembershipAL.get(index));
                    break;
                }

                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                indexTab = line.indexOf('\t');
                line = line.substring(indexTab + 1);

                indexTab = line1.indexOf(' ');
                relevance = Integer.parseInt(line1.substring(0, indexTab));
                line1 = line1.substring(indexTab + 1);
                indexTab = line1.indexOf(' ');
                line1 = line1.substring(0, indexTab);
                indexTab = line1.indexOf(':');
                classLabel = line1.substring(indexTab + 1);

                System.out.println("relevance: " + relevance);
                System.out.println("classLabel: " + classLabel);

                if (!classLabelPrev.equals("") && !classLabel.equals(classLabelPrev)) {
                    //System.out.println("lineMembershipAL: " + lineMembershipAL);
                    for (index = 0; index < k; index++) {
                        pw1.print((index + 1) + "^" + lineMembershipAL.get(index) + ",");
                    }
                    pw1.println("CLASS=" + classLabelPrev + "^" + lineMembershipAL.get(index));
                    isFirstLine = true;
                    lineMembershipAL.clear();
                } else if (classLabelPrev.equals("")) {
                    isFirstLine = true;
                } else {
                    isFirstLine = false;
                }

                classLabelPrev = classLabel;
                if (membershipsMap.containsKey(lineNumber)) {
                    membershipsLine = (String) membershipsMap.get(lineNumber);
                    st = new StringTokenizer(membershipsLine);
                    //System.out.println("Short cut: " + lineNumber);

                    for (index = 0; st.hasMoreTokens(); index++) {
                        token = st.nextToken();
                        pw.print((index + 1) + "^" + token + ",");
                        if (!isFirstLine) {
                            membershipSum = (Float) lineMembershipAL.get(index);
                            lineMembershipAL.remove(index);
                        } else {
                            membershipSum = 0;
                        }
                        membershipSum += Float.parseFloat(token);
                        lineMembershipAL.add(index, membershipSum);
                    }
                    if (!isFirstLine) {
                        membershipSum = (Float) lineMembershipAL.get(index);
                        lineMembershipAL.remove(index);
                    } else {
                        membershipSum = 0;
                    }

                    classLabelMembership = ((float) (relevance + 1)) / ((float) 5);
                    membershipSum += classLabelMembership;
                    lineMembershipAL.add(index, membershipSum);
                    //pw.println("CLASS=" + classLabel + "^" + classLabelMembership);
                    pw.println("CLASS=Relevant^" + classLabelMembership);
                    //System.out.println("CLASS=" + classLabel + "^" + classLabelMembership);
                }
            }
            bufReader.close();
            bufReader1.close();
            pw.close();
            pw1.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetFloatMultiClassYLR(String vectorIndexMapFileName, String YLRFile, String membershipsFileName, int vectorSize, int k) {

        BufferedReader bufReader = null, bufReader1 = null;
        String line, line1, lineNumber, membershipsLine, token;
        StringTokenizer st = null;
        HashMap membershipsMap = new HashMap();
        int indexTab, index, relevance;
        PrintWriter pw = null;

        try {
            bufReader = new BufferedReader(new FileReader(membershipsFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                membershipsMap.put(lineNumber, line);

            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("training_" + k + ".txt", false), true);
            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            bufReader1 = new BufferedReader(new FileReader(YLRFile));

            while (true) {
                line = bufReader.readLine();
                line1 = bufReader1.readLine();
                if (line == null) {
                    break;
                }
                indexTab = line.indexOf('\t');
                lineNumber = line.substring(0, indexTab);
                line = line.substring(indexTab + 1);
                indexTab = line.indexOf('\t');
                line = line.substring(indexTab + 1);

                indexTab = line1.indexOf(' ');
                relevance = Integer.parseInt(line1.substring(0, indexTab));

                System.out.println("relevance: " + relevance);

                if (membershipsMap.containsKey(lineNumber)) {
                    membershipsLine = (String) membershipsMap.get(lineNumber);
                    st = new StringTokenizer(membershipsLine);
                    //System.out.println("Short cut: " + lineNumber);

                    for (index = 0; st.hasMoreTokens(); index++) {
                        token = st.nextToken();
                        pw.print((index + 1) + "^" + token + ",");
                    }
                    pw.println("CLASS=" + relevance + "^1");
                }
            }
            bufReader.close();
            bufReader1.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetFloatMultiClassAllFeaturesYLR(String YLRFile, int vectorSize) {

        BufferedReader bufReader = null, bufReader1 = null;
        String line, line1, lineNumber, membershipsLine, token;
        StringTokenizer st = null;
        HashMap membershipsMap = new HashMap();
        int indexTab, index, relevance;
        PrintWriter pw = null;
        float classLabelMembership;

        try {
            pw = new PrintWriter(new FileWriter("test.txt", false), true);
            bufReader = new BufferedReader(new FileReader(YLRFile));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (!line.contains("@") && line.contains(",")) {
                    st = new StringTokenizer(line, ",");
                    relevance = Integer.parseInt(st.nextToken());

                    for (index = 0; st.hasMoreTokens(); index++) {
                        token = st.nextToken();
                        pw.print((index + 1) + "^" + token + ",");
                    }
                    pw.println("CLASS=" + relevance + "^1");
                    classLabelMembership = ((float) (relevance + 1)) / ((float) 5);
                    //pw.println("CLASS=Relevant" + "^" + classLabelMembership);
                }
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createBOWFormatMIR(String fileName, String classIndexMappingFileName, int k) {
        BufferedReader bufReader = null;
        String line = null, token;
        ArrayList lineMembershipAL = new ArrayList();
        TreeSet classSet = new TreeSet();
        int index, indexCaret, lineNumber1 = 0;
        PrintWriter pw = null, pw1 = null;
        StringTokenizer st = null;
        float membership, membershipSum, totalMembershipSum = 0;
        boolean isFirstLine = true;
        TreeMap<String, Integer> classIndexMap = new TreeMap();
        boolean[] classExistence = null;

        try {
            bufReader = new BufferedReader(new FileReader(classIndexMappingFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                token = st.nextToken();
                classIndexMap.put(new String("CLASS=" +  token + "^1"), Integer.parseInt(st.nextToken()));
                classSet.add(new String(token));
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("testBOW.txt", false), true);
            pw1 = new PrintWriter(new FileWriter("testBOWGroundTruth.txt", false), true);
            classExistence = new boolean[classIndexMap.size()];

            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                } else if (line.equals("END OF DATA POINT")) {
                    for (index = 0; index < k - 1; index++) {
                        pw.print((index + 1) + "^" + (((Float) lineMembershipAL.get(index)) / totalMembershipSum) + ",");
                    }
                    pw.println((index + 1) + "^" + (((Float) lineMembershipAL.get(index)) / totalMembershipSum));
                    pw.println("END OF DATA POINT");

                    Iterator iterator = classSet.iterator();
                    
                    for (index = 0; index < classExistence.length - 1; index++) {
                        if (classExistence[index]) {
                            pw1.print(iterator.next() + "=1,");
                        } else {
                            pw1.print(iterator.next() + "=0,");
                        }
                    }
                    if (classExistence[index]) {
                        pw1.println(iterator.next() + "=1");
                    } else {
                        pw1.println(iterator.next() + "=0");
                    }

                    isFirstLine = true;
                    lineMembershipAL.clear();
                    classExistence = new boolean[classIndexMap.size()];
                    totalMembershipSum = 0;
                    continue;
                }
                st = new StringTokenizer(line, ",");
                for (index = 0; index < k; index++) {
                    token = st.nextToken();
                    indexCaret = token.indexOf('^');
                    token = token.substring(indexCaret + 1);
                    membership = Float.parseFloat(token);
                    if (!isFirstLine) {
                        membershipSum = (Float) lineMembershipAL.get(index);
                        lineMembershipAL.remove(index);
                    } else {
                        membershipSum = 0;
                    }
                    membershipSum += membership;
                    totalMembershipSum += membership;
                    lineMembershipAL.add(index, membershipSum);
                }
                if (isFirstLine) {
                    while (st.hasMoreTokens()) {
                        index = classIndexMap.get(st.nextToken());
                        classExistence[index - 1] = true;
                    }
                    isFirstLine = false;
                }
            }
            pw.close();
            bufReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createCrispBOWFormatMIR(String fileName, String classIndexMappingFileName, int k, boolean isTest) {
        BufferedReader bufReader = null;
        String line = null, token, className = null;
        HashMap lineMembershipMap = new HashMap();
        TreeSet classSet = new TreeSet();
        int index, lineNumber1 = 0, indexCaret;
        PrintWriter pw = null, pw1 = null;
        StringTokenizer st = null;
        int membership, membershipSum, totalMembershipSum = 0;
        boolean isFirstLine = true;
        TreeMap<String, Integer> classIndexMap = new TreeMap();
        boolean[] classExistence = null;

        try {
            bufReader = new BufferedReader(new FileReader(classIndexMappingFileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                token = st.nextToken();
                classIndexMap.put(new String("CLASS=" +  token + "^1"), Integer.parseInt(st.nextToken()));
                classSet.add(new String(token));
            }
            bufReader.close();

            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("testBOWK.txt", false), true);
            pw1 = new PrintWriter(new FileWriter("testBOWGroundTruthK.txt", false), true);
            classExistence = new boolean[classIndexMap.size()];

            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                } else if (line.equals("END OF DATA POINT")) {
                    for (index = 0; index < k - 1; index++) {
                        if (lineMembershipMap.containsKey(index)) {
                            pw.print((index + 1) + "^" + ((Integer) lineMembershipMap.get(index)) + ",");
                        }
                    }
                    if (lineMembershipMap.containsKey(index)) {
                        pw.print((index + 1) + "^" + ((Integer) lineMembershipMap.get(index)));
                        if(!isTest) {
                            pw.println("," + className);
                        }
                        else {
                            pw.println();
                        }
                    }
                    else {
                        if(!isTest) {
                            pw.println(className);
                        } else {
                            pw.println();
                        }
                    }
                    pw.println("END OF DATA POINT");

                    Iterator iterator = classSet.iterator();

                    for (index = 0; index < classExistence.length - 1; index++) {
                        if (classExistence[index]) {
                            pw1.print(iterator.next() + "=1,");
                        } else {
                            pw1.print(iterator.next() + "=0,");
                        }
                    }
                    if (classExistence[index]) {
                        pw1.println(iterator.next() + "=1");
                    } else {
                        pw1.println(iterator.next() + "=0");
                    }

                    isFirstLine = true;
                    lineMembershipMap.clear();
                    classExistence = new boolean[classIndexMap.size()];
                    totalMembershipSum = 0;
                    continue;
                }
                st = new StringTokenizer(line, ",");
                token = st.nextToken();
                indexCaret = token.indexOf('^');
                index = Integer.parseInt(token.substring(0, indexCaret));
                membership = Integer.parseInt(token.substring(indexCaret + 1));
                if (!isFirstLine && lineMembershipMap.containsKey(index)) {
                    membershipSum = (Integer) lineMembershipMap.get(index);
                    lineMembershipMap.remove(index);
                } else {
                    membershipSum = 0;
                }
                membershipSum += membership;
                totalMembershipSum += membership;
                lineMembershipMap.put(index, membershipSum);

                if (isFirstLine) {
                    while (st.hasMoreTokens()) {
                        className = st.nextToken();
                        index = classIndexMap.get(className);
                        classExistence[index - 1] = true;
                    }
                    isFirstLine = false;
                }
            }
            pw.close();
            bufReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createBOWFormat(String fileName, int k) {
        BufferedReader bufReader = null;
        String line = null, classLabel = null, token;
        ArrayList lineMembershipAL = new ArrayList();
        int index, indexCaret, classLabelCount = 0;
        PrintWriter pw = null;
        StringTokenizer st = null;
        float membership, membershipSum;
        boolean isFirstLine = true;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("test_bow_" + k + ".txt", false), true);

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                } else if (line.equals("END OF DATA POINT")) {
                    for (index = 0; index < k; index++) {
                        pw.print((index + 1) + "^" + lineMembershipAL.get(index) + ",");
                    }
                    pw.println(classLabel + "^" + classLabelCount);
                    pw.println("END OF DATA POINT");
                    isFirstLine = true;
                    classLabelCount = 0;
                    lineMembershipAL.clear();
                    continue;
                }
                st = new StringTokenizer(line, ",");
                for (index = 0; index < k; index++) {
                    token = st.nextToken();
                    indexCaret = token.indexOf('^');
                    token = token.substring(indexCaret + 1);
                    membership = Float.parseFloat(token);
                    if (!isFirstLine) {
                        membershipSum = (Float) lineMembershipAL.get(index);
                        lineMembershipAL.remove(index);
                    } else {
                        membershipSum = 0;
                    }
                    membershipSum += membership;
                    lineMembershipAL.add(index, membershipSum);
                }
                classLabelCount++;
                if (isFirstLine) {
                    token = st.nextToken();
                    System.out.println("token: " + token);
                    indexCaret = token.indexOf('^');
                    classLabel = token.substring(0, indexCaret);
                    System.out.println("classLabel: " + classLabel);
                    isFirstLine = false;
                }
            }
            pw.close();
            bufReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createFuzzyDatasetTrainingClusterCentresFloatYLR(String vectorIndexMapFileName, String centresFileName, String YLRFile, int vectorSize, int k) {

        BufferedReader bufReader = null, bufReader1 = null;
        String line, line1, classLabel;
        StringTokenizer st = null;
        int indexTab, index;
        PrintWriter pw = null, pw1 = null;
        float tempDoubleArray[] = null, centresFloatArray[][] = null, cosineSimilarity;
        float cosineSimilarityTotal;
        ArrayList membershipsAL = new ArrayList(), lineAL = new ArrayList();

        try {
            bufReader = new BufferedReader(new FileReader(centresFileName));
            bufReader1 = new BufferedReader(new FileReader(YLRFile));
            centresFloatArray = new float[k][vectorSize];
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

            bufReader = new BufferedReader(new FileReader(vectorIndexMapFileName));
            pw = new PrintWriter(new FileWriter("training_clusterCentres" + k + ".txt", false), true);
            pw1 = new PrintWriter(new FileWriter("training_clusterCentres_cosineSimlarity_" + k + ".txt", false), true);
            while (true) {
                System.out.println("START: " + "..." + Calendar.getInstance().getTime());
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                line1 = bufReader1.readLine();
                indexTab = line1.indexOf(' ');
                line1 = line1.substring(indexTab + 1);
                indexTab = line1.indexOf(' ');
                line1 = line1.substring(0, indexTab);
                indexTab = line1.indexOf(':');
                classLabel = line1.substring(indexTab + 1);

                st = new StringTokenizer(line, ",");
                st.nextToken();

                tempDoubleArray = new float[vectorSize];
                for (index = 0; index < vectorSize; index++) {
                    tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                }

                membershipsAL.clear();
                cosineSimilarityTotal = 0;

                for (index = 0; index < k; index++) {
                    cosineSimilarity = CosineSimilarityARM.similarity(tempDoubleArray, centresFloatArray[index]);
                    membershipsAL.add(cosineSimilarity);
                    cosineSimilarityTotal += cosineSimilarity;
                }
                pw.print("CLASS=" + classLabel + ",");
                for (index = 0; index < (k - 1); index++) {
                    pw.print((index + 1) + "^" + ((Float) membershipsAL.get(index) / (float) cosineSimilarityTotal) + ",");
                }
                //pw.println((index + 1) + "^" + ((Float) membershipsAL.get(index) / (float) cosineSimilarityTotal) + ",CLASS=" + groundtruthMap.get(imageName) + "^1");
                //pw.println((index + 1) + "^" + ((Float) membershipsAL.get(index) / (float) cosineSimilarityTotal) + "," + classAttribute + "^1," + lineNumber + "," + imageName);
                pw.println((index + 1) + "^" + ((Float) membershipsAL.get(index) / (float) cosineSimilarityTotal));
                pw1.println(cosineSimilarityTotal / membershipsAL.size());
            }
            //pw.println("END OF DATA POINT");
            bufReader.close();
            pw.close();
            pw1.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SURF surf = new SURF();
        //surf.convertToFCMFormat(args[0]);
        //surf.convertToFCMFormatYLR(args[0]);
        ////surf.createFuzzyDataset(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
        //surf.createFuzzyDatasetFloat(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), false);
        //surf.createFuzzyDatasetTrainingPartitionsFloat(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), true);
        //surf.createBOWFormat(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        //surf.createFuzzyDatasetTrainingClusterCentresFloat(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        //surf.createFuzzyDatasetTrainingClusterCentresFloatYLR(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        //surf.createFuzzyDatasetFloatYLR(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        //surf.createFuzzyDatasetFloatMultiClassYLR(args[0], args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        //surf.createFuzzyDatasetFloatMultiClassAllFeaturesYLR(args[0], Integer.parseInt(args[1]));
        //surf.MIRConvertToFCMFormat(Integer.parseInt(args[0]), args[1], args[2]);
        //surf.MIRConvertToFCMFormatCopy(Integer.parseInt(args[0]), args[1], args[2]);
        //surf.deDupe(args[0]);
        //surf.mapSampledToOriginal(args[0], args[1]);
        //surf.createFuzzyDatasetFloat(args[0], args[1], args[2], args[3], args[4], Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), false);
        surf.createFuzzyDatasetTrainingClusterCentresFloat(args[0], args[1], args[2], args[3], args[4], Integer.parseInt(args[5]), Integer.parseInt(args[6]), true);
        //surf.createBOWFormatMIR(args[0], args[1], Integer.parseInt(args[2]));
        //surf.createCrispBOWFormatMIR(args[0], args[1], Integer.parseInt(args[2]), true);
        //surf.createFuzzyDatasetTrainingCrispClusterCentresFloat(args[0], args[1], args[2], args[3], args[4], Integer.parseInt(args[5]), Integer.parseInt(args[6]), true);
    }
}
