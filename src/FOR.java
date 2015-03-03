
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

public class FOR {

    private ArrayList classLabel = new ArrayList();
    private ArrayList classType = new ArrayList();
    private HashMap classFilename = new HashMap();
    private ArrayList trainingData = new ArrayList();
    private ArrayList fuzzyTrainingDataFinal = null;
    private HashMap numAttribute = new HashMap();
    private HashMap numValue = new HashMap();
    private String decisionClassLabel = null;
    private float support=1;
    private float trainingDataSizeCutOff = 0;
    private ArrayList nonDecisionItemSetsAL = null;
    
    private ArrayList itemsetsFinal = null;
    
    private HashMap nextItemsets = null;
    private HashMap currentItemsets = null;
    private ArrayList currentItemsetsAL = null;
    
    private HashMap nonDecisionItemSets = new HashMap();
    //private HashMap decisionItemSets = new HashMap();
    
    private int numberItemsetsGenerated = 0;
    
    private File currentItemsetsFile = null;

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
        int index, indexFuzzy, indexRule = 0, caretIndex = 0, commaIndex = 0, commaIndex1 = 0, trainingDataSize=0;
        StringTokenizer st = null, st1 = null;
        String line, attribute, value, fuzzyValue, fuzzyMembership, fuzzyMembershipString, type, rule, oldRule, tempRule, tempRule1, preRule, currentRule, postRule;
        ArrayList numAttributeTemp = null, oldfuzzyTrainingData=null;
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
                    if (Float.parseFloat(fuzzyMembership) >= 0.1) {
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
            oldfuzzyTrainingData=null;
            
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
                        if (Float.parseFloat(fuzzyMembership) >= 0.1) {
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
                
        try
        {
            fw = new FileWriter( "fuzzytrainingdata.txt", false );
        } catch(IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw, true);

        
        for(index=0; index < fuzzyTrainingData.size(); index++) {
            System.out.println(fuzzyTrainingData.get(index));
            pw.println(fuzzyTrainingData.get(index));
        }
        fuzzyTrainingDataFinal = fuzzyTrainingData;
        

    }
    
    public void generateAssociationRules() {
        int index, index1, index2;
        float totalFuzzyMembership;
        String line, singleton, fuzzyMembership;
        StringTokenizer st = null, st1 = null, st2 =null;
        ArrayList temp = null, currentTidList1;
        
        System.out.println("Program started..............................................  " + Calendar.getInstance().getTime());
        if (fuzzyTrainingDataFinal == null || fuzzyTrainingDataFinal.size() == 0) {
            
            fuzzyTrainingDataFinal = new ArrayList();
            
            try {
                File file = new File("fuzzytrainingdata.txt");
                FileReader file_reader = new FileReader(file);
                BufferedReader buf_reader = new BufferedReader(file_reader);


                support = Float.parseFloat(buf_reader.readLine());
                decisionClassLabel = buf_reader.readLine();

                while (true) {
                    line = buf_reader.readLine();
                    if (line == null) {
                        break;
                    }
                    fuzzyTrainingDataFinal.add(line);
                }



            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
        }
        
        trainingDataSizeCutOff = ((float) fuzzyTrainingDataFinal.size()) * support;
        
        
        
        System.out.println("file read over, trainingDataSizeCutOff: " + trainingDataSizeCutOff);
        for(index=0; index < fuzzyTrainingDataFinal.size(); index++) {
            line = (String) fuzzyTrainingDataFinal.get(index);
            
            //System.out.println("line:" + line);
            st = new StringTokenizer(line, ",");
            while(st.hasMoreTokens()) {
                st1 = new StringTokenizer(st.nextToken(), "^");
                singleton = st1.nextToken();
                //System.out.println("\n\nsingleton:" + singleton);
                
                fuzzyMembership = st1.nextToken();
                
                //System.out.println("fuzzyMembership:" + fuzzyMembership);
                
                st2 = new StringTokenizer(singleton, "=");
                st2.nextToken();
                
                /*if(attribute.equalsIgnoreCase(decisionClassLabel)) {
                    if(!decisionItemSets.containsKey(singleton)) {
                        temp = new ArrayList();
                        temp.add(index + "^" + fuzzyMembership);
                        decisionItemSets.put(singleton, temp);
                    }
                    
                    else {
                        temp = (ArrayList) decisionItemSets.get(singleton);
                        temp.add(index + "^" + fuzzyMembership);
                        decisionItemSets.remove(singleton);
                        decisionItemSets.put(singleton, temp);
                    }
                       
                }
                
                else {*/
                    if(!nonDecisionItemSets.containsKey(singleton)) {
                        temp = new ArrayList();
                        temp.add(index + "^" + fuzzyMembership);
                        nonDecisionItemSets.put(singleton, temp);
                    }
                    
                    else {
                        temp = (ArrayList) nonDecisionItemSets.get(singleton);
                        temp.add(index + "^" + fuzzyMembership);
                        nonDecisionItemSets.remove(singleton);
                        nonDecisionItemSets.put(singleton, temp);
                    }
                //}
                
                
            }
        }
        
        fuzzyTrainingDataFinal = null;
        //System.out.println(decisionItemSets);
        //System.out.println("\n\n\n\n" + nonDecisionItemSets);
        
        nonDecisionItemSetsAL = new ArrayList(nonDecisionItemSets.keySet());
        //decisionItemSetsAL = new ArrayList(decisionItemSets.keySet());
        
        System.out.println("singletons generated");
        
        //prune decisionItemSets
        //System.out.println("decisionItemSetsAL.size(): " + decisionItemSetsAL.size());
        
        /*for(index=0;index < decisionItemSetsAL.size(); index++) {
            singleton = (String) decisionItemSetsAL.get(index);
            temp = (ArrayList) decisionItemSets.get(singleton);
            totalFuzzyMembership = 0;
            
            for(index1 = 0; index1 < temp.size(); index1++) {
                st = new StringTokenizer((String) temp.get(index1), "^");
                st.nextToken();
                totalFuzzyMembership += Float.parseFloat(st.nextToken());
            }
            
            if(totalFuzzyMembership < trainingDataSizeCutOff) {
                temp = null;
                decisionItemSets.remove(singleton);
                decisionItemSetsAL.remove(index);
                index--;
                System.out.println("Pruned at start, decisionItemSets: " + singleton + ", " + totalFuzzyMembership);
            }
       }*/
        //prune nonDecisionItemSets
        //System.out.println("decisionItemSetsAL.size(): " + decisionItemSetsAL.size());
        
        System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size());
        
        for(index=0;index < nonDecisionItemSetsAL.size(); index++) {
            singleton = (String) nonDecisionItemSetsAL.get(index);
            temp = (ArrayList) nonDecisionItemSets.get(singleton);
            totalFuzzyMembership = 0;
            
            for(index1 = 0; index1 < temp.size(); index1++) {
                st = new StringTokenizer((String) temp.get(index1), "^");
                st.nextToken();
                totalFuzzyMembership += Float.parseFloat(st.nextToken());
            }
            
            if(totalFuzzyMembership < trainingDataSizeCutOff) {
                temp = null;
                nonDecisionItemSets.remove(singleton);
                nonDecisionItemSetsAL.remove(index);
                index--;
                System.out.println("Pruned at start, nonDecisionItemSets: " + singleton + ", " + totalFuzzyMembership);
            }
       }
        
        System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size());
        //nonDecisionItemSetsAL = null;
        System.out.println("pruning over");
        
        // writing nonDecisionItemSets
        System.out.println("writing nonDecisionItemSets start");
        File file = new File("nextItemsets.txt");
        if(file.exists())
            file.delete();
        
        file = null;
        
        FileWriter fw = null;

        try {
            fw = new FileWriter("nextItemsets.txt", false);

            PrintWriter pw = new PrintWriter(fw, true);

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);
                temp = (ArrayList) nonDecisionItemSets.get(singleton);
                //pw.println("^^^Singleton^^^");
                pw.println(singleton);
                for (index1 = 0; index1 < temp.size(); index1++) {
                    //pw.println((String) temp.get(index1));
                    pw.print(temp.get(index1) + ",");
                }
                pw.println();
            }

            fw.close();
            pw.close();
            
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
        boolean nextItemsetsSizeMore = false;
        
        if(nonDecisionItemSets.size() > 1)
                nextItemsetsSizeMore = true;
        
        nonDecisionItemSets = null;
        nonDecisionItemSetsAL = null;
        System.out.println("writing nonDecisionItemSets over");
        //generate itemsets
        
        /*for(index = 0; index < decisionItemSetsAL.size(); index++) {
            nextItemsets = new HashMap();
            itemsetsFinal = new ArrayList();
            singletonDecision = (String) decisionItemSetsAL.get(index);
            
            if(index!=0) {
                nonDecisionItemSets = new HashMap();
                readNonDecisionItemSets();
                nonDecisionItemSetsAL = new ArrayList(nonDecisionItemSets.keySet());
            }
            
           for(index1 = 0; index1 < nonDecisionItemSetsAL.size(); index1++) {

                singletonNonDecision = (String) nonDecisionItemSetsAL.get(index1);
                createTwoItemsets(singletonDecision, singletonNonDecision);
            }
            
            nonDecisionItemSetsAL = null;
            nonDecisionItemSets = null;
            
            System.out.println("\n\nsingletonDecision over: " + singletonDecision);*/
            //nextItemsets = null;
            
            numberItemsetsGenerated = 0;
            
            
            /*if(nextItemsetsFile.exists())
                nextItemsetsFile.delete();
            nextItemsetsFile = null;*/
            
            while (numberItemsetsGenerated > 1 || nextItemsetsSizeMore) {
                //currentItemsets = nextItemsets;
                File nextItemsetsFile;
                System.out.println("Next iteration started..............................................  " + Calendar.getInstance().getTime());
                nextItemsetsSizeMore = false;
                numberItemsetsGenerated = 0;

                nextItemsetsFile = new File("nextItemsets.txt");
                currentItemsetsFile = new File("currentItemsets.txt");
                if(currentItemsetsFile.exists())
                    currentItemsetsFile.delete();

                //if (nextItemsetsFile.exists()) {
                    System.out.println("nextItemsetsFile Size: " + nextItemsetsFile.length());
                    currentItemsets = new HashMap();
                    try {

                        FileReader file_reader = new FileReader(nextItemsetsFile);
                        BufferedReader buf_reader = new BufferedReader(file_reader);
                        boolean start = true;
                        String tempItemset = null;
                        Integer lineLength;
                        

                        while (true) {
                            line = buf_reader.readLine();
                            if (line == null) {
                                break;
                            }
                            if (start) {
                                tempItemset = line;
                                System.out.println("tempItemset: " + tempItemset);
                                start = false;
                                continue;
                            } else {
                            
                                /*st = new StringTokenizer(line, ",");
                                tempAL = new ArrayList();
                                while (st.hasMoreTokens()) {
                                    tempAL.add(st.nextToken());
                                }*/
                                lineLength = new Integer(line.length());
                                currentItemsets.put(tempItemset, lineLength);
                                lineLength = null;
                                start = true;
                            }

                        }
                        file_reader.close();
                        buf_reader.close();
                        

                    } catch (IOException e) {
                        System.out.println("IO exception = " + e);
                        e.printStackTrace();
                    }

                    System.out.println("Rename done: " + nextItemsetsFile.renameTo(currentItemsetsFile) + "......................" + currentItemsets.size());
                    nextItemsetsFile = null;
                    
                    //System.out.println("\n\nsingletonDecision file read over: " + singletonDecision + "..." + currentItemsets.size());

                //}
                /*else
                    currentItemsets = nonDecisionItemSets; // 1st time entry*/
                
                FileWriter fwNextItemsets = null;
                PrintWriter pwNextItemsets = null;

                try {
                    fwNextItemsets = new FileWriter("nextItemsets.txt", false);

                    pwNextItemsets = new PrintWriter(fwNextItemsets, true);

                } catch (IOException ioe) {
                    System.out.println("IOException: " + ioe);
                    ioe.printStackTrace();
                }

               currentItemsetsAL = new ArrayList(currentItemsets.keySet());

               
               nextItemsets = new HashMap();
               
               FileReader frCurrentItemsets1 = null;
               BufferedReader brCurrentItemsets1 = null;
               currentItemsetsFile = new File("currentItemsets.txt");
                 
              try {
                for (index1 = 0; index1 < currentItemsetsAL.size() - 1; index1++) {


                    frCurrentItemsets1 = new FileReader(currentItemsetsFile);
                    brCurrentItemsets1 = new BufferedReader(frCurrentItemsets1);
                    
                    System.out.println("currentTidList1 read from disk start: ....." + Calendar.getInstance().getTime());


                    currentTidList1 = getTidList((String) currentItemsetsAL.get(index1), brCurrentItemsets1);
                    
                    System.out.println("currentTidList1 read from disk end: " + currentItemsetsAL.get(index1) + "....." + Calendar.getInstance().getTime());

                    frCurrentItemsets1.close();
                    brCurrentItemsets1.close();

                    for (index2 = index1 + 1; index2 < currentItemsetsAL.size(); index2++) {

                        System.out.println("createNItemsets start: ...." + Calendar.getInstance().getTime());

                        createNItemsets((String) currentItemsetsAL.get(index1), (String) currentItemsetsAL.get(index2), currentTidList1, index2, pwNextItemsets);
                        
                        System.out.println("createNItemsets end: ...." + Calendar.getInstance().getTime());
                    }

                    /*tempAL = (ArrayList) currentItemsets.get((String) currentItemsetsAL.get(index1));
                    tempAL = null;*/
                    currentTidList1 = null;
                    
                    //currentItemsets.remove(currentItemsetsAL.get(index1));
                    System.out.println("Removed from current itemsetsAL: " + (String) currentItemsetsAL.get(index1) + "..." + index1 + "....................." + currentItemsetsAL.size());
                    currentItemsetsAL.remove(index1);

                    index1--;

                }
                currentItemsets = null;
                currentItemsetsAL = null;
                nextItemsets = null;


            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }

        //System.out.println("\n\nsingletonDecision generate n itemsets over: " + singletonDecision);
            System.out.println("Current iteration ended..............................................  " + Calendar.getInstance().getTime());

        }
            

        System.out.println("itemsetsFinal:" + itemsetsFinal);
        System.out.println("Program ended..............................................  " + Calendar.getInstance().getTime());
    //itemsetsFinal = null;
    //}

    }

    private ArrayList getTidList(String itemset, BufferedReader brCurrentItemsets) {
        String line = null;
        ArrayList tidList = null;
        boolean getTidList = false;
        StringTokenizer st = null;
        int skipLength;
        while (true) {
            try {
                line = brCurrentItemsets.readLine();
                
                /*if(!getTidList)
                    System.out.println("line: " + line);*/

                if (line == null) {
                    break;
                }

                if (line.equalsIgnoreCase(itemset)) {
                    getTidList = true;
                    line = null;
                    continue;

                } else if (getTidList) {
                    st = new StringTokenizer(line, ",");
                    tidList = new ArrayList();
                    while (st.hasMoreTokens()) {
                        tidList.add(st.nextToken());
                    }
                    line = null;

                    return tidList;

                } else {
                    skipLength = ((Integer) currentItemsets.get(line)).intValue() + 2;
                    //System.out.println("skipLength: " + skipLength);
                    brCurrentItemsets.skip(skipLength);
                    continue;
                }
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
        }
        System.out.println("Fatal write while retrieving tidlist for: " + itemset);
        return null;
    }
    
    
    /*private void readNonDecisionItemSets() {

        try {
            File file = new File("nonDecisionItemSets.txt");
            FileReader file_reader = new FileReader(file);
            BufferedReader buf_reader = new BufferedReader(file_reader);
            String line, singleton = null;
            ArrayList temp = null;
            boolean newSingleton = false;


            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }

                if (line.equalsIgnoreCase("^^^Singleton^^^")) {
                    if (temp != null) {
                        nonDecisionItemSets.put(singleton, temp);
                    }
                    temp = new ArrayList();
                    newSingleton = true;
                    continue;
                } else if (newSingleton) {
                    singleton = line;
                    newSingleton = false;
                    continue;
                } else {
                    temp.add(line);
                }
            }
            
            nonDecisionItemSets.put(singleton, temp);
            file_reader.close();
            buf_reader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }*/
    
    private void createNItemsets(String itemset1, String itemset2, ArrayList currentTidList1, int index2, PrintWriter pwNextItemsets) {
       boolean mismatch = false, returnMethod; 
       StringTokenizer st;
       HashMap tempHashMap = new HashMap();
       String token = null, newItemset, tid;
       int index;
       ArrayList nextItemsetsAL, tempItemset1, tempItemset2 = null, tempAL, tempAL1, twoItemsetsAL = new ArrayList();
       float fuzzyMembership, fuzzyMembershipTemp, fuzzyMembershipTotal = 0;
       
       FileReader frCurrentItemsets2 = null;
       BufferedReader brCurrentItemsets2 = null;
       
       //create newItemset
       
       st = new StringTokenizer(itemset2, ",");
       while(st.hasMoreTokens()) {
           tempHashMap.put(st.nextToken(), null);
       }
       
       st = new StringTokenizer(itemset1, ",");
       while(st.hasMoreTokens()) {
           token = st.nextToken();
           
           if(mismatch && !tempHashMap.containsKey(token)) {
               tempHashMap = null;
               return;
           }
               
           else if(!tempHashMap.containsKey(token))
               mismatch = true;
        }
       
       newItemset = itemset1;
       tempHashMap = null;
       tempHashMap = new HashMap();
       
       st = new StringTokenizer(itemset1, ",");
       while(st.hasMoreTokens()) {
           tempHashMap.put(st.nextToken(), null);
       }
       
       st = new StringTokenizer(itemset2, ",");
       while(st.hasMoreTokens()) {
            token = st.nextToken();
            
            if(!tempHashMap.containsKey(token)) {
                System.out.println("token: " + token);
                newItemset = newItemset + "," + token; 
                break;
            }
                
       }
       
       System.out.println("newItemset: " + newItemset);
       System.out.println("itemset1: " + itemset1);
       System.out.println("itemset2: " + itemset2);
        //create newItemset - finish
       
       //check if newItemset already in nextItemsets
       tempHashMap.put(token, null);
       
       nextItemsetsAL = new ArrayList(nextItemsets.keySet());
       
       
       for(index = 0; index < nextItemsetsAL.size(); index++) {
           
           returnMethod = true;
           
           st = new StringTokenizer((String) nextItemsetsAL.get(index), ",");
           while(st.hasMoreTokens()) {
               token = st.nextToken();
               if(!tempHashMap.containsKey(token)) {
                   returnMethod = false;
                   break;
               }
           }
           if(returnMethod) {
               System.out.println("returnMethod: " + returnMethod);
               return;
           }
               
       }
       
       tempHashMap = null;
       nextItemsetsAL = null;
       //check if newItemset already in itemsetFinal - finish
       
       tempItemset1 = currentTidList1;
       //get tempItemset2
       
        try {
            frCurrentItemsets2 = new FileReader(currentItemsetsFile);
            brCurrentItemsets2 = new BufferedReader(frCurrentItemsets2);


            System.out.println("currentTidList2 read from disk start: ...." + Calendar.getInstance().getTime());

            tempItemset2 = getTidList((String) currentItemsetsAL.get(index2), brCurrentItemsets2);

            System.out.println("currentTidList2 read from disk end: " + currentItemsetsAL.get(index2) + "......" + Calendar.getInstance().getTime());

            frCurrentItemsets2.close();
            brCurrentItemsets2.close();

        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
       
       
       
       if(tempItemset1.size() <= tempItemset2.size()) {
            tempAL = tempItemset1;
            tempAL1 = tempItemset2;
        }
            
       else {
            tempAL = tempItemset2;
            tempAL1 = tempItemset1;
        }
        tempItemset1 = null;
        tempItemset2 = null;
        
        tempHashMap = new HashMap();
        
        for (index = 0; index < tempAL1.size(); index++) {
            st = new StringTokenizer((String) tempAL1.get(index), "^");
            tempHashMap.put(st.nextToken(), st.nextToken());
        }
        tempAL1 = null;
        
        
        for(index = 0; index < tempAL.size(); index++) {
            st = new StringTokenizer((String) tempAL.get(index), "^");
            
            tid = st.nextToken();
            fuzzyMembershipTemp = Float.parseFloat(st.nextToken());
            
            if(tempHashMap.containsKey(tid)) {
                fuzzyMembership = Float.parseFloat((String) tempHashMap.get(tid));
                
                if(fuzzyMembershipTemp < fuzzyMembership)
                    fuzzyMembership = fuzzyMembershipTemp;
                
                fuzzyMembershipTotal += fuzzyMembership;
                twoItemsetsAL.add(tid + "^" + fuzzyMembership);
            }
        }
        tempHashMap = null;
        tempAL = null;
        
        if(fuzzyMembershipTotal >= trainingDataSizeCutOff) {
            if(itemsetsFinal == null)
                itemsetsFinal = new ArrayList();
            itemsetsFinal.add(newItemset + "^" + fuzzyMembershipTotal);
            
            pwNextItemsets.println(newItemset);
            for(index=0; index < twoItemsetsAL.size(); index++) {
                pwNextItemsets.print(twoItemsetsAL.get(index) + ",");
            }
            pwNextItemsets.println();
            twoItemsetsAL = null;
            
            numberItemsetsGenerated++;
            nextItemsets.put(newItemset, null);
            System.out.println("newItemset: " + newItemset + "^^^" + fuzzyMembershipTotal);
        }
        else {
            twoItemsetsAL = null;
            System.out.println("newItemset not put in: " + newItemset + "^^^" + fuzzyMembershipTotal);
        }
        
    }
    
    /*private void createTwoItemsets(String singletonDecision, String singletonNonDecision) {
        ArrayList tempDecision, tempNonDecision;
        int index;
        float fuzzyMembership, fuzzyMembershipTemp, fuzzyMembershipTotal = 0;
        HashMap tempHashMap = new HashMap();
        ArrayList tempAL, tempAL1, twoItemsetsAL = new ArrayList();
        StringTokenizer st;
        String tid, resultItemset;
        
        tempDecision = (ArrayList) decisionItemSets.get(singletonDecision);
        tempNonDecision = (ArrayList) nonDecisionItemSets.get(singletonNonDecision);
        
        if(tempDecision.size() <= tempNonDecision.size()) {
            tempAL = tempDecision;
            tempAL1 = tempNonDecision;
        }
            
        else {
            tempAL = tempNonDecision;
            tempAL1 = tempDecision;
        }
        
        
        for (index = 0; index < tempAL1.size(); index++) {
            st = new StringTokenizer((String) tempAL1.get(index), "^");
            tempHashMap.put(st.nextToken(), st.nextToken());
        }

        
        
        for(index = 0; index < tempAL.size(); index++) {
            st = new StringTokenizer((String) tempAL.get(index), "^");
            
            tid = st.nextToken();
            fuzzyMembershipTemp = Float.parseFloat(st.nextToken());
            
            if(tempHashMap.containsKey(tid)) {
                fuzzyMembership = Float.parseFloat((String) tempHashMap.get(tid));
                
                if(fuzzyMembershipTemp < fuzzyMembership)
                    fuzzyMembership = fuzzyMembershipTemp;
                
                fuzzyMembershipTotal += fuzzyMembership;
                twoItemsetsAL.add(tid + "^" + fuzzyMembership);
            }
        }
        tempHashMap = null;
        
        if(fuzzyMembershipTotal >= trainingDataSizeCutOff) {
            resultItemset = singletonDecision + "," + singletonNonDecision;
            if(itemsetsFinal == null)
                itemsetsFinal = new ArrayList();
            itemsetsFinal.add(resultItemset + "^" + fuzzyMembershipTotal);
            nextItemsets.put(resultItemset, twoItemsetsAL);
            numberItemsetsGenerated++;
            System.out.println("resultItemset: " + resultItemset + "," + fuzzyMembershipTotal);
        }
        else
            twoItemsetsAL = null;
        
    }*/
}
