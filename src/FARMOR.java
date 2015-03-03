
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

public class FARMOR {

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
    private int numberOfPartitions;
    private long partitionSize;
    private int indexPartition;
    private int countK=0;
    private float lastPartitionCutOff;
    
    private ArrayList nonDecisionItemSetsAL = null;
    
    private HashMap itemsetsFinal = new HashMap();
    private HashMap itemsetsFinalPartition = new HashMap();
    private ArrayList newItemsetUpdate = null;
    
    private HashMap nextItemsets = null;
    private HashMap currentItemsets = null;
    private ArrayList currentItemsetsAL = null;
    
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
                
        fuzzyTrainingDataFile = new File("fuzzytrainingdata.txt");
        
        initialize();
        
        try {
            file_readerGlobal = new FileReader(fuzzyTrainingDataFile);

            buf_readerGlobal = new BufferedReader(file_readerGlobal);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        


        // 1st phase        
        for (indexPartition = 1; indexPartition <= numberOfPartitions; indexPartition++) {
            System.out.println("indexPartition............................................." + indexPartition + "..." + Calendar.getInstance().getTime());
            nonDecisionItemSets = new HashMap();
            generatePartitionSingletons();
            System.out.println("singletons generated");

            //generate itemsets

            nextItemsets = nonDecisionItemSets;
            nonDecisionItemSets = null;

            for (countK = 2; nextItemsets.size() > 1; countK++) {

                currentItemsets = nextItemsets;

                System.out.println("Next iteration started.............................................." + countK + "..." + Calendar.getInstance().getTime());

                currentItemsetsAL = new ArrayList(currentItemsets.keySet());


                nextItemsets = new HashMap();
                newItemsetUpdate = new ArrayList();

                for (index1 = 0; index1 < currentItemsetsAL.size() - 1; index1++) {
                    for (index2 = index1 + 1; index2 < currentItemsetsAL.size(); index2++) {
                        System.out.println("Start~~~~" + Calendar.getInstance().getTime());
                        createNItemsets((String) currentItemsetsAL.get(index1), (String) currentItemsetsAL.get(index2));
                        System.out.println("End~~~~" + Calendar.getInstance().getTime());
                    }

                    currentItemsets.remove(currentItemsetsAL.get(index1));
                    System.out.println("Removed from current itemsetsAL: " + (String) currentItemsetsAL.get(index1) + "..." + index1 + "..." + currentItemsetsAL.size() + "..." + Calendar.getInstance().getTime());
                    currentItemsetsAL.remove(index1);

                    index1--;

                }
                
                currentItemsets = null;
                currentItemsetsAL = null;
                newItemsetUpdate = null;
                
                System.out.println("Next iteration FINISH.............................................." + countK + "..." + Calendar.getInstance().getTime());
            }
            
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
        
        FileWriter fw = null;
         
        try {
            fw = new FileWriter("firstphasedata.txt", false);

            PrintWriter pw = new PrintWriter(fw, true);
            String itemset;

            currentItemsetsAL = new ArrayList(itemsetsFinal.keySet());
            pw.println(trainingDataSizeCutOff);
            pw.println(numberOfPartitions);
            pw.println(partitionSize);

            for (index1 = 0; index1 < currentItemsetsAL.size(); index1++) {
                itemset = (String) currentItemsetsAL.get(index1);
                pw.println(itemset);
                pw.println(itemsetsFinal.get(itemset));
                pw.println(itemsetsFinalPartition.get(itemset));
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

    
    private void createNItemsets(String itemset1, String itemset2) {
       boolean mismatch = false, returnMethod, newItemsetPresentInItemsetFinal = false, itemsetFinalUpdate = false, removeSuperSets = false; 
       StringTokenizer st;
       HashMap tempHashMap = new HashMap(), tempHashMap1;
       String token = null, newItemset, tid, oldItemset = null;
       int index, currentSingletonAddedInPartition;
       ArrayList nextItemsetsAL, tempItemset1, tempItemset2, tempAL, tempAL1, twoItemsetsAL = new ArrayList();
       float fuzzyMembership, fuzzyMembershipTemp, fuzzyMembershipTotal = 0, currentCutOff;
       
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
       
       //check if new itemset in itemsetsFinal
       nextItemsetsAL = new ArrayList(itemsetsFinal.keySet());
       
       
        for (index = 0; index < nextItemsetsAL.size(); index++) {

            returnMethod = true;

            st = new StringTokenizer((String) nextItemsetsAL.get(index), ",");
            if(st.countTokens()!=countK )
                continue;
            
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
                    System.out.println("itemsetFinalUpdate: " + oldItemset + ";" +  itemsetFinalUpdate);
                    return;
                }

            }

             //calculate cut off
            
            currentSingletonAddedInPartition = ((Integer) itemsetsFinalPartition.get(oldItemset)).intValue();
            
            if (indexPartition != numberOfPartitions) 
                currentCutOff = (indexPartition - currentSingletonAddedInPartition + 1) * trainingDataSizeCutOff / numberOfPartitions;
            
            else 
                currentCutOff = ((indexPartition - currentSingletonAddedInPartition) * trainingDataSizeCutOff / numberOfPartitions) + lastPartitionCutOff;
            
        } 
        
        else {
             //calculate cut off
           
            if (indexPartition != numberOfPartitions) 
                currentCutOff = trainingDataSizeCutOff / numberOfPartitions;
            else 
                currentCutOff = lastPartitionCutOff;
        }
       
       //check if new itemset in newItemsetUpdate - finish
        
        /*else {
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
        }*/
       tempHashMap1 = tempHashMap;
       tempHashMap = null;
       nextItemsetsAL = null;
       //check if newItemset already in nextItemsets - finish
       
       //generate tidlist
       
       tempItemset1 = (ArrayList) currentItemsets.get(itemset1);
       tempItemset2 = (ArrayList) currentItemsets.get(itemset2);
        
       if(tempItemset1.size() <= tempItemset2.size()) {
            tempAL = tempItemset1;
            tempAL1 = tempItemset2;
        }
            
       else {
            tempAL = tempItemset2;
            tempAL1 = tempItemset1;
        }
        
        tempHashMap = new HashMap();
        tempItemset1 = null;
        tempItemset2 = null;
        
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
        //generate tidlist -- finish
        
        //update in itemsetsFinal if newItemset already present in itemsetsFinal
        
        
        if (itemsetFinalUpdate && newItemsetPresentInItemsetFinal) {
           
            fuzzyMembership = ((Float) itemsetsFinal.get(oldItemset)).floatValue();
            fuzzyMembershipTotal += fuzzyMembership;
            
            itemsetsFinal.remove(oldItemset);
            itemsetsFinal.put(oldItemset, new Float(fuzzyMembershipTotal));
            
            
            //indexPartitionOld = (String) itemsetsFinalPartition.get(oldItemset);
            //itemsetsFinalPartition.remove(oldItemset);
            //itemsetsFinalPartition.put(oldItemset, indexPartitionOld + ",*" + indexPartition + "*");
            
            newItemsetUpdate.add(oldItemset);
            
            //freq itemsets would be expanded further
            
            if(fuzzyMembershipTotal >= currentCutOff) {
                nextItemsets.put(newItemset, twoItemsetsAL);
                System.out.println("newItemset put in nextitemsets:" + newItemset + "____________________" + fuzzyMembershipTotal + " >>>>>>>>> "+ currentCutOff);
            }
            
            else 
                removeSuperSets = true;
            
            twoItemsetsAL = null;
            
            System.out.println("oldItemset: " + oldItemset + "==========================" + fuzzyMembership + "============" + fuzzyMembershipTotal);
            return;
            
        }
        
        //update in itemsetsFinal if newItemset already present in itemsetsFinal - finish
        
        else if(fuzzyMembershipTotal >= currentCutOff) {
            
            itemsetsFinal.put(newItemset, new Float(fuzzyMembershipTotal));
            itemsetsFinalPartition.put(newItemset, indexPartition);
            
            nextItemsets.put(newItemset, twoItemsetsAL);
            
            newItemsetUpdate.add(newItemset);
            twoItemsetsAL = null;
            System.out.println("newItemset: " + newItemset + "---------------------------------------" + fuzzyMembershipTotal + " >>>>>>>>> "+ currentCutOff);
        
        
        } else {
            itemsetsFinal.put(newItemset, new Float(fuzzyMembershipTotal));
            itemsetsFinalPartition.put(newItemset, indexPartition);
            
            newItemsetUpdate.add(newItemset);
            
            twoItemsetsAL = null;
            removeSuperSets = true;
            System.out.println("newItemset NBE: " + newItemset + "^^^NBE^^^" + fuzzyMembershipTotal);
        }
        
        //superset removal of NB.
        
        if (removeSuperSets) {
            
            tempHashMap = tempHashMap1;
            tempHashMap1 = null;
            
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
        }

    }

    private void initialize() {
        try {
            FileReader file_reader1 = new FileReader("ARMparameters.txt"), file_reader;
            BufferedReader buf_reader1 = new BufferedReader(file_reader1), buf_reader;
            String line;
            

            support = Float.parseFloat(buf_reader1.readLine());
            numberOfPartitions = Integer.parseInt(buf_reader1.readLine());
            
            file_reader1.close();
            buf_reader1.close();
            
            file_reader = new FileReader(fuzzyTrainingDataFile);
            buf_reader = new BufferedReader(file_reader);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                /*st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    singleton = st1.nextToken();

                    if (singletonList.indexOf(singleton) != -1) {
                        singletonList.add(singleton);
                    }
                }*/

                trainingDataSize++;
            }

            file_reader.close();
            buf_reader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        trainingDataSizeCutOff = ((float) trainingDataSize) * support;
        partitionSize = trainingDataSize / numberOfPartitions;
        
        lastPartitionCutOff = ((float) (trainingDataSize - partitionSize * (numberOfPartitions - 1))) * support;
        

        System.out.println(Calendar.getInstance().getTime());
        System.out.println("trainingDataSize, trainingDataSizeCutOff, numberOfPartitions, partitionSize, support, lastPartitionCutOff: " + trainingDataSize + "..." + trainingDataSizeCutOff + "..." + numberOfPartitions + "..." + partitionSize + "..." + support + "..." + lastPartitionCutOff);

        return;

    }

    private void generatePartitionSingletons() {
        try {
            String line, singleton, fuzzyMembership;
            StringTokenizer st, st1;
            int index, currentSingletonAddedInPartition;
            ArrayList temp;
            float totalFuzzyMembership, currentCutOff, newFuzzyMembership;
            
            if(indexPartition==1) {
                buf_readerGlobal.readLine();
                buf_readerGlobal.readLine();
            }

            for (index = 0; index < partitionSize; index++) {
                line = buf_readerGlobal.readLine();
                if(line==null)
                    break;

                //System.out.println("line:" + line);
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    singleton = st1.nextToken();
                    //System.out.println("\n\nsingleton:" + singleton);
                    fuzzyMembership = st1.nextToken();

                    if (!nonDecisionItemSets.containsKey(singleton)) {
                        temp = new ArrayList();
                        temp.add(index + "^" + fuzzyMembership);
                        nonDecisionItemSets.put(singleton, temp);
                        temp = null;
                    
                    } else {
                        temp = (ArrayList) nonDecisionItemSets.get(singleton);
                        temp.add(index + "^" + fuzzyMembership);
                        nonDecisionItemSets.remove(singleton);
                        nonDecisionItemSets.put(singleton, temp);
                        temp = null;
                    }
                    
                    //put counts in itemsetFinal
                    
                    if(itemsetsFinal.containsKey(singleton)) {
                        
                        newFuzzyMembership = Float.parseFloat(fuzzyMembership);
                        newFuzzyMembership += ((Float) itemsetsFinal.get(singleton)).floatValue();
                        
                        itemsetsFinal.remove(singleton);
                        //System.out.println("fuzzyMembership: " + fuzzyMembership);
                        itemsetsFinal.put(singleton, new Float(newFuzzyMembership));
                    }
                    else {
                        itemsetsFinal.put(singleton, new Float(fuzzyMembership));
                        itemsetsFinalPartition.put(singleton, indexPartition);
                    }
                }
            }

            //pruning
            nonDecisionItemSetsAL = new ArrayList(nonDecisionItemSets.keySet());

            System.out.println("nonDecisionItemSetsAL.size(): " + nonDecisionItemSetsAL.size());

            for (index = 0; index < nonDecisionItemSetsAL.size(); index++) {
                singleton = (String) nonDecisionItemSetsAL.get(index);
                //temp = (ArrayList) nonDecisionItemSets.get(singleton);
                totalFuzzyMembership = 0;

                /*for (index1 = 0; index1 < temp.size(); index1++) {
                    st = new StringTokenizer((String) temp.get(index1), "^");
                    st.nextToken();
                    totalFuzzyMembership += Float.parseFloat(st.nextToken());
                }*/
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
                }
                else
                    System.out.println("nonDecisionItemSets, currentCutOff: " + singleton + ", " + totalFuzzyMembership + " >= " + currentCutOff);
                //temp = null;
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
    
    public void generateAssociationRulesSecondPhase() {
        String line = null, itemset;
        int index1, partitionNumber;
        File file = new File("firstphasedata.txt");
        FileReader fileReader;
        BufferedReader bufferedReader;
        ArrayList itemsetsFinalAL = null;
        FileWriter fw = null;
        PrintWriter pw  = null;
        float totalFuzzyMembership;

        fuzzyTrainingDataFile = new File("fuzzytrainingdata.txt");
        
        System.out.println("Initialize.... " + Calendar.getInstance().getTime());

        try {
            file_readerGlobal = new FileReader(fuzzyTrainingDataFile);
            buf_readerGlobal = new BufferedReader(file_readerGlobal);
            
            fw = new FileWriter("FinalResults.txt", false);

            pw = new PrintWriter(fw, true);
            

            if (itemsetsFinal == null || itemsetsFinal.size() == 0) {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);

                trainingDataSizeCutOff = Float.parseFloat(bufferedReader.readLine());
                numberOfPartitions = Integer.parseInt(bufferedReader.readLine());
                partitionSize = Long.parseLong(bufferedReader.readLine());

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
            
            for(index1 = 0; index1 < itemsetsFinalAL.size(); index1++) {
                itemset = (String) itemsetsFinalAL.get(index1);
                partitionNumber = ((Integer) itemsetsFinalPartition.get(itemset)).intValue();
                totalFuzzyMembership = ((Float) itemsetsFinal.get(itemset)).floatValue();
                
                if(partitionNumber == indexPartition) {
                    
                    if (totalFuzzyMembership >= trainingDataSizeCutOff) {
                        pw.println(itemset);
                        pw.println(totalFuzzyMembership);
                    }
                    
                    itemsetsFinal.remove(itemset);
                    itemsetsFinalPartition.remove(itemset);
                    itemsetsFinalAL.remove(index1);
                    index1--;
                }
            }
            
            if(itemsetsFinalAL.size() == 0) {
                System.out.println(itemsetsFinalAL.size() + "=" + itemsetsFinal.size() + "=" + itemsetsFinalPartition.size() + "..... breaking from main loop...");
                break;
            }
            
            //Output itemsets that were added in the current partition in the 1st phase -- finish
            System.out.println("Itemsets output end.... " + Calendar.getInstance().getTime());
            
            System.out.println("Singletons generating...." + Calendar.getInstance().getTime());
            
            generatePartitionSingletonsSecondPhase();
            
            System.out.println("Singletons generation end...." + Calendar.getInstance().getTime());
            
            //update itemsets
            for(index1 = 0; index1 < itemsetsFinalAL.size(); index1++) {
                itemset = (String) itemsetsFinalAL.get(index1);
                
                System.out.println("generateTidListSecondPhase...." + itemset + "..." + Calendar.getInstance().getTime() + "..." + index1 + "," + itemsetsFinalAL.size());
                generateTidListSecondPhase(itemset);
                System.out.println("generateTidListSecondPhase end...." + itemset + "..." + Calendar.getInstance().getTime() + "..." + index1 + "," + itemsetsFinalAL.size());
            }

            //update itemsets -- finish
            nonDecisionItemSets = null;
            
        
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

    }
    private void generatePartitionSingletonsSecondPhase() {
        try {
            String line, singleton, fuzzyMembership;
            StringTokenizer st, st1;
            int index;
            ArrayList temp;
            
            nonDecisionItemSets = new HashMap();
            
            if(indexPartition==1) {
                buf_readerGlobal.readLine();
                buf_readerGlobal.readLine();
            }

            for (index = 0; index < partitionSize; index++) {
                line = buf_readerGlobal.readLine();
                if(line==null)
                    break;

                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    singleton = st1.nextToken();

                    fuzzyMembership = st1.nextToken();

                    if (!nonDecisionItemSets.containsKey(singleton)) {
                        temp = new ArrayList();
                        temp.add(index + "^" + fuzzyMembership);
                        nonDecisionItemSets.put(singleton, temp);
                        temp = null;
                    
                    } else {
                        temp = (ArrayList) nonDecisionItemSets.get(singleton);
                        temp.add(index + "^" + fuzzyMembership);
                        nonDecisionItemSets.remove(singleton);
                        nonDecisionItemSets.put(singleton, temp);
                        temp = null;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
    
    private void generateTidListSecondPhase(String itemset) {

        StringTokenizer st, st1;
        HashMap tempHashMap = new HashMap(), tempHashMap1;
        String itemset1, itemset2, tid;
        int index;
        ArrayList tempItemset1, tempItemset2, tempAL, tempAL1, twoItemsetsAL = new ArrayList();
        float fuzzyMembership, fuzzyMembershipTemp, fuzzyMembershipTotal = 0;
        
        //generate tidlist

        st1 = new StringTokenizer(itemset, ",");
        
        itemset1 = st1.nextToken();
        tempItemset1 = (ArrayList) nonDecisionItemSets.get(itemset1);
        if(tempItemset1 == null) {
            System.out.println("tempItemset1 is null:" + itemset1 + "..." + itemset);
            return;
        }
        
        while(st1.hasMoreTokens()) {
            fuzzyMembershipTotal = 0;
            itemset2 = st1.nextToken();
            tempItemset2 = (ArrayList) nonDecisionItemSets.get(itemset2);
            
            if(tempItemset2 == null) {
                System.out.println("tempItemset2 is null:" + itemset2 + "..." + itemset);
                return;
            }
        

            if (tempItemset1.size() <= tempItemset2.size()) {
                tempAL = tempItemset1;
                tempAL1 = tempItemset2;
            } else {
                tempAL = tempItemset2;
                tempAL1 = tempItemset1;
            }

            tempHashMap = new HashMap();
            tempItemset1 = null;
            tempItemset2 = null;

            for (index = 0; index < tempAL1.size(); index++) {
                st = new StringTokenizer((String) tempAL1.get(index), "^");
                tempHashMap.put(st.nextToken(), st.nextToken());
            }

            tempAL1 = null;

            for (index = 0; index < tempAL.size(); index++) {
                st = new StringTokenizer((String) tempAL.get(index), "^");

                tid = st.nextToken();
                fuzzyMembershipTemp = Float.parseFloat(st.nextToken());

                if (tempHashMap.containsKey(tid)) {
                    fuzzyMembership = Float.parseFloat((String) tempHashMap.get(tid));

                    if (fuzzyMembershipTemp < fuzzyMembership) {
                        fuzzyMembership = fuzzyMembershipTemp;
                    }
                    fuzzyMembershipTotal += fuzzyMembership;
                    twoItemsetsAL.add(tid + "^" + fuzzyMembership);
                }
            }
            tempItemset1 = tempAL;
            tempHashMap = null;
            tempAL = null;
        
        }
        //generate tidlist -- finish
        
        fuzzyMembership = ((Float) itemsetsFinal.get(itemset)).floatValue();
        fuzzyMembershipTotal += fuzzyMembership;

        itemsetsFinal.remove(itemset);
        itemsetsFinal.put(itemset, new Float(fuzzyMembershipTotal));
        
        return;
    }
}
