
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

public class FApriori {

    private ArrayList classLabel = new ArrayList();
    private ArrayList classType = new ArrayList();
    private HashMap classFilename = new HashMap();
    private ArrayList trainingData = new ArrayList();
    private HashMap numAttribute = new HashMap();
    private HashMap numValue = new HashMap();
    private String decisionClassLabel = null;
    
    private float support=1;
    private float trainingDataSizeCutOff = 0;
    private long trainingDataSize = 0;
    private int countK=0;
    
    private ArrayList nonDecisionItemSetsAL = null;
    
    private HashMap itemsetsFinal = new HashMap();
    
    private HashMap nextItemsets = null;
    private HashMap currentItemsets = null;
    private ArrayList currentItemsetsAL = null;
    private ArrayList currentItemsetsALNB = null;
    private ArrayList previousItemsetsALNB = null;
    
    private HashMap nonDecisionItemSets = null;

    private File fuzzyTrainingDataFile;
    private FileReader file_readerGlobal;
    private BufferedReader buf_readerGlobal;

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
        

    }
    
    public void generateAssociationRules() {
        int index1, index2;

        fuzzyTrainingDataFile = new File("fuzzyTrainingData0.txt");

        try {
            file_readerGlobal = new FileReader(fuzzyTrainingDataFile);

            buf_readerGlobal = new BufferedReader(file_readerGlobal);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

      // 1st phase        
        //for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
        nonDecisionItemSets = new HashMap();

        try {
            file_readerGlobal = new FileReader(fuzzyTrainingDataFile);

            buf_readerGlobal = new BufferedReader(file_readerGlobal);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("singletons generating" + "..." + Calendar.getInstance().getTime());
        
        generateSingletons();

        try {
            file_readerGlobal.close();

            buf_readerGlobal.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("singletons generated" + "..." + Calendar.getInstance().getTime());

        //generate itemsets

        nextItemsets = nonDecisionItemSets;
        nonDecisionItemSets = null;

        for (countK = 2; nextItemsets.size() > 1; countK++) {

            currentItemsets = nextItemsets;
            
            System.out.println("Next iteration started.............................................." + countK + "..." + Calendar.getInstance().getTime());

            currentItemsetsAL = new ArrayList(currentItemsets.keySet());

            nextItemsets = new HashMap();
            
            currentItemsetsALNB = new ArrayList();

            for (index1 = 0; index1 < currentItemsetsAL.size() - 1; index1++) {
                for (index2 = index1 + 1; index2 < currentItemsetsAL.size(); index2++) {
                    //System.out.println("Start~~~~" + Calendar.getInstance().getTime());
                    createNItemsets((String) currentItemsetsAL.get(index1), (String) currentItemsetsAL.get(index2));
                    //System.out.println("End~~~~" + Calendar.getInstance().getTime());
                }

                currentItemsets.remove(currentItemsetsAL.get(index1));
                System.out.println("Removed from current itemsetsAL: " + (String) currentItemsetsAL.get(index1) + "..." + index1 + "..." + currentItemsetsAL.size() + "..." + Calendar.getInstance().getTime());
                currentItemsetsAL.remove(index1);

                index1--;

            }

            currentItemsets = null;
            currentItemsetsAL = null;
            
            try {
                file_readerGlobal = new FileReader(fuzzyTrainingDataFile);

                buf_readerGlobal = new BufferedReader(file_readerGlobal);
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            
            System.out.println("Start ~~~~ countAndPrune()" + Calendar.getInstance().getTime());

            countAndPrune();
            
            System.out.println("End ~~~~ countAndPrune()" + Calendar.getInstance().getTime());

            try {
                file_readerGlobal.close();

                buf_readerGlobal.close();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            
            previousItemsetsALNB = currentItemsetsALNB;
            currentItemsetsALNB = null;

            System.out.println("Next iteration FINISH.............................................." + countK + "..." + Calendar.getInstance().getTime());
        }

        System.out.println("itemsetsFinal:" + itemsetsFinal);

        FileWriter fw = null;

        try {
            fw = new FileWriter("fapriori_results.txt", false);

            PrintWriter pw = new PrintWriter(fw, true);
            String itemset;

            currentItemsetsAL = new ArrayList(itemsetsFinal.keySet());

            for (index1 = 0; index1 < currentItemsetsAL.size(); index1++) {
                itemset = (String) currentItemsetsAL.get(index1);
                pw.print(itemset + "\t");
                pw.print(itemsetsFinal.get(itemset) + "\n");
            }
            pw.close();
            fw.close();

        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }
        currentItemsetsAL = null;

        System.out.println(Calendar.getInstance().getTime());
    }
    
    private void countAndPrune() {

        String line = null, singleton, itemset;
        StringTokenizer st = null, st1, st2;
        HashMap tempHashMap = null;
        ArrayList nextItemsetsAL;
        int index, index1;
        boolean notAddItemset, firstTime;
        float fuzzyMembership, tempFuzzyMembership;

        nextItemsetsAL = new ArrayList(nextItemsets.keySet());
                
        index1 = -1;
        while (true) {
            try {
                line = buf_readerGlobal.readLine();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            
            if(line == null)
                break;
            
            index1++;
            
            //System.out.println("\nindex1:" + index1 + "..." + Calendar.getInstance().getTime());
            
            st2 = new StringTokenizer(line, ",");
            tempHashMap = new HashMap();
            
            while(st2.hasMoreTokens()) { // put line in HashMap
                st1 = new StringTokenizer(st2.nextToken(), "^");
                tempHashMap.put(st1.nextToken(), st1.nextToken());
            }
            //System.out.println("\ntempHashMap:" + tempHashMap + "..." + Calendar.getInstance().getTime());
           
            for (index = 0; index < nextItemsetsAL.size(); index++) {
                
                fuzzyMembership = 0;
                notAddItemset = false;
                firstTime = true;
                itemset = (String) nextItemsetsAL.get(index);
                //System.out.println("\nitemset:" + itemset + "..." + Calendar.getInstance().getTime());
                st = new StringTokenizer(itemset, ",");
                
                while(st.hasMoreTokens()) {
                    singleton = st.nextToken();
                    if(tempHashMap.containsKey(singleton)){
                        tempFuzzyMembership = Float.parseFloat((String) tempHashMap.get(singleton));
                        
                        if(firstTime) {
                            fuzzyMembership = tempFuzzyMembership;
                            firstTime = false;
                        }
                        else if(tempFuzzyMembership < fuzzyMembership) {
                            fuzzyMembership = tempFuzzyMembership;
                            
                        }
                            
                    }
                    else {
                        notAddItemset = true;
                        break;
                    }
                    
                }
                if(!notAddItemset) {
                    
                    tempFuzzyMembership = ((Float) nextItemsets.get(itemset));
                    fuzzyMembership += tempFuzzyMembership;
                    nextItemsets.remove(itemset);
                    nextItemsets.put(itemset, new Float(fuzzyMembership));
                    
                    //System.out.println("\nitemset:" + itemset + "......." + tempFuzzyMembership  + "............" + fuzzyMembership);
                    
                }
            }
            tempHashMap = null;
            
            //System.out.println("index1 End:" + index1 + "..." + Calendar.getInstance().getTime());
        }
        
        //prune
        System.out.println("Prune:" + countK + "..." + nextItemsets.size() + "..." + Calendar.getInstance().getTime());
        
        for (index = 0; index < nextItemsetsAL.size(); index++) {
            itemset = (String) nextItemsetsAL.get(index);
            fuzzyMembership = ((Float) nextItemsets.get(itemset));
            
            if(fuzzyMembership >= trainingDataSizeCutOff) {
                itemsetsFinal.put(itemset, fuzzyMembership);
                System.out.println("itemset put in itemsetFinal..." + itemset + "..." + fuzzyMembership + " >= " + trainingDataSizeCutOff);
            }
            else {
                nextItemsets.remove(itemset);
                currentItemsetsALNB.add(itemset);
                System.out.println("itemset NOT put in itemsetFinal..." + itemset + "..." + fuzzyMembership + " < " + trainingDataSizeCutOff);
            }
        }
        nextItemsetsAL = null;
        System.out.println("Prune End:" + countK + "..." + nextItemsets.size() + "..." + Calendar.getInstance().getTime());
    }

    
    private void createNItemsets(String itemset1, String itemset2) {
       boolean mismatch = false, returnMethod;
       StringTokenizer st;
       HashMap tempHashMap = new HashMap();
       String token = null, newItemset;
       int index;
       ArrayList nextItemsetsAL;
       
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
       
       tempHashMap.put(token, null);
   
        //check if newItemset already in nextItemsets
        nextItemsetsAL = new ArrayList(nextItemsets.keySet());

        for (index = 0; index < nextItemsetsAL.size(); index++) {

            returnMethod = true;

            st = new StringTokenizer((String) nextItemsetsAL.get(index), ",");
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                if (!tempHashMap.containsKey(token)) {
                    returnMethod = false;
                    break;
                }
            }
            if (returnMethod) {
                System.out.println("returnMethod: " + returnMethod);
                return;
            }

        }
        
        nextItemsetsAL = null;
        //check if newItemset already in nextItemsets - finish
        
        // apriori prune
        
        if (previousItemsetsALNB != null) {

            for (index = 0; index < previousItemsetsALNB.size(); index++) {
                st = new StringTokenizer((String) previousItemsetsALNB.get(index), ",");
                returnMethod = true;
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    if (!tempHashMap.containsKey(token)) {
                        returnMethod = false;
                        break;
                    }
                }
                if (returnMethod) {
                    System.out.println("previousItemsetsALNB returnMethod: " + returnMethod);
                    return;
                }
            }
        }
        tempHashMap = null;
        
        nextItemsets.put(newItemset, new Float(0));
        System.out.println("newItemset added to newItemsets: " + newItemset);
    
    }


    private void generateSingletons() {
        try {
            String line, singleton, fuzzyMembership;
            StringTokenizer st, st1;
            int index;
            float totalFuzzyMembership, newFuzzyMembership;
            
            FileReader file_reader1 = new FileReader("ARMParametersFApriori.txt");
            BufferedReader buf_reader1 = new BufferedReader(file_reader1);
            
            support = Float.parseFloat(buf_reader1.readLine());
            
            file_reader1.close();
            buf_reader1.close();
            
            System.out.println(Calendar.getInstance().getTime());
            //System.out.println("trainingDataSize, trainingDataSizeCutOff, support: " + trainingDataSize + "..." + trainingDataSizeCutOff + "..." +  support);
            while (true) {
                line = buf_readerGlobal.readLine();
                if(line==null)
                    break;
                
                trainingDataSize++;

                //System.out.println("line:" + line);
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    singleton = st1.nextToken();
                    //System.out.println("\n\nsingleton:" + singleton);
                    fuzzyMembership = st1.nextToken();

                    //put counts in nonDecisionItemSets
                    
                    if(nonDecisionItemSets.containsKey(singleton)) {
                        
                        newFuzzyMembership = Float.parseFloat(fuzzyMembership);
                        newFuzzyMembership += (Float) nonDecisionItemSets.get(singleton);
                        
                        nonDecisionItemSets.remove(singleton);
                        //System.out.println("fuzzyMembership: " + fuzzyMembership);
                        nonDecisionItemSets.put(singleton, new Float(newFuzzyMembership));
                    }
                    else {
                        nonDecisionItemSets.put(singleton, new Float(fuzzyMembership));
                    }
                }
            }
            trainingDataSizeCutOff = ((float) trainingDataSize) * support;
            System.out.println("trainingDataSize, trainingDataSizeCutOff, support: " + trainingDataSize + "..." + trainingDataSizeCutOff + "..." +  support);

            //pruning
            nonDecisionItemSetsAL = new ArrayList(nonDecisionItemSets.keySet());

            System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size());

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);

                totalFuzzyMembership = ((Float) nonDecisionItemSets.get(singleton));
                if (totalFuzzyMembership < trainingDataSizeCutOff) {
                    nonDecisionItemSets.remove(singleton);
                    nonDecisionItemSetsAL.remove(index);
                    index--;
                    //currentItemsetsALNB.add(singleton);
                    System.out.println("Pruned at start, nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " < " + trainingDataSizeCutOff);
                }
                else {
                    itemsetsFinal.put(singleton, new Float(totalFuzzyMembership));
                    System.out.println("nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " >= " + trainingDataSizeCutOff);
                }
            }

            System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size());
            nonDecisionItemSetsAL = null;
            System.out.println("pruning over");
        //pruning over

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

        public void generateCARs(String attribute, float confidence) {
        String fuzzyTrainingDataFile = "fapriori_results.txt";

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
                if (decisionClass != null) {
                    itemsetNew.append(decisionClass);
                }
                itemsetsMapNew.put(itemsetNew.toString().trim(), itemsetsMapOld.get(itemset));
            }

            tempAL = new ArrayList(itemsetsMapNew.keySet());
            pw = new PrintWriter(new FileWriter("CAR_fapriori_" + attribute + ".txt"));
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
