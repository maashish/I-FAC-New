
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

public class FARMORBzip2ByteArraySubPartitionZipLocal {

    private ArrayList classLabel = new ArrayList();
    private ArrayList classType = new ArrayList();
    private HashMap classFilename = new HashMap();
    private ArrayList trainingData = new ArrayList();
    private HashMap numAttribute = new HashMap();
    private HashMap numValue = new HashMap();
    private String decisionClassLabel = null;
    private ArrayList decisionClassesAL = new ArrayList();
    private HashMap decisionClassesCountMap = new HashMap();
    
    private String trainingFilename = null;
    private String testFilename = null;
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
    private float hundred = 100;
    private ArrayList nonDecisionItemSetsAL = null;
    private HashMap itemsetsFinal = null;
    private HashMap itemsetsFinalPartition = null;
    private HashMap itemsetsSubPartition = null;
    private ArrayList newItemsetUpdate = null;
    private HashMap nextItemsets = null;
    private HashMap currentItemsets = null;
    private ArrayList currentItemsetsAL = null;
    private HashMap nonDecisionItemSets = null;
    private boolean secondPhase = false;

    public void ReadFiles() {

        File file = new File("facme.csv");
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        StringTokenizer st = null;

        String label, type, filename = null, line, token;
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
            //System.out.println("rule: " + rule);
            } else if (type.equalsIgnoreCase("num")) {
                numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                numValueTemp = (HashMap) numValue.get(attribute);
                fuzzyMembershipString = (String) numValueTemp.get(value);
                st1 = new StringTokenizer(fuzzyMembershipString, ",");

                for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                    fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                    fuzzyMembership = st1.nextToken();
                    if (Float.parseFloat(fuzzyMembership) >= 0.3) {
                        rule = attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                        fuzzyTrainingData.add(rule);
                    //System.out.println("Fuzzy rule: " + rule);
                    }
                }
            }

        }
        System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
        //System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);

        for (index = 1; index < classLabel.size(); index++) {

            attribute = (String) classLabel.get(index);
            type = (String) classType.get(index);
            trainingDataSize = fuzzyTrainingData.size();
            oldfuzzyTrainingData = null;
            System.gc();

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
                //System.out.println("rule: " + rule);
                } else if (type.equalsIgnoreCase("num")) {
                    numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                    numValueTemp = (HashMap) numValue.get(attribute);
                    fuzzyMembershipString = (String) numValueTemp.get(currentRule);
                    st1 = new StringTokenizer(fuzzyMembershipString, ",");

                    for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                        fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                        fuzzyMembership = st1.nextToken();
                        if (Float.parseFloat(fuzzyMembership) >= 0.3) {
                            rule = preRule + attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                            fuzzyTrainingData.add(rule);
                        //System.out.println("Fuzzy rule: " + rule);
                        }
                    }
                }



            }

            /*if(index < classLabel.size()-1)
            oldIndexRule = indexRule;*/

            System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
        // System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);
        }

        /* System.out.println("Temporary Fuzzy Rules\n\n");
        
        for(index=0; index < oldIndexRule-1; index++) {
        System.out.println(fuzzyTrainingData.get(index));
        }*/

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

    private void ReadFilesFileSystem(boolean isTest, int fileSerialNumber) {

        File file = new File("facme.csv");
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        StringTokenizer st = null;

        String label, type, filename = null, line, token;
        int index = 0, commaIndex = 0;

        try {
            FileReader file_reader = new FileReader(file);
            BufferedReader buf_reader = new BufferedReader(file_reader);

            if(isTest)
                testFilename = buf_reader.readLine() + fileSerialNumber + ".txt";
            else
                trainingFilename = buf_reader.readLine() + fileSerialNumber + ".txt";
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
                    buf_reader.close();
                    file_reader.close();
                }
            }

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

    }
    
    public void generateFuzzyTrainingDataFileSystem(int fileSerialNumber) {
        int index, indexFuzzy, caretIndex = 0, commaIndex = 0, commaIndex1 = 0;
        StringTokenizer st = null, st1 = null;
        String line, attribute, value, fuzzyValue, fuzzyMembership, fuzzyMembershipString, type, rule, oldRule, tempRule, tempRule1, preRule, currentRule, postRule;
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        File oldFuzzyTrainingDataFile = null, newFuzzyTrainingDataFile = null;
        BufferedReader buf_reader = null;
        PrintWriter fuzzyTrainingDataPW = null;

        ReadFilesFileSystem(false, fileSerialNumber);

        attribute = (String) classLabel.get(0);
        type = (String) classType.get(0);

        System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

        try {
            buf_reader = new BufferedReader(new FileReader(trainingFilename));
            fuzzyTrainingDataPW = new PrintWriter(new FileWriter("fuzzyTrainingData" + fileSerialNumber + ".txt", false), true);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }

                st = new StringTokenizer(line, ",");
                commaIndex = line.indexOf(',');

                postRule = line.substring(commaIndex);

                value = st.nextToken();

                if (type.equalsIgnoreCase("nom")) {
                    rule = attribute + "=" + value + "^1" + postRule;
                    fuzzyTrainingDataPW.println(rule);
                //System.out.println("rule: " + rule);
                } else if (type.equalsIgnoreCase("num")) {
                    numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                    numValueTemp = (HashMap) numValue.get(attribute);
                    fuzzyMembershipString = (String) numValueTemp.get(value);
                    st1 = new StringTokenizer(fuzzyMembershipString, ",");

                    for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                        fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                        fuzzyMembership = st1.nextToken();
                        if (Float.parseFloat(fuzzyMembership) >= 0.3) {
                            rule = attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                            fuzzyTrainingDataPW.println(rule);
                            //System.out.println("fuzzyMembership: " + fuzzyMembership);
                        }
                    }
                }
            }
            buf_reader.close();
            fuzzyTrainingDataPW.close();
            System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
            //System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);
            
            //Thread.sleep(1000);

            for (index = 1; index < classLabel.size(); index++) {

                attribute = (String) classLabel.get(index);
                type = (String) classType.get(index);

                newFuzzyTrainingDataFile = new File("fuzzyTrainingData" + fileSerialNumber + ".txt");
                oldFuzzyTrainingDataFile = new File("oldFuzzyTrainingData" + fileSerialNumber + ".txt");
                if (newFuzzyTrainingDataFile.exists()) {
                    System.out.println("Rename: " + newFuzzyTrainingDataFile.renameTo(oldFuzzyTrainingDataFile));
                }

                newFuzzyTrainingDataFile = null;
                oldFuzzyTrainingDataFile = null;

                //buf_reader = new BufferedReader(new FileReader(new File("oldFuzzyTrainingData" + fileSerialNumber + ".txt")));
                buf_reader = new BufferedReader(new FileReader("oldFuzzyTrainingData" + fileSerialNumber + ".txt"));

                fuzzyTrainingDataPW = new PrintWriter(new FileWriter("fuzzyTrainingData" + fileSerialNumber + ".txt", false), true);

                System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

                while (true) {
                    oldRule = buf_reader.readLine();
                    if (oldRule == null) {
                        break;
                    }
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
                        fuzzyTrainingDataPW.println(rule);
                    //System.out.println("rule: " + rule);
                    } else if (type.equalsIgnoreCase("num")) {
                        numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                        numValueTemp = (HashMap) numValue.get(attribute);
                        fuzzyMembershipString = (String) numValueTemp.get(currentRule);
                        /*System.out.println("currentRule: " + currentRule);
                        System.out.println("fuzzyMembershipString: " + fuzzyMembershipString);*/
                        st1 = new StringTokenizer(fuzzyMembershipString, ",");

                        for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                            fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                            fuzzyMembership = st1.nextToken();
                            if (Float.parseFloat(fuzzyMembership) >= 0.3) {
                                rule = preRule + attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                                fuzzyTrainingDataPW.println(rule);
                            //System.out.println("Fuzzy rule: " + rule);
                            }
                        }
                    }
                }
                buf_reader.close();
                fuzzyTrainingDataPW.close();

                oldFuzzyTrainingDataFile = new File("oldFuzzyTrainingData" + fileSerialNumber + ".txt");
                if (oldFuzzyTrainingDataFile.exists()) {
                    oldFuzzyTrainingDataFile.delete();
                }
                System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
                
                //Thread.sleep(1000);
            }

        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        } 
    }

    public void generateFuzzyTestDataFileSystem(int fileSerialNumber) {
        int index, index1, indexFuzzy, caretIndex = 0, commaIndex = 0, commaIndex1 = 0;
        StringTokenizer st = null, st1 = null;
        String line, attribute, value, fuzzyValue, fuzzyMembership, fuzzyMembershipString, type, rule, oldRule, tempRule, tempRule1, preRule, currentRule, postRule;
        ArrayList numAttributeTemp = null, tempRuleAL = null, tempOldRuleAL = null;
        HashMap numValueTemp = null;
        BufferedReader buf_reader = null;
        PrintWriter fuzzyTrainingDataPW = null;

        ReadFilesFileSystem(true, fileSerialNumber);

        try {
            buf_reader = new BufferedReader(new FileReader(new File(testFilename)));
            fuzzyTrainingDataPW = new PrintWriter(new FileWriter("fuzzyTestData" + fileSerialNumber + ".txt", false), true);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                
                attribute = (String) classLabel.get(0);
                type = (String) classType.get(0);

                System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

                tempRuleAL = new ArrayList();

                st = new StringTokenizer(line, ",");
                commaIndex = line.indexOf(',');

                postRule = line.substring(commaIndex);

                value = st.nextToken();

                if (type.equalsIgnoreCase("nom")) {
                    rule = attribute + "=" + value + "^1" + postRule;
                    tempRuleAL.add(rule);
                //System.out.println("rule: " + rule);
                } else if (type.equalsIgnoreCase("num")) {
                    numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                    numValueTemp = (HashMap) numValue.get(attribute);
                    if (numValueTemp.containsKey(value)) {
                        fuzzyMembershipString = (String) numValueTemp.get(value);
                    } else {
                        interpolate(value, numValueTemp);
                        fuzzyMembershipString = (String) numValueTemp.get(value);
                    }
                    st1 = new StringTokenizer(fuzzyMembershipString, ",");

                    for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                        fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                        fuzzyMembership = st1.nextToken();
                        if (Float.parseFloat(fuzzyMembership) >= 0.3) {
                            rule = attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                            tempRuleAL.add(rule);
                        //System.out.println("Fuzzy rule: " + rule);
                        }
                    }
                }

                System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
                //System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);

                for (index = 1; index < classLabel.size(); index++) {
                    
                    System.out.println("tempRuleAL: " + tempRuleAL);

                    attribute = (String) classLabel.get(index);
                    type = (String) classType.get(index);

                    tempOldRuleAL = tempRuleAL;
                    tempRuleAL = new ArrayList();
                    System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

                    for (index1 = 0; index1 < tempOldRuleAL.size(); index1++) {
                        oldRule = (String) tempOldRuleAL.get(index1);

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
                            tempRuleAL.add(rule);
                        //System.out.println("rule: " + rule);
                        } else if (type.equalsIgnoreCase("num")) {
                            numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                            numValueTemp = (HashMap) numValue.get(attribute);
                            if (numValueTemp.containsKey(currentRule)) {
                                fuzzyMembershipString = (String) numValueTemp.get(currentRule);
                            } else {
                                interpolate(currentRule, numValueTemp);
                                fuzzyMembershipString = (String) numValueTemp.get(currentRule);
                            }
                            //fuzzyMembershipString = (String) numValueTemp.get(currentRule);
                            st1 = new StringTokenizer(fuzzyMembershipString, ",");

                            for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                                fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                                fuzzyMembership = st1.nextToken();
                                if (Float.parseFloat(fuzzyMembership) >= 0.3) {
                                    rule = preRule + attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                                    tempRuleAL.add(rule);
                                //System.out.println("Fuzzy rule: " + rule);
                                }
                            }
                        }
                    }

                    System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
                // System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);
                }
                
                for(index = 0; index < tempRuleAL.size(); index++) {
                    fuzzyTrainingDataPW.println(tempRuleAL.get(index));
                }
                fuzzyTrainingDataPW.println("END OF DATA POINT");
            }
            buf_reader.close();
            fuzzyTrainingDataPW.close();

        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
    }
    
    private void interpolate(String valueString, HashMap numValueTemp) {
        StringBuffer fuzzyMembershipString = new StringBuffer();
        float value = Float.parseFloat(valueString), valueLower = value, valueUpper = value, temp, mu, deltaLower=Float.POSITIVE_INFINITY, deltaUpper=Float.NEGATIVE_INFINITY;
        ArrayList tempAL = new ArrayList(numValueTemp.keySet());
        int index;
        String valueLowerString, valueUpperString, fuzzyMembershipLowerString, fuzzyMembershipUpperString;
        StringTokenizer st1, st2;
        
        System.out.println("valueString from interpolate(): " + valueString);
        
        for(index = 0; index < tempAL.size(); index++) {
            temp = Float.parseFloat((String) tempAL.get(index));
            //System.out.println("value - temp, deltaLower, deltaUpper: " + (value-temp) + "..." + deltaLower + "..." + deltaUpper);
            if(value - temp > 0 && value - temp < deltaLower) {
                valueLower = temp;
                deltaLower = value - temp;
            }
            else if(value - temp < 0 && value - temp > deltaUpper) {
                valueUpper = temp;
                deltaUpper = value - temp;
            }
        }
                
        valueLowerString = Float.toString(valueLower);
        valueUpperString = Float.toString(valueUpper);
        System.out.println("valueLowerString: " + valueLowerString);
        System.out.println("valueUpperString: " + valueUpperString);
        
        fuzzyMembershipLowerString = (String) numValueTemp.get(valueLowerString);
        fuzzyMembershipUpperString = (String) numValueTemp.get(valueUpperString);
        
        if (fuzzyMembershipLowerString == null) {
            deltaUpper = Float.NEGATIVE_INFINITY;
            valueLower = valueUpper;
            for (index = 0; index < tempAL.size(); index++) {
                temp = Float.parseFloat((String) tempAL.get(index));
                //System.out.println("value - temp, deltaLower, deltaUpper: " + (value-temp) + "..." + deltaLower + "..." + deltaUpper);
                if (valueLower - temp < 0 && valueLower - temp > deltaUpper) {
                    valueUpper = temp;
                    deltaUpper = valueLower - temp;
                }
            }

            valueLowerString = Float.toString(valueLower);
            valueUpperString = Float.toString(valueUpper);
            System.out.println("valueLowerString Amended for Extrapolation: " + valueLowerString);
            System.out.println("valueUpperString Amended for Extrapolation: " + valueUpperString);

            fuzzyMembershipLowerString = (String) numValueTemp.get(valueLowerString);
            fuzzyMembershipUpperString = (String) numValueTemp.get(valueUpperString);

            
        } else if (fuzzyMembershipUpperString == null) {
            deltaLower = Float.POSITIVE_INFINITY;
            valueUpper = valueLower;
            for (index = 0; index < tempAL.size(); index++) {
                temp = Float.parseFloat((String) tempAL.get(index));
                //System.out.println("value - temp, deltaLower, deltaUpper: " + (value-temp) + "..." + deltaLower + "..." + deltaUpper);
                if (valueUpper - temp > 0 && valueUpper - temp < deltaLower) {
                    valueLower = temp;
                    deltaLower = valueUpper - temp;
                }
            }
            
            valueLowerString = Float.toString(valueLower);
            valueUpperString = Float.toString(valueUpper);
            System.out.println("valueLowerString Amended for Extrapolation: " + valueLowerString);
            System.out.println("valueUpperString Amended for Extrapolation: " + valueUpperString);

            fuzzyMembershipLowerString = (String) numValueTemp.get(valueLowerString);
            fuzzyMembershipUpperString = (String) numValueTemp.get(valueUpperString);
        }
        
        st1 = new StringTokenizer(fuzzyMembershipLowerString, ",");
        st2 = new StringTokenizer(fuzzyMembershipUpperString, ",");
        
        mu = (value - valueLower) / (valueUpper - valueLower);
        
        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            if (st1.countTokens() == 1 || st2.countTokens() == 1) {
                fuzzyMembershipString.append(cosineInterpolate(Float.parseFloat(st1.nextToken()), Float.parseFloat(st2.nextToken()), mu));
            } else {
                fuzzyMembershipString.append(cosineInterpolate(Float.parseFloat(st1.nextToken()), Float.parseFloat(st2.nextToken()), mu) + ",");
            }
        }

        numValueTemp.put(valueString, fuzzyMembershipString.toString());
        System.out.println("fuzzyMembershipString: " + fuzzyMembershipString);
        
    }
    
    float cosineInterpolate(float y1, float y2, float mu) {
        float mu2;

        mu2 = (float)(1 - Math.cos(mu * Math.PI)) / 2;
        return (y1 * (1 - mu2) + y2 * mu2);
    }


    public void generateAssociationRulesLocal(int fileSerialNumber) {
        int index1, index2, indexDecisionClass;
        String itemset1 = null, fileName;
        byte[] tempItemset1 = null;
        BufferedReader buf_reader =null, buf_readerGlobal = null;
        String fuzzyTrainingDataFile = "fuzzyTrainingData" + fileSerialNumber + ".txt";
        PrintWriter pw = null;
        try {
            buf_reader = new BufferedReader(new FileReader("facme.csv"));

            buf_reader.readLine();
            decisionClassLabel = buf_reader.readLine();
            buf_reader.close();

            System.out.println("decisionClassLabel: " + decisionClassLabel);
            initializeLocal(fuzzyTrainingDataFile, fileSerialNumber);
            
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        // 1st phase
        for (indexDecisionClass = 0; indexDecisionClass < decisionClassesAL.size(); indexDecisionClass++) {
            try {
                fileName = (String) decisionClassesAL.get(indexDecisionClass) + "_" +  fileSerialNumber + ".txt";
                buf_readerGlobal = new BufferedReader(new FileReader(fileName));
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            trainingDataSize = (Integer) decisionClassesCountMap.get(decisionClassesAL.get(indexDecisionClass));
            trainingDataSizeCutOff = ((float) trainingDataSize) * support;
            partitionSize = (int) Math.ceil((float) trainingDataSize / (float) numberOfPartitions);
            subPartitionSize = (int) Math.ceil((float) partitionSize / (float) numberOfSubPartitions);

            lastPartitionCutOff = ((float) (trainingDataSize - partitionSize * (numberOfPartitions - 1))) * support;
            System.out.println("trainingDataSize, trainingDataSizeCutOff, numberOfPartitions, partitionSize, support, lastPartitionCutOff, subPartitionSize, numberOfSubPartitions : " + trainingDataSize + "..." + trainingDataSizeCutOff + "..." + numberOfPartitions + "..." + partitionSize + "..." + support + "..." + lastPartitionCutOff + "..." + subPartitionSize + "..." + numberOfSubPartitions);

            itemsetsFinal = new HashMap();
            itemsetsFinalPartition = new HashMap();

            for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
                System.out.println("indexPartition............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

                nonDecisionItemSets = new HashMap();
                //itemsetsSubPartition = new HashMap();

                for (index1 = 0; index1 < numberOfSubPartitions; index1++) {
                    System.out.println("index1 Start........" + index1 + "..." + Calendar.getInstance().getTime());
                    generatePartitionSingletons(buf_readerGlobal, index1);
                    System.out.println("index1 END........" + index1 + "..." + Calendar.getInstance().getTime());
                    
                }
                pruneSingletons();
                System.out.println("singletons generated");

                //generate itemsets

                nextItemsets = nonDecisionItemSets;
                nonDecisionItemSets = null;
                //itemsetsSubPartition = null;

                for (countK = 2; nextItemsets.size() > 1; countK++) {

                    currentItemsets = nextItemsets;

                    System.out.println("Next iteration started.............................................." + countK + "..." + Calendar.getInstance().getTime());

                    currentItemsetsAL = new ArrayList(currentItemsets.keySet());
                    nextItemsets = new HashMap();
                    newItemsetUpdate = new ArrayList();

                    for (index1 = 0; index1 < currentItemsetsAL.size() - 1; index1++) {
                        try {
                            itemset1 = (String) currentItemsetsAL.get(index1);

                            System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + itemset1 + "..." + ((byte[]) currentItemsets.get(itemset1)).length + "..." + Calendar.getInstance().getTime());
                            tempItemset1 = StringUtilsBzip2.uncompress((byte[]) currentItemsets.get(itemset1));
                            System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + itemset1 + "..." + tempItemset1.length + "..." + Calendar.getInstance().getTime());
                        } catch (IOException e) {
                            System.out.println("IO exception = " + e);
                            e.printStackTrace();
                        }

                        for (index2 = index1 + 1; index2 < currentItemsetsAL.size(); index2++) {
                            System.out.println("Start~~~~" + Calendar.getInstance().getTime());
                            createNItemsets(itemset1, (String) currentItemsetsAL.get(index2), tempItemset1);
                            System.out.println("End~~~~" + Calendar.getInstance().getTime());
                        }
                        tempItemset1 = null;

                        currentItemsets.remove(itemset1);
                        System.out.println("Removed from current itemsetsAL: " + (String) currentItemsetsAL.get(index1) + "..." + index1 + "..." + currentItemsetsAL.size() + "..." + Calendar.getInstance().getTime());
                        currentItemsetsAL.remove(index1);
                        itemset1 = null;
                        System.gc();
                        index1--;
                    }
                    currentItemsets = null;
                    currentItemsetsAL = null;
                    newItemsetUpdate = null;
                    System.gc();
                    System.out.println("Next iteration FINISH.............................................." + countK + "..." + Calendar.getInstance().getTime());
                }
                System.out.println("indexPartition FINISH............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

                System.out.println("itemsetsFinal:" + itemsetsFinal);
                System.out.println("\n\nitemsetsFinalPartition:" + itemsetsFinalPartition);
                System.out.println("\n\n\n");
            }

            try {
                buf_readerGlobal.close();
                buf_readerGlobal = null;
                pw = new PrintWriter(new FileWriter(decisionClassesAL.get(indexDecisionClass) +  "_FirstPhaseData" + fileSerialNumber + ".txt", false), true);
                String itemset;

                currentItemsetsAL = new ArrayList(itemsetsFinal.keySet());
                pw.println(trainingDataSizeCutOff);
                pw.println(numberOfPartitions);
                pw.println(partitionSize);
                pw.println(subPartitionSize);
                pw.println(numberOfSubPartitions);

                for (index1 = 0; index1 < currentItemsetsAL.size(); index1++) {
                    itemset = (String) currentItemsetsAL.get(index1);
                    pw.println(itemset);
                    pw.println(itemsetsFinal.get(itemset));
                    pw.println(itemsetsFinalPartition.get(itemset));
                }
                pw.close();
               
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            currentItemsetsAL = null;
            itemsetsFinal = null;
            itemsetsFinalPartition = null;

        }

        try {
            pw = new PrintWriter(new FileWriter("ARMParametersSecondPhase" + fileSerialNumber + ".txt", false), true);

            for (index1 = 0; index1 < decisionClassesAL.size(); index1++) {
                pw.println(decisionClassesAL.get(index1));
            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println(Calendar.getInstance().getTime());

    }

    private void createNItemsets(String itemset1, String itemset2, byte[] tempItemset1) {
        boolean mismatch = false, returnMethod, newItemsetPresentInItemsetFinal = false, itemsetFinalUpdate = false, removeSuperSets = false;
        StringTokenizer st;
        HashMap tempHashMap = new HashMap(), tempHashMap1;
        String token = null, newItemset, oldItemset = null;
        int index, currentSingletonAddedInPartition;
        ArrayList nextItemsetsAL;
        float currentCutOff, fuzzyMembershipTotal = 0;
        byte fuzzyMembership, fuzzyMembershipTemp;
        byte[] tempItemset2, tempAL, tempAL1, twoItemsetsAL = new byte[partitionSize];

        //create newItemset

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
        tempHashMap = null;
        tempHashMap = new HashMap();

        st = new StringTokenizer(itemset1, ",");
        while (st.hasMoreTokens()) {
            tempHashMap.put(st.nextToken(), null);
        }

        st = new StringTokenizer(itemset2, ",");
        while (st.hasMoreTokens()) {
            token = st.nextToken();

            if (!tempHashMap.containsKey(token)) {
                System.out.println("token: " + token);
                newItemset = newItemset + "," + token;
                break;
            }

        }

        System.out.println("newItemset: " + newItemset);
        System.out.println("itemset1: " + itemset1);
        System.out.println("itemset2: " + itemset2);
        //create newItemset - finish


        tempHashMap.put(token, null);

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
                itemsetFinalUpdate = true;
                oldItemset = (String) nextItemsetsAL.get(index);
                System.out.println("newItemsetPresentInItemsetFinal: " + newItemsetPresentInItemsetFinal);
                break;
            }

        }
        nextItemsetsAL = null;
        //check if new itemset in itemsetsFinal - finish

        //check if new itemset in newItemsetUpdate

        if (newItemsetPresentInItemsetFinal) {
            for (index = 0; index < newItemsetUpdate.size(); index++) {

                returnMethod = true;
                itemsetFinalUpdate = true;

                st = new StringTokenizer((String) newItemsetUpdate.get(index), ",");
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    if (!tempHashMap.containsKey(token)) {
                        returnMethod = false;
                        break;
                    }
                }
                if (returnMethod) {
                    itemsetFinalUpdate = false;
                    System.out.println("itemsetFinalUpdate: " + oldItemset + ";" + itemsetFinalUpdate);
                    return;
                }
            }

            //calculate cut off

            currentSingletonAddedInPartition = ((Integer) itemsetsFinalPartition.get(oldItemset)).intValue();

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
        //currentCutOff *= hundred;

        //check if new itemset in newItemsetUpdate - finish

        nextItemsetsAL = null;
        //check if newItemset already in nextItemsets - finish

        //generate tidlist

        try {
            System.out.println("StringUtilsBzip2.UNCOMPRESS START......." + itemset2 + "..." + Calendar.getInstance().getTime());
            tempItemset2 = StringUtilsBzip2.uncompress((byte[]) currentItemsets.get(itemset2));
            System.out.println("StringUtilsBzip2.UNCOMPRESS END......." + itemset2 + "..." + Calendar.getInstance().getTime());

            //if (((Integer) currentItemsetsCheck.get(itemset1)) <= ((Integer) currentItemsetsCheck.get(itemset2))) {
            tempAL = tempItemset1;
            tempAL1 = tempItemset2;
            /*    returnMethod = true;
            } else {
            tempAL = tempItemset2;
            tempAL1 = tempItemset1;
            returnMethod = false;
            }*/
            tempItemset1 = null;
            tempItemset2 = null;

            for (index = 0; index < partitionSize; index++) {
                fuzzyMembership = tempAL1[index];

                if (fuzzyMembership > 0) {
                    fuzzyMembershipTemp = tempAL[index];

                    if (fuzzyMembershipTemp < fuzzyMembership) {
                        fuzzyMembership = fuzzyMembershipTemp;
                    }
                    fuzzyMembershipTotal += fuzzyMembership;
                    twoItemsetsAL[index] = fuzzyMembership;
                //numberOfTids++;
                }
            }
            tempAL1 = null;
            tempAL = null;
            fuzzyMembershipTotal = fuzzyMembershipTotal / hundred;

            System.out.println("System.gc() START 2: " + Calendar.getInstance().getTime());
            System.gc();
            System.out.println("System.gc() END 2: " + Calendar.getInstance().getTime());

            //generate tidlist -- finish

            //update in itemsetsFinal if newItemset already present in itemsetsFinal

            if (itemsetFinalUpdate && newItemsetPresentInItemsetFinal) {

                fuzzyMembershipTotal += ((Float) itemsetsFinal.get(oldItemset)).floatValue();

                itemsetsFinal.remove(oldItemset);
                itemsetsFinal.put(oldItemset, fuzzyMembershipTotal);

                newItemsetUpdate.add(oldItemset);

                //freq itemsets would be expanded further

                if (fuzzyMembershipTotal >= currentCutOff) {
                    System.out.println("StringUtilsBzip2.COMPRESS1 START......." + newItemset + "..." + Calendar.getInstance().getTime());
                    nextItemsets.put(newItemset, StringUtilsBzip2.compress(twoItemsetsAL));
                    System.out.println("StringUtilsBzip2.COMPRESS1 END......." + newItemset + "..." + Calendar.getInstance().getTime());

                    //nextItemsetsCheck.put(newItemset, numberOfTids);
                    System.out.println("newItemset put in nextitemsets:" + newItemset + "____________________" + fuzzyMembershipTotal + " >>>>>>>>> " + currentCutOff);

                } else {
                    removeSuperSets = true;
                }
                twoItemsetsAL = null;

                System.out.println("oldItemset: " + oldItemset + "==========================" + itemsetsFinal.get(oldItemset) + "============" + fuzzyMembershipTotal / hundred);
                return;

            } //update in itemsetsFinal if newItemset already present in itemsetsFinal - finish
            else if (fuzzyMembershipTotal >= currentCutOff) {

                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                itemsetsFinalPartition.put(newItemset, indexPartition);

                System.out.println("StringUtilsBzip2.COMPRESS START......." + newItemset + "..." + Calendar.getInstance().getTime());
                nextItemsets.put(newItemset, StringUtilsBzip2.compress(twoItemsetsAL));
                System.out.println("StringUtilsBzip2.COMPRESS END......." + newItemset + "..." + Calendar.getInstance().getTime());

                //nextItemsetsCheck.put(newItemset, numberOfTids);

                newItemsetUpdate.add(newItemset);
                twoItemsetsAL = null;
                System.out.println("newItemset: " + newItemset + "---------------------------------------" + fuzzyMembershipTotal + " >>>>>>>>> " + currentCutOff);

            } else {
                itemsetsFinal.put(newItemset, fuzzyMembershipTotal);
                itemsetsFinalPartition.put(newItemset, indexPartition);

                newItemsetUpdate.add(newItemset);

                twoItemsetsAL = null;
                removeSuperSets = true;
                System.out.println("newItemset NBE: " + newItemset + "^^^NBE^^^" + fuzzyMembershipTotal);
            }

            System.out.println("System.gc() START 3: " + Calendar.getInstance().getTime());
            System.gc();
            System.out.println("System.gc() END 3: " + Calendar.getInstance().getTime());
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("currentCutOff --- fuzzyMembershipTotal: " + currentCutOff + "---" + fuzzyMembershipTotal);

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

            System.out.println("System.gc() START 4: " + Calendar.getInstance().getTime());
            System.gc();
            System.out.println("System.gc() END 4: " + Calendar.getInstance().getTime());
        }
        tempHashMap = null;
    ///System.gc();
    }

    private void initializeLocal(String fuzzyTrainingDataFile, int fileSerialNumber) {
        try {
            FileReader file_reader1 = new FileReader("ARMParameters.txt");
            BufferedReader buf_reader1 = new BufferedReader(file_reader1), buf_reader;
            String line, subLine;
            StringTokenizer st = null;
            PrintWriter pw = null;
            int count, index;
            HashMap decisionClassesPWMap = new HashMap();

            support = Float.parseFloat(buf_reader1.readLine());
            numberOfPartitions = Integer.parseInt(buf_reader1.readLine());
            numberOfSubPartitions = Integer.parseInt(buf_reader1.readLine());

            file_reader1.close();
            buf_reader1.close();

            buf_reader = new BufferedReader(new FileReader(fuzzyTrainingDataFile));

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                while(st.hasMoreTokens()) {
                    subLine = st.nextToken();
                    if(subLine.contains(decisionClassLabel)) {
                        if(!decisionClassesAL.contains(subLine)) {
                            decisionClassesAL.add(subLine);
                            pw = new PrintWriter(new FileWriter(subLine + "_" + fileSerialNumber + ".txt", false), true);
                            decisionClassesPWMap.put(subLine, pw);
                            decisionClassesCountMap.put(subLine, 1);
                        }
                        else {
                            pw = (PrintWriter) decisionClassesPWMap.get(subLine);
                            count = (Integer) decisionClassesCountMap.get(subLine);
                            decisionClassesCountMap.remove(subLine);
                            count++;
                            decisionClassesCountMap.put(subLine, count);
                        }
                        pw.println(line);
                        break;

                    }
                }
            }
            buf_reader.close();
            for(index = 0; index < decisionClassesAL.size(); index++) {
                pw = (PrintWriter) decisionClassesPWMap.get(decisionClassesAL.get(index));
                pw.close();
                pw=null;
            }
            decisionClassesPWMap = null;
            System.out.println(Calendar.getInstance().getTime());

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

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

                //System.out.println("line:" + line);
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    singleton = st1.nextToken();
                    //System.out.println("\n\nsingleton:" + singleton);
                    fuzzyMembership = st1.nextToken();

                    if (!nonDecisionItemSets1.containsKey(singleton)) {
                        tempByteArray = new byte[subPartitionSize];
                        tempByteArray[index] = (byte) Math.ceil(Float.parseFloat(fuzzyMembership) * hundred);
                        nonDecisionItemSets1.put(singleton, tempByteArray);
                        tempByteArray = null;
                    //System.gc();
                    //nextItemsetsCheck.put(singleton, 1);

                    } else {
                        tempByteArray = (byte[]) nonDecisionItemSets1.get(singleton);
                        tempByteArray[index] = (byte) Math.ceil(Float.parseFloat(fuzzyMembership) * hundred);
                        nonDecisionItemSets1.remove(singleton);
                        nonDecisionItemSets1.put(singleton, tempByteArray);
                        tempByteArray = null;
                    
                    }

                    //put counts in itemsetFinal

                    //if (secondPhaseOutputted == null || !secondPhaseOutputted.contains(singleton)) {
                    if (itemsetsFinal.containsKey(singleton)) {

                        newFuzzyMembership = Float.parseFloat(fuzzyMembership);
                        newFuzzyMembership += ((Float) itemsetsFinal.get(singleton)).floatValue();

                        itemsetsFinal.remove(singleton);
                        //System.out.println("fuzzyMembership: " + fuzzyMembership);
                        itemsetsFinal.put(singleton, newFuzzyMembership);
                    } else if (!secondPhase) {
                        itemsetsFinal.put(singleton, new Float(fuzzyMembership));
                        itemsetsFinalPartition.put(singleton, indexPartition);
                    }
                    //}

                    if (!itemsetsSubPartition.containsKey(singleton)) {
                        itemsetsSubPartition.put(singleton, indexGlobal);
                    }
                }
            }

            nonDecisionItemSetsAL = new ArrayList(itemsetsSubPartition.keySet());

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);

                System.out.println("StringUtilsBzip2.COMPRESS/UNCOMPRESS START......." + singleton + "..." + Calendar.getInstance().getTime());

                tempNow = (byte[]) nonDecisionItemSets1.get(singleton);
                nonDecisionItemSets1.remove(singleton);
                if (nonDecisionItemSets.containsKey(singleton)) {
                    tempPrev = StringUtilsBzip2.uncompress((byte[]) nonDecisionItemSets.get(singleton));
                    System.out.println("tempNow: " + tempNow);
                    tempByteArray = new byte[tempNow.length + tempPrev.length];
                    System.arraycopy(tempPrev, 0, tempByteArray, 0, tempPrev.length);
                    System.arraycopy(tempNow, 0, tempByteArray, tempPrev.length, tempNow.length);

                    tempNow = null;
                    tempPrev = null;

                    nonDecisionItemSets.remove(singleton);
                    nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(tempByteArray));
                    tempByteArray = null;
                } else {
                    if (((Integer) itemsetsSubPartition.get(singleton)).intValue() == 0) {
                        nonDecisionItemSets.put(singleton, StringUtilsBzip2.compress(tempNow));
                        tempNow = null;

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
                System.out.println("StringUtilsBzip2.COMPRESS/UNCOMPRESS END......." + singleton + "..." + Calendar.getInstance().getTime());
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
            totalFuzzyMembership = 0;

            totalFuzzyMembership = ((Float) itemsetsFinal.get(singleton)).floatValue();

            //calculate cut off

            currentSingletonAddedInPartition = ((Integer) itemsetsFinalPartition.get(singleton)).intValue();

            if (indexPartition != numberOfPartitions) {
                currentCutOff = (indexPartition - currentSingletonAddedInPartition + 1) * trainingDataSizeCutOff / numberOfPartitions;
            } else {
                currentCutOff = ((indexPartition - currentSingletonAddedInPartition) * trainingDataSizeCutOff / numberOfPartitions) + lastPartitionCutOff;
            }

            if (totalFuzzyMembership < currentCutOff) {

                nonDecisionItemSets.remove(singleton);
                nonDecisionItemSetsAL.remove(index);

                index--;
                System.out.println("Pruned at start, nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " < " + currentCutOff);
                System.gc();

            } else {
                System.out.println("nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " >= " + currentCutOff);
            }
        }

        System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size() + "..." + Calendar.getInstance().getTime());
        //nonDecisionItemSets = null;
        nonDecisionItemSetsAL = null;
        ///System.gc();
        System.out.println("pruning over");
    //pruning over
    }

    public void generateAssociationRulesSecondPhaseLocal(int fileSerialNumber) {
        String line = null, itemset, singleton = null, fileName;
        int index1, partitionNumber, index, indexDecisionClass;
        ArrayList itemsetsFinalAL = null, itemsetsAlreadyDone = null;
        PrintWriter pw = null;
        float totalFuzzyMembership;
        BufferedReader buf_readerGlobal = null, bufferedReader = null;
        byte[] tempItemset = null;
        boolean isFrequentItemsetGeneratedForDecisionClass;

        secondPhase = true;

        System.out.println("SECOND PHASE START");

        initializeSecondPhase(fileSerialNumber);
        try {
            pw = new PrintWriter(new FileWriter("FinalResults" + fileSerialNumber + ".txt", false), true);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        for (indexDecisionClass = 0; indexDecisionClass < decisionClassesAL.size(); indexDecisionClass++) {

            System.out.println("Start, indexDecisionClass: " + indexDecisionClass + "..." + Calendar.getInstance().getTime());
            itemsetsFinal = new HashMap();
            itemsetsFinalPartition = new HashMap();
            isFrequentItemsetGeneratedForDecisionClass = false;
            
            try {
                fileName = decisionClassesAL.get(indexDecisionClass) + "_" + fileSerialNumber + ".txt";
                buf_readerGlobal = new BufferedReader(new FileReader(fileName));
                fileName = decisionClassesAL.get(indexDecisionClass) + "_FirstPhaseData" + fileSerialNumber + ".txt";
                bufferedReader = new BufferedReader(new FileReader(fileName));

                trainingDataSizeCutOff = Float.parseFloat(bufferedReader.readLine());
                numberOfPartitions = Integer.parseInt(bufferedReader.readLine());
                partitionSize = Integer.parseInt(bufferedReader.readLine());
                subPartitionSize = Integer.parseInt(bufferedReader.readLine());
                numberOfSubPartitions = Integer.parseInt(bufferedReader.readLine());
                System.out.println("trainingDataSizeCutOff, numberOfPartitions, partitionSize, subPartitionSize, numberOfSubPartitions : " + trainingDataSizeCutOff + "..." + numberOfPartitions + "..." + partitionSize + "..." + subPartitionSize + "..." + numberOfSubPartitions);

                while (true) {
                    line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    itemsetsFinal.put(line, new Float(bufferedReader.readLine()));
                    itemsetsFinalPartition.put(line, new Integer(bufferedReader.readLine()));
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }

            itemsetsFinalAL = new ArrayList(itemsetsFinal.keySet());

            // 2nd phase
            for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
                System.out.println("indexPartition............................................." + indexPartition + "..." + Calendar.getInstance().getTime());

                //Output itemsets that were added in the current partition in the 1st phase
                System.out.println("Itemsets output.... " + Calendar.getInstance().getTime());

                for (index1 = 0; index1 < itemsetsFinalAL.size(); index1++) {
                    itemset = (String) itemsetsFinalAL.get(index1);
                    partitionNumber = ((Integer) itemsetsFinalPartition.get(itemset)).intValue();

                    if (partitionNumber == indexPartition) {
                        totalFuzzyMembership = ((Float) itemsetsFinal.get(itemset)).floatValue();

                        if (totalFuzzyMembership >= trainingDataSizeCutOff) {
                            pw.println(itemset + "\t" + totalFuzzyMembership);
                            if(!itemset.contains(decisionClassLabel))
                                isFrequentItemsetGeneratedForDecisionClass = true;
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
                System.gc();

                nonDecisionItemSets = new HashMap();
                //itemsetsSubPartition = new HashMap();
                System.out.println("Singletons generating...." + Calendar.getInstance().getTime());
                for (index1 = 0; index1 < numberOfSubPartitions; index1++) {
                    System.out.println("index1 Start........" + index1 + "..." + Calendar.getInstance().getTime());
                    generatePartitionSingletons(buf_readerGlobal, index1);
                    System.out.println("index1 END........" + index1 + "..." + Calendar.getInstance().getTime());
                }
                //pruneSingletons();

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
                    System.gc();
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
                buf_readerGlobal.close();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            if(!isFrequentItemsetGeneratedForDecisionClass)
                System.out.println("WARNING, No frequent itemset generated for decisionClass: " + decisionClassesAL.get(indexDecisionClass));
            System.out.println("End, indexDecisionClass: " + indexDecisionClass + "..." + Calendar.getInstance().getTime());
        }
        pw.close();
    }

    private void initializeSecondPhase(int fileSerialNumber) {
        BufferedReader buf_reader = null;
        String file = "ARMParametersSecondPhase" + fileSerialNumber + ".txt";
        String line = null;
        if (decisionClassesAL != null && !decisionClassesAL.isEmpty()) {
            decisionClassesAL = new ArrayList();
        }

        try {
            buf_reader = new BufferedReader(new FileReader(file));
            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                decisionClassesAL.add(line);
            }
            buf_reader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
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
                    //System.out.println("singleton1: " + singleton1);
                    //System.out.println("nonDecisionItemSets.get(singleton1: " + nonDecisionItemSets.get(singleton1));
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
                        fuzzyMembershipTotal += fuzzyMembership;
                        twoItemsetsAL[index] = fuzzyMembership;
                    }
                }
                tempItemset1 = twoItemsetsAL;
                tempItemset2 = null;
                System.gc();
            }
            //generate tidlist -- finish

            fuzzyMembershipTotal += ((Float) itemsetsFinal.get(itemset)).floatValue();

            itemsetsFinal.remove(itemset);
            itemsetsFinal.put(itemset, fuzzyMembershipTotal);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.gc();

        return;
    }

    public void removeDuplicateItemsets(int fileSerialNumber) {
        BufferedReader buf_reader = null;
        String fileName = "facme.csv", line = null, itemset = null, itemset1 = null;
        ArrayList itemsetsAL = new ArrayList(), tempItemsetAL = null;
        StringTokenizer st = null;
        int index, index1, count = 0;
        File file = null;
        boolean shouldRemove = false;
        PrintWriter pw = null;
        System.out.println("REMOVE DUPLICATE ITEMSETS");
        try {
            buf_reader = new BufferedReader(new FileReader(fileName));
            trainingFilename = buf_reader.readLine() + fileSerialNumber + ".txt";
            decisionClassLabel = buf_reader.readLine();
            buf_reader.close();
            fileName = "FinalResults" + fileSerialNumber + ".txt";
            buf_reader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                count++;
                if(line.contains(decisionClassLabel))
                    continue;
                st = new StringTokenizer(line, "\t");
                itemsetsAL.add(st.nextToken());
            }
            buf_reader.close();
            System.out.println("Number of itemsets at START: " + count);
            System.out.println("Number of itemsets after decisionClassLabel itemsets removed: " + itemsetsAL.size());
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        for(index = 0; index < itemsetsAL.size()-1; index++) {
            
            tempItemsetAL = new ArrayList();
            itemset = (String) itemsetsAL.get(index);
            if (itemset.contains(",")) {
                st = new StringTokenizer(itemset, ",");
                while (st.hasMoreTokens()) {
                    tempItemsetAL.add(st.nextToken());
                }
            }
            
            for(index1 = index+1; index1 < itemsetsAL.size(); index1++) {
                itemset1 = (String) itemsetsAL.get(index1);
                if((itemset1.contains(",") && !itemset1.contains(",")) || !itemset1.contains(",") && itemset1.contains(",")) {
                    continue;
                }
                else if(!itemset1.contains(",") && !itemset1.contains(",")) {
                    if(itemset.equals(itemset1)) {
                        itemsetsAL.remove(index1);
                        index1--;
                    }
                }
                else {
                    st = new StringTokenizer(itemset1, ",");
                    if(st.countTokens() == tempItemsetAL.size()) {
                        shouldRemove = true;
                        while(st.hasMoreTokens()) {
                            if(!tempItemsetAL.contains(st.nextToken())) {
                                shouldRemove = false;
                                break;
                            }
                        }
                        if(shouldRemove) {
                            itemsetsAL.remove(index1);
                            index1--;
                       }
                        shouldRemove = false;
                    }
                }
            }
        }

        System.out.println("Number of itemsets after duplicate itemsets removed: " + itemsetsAL.size());
        file = new File(fileName);
        file.delete();

        try {
            pw = new PrintWriter(new FileWriter(fileName, false), true);
            for(index = 0; index < itemsetsAL.size(); index++) {
                pw.println(itemsetsAL.get(index));
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
}
