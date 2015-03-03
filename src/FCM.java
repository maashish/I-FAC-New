
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.StringTokenizer;

public class FCM {
    /*public void generateFuzzyClusters(String fileName, int k, int vectorSize, double m){
    BufferedReader bufReader = null;
    double jNew = 0, jOld, difference;
    double u = (double) 1 / (double) k;
    float uNewArray [][] = null;
    int iteration, index, i, j, j1, lineNumbers[] = null;
    String line = null;
    StringTokenizer st = null;
    DenseVector dv = null, dv1 = null;
    double clusterCenters[][] = null, tempDoubleArray[] = null, uTotal[] = null;
    double cosineDistance, x;
    float uRandomTotal;
    Random random = new Random();
    PrintWriter pw = null;
    CosineSimilarity metric = new CosineSimilarity();

    try { */
    /*bufReader = new BufferedReader(new FileReader(fileName));
    for (j = 0; j < k; j++) {
    line = bufReader.readLine();
    st = new StringTokenizer(line);
    st.nextToken();
    clusterCenters = new double [k] [vectorSize];

    for(index = 0; st.hasMoreTokens(); index++) {
    clusterCenters[j][index] = Double.parseDouble(st.nextToken());
    }
    }*/
    /*iteration = -1;
    while (true) {
    jOld = jNew;
    jNew = 0;
    iteration++;
    // Calculate J - START

    bufReader = new BufferedReader(new FileReader(fileName));
    for (i = 0; true; i++) {
    line = bufReader.readLine();
    if (line == null) {
    break;
    }
    if (iteration == 0) {
    continue;
    }

    st = new StringTokenizer(line);
    st.nextToken();
    st.nextToken();
    tempDoubleArray = new double[vectorSize];

    for (index = 0; st.hasMoreTokens(); index++) {
    tempDoubleArray[index] = Double.parseDouble(st.nextToken());
    }
    dv = new DenseVector(tempDoubleArray);

    for (j = 0; j < k; j++) {
    u = uNewArray[i][j];
    dv1 = new DenseVector(clusterCenters[j]);
    cosineDistance = metric.distance(dv, dv1);
    jNew += Math.pow(u, m) * Math.pow(cosineDistance, 2);
    }
    }

    if(iteration==0) {
    lineNumbers = new int[i];
    }

    bufReader.close();
    difference = Math.abs(jNew -  jOld);
    System.out.println("Iteration, jOld, jNew, difference: " + iteration + "..." + jOld + "..." + jNew + "..." + difference + "..." + Calendar.getInstance().getTime());
    if ((iteration > 20 && difference <= 0.1 || (iteration <= 20 && difference <= 0.0001)) && jNew != 0) {
    index = i;
    pw = new PrintWriter(new FileWriter("fuzzyMemberships" + k + "_" + m + ".txt", false), true);
    for (i = 0; i < index; i++) {
    pw.print((lineNumbers[i]) + "\t");
    for(j = 0; j < k - 1; j++) {
    pw.print(uNewArray[i][j] + "\t");
    }
    pw.println(uNewArray[i][j]);

    }
    pw.close();
    pw = new PrintWriter(new FileWriter("fuzzyClusters" + k + "_" + m + ".txt", false), true);
    for (j = 0; j < k; j++) {
    for (index = 0; index < vectorSize - 1; index++) {
    pw.print(clusterCenters[j][index] + "\t");
    }
    pw.println(clusterCenters[j][index]);
    }
    pw.close();
    break;
    }

    // Calculate J - END

    // Calculate Random u - START

    if(iteration == 0) {

    uNewArray = new float[i][k];
    index = i;
    for(i = 0; i < index; i++) {
    uRandomTotal = 0;
    for(j = 0; j < k; j++) {
    uNewArray[i][j] = random.nextFloat();
    uRandomTotal += uNewArray[i][j];
    }
    for(j = 0; j < k; j++) {
    uNewArray[i][j] = uNewArray[i][j] / uRandomTotal;
    }
    }
    //System.out.println("uNewArray RANDOM");
    //System.out.println(Arrays.toString(uNewArray[0]));
    //System.out.println(Arrays.toString(uNewArray[1]));
    }

    // Calculate Random u - END

    // Calculate C - START
    bufReader = new BufferedReader(new FileReader(fileName));
    clusterCenters = null;
    System.gc();
    clusterCenters = new double [k] [vectorSize];
    uTotal = new double[k];

    //System.out.println("uTotal");
    //System.out.println(Arrays.toString(uTotal));

    for (i = 0; true; i++) {
    line = bufReader.readLine();
    if (line == null) {
    break;
    }

    st = new StringTokenizer(line);
    if(iteration == 0) {
    lineNumbers[i] = Integer.parseInt(st.nextToken());
    }
    else {
    st.nextToken();
    }
    st.nextToken();
    for (index = 0; index < vectorSize; index++) {
    x = Double.parseDouble(st.nextToken());
    for (j = 0; j < k; j++) {
    //if (iteration != 0) {
    u = uNewArray[i][j];
    //}
    uTotal[j] += Math.pow(u, m);

    if(i == 0) {
    clusterCenters[j][index] = Math.pow(u, m) * x;
    }
    else {
    clusterCenters[j][index] += Math.pow(u, m) * x;
    }
    }
    }
    }

    for (index = 0; index < vectorSize; index++) {
    for (j = 0; j < k; j++) {
    clusterCenters[j][index] = clusterCenters[j][index] / uTotal[j];
    }
    }
    bufReader.close();

    //System.out.println("clusterCenters");
    //System.out.println(Arrays.toString(clusterCenters[0]));


    // Calculate C - END

    // Calculate u - START

    uNewArray = null;
    System.gc();
    uNewArray = new float[i][k];
    bufReader = new BufferedReader(new FileReader(fileName));

    for (i = 0; true; i++) {
    line = bufReader.readLine();
    if (line == null) {
    break;
    }
    st = new StringTokenizer(line);
    st.nextToken();
    st.nextToken();

    tempDoubleArray = new double[vectorSize];
    for (index = 0; st.hasMoreTokens(); index++) {
    tempDoubleArray[index] = Double.parseDouble(st.nextToken());
    }
    dv = new DenseVector(tempDoubleArray);
    //System.out.println("LINE: " + dv.toString());

    tempDoubleArray = new double[k];
    for (j = 0; j < k; j++) {
    dv1 = new DenseVector(clusterCenters[j]);
    tempDoubleArray[j] = metric.distance(dv, dv1);
    }
    //System.out.println("tempDoubleArray: " + Arrays.toString(tempDoubleArray));

    for (j = 0; j < k; j++) {
    for (j1 = 0; j1 < k; j1++) {
    uNewArray[i][j] += Math.pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2/(m-1)));
    }
    uNewArray[i][j] = (float) 1 / uNewArray[i][j];
    }
    }
    index = i;
    bufReader.close();

    }
    } catch (IOException ioe) {
    ioe.printStackTrace();
    }
    }*/

    public void generateFuzzyClustersFloat(String fileName, int k, int vectorSize, float m) {
        BufferedReader bufReader = null;
        float jNew = 0, jOld, difference;
        float u = (float) 1 / (float) k;
        float uNewArray[][] = null;
        int iteration, index, i, j, j1, lineNumbers[] = null;
        String line = null;
        StringTokenizer st = null;
        ////DenseVector dv = null, dv1 = null;
        float clusterCenters[][] = null, tempDoubleArray[] = null, uTotal[] = null, tempDoubleArray1[] = null;
        float cosineDistance, x;
        float uRandomTotal;
        Random random = new Random();
        PrintWriter pw = null;
        File file = null;
        long startMs, endMs, startMs1, endMs1, startMs2, endMs2;

        try {
            iteration = -1;
            while (true) {
                jOld = jNew;
                jNew = 0;
                iteration++;
                // Calculate J - START
                startMs = System.currentTimeMillis();

                bufReader = new BufferedReader(new FileReader(fileName));
                for (i = 0; true; i++) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (iteration == 0) {
                        continue;
                    }

                    st = new StringTokenizer(line);
                    st.nextToken();
                    st.nextToken();
                    tempDoubleArray = new float[vectorSize];

                    for (index = 0; st.hasMoreTokens(); index++) {
                        tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                    }
                    ////dv = new DenseVector(tempDoubleArray);

                    for (j = 0; j < k; j++) {
                        u = uNewArray[i][j];
                        ////dv1 = new DenseVector(clusterCenters[j]);
                        ////cosineDistance = metric.distance(dv, dv1);
                        cosineDistance = CosineSimilarityARM.distance(tempDoubleArray, clusterCenters[j]);
                        jNew += (float) (Math.pow(u, m) * Math.pow(cosineDistance, 2));
                        //jNew += (float) (pow(u, m) * pow(cosineDistance, 2));
                    }
                }

                if (iteration == 0) {
                    lineNumbers = new int[i];
                }

                bufReader.close();
                difference = (float) Math.abs(jNew - jOld);
                System.out.println("Iteration, jOld, jNew, difference: " + iteration + "..." + jOld + "..." + jNew + "..." + difference + "..." + Calendar.getInstance().getTime());
                //if ((iteration > 15 && difference <= 1 || (iteration <= 15 && difference <= 0.001)) && jNew != 0) {
                if (iteration != 0) {
                    index = i;
                    file = new File("fuzzyMemberships" + k + "_" + m + ".txt");
                    if (file.exists()) {
                        System.out.println("fuzzyMemberships file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter("fuzzyMemberships" + k + "_" + m + ".txt", false), true);
                    for (i = 0; i < index; i++) {
                        pw.print((lineNumbers[i]) + "\t");
                        for (j = 0; j < k - 1; j++) {
                            pw.print(uNewArray[i][j] + "\t");
                        }
                        pw.println(uNewArray[i][j]);

                    }
                    pw.close();
                    file = new File("fuzzyClusters" + k + "_" + m + ".txt");
                    if (file.exists()) {
                        System.out.println("fuzzyClusters file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter("fuzzyClusters" + k + "_" + m + ".txt", false), true);
                    for (j = 0; j < k; j++) {
                        for (index = 0; index < vectorSize - 1; index++) {
                            pw.print(clusterCenters[j][index] + "\t");
                        }
                        pw.println(clusterCenters[j][index]);
                    }
                    pw.close();
                    if ((iteration > 15 && difference <= 1 || (iteration <= 15 && difference <= 0.001)) && jNew != 0) {
                        System.out.println("FCM END");
                        break;
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time J: " + (endMs - startMs));

                // Calculate J - END

                // Calculate Random u - START

                if (iteration == 0) {

                    uNewArray = new float[i][k];
                    index = i;
                    for (i = 0; i < index; i++) {
                        uRandomTotal = 0;
                        for (j = 0; j < k; j++) {
                            uNewArray[i][j] = random.nextFloat();
                            uRandomTotal += uNewArray[i][j];
                        }
                        for (j = 0; j < k; j++) {
                            uNewArray[i][j] = uNewArray[i][j] / uRandomTotal;
                        }
                    }
                    //System.out.println("uNewArray RANDOM");
                    //System.out.println(Arrays.toString(uNewArray[0]));
                    //System.out.println(Arrays.toString(uNewArray[1]));
                }

                // Calculate Random u - END

                // Calculate C - START
                startMs = System.currentTimeMillis();
                bufReader = new BufferedReader(new FileReader(fileName));
                clusterCenters = null;
                System.gc();
                clusterCenters = new float[k][vectorSize];
                uTotal = new float[k];

                //System.out.println("uTotal");
                //System.out.println(Arrays.toString(uTotal));

                for (i = 0; true; i++) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }

                    st = new StringTokenizer(line);
                    if (iteration == 0) {
                        lineNumbers[i] = Integer.parseInt(st.nextToken());
                    } else {
                        st.nextToken();
                    }
                    st.nextToken();
                    for (index = 0; index < vectorSize; index++) {
                        x = Float.parseFloat(st.nextToken());
                        for (j = 0; j < k; j++) {
                            //if (iteration != 0) {
                            u = uNewArray[i][j];
                            //}
                            uTotal[j] += (float) Math.pow(u, m);
                            //uTotal[j] += (float) pow(u, m);

                            if (i == 0) {
                                clusterCenters[j][index] = (float) (Math.pow(u, m) * x);
                                //clusterCenters[j][index] = (float) (pow(u, m) * x);
                            } else {
                                clusterCenters[j][index] += (float) (Math.pow(u, m) * x);
                                //clusterCenters[j][index] += (float) (pow(u, m) * x);
                            }
                        }
                    }
                }

                for (index = 0; index < vectorSize; index++) {
                    for (j = 0; j < k; j++) {
                        clusterCenters[j][index] = clusterCenters[j][index] / uTotal[j];
                    }
                }
                bufReader.close();
                endMs = System.currentTimeMillis();
                System.out.println("Run Time C: " + (endMs - startMs));

                //System.out.println("clusterCenters");
                //System.out.println(Arrays.toString(clusterCenters[0]));

                // Calculate C - END

                // Calculate u - START

                startMs = System.currentTimeMillis();

                uNewArray = null;
                System.gc();
                uNewArray = new float[i][k];
                bufReader = new BufferedReader(new FileReader(fileName));

                for (i = 0; true; i++) {
                    //startMs1 = System.currentTimeMillis();
                    //startMs2 = System.currentTimeMillis();
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    st = new StringTokenizer(line);
                    st.nextToken();
                    st.nextToken();

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; st.hasMoreTokens(); index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u11: " + (endMs1 - startMs1));
                    //startMs1 = System.currentTimeMillis();
                    ////dv = new DenseVector(tempDoubleArray);
                    //System.out.println("LINE: " + dv.toString());

                    tempDoubleArray = new float[k];
                    for (j = 0; j < k; j++) {
                        ////dv1 = new DenseVector(clusterCenters[j]);
                        ////tempDoubleArray[j] = metric.distance(dv, dv1);
                        tempDoubleArray[j] = CosineSimilarityARM.distance(tempDoubleArray1, clusterCenters[j]);
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u12: " + (endMs1 - startMs1));
                    //startMs1 = System.currentTimeMillis();
                    //System.out.println("tempDoubleArray: " + Arrays.toString(tempDoubleArray));

                    for (j = 0; j < k; j++) {
                        for (j1 = 0; j1 < k; j1++) {
                            uNewArray[i][j] += Math.pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2 / (m - 1)));
                            //uNewArray[i][j] += pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2/(m-1)));
                        }
                        uNewArray[i][j] = (float) 1 / uNewArray[i][j];
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u13: " + (endMs1 - startMs1));
                    //endMs2 = System.currentTimeMillis();
                    //System.out.println("Run Time u2: " + (endMs2 - startMs2));
                }
                index = i;
                bufReader.close();
                endMs = System.currentTimeMillis();
                System.out.println("Run Time u: " + (endMs - startMs));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void generateFuzzyClustersFloatInMemory(String fileName, int k, int vectorSize, float m) {
        BufferedReader bufReader = null;
        float jNew = 0, jOld, difference;
        float u = (float) 1 / (float) k;
        float uNewArray[][] = null;
        int iteration, index, i, j, j1, lineNumbers[] = null, lineNumber = 0;
        String line = null;
        StringTokenizer st = null;
        ////DenseVector dv = null, dv1 = null;
        float clusterCenters[][] = null, tempDoubleArray[] = null, uTotal[] = null, tempDoubleArray1[] = null;
        float cosineDistance, x;
        float uRandomTotal;
        Random random = new Random();
        PrintWriter pw = null;
        File file = null;
        ArrayList lineAL = new ArrayList();
        long startMs, endMs, startMs1, endMs1, startMs2, endMs2;

        try {
            iteration = -1;

            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if(lineNumber++ % 100000 == 0) {
                    System.out.println("lineNumber: " + lineNumber + "..." + Calendar.getInstance().getTime());
                }
                lineAL.add(line);
            }
            bufReader.close();
            System.out.println(fileName + "reading DONE");

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
                        st.nextToken();
                        //st.nextToken();
                        tempDoubleArray = new float[vectorSize];

                        //for (index = 0; st.hasMoreTokens(); index++) {
                        for (index = 0; index < vectorSize; index++) {
                            tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                        }

                        for (j = 0; j < k; j++) {
                            u = uNewArray[i][j];
                            cosineDistance = CosineSimilarityARM.distance(tempDoubleArray, clusterCenters[j]);
                            jNew += (float) (Math.pow(u, m) * Math.pow(cosineDistance, 2));
                            //jNew += (float) (pow(u, m) * pow(cosineDistance, 2));
                        }
                    }
                }

                if (iteration == 0) {
                    lineNumbers = new int[lineAL.size()];
                }

                difference = (float) Math.abs(jNew - jOld);
                System.out.println("Iteration, jOld, jNew, difference: " + iteration + "..." + jOld + "..." + jNew + "..." + difference + "..." + Calendar.getInstance().getTime());
                //if ((iteration > 15 && difference <= 1 || (iteration <= 15 && difference <= 0.001)) && jNew != 0) {
                if (iteration != 0) {
                    index = lineAL.size();
                    file = new File("fuzzyMemberships" + k + "_" + m + ".txt");
                    if (file.exists()) {
                        System.out.println("fuzzyMemberships file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter("fuzzyMemberships" + k + "_" + m + ".txt", false), true);
                    for (i = 0; i < index; i++) {
                        pw.print((lineNumbers[i]) + "\t");
                        for (j = 0; j < k - 1; j++) {
                            pw.print(uNewArray[i][j] + "\t");
                        }
                        pw.println(uNewArray[i][j]);
                    }
                    pw.close();
                    file = new File("fuzzyClusters" + k + "_" + m + ".txt");
                    if (file.exists()) {
                        System.out.println("fuzzyClusters file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter("fuzzyClusters" + k + "_" + m + ".txt", false), true);
                    for (j = 0; j < k; j++) {
                        for (index = 0; index < vectorSize - 1; index++) {
                            pw.print(clusterCenters[j][index] + "\t");
                        }
                        pw.println(clusterCenters[j][index]);
                    }
                    pw.close();
                    if ((iteration > 15 && difference <= 1 || (iteration <= 15 && difference <= 0.001)) && jNew != 0) {
                        System.out.println("FCM END");
                        break;
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time J: " + (endMs - startMs));

                // Calculate J - END

                // Calculate Random u - START

                if (iteration == 0) {
                    uNewArray = new float[lineAL.size()][k];
                    index = lineAL.size();
                    for (i = 0; i < index; i++) {
                        uRandomTotal = 0;
                        for (j = 0; j < k; j++) {
                            uNewArray[i][j] = random.nextFloat();
                            uRandomTotal += uNewArray[i][j];
                        }
                        for (j = 0; j < k; j++) {
                            uNewArray[i][j] = uNewArray[i][j] / uRandomTotal;
                        }
                    }
                }

                // Calculate Random u - END

                // Calculate C - START
                startMs = System.currentTimeMillis();

                clusterCenters = null;
                System.gc();
                clusterCenters = new float[k][vectorSize];
                uTotal = new float[k];

                for (i = 0, lineNumber = 0; i < lineAL.size(); i++) {
                    line = (String) lineAL.get(i);

                    if (lineNumber++ % 10000 == 0) {
                        System.out.println("lineNumber: " + lineNumber + "..." + Calendar.getInstance().getTime());
                    }

                    /*System.out.println();
                    System.out.println("line: " + line);
                    System.out.println();
                    System.out.print("i = " + i + ", index = ");*/
                    
                    st = new StringTokenizer(line);
                    if (iteration == 0) {
                        lineNumbers[i] = Integer.parseInt(st.nextToken());
                    } else {
                        st.nextToken();
                    }
                    //st.nextToken();
                    for (index = 0; index < vectorSize; index++) {
                    //for (index = 0; st.hasMoreTokens(); index++) {
                        //System.out.print(index + ", ");
                        x = Float.parseFloat(st.nextToken());
                        for (j = 0; j < k; j++) {
                            //if (iteration != 0) {
                            u = uNewArray[i][j];
                            //}
                            uTotal[j] += (float) Math.pow(u, m);
                            //uTotal[j] += (float) pow(u, m);

                            if (i == 0) {
                                clusterCenters[j][index] = (float) (Math.pow(u, m) * x);
                                //clusterCenters[j][index] = (float) (pow(u, m) * x);
                            } else {
                                clusterCenters[j][index] += (float) (Math.pow(u, m) * x);
                                //clusterCenters[j][index] += (float) (pow(u, m) * x);
                            }
                        }
                    }
                }

                for (index = 0; index < vectorSize; index++) {
                    for (j = 0; j < k; j++) {
                        clusterCenters[j][index] = clusterCenters[j][index] / uTotal[j];
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time C: " + (endMs - startMs));

                // Calculate C - END
                // Calculate u - START

                startMs = System.currentTimeMillis();
                uNewArray = null;
                System.gc();
                uNewArray = new float[i][k];

                for (i = 0, lineNumber = 0; i < lineAL.size(); i++) {

                    if (lineNumber++ % 10000 == 0) {
                        System.out.println("lineNumber: " + lineNumber + "..." + Calendar.getInstance().getTime());
                    }
                    //startMs1 = System.currentTimeMillis();
                    //startMs2 = System.currentTimeMillis();
                    line = (String) lineAL.get(i);
                    st = new StringTokenizer(line);
                    st.nextToken();
                    //st.nextToken();

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; index < vectorSize /*st.hasMoreTokens()*/; index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u11: " + (endMs1 - startMs1));
                    //startMs1 = System.currentTimeMillis();

                    tempDoubleArray = new float[k];
                    for (j = 0; j < k; j++) {
                        tempDoubleArray[j] = CosineSimilarityARM.distance(tempDoubleArray1, clusterCenters[j]);
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u12: " + (endMs1 - startMs1));
                    //startMs1 = System.currentTimeMillis();

                    for (j = 0; j < k; j++) {
                        for (j1 = 0; j1 < k; j1++) {
                            uNewArray[i][j] += Math.pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2 / (m - 1)));
                            //uNewArray[i][j] += pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2/(m-1)));
                        }
                        uNewArray[i][j] = (float) 1 / uNewArray[i][j];
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u13: " + (endMs1 - startMs1));
                    //endMs2 = System.currentTimeMillis();
                    //System.out.println("Run Time u2: " + (endMs2 - startMs2));
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time u: " + (endMs - startMs));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void generateFuzzyClustersFloatInMemoryOneDimensional(String fileName, int k, float m, int columnNumber, String columnName, float lowerThreshold, float higherThreshold, boolean isHeaderRequired) {
        BufferedReader bufReader = null;
        float jNew = 0, jOld, difference;
        float u = (float) 1 / (float) k;
        float uNewArray[][] = null;
        int iteration, index, i, j, j1;
        int vectorSize = 1;
        String line = null, token = null;
        StringTokenizer st = null;
        ////DenseVector dv = null, dv1 = null;
        float clusterCenters[][] = null, tempDoubleArray[] = null, uTotal[] = null, tempDoubleArray1[] = null;
        float cosineDistance, x, datumValue;
        float uRandomTotal;
        Random random = new Random();
        PrintWriter pw = null;
        File file = null;
        ArrayList lineAL = new ArrayList();
        long startMs, endMs, startMs1, endMs1, startMs2, endMs2;

        try {
            iteration = -1;

            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",\t");
                for(index = 0; index < columnNumber; index++) {
                    token = st.nextToken();
                }
                //token = new String(token);
                if (token.equals("?")) {
                    continue;
                }

                datumValue = Float.parseFloat(token);
                if (datumValue >= lowerThreshold && datumValue <= higherThreshold) {
                    //if (!lineAL.contains(token)) {
                        lineAL.add(new String(token));
                    //}
                }
            }
            bufReader.close();

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
                        //st.nextToken();
                        //st.nextToken();
                        tempDoubleArray = new float[vectorSize];

                        for (index = 0; st.hasMoreTokens(); index++) {
                            tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                        }

                        for (j = 0; j < k; j++) {
                            u = uNewArray[i][j];
                            cosineDistance = EuclideanDistanceARM.distance(tempDoubleArray, clusterCenters[j]);
                            //System.out.println("cosineDistance: " + cosineDistance);
                            jNew += (float) (Math.pow(u, m) * Math.pow(cosineDistance, 2));
                            //jNew += (float) (pow(u, m) * pow(cosineDistance, 2));
                        }
                    }
                }

                /*if (iteration == 0) {
                    lineNumbers = new int[lineAL.size()];
                }*/

                difference = (float) Math.abs(jNew - jOld);
                System.out.println("Iteration, jOld, jNew, difference: " + iteration + "..." + jOld + "..." + jNew + "..." + difference + "..." + Calendar.getInstance().getTime());
                //if ((iteration > 15 && difference <= 1 || (iteration <= 15 && difference <= 0.001)) && jNew != 0) {
                if (iteration != 0) {
                    index = lineAL.size();
                    //file = new File("fuzzyMemberships" + k + "_" + m + ".txt");
                    file = new File(columnName + ".csv");
                    if (file.exists()) {
                        System.out.println("fuzzyMemberships file.delete(): " + file.delete());
                    }
                    file = null;
                    //pw = new PrintWriter(new FileWriter("fuzzyMemberships" + k + "_" + m + ".txt", false), true);
                    pw = new PrintWriter(new FileWriter(columnName + ".csv", false), true);
                    if(isHeaderRequired) {
                        pw.print(columnName);
                        for(j = 0; j < k; j++) {
                            pw.print("," + j);
                        }
                        pw.println();
                    }
                    for (i = 0; i < index; i++) {
                        pw.print(Float.parseFloat((String) lineAL.get(i)) + ",");
                        for (j = 0; j < k - 1; j++) {
                            pw.print(uNewArray[i][j] + ",");
                        }
                        pw.println(uNewArray[i][j]);
                    }
                    pw.close();
                    //file = new File("fuzzyClusters" + k + "_" + m + ".txt");
                    file = new File(columnName + "_fuzzyClusters.csv");
                    if (file.exists()) {
                        System.out.println("fuzzyClusters file.delete(): " + file.delete());
                    }
                    file = null;
                    pw = new PrintWriter(new FileWriter(columnName + "_fuzzyClusters.csv", false), true);
                    for (j = 0; j < k; j++) {
                        for (index = 0; index < vectorSize - 1; index++) {
                            pw.print(clusterCenters[j][index] + ",");
                        }
                        pw.println(clusterCenters[j][index]);
                    }
                    pw.close();
                    if (((iteration > 15 && difference <= 0.0000001) || (iteration <= 15 && difference <= 0.0000001) || (iteration > 100 && difference <= 0.1) || (Math.abs(jNew) <= 0.1)) && jNew != 0) {
                        if (iteration > 1000) {
                            System.out.println("FCM END with iteration > 1000");
                        }
                        System.out.println("FCM END");
                        break;
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time J: " + (endMs - startMs));

                // Calculate J - END

                // Calculate Random u - START

                if (iteration == 0) {
                    uNewArray = new float[lineAL.size()][k];
                    index = lineAL.size();
                    for (i = 0; i < index; i++) {
                        uRandomTotal = 0;
                        for (j = 0; j < k; j++) {
                            uNewArray[i][j] = random.nextFloat();
                            uRandomTotal += uNewArray[i][j];
                        }
                        for (j = 0; j < k; j++) {
                            uNewArray[i][j] = uNewArray[i][j] / uRandomTotal;
                        }
                    }
                }

                // Calculate Random u - END

                // Calculate C - START
                startMs = System.currentTimeMillis();

                clusterCenters = null;
                System.gc();
                clusterCenters = new float[k][vectorSize];
                uTotal = new float[k];

                for (i = 0; i < lineAL.size(); i++) {
                    line = (String) lineAL.get(i);

                    /*System.out.println();
                    System.out.println("line: " + line);
                    System.out.println();
                    System.out.print("i = " + i + ", index = ");*/

                    st = new StringTokenizer(line);
                    /*if (iteration == 0) {
                        lineNumbers[i] = Integer.parseInt(st.nextToken());
                    } else {
                        st.nextToken();
                    }
                    st.nextToken();*/
                    for (index = 0; index < vectorSize; index++) {
                    //for (index = 0; st.hasMoreTokens(); index++) {
                        //System.out.print(index + ", ");
                        x = Float.parseFloat(st.nextToken());
                        for (j = 0; j < k; j++) {
                            //if (iteration != 0) {
                            u = uNewArray[i][j];
                            //}
                            uTotal[j] += (float) Math.pow(u, m);
                            //uTotal[j] += (float) pow(u, m);

                            if (i == 0) {
                                clusterCenters[j][index] = (float) (Math.pow(u, m) * x);
                                //clusterCenters[j][index] = (float) (pow(u, m) * x);
                            } else {
                                clusterCenters[j][index] += (float) (Math.pow(u, m) * x);
                                //clusterCenters[j][index] += (float) (pow(u, m) * x);
                            }
                        }
                    }
                }

                for (index = 0; index < vectorSize; index++) {
                    for (j = 0; j < k; j++) {
                        clusterCenters[j][index] = clusterCenters[j][index] / uTotal[j];
                    }
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time C: " + (endMs - startMs));

                // Calculate C - END
                // Calculate u - START

                startMs = System.currentTimeMillis();
                uNewArray = null;
                System.gc();
                uNewArray = new float[i][k];

                for (i = 0; i < lineAL.size(); i++) {
                    //startMs1 = System.currentTimeMillis();
                    //startMs2 = System.currentTimeMillis();
                    line = (String) lineAL.get(i);
                    st = new StringTokenizer(line);
                    //st.nextToken();
                    //st.nextToken();

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; index < vectorSize /*st.hasMoreTokens()*/; index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u11: " + (endMs1 - startMs1));
                    //startMs1 = System.currentTimeMillis();

                    tempDoubleArray = new float[k];
                    for (j = 0; j < k; j++) {
                        tempDoubleArray[j] = EuclideanDistanceARM.distance(tempDoubleArray1, clusterCenters[j]);
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u12: " + (endMs1 - startMs1));
                    //startMs1 = System.currentTimeMillis();

                    for (j = 0; j < k; j++) {
                        for (j1 = 0; j1 < k; j1++) {
                            uNewArray[i][j] += Math.pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2 / (m - 1)));
                            //uNewArray[i][j] += pow((tempDoubleArray[j] / tempDoubleArray[j1]), (2/(m-1)));
                        }
                        uNewArray[i][j] = (float) 1 / uNewArray[i][j];
                    }
                    //endMs1 = System.currentTimeMillis();
                    //System.out.println("Run Time u13: " + (endMs1 - startMs1));
                    //endMs2 = System.currentTimeMillis();
                    //System.out.println("Run Time u2: " + (endMs2 - startMs2));
                }
                endMs = System.currentTimeMillis();
                System.out.println("Run Time u: " + (endMs - startMs));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void generateKMeansClustersFloatInMemory(String fileName, int k, int vectorSize) {
        BufferedReader bufReader = null;
        float jNew = 0, jOld, difference;
        int clusterNumber[] = null;
        int iteration, index, i, j, lineNumbers[] = null, minClusterNumber = -9999, clusterCenterTotal[] = null, randomNumber;
        String line = null;
        StringTokenizer st = null;
        float clusterCenters[][] = null, tempDoubleArray[] = null, tempDoubleArray1[] = null;
        float cosineDistance, x, minCosineDistance;
        PrintWriter pw = null;
        File file = null;
        ArrayList lineAL = new ArrayList(), randomNumberAL = new ArrayList();
        long startMs, endMs;
        Random random = new Random();

        try {
            iteration = -1;
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                lineAL.add(line);
            }
            bufReader.close();

            clusterNumber = new int[lineAL.size()];
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
                        //st.nextToken();
                        st.nextToken();
                        tempDoubleArray = new float[vectorSize];

                        for (index = 0; st.hasMoreTokens(); index++) {
                            tempDoubleArray[index] = Float.parseFloat(st.nextToken());
                        }

                        for (j = 0; j < k; j++) {
                            cosineDistance = CosineSimilarityARM.distance(tempDoubleArray, clusterCenters[j]);
                            jNew += cosineDistance;
                        }
                    }
                }

                if (iteration == 0) {
                    lineNumbers = new int[lineAL.size()];
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
                        pw.println((lineNumbers[i]) + "\t" + clusterNumber[i]);
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
                        //st.nextToken();
                        st.nextToken();

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
                    //st.nextToken();
                    st.nextToken();

                    tempDoubleArray1 = new float[vectorSize];
                    for (index = 0; st.hasMoreTokens(); index++) {
                        tempDoubleArray1[index] = Float.parseFloat(st.nextToken());
                    }

                    minCosineDistance = 1;
                    tempDoubleArray = new float[k];
                    for (j = 0; j < k; j++) {
                        cosineDistance = CosineSimilarityARM.distance(tempDoubleArray1, clusterCenters[j]);
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
                    if (iteration == 0) {
                        lineNumbers[i] = Integer.parseInt(st.nextToken());
                    } else {
                        st.nextToken();
                    }
                    //st.nextToken();

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
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FCM fcm = new FCM();
        ////fcm.generateFuzzyClusters(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]));
        //fcm.generateFuzzyClustersFloat(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Float.parseFloat(args[3]));
        //fcm.generateFuzzyClustersFloatInMemory(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Float.parseFloat(args[3]));
        //fcm.generateFuzzyClustersFloatInMemoryOneDimensional(args[0], Integer.parseInt(args[1]), Float.parseFloat(args[2]), Integer.parseInt(args[3]), args[4], Float.parseFloat(args[5]), Float.parseFloat(args[6]), false);
        fcm.generateKMeansClustersFloatInMemory(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
}
