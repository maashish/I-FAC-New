
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
import java.util.StringTokenizer;

public class FARMORBzip2ByteArraySubPartitionZipSparseShort {

    private ArrayList classLabel = new ArrayList();
    private ArrayList classType = new ArrayList();
    private HashMap classFilename = new HashMap();
    private ArrayList trainingData = new ArrayList();
    private HashMap numAttribute = new HashMap();
    private HashMap numValue = new HashMap();
    private String decisionClassLabel = null;
    private float support = 1;
    private float trainingDataSizeCutOff = 0;
    private int trainingDataSize = 0;
    private int numberOfPartitions;
    private int partitionSize;
    private int indexPartition;
    private int countK = 0;
    private float lastPartitionCutOff;
    private int numberOfSubPartitions;
    private int subPartitionSize;
    private float hundred = 10000;
    private float tenThousand = (float) 10000;
    private ArrayList nonDecisionItemSetsAL = null;
    private HashMap itemsetsFinal = new HashMap();
    private HashMap itemsetsFinalPartition = new HashMap();
    private HashMap itemsetsSubPartition = null;
    private ArrayList newItemsetUpdate = null;
    private HashMap nextItemsets = null;
    private HashMap currentItemsets = null;
    private ArrayList currentItemsetsAL = null;
    private HashMap nonDecisionItemSets = null;
    private boolean secondPhase = false;
    private long compressTime = 0, uncompressTime = 0, fullTime = 0;
    private int compressCount = 0, uncompressCount = 0, fullCount = 0;
    private long maxMemoryUsed;

    public void ReadFiles() {

        File file = new File("facme.csv");
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        StringTokenizer st = null;

        String label, type, filename = null, trainingFilename, line, token;
        int index = 0, commaIndex = 0;

        try {

            FileReader file_reader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(file_reader);


            trainingFilename = bufReader.readLine();
            decisionClassLabel = bufReader.readLine();

            System.out.println("decisionClassLabel: " + decisionClassLabel);

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                label = st.nextToken();
                type = st.nextToken();
                if (st.hasMoreTokens()) {
                    filename = st.nextToken();
                }
                classLabel.add(label);
                classType.add(type);
                if (type.equalsIgnoreCase("num")) {
                    classFilename.put(label, filename);
                }
            }

            System.out.println("classLabel: " + classLabel);
            System.out.println("classType: " + classType);
            System.out.println("classFilename: " + classFilename);

            bufReader.close();
            file_reader.close();

            file = new File(trainingFilename);
            file_reader = new FileReader(file);
            bufReader = new BufferedReader(file_reader);

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                trainingData.add(line);
            }

            //System.out.println("trainingData: " + trainingData);

            bufReader.close();
            file_reader.close();


            for (index = 0; index < classType.size(); index++) {
                label = (String) classLabel.get(index);
                type = (String) classType.get(index);
                if (type.equalsIgnoreCase("num")) {
                    file = new File((String) classFilename.get(label));

                    file_reader = new FileReader(file);
                    bufReader = new BufferedReader(file_reader);

                    line = bufReader.readLine();

                    st = new StringTokenizer(line, ",");
                    token = st.nextToken();
                    if (!token.equals(label)) {
                        System.out.println("Fatal Error, label mismatch... Exiting");
                        System.exit(0);
                    }

                    numAttributeTemp = new ArrayList();
                    while (st.hasMoreTokens()) {
                        numAttributeTemp.add(st.nextToken());
                    }

                    numAttribute.put(label, numAttributeTemp);

                    //System.out.println("numAttributeTemp: " + numAttributeTemp);
                    //System.out.println("numAttribute: " + numAttribute);



                    numValueTemp = new HashMap();

                    while (true) {
                        line = bufReader.readLine();
                        if (line == null) {
                            break;
                        }
                        commaIndex = line.indexOf(',');

                        numValueTemp.put(line.substring(0, commaIndex), line.substring(commaIndex + 1));
                    }

                    numValue.put(label, numValueTemp);

                    //System.out.println("numValueTemp: " + numValueTemp);
                    //System.out.println("numValue: " + numValue);

                    bufReader.close();
                    file_reader.close();

                }
            }


        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

    }

    public void generateFuzzyTrainingData() {
        int index, indexFuzzy, indexRule = 0, caretIndex = 0, commaIndex = 0, commaIndex1 = 0;
        StringTokenizer st = null, st1 = null;
        String line, attribute, value, fuzzyValue, fuzzyMembership, fuzzyMembershipString, type, rule, oldRule, tempRule, tempRule1, preRule, currentRule, postRule;
        ArrayList numAttributeTemp = null, oldfuzzyTrainingData = null;
        HashMap numValueTemp = null;
        ArrayList fuzzyTrainingData = new ArrayList();

        attribute = (String) classLabel.get(0);
        type = (String) classType.get(0);

        System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

        for (index = 0; index < trainingData.size(); index++) {

            line = (String) trainingData.get(index);
            st = new StringTokenizer(line, ",");
            commaIndex = line.indexOf(',');

            postRule = line.substring(commaIndex);

            value = st.nextToken();

            if (type.equalsIgnoreCase("nom")) {
                rule = attribute + "=" + value + "^1" + postRule;
                fuzzyTrainingData.add(rule);
            } else if (type.equalsIgnoreCase("num")) {
                numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                numValueTemp = (HashMap) numValue.get(attribute);
                fuzzyMembershipString = (String) numValueTemp.get(value);
                st1 = new StringTokenizer(fuzzyMembershipString, ",");

                for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                    fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                    fuzzyMembership = st1.nextToken();
                    if (Float.parseFloat(fuzzyMembership) >= 0.1) {
                        rule = attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                        fuzzyTrainingData.add(rule);
                        //System.out.println("Fuzzy rule: " + rule);
                    }
                }
            }

        }
        System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

        for (index = 1; index < classLabel.size(); index++) {

            attribute = (String) classLabel.get(index);
            type = (String) classType.get(index);
            trainingDataSize = fuzzyTrainingData.size();
            oldfuzzyTrainingData = null;

            oldfuzzyTrainingData = fuzzyTrainingData;
            fuzzyTrainingData = new ArrayList();


            System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

            for (indexRule = 0; indexRule < trainingDataSize; indexRule++) {
                oldRule = (String) oldfuzzyTrainingData.get(indexRule);
                caretIndex = oldRule.lastIndexOf('^');
                tempRule = oldRule.substring(0, caretIndex + 1);
                tempRule1 = oldRule.substring(caretIndex + 1);
                commaIndex = tempRule1.indexOf(',');

                preRule = tempRule + tempRule1.substring(0, commaIndex + 1);

                commaIndex1 = tempRule1.indexOf(',', commaIndex + 1);

                if (commaIndex1 > -1) {
                    currentRule = tempRule1.substring(commaIndex + 1, commaIndex1);
                    postRule = tempRule1.substring(commaIndex1);
                } else {
                    currentRule = tempRule1.substring(commaIndex + 1);
                    postRule = "";
                }

                if (type.equalsIgnoreCase("nom")) {
                    rule = preRule + attribute + "=" + currentRule + "^1" + postRule;
                    fuzzyTrainingData.add(rule);
                } else if (type.equalsIgnoreCase("num")) {
                    numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                    numValueTemp = (HashMap) numValue.get(attribute);
                    fuzzyMembershipString = (String) numValueTemp.get(currentRule);
                    st1 = new StringTokenizer(fuzzyMembershipString, ",");

                    for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                        fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                        fuzzyMembership = st1.nextToken();
                        if (Float.parseFloat(fuzzyMembership) >= 0.1) {
                            rule = preRule + attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                            fuzzyTrainingData.add(rule);
                        }
                    }
                }
            }

            System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
        }

        System.out.println("Actual Fuzzy Rules\n\n");

        FileWriter fw = null;
        try {
            fw = new FileWriter("fuzzytrainingdata.txt", false);
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw, true);

        for (index = 0; index < fuzzyTrainingData.size(); index++) {
            System.out.println(fuzzyTrainingData.get(index));
            pw.println(fuzzyTrainingData.get(index));
        }
    }

    private void calculateMaxMemoryUsed() {
        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (memoryUsed > maxMemoryUsed) {
            maxMemoryUsed = memoryUsed;
        }
    }

    public void generateAssociationRules(int fileSerialNumber, String attribute, String parametersFile, boolean isOnlyClass) {
        int index1, index2;
        String itemset1 = null;
        short[] tempItemset1 = null;
        BufferedReader bufReaderGlobal = null;
        String fuzzyTrainingDataFile = null;
        ArrayList tempAL = null;
        maxMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        if (attribute.equals("GLOBAL")) {
            fuzzyTrainingDataFile = "fuzzyTrainingData" + fileSerialNumber + ".txt";
        } else {
            fuzzyTrainingDataFile = "fuzzyTrainingData_" + fileSerialNumber + "_" + attribute + ".txt";
        }

        initialize(fuzzyTrainingDataFile, parametersFile);
        calculateMaxMemoryUsed();

        try {
            System.out.println("file_readerGlobal END bufReaderGlobal START ........." + Calendar.getInstance().getTime());

            bufReaderGlobal = new BufferedReader(new FileReader(fuzzyTrainingDataFile));

            System.out.println("bufReaderGlobal END........." + Calendar.getInstance().getTime());
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        // 1st phase        
        for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
            System.out.println("indexPartition............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

            nonDecisionItemSets = new HashMap();
            itemsetsSubPartition = new HashMap();

            for (index1 = 0; index1 < numberOfSubPartitions; index1++) {
                System.out.println("index1 Start........" + index1 + "..." + Calendar.getInstance().getTime());
                generatePartitionSingletons(bufReaderGlobal, index1);
                calculateMaxMemoryUsed();
                System.out.println("index1 END........" + index1 + "..." + Calendar.getInstance().getTime());
            }
            pruneSingletons();
            calculateMaxMemoryUsed();
            System.out.println("singletons generated");

            //generate itemsets

            tempAL = new ArrayList(nonDecisionItemSets.keySet());
            byte [] tempByteArray = null;
            for (index1 = 0; index1 < tempAL.size() - 1; index1++) {

                try {
                    itemset1 = (String) tempAL.get(index1);
                    //CLASS Specific - Start
                    if (isOnlyClass && !itemset1.contains("CLASS")) {
                        continue;
                    }
                    //CLASS Specific - End

                    //System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + itemset1 + "..." + ((byte[]) nonDecisionItemSets.get(itemset1)).length + "..." + Calendar.getInstance().getTime());
                    //
                    tempByteArray = StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(itemset1));
                    tempItemset1 = ByteStreamConverters.byteArrayToShortArray(tempByteArray);
                    ///System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + itemset1 + "..." + tempItemset1.length + "..." + Calendar.getInstance().getTime());
                    System.out.print("itemset1: " + itemset1);
                    System.out.print("((byte[]) nonDecisionItemSets.get(itemset1)) Array: ");
                    for(int i = 0; i < ((byte[]) nonDecisionItemSets.get(itemset1)).length; i++) {
                        System.out.print(((byte[]) nonDecisionItemSets.get(itemset1))[i] + " ");
                    }
                    System.out.println();
                    System.out.print("tempByteArray Array: ");
                    for(int i = 0; i < tempByteArray.length; i++) {
                        System.out.print(tempByteArray[i] + " ");
                    }
                    System.out.println();
                    System.out.print("tempItemset1 Array: ");
                    for(int i = 0; i < tempItemset1.length; i++) {
                        System.out.print(tempItemset1[i] + " ");
                    }
                    System.out.println();
                } catch (IOException e) {
                    System.out.println("IO exception = " + e);
                    e.printStackTrace();
                }
                //CLASS Specific - Start
                for (index2 = 0; index2 < tempAL.size(); index2++) {
                    if(index2 == index1) {
                        continue;
                    }
                //CLASS Specific - End
                //for (index2 = index1 + 1; index2 < tempAL.size(); index2++) {
                    combineDFS(itemset1, tempItemset1, index2, tempAL, isOnlyClass);
                }

                tempItemset1 = null;
                System.out.println("Removed from current itemsetsAL: " + (String) tempAL.get(index1) + "..." + index1 + "..." + nonDecisionItemSets.size() + "..." + Calendar.getInstance().getTime());
                nonDecisionItemSets.remove(itemset1);
                tempAL.remove(index1);
                itemset1 = null;
                calculateMaxMemoryUsed();
                System.gc();
                index1--;
            }

            System.out.println("indexPartition FINISH............................................." + indexPartition + "..." + Calendar.getInstance().getTime());
            System.out.println("itemsetsFinal:" + itemsetsFinal);
            System.out.println("\n\nitemsetsFinalPartition:" + itemsetsFinalPartition);
            System.out.println("\n\n\n");
        }

        try {
            bufReaderGlobal.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter("firstphasedata_" + fileSerialNumber + "_" + attribute + ".txt", false);

            PrintWriter pw = new PrintWriter(fw, true);
            String itemset;
            currentItemsetsAL = new ArrayList(itemsetsFinal.keySet());
            pw.println(trainingDataSizeCutOff);
            pw.flush();
            pw.println(numberOfPartitions);
            pw.flush();
            pw.println(partitionSize);
            pw.flush();
            pw.println(subPartitionSize);
            pw.flush();
            pw.println(numberOfSubPartitions);
            pw.flush();

            for (index1 = 0; index1 < currentItemsetsAL.size(); index1++) {
                itemset = (String) currentItemsetsAL.get(index1);
                pw.println(itemset);
                pw.flush();
                pw.println(itemsetsFinal.get(itemset));
                pw.flush();
                pw.println(itemsetsFinalPartition.get(itemset));
                pw.flush();
            }
            pw.close();
            fw.close();

        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
        currentItemsetsAL = null;
        System.out.println(Calendar.getInstance().getTime());
        System.out.println("FIRST PHASE MAX MEMORY USED: " + maxMemoryUsed + "bytes");
    }

    private void combineDFS(String itemset1, short[] tempItemset1, int index2, ArrayList tempOuterAL, boolean isOnlyClass) {
        boolean processFurther = false, removeSuperSets = false;
        String token = null, newItemset, itemset2 = null;
        int index, currentSingletonAddedInPartition;
        ArrayList nextItemsetsAL;
        float currentCutOff, fuzzyMembershipTotal = 0;
        short fuzzyMembership, fuzzyMembershipTemp;
        short[] tempItemset2, tempAL, tempAL1, twoItemsetsAL = new short[partitionSize];

        //create newItemset
        itemset2 = (String) tempOuterAL.get(index2);
        //CLASS Specific - Start
        if (isOnlyClass && itemset2.contains("CLASS")) {
            return;
        }
        //CLASS Specific - End
        newItemset = itemset1 + "," + itemset2;

        if (itemsetsFinalPartition.containsKey(newItemset)) {
            currentSingletonAddedInPartition = (Integer) itemsetsFinalPartition.get(newItemset);

            if (indexPartition != numberOfPartitions) {
                currentCutOff = (indexPartition - currentSingletonAddedInPartition + 1) * trainingDataSizeCutOff / numberOfPartitions;
            } else {
                currentCutOff = ((indexPartition - currentSingletonAddedInPartition) * trainingDataSizeCutOff / numberOfPartitions) + lastPartitionCutOff;
            }
        } else {
            //calculate cut off
            if (indexPartition != numberOfPartitions) {
                currentCutOff = trainingDataSizeCutOff / (float) numberOfPartitions;
            } else {
                currentCutOff = lastPartitionCutOff;
            }
        }

        //generate tidlist

        try {
            //System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + itemset2 + "..." + Calendar.getInstance().getTime());
            tempItemset2 = ByteStreamConverters.byteArrayToShortArray(StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(itemset2)));
            //System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + itemset2 + "..." + Calendar.getInstance().getTime());

            tempAL = tempItemset1;
            tempAL1 = tempItemset2;
            tempItemset1 = null;
            tempItemset2 = null;

            for (index = 0; index < partitionSize; index++) {
                fuzzyMembership = tempAL1[index];

                if (fuzzyMembership > 0) {
                    fuzzyMembershipTemp = tempAL[index];

                    if (fuzzyMembershipTemp < fuzzyMembership) {
                        fuzzyMembership = fuzzyMembershipTemp;
                    }
                    fuzzyMembershipTotal += (float) fuzzyMembership;
                    twoItemsetsAL[index] = fuzzyMembership;
                }
            }
            tempAL1 = null;
            tempAL = null;
            fuzzyMembershipTotal = fuzzyMembershipTotal / hundred;

            //System.out.println("System.gc() START 2: " + Calendar.getInstance().getTime());
            calculateMaxMemoryUsed();
            System.gc();
            //System.out.println("System.gc() END 2: " + Calendar.getInstance().getTime());

            //generate tidlist -- finish

            //update in itemsetsFinal if newItemset already present in itemsetsFinal

            if (itemsetsFinalPartition.containsKey(newItemset)) {
                fuzzyMembershipTotal += (Float) itemsetsFinal.get(newItemset);

                itemsetsFinal.remove(newItemset);
                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                //newItemsetUpdate.add(oldItemset);

                //freq itemsets would be expanded further

                if (fuzzyMembershipTotal >= currentCutOff) {

                    processFurther = true;
                    System.out.println("newItemset put in nextitemsets:" + newItemset + "____________________" + fuzzyMembershipTotal + " >>>>>>>>> " + currentCutOff);

                } else {
                    removeSuperSets = true;
                    System.out.println("newItemset NBE: " + newItemset + "~~~NEGATIVE BORDER~~~" + fuzzyMembershipTotal);
                }

            } //update in itemsetsFinal if newItemset already present in itemsetsFinal - finish
            else if (fuzzyMembershipTotal >= currentCutOff) {

                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                itemsetsFinalPartition.put(newItemset, indexPartition);
                processFurther = true;

                System.out.println("newItemset: " + newItemset + "---------------------------------------" + fuzzyMembershipTotal + " >>>>>>>>> " + currentCutOff);

            } else {
                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                itemsetsFinalPartition.put(newItemset, indexPartition);

                removeSuperSets = true;
                System.out.println("newItemset NBE: " + newItemset + "^^^NEGATIVE BORDER^^^" + fuzzyMembershipTotal);
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("currentCutOff --- fuzzyMembershipTotal: " + currentCutOff + "---" + fuzzyMembershipTotal);

        if (processFurther) {
            for (index2 = index2 + 1; index2 < tempOuterAL.size(); index2++) {
                combineDFS(newItemset, twoItemsetsAL, index2, tempOuterAL, isOnlyClass);
            }
        }

        twoItemsetsAL = null;
        //superset removal of NEGATIVE BORDER.
        if (removeSuperSets) {
            nextItemsetsAL = new ArrayList(itemsetsFinal.keySet());
            for (index = 0; index < nextItemsetsAL.size(); index++) {

                token = (String) nextItemsetsAL.get(index);

                if (token.contains(newItemset)) {
                    itemsetsFinal.remove(token);
                    itemsetsFinalPartition.remove(token);

                    System.out.println("\nremoveSuperSets: " + token);
                }
            }
            nextItemsetsAL = null;
        }
        calculateMaxMemoryUsed();
        System.gc();
    }

    private void initialize(String fuzzyTrainingDataFile, String parametersFile) {
        try {
            FileReader file_reader1 = new FileReader(parametersFile), file_reader;
            BufferedReader bufReader1 = new BufferedReader(file_reader1), bufReader;
            String line;

            support = Float.parseFloat(bufReader1.readLine());
            numberOfPartitions = Integer.parseInt(bufReader1.readLine());
            numberOfSubPartitions = Integer.parseInt(bufReader1.readLine());

            file_reader1.close();
            bufReader1.close();

            if (trainingDataSize == 0) {
                file_reader = new FileReader(fuzzyTrainingDataFile);
                bufReader = new BufferedReader(file_reader);

                while (true) {
                    line = bufReader.readLine();
                    if (line == null) {
                        break;
                    }
                    trainingDataSize++;
                }
                file_reader.close();
                bufReader.close();
            }

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        trainingDataSizeCutOff = ((float) trainingDataSize) * support;
        partitionSize = (int) Math.ceil((float) trainingDataSize / (float) numberOfPartitions);
        subPartitionSize = (int) Math.ceil((float) partitionSize / (float) numberOfSubPartitions);
        lastPartitionCutOff = ((float) (trainingDataSize - partitionSize * (numberOfPartitions - 1))) * support;

        System.out.println(Calendar.getInstance().getTime());
        System.out.println("trainingDataSize, trainingDataSizeCutOff, numberOfPartitions, partitionSize, support, lastPartitionCutOff, subPartitionSize, numberOfSubPartitions : " + trainingDataSize + "..." + trainingDataSizeCutOff + "..." + numberOfPartitions + "..." + partitionSize + "..." + support + "..." + lastPartitionCutOff + "..." + subPartitionSize + "..." + numberOfSubPartitions);
        return;
    }

    private void generatePartitionSingletons(BufferedReader bufReaderGlobal, int indexGlobal) {
        try {
            String line, singleton, fuzzyMembership;
            StringTokenizer st, st1;
            int index, indexCount = 0;
            short[] tempByteArray, tempNow, tempPrev;
            float newFuzzyMembership;
            HashMap nonDecisionItemSets1 = new HashMap();
            long oneEnd, oneStart, twoEnd, twoStart;

            itemsetsSubPartition = new HashMap();

            for (index = 0; index < subPartitionSize; index++) {
                line = bufReaderGlobal.readLine();
                indexCount++;
                if (indexCount % 10000 == 0) {
                    System.out.println("indexCount: " + indexCount + "..." + Calendar.getInstance().getTime());
                }
                if (line == null) {
                    break;
                }

                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    singleton = st1.nextToken();
                    fuzzyMembership = st1.nextToken();

                    if (!nonDecisionItemSets1.containsKey(singleton)) {
                        tempByteArray = new short[subPartitionSize];
                        //tempByteArray[index] = (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        tempByteArray[index] = (short) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        //System.out.println("singleton0 top: " + (short) Math.round(Float.parseFloat(fuzzyMembership) * hundred));
                        //System.out.println("singleton1 top: " + (byte) ((((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) /  hundred) * hundred));
                        nonDecisionItemSets1.put(singleton, tempByteArray);
                        tempByteArray = null;

                    } else {
                        tempByteArray = (short[]) nonDecisionItemSets1.get(singleton);
                        //tempByteArray[index] = (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        tempByteArray[index] = (short) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        nonDecisionItemSets1.remove(singleton);
                        nonDecisionItemSets1.put(singleton, tempByteArray);
                        tempByteArray = null;
                    }

                    //put counts in itemsetFinal
                    if (itemsetsFinal.containsKey(singleton)) {

                        //newFuzzyMembership = ((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) / hundred;
                        newFuzzyMembership = (float) ((short) Math.round(Float.parseFloat(fuzzyMembership) * hundred));
                        newFuzzyMembership += (Float) itemsetsFinal.get(singleton);

                        itemsetsFinal.remove(singleton);
                        itemsetsFinal.put(singleton, newFuzzyMembership);
                    } else if (!secondPhase) {
                        //itemsetsFinal.put(singleton, ((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) / hundred);
                        itemsetsFinal.put(singleton, (float) ((short) Math.round(Float.parseFloat(fuzzyMembership) * hundred)));
                        itemsetsFinalPartition.put(singleton, indexPartition);
                        //System.out.println("singleton0: " + Float.parseFloat(fuzzyMembership));
                        //System.out.println("singleton3: " + ((float) (Math.round(Float.parseFloat(fuzzyMembership) * tenThousand))) / tenThousand);
                        //System.out.println("singleton4: " + ((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) / hundred);
                    }
                    if (!itemsetsSubPartition.containsKey(singleton)) {
                        itemsetsSubPartition.put(singleton, indexGlobal);
                    }
                }
            }

            nonDecisionItemSetsAL = new ArrayList(itemsetsSubPartition.keySet());

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);

                //System.out.println("StringUtilsBzip2.COMPRESS/UNCOMPRESS START......." + singleton + "..." + Calendar.getInstance().getTime());

                tempNow = (short[]) nonDecisionItemSets1.get(singleton);
                nonDecisionItemSets1.remove(singleton);
                if (nonDecisionItemSets.containsKey(singleton)) {
                    twoStart = System.currentTimeMillis();
                    tempPrev = ByteStreamConverters.byteArrayToShortArray(StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton)));
                    twoEnd = System.currentTimeMillis();
                    //System.out.println("twoEnd - twoStart: " + (twoEnd - twoStart));
                    uncompressCount++;
                    uncompressTime += twoEnd - twoStart;
                    tempByteArray = null;

                    tempByteArray = new short[tempNow.length + tempPrev.length];
                    System.arraycopy(tempPrev, 0, tempByteArray, 0, tempPrev.length);
                    System.arraycopy(tempNow, 0, tempByteArray, tempPrev.length, tempNow.length);

                    tempNow = null;
                    tempPrev = null;
                    nonDecisionItemSets.remove(singleton);

                    oneStart = System.currentTimeMillis();
                    nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(ByteStreamConverters.shortArrayToByteArray(tempByteArray)));
                    oneEnd = System.currentTimeMillis();
                    //System.out.println("oneEnd - oneStart: " + (oneEnd - oneStart));
                    compressCount++;
                    compressTime += oneEnd - oneStart;
                    tempByteArray = null;
                } else {
                    if (((Integer) itemsetsSubPartition.get(singleton)).intValue() == 0) {
                        oneStart = System.currentTimeMillis();
                        nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(ByteStreamConverters.shortArrayToByteArray(tempNow)));
                        tempNow = null;

                        oneEnd = System.currentTimeMillis();
                        //System.out.println("oneEnd - oneStart: " + (oneEnd - oneStart));
                        compressCount++;
                        compressTime += oneEnd - oneStart;

                    } else {
                        tempByteArray = new short[subPartitionSize * (indexGlobal + 1)];
                        System.arraycopy(tempNow, 0, tempByteArray, subPartitionSize * indexGlobal, tempNow.length);
                        tempNow = null;

                        nonDecisionItemSets.remove(singleton);
                        nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(ByteStreamConverters.shortArrayToByteArray(tempByteArray)));
                        tempByteArray = null;
                    }
                }
                System.gc();
                //System.out.println("StringUtilsBzip2.COMPRESS/UNCOMPRESS END......." + singleton + "..." + Calendar.getInstance().getTime());
            }
            System.out.println("itemsetsSubPartition.size(): " + itemsetsSubPartition.size());
            nonDecisionItemSetsAL = null;
            nonDecisionItemSets1 = null;
            itemsetsSubPartition = null;
            System.gc();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    private void pruneSingletons() {
        String singleton;
        int index, currentSingletonAddedInPartition;
        float totalFuzzyMembership, currentCutOff;

        //pruning
        nonDecisionItemSetsAL = new ArrayList(nonDecisionItemSets.keySet());

        System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size() + "..." + Calendar.getInstance().getTime());

        for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
            singleton = (String) nonDecisionItemSetsAL.get(index);
            totalFuzzyMembership = ((Float) itemsetsFinal.get(singleton)) / hundred;

            //calculate cut off

            currentSingletonAddedInPartition = ((Integer) itemsetsFinalPartition.get(singleton)).intValue();
            if (indexPartition != numberOfPartitions) {
                currentCutOff = (indexPartition - currentSingletonAddedInPartition + 1) * trainingDataSizeCutOff / numberOfPartitions;
            } else {
                currentCutOff = ((indexPartition - currentSingletonAddedInPartition) * trainingDataSizeCutOff / numberOfPartitions) + lastPartitionCutOff;
            }

            if ((totalFuzzyMembership) < currentCutOff) {

                nonDecisionItemSets.remove(singleton);
                nonDecisionItemSetsAL.remove(index);
                index--;
                System.out.println("Pruned at start, nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " < " + currentCutOff);
                System.gc();
            } else {

                System.out.println("nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " >= " + currentCutOff);
            }
            //itemsetsFinal.remove(singleton);
            //itemsetsFinal.put(singleton, totalFuzzyMembership);
        }

        System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size() + "..." + Calendar.getInstance().getTime());
        nonDecisionItemSetsAL = null;
        System.out.println("pruning over");
        //pruning over
    }

    public void generateAssociationRulesSecondPhase(int fileSerialNumber, String attribute) {
        String line = null, itemset;
        int index1, partitionNumber, index;
        File file = new File("firstphasedata_" + fileSerialNumber + "_" + attribute + ".txt");
        FileReader fileReader;
        BufferedReader bufferedReader;
        ArrayList itemsetsFinalAL = null, itemsetsAlreadyDone = null;
        FileWriter fw = null;
        PrintWriter pw = null;
        float totalFuzzyMembership;
        FileReader file_readerGlobal = null;
        BufferedReader bufReaderGlobal = null;
        short[] tempItemset = null;
        maxMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        secondPhase = true;
        String fuzzyTrainingDataFile = null, singleton = null;

        if (attribute.equals("GLOBAL")) {
            fuzzyTrainingDataFile = "fuzzyTrainingData" + fileSerialNumber + ".txt";
        } else {
            fuzzyTrainingDataFile = "fuzzyTrainingData_" + fileSerialNumber + "_" + attribute + ".txt";
        }

        System.out.println("Initialize.... " + Calendar.getInstance().getTime());

        try {
            file_readerGlobal = new FileReader(fuzzyTrainingDataFile);
            bufReaderGlobal = new BufferedReader(file_readerGlobal);

            fw = new FileWriter("frequentItemsets_" + fileSerialNumber + "_" + attribute + ".txt", false);

            pw = new PrintWriter(fw, true);

            if (itemsetsFinal == null || itemsetsFinal.size() == 0) {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);

                trainingDataSizeCutOff = Float.parseFloat(bufferedReader.readLine());
                numberOfPartitions = Integer.parseInt(bufferedReader.readLine());
                partitionSize = Integer.parseInt(bufferedReader.readLine());
                subPartitionSize = Integer.parseInt(bufferedReader.readLine());
                numberOfSubPartitions = Integer.parseInt(bufferedReader.readLine());

                while (true) {
                    line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    itemsetsFinal.put(line, new Float(bufferedReader.readLine()));
                    itemsetsFinalPartition.put(line, new Integer(bufferedReader.readLine()));
                }
                fileReader.close();
                bufferedReader.close();
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        itemsetsFinalAL = new ArrayList(itemsetsFinal.keySet());
        System.out.println("Initialize end.... " + Calendar.getInstance().getTime());

        // 2nd phase        
        for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
            System.out.println("indexPartition............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

            //Output itemsets that were added in the current partition in the 1st phase
            System.out.println("Itemsets output.... " + Calendar.getInstance().getTime());

            for (index1 = 0; index1 < itemsetsFinalAL.size(); index1++) {
                itemset = (String) itemsetsFinalAL.get(index1);
                partitionNumber = ((Integer) itemsetsFinalPartition.get(itemset));

                if (partitionNumber == indexPartition) {
                    totalFuzzyMembership = ((Float) itemsetsFinal.get(itemset));

                    if ((totalFuzzyMembership) >= trainingDataSizeCutOff) {
                        pw.println(itemset + "\t" + (totalFuzzyMembership));
                        pw.flush();
                    }
                    itemsetsFinal.remove(itemset);
                    itemsetsFinalPartition.remove(itemset);
                    itemsetsFinalAL.remove(index1);
                    index1--;
                }
            }

            //Output itemsets that were added in the current partition in the 1st phase -- finish
            System.out.println("Itemsets output end.... " + Calendar.getInstance().getTime());
            System.out.println(itemsetsFinalAL.size() + "=" + itemsetsFinal.size() + "=" + itemsetsFinalPartition.size());

            if (itemsetsFinalAL.size() == 0) {
                System.out.println(itemsetsFinalAL.size() + "=" + itemsetsFinal.size() + "=" + itemsetsFinalPartition.size() + "..... breaking from main loop...");
                break;
            }
            calculateMaxMemoryUsed();
            System.gc();

            nonDecisionItemSets = new HashMap();
            itemsetsSubPartition = new HashMap();
            System.out.println("Singletons generating...." + Calendar.getInstance().getTime());
            for (index1 = 0; index1 < numberOfSubPartitions; index1++) {
                System.out.println("index1 Start........" + index1 + "..." + Calendar.getInstance().getTime());
                generatePartitionSingletons(bufReaderGlobal, index1);
                calculateMaxMemoryUsed();
                System.out.println("index1 END........" + index1 + "..." + Calendar.getInstance().getTime());
            }
            System.out.println("Singletons generation end...." + Calendar.getInstance().getTime());

            //update itemsets
            nonDecisionItemSetsAL = new ArrayList(nonDecisionItemSets.keySet());
            itemsetsAlreadyDone = new ArrayList();

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);

                try {
                    //System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + singleton + "..." + ((byte[]) nonDecisionItemSets.get(singleton)).length + "..." + Calendar.getInstance().getTime());
                    tempItemset = ByteStreamConverters.byteArrayToShortArray(StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton)));
                    //System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + singleton + "..." + tempItemset.length + "..." + Calendar.getInstance().getTime());
                } catch (IOException e) {
                    System.out.println("IO exception = " + e);
                    e.printStackTrace();
                }

                for (index1 = 0; index1 < itemsetsFinalAL.size(); index1++) {
                    itemset = (String) itemsetsFinalAL.get(index1);

                    if (!(itemsetsAlreadyDone.contains(itemset)) && itemset.contains(singleton) && itemset.indexOf(',') != -1) {
                        System.out.println("generateTidListSecondPhase...." + itemset + "..." + Calendar.getInstance().getTime() + "..." + index1 + "," + itemsetsFinalAL.size());
                        generateTidListSecondPhase(itemset, singleton, tempItemset);
                        itemsetsAlreadyDone.add(itemset);
                        System.out.println("generateTidListSecondPhase end...." + itemset + "..." + Calendar.getInstance().getTime() + "..." + index1 + "," + itemsetsFinalAL.size());
                    }
                }
            }

            //update itemsets -- finish
            nonDecisionItemSets = null;
            itemsetsAlreadyDone = null;

            System.gc();

            System.out.println("indexPartition FINISH............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

            System.out.println("itemsetsFinal:" + itemsetsFinal);
            System.out.println("\n\nitemsetsFinalPartition:" + itemsetsFinalPartition);
            System.out.println("\n\n\n");
        }
        try {
            file_readerGlobal.close();

            bufReaderGlobal.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println(Calendar.getInstance().getTime());
        System.out.println("SECOND PHASE MAX MEMORY USED: " + maxMemoryUsed + "bytes");
    }

    private void generateTidListSecondPhase(String itemset, String singleton, short[] tempItemset) {

        StringTokenizer st1;
        String singleton1;
        int index;
        short[] tempItemset1, tempItemset2, twoItemsetsAL;
        short fuzzyMembership, fuzzyMembershipTemp;
        float fuzzyMembershipTotal = 0;

        //generate tidlist

        try {
            st1 = new StringTokenizer(itemset, ",");
            singleton1 = st1.nextToken();

            if (singleton1.equals(singleton)) {
                tempItemset1 = tempItemset;
            } else {
                if (nonDecisionItemSets.containsKey(singleton1)) {
                    tempItemset1 = ByteStreamConverters.byteArrayToShortArray(StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton1)));
                } else {
                    return;
                }
            }

            while (st1.hasMoreTokens()) {
                twoItemsetsAL = new short[partitionSize];
                fuzzyMembershipTotal = 0;
                singleton1 = st1.nextToken();

                if (singleton1.equals(singleton)) {
                    tempItemset2 = tempItemset;
                } else {
                    if (nonDecisionItemSets.containsKey(singleton1)) {
                        tempItemset2 = ByteStreamConverters.byteArrayToShortArray(StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton1)));
                    } else {
                        return;
                    }
                }
                for (index = 0; index < partitionSize; index++) {
                    fuzzyMembership = tempItemset2[index];
                    if (fuzzyMembership > 0) {
                        fuzzyMembershipTemp = tempItemset1[index];

                        if (fuzzyMembershipTemp < fuzzyMembership) {
                            fuzzyMembership = fuzzyMembershipTemp;
                        }
                        fuzzyMembershipTotal += fuzzyMembership;
                        twoItemsetsAL[index] = fuzzyMembership;
                    }
                }
                tempItemset1 = twoItemsetsAL;
                tempItemset2 = null;
                calculateMaxMemoryUsed();
                System.gc();
            }
            //generate tidlist -- finish

            fuzzyMembershipTotal += ((Float) itemsetsFinal.get(itemset)) / hundred;

            itemsetsFinal.remove(itemset);
            itemsetsFinal.put(itemset, fuzzyMembershipTotal);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return;
    }

    public void generateCARs(int fileSerialNumber, String attribute, float confidence) {
        String fuzzyTrainingDataFile = "frequentItemsets_" + fileSerialNumber + "_" + attribute + ".txt";

        String token = null, token1 = null, itemset = null, decisionClass = null, antecedent = null, precedent = null;
        StringBuffer itemsetNew = null;
        PrintWriter pw = null;
        BufferedReader bufReader = null;
        HashMap itemsetsMapOld = new HashMap(), itemsetsMapNew = new HashMap();
        String line = null;
        StringTokenizer st = null, st1 = null;
        float precedentCount, antecedentCount;
        ArrayList singletonsAL = new ArrayList(), tempAL = null, itemsAL = new ArrayList();
        int index, index1, lastIndex;

        try {
            bufReader = new BufferedReader(new FileReader(fuzzyTrainingDataFile));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                token = st.nextToken("\t");
                itemsetsMapOld.put(token, Float.parseFloat(st.nextToken()));
                st1 = new StringTokenizer(token, ",");
                while (st1.hasMoreTokens()) {
                    token1 = st1.nextToken();
                    if (!singletonsAL.contains(token1)) {
                        singletonsAL.add(token1);
                    }
                }
            }
            bufReader.close();
            System.out.println(singletonsAL);
            Collections.sort(singletonsAL, String.CASE_INSENSITIVE_ORDER);
            System.out.println("\n\n\n\n" + singletonsAL);


            bufReader = new BufferedReader(new FileReader("facme.csv"));

            bufReader.readLine();
            decisionClassLabel = bufReader.readLine();
            bufReader.close();

            System.out.println("decisionClassLabel: " + decisionClassLabel);

            System.out.println("itemsetsMapOld: " + itemsetsMapOld);
            System.out.println("itemsetsMapOld.size(): " + itemsetsMapOld.size());

            tempAL = new ArrayList(itemsetsMapOld.keySet());

            for (index = 0; index < tempAL.size(); index++) {

                itemset = (String) tempAL.get(index);
                st = new StringTokenizer(itemset, ",");
                itemsAL.clear();
                while (st.hasMoreTokens()) {
                    itemsAL.add(st.nextToken());
                }
                itemsetNew = new StringBuffer();
                decisionClass = null;
                for (index1 = 0; index1 < singletonsAL.size(); index1++) {
                    token = (String) singletonsAL.get(index1);
                    if (itemsAL.contains(token) && token.contains(decisionClassLabel)) {
                        decisionClass = token;
                    } else if (itemsAL.contains(token)) {
                        itemsetNew.append(token + " ");
                    }
                }
                /*for (index1 = 0; index1 < singletonsAL.size(); index1++) {
                token = (String) singletonsAL.get(index1);
                for(index2 = 0; index2 < itemsAL.size(); index2++) {
                item = (String) itemsAL.get(index2);
                if (item.equals(token) && token.contains(decisionClassLabel)) {
                decisionClass = token;
                } else if (item.equals(token)) {
                itemsetNew.append(token + " ");
                }
                }
                }*/
                if (decisionClass != null) {
                    itemsetNew.append(decisionClass);
                }
                itemsetsMapNew.put(itemsetNew.toString().trim(), itemsetsMapOld.get(itemset));
            }

            tempAL = new ArrayList(itemsetsMapNew.keySet());
            pw = new PrintWriter(new FileWriter("CAR_" + fileSerialNumber + "_" + attribute + ".txt"));
            System.out.println("itemsetsMapNew: " + itemsetsMapNew);
            System.out.println("itemsetsMapNew.size(): " + itemsetsMapNew.size());

            for (index = 0; index < tempAL.size(); index++) {
                itemset = (String) tempAL.get(index);
                System.out.println("itemsetNew: " + itemset);
                if (!itemset.contains(decisionClassLabel)) {
                    continue;
                } else {
                    lastIndex = itemset.lastIndexOf(' ');
                    if (lastIndex == -1) {
                        System.out.println("SINGLE itemsetNew: " + itemset);
                        continue;
                    }
                    precedent = itemset.substring(0, lastIndex);
                    antecedent = itemset.substring(lastIndex);
                    System.out.println("precedent: " + precedent);

                    precedentCount = (Float) itemsetsMapNew.get(precedent);
                    antecedentCount = (Float) itemsetsMapNew.get(itemset);
                    pw.println(precedent + " " + precedentCount + " ~~~~~ " + antecedent + " " + antecedentCount);

                }
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public void generateLocalCARs(int fileSerialNumber, String attribute, float confidence) {
        String fuzzyTrainingDataFile = "frequentItemsets_" + fileSerialNumber + "_" + attribute + ".txt";
        PrintWriter pw = null;
        BufferedReader br = null;
        String strLine = null;
        StringTokenizer st = null;

        try {
            br = new BufferedReader(new FileReader(fuzzyTrainingDataFile));
            pw = new PrintWriter(new FileWriter("CAR_" + fileSerialNumber + "_" + attribute + ".txt"));
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains("CLASS") && strLine.contains(",")) {
                    //System.out.println("strLine: " + strLine);
                    st = new StringTokenizer(strLine);
                    String str = st.nextToken();
                    StringTokenizer str1 = new StringTokenizer(str, ",");
                    String labelClass = str1.nextToken();
                    //System.out.println("labelClass: " + labelClass);
                    while (str1.hasMoreTokens()) {
                        pw.print(str1.nextToken() + " ");
                    }
                    String prob = st.nextToken();
                    pw.println(prob + "~~~~~" + labelClass + " " + prob);
                }
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
}
