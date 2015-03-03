
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

public class FACME2 {

    private String decisionClassLabel = null;
    private ArrayList decisionClassesAL = new ArrayList(),  itemsetsAL = new ArrayList();
    private HashMap observedDecisionClassItemsetMap = new HashMap(),  decisionClassCountMap = new HashMap();
    private boolean isIsolatedInitialize = false;
    private boolean isReadFromIsolatedInitializeFile = false;
    private boolean isBigDecimal = false;
   
    private HashMap z = new HashMap();
    private HashMap s = new HashMap();
    private HashMap lambda = new HashMap();
    
    public void setIsBigDecimal(boolean isBigDecimal) {
        this.isBigDecimal = isBigDecimal;
    }

    public void setIsReadFromIsolatedInitializeFile(boolean isReadFromIsolatedInitializeFile) {
        this.isReadFromIsolatedInitializeFile = isReadFromIsolatedInitializeFile;
    }

    public void setIsIsolatedInitialize(boolean isIsolatedInitialize) {
        this.isIsolatedInitialize = isIsolatedInitialize;
    }

    public void trainFACME2(int fileSerialNumber) {
        System.out.println("initialize START......." + Calendar.getInstance().getTime());

        if (!isReadFromIsolatedInitializeFile) {
            initialize(fileSerialNumber);
        } else {
            readFromIsolatedInitializeFile(fileSerialNumber);
        }

        System.out.println(observedDecisionClassItemsetMap);

        System.out.println("initialize END......." + Calendar.getInstance().getTime());

        //actualCalculation();
        //actualCalculationGIS();
        //actualCalculationGISNonLogPudi();
        //actualCalculationGISBigDecimalPudi();
        actualCalculationGISLogPudi(fileSerialNumber);

        //System.out.println(lambda);

    }

    public ArrayList testFACME2(int fileSerialNumber) {
        System.out.println("initialize START......." + Calendar.getInstance().getTime());
        ArrayList accuracyAL;

        readFromIsolatedInitializeTestFile(fileSerialNumber);
        //accuracyAL = actualTestCalculation(fileSerialNumber);
        accuracyAL = actualTestCalculationTotal(fileSerialNumber);


        System.out.println(observedDecisionClassItemsetMap);
        System.out.println("initialize END......." + Calendar.getInstance().getTime());
        return accuracyAL;
    }

    private ArrayList actualTestCalculation(int fileSerialNumber) {
        BufferedReader bufReader;
        String line, fileName, actualClass = null, currentClass, itemset, singleton, bestClass = null, subLine;
        StringTokenizer st = null;
        HashMap tempMap = null, lineClassFuzzyMembershipSumMap = null, pointClassFuzzyMembershipSumMap = null, allLineClassFuzzyMembershipSumMap = new HashMap();
        boolean isFirstTime = true, isMatch;
        ArrayList tempAL = null, tempAL1 = null, clashingClasses = new ArrayList(), accuracyAL = new ArrayList();
        int startIndex, endIndex, index, index1, correctCount = 0, totalCount = 0;
        double fuzzyMembership, minFuzzyMembership, totalCurrentClassFuzzyMembershipSum, currentClassFuzzyMembershipSum = 0, maxClassFuzzyMembershipSum, bestClassFuzzyMembership, maxPointClassFuzzyMembershipSum, support, itemsetProbability;

        try {
            fileName = "fuzzyTestData" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            tempAL = new ArrayList(lambda.keySet());

            maxPointClassFuzzyMembershipSum = Double.NEGATIVE_INFINITY;

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("END OF DATA POINT")) {
                    isFirstTime = true;
                    totalCount++;

                    tempAL1 = new ArrayList(pointClassFuzzyMembershipSumMap.keySet());
                    bestClassFuzzyMembership = Double.NEGATIVE_INFINITY;
                    for (index = 0; index < tempAL1.size(); index++) {
                        currentClass = (String) tempAL1.get(index);
                        fuzzyMembership = (Double) pointClassFuzzyMembershipSumMap.get(currentClass);
                        if (fuzzyMembership / maxPointClassFuzzyMembershipSum > bestClassFuzzyMembership) {
                            bestClassFuzzyMembership = fuzzyMembership / maxPointClassFuzzyMembershipSum;
                            bestClass = currentClass;
                            clashingClasses.clear();
                        }
                        else if (fuzzyMembership / maxPointClassFuzzyMembershipSum == bestClassFuzzyMembership) {
                            if(!clashingClasses.contains(bestClass)) {
                                clashingClasses.add(bestClass);
                            }
                            clashingClasses.add(currentClass);
                        }
                    }

                    System.out.println("pointClassFuzzyMembershipSumMap " + pointClassFuzzyMembershipSumMap);

                    System.out.println("actualClass, bestClass, bestClassFuzzyMembership" + "..." + actualClass + "..." + bestClass + "..." + bestClassFuzzyMembership);
                    
                    bestClassFuzzyMembership = 0;
                    if(clashingClasses.size() > 1) {
                        for(index = 0; index < clashingClasses.size(); index++) {
                            currentClass = (String) clashingClasses.get(index);
                            fuzzyMembership = (Double) allLineClassFuzzyMembershipSumMap.get(currentClass);
                            if(bestClassFuzzyMembership < fuzzyMembership) {
                                bestClassFuzzyMembership = fuzzyMembership;
                                bestClass = currentClass;
                            }
                        }
                        
                        System.out.println("CLASH actualClass, bestClass, bestClassFuzzyMembership" + "..." + actualClass + "..." + bestClass + "..." + bestClassFuzzyMembership);
                    }
                    
                    if ((actualClass + "^1").equals(bestClass)) {
                        System.out.println("CORRECT");
                        correctCount++;
                    } else {
                        System.out.println("WRONG");
                    }
                    
                    System.out.println("correctCount, totalCount, (correctCount/totalCount): " + correctCount + "..." + totalCount + "..." + ((float)correctCount/(float)totalCount));
                    System.out.println();
                    
                    allLineClassFuzzyMembershipSumMap.clear();
                    clashingClasses.clear();
                } else {

                    if (isFirstTime) {
                        isFirstTime = false;
                        pointClassFuzzyMembershipSumMap = new HashMap();
                        maxPointClassFuzzyMembershipSum = Double.NEGATIVE_INFINITY;

                        startIndex = line.indexOf(decisionClassLabel);
                        endIndex = line.indexOf('^', startIndex);
                        /*if(endIndex == -1)
                        endIndex = line.length();*/
                        actualClass = line.substring(startIndex, endIndex);

                    }
                    maxClassFuzzyMembershipSum = Double.NEGATIVE_INFINITY;
                    lineClassFuzzyMembershipSumMap = new HashMap();

                    for (index = 0; index < tempAL.size(); index++) {
                        currentClass = (String) tempAL.get(index);
                        tempMap = (HashMap) lambda.get(currentClass);
                        tempAL1 = new ArrayList(tempMap.keySet());
                        currentClassFuzzyMembershipSum = 0;
                        support = (Double) decisionClassCountMap.get(currentClass);

                        for (index1 = 0; index1 < tempAL1.size(); index1++) {
                            itemset = (String) tempAL1.get(index1);
                            itemsetProbability = (Double) tempMap.get(itemset);
                            st = new StringTokenizer(itemset, ",");
                            isMatch = true;
                            minFuzzyMembership = Double.POSITIVE_INFINITY;
                            while (st.hasMoreTokens()) {
                                singleton = st.nextToken();
                                if (!line.contains(singleton)) {
                                    isMatch = false;
                                    break;

                                } else {
                                    startIndex = line.indexOf(singleton);
                                    endIndex = line.indexOf(',', startIndex);
                                    if (endIndex == -1) {
                                        subLine = line.substring(startIndex);
                                    } else {
                                        subLine = line.substring(startIndex, endIndex);
                                    }
                                    startIndex = subLine.indexOf('^');

                                    fuzzyMembership = Double.parseDouble(subLine.substring(startIndex + 1));
                                    if (fuzzyMembership < minFuzzyMembership) {
                                        minFuzzyMembership = fuzzyMembership;
                                    }
                                }
                            }

                            if (isMatch) {
                                fuzzyMembership = exp(itemsetProbability) * minFuzzyMembership * support;
                                currentClassFuzzyMembershipSum += fuzzyMembership;
                                //System.out.println("itemsetProbability, exp(itemsetProbability), minFuzzyMembership, support, fuzzyMembership, currentClassFuzzyMembershipSum: " + itemsetProbability + "..." + exp(itemsetProbability) + "..." + minFuzzyMembership + "..." + support + "..." + fuzzyMembership + "..." + currentClassFuzzyMembershipSum);
                            }
                        }

                        lineClassFuzzyMembershipSumMap.put(currentClass, currentClassFuzzyMembershipSum);
                        
                        if (maxClassFuzzyMembershipSum < currentClassFuzzyMembershipSum) {
                            maxClassFuzzyMembershipSum = currentClassFuzzyMembershipSum;
                        }
                        
                        if(allLineClassFuzzyMembershipSumMap.containsKey(currentClass)) {
                            totalCurrentClassFuzzyMembershipSum = (Double) allLineClassFuzzyMembershipSumMap.get(currentClass);
                            allLineClassFuzzyMembershipSumMap.remove(currentClass);
                        }
                        else {
                            totalCurrentClassFuzzyMembershipSum = 0;
                        }
                        totalCurrentClassFuzzyMembershipSum += currentClassFuzzyMembershipSum;
                        allLineClassFuzzyMembershipSumMap.put(currentClass, totalCurrentClassFuzzyMembershipSum);
                        
                        System.out.println("currentClass, currentClassFuzzyMembershipSum " + currentClass + "..." + currentClassFuzzyMembershipSum);
                    }

                    //for line select best class
                    tempAL1 = new ArrayList(lineClassFuzzyMembershipSumMap.keySet());
                    bestClassFuzzyMembership = Double.NEGATIVE_INFINITY;
                    System.out.println();
                    for (index = 0; index < tempAL1.size(); index++) {
                        currentClass = (String) tempAL1.get(index);
                        fuzzyMembership = (Double) lineClassFuzzyMembershipSumMap.get(currentClass);
                        /*support = (Double) decisionClassCountMap.get(currentClass);
                        fuzzyMembership = exp(fuzzyMembership) * support;*/

                        //System.out.println("currentClass, fuzzyMembership " + currentClass + "..." + fuzzyMembership);

                        if (fuzzyMembership / maxClassFuzzyMembershipSum > bestClassFuzzyMembership) {
                            bestClassFuzzyMembership = fuzzyMembership / maxClassFuzzyMembershipSum;
                            bestClass = currentClass;
                        }
                    }
                    System.out.println();
                    //lineClassFuzzyMembershipSumMap.clear();
                    if (pointClassFuzzyMembershipSumMap.containsKey(bestClass)) {
                        fuzzyMembership = (Double) pointClassFuzzyMembershipSumMap.get(bestClass);
                        bestClassFuzzyMembership += fuzzyMembership;
                        pointClassFuzzyMembershipSumMap.remove(bestClass);
                        pointClassFuzzyMembershipSumMap.put(bestClass, bestClassFuzzyMembership);

                    } else {
                        pointClassFuzzyMembershipSumMap.put(bestClass, bestClassFuzzyMembership);
                    }

                    if (maxPointClassFuzzyMembershipSum < bestClassFuzzyMembership) {
                        maxPointClassFuzzyMembershipSum = bestClassFuzzyMembership;
                    }
                    System.out.println("bestClass, bestClassFuzzyMembership " + bestClass + "..." + bestClassFuzzyMembership);
                }

            }
            bufReader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        accuracyAL.add(correctCount);
        accuracyAL.add(totalCount);
        return accuracyAL;

    }

    private ArrayList actualTestCalculationTotal(int fileSerialNumber) {
        BufferedReader bufReader;
        String line, fileName, actualClass = null, currentClass, itemset, singleton, bestClass = null, subLine;
        StringTokenizer st = null;
        HashMap tempMap = null, lineClassFuzzyMembershipSumMap = null, pointClassFuzzyMembershipSumMap = null, allLineClassFuzzyMembershipSumMap = new HashMap();
        boolean isFirstTime = true, isMatch;
        ArrayList tempAL = null, tempAL1 = null, clashingClasses = new ArrayList(), accuracyAL = new ArrayList();
        int startIndex, endIndex, index, index1, correctCount = 0, totalCount = 0;
        double fuzzyMembership, minFuzzyMembership, totalCurrentClassFuzzyMembershipSum, currentClassFuzzyMembershipSum = 0, maxClassFuzzyMembershipSum, bestClassFuzzyMembership, maxPointClassFuzzyMembershipSum, support, itemsetProbability;

        try {
            fileName = "fuzzyTestData" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            tempAL = new ArrayList(lambda.keySet());

            maxPointClassFuzzyMembershipSum = Double.NEGATIVE_INFINITY;

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("END OF DATA POINT")) {
                    isFirstTime = true;
                    totalCount++;

                    tempAL1 = new ArrayList(pointClassFuzzyMembershipSumMap.keySet());
                    bestClassFuzzyMembership = Double.NEGATIVE_INFINITY;
                    for (index = 0; index < tempAL1.size(); index++) {
                        currentClass = (String) tempAL1.get(index);
                        fuzzyMembership = (Double) allLineClassFuzzyMembershipSumMap.get(currentClass);
                        if (fuzzyMembership > bestClassFuzzyMembership) {
                            bestClassFuzzyMembership = fuzzyMembership;
                            bestClass = currentClass;
                            clashingClasses.clear();
                        }
                        else if (fuzzyMembership == bestClassFuzzyMembership) {
                            if(!clashingClasses.contains(bestClass)) {
                                clashingClasses.add(bestClass);
                            }
                            clashingClasses.add(currentClass);
                        }
                    }

                    System.out.println("pointClassFuzzyMembershipSumMap " + pointClassFuzzyMembershipSumMap);
                    
                    System.out.println("allLineClassFuzzyMembershipSumMap " + allLineClassFuzzyMembershipSumMap);

                    System.out.println("actualClass, bestClass, bestClassFuzzyMembership" + "..." + actualClass + "..." + bestClass + "..." + bestClassFuzzyMembership);
                    
                    bestClassFuzzyMembership = 0;
                    if(clashingClasses.size() > 1) {
                        for(index = 0; index < clashingClasses.size(); index++) {
                            currentClass = (String) clashingClasses.get(index);
                            fuzzyMembership = (Double) allLineClassFuzzyMembershipSumMap.get(currentClass);
                            if(bestClassFuzzyMembership < fuzzyMembership) {
                                bestClassFuzzyMembership = fuzzyMembership;
                                bestClass = currentClass;
                            }
                        }
                        
                        System.out.println("CLASH actualClass, bestClass, bestClassFuzzyMembership" + "..." + actualClass + "..." + bestClass + "..." + bestClassFuzzyMembership);
                    }
                    
                    if ((actualClass + "^1").equals(bestClass)) {
                        System.out.println("CORRECT");
                        correctCount++;
                    } else {
                        System.out.println("WRONG");
                    }
                    
                    System.out.println("correctCount, totalCount, (correctCount/totalCount): " + correctCount + "..." + totalCount + "..." + ((float)correctCount/(float)totalCount));
                    System.out.println();
                    
                    allLineClassFuzzyMembershipSumMap.clear();
                    clashingClasses.clear();
                } else {

                    if (isFirstTime) {
                        isFirstTime = false;
                        pointClassFuzzyMembershipSumMap = new HashMap();
                        maxPointClassFuzzyMembershipSum = Double.NEGATIVE_INFINITY;

                        startIndex = line.indexOf(decisionClassLabel);
                        endIndex = line.indexOf('^', startIndex);
                        /*if(endIndex == -1)
                        endIndex = line.length();*/
                        actualClass = line.substring(startIndex, endIndex);

                    }
                    maxClassFuzzyMembershipSum = Double.NEGATIVE_INFINITY;
                    lineClassFuzzyMembershipSumMap = new HashMap();

                    for (index = 0; index < tempAL.size(); index++) {
                        currentClass = (String) tempAL.get(index);
                        tempMap = (HashMap) lambda.get(currentClass);
                        tempAL1 = new ArrayList(tempMap.keySet());
                        currentClassFuzzyMembershipSum = 0;
                        support = (Double) decisionClassCountMap.get(currentClass);

                        for (index1 = 0; index1 < tempAL1.size(); index1++) {
                            itemset = (String) tempAL1.get(index1);
                            itemsetProbability = (Double) tempMap.get(itemset);
                            st = new StringTokenizer(itemset, ",");
                            isMatch = true;
                            minFuzzyMembership = Double.POSITIVE_INFINITY;
                            while (st.hasMoreTokens()) {
                                singleton = st.nextToken();
                                if (!line.contains(singleton)) {
                                    isMatch = false;
                                    break;

                                } else {
                                    startIndex = line.indexOf(singleton);
                                    endIndex = line.indexOf(',', startIndex);
                                    if (endIndex == -1) {
                                        subLine = line.substring(startIndex);
                                    } else {
                                        subLine = line.substring(startIndex, endIndex);
                                    }
                                    startIndex = subLine.indexOf('^');

                                    fuzzyMembership = Double.parseDouble(subLine.substring(startIndex + 1));
                                    if (fuzzyMembership < minFuzzyMembership) {
                                        minFuzzyMembership = fuzzyMembership;
                                    }
                                }
                            }

                            if (isMatch) {
                                fuzzyMembership = exp(itemsetProbability) * minFuzzyMembership * support;
                                currentClassFuzzyMembershipSum += fuzzyMembership;
                                //System.out.println("itemsetProbability, exp(itemsetProbability), minFuzzyMembership, support, fuzzyMembership, currentClassFuzzyMembershipSum: " + itemsetProbability + "..." + exp(itemsetProbability) + "..." + minFuzzyMembership + "..." + support + "..." + fuzzyMembership + "..." + currentClassFuzzyMembershipSum);
                            }
                        }

                        lineClassFuzzyMembershipSumMap.put(currentClass, currentClassFuzzyMembershipSum);
                        
                        if (maxClassFuzzyMembershipSum < currentClassFuzzyMembershipSum) {
                            maxClassFuzzyMembershipSum = currentClassFuzzyMembershipSum;
                        }
                        
                        if(allLineClassFuzzyMembershipSumMap.containsKey(currentClass)) {
                            totalCurrentClassFuzzyMembershipSum = (Double) allLineClassFuzzyMembershipSumMap.get(currentClass);
                            allLineClassFuzzyMembershipSumMap.remove(currentClass);
                        }
                        else {
                            totalCurrentClassFuzzyMembershipSum = 0;
                        }
                        totalCurrentClassFuzzyMembershipSum += currentClassFuzzyMembershipSum;
                        allLineClassFuzzyMembershipSumMap.put(currentClass, totalCurrentClassFuzzyMembershipSum);
                        
                        System.out.println("currentClass, currentClassFuzzyMembershipSum " + currentClass + "..." + currentClassFuzzyMembershipSum);
                    }

                    //for line select best class
                    tempAL1 = new ArrayList(lineClassFuzzyMembershipSumMap.keySet());
                    bestClassFuzzyMembership = Double.NEGATIVE_INFINITY;
                    System.out.println();
                    for (index = 0; index < tempAL1.size(); index++) {
                        currentClass = (String) tempAL1.get(index);
                        fuzzyMembership = (Double) lineClassFuzzyMembershipSumMap.get(currentClass);
                        

                        if (pointClassFuzzyMembershipSumMap.containsKey(currentClass)) {
                            fuzzyMembership = (Double) pointClassFuzzyMembershipSumMap.get(currentClass);
                            bestClassFuzzyMembership += fuzzyMembership;
                            pointClassFuzzyMembershipSumMap.remove(currentClass);
                            pointClassFuzzyMembershipSumMap.put(currentClass, bestClassFuzzyMembership);

                        } else {
                            pointClassFuzzyMembershipSumMap.put(currentClass, bestClassFuzzyMembership);
                        }
                    }
                    
                }

            }
            bufReader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        accuracyAL.add(correctCount);
        accuracyAL.add(totalCount);
        return accuracyAL;

    }
    private void readFromIsolatedInitializeTestFile(int fileSerialNumber) {
        BufferedReader bufReader;
        String line, fileName, decisionClass = null, tempStr, tempStr1;
        StringTokenizer st = null;
        HashMap tempMap = null;
        boolean isNewClass = true;
        int index;

        try {
            fileName = "trainedClassifier" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            decisionClassLabel = bufReader.readLine();

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("END OF CLASS")) {
                    isNewClass = true;
                    if (tempMap != null && !tempMap.isEmpty()) {
                        lambda.put(decisionClass, tempMap);
                    }
                    continue;
                } else if (isNewClass) {
                    isNewClass = false;
                    tempMap = new HashMap();
                    st = new StringTokenizer(line, "@@@@@");
                    decisionClass = st.nextToken();
                    decisionClassCountMap.put(decisionClass, (Double.parseDouble(st.nextToken())));// / 480));
                    continue;
                }
                index = line.indexOf("-->>>");
                
                tempStr = line.substring(0, index);
                tempStr1 = line.substring(index + 5);
                
                /*System.out.println("line: " + line);
                System.out.println("tempStr: " + tempStr);
                System.out.println("tempStr1: " + tempStr1);*/
                
                tempMap.put(tempStr, Double.parseDouble(tempStr1));
            }
            bufReader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    private void actualCalculation() {
        int index, index1, index2 = 0, trainingDataSize = 0, infiniteIndex, startIndex, endIndex;
        String fileName = "fuzzyTrainingData.txt", line = null, subLine = null, itemset = null, decisionClass = null, token = null;
        BufferedReader bufReader;
        StringTokenizer st = null, st1 = null;
        double minFuzzyMembership, maxFuzzyMembership, fuzzyMembership, tempZ, tempS, tempLambda = 0, expected = 0, tempDelta = 0, observed, tempDouble, classCount, minClassCountLine;
        ArrayList tempItemsetAL = new ArrayList(), tempTrainingDataMatchAL = null, tempStopProcessingItemsetAL = null, tempTempStopProcessingItemsetAL = null, doneCountAL = new ArrayList(), decisionClassisCountedAL = new ArrayList(), trainingData = new ArrayList();
        boolean isMatch;
        HashMap tempMap = null, tempMap1 = null, tempObservedMap = null, delta = null, trainingDataMatchMap = null, stopProcessingDecisionClassesItemsetMap = new HashMap(), noFSAcessMap = new HashMap(), expectedClassMap = new HashMap(), tempExpectedMap = null;
        PrintWriter pw = null;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));

            while (true) {
                if (index2 % 10000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }

                line = bufReader.readLine();
                if (line == null) {

                    break;
                }
                trainingData.add(line);
                index2++;
            }
            bufReader.close();

            for (infiniteIndex = 0; doneCountAL.size() < (itemsetsAL.size() * decisionClassesAL.size()); infiniteIndex++) {

                System.out.println("\n\ninfiniteIndex, doneCountAL.size(), itemsetsAL.size() * decisionClassesAL.size() : " + infiniteIndex + "..." + doneCountAL.size() + "..." + (itemsetsAL.size() * decisionClassesAL.size()));

                //s.clear();
                //z.clear();

                for (index = 0; index < itemsetsAL.size(); index++) {

                    ////System.out.println("itemsetsAL START..................................." + index + "..." + Calendar.getInstance().getTime());

                    itemset = (String) itemsetsAL.get(index);

                    //expected = 0;

                    delta = new HashMap();
                    trainingDataMatchMap = new HashMap();
                    tempTempStopProcessingItemsetAL = new ArrayList();


                    for (index1 = 0  , expected = 0; index1 < decisionClassesAL.size(); index1++, expected = 0) {
                        ////System.out.println("decisionClassesAL START......." + index1 + "..." + Calendar.getInstance().getTime());

                        decisionClass = (String) decisionClassesAL.get(index1);

                        if (stopProcessingDecisionClassesItemsetMap.containsKey(decisionClass)) {
                            tempStopProcessingItemsetAL = (ArrayList) stopProcessingDecisionClassesItemsetMap.get(decisionClass);
                        } else {
                            tempStopProcessingItemsetAL = new ArrayList();
                            stopProcessingDecisionClassesItemsetMap.put(decisionClass, tempStopProcessingItemsetAL);
                        }

                        if (tempStopProcessingItemsetAL.contains(index)) {
                            ////System.out.println("Already DONE: " + index + "$" + index1);
                            continue;
                        }

                        if (s.containsKey(decisionClass)) {
                            tempMap = (HashMap) s.get(decisionClass);
                        } else {
                            tempMap = new HashMap();
                            s.put(decisionClass, tempMap);
                        }

                        if (trainingDataMatchMap.containsKey(decisionClass)) {
                            tempTrainingDataMatchAL = (ArrayList) trainingDataMatchMap.get(decisionClass);
                        } else {
                            tempTrainingDataMatchAL = new ArrayList();
                            trainingDataMatchMap.put(decisionClass, tempTrainingDataMatchAL);
                        }
                        tempObservedMap = (HashMap) observedDecisionClassItemsetMap.get(decisionClass);

                        if (tempObservedMap.containsKey(index)) {
                            observed = (Double) tempObservedMap.get(index);
                        } else {
                            if (!doneCountAL.contains(index + "$" + index1)) {
                                doneCountAL.add(index + "$" + index1);
                            }
                            tempStopProcessingItemsetAL.add(index);
                            tempTempStopProcessingItemsetAL.add(index1);
                            System.out.println("ERROR... No observed found for decisionClass, index : " + decisionClass + "..." + index);
                            continue;
                        }
                        if (observed <= 0) {
                            if (!doneCountAL.contains(index + "$" + index1)) {
                                doneCountAL.add(index + "$" + index1);
                            }
                            tempStopProcessingItemsetAL.add(index);
                            tempTempStopProcessingItemsetAL.add(index1);
                            System.out.println("ERROR... Observed found is <= 0 for decisionClass, index : " + decisionClass + "..." + index);
                            continue;
                        }

                        maxFuzzyMembership = 0;
                        ////bufReader = new BufferedReader(new FileReader(fileName));

                        ////for (index2 = 0    , classCount = 0; true; index2++) {
                        for (index2 = 0      , classCount = 0; index2 <= trainingData.size(); index2++) {
                            if (index2 % 100000 == 0) {
                                ////System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                            }

                            /******line = bufReader.readLine();
                            if (line == null) {
                            trainingDataSize = index2;
                            if (!decisionClassCountMap.containsKey(decisionClass)) {
                            decisionClassCountMap.put(decisionClass, classCount);
                            }
                            break;
                            }******/
                            if (index2 == trainingData.size()) {
                                trainingDataSize = index2;
                                if (!decisionClassCountMap.containsKey(decisionClass)) {
                                    decisionClassCountMap.put(decisionClass, classCount);
                                }
                                break;
                            }
                            line = (String) trainingData.get(index2);

                            /****if (!line.contains(decisionClass)) {
                            continue;
                            } else ****/
                            if (!decisionClassCountMap.containsKey(decisionClass)) {

                                minClassCountLine = Double.POSITIVE_INFINITY;

                                for (subLine = line      , startIndex = 0; true;) {
                                    startIndex = subLine.indexOf('^', startIndex);
                                    endIndex = subLine.indexOf(',', startIndex);
                                    if (endIndex == -1) {
                                        tempDouble = Double.parseDouble(subLine.substring(startIndex + 1));
                                        if (tempDouble < minClassCountLine) {
                                            minClassCountLine = tempDouble;
                                        }
                                        break;
                                    } else {
                                        tempDouble = Double.parseDouble(subLine.substring(startIndex + 1, endIndex));

                                        if (tempDouble < minClassCountLine) {
                                            minClassCountLine = tempDouble;
                                        }
                                        subLine = subLine.substring(startIndex + 1);
                                    }
                                }
                                classCount += minClassCountLine;
                            }

                            if (z.containsKey(index2)) {
                                tempZ = (Double) z.get(index2);
                            } else {
                                tempZ = decisionClassesAL.size();
                                z.put(index2, tempZ);
                            }

                            if (tempMap.containsKey(index2)) {
                                tempS = (Double) tempMap.get(index2);
                            } else {
                                tempS = 0;
                                tempMap.put(index2, tempS);
                            }

                            st = new StringTokenizer(line, ",");
                            minFuzzyMembership = 0;
                            tempItemsetAL = new ArrayList();
                            while (st.hasMoreTokens()) {
                                subLine = st.nextToken();
                                if (!subLine.contains(decisionClass)) {
                                    st1 = new StringTokenizer(subLine, "^");
                                    tempItemsetAL.add(st1.nextToken());
                                    fuzzyMembership = Double.parseDouble(st1.nextToken());
                                    if (minFuzzyMembership == 0 || fuzzyMembership < minFuzzyMembership) {
                                        minFuzzyMembership = fuzzyMembership;
                                    }
                                }
                            }
                            st = new StringTokenizer(itemset, ",");
                            isMatch = true;
                            while (st.hasMoreTokens()) {
                                token = st.nextToken();
                                if (!tempItemsetAL.contains(token)) {
                                    isMatch = false;
                                    break;
                                }
                            }
                            if (isMatch) {
                                tempTrainingDataMatchAL.add(index2);
                                //expected += minFuzzyMembership * Math.exp(tempS) / tempZ;
                                expected += Math.exp(tempS) / tempZ;
                                //expected += exp(tempS) / tempZ;
                                if (Double.isNaN(expected)) {
                                    System.out.println("expected is NaN, index, index1, index2: " + expected + "..." + index + "..." + index1 + "..." + index2);
                                }
                                if (expected <= 0) {
                                    System.out.println("index, index1, index2, tempS, tempZ, expected, previous expected, Math.exp(tempS) / tempZ: " + index + "..." + index1 + "..." + index2 + "..." + tempS + "..." + tempZ + "..." + expected + (expected - (Math.exp(tempS) / tempZ)) + "..." + (Math.exp(tempS) / tempZ));
                                }
                                if (minFuzzyMembership > maxFuzzyMembership) {
                                    maxFuzzyMembership = minFuzzyMembership;
                                }
                            }
                        }
                        ////bufReader.close();

                        if (expected <= 0) {
                            if (!doneCountAL.contains(index + "$" + index1)) {
                                doneCountAL.add(index + "$" + index1);
                            }
                            tempStopProcessingItemsetAL.add(index);
                            tempTempStopProcessingItemsetAL.add(index1);
                            System.out.println("ERROR... Expected is <= 0 for decisionClass: " + decisionClass);
                            continue;
                        }
                        if (lambda.containsKey(decisionClass)) {
                            tempMap1 = (HashMap) lambda.get(decisionClass);
                        } else {
                            tempMap1 = new HashMap();
                            lambda.put(decisionClass, tempMap1);
                        }

                        if (tempMap1.containsKey(index)) {
                            tempLambda = (Double) tempMap1.get(index);
                        } else {
                            tempLambda = 0;
                        }
                        tempDelta = Math.log(observed / expected);
                        if (Double.isNaN(tempDelta)) {
                            System.out.println("tempDelta is NaN, index, index1, index2: " + tempDelta + "..." + index + "..." + index1 + "..." + index2);
                        //tempDelta = (1/maxFuzzyMembership) * (ln(observed / expected));
                        //tempDelta = (1 / maxFuzzyMembership) * Math.log(observed / expected);
                        //tempDelta =  (Math.log(observed / expected));
                        }
                        delta.put(decisionClass, tempDelta);
                        tempLambda += tempDelta;

                        if (Double.isNaN(tempLambda)) {
                            System.out.println("tempLambda is NaN, index, index1, index2: " + tempLambda + "..." + index + "..." + index1 + "..." + index2);
                        }
                        tempMap1.remove(index);
                        tempMap1.put(index, tempLambda);
                        tempDouble = observed / expected;
                        if (Double.isNaN(tempDouble)) {
                            System.out.println("tempDouble is NaN, index, index1, index2: " + tempDouble + "..." + index + "..." + index1 + "..." + index2);
                        }
                        if (0.95 <= tempDouble && tempDouble <= 1.05) {
                            tempStopProcessingItemsetAL.add(index);
                            tempTempStopProcessingItemsetAL.add(index1);

                            if (!doneCountAL.contains(index + "$" + index1)) {
                                doneCountAL.add(index + "$" + index1);
                            }
                        ////System.out.println("tempStopProcessingItemsetAL.add for index, index1: " + index + "..." + index1);
                        }

                        System.out.println("observed, expected, tempDelta, tempLambda, tempDouble, maxFuzzyMembership: " + observed + "..." + expected + "..." + tempDelta + "..." + tempLambda + "..." + tempDouble + "..." + maxFuzzyMembership);

                    ////System.out.println("decisionClassesAL END......." + index1 + "..." + Calendar.getInstance().getTime());
                    }

                    for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {

                        ////System.out.println("decisionClassesAL START__________" + index1 + "..." + Calendar.getInstance().getTime());

                        if (doneCountAL.contains(index + "$" + index1)) {
                            ////System.out.println("Already DONE: " + index + "$" + index1);
                            continue;
                        }
                        decisionClass = (String) decisionClassesAL.get(index1);

                        tempStopProcessingItemsetAL = (ArrayList) stopProcessingDecisionClassesItemsetMap.get(decisionClass);

                        if (tempStopProcessingItemsetAL.contains(index) && !tempTempStopProcessingItemsetAL.contains(index1)) {
                            //// System.out.println("tempStopProcessingItemsetAL present for (and continue) index, index1: " + index + "..." + index1);
                            continue;
                        }

                        tempMap = (HashMap) s.get(decisionClass);

                        tempTrainingDataMatchAL = (ArrayList) trainingDataMatchMap.get(decisionClass);

                        if (delta.containsKey(decisionClass)) {
                            tempDelta = (Double) delta.get(decisionClass);

                        } else {
                            continue;
                        }
                        for (index2 = 0; index2 <= trainingDataSize; index2++) {

                            if (index2 % 100000 == 0) {
                                ////System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                            }

                            if (!tempTrainingDataMatchAL.contains(index2)) {
                                continue;
                            }
                            if (z.containsKey(index2)) {
                                tempZ = (Double) z.get(index2);
                            } else {
                                continue;
                            }

                            if (tempMap.containsKey(index2)) {
                                tempS = (Double) tempMap.get(index2);
                            } else {
                                continue;
                            }
                            ////System.out.println("BEFORE index, index1, index2, tempS, tempZ, tempDelta: " + index + "..." + index1 + "..." + index2 + "..." + tempS + "..." + tempZ + "..." + tempDelta);

                            tempZ -= Math.exp(tempS);
                            if (Double.isNaN(tempZ)) {
                                System.out.println("tempZ is NaN, index, index1, index2: " + tempZ + "..." + index + "..." + index1 + "..." + index2);
                            //tempZ -= exp(tempS);
                            }
                            tempS += tempDelta;
                            if (Double.isNaN(tempS)) {
                                System.out.println("tempS is NaN, index, index1, index2: " + tempS + "..." + index + "..." + index1 + "..." + index2);
                            }
                            tempZ += Math.exp(tempS);
                            if (Double.isNaN(tempZ)) {
                                System.out.println("tempZ1 is NaN, index, index1, index2: " + tempZ + "..." + index + "..." + index1 + "..." + index2);
                            //tempZ += exp(tempS);

                            ////System.out.println("AFTER index, index1, index2, tempS, tempZ, tempDelta: " + index + "..." + index1 + "..." + index2 + "..." + tempS + "..." + tempZ + "..." + tempDelta);
                            }
                            z.remove(index2);
                            z.put(index2, tempZ);
                            tempMap.remove(index2);
                            tempMap.put(index2, tempS);
                        }

                    ////System.out.println("decisionClassesAL END__________" + index1 + "..." + Calendar.getInstance().getTime());
                    }

                ////System.out.println("itemsetsAL END..................................." + index + "..." + Calendar.getInstance().getTime());
                }
                System.out.println("\n\nlambda for infiniteIndex: " + infiniteIndex);
                System.out.println(lambda);
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        ArrayList tempAL = new ArrayList(lambda.keySet());
        ArrayList tempAL1 = null;
        try {
            fileName = "FinalResults.txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("trainedClassifier.txt", false), true);
            pw.println(decisionClassLabel);

            for (index = 0; index < tempAL.size(); index++) {
                decisionClass = (String) tempAL.get(index);
                pw.println(decisionClass + "@@@@@" + decisionClassCountMap.get(decisionClass));
                tempMap = (HashMap) lambda.get(decisionClass);
                tempAL1 = new ArrayList(tempMap.keySet());


                for (index1 = 0; index1 < tempAL1.size(); index1++) {
                    index2 = (Integer) tempAL1.get(index1);
                    if (tempAL1.contains(index2)) {
                        pw.println(itemsetsAL.get(index2) + "-->>>" + tempMap.get(index2));
                    }
                }
                pw.println("END OF CLASS");

            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
    
    private void actualCalculationGISNonLogPudi() {
        int index, index1, index2 = 0, infiniteIndex;
        String fileName = "FACMETrainingRecords.txt", line = null, subLine = null, itemset = null, decisionClass = null, token = null;
        BufferedReader bufReader = null;
        StringTokenizer st = null, st1 = null;
        double tempLambda = 0, expected = 0, observed, tempDouble, trainingDataSizeInverse, probability, probabilitySum = 0, slowingFactor, tempSlowingFactor;
        ArrayList tempItemsetAL = new ArrayList(), tempTrainingDataMatchAL = null, tempStopProcessingItemsetAL = null, tempTempStopProcessingItemsetAL = null, doneCountAL = new ArrayList(), discardedDoneCountAL = new ArrayList(), decisionClassisCountedAL = new ArrayList(), trainingData = new ArrayList();
        boolean isMatch;
        HashMap tempMap = null, tempMap1 = null, tempObservedMap = null, expectedMap = null, tempExpectedMap = null, probabilityOfTransaction = null, tempProbabilityOfTransaction = null, probabilityOfTransactionSum;
        PrintWriter pw = null;

        expectedMap = new HashMap();
        probabilityOfTransaction = new HashMap();
        probabilityOfTransactionSum = new HashMap();
        
        System.out.println("itemsetsAL: " + itemsetsAL);
        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }

                line = bufReader.readLine();

                if (line == null) {
                    break;
                } else {
                    trainingData.add(line);
                }
                index2++;
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        trainingDataSizeInverse = 1 / ((double) (trainingData.size() + 1));
        slowingFactor = itemsetsAL.size();
        
        
        for (infiniteIndex = 0; (doneCountAL.size() + discardedDoneCountAL.size()) < (itemsetsAL.size() * decisionClassesAL.size()); infiniteIndex++) {
            System.out.println("\n\ninfiniteIndex, (doneCountAL.size() + discardedDoneCountAL.size()), itemsetsAL.size() * decisionClassesAL.size() : " + infiniteIndex + "..." + (doneCountAL.size() + discardedDoneCountAL.size()) + "..." + (itemsetsAL.size() * decisionClassesAL.size()));
            expectedMap.clear();

            for (index2 = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                line = (String) trainingData.get(index2);

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    decisionClass = (String) decisionClassesAL.get(index1);
                    /*****************if (!line.contains(decisionClass)) {
                        continue;
                    }************************/
                    if (infiniteIndex != 0) {
                        if (!probabilityOfTransaction.containsKey(decisionClass)) {
                            System.out.println("FATAL ERROR probabilityOfTransaction has no entry for decisionClass: " + decisionClass);
                            continue;
                        } else {
                            tempProbabilityOfTransaction = (HashMap) probabilityOfTransaction.get(decisionClass);
                        }

                        if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                            System.out.println("FATAL ERROR probabilitySum is 0 for decisionClass: " + decisionClass);
                            continue;
                        } else {
                            probabilitySum = (Double) probabilityOfTransactionSum.get(decisionClass);
                            probabilitySum += 1;
                        }
                    }

                    if (!expectedMap.containsKey(decisionClass)) {
                        tempExpectedMap = new HashMap();
                        expectedMap.put(decisionClass, tempExpectedMap);
                    } else {
                        tempExpectedMap = (HashMap) expectedMap.get(decisionClass);
                    }

                    for (index = 0, tempSlowingFactor = 0; index < itemsetsAL.size(); index++) {

                        if (doneCountAL.contains(index + "$" + index1) || discardedDoneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }
                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            if (!tempExpectedMap.containsKey(index)) {
                                expected = 0;
                            } else {
                                expected = (Double) tempExpectedMap.get(index);
                            }

                            if (infiniteIndex == 0) {
                                expected += trainingDataSizeInverse;
                            } else if (tempProbabilityOfTransaction.containsKey(index2)) {
                                tempSlowingFactor++;
                                probability = (Double) tempProbabilityOfTransaction.get(index2);
                                
                                //if(infiniteIndex >= 239)
                                //    System.out.println("BEFORE infiniteIndex >= 239 index, index1, index2, prob, probSum, expected: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum + "..." + expected);
                                
                                probability = probability / probabilitySum;
                                //////////probability = (Math.exp(probability)) / probabilitySum;
                                expected += probability;
                                
                                //if(infiniteIndex >= 239)
                                  //  System.out.println("AFTER infiniteIndex >= 239 index, index1, index2, prob, probSum, expected: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum + "..." + expected);
                                
                                if(Double.isNaN(expected))
                                    System.out.println("Double.isNaN(expected) index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);
                            }
                            
                            if (tempExpectedMap.containsKey(index)) {
                                tempExpectedMap.remove(index);
                            }
                            tempExpectedMap.put(index, expected);
                        }
                        
                    }
                    
                    if((infiniteIndex != 0) && (tempSlowingFactor > slowingFactor)) {
                        slowingFactor = tempSlowingFactor;
                    }
                }
            }
            
            System.out.println("expectedMap: " + expectedMap);

            for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                decisionClass = (String) decisionClassesAL.get(index1);
                tempObservedMap = (HashMap) observedDecisionClassItemsetMap.get(decisionClass);
                tempExpectedMap = (HashMap) expectedMap.get(decisionClass);

                if (lambda.containsKey(decisionClass)) {
                    tempMap1 = (HashMap) lambda.get(decisionClass);
                } else {
                    tempMap1 = new HashMap();
                    lambda.put(decisionClass, tempMap1);
                }

                for (index = 0; index < itemsetsAL.size(); index++) {

                    if (doneCountAL.contains(index + "$" + index1) || discardedDoneCountAL.contains(index + "$" + index1)) {
                        continue;
                    }

                    if (tempObservedMap.containsKey(index)) {
                        observed = (Double) tempObservedMap.get(index);
                    } else {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add OBSERVED for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (observed <= 0) {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add OBSERVED <= 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempExpectedMap.containsKey(index)) {
                        expected = (Double) tempExpectedMap.get(index);
                    } else {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add EXPECTED for index, index1: " + index + "..." + index1);
                        continue;
                    }
                    if (expected <= 0) {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add EXPECTED <= 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempMap1.containsKey(index)) {
                        tempLambda = (Double) tempMap1.get(index);
                    } else {
                        tempLambda = 1;
                        //////////tempLambda = 0;
                    }
                    
                    tempDouble = (observed / expected);
                    tempLambda *= Math.pow(tempDouble, (1/slowingFactor));
                    //////////tempLambda += (Math.log(tempDouble)) / slowingFactor ;
                    
                    if(Double.isNaN(tempLambda))
                        System.out.println("Double.isNaN(tempLambda) index, index1: " + index + "..." + index1 + "..." + observed + "..." + expected + "..." + tempLambda);

                    tempMap1.put(index, tempLambda);

                    

                    if (Double.isNaN(tempDouble)) {
                        System.out.println("tempDouble is NaN, index, index1: " + tempDouble + "..." + index + "..." + index1);
                    }
                    if (0.95 <= tempDouble && tempDouble <= 1.05) {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add REALLY for index, index1: " + index + "..." + index1);
                    }

                    System.out.println("index + $ + index1: " + index + "$" + index1);

                    System.out.println("observed, expected, tempLambda, tempDouble, slowingFactor: " + observed + "..." + expected + "..." + tempLambda + "..." + tempDouble + "..." + slowingFactor);
                }
            }

            probabilityOfTransaction.clear();
            probabilityOfTransactionSum.clear();
            
            System.out.println("lambda: " + lambda);

            for (index2 = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                line = (String) trainingData.get(index2);

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    decisionClass = (String) decisionClassesAL.get(index1);

                    /*******************if (!line.contains(decisionClass)) {
                        continue;
                    }*************************/
                    if (!probabilityOfTransaction.containsKey(decisionClass)) {
                        tempProbabilityOfTransaction = new HashMap();
                        probabilityOfTransaction.put(decisionClass, tempProbabilityOfTransaction);
                    } else {
                        tempProbabilityOfTransaction = (HashMap) probabilityOfTransaction.get(decisionClass);
                    }

                    if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                        probabilitySum = 0;
                    } else {
                        probabilitySum = (Double) probabilityOfTransactionSum.get(decisionClass);
                    }
                    probability = 1;
                    //////////probability = 0;

                    tempMap1 = (HashMap) lambda.get(decisionClass);

                    for (index = 0; index < itemsetsAL.size(); index++) {
                        
                        if (discardedDoneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }
                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            tempLambda = (Double) tempMap1.get(index);
                            probability *= tempLambda;
                            //////////probability += tempLambda;
                            
                            if(Double.isNaN(probability))
                                System.out.println("Double.isNaN(probability) index, index1: " + index + "..." + index1 + "..." + probability + "..." + probabilitySum + "..." + tempLambda);
                        }
                    }
                    tempProbabilityOfTransaction.put(index2, probability);
                    
                    //if(infiniteIndex >= 23)
                    //    System.out.println("infiniteIndex >= 23 BEFORE index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);

                    probabilitySum += probability;
                    //////////probabilitySum += Math.exp(probability);
                    
                    //if(infiniteIndex >= 23)
                    //    System.out.println("infiniteIndex >= 23 AFTER index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);
                    if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                        probabilityOfTransactionSum.remove(decisionClass);
                    }
                    probabilityOfTransactionSum.put(decisionClass, probabilitySum);
                }
            }
            System.out.println("\n\nlambda for infiniteIndex: " + infiniteIndex);
            System.out.println(lambda);
            System.out.println(doneCountAL);
        }

        ArrayList tempAL = new ArrayList(lambda.keySet());
        ArrayList tempAL1 = null;
        try {
            fileName = "FinalResults.txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("trainedClassifier.txt", false), true);
            pw.println(decisionClassLabel);

            for (index = 0; index < tempAL.size(); index++) {
                decisionClass = (String) tempAL.get(index);
                pw.println(decisionClass + "@@@@@" + decisionClassCountMap.get(decisionClass));
                tempMap = (HashMap) lambda.get(decisionClass);
                tempAL1 = new ArrayList(tempMap.keySet());


                for (index1 = 0; index1 < tempAL1.size(); index1++) {
                    index2 = (Integer) tempAL1.get(index1);
                    if (tempAL1.contains(index2)) {
                        pw.println(itemsetsAL.get(index2) + "-->>>" + tempMap.get(index2));
                    }
                }
                pw.println("END OF CLASS");

            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
    
    private void actualCalculationGISBigDecimalPudi() {
        int index, index1, index2 = 0, infiniteIndex;
        String fileName = "FACMETrainingRecords.txt", line = null, subLine = null, itemset = null, decisionClass = null, token = null;
        BufferedReader bufReader = null;
        StringTokenizer st = null, st1 = null;
        BigDecimal tempLambda = null, expected = null, observed, tempDouble, trainingDataSizeInverse, probability, probabilitySum = null;
        ArrayList tempItemsetAL = new ArrayList(), tempTrainingDataMatchAL = null, tempStopProcessingItemsetAL = null, tempTempStopProcessingItemsetAL = null, doneCountAL = new ArrayList(), discardedDoneCountAL = new ArrayList(), decisionClassisCountedAL = new ArrayList(), trainingData = new ArrayList();
        boolean isMatch;
        HashMap tempMap = null, tempMap1 = null, tempObservedMap = null, expectedMap = null, tempExpectedMap = null, probabilityOfTransaction = null, tempProbabilityOfTransaction = null, probabilityOfTransactionSum;
        PrintWriter pw = null;

        expectedMap = new HashMap();
        probabilityOfTransaction = new HashMap();
        probabilityOfTransactionSum = new HashMap();
        System.out.println("itemsetsAL: " + itemsetsAL);
        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }

                line = bufReader.readLine();

                if (line == null) {
                    break;
                } else {
                    trainingData.add(line);
                }
                index2++;
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        trainingDataSizeInverse = BigDecimal.valueOf(1).divide(BigDecimal.valueOf((trainingData.size() + 1)));
        
        

        for (infiniteIndex = 0; (doneCountAL.size() + discardedDoneCountAL.size()) < (itemsetsAL.size() * decisionClassesAL.size()); infiniteIndex++) {
            System.out.println("\n\ninfiniteIndex, (doneCountAL.size() + discardedDoneCountAL.size()), itemsetsAL.size() * decisionClassesAL.size() : " + infiniteIndex + "..." + (doneCountAL.size() + discardedDoneCountAL.size()) + "..." + (itemsetsAL.size() * decisionClassesAL.size()));
            expectedMap.clear();

            for (index2 = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                line = (String) trainingData.get(index2);

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    decisionClass = (String) decisionClassesAL.get(index1);
                    if (!line.contains(decisionClass)) {
                        continue;
                    }
                    if (infiniteIndex != 0) {
                        if (!probabilityOfTransaction.containsKey(decisionClass)) {
                            System.out.println("FATAL ERROR probabilityOfTransaction has no entry for decisionClass: " + decisionClass);
                            continue;
                        } else {
                            tempProbabilityOfTransaction = (HashMap) probabilityOfTransaction.get(decisionClass);
                        }

                        if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                            System.out.println("FATAL ERROR probabilitySum is 0 for decisionClass: " + decisionClass);
                            continue;
                        } else {
                            probabilitySum = ((BigDecimal) probabilityOfTransactionSum.get(decisionClass)).add(BigDecimal.valueOf(1));
                            
                        }
                    }

                    if (!expectedMap.containsKey(decisionClass)) {
                        tempExpectedMap = new HashMap();
                        expectedMap.put(decisionClass, tempExpectedMap);
                    } else {
                        tempExpectedMap = (HashMap) expectedMap.get(decisionClass);
                    }

                    for (index = 0; index < itemsetsAL.size(); index++) {

                        if (doneCountAL.contains(index + "$" + index1) || discardedDoneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }
                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            if (!tempExpectedMap.containsKey(index)) {
                                expected = BigDecimal.valueOf(0);
                            } else {
                                expected = (BigDecimal) tempExpectedMap.get(index);
                            }

                            if (infiniteIndex == 0) {
                                expected = expected.add(trainingDataSizeInverse);
                            } else if (tempProbabilityOfTransaction.containsKey(index2)) {
                                probability = (BigDecimal) tempProbabilityOfTransaction.get(index2);
                                //if(infiniteIndex >= 24)
                                //    System.out.println("infiniteIndex >= 24 index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);
                                
                                //////////probability = probability / probabilitySum;
                                probability = probability.divide(probabilitySum, BigDecimal.ROUND_FLOOR);
                                expected = expected.add(probability);
                                
                                //if(Double.isNaN(expected))
                                //    System.out.println("Double.isNaN(expected) index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);
                            }
                            
                            if (tempExpectedMap.containsKey(index)) {
                                tempExpectedMap.remove(index);
                            }
                            tempExpectedMap.put(index, expected);
                        }
                        
                    }
                }
            }
            
            System.out.println("expectedMap: " + expectedMap);

            for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                decisionClass = (String) decisionClassesAL.get(index1);
                tempObservedMap = (HashMap) observedDecisionClassItemsetMap.get(decisionClass);
                tempExpectedMap = (HashMap) expectedMap.get(decisionClass);

                if (lambda.containsKey(decisionClass)) {
                    tempMap1 = (HashMap) lambda.get(decisionClass);
                } else {
                    tempMap1 = new HashMap();
                    lambda.put(decisionClass, tempMap1);
                }

                for (index = 0; index < itemsetsAL.size(); index++) {

                    if (doneCountAL.contains(index + "$" + index1) || discardedDoneCountAL.contains(index + "$" + index1)) {
                        continue;
                    }

                    if (tempObservedMap.containsKey(index)) {
                        observed = (BigDecimal) tempObservedMap.get(index);
                    } else {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add OBSERVED for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (observed.compareTo(BigDecimal.valueOf(0)) <= 0) {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add OBSERVED <= 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempExpectedMap.containsKey(index)) {
                        expected = (BigDecimal) tempExpectedMap.get(index);
                    } else {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add EXPECTED for index, index1: " + index + "..." + index1);
                        continue;
                    }
                    if (expected.compareTo(BigDecimal.valueOf(0)) <= 0) {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add EXPECTED <= 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempMap1.containsKey(index)) {
                        tempLambda = (BigDecimal) tempMap1.get(index);
                    } else {
                        //////////tempLambda = 1;
                        tempLambda = BigDecimal.valueOf(1);
                    }
                    
                    tempDouble = observed.divide(expected, BigDecimal.ROUND_FLOOR);
                    //////////tempLambda *= observed / expected;
                    tempLambda = tempLambda.multiply(tempDouble) ;
                    
                    //if(Double.isNaN(tempLambda))
                    //    System.out.println("Double.isNaN(tempLambda) index, index1: " + index + "..." + index1 + "..." + observed + "..." + expected + "..." + tempLambda);

                    tempMap1.put(index, tempLambda);

                    

                    /*if (Double.isNaN(tempDouble)) {
                        System.out.println("tempDouble is NaN, index, index1: " + tempDouble + "..." + index + "..." + index1);
                    }*/
                    if (0 <= tempDouble.compareTo(BigDecimal.valueOf(0.95)) && tempDouble.compareTo(BigDecimal.valueOf(1.05)) <= 0) {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add REALLY for index, index1: " + index + "..." + index1);
                    }

                    System.out.println("index + $ + index1: " + index + "$" + index1);

                    System.out.println("observed, expected, tempLambda, tempDouble: " + observed + "..." + expected + "..." + tempLambda + "..." + tempDouble);
                }
            }

            probabilityOfTransaction.clear();
            probabilityOfTransactionSum.clear();
            
            System.out.println("lambda: " + lambda);

            for (index2 = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                line = (String) trainingData.get(index2);

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    decisionClass = (String) decisionClassesAL.get(index1);

                    if (!line.contains(decisionClass)) {
                        continue;
                    }
                    if (!probabilityOfTransaction.containsKey(decisionClass)) {
                        tempProbabilityOfTransaction = new HashMap();
                        probabilityOfTransaction.put(decisionClass, tempProbabilityOfTransaction);
                    } else {
                        tempProbabilityOfTransaction = (HashMap) probabilityOfTransaction.get(decisionClass);
                    }

                    if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                        probabilitySum = BigDecimal.valueOf(0);
                    } else {
                        probabilitySum = (BigDecimal) probabilityOfTransactionSum.get(decisionClass);
                    }
                    //////////probability = 1;
                    probability = BigDecimal.valueOf(1);

                    tempMap1 = (HashMap) lambda.get(decisionClass);

                    for (index = 0; index < itemsetsAL.size(); index++) {
                        
                        if (discardedDoneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }
                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            tempLambda = (BigDecimal) tempMap1.get(index);
                            //////////probability *= tempLambda;
                            probability = probability.multiply(tempLambda);
                            
                            //if(Double.isNaN(probability))
                            //    System.out.println("Double.isNaN(probability) index, index1: " + index + "..." + index1 + "..." + probability + "..." + probabilitySum + "..." + tempLambda);
                        }
                    }
                    tempProbabilityOfTransaction.put(index2, probability);
                    
                    //if(infiniteIndex >= 23)
                    //    System.out.println("infiniteIndex >= 23 BEFORE index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);

                    //////////probabilitySum += probability;
                    probabilitySum = probabilitySum.add(probability);
                    
                    //if(infiniteIndex >= 23)
                    //    System.out.println("infiniteIndex >= 23 AFTER index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);
                    if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                        probabilityOfTransactionSum.remove(decisionClass);
                    }
                    probabilityOfTransactionSum.put(decisionClass, probabilitySum);
                }
            }

            System.out.println("\n\nlambda for infiniteIndex: " + infiniteIndex);
            System.out.println(lambda);
            System.out.println(doneCountAL);
        }

        ArrayList tempAL = new ArrayList(lambda.keySet());
        ArrayList tempAL1 = null;
        try {
            fileName = "FinalResults.txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("trainedClassifier.txt", false), true);
            pw.println(decisionClassLabel);

            for (index = 0; index < tempAL.size(); index++) {
                decisionClass = (String) tempAL.get(index);
                pw.println(decisionClass + "@@@@@" + decisionClassCountMap.get(decisionClass));
                tempMap = (HashMap) lambda.get(decisionClass);
                tempAL1 = new ArrayList(tempMap.keySet());


                for (index1 = 0; index1 < tempAL1.size(); index1++) {
                    index2 = (Integer) tempAL1.get(index1);
                    if (tempAL1.contains(index2)) {
                        pw.println(itemsetsAL.get(index2) + "-->>>" + tempMap.get(index2));
                    }
                }
                pw.println("END OF CLASS");

            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    private void actualCalculationGISLogPudi(int fileSerialNumber) {
        int index, index1, index2 = 0, infiniteIndex;
        String fileName = "FACMETrainingRecords" + fileSerialNumber + ".txt";
        String line = null, subLine = null, itemset = null, decisionClass = null, token = null;
        BufferedReader bufReader = null;
        StringTokenizer st = null, st1 = null;
        double tempLambda = 0, expected = 0, observed, tempDouble, trainingDataSizeInverse, probability, probabilitySum = 0, slowingFactor, tempSlowingFactor;
        ArrayList tempItemsetAL = new ArrayList(), tempTrainingDataMatchAL = null, tempStopProcessingItemsetAL = null, tempTempStopProcessingItemsetAL = null, doneCountAL = new ArrayList(), discardedDoneCountAL = new ArrayList(), decisionClassisCountedAL = new ArrayList(), trainingData = new ArrayList();
        boolean isMatch;
        HashMap tempMap = null, tempMap1 = null, tempObservedMap = null, expectedMap = null, tempExpectedMap = null, probabilityOfTransaction = null, tempProbabilityOfTransaction = null, probabilityOfTransactionSum;
        PrintWriter pw = null;

        expectedMap = new HashMap();
        probabilityOfTransaction = new HashMap();
        probabilityOfTransactionSum = new HashMap();
        System.out.println("itemsetsAL: " + itemsetsAL);
        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }

                line = bufReader.readLine();

                if (line == null) {
                    break;
                } else {
                    trainingData.add(line);
                }
                index2++;
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        trainingDataSizeInverse = 1 / ((double) (trainingData.size() + 1));
                
        
        for (infiniteIndex = 0, slowingFactor = 1; (((doneCountAL.size() + discardedDoneCountAL.size()) < (itemsetsAL.size() * decisionClassesAL.size()))/* && infiniteIndex < 250*/); infiniteIndex++, slowingFactor = 1) {
            System.out.println("\n\ninfiniteIndex, (doneCountAL.size() + discardedDoneCountAL.size()), itemsetsAL.size() * decisionClassesAL.size() : " + infiniteIndex + "..." + (doneCountAL.size() + discardedDoneCountAL.size()) + "..." + (itemsetsAL.size() * decisionClassesAL.size()));
            expectedMap.clear();

            for (index2 = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                line = (String) trainingData.get(index2);

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    decisionClass = (String) decisionClassesAL.get(index1);
                   
                    if (infiniteIndex != 0) {
                        if (!probabilityOfTransaction.containsKey(decisionClass)) {
                            System.out.println("FATAL ERROR probabilityOfTransaction has no entry for decisionClass: " + decisionClass);
                            continue;
                        } else {
                            tempProbabilityOfTransaction = (HashMap) probabilityOfTransaction.get(decisionClass);
                        }

                        if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                            System.out.println("FATAL ERROR probabilitySum is 0 for decisionClass: " + decisionClass);
                            continue;
                        } else {
                            probabilitySum = (Double) probabilityOfTransactionSum.get(decisionClass);
                            probabilitySum += 1;
                        }
                    }

                    if (!expectedMap.containsKey(decisionClass)) {
                        tempExpectedMap = new HashMap();
                        expectedMap.put(decisionClass, tempExpectedMap);
                    } else {
                        tempExpectedMap = (HashMap) expectedMap.get(decisionClass);
                    }

                    for (index = 0, tempSlowingFactor = 0; index < itemsetsAL.size(); index++) {

                        if (doneCountAL.contains(index + "$" + index1) || discardedDoneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }
                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            if (!tempExpectedMap.containsKey(index)) {
                                expected = 0;
                            } else {
                                expected = (Double) tempExpectedMap.get(index);
                            }
                            tempSlowingFactor++;

                            if (infiniteIndex == 0) {
                                expected += trainingDataSizeInverse;
                            } else if (tempProbabilityOfTransaction.containsKey(index2)) {
                                //////////tempSlowingFactor++;
                                probability = (Double) tempProbabilityOfTransaction.get(index2);
                                
                                probability = (Math.exp(probability)) / probabilitySum;
                                expected += probability;
                                
                                if(Double.isNaN(expected))
                                    System.out.println("Double.isNaN(expected) index, index1, index2: " + index + "..." + index1 + "..." + index2 + "..." + probability + "..." + probabilitySum);
                            }
                            
                            if (tempExpectedMap.containsKey(index)) {
                                tempExpectedMap.remove(index);
                            }
                            tempExpectedMap.put(index, expected);
                        }
                        
                    }
                    
                    if(/*(infiniteIndex != 0) && */(tempSlowingFactor > slowingFactor)) {
                        slowingFactor = tempSlowingFactor;
                    }
                }
            }
            
            System.out.println("expectedMap: " + expectedMap);

            for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                decisionClass = (String) decisionClassesAL.get(index1);
                tempObservedMap = (HashMap) observedDecisionClassItemsetMap.get(decisionClass);
                tempExpectedMap = (HashMap) expectedMap.get(decisionClass);

                if (lambda.containsKey(decisionClass)) {
                    tempMap1 = (HashMap) lambda.get(decisionClass);
                } else {
                    tempMap1 = new HashMap();
                    lambda.put(decisionClass, tempMap1);
                }

                for (index = 0; index < itemsetsAL.size(); index++) {

                    if (doneCountAL.contains(index + "$" + index1) || discardedDoneCountAL.contains(index + "$" + index1)) {
                        continue;
                    }

                    if (tempObservedMap.containsKey(index)) {
                        observed = (Double) tempObservedMap.get(index);
                    } else {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add OBSERVED for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (observed <= 0) {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add OBSERVED <= 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempExpectedMap.containsKey(index)) {
                        expected = (Double) tempExpectedMap.get(index);
                    } else {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add EXPECTED for index, index1: " + index + "..." + index1);
                        continue;
                    }
                    if (expected <= 0) {
                        if (!discardedDoneCountAL.contains(index + "$" + index1)) {
                            discardedDoneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("discardedDoneCountAL.add EXPECTED <= 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempMap1.containsKey(index)) {
                        tempLambda = (Double) tempMap1.get(index);
                    } else {
                        tempLambda = 0;
                    }
                    
                    tempDouble = (observed / expected);
                    tempLambda += (Math.log(tempDouble)) / slowingFactor ;
                    
                    if(Double.isNaN(tempLambda))
                        System.out.println("Double.isNaN(tempLambda) index, index1: " + index + "..." + index1 + "..." + observed + "..." + expected + "..." + tempLambda);

                    tempMap1.put(index, tempLambda);

                    if (Double.isNaN(tempDouble)) {
                        System.out.println("tempDouble is NaN, index, index1: " + tempDouble + "..." + index + "..." + index1);
                    }
                    if (0.95 <= tempDouble && tempDouble <= 1.05) {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add REALLY for index, index1: " + index + "..." + index1);
                    }

                    System.out.println("index + $ + index1: " + index + "$" + index1);

                    System.out.println("observed, expected, tempLambda, tempDouble, slowingFactor: " + observed + "..." + expected + "..." + tempLambda + "..." + tempDouble + "..." + slowingFactor);
                }
            }

            probabilityOfTransaction.clear();
            probabilityOfTransactionSum.clear();
            
            System.out.println("lambda: " + lambda);

            for (index2 = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                line = (String) trainingData.get(index2);

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    decisionClass = (String) decisionClassesAL.get(index1);

                    if (!probabilityOfTransaction.containsKey(decisionClass)) {
                        tempProbabilityOfTransaction = new HashMap();
                        probabilityOfTransaction.put(decisionClass, tempProbabilityOfTransaction);
                    } else {
                        tempProbabilityOfTransaction = (HashMap) probabilityOfTransaction.get(decisionClass);
                    }

                    if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                        probabilitySum = 0;
                    } else {
                        probabilitySum = (Double) probabilityOfTransactionSum.get(decisionClass);
                    }
                    probability = 0;

                    tempMap1 = (HashMap) lambda.get(decisionClass);

                    for (index = 0; index < itemsetsAL.size(); index++) {
                        
                        if (discardedDoneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }
                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            tempLambda = (Double) tempMap1.get(index);
                            probability += tempLambda;
                            
                            if(Double.isNaN(probability))
                                System.out.println("Double.isNaN(probability) index, index1: " + index + "..." + index1 + "..." + probability + "..." + probabilitySum + "..." + tempLambda);
                        }
                    }
                    tempProbabilityOfTransaction.put(index2, probability);
                                        
                    probabilitySum += Math.exp(probability);
                                       
                    if (!probabilityOfTransactionSum.containsKey(decisionClass)) {
                        probabilityOfTransactionSum.remove(decisionClass);
                    }
                    probabilityOfTransactionSum.put(decisionClass, probabilitySum);
                }
            }

            System.out.println("\n\nlambda for infiniteIndex: " + infiniteIndex);
            System.out.println(lambda);
            System.out.println(doneCountAL);
        }

        ArrayList tempAL = new ArrayList(lambda.keySet());
        ArrayList tempAL1 = null;
        try {
            fileName = "FinalResults" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("trainedClassifier" + fileSerialNumber + ".txt", false), true);
            pw.println(decisionClassLabel);

            for (index = 0; index < tempAL.size(); index++) {
                decisionClass = (String) tempAL.get(index);
                pw.println(decisionClass + "@@@@@" + decisionClassCountMap.get(decisionClass));
                tempMap = (HashMap) lambda.get(decisionClass);
                tempAL1 = new ArrayList(tempMap.keySet());

                for (index1 = 0; index1 < tempAL1.size(); index1++) {
                    index2 = (Integer) tempAL1.get(index1);
                    if (tempAL1.contains(index2)) {
                        pw.println(itemsetsAL.get(index2) + "-->>>" + tempMap.get(index2));
                    }
                }
                pw.println("END OF CLASS");

            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    private void actualCalculationGIS() {
        int index,  index1,  index2 = 0,  infiniteIndex,  startIndex,  endIndex,  indexTemp;
        String fileName = "fuzzyTrainingData.txt",line  = null,subLine  = null,itemset  = null,decisionClass  = null,token  = null;
        BufferedReader bufReader = null;
        StringTokenizer st = null,st1  = null;
        double minFuzzyMembership,  maxFuzzyMembership,  fuzzyMembershipSum,  fuzzyMembership,  tempZ,  tempS,  tempLambda = 0,  expected = 0,  observed,  tempDouble,  classCount,  minClassCountLine,  tempExpected;
        ArrayList tempItemsetAL = new ArrayList(),tempTrainingDataMatchAL  = null,tempStopProcessingItemsetAL  = null,tempTempStopProcessingItemsetAL  = null,doneCountAL  = new ArrayList(),decisionClassisCountedAL  = new ArrayList(),trainingData  = new ArrayList();
        boolean isMatch;
        HashMap tempMap = null,tempMap1  = null,tempObservedMap  = null,expectedMap  = null,tempExpectedMap  = null,trainingDataMatchMap  = null,tempTrainingDataMatchMap  = null;
        PrintWriter pw = null;
        expectedMap = new HashMap();
        try {

            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {

                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }

                line = bufReader.readLine();

                if (line == null) {
                    break;
                } else {
                    trainingData.add(line);
                }
                index2++;
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        for (infiniteIndex = 0; doneCountAL.size() < (itemsetsAL.size() * decisionClassesAL.size()); infiniteIndex++) {
            System.out.println("\n\ninfiniteIndex, doneCountAL.size(), itemsetsAL.size() * decisionClassesAL.size() : " + infiniteIndex + "..." + doneCountAL.size() + "..." + (itemsetsAL.size() * decisionClassesAL.size()));
            ////expectedMap = new HashMap();
            maxFuzzyMembership = 0;

            /*****try {
            bufReader = new BufferedReader(new FileReader(fileName));
            
            } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
            }*****/
            s.clear();

            /////for (index2 = 0  , classCount = 0; true; index2++) {
            for (index2 = 0    , classCount = 0; index2 < trainingData.size(); index2++) {
                if (index2 % 100000 == 0) {
                    System.out.println("index2: " + index2 + "..." + Calendar.getInstance().getTime());
                }
                /******try {
                line = bufReader.readLine();
                } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
                }
                if (line == null) {
                break;
                }*****/
                line = (String) trainingData.get(index2);
                trainingDataMatchMap = new HashMap();
                //tempTempStopProcessingItemsetAL = new ArrayList();

                if (s.containsKey(index2)) {
                    tempMap = (HashMap) s.get(index2);
                } else {
                    tempMap = new HashMap();
                    s.put(index2, tempMap);
                }

                tempZ = 0;

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    //System.out.println("decisionClassesAL START index1......." + index1 + "..." + Calendar.getInstance().getTime());

                    decisionClass = (String) decisionClassesAL.get(index1);

                    if (tempMap.containsKey(decisionClass)) {
                        tempS = (Double) tempMap.get(decisionClass);
                    } else {
                        tempS = 0;
                        tempMap.put(decisionClass, tempS);
                    }

                    if (lambda.containsKey(decisionClass)) {
                        tempMap1 = (HashMap) lambda.get(decisionClass);
                    } else {
                        tempMap1 = new HashMap();
                        lambda.put(decisionClass, tempMap1);
                    }

                    if (infiniteIndex == 0 && line.contains(decisionClass)) {
                        minClassCountLine = Double.POSITIVE_INFINITY;

                        if (!decisionClassCountMap.containsKey(decisionClass)) {
                            classCount = 0;
                        } else {
                            classCount = (Double) decisionClassCountMap.get(decisionClass);
                        }

                        for (subLine = line  , startIndex = 0; true;) {
                            startIndex = subLine.indexOf('^', startIndex);
                            endIndex = subLine.indexOf(',', startIndex);
                            if (endIndex == -1) {
                                tempDouble = Double.parseDouble(subLine.substring(startIndex + 1));
                                if (tempDouble < minClassCountLine) {
                                    minClassCountLine = tempDouble;
                                }
                                break;
                            } else {
                                tempDouble = Double.parseDouble(subLine.substring(startIndex + 1, endIndex));

                                if (tempDouble < minClassCountLine) {
                                    minClassCountLine = tempDouble;
                                }
                                subLine = subLine.substring(startIndex + 1);
                            }
                        }
                        classCount += minClassCountLine;
                        if (!decisionClassCountMap.containsKey(decisionClass)) {
                            decisionClassCountMap.put(decisionClass, classCount);
                        } else {
                            decisionClassCountMap.remove(decisionClass);
                            decisionClassCountMap.put(decisionClass, classCount);
                        }
                    }

                    tempTrainingDataMatchMap = new HashMap();
                    trainingDataMatchMap.put(decisionClass, tempTrainingDataMatchMap);

                    fuzzyMembershipSum = 0;

                    for (index = 0; index < itemsetsAL.size(); index++) {

                        if (doneCountAL.contains(index + "$" + index1)) {
                            continue;

                        //System.out.println("itemsetsAL START index..................................." + index + "..." + Calendar.getInstance().getTime());
                        }
                        if (tempMap1.containsKey(index)) {
                            tempLambda = (Double) tempMap1.get(index);
                        } else {
                            tempLambda = 0;
                        }

                        itemset = (String) itemsetsAL.get(index);

                        st = new StringTokenizer(line, ",");
                        minFuzzyMembership = 0;
                        tempItemsetAL = new ArrayList();
                        while (st.hasMoreTokens()) {
                            subLine = st.nextToken();
                            if (!subLine.contains(decisionClass)) {
                                st1 = new StringTokenizer(subLine, "^");
                                tempItemsetAL.add(st1.nextToken());
                                fuzzyMembership = Double.parseDouble(st1.nextToken());
                                if (minFuzzyMembership == 0 || fuzzyMembership < minFuzzyMembership) {
                                    minFuzzyMembership = fuzzyMembership;
                                }
                            }
                        }
                        st = new StringTokenizer(itemset, ",");
                        isMatch = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (!tempItemsetAL.contains(token)) {
                                isMatch = false;
                                break;
                            }
                        }
                        if (isMatch) {
                            tempTrainingDataMatchMap.put(index, minFuzzyMembership);
                            tempS += tempLambda;//// * minFuzzyMembership;

                            fuzzyMembershipSum += minFuzzyMembership;
                        ////System.out.println("index, index1, index2, tempS, tempZ, expected: " + index + "..." + index1 + "..." + index2 + "..." + tempS + "..." + tempZ + "..." + expected);

                        }
                    }

                    if (Double.isNaN(tempS)) {
                        System.out.println("tempS is NaN, index, index1: " + tempS + "..." + index + "..." + index1);
                    }
                    if (maxFuzzyMembership < fuzzyMembershipSum) {
                        maxFuzzyMembership = fuzzyMembershipSum;
                    }
                    tempMap.remove(decisionClass);
                    tempMap.put(decisionClass, tempS);
                    tempZ += Math.exp(tempS);

                    if (Double.isNaN(tempZ)) {
                        System.out.println("tempZ is NaN, index, index1: " + tempZ + "..." + index + "..." + index1);
                    }
                }

                for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                    //System.out.println("decisionClassesAL22222 START index1......." + index1 + "..." + Calendar.getInstance().getTime());

                    decisionClass = (String) decisionClassesAL.get(index1);

                    if (tempMap.containsKey(decisionClass)) {
                        tempS = (Double) tempMap.get(decisionClass);
                    } else {
                        tempS = 0;
                        tempMap.put(decisionClass, tempS);
                    }

                    tempTrainingDataMatchMap = (HashMap) trainingDataMatchMap.get(decisionClass);
                    tempTrainingDataMatchAL = new ArrayList(tempTrainingDataMatchMap.keySet());

                    if (!expectedMap.containsKey(decisionClass)) {
                        tempExpectedMap = new HashMap();
                    } else {
                        tempExpectedMap = (HashMap) expectedMap.get(decisionClass);
                    }

                    for (indexTemp = 0; indexTemp < tempTrainingDataMatchAL.size(); indexTemp++) {
                        index = (Integer) tempTrainingDataMatchAL.get(indexTemp);
                        //System.out.println("itemsetsAL22222 START index..................................." + index + "..." + Calendar.getInstance().getTime());

                        if (doneCountAL.contains(index + "$" + index1)) {
                            continue;
                        }

                        if (!tempExpectedMap.containsKey(index)) {
                            expected = 0;
                        } else {
                            expected = (Double) tempExpectedMap.get(index);
                        }

                        minFuzzyMembership = (Double) tempTrainingDataMatchMap.get(index);

                        //expected += minFuzzyMembership * (Math.exp(tempS) / tempZ);
                        tempExpected = expected;
                        tempExpected += Math.exp(tempS) / tempZ;

                        if (Double.isNaN(tempExpected)) {
                            System.out.println("Expected will now be NaN, expected, tempS, tempZ, Math.exp(tempS) / tempZ, index, index1: " + expected + "..." + tempS + "..." + tempZ + "..." + (Math.exp(tempS) / tempZ) + "..." + index + "..." + index1);
                        }
                        expected += Math.exp(tempS) / tempZ;

                        if (Double.isNaN(expected)) {
                            System.out.println("expected is NaN, index, index1: " + expected + "..." + index + "..." + index1);
                        }
                        if (expected <= 0) {
                            if (!doneCountAL.contains(index + "$" + index1)) {
                                doneCountAL.add(index + "$" + index1);
                            }
                            System.out.println("doneCountAL.add EXPECTED 0 for index, index1: " + index + "..." + index1);
                            continue;
                        }

                        if (tempExpectedMap.containsKey(index)) {
                            tempExpectedMap.remove(index);
                        }
                        tempExpectedMap.put(index, expected);
                    }

                    if (expectedMap.containsKey(decisionClass)) {
                        expectedMap.remove(decisionClass);
                    }
                    expectedMap.put(decisionClass, tempExpectedMap);

                }
            }

            for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                decisionClass = (String) decisionClassesAL.get(index1);
                tempObservedMap = (HashMap) observedDecisionClassItemsetMap.get(decisionClass);
                tempExpectedMap = (HashMap) expectedMap.get(decisionClass);
                if (lambda.containsKey(decisionClass)) {
                    tempMap1 = (HashMap) lambda.get(decisionClass);
                } else {
                    tempMap1 = new HashMap();
                    lambda.put(decisionClass, tempMap1);
                }

                for (index = 0; index < itemsetsAL.size(); index++) {

                    if (doneCountAL.contains(index + "$" + index1)) {
                        continue;
                    }
                    if (tempObservedMap.containsKey(index)) {
                        observed = (Double) tempObservedMap.get(index);
                    } else {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add OBSERVED for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (observed <= 0) {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add OBSERVED 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempExpectedMap.containsKey(index)) {
                        expected = (Double) tempExpectedMap.get(index);
                    } else {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add EXPECTED for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (expected <= 0) {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add EXPECTED 0 for index, index1: " + index + "..." + index1);
                        continue;
                    }

                    if (tempMap1.containsKey(index)) {
                        tempLambda = (Double) tempMap1.get(index);
                    } else {
                        tempLambda = 0;
                    }

                    tempLambda += Math.log(observed / expected) / maxFuzzyMembership;

                    if (Double.isNaN(tempLambda)) {
                        System.out.println("tempLambda is NaN, index, index1: " + tempLambda + "..." + index + "..." + index1);
                    }
                    if (tempMap1.containsKey(index)) {
                        tempMap1.remove(index);
                    }
                    tempMap1.put(index, tempLambda);

                    tempDouble = observed / expected;

                    if (Double.isNaN(tempDouble)) {
                        System.out.println("tempDouble is NaN, index, index1: " + tempDouble + "..." + index + "..." + index1);
                    }
                    if (0.95 <= tempDouble && tempDouble <= 1.05) {
                        if (!doneCountAL.contains(index + "$" + index1)) {
                            doneCountAL.add(index + "$" + index1);
                        }
                        System.out.println("doneCountAL.add REALLY for index, index1: " + index + "..." + index1);
                    }

                    System.out.println("index + $ + index1: " + index + "$" + index1);

                    System.out.println("observed, expected, tempLambda, tempDouble, maxFuzzyMembership: " + observed + "..." + expected + "..." + tempLambda + "..." + tempDouble + "..." + maxFuzzyMembership);

                }
            }
            System.out.println("\n\nlambda for infiniteIndex: " + infiniteIndex);
            System.out.println(lambda);
            System.out.println(doneCountAL);
        }


        ArrayList tempAL = new ArrayList(lambda.keySet());
        ArrayList tempAL1 = null;
        try {
            fileName = "FinalResults.txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("trainedClassifier.txt", false), true);
            pw.println(decisionClassLabel);

            for (index = 0; index < tempAL.size(); index++) {
                decisionClass = (String) tempAL.get(index);
                pw.println(decisionClass + "@@@@@" + decisionClassCountMap.get(decisionClass));
                tempMap = (HashMap) lambda.get(decisionClass);
                tempAL1 = new ArrayList(tempMap.keySet());


                for (index1 = 0; index1 < tempAL1.size(); index1++) {
                    index2 = (Integer) tempAL1.get(index1);
                    if (tempAL1.contains(index2)) {
                        pw.println(itemsetsAL.get(index2) + "-->>>" + tempMap.get(index2));
                    }
                }
                pw.println("END OF CLASS");

            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    private void readFromIsolatedInitializeFile(int fileSerialNumber) {
        BufferedReader bufReader;
        String line,fileName ,tempDecisionClass  = null;
        StringTokenizer st = null,st1  = null;
        HashMap tempMap = null;
        double classCount;

        try {
            fileName = "facme.csv";
            bufReader = new BufferedReader(new FileReader(fileName));
            bufReader.readLine();
            decisionClassLabel = bufReader.readLine();
            bufReader.close();

            fileName = "FinalResults" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            fileName = "FACMEInitialize" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            while (true) {
                tempDecisionClass = bufReader.readLine();
                if (tempDecisionClass == null) {
                    break;
                }
                
                
                st = new StringTokenizer(tempDecisionClass, "@@@@@");
                tempDecisionClass = st.nextToken();
                classCount = Double.parseDouble(st.nextToken());
                if (!decisionClassesAL.contains(tempDecisionClass)) {
                    decisionClassesAL.add(tempDecisionClass);
                }
                decisionClassCountMap.put(tempDecisionClass, classCount);

                line = bufReader.readLine();
                line = line.substring(1, line.length() - 1);
                //System.out.println("line: " + line);
                tempMap = new HashMap();
                st = new StringTokenizer(line, ", ");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "=");
                    if(isBigDecimal)
                        tempMap.put(Integer.parseInt(st1.nextToken()), (BigDecimal.valueOf(Double.parseDouble(st1.nextToken())).divide(BigDecimal.valueOf(480), BigDecimal.ROUND_FLOOR)));
                    else
                        tempMap.put(Integer.parseInt(st1.nextToken()), (Double.parseDouble(st1.nextToken())/480));
                }
                observedDecisionClassItemsetMap.put(tempDecisionClass, tempMap);
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public void generateRecordsFromAllSingletons(int fileSerialNumber) {
        BufferedReader bufReader;
        String line, fileName, singleton  = null,itemset  = null,newItemset  = null, attribute = null;
        StringTokenizer st = null;
        ArrayList singletonAL = null,currentKAL  = null,nextKAL  = null;
        int index,  index1 = 0,  indexCaret,  indexLastComma,  index3, indexEqualTo;
        boolean isDuplicate;

        PrintWriter pw = null;

        try {
            fileName = "facme.csv";
            bufReader = new BufferedReader(new FileReader(fileName));
            bufReader.readLine();
            decisionClassLabel = bufReader.readLine();
            bufReader.close();

            fileName = "fuzzyTrainingData" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            pw = new PrintWriter(new FileWriter("FACMETrainingRecords" + fileSerialNumber + ".txt", false), true);
            singletonAL = new ArrayList();

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                // Enumerate all singletons
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    singleton = st.nextToken();
                    indexCaret = singleton.indexOf('^');
                    singleton = singleton.substring(0, indexCaret);
                    if (singleton.contains(decisionClassLabel)) {
                    continue;
                    }
                    else
                    if (!singletonAL.contains(singleton + "^1")) {
                        singletonAL.add(singleton + "^1");
                        pw.println(singleton + "^1");
                    }
                }
            }
            bufReader.close();

            currentKAL = singletonAL;

            while (!currentKAL.isEmpty()) {
                nextKAL = new ArrayList();
                for (index = 0; index < currentKAL.size(); index++) {
                    itemset = (String) currentKAL.get(index);
                    indexLastComma = itemset.lastIndexOf(',');
                    if (indexLastComma != -1) {
                        singleton = itemset.substring(indexLastComma + 1);
                    } else {
                        singleton = itemset;
                    }
                    index3 = singletonAL.indexOf(singleton);

                    for (index1 = index3 + 1; index1 < singletonAL.size(); index1++) {
                        singleton = (String) singletonAL.get(index1);
                        indexEqualTo = singleton.indexOf('=');
                        attribute = singleton.substring(0, indexEqualTo);

                        if (!itemset.contains(attribute)) {
                            isDuplicate = false;
                            newItemset = itemset + "," + singleton;

                            // Check if itemset is duplicate
                            /*****for (index2 = 0; index2 < nextKAL.size(); index2++) {
                            st = new StringTokenizer(newItemset, ",");
                            itemset1 = (String) nextKAL.get(index2);
                            while (st.hasMoreTokens()) {
                            if(!itemset1.contains(st.nextToken())) {
                            break;
                            }
                            else if (!st.hasMoreTokens()) {
                            isDuplicate = true;
                            //break;
                            }
                            }
                            if (isDuplicate) {
                            break;
                            }
                            }****/
                            if (!isDuplicate) {
                                nextKAL.add(newItemset);
                                pw.println(newItemset);
                            }

                        /*if(!isIndexNextSet) {
                        indexNext = index1 + 1;
                        isIndexNextSet = true;
                        }*/
                        }
                    }
                }
                currentKAL = nextKAL;

            }
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return;
    }
    
    public void generateRecordsFromAllSingletonsFileSystem(int fileSerialNumber) {
        BufferedReader bufReader, bufReaderCurrent;
        String line, fileName, singleton  = null,itemset  = null,newItemset  = null, attribute = null;
        StringTokenizer st = null;
        ArrayList singletonAL = null;
        int index1 = 0,  indexCaret,  indexLastComma,  index3, indexEqualTo;
        boolean isEmpty = true;
        File nextFile, currentFile = null;

        PrintWriter pw = null, pwNext = null;

        try {
            fileName = "facme.csv";
            bufReader = new BufferedReader(new FileReader(fileName));
            bufReader.readLine();
            decisionClassLabel = bufReader.readLine();
            bufReader.close();

            fileName = "fuzzyTrainingData" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            pw = new PrintWriter(new FileWriter("FACMETrainingRecords" + fileSerialNumber + ".txt", false), true);
            pwNext = new PrintWriter(new FileWriter("tempCurrent.txt", false), true);
            singletonAL = new ArrayList();

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                // Enumerate all singletons
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    singleton = st.nextToken();
                    indexCaret = singleton.indexOf('^');
                    singleton = singleton.substring(0, indexCaret);
                    if (singleton.contains(decisionClassLabel)) {
                    continue;
                    }
                    else
                    if (!singletonAL.contains(singleton + "^1")) {
                        singletonAL.add(singleton + "^1");
                        pw.println(singleton + "^1");
                        pwNext.println(singleton + "^1");
                        isEmpty = false;
                    }
                }
            }
            bufReader.close();
            pwNext.close();

            while (!isEmpty) {
                pwNext = new PrintWriter(new FileWriter("tempNext.txt", false), true);
                bufReaderCurrent = new BufferedReader(new FileReader("tempCurrent.txt"));
                isEmpty = true;
                while (true) {
                    itemset = bufReaderCurrent.readLine();
                    if(itemset == null) {
                        break;
                    }
                    indexLastComma = itemset.lastIndexOf(',');
                    if (indexLastComma != -1) {
                        singleton = itemset.substring(indexLastComma + 1);
                    } else {
                        singleton = itemset;
                    }
                    index3 = singletonAL.indexOf(singleton);

                    for (index1 = index3 + 1; index1 < singletonAL.size(); index1++) {
                        singleton = (String) singletonAL.get(index1);
                        indexEqualTo = singleton.indexOf('=');
                        attribute = singleton.substring(0, indexEqualTo);

                        if (!itemset.contains(attribute)) {
                            newItemset = itemset + "," + singleton;

                            pwNext.println(newItemset);
                            pw.println(newItemset);
                            isEmpty = false;

                        }
                    }
                }
                bufReaderCurrent.close();
                pwNext.close();
                nextFile = new File("tempNext.txt");
                currentFile = new File("tempCurrent.txt");
                if (currentFile.exists()) {
                    System.out.println("Delete: " + currentFile.delete());
                }
                Thread.sleep(100);
                if (nextFile.exists()) {
                    System.out.println("Rename: " + nextFile.renameTo(currentFile));
                }

            }
            pw.close();
            if (currentFile.exists()) {
                System.out.println("Delete Last: " + currentFile.delete());
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        } 
        catch (InterruptedException ie) {
            System.out.println("InterruptedException: " + ie);
            ie.printStackTrace();
        }
        return;
    }

    public void generateRecordsFromFrequentSingletons() {
        BufferedReader bufReader;
        String line,fileName ,singleton  = null,itemset  = null,itemset1  = null,newItemset  = null;
        StringTokenizer st = null;
        ArrayList singletonAL = null,currentKAL  = null,nextKAL  = null;
        int index,  index1 = 0,  index2;
        boolean isDuplicate;

        PrintWriter pw = null;

        try {
            fileName = "facme.csv";
            bufReader = new BufferedReader(new FileReader(fileName));
            bufReader.readLine();
            decisionClassLabel = bufReader.readLine();
            bufReader.close();

            fileName = "FinalResults.txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            pw = new PrintWriter(new FileWriter("FACMETrainingRecords.txt", false), true);
            singletonAL = new ArrayList();

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                // Enumerate all singletons
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    singleton = st.nextToken();
                    if (!singletonAL.contains(singleton + "^1")) {
                        singletonAL.add(singleton + "^1");
                        pw.println(singleton + "^1");
                    }
                }
            }
            bufReader.close();

            currentKAL = singletonAL;

            while (!currentKAL.isEmpty()) {
                nextKAL = new ArrayList();
                for (index = 0; index < currentKAL.size(); index++) {
                    itemset = (String) currentKAL.get(index);

                    for (index1 = 0; index1 < singletonAL.size(); index1++) {
                        singleton = (String) singletonAL.get(index1);

                        if (!itemset.contains(singleton)) {
                            isDuplicate = false;
                            newItemset = itemset + "," + singleton;

                            // Check if itemset is duplicate
                            for (index2 = 0; index2 < nextKAL.size(); index2++) {
                                st = new StringTokenizer(newItemset, ",");
                                itemset1 = (String) nextKAL.get(index2);
                                while (st.hasMoreTokens()) {
                                    if (!itemset1.contains(st.nextToken())) {
                                        break;
                                    } else if (!st.hasMoreTokens()) {
                                        isDuplicate = true;
                                    //break;
                                    }
                                }
                                if (isDuplicate) {
                                    break;
                                }
                            }

                            if (!isDuplicate) {
                                nextKAL.add(newItemset);
                                pw.println(newItemset);
                            }

                        /*if(!isIndexNextSet) {
                        indexNext = index1 + 1;
                        isIndexNextSet = true;
                        }*/
                        }
                    }
                }
                currentKAL = nextKAL;

            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return;
    }

    public void initialize(int fileSerialNumber) {
        BufferedReader bufReader;
        String line,subLine ,fileName ,tempDecisionClass  = null,itemset  = null,token  = null;
        StringTokenizer st = null,st1  = null;
        ArrayList tempItemsetAL = null,tempAL  = null;
        double minFuzzyMembership, fuzzyMembership, classCount;
        HashMap tempMap = null;
        int index,  indexCount = 0;
        boolean isMatch;
        PrintWriter pw = null;

        try {
            fileName = "facme.csv";
            bufReader = new BufferedReader(new FileReader(fileName));
            bufReader.readLine();
            decisionClassLabel = bufReader.readLine();
            bufReader.close();

            fileName = "FinalResults" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(decisionClassLabel)) {
                    continue;
                }
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            bufReader.close();

            fileName = "fuzzyTrainingData" + fileSerialNumber + ".txt";
            bufReader = new BufferedReader(new FileReader(fileName));

            while (true) {
                indexCount++;
                if (indexCount % 10000 == 0) {
                    System.out.println("indexCount: " + indexCount + "..." + Calendar.getInstance().getTime());
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                minFuzzyMembership = 0;
                tempItemsetAL = new ArrayList();
                while (st.hasMoreTokens()) {
                    subLine = st.nextToken();
                    if (subLine.contains(decisionClassLabel)) {
                        tempDecisionClass = subLine;
                        if (!decisionClassesAL.contains(tempDecisionClass)) {
                            decisionClassesAL.add(tempDecisionClass);
                            observedDecisionClassItemsetMap.put(tempDecisionClass, new HashMap());
                        }
                    //break;
                    } else {
                        st1 = new StringTokenizer(subLine, "^");
                        tempItemsetAL.add(st1.nextToken());
                        fuzzyMembership = Double.parseDouble(st1.nextToken());
                        if (minFuzzyMembership == 0 || fuzzyMembership < minFuzzyMembership) {
                            minFuzzyMembership = fuzzyMembership;
                        }
                    }
                }
                
                if (!decisionClassCountMap.containsKey(tempDecisionClass)) {
                    classCount = 0;
                } else {
                    classCount = (Double) decisionClassCountMap.get(tempDecisionClass);
                    decisionClassCountMap.remove(tempDecisionClass);
                }
                
                classCount += minFuzzyMembership;
                decisionClassCountMap.put(tempDecisionClass, classCount);
                
                //System.out.println("indexCount, tempDecisionClass, minFuzzyMembership, classCount: " + indexCount + "..." + tempDecisionClass + "..." + minFuzzyMembership + "..." + classCount);
            
                tempMap = (HashMap) observedDecisionClassItemsetMap.get(tempDecisionClass);

                for (index = 0; index < itemsetsAL.size(); index++) {
                    itemset = (String) itemsetsAL.get(index);
                    st = new StringTokenizer(itemset, ",");
                    isMatch = true;
                    while (st.hasMoreTokens()) {
                        token = st.nextToken();
                        if (!tempItemsetAL.contains(token)) {
                            isMatch = false;
                            break;
                        }

                    }
                    if (isMatch) {
                        if (!tempMap.containsKey(index)) {
                            tempMap.put(index, minFuzzyMembership);
                        } else {
                            fuzzyMembership = (Double) tempMap.get(index);
                            fuzzyMembership += minFuzzyMembership;
                            tempMap.remove(index);
                            tempMap.put(index, fuzzyMembership);
                        }
                    }
                }
            }
            bufReader.close();

            if (isIsolatedInitialize) {
                pw = new PrintWriter(new FileWriter("FACMEInitialize" + fileSerialNumber + ".txt", false), true);
                tempAL = new ArrayList(observedDecisionClassItemsetMap.keySet());
                for (index = 0; index < tempAL.size(); index++) {
                    tempDecisionClass = (String) tempAL.get(index);
                    pw.println(tempDecisionClass + "@@@@@" + decisionClassCountMap.get(tempDecisionClass));
                    pw.println(observedDecisionClassItemsetMap.get(tempDecisionClass));
                }
                pw.close();
            }

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        return;
    }

    public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    public static double exp(double val) {
        final long tmp = (long) (1512775 * val + (1072693248 - 60801));
        return Double.longBitsToDouble(tmp << 32);
    }

    public double ln(double val) {
        final double x = (Double.doubleToLongBits(val) >> 32);
        return (x - 1072632447) / 1512775;
    }
}
