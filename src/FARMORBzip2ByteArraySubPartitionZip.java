
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

public class FARMORBzip2ByteArraySubPartitionZip {

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
    private float hundred = (float) 100;
    private float tenThousand = (float) 10000;
    private ArrayList nonDecisionItemSetsAL = null;
    private HashMap itemsetsFinal = new HashMap();
    private HashMap itemsetsFinalPartition = new HashMap();
    private HashMap itemsetsSubPartition = null;
    private ArrayList partitionRepeatItemsetAL = null;
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
            BufferedReader buf_reader = new BufferedReader(file_reader);


            trainingFilename = buf_reader.readLine();
            decisionClassLabel = buf_reader.readLine();

            System.out.println("decisionClassLabel: " + decisionClassLabel);

            while (true) {
                line = buf_reader.readLine();
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

            buf_reader.close();
            file_reader.close();

            file = new File(trainingFilename);
            file_reader = new FileReader(file);
            buf_reader = new BufferedReader(file_reader);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                trainingData.add(line);
            }

            //System.out.println("trainingData: " + trainingData);

            buf_reader.close();
            file_reader.close();


            for (index = 0; index < classType.size(); index++) {
                label = (String) classLabel.get(index);
                type = (String) classType.get(index);
                if (type.equalsIgnoreCase("num")) {
                    file = new File((String) classFilename.get(label));

                    file_reader = new FileReader(file);
                    buf_reader = new BufferedReader(file_reader);

                    line = buf_reader.readLine();

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
                        line = buf_reader.readLine();
                        if (line == null) {
                            break;
                        }
                        commaIndex = line.indexOf(',');

                        numValueTemp.put(line.substring(0, commaIndex), line.substring(commaIndex + 1));
                    }

                    numValue.put(label, numValueTemp);

                    //System.out.println("numValueTemp: " + numValueTemp);
                    //System.out.println("numValue: " + numValue);

                    buf_reader.close();
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

    public void generateAssociationRules(int fileSerialNumber, String attribute, String parametersFile) {
        int index1, index2;
        String itemset1 = null;
        byte[] tempItemset1 = null;
        BufferedReader buf_readerGlobal = null;
        String fuzzyTrainingDataFile = null;
        maxMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (attribute.equals("GLOBAL")) {
            fuzzyTrainingDataFile = "fuzzyTrainingData" + fileSerialNumber + ".txt";
        } else {
            fuzzyTrainingDataFile = "fuzzyTrainingData_" + fileSerialNumber + "_" + attribute + ".txt";
        }

        initialize(fuzzyTrainingDataFile, parametersFile);
        calculateMaxMemoryUsed();

        try {
            System.out.println("file_readerGlobal END buf_readerGlobal START ........." + Calendar.getInstance().getTime());

            buf_readerGlobal = new BufferedReader(new FileReader(fuzzyTrainingDataFile));

            System.out.println("buf_readerGlobal END........." + Calendar.getInstance().getTime());
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        // 1st phase        
        for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
            System.out.println("indexPartition, partitionRepeatItemsetAL................................." + indexPartition + "..." + partitionRepeatItemsetAL + "..." + Calendar.getInstance().getTime());

            nonDecisionItemSets = new HashMap();
            itemsetsSubPartition = new HashMap();
            partitionRepeatItemsetAL = new ArrayList();

            for (index1 = 0; index1 < numberOfSubPartitions; index1++) {
                System.out.println("index1 Start........" + index1 + "..." + Calendar.getInstance().getTime());
                generatePartitionSingletons(buf_readerGlobal, index1);
                calculateMaxMemoryUsed();
                System.out.println("index1 END........" + index1 + "..." + Calendar.getInstance().getTime());
            }
            pruneSingletons();
            calculateMaxMemoryUsed();
            System.out.println("singletons generated");

            //generate itemsets

            nextItemsets = nonDecisionItemSets;
            nonDecisionItemSets = null;
            itemsetsSubPartition = null;

            for (countK = 2; nextItemsets.size() > 1; countK++) {
                currentItemsets = nextItemsets;
                System.out.println("Next iteration started.............................................." + countK + "..." + Calendar.getInstance().getTime());
                currentItemsetsAL = new ArrayList(currentItemsets.keySet());
                Collections.sort(currentItemsetsAL, String.CASE_INSENSITIVE_ORDER);

                nextItemsets = new HashMap();
                
                for (index1 = 0; index1 < currentItemsetsAL.size() - 1; index1++) {
                    try {
                        itemset1 = (String) currentItemsetsAL.get(index1);

                        //System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + itemset1 + "..." + ((byte[]) currentItemsets.get(itemset1)).length + "..." + Calendar.getInstance().getTime());
                        tempItemset1 = StringUtilsBzip2.uncompress((byte[]) currentItemsets.get(itemset1));
                        //System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + itemset1 + "..." + tempItemset1.length + "..." + Calendar.getInstance().getTime());
                    } catch (IOException e) {
                        System.out.println("IO exception = " + e);
                        e.printStackTrace();
                    }

                    for (index2 = index1 + 1; index2 < currentItemsetsAL.size(); index2++) {
                        //System.out.println("Start~~~~" + Calendar.getInstance().getTime());
                        createNItemsets(itemset1, (String) currentItemsetsAL.get(index2), tempItemset1);
                        //System.out.println("End~~~~" + Calendar.getInstance().getTime());
                    }
                    tempItemset1 = null;

                    currentItemsets.remove(itemset1);
                    System.out.println("Removed from current itemsetsAL: " + (String) currentItemsetsAL.get(index1) + "..." + index1 + "..." + currentItemsetsAL.size() + "..." + Calendar.getInstance().getTime());
                    currentItemsetsAL.remove(index1);
                    itemset1 = null;
                    calculateMaxMemoryUsed();
                    System.gc();
                    index1--;

                }
                currentItemsets = null;
                currentItemsetsAL = null;
                System.gc();
                System.out.println("Next iteration FINISH.............................................." + countK + "..." + Calendar.getInstance().getTime());
            }
            partitionRepeatItemsetAL = null;
            System.out.println("indexPartition FINISH............................................." + indexPartition + "..." + Calendar.getInstance().getTime());
            System.out.println("itemsetsFinal:" + itemsetsFinal);
            System.out.println("\n\nitemsetsFinalPartition:" + itemsetsFinalPartition);
            System.out.println("\n\n\n");
        }

        System.out.println("Full Avg time: " + fullTime + "..." + fullCount + "..." + ((double) fullTime / (double) fullCount));
        System.out.println("Compress Avg time: " + compressTime + "..." + compressCount + "..." + ((double) compressTime / (double) compressCount));
        System.out.println("Uncompress Avg time: " + uncompressTime + "..." + uncompressCount + "..." + ((double) uncompressTime / (double) uncompressCount));

        try {
            buf_readerGlobal.close();
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

    private void createNItemsets(String itemset1, String itemset2, byte[] tempItemset1) {
        boolean mismatch = false, returnMethod, newItemsetPresentInItemsetFinal = false, isPartitionRepeatItemset = false, removeSuperSets = false;
        StringTokenizer st;
        HashMap tempHashMap = new HashMap(), tempHashMap1;
        String token = null, newItemset, oldItemset = null;
        int index, currentSingletonAddedInPartition;
        ArrayList nextItemsetsAL;
        float currentCutOff, fuzzyMembershipTotal = 0;
        byte fuzzyMembership, fuzzyMembershipTemp;
        byte[] tempItemset2, tempAL, tempAL1, twoItemsetsAL = new byte[partitionSize];
        long fullStart, fullEnd, twoStart = 0, twoEnd = 0, oneStart = 0, oneEnd = 0;

        //create newItemset
        fullStart = System.currentTimeMillis();
        st = new StringTokenizer(itemset2, ",");
        while (st.hasMoreTokens()) {
            tempHashMap.put(st.nextToken(), null);
        }

        st = new StringTokenizer(itemset1, ",");
        while (st.hasMoreTokens()) {
            token = st.nextToken();

            if (mismatch && !tempHashMap.containsKey(token)) {
                tempHashMap = null;
                return;
            } else if (!tempHashMap.containsKey(token)) {
                mismatch = true;
            }
        }
        newItemset = itemset1;
        tempHashMap = new HashMap();

        st = new StringTokenizer(itemset1, ",");
        while (st.hasMoreTokens()) {
            tempHashMap.put(st.nextToken(), null);
        }
        st = new StringTokenizer(itemset2, ",");
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (!tempHashMap.containsKey(token)) {
                System.out.println();
                System.out.println("token: " + token);
                newItemset = newItemset + "," + token;
                tempHashMap.put(token, null);
                break;
            }
        }
        System.out.println("newItemset: " + newItemset);
        System.out.println("itemset1: " + itemset1);
        System.out.println("itemset2: " + itemset2);
        //create newItemset - finish
        //check if new itemset in itemsetsFinal
        nextItemsetsAL = new ArrayList(itemsetsFinal.keySet());
        for (index = 0; index < nextItemsetsAL.size(); index++) {
            returnMethod = true;
            st = new StringTokenizer((String) nextItemsetsAL.get(index), ",");
            if (st.countTokens() != countK) {
                continue;
            }

            while (st.hasMoreTokens()) {
                token = st.nextToken();
                if (!tempHashMap.containsKey(token)) {
                    returnMethod = false;
                    break;
                }
            }
            if (returnMethod) {
                newItemsetPresentInItemsetFinal = true;
                oldItemset = (String) nextItemsetsAL.get(index);
                System.out.println("newItemsetPresentInItemsetFinal: " + newItemsetPresentInItemsetFinal);
                break;
            }
        }
        nextItemsetsAL = null;
        //check if new itemset in itemsetsFinal - finish

        //check if new itemset in newItemsetUpdate

        if (newItemsetPresentInItemsetFinal) {
            for (index = 0; index < partitionRepeatItemsetAL.size(); index++) {
                returnMethod = true;
                isPartitionRepeatItemset = false;

                st = new StringTokenizer((String) partitionRepeatItemsetAL.get(index), ",");
                if (st.countTokens() != countK) {
                    continue;
                }
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    if (!tempHashMap.containsKey(token)) {
                        returnMethod = false;
                        break;
                    }
                }
                if (returnMethod) {
                    isPartitionRepeatItemset = true;
                    System.out.println("isPartitionRepeatItemset: " + oldItemset + ";" + isPartitionRepeatItemset);
                    System.out.println(newItemset + " already evaluated in current partition");
                    return;
                }
            }
            //calculate cut off
            currentSingletonAddedInPartition = (Integer) itemsetsFinalPartition.get(oldItemset);
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
        partitionRepeatItemsetAL.add(newItemset);
        //check if new itemset in newItemsetUpdate - finish
        nextItemsetsAL = null;
        //check if newItemset already in nextItemsets - finish
        //generate tidlist

        try {
            twoStart = System.currentTimeMillis();
            tempItemset2 = StringUtilsBzip2.uncompress((byte[]) currentItemsets.get(itemset2));
            twoEnd = System.currentTimeMillis();
            System.out.println("twoEnd - twoStart: " + (twoEnd - twoStart));
            uncompressCount++;
            uncompressTime += twoEnd - twoStart;

            tempAL = tempItemset1;
            tempAL1 = tempItemset2;
            tempItemset1 = null;
            tempItemset2 = null;
            fuzzyMembershipTotal = 0;

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
            //fuzzyMembershipTotal = fuzzyMembershipTotal / hundred;
            calculateMaxMemoryUsed();
            System.gc();

            //generate tidlist -- finish
            //update in itemsetsFinal if newItemset already present in itemsetsFinal

            //if (itemsetFinalUpdate && newItemsetPresentInItemsetFinal) {
            if (newItemsetPresentInItemsetFinal && !isPartitionRepeatItemset) {
                fuzzyMembershipTotal += (Float) itemsetsFinal.get(oldItemset);
                itemsetsFinal.remove(oldItemset);
                itemsetsFinal.put(oldItemset, fuzzyMembershipTotal);
                
                //freq itemsets would be expanded further

                if ((fuzzyMembershipTotal / hundred) >= currentCutOff) {
                    //System.out.println("StringUtilsBzip2.COMPRESS1 START......." + newItemset + "..." + Calendar.getInstance().getTime());
                    oneStart = System.currentTimeMillis();
                    nextItemsets.put(newItemset, StringUtilsBzip2.compress(twoItemsetsAL));
                    //System.out.println("StringUtilsBzip2.COMPRESS1 END......." + newItemset + "..." + Calendar.getInstance().getTime());
                    oneEnd = System.currentTimeMillis();
                    System.out.println("oneEnd - oneStart: " + (oneEnd - oneStart));
                    compressCount++;
                    compressTime += oneEnd - oneStart;
                    System.out.println("newItemset put in nextitemsets:" + newItemset + "____________________" + (fuzzyMembershipTotal / hundred) + " >>>>>>>>> " + currentCutOff);
                } else {
                    removeSuperSets = true;
                    System.out.println("newItemset NBE: " + newItemset + "~~~NEGATIVE BORDER~~~" + (fuzzyMembershipTotal / hundred));
                }
                twoItemsetsAL = null;

                System.out.println("oldItemset: " + oldItemset + "==========================" + itemsetsFinal.get(oldItemset) + "============" + (fuzzyMembershipTotal / hundred));

            } //update in itemsetsFinal if newItemset already present in itemsetsFinal - finish
            else if ((fuzzyMembershipTotal / hundred) >= currentCutOff) {

                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                itemsetsFinalPartition.put(newItemset, indexPartition);

                //System.out.println("StringUtilsBzip2.COMPRESS START......." + newItemset + "..." + Calendar.getInstance().getTime());
                oneStart = System.currentTimeMillis();
                nextItemsets.put(newItemset, StringUtilsBzip2.compress(twoItemsetsAL));
                oneEnd = System.currentTimeMillis();
                System.out.println("oneEnd - oneStart: " + (oneEnd - oneStart));
                compressCount++;
                compressTime += oneEnd - oneStart;
                //System.out.println("StringUtilsBzip2.COMPRESS END......." + newItemset + "..." + Calendar.getInstance().getTime());

                //newItemsetUpdate.add(newItemset);
                twoItemsetsAL = null;
                System.out.println("newItemset: " + newItemset + "---------------------------------------" + (fuzzyMembershipTotal / hundred) + " >>>>>>>>> " + currentCutOff);

            } else {
                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                itemsetsFinalPartition.put(newItemset, indexPartition);

                //newItemsetUpdate.add(newItemset);

                twoItemsetsAL = null;
                removeSuperSets = true;
                System.out.println("newItemset NBE: " + newItemset + "^^^NEGATIVE BORDER^^^" + (fuzzyMembershipTotal / hundred));
            }
            calculateMaxMemoryUsed();
            System.gc();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("currentCutOff --- (fuzzyMembershipTotal / hundred): " + currentCutOff + "---" + (fuzzyMembershipTotal / hundred));

        //superset removal of NB.

        if (removeSuperSets) {
            nextItemsetsAL = new ArrayList(itemsetsFinal.keySet());
            for (index = 0; index < nextItemsetsAL.size(); index++) {
                returnMethod = true;

                st = new StringTokenizer((String) nextItemsetsAL.get(index), ",");
                if (st.countTokens() <= countK) {
                    continue;
                }
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    if (!tempHashMap.containsKey(token)) {
                        returnMethod = false;
                        break;
                    }
                }
                if (returnMethod) {
                    itemsetsFinal.remove(nextItemsetsAL.get(index));
                    itemsetsFinalPartition.remove(nextItemsetsAL.get(index));

                    System.out.println("\nremoveSuperSets: " + nextItemsetsAL.get(index));
                }
            }
            nextItemsetsAL = null;
            tempHashMap = null;
            calculateMaxMemoryUsed();
            System.gc();
        }
        tempHashMap = null;
        fullEnd = System.currentTimeMillis();
        System.out.println("fullEnd - fullStart: " + (fullEnd - fullStart - (twoEnd - twoStart) - (oneEnd - oneStart)));
        fullCount++;
        fullTime += fullEnd - fullStart - (twoEnd - twoStart) - (oneEnd - oneStart);
    }

    private void initialize(String fuzzyTrainingDataFile, String parametersFile) {
        try {
            FileReader file_reader1 = new FileReader(parametersFile), file_reader;
            BufferedReader buf_reader1 = new BufferedReader(file_reader1), buf_reader;
            String line;

            support = Float.parseFloat(buf_reader1.readLine());
            numberOfPartitions = Integer.parseInt(buf_reader1.readLine());
            numberOfSubPartitions = Integer.parseInt(buf_reader1.readLine());

            file_reader1.close();
            buf_reader1.close();

            if (trainingDataSize == 0) {
                file_reader = new FileReader(fuzzyTrainingDataFile);
                buf_reader = new BufferedReader(file_reader);

                while (true) {
                    line = buf_reader.readLine();
                    if (line == null) {
                        break;
                    }
                    trainingDataSize++;
                }
                file_reader.close();
                buf_reader.close();
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

    private void generatePartitionSingletons(BufferedReader buf_readerGlobal, int indexGlobal) {
        try {
            String line, singleton, fuzzyMembership;
            StringTokenizer st, st1;
            int index, indexCount = 0;
            byte[] tempByteArray, tempNow, tempPrev;
            float newFuzzyMembership;
            HashMap nonDecisionItemSets1 = new HashMap();
            long oneEnd, oneStart, twoEnd, twoStart;

            itemsetsSubPartition = new HashMap();

            for (index = 0; index < subPartitionSize; index++) {
                line = buf_readerGlobal.readLine();
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
                        tempByteArray = new byte[subPartitionSize];
                        //tempByteArray[index] = (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        tempByteArray[index] = (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        System.out.println("singleton0 top: " + (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred));
                        //System.out.println("singleton1 top: " + (byte) ((((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) /  hundred) * hundred));
                        nonDecisionItemSets1.put(singleton, tempByteArray);
                        tempByteArray = null;

                    } else {
                        tempByteArray = (byte[]) nonDecisionItemSets1.get(singleton);
                        //tempByteArray[index] = (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        tempByteArray[index] = (byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred);
                        nonDecisionItemSets1.remove(singleton);
                        nonDecisionItemSets1.put(singleton, tempByteArray);
                        tempByteArray = null;
                    }
                    //put counts in itemsetFinal
                    if (itemsetsFinal.containsKey(singleton)) {
                        //newFuzzyMembership = ((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) / hundred;
                        newFuzzyMembership = (float) ((byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred));
                        newFuzzyMembership += (Float) itemsetsFinal.get(singleton);

                        itemsetsFinal.remove(singleton);
                        itemsetsFinal.put(singleton, newFuzzyMembership);
                    } else if (!secondPhase) {
                        //itemsetsFinal.put(singleton, ((float) (Math.round(Float.parseFloat(fuzzyMembership) * hundred))) / hundred);
                        itemsetsFinal.put(singleton, (float) ((byte) Math.round(Float.parseFloat(fuzzyMembership) * hundred)));
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
            System.out.println("itemsetsFinal: " + itemsetsFinal);
            System.out.println("itemsetsFinalPartition: " + itemsetsFinalPartition);
            nonDecisionItemSetsAL = new ArrayList(itemsetsSubPartition.keySet());

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);

                //System.out.println("StringUtilsBzip2.COMPRESS/UNCOMPRESS START......." + singleton + "..." + Calendar.getInstance().getTime());

                tempNow = (byte[]) nonDecisionItemSets1.get(singleton);
                nonDecisionItemSets1.remove(singleton);
                if (nonDecisionItemSets.containsKey(singleton)) {
                    twoStart = System.currentTimeMillis();
                    tempPrev = StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton));
                    twoEnd = System.currentTimeMillis();
                    //System.out.println("twoEnd - twoStart: " + (twoEnd - twoStart));
                    uncompressCount++;
                    uncompressTime += twoEnd - twoStart;
                    tempByteArray = null;

                    tempByteArray = new byte[tempNow.length + tempPrev.length];
                    System.arraycopy(tempPrev, 0, tempByteArray, 0, tempPrev.length);
                    System.arraycopy(tempNow, 0, tempByteArray, tempPrev.length, tempNow.length);

                    tempNow = null;
                    tempPrev = null;
                    nonDecisionItemSets.remove(singleton);

                    oneStart = System.currentTimeMillis();
                    nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(tempByteArray));
                    oneEnd = System.currentTimeMillis();
                    //System.out.println("oneEnd - oneStart: " + (oneEnd - oneStart));
                    compressCount++;
                    compressTime += oneEnd - oneStart;
                    tempByteArray = null;
                } else {
                    if (((Integer) itemsetsSubPartition.get(singleton)).intValue() == 0) {
                        oneStart = System.currentTimeMillis();
                        nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(tempNow));
                        tempNow = null;

                        oneEnd = System.currentTimeMillis();
                        //System.out.println("oneEnd - oneStart: " + (oneEnd - oneStart));
                        compressCount++;
                        compressTime += oneEnd - oneStart;

                    } else {
                        tempByteArray = new byte[subPartitionSize * (indexGlobal + 1)];
                        System.arraycopy(tempNow, 0, tempByteArray, subPartitionSize * indexGlobal, tempNow.length);
                        tempNow = null;

                        nonDecisionItemSets.remove(singleton);
                        nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(tempByteArray));
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
            totalFuzzyMembership = (Float) itemsetsFinal.get(singleton);

            //calculate cut off

            currentSingletonAddedInPartition = (Integer) itemsetsFinalPartition.get(singleton);
            if (indexPartition != numberOfPartitions) {
                currentCutOff = (indexPartition - currentSingletonAddedInPartition + 1) * trainingDataSizeCutOff / numberOfPartitions;
            } else {
                currentCutOff = ((indexPartition - currentSingletonAddedInPartition) * trainingDataSizeCutOff / numberOfPartitions) + lastPartitionCutOff;
            }
            if ((totalFuzzyMembership / hundred) < currentCutOff) {
                nonDecisionItemSets.remove(singleton);
                nonDecisionItemSetsAL.remove(index);
                index--;
                System.out.println("Pruned at start, nonDecisionItemSets, currentCutOff: " + singleton + ", " + (totalFuzzyMembership / hundred) + " < " + currentCutOff);
                System.gc();
            } else {
                System.out.println("nonDecisionItemSets, currentCutOff: " + singleton + ", " + (totalFuzzyMembership / hundred) + " >= " + currentCutOff);
            }
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
        BufferedReader buf_readerGlobal = null;
        byte[] tempItemset = null;
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
            buf_readerGlobal = new BufferedReader(file_readerGlobal);

            fw = new FileWriter("frequentItemsets_" + fileSerialNumber + "_" + attribute + ".txt", false);

            pw = new PrintWriter(fw, true);

            if (itemsetsFinal == null || itemsetsFinal.isEmpty()) {
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
                    System.out.println("itemset: " + itemset);
                    totalFuzzyMembership = (Float) itemsetsFinal.get(itemset);
                    if ((totalFuzzyMembership / hundred) >= trainingDataSizeCutOff) {
                        pw.println(itemset + "\t" + ((totalFuzzyMembership / hundred)));
                        pw.flush();
                        System.out.println("(totalFuzzyMembership / hundred) >= trainingDataSizeCutOff: " + (totalFuzzyMembership / hundred)  + " >= " + trainingDataSizeCutOff);
                    }
                    else {
                        System.out.println("(totalFuzzyMembership / hundred) < trainingDataSizeCutOff: " + (totalFuzzyMembership / hundred)  + " < " + trainingDataSizeCutOff);
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

            if (itemsetsFinalAL.isEmpty()) {
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
                generatePartitionSingletons(buf_readerGlobal, index1);
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
                    System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + singleton + "..." + ((byte[]) nonDecisionItemSets.get(singleton)).length + "..." + Calendar.getInstance().getTime());
                    tempItemset = StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton));
                    System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + singleton + "..." + tempItemset.length + "..." + Calendar.getInstance().getTime());
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
            System.out.println("indexPartition FINISH............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

            System.out.println("itemsetsFinal:" + itemsetsFinal);
            System.out.println("\n\nitemsetsFinalPartition:" + itemsetsFinalPartition);
            System.out.println("\n\n\n");
        }
        try {
            file_readerGlobal.close();

            buf_readerGlobal.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println(Calendar.getInstance().getTime());
        System.out.println("SECOND PHASE MAX MEMORY USED: " + maxMemoryUsed + "bytes");
    }

    private void generateTidListSecondPhase(String itemset, String singleton, byte[] tempItemset) {

        StringTokenizer st1;
        String singleton1;
        int index;
        byte[] tempItemset1, tempItemset2, twoItemsetsAL;
        byte fuzzyMembership, fuzzyMembershipTemp;
        float fuzzyMembershipTotal = 0;

        //generate tidlist

        try {
            st1 = new StringTokenizer(itemset, ",");
            singleton1 = st1.nextToken();

            if (singleton1.equals(singleton)) {
                tempItemset1 = tempItemset;
            } else {
                if (nonDecisionItemSets.containsKey(singleton1)) {
                    tempItemset1 = StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton1));
                } else {
                    return;
                }
            }
            while (st1.hasMoreTokens()) {
                twoItemsetsAL = new byte[partitionSize];
                fuzzyMembershipTotal = 0;
                singleton1 = st1.nextToken();

                if (singleton1.equals(singleton)) {
                    tempItemset2 = tempItemset;
                } else {
                    if (nonDecisionItemSets.containsKey(singleton1)) {
                        tempItemset2 = StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton1));
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
                        fuzzyMembershipTotal += (float) fuzzyMembership;
                        twoItemsetsAL[index] = fuzzyMembership;
                    }
                }
                tempItemset1 = twoItemsetsAL;
                tempItemset2 = null;
                calculateMaxMemoryUsed();
                System.gc();
            }
            //generate tidlist -- finish

            //fuzzyMembershipTotal += (Float) itemsetsFinal.get(itemset) / hundred;
            fuzzyMembershipTotal += (Float) itemsetsFinal.get(itemset);
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
        BufferedReader buf_reader = null;
        HashMap itemsetsMapOld = new HashMap(), itemsetsMapNew = new HashMap();
        String line = null;
        StringTokenizer st = null, st1 = null;
        float precedentCount, antecedentCount;
        ArrayList singletonsAL = new ArrayList(), tempAL = null, itemsAL = new ArrayList();
        int index, index1, lastIndex;

        try {
            buf_reader = new BufferedReader(new FileReader(fuzzyTrainingDataFile));

            while (true) {
                line = buf_reader.readLine();
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
            buf_reader.close();
            System.out.println(singletonsAL);
            Collections.sort(singletonsAL, String.CASE_INSENSITIVE_ORDER);
            System.out.println("\n\n\n\n" + singletonsAL);


            buf_reader = new BufferedReader(new FileReader("facme.csv"));

            buf_reader.readLine();
            decisionClassLabel = buf_reader.readLine();
            buf_reader.close();

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
                    if ((float) antecedentCount / (float) precedentCount >= confidence) {
                        pw.println(precedent + " " + precedentCount + " ~~~~~ " + antecedent + " " + antecedentCount);
                    }
                }
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
}
