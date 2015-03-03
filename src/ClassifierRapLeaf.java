
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ClassifierRapLeaf {

    private String claSS = null;
    private HashMap classCountMap = new HashMap();
    private ArrayList rules = new ArrayList();
    private HashMap ruleIG = new HashMap();
    private HashMap ruleClassCountMap = new HashMap();
    private ArrayList secondPhaseClasses = null;
    private int trainingDataSize = 0;
    private float localARMCutOff;
    private String trainingFile = null;
    private ArrayList classes = null;
    //Phase 2 instance variables
    private HashMap precedentCountMap = null;
    //private float marginForTopRules;

   
    public void fuzzyPreProcess(int fileSerialNumber, String attribute) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        String line, token, antecedent;
        StringTokenizer st = null, st1 = null;
        int index;
        float tempCount, membershipValue, minMembershipValue;
        try {
            file_reader = new FileReader("classifierParameters.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();
            localARMCutOff = Float.parseFloat(buf_reader.readLine());
            file_reader.close();
            buf_reader.close();

            trainingFile = "fuzzyTrainingData" + fileSerialNumber + ".txt";
            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));
            line = trainingFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");
            while (true) {
                line = trainingFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                trainingDataSize++;
                st = new StringTokenizer(line, ",");
                minMembershipValue = 1;
                antecedent = null;

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (token.contains(claSS+"=")) {
                        st1 = new StringTokenizer(token, "^");
                        antecedent = st1.nextToken();
                    } else {
                        st1 = new StringTokenizer(token, "^");
                        st1.nextToken();
                        membershipValue = Float.parseFloat(st1.nextToken());
                        if (minMembershipValue > membershipValue) {
                            minMembershipValue = membershipValue;
                        }
                    }
                }

                if (antecedent != null) {
                    if (classCountMap.containsKey(antecedent)) {
                        tempCount = (Float) classCountMap.get(antecedent);
                        tempCount += minMembershipValue;
                        classCountMap.remove(antecedent);
                        classCountMap.put(antecedent, tempCount);
                    } else {
                        classCountMap.put(antecedent, minMembershipValue);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("classCountMap: "+ classCountMap);
    }

    public void fuzzyPhase1(int fileSerialNumber, String attribute, boolean allClassesARMInSecondPhase, boolean isPrune) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        PrintWriter pw = null, pw1 = null;
        String line, subLine, subLine1, precedent, antecedent, precedent1, antecedent1, token;
        StringTokenizer st = null, st1 = null;
        HashMap secondPhaseClassesBufferedReaders = null;
        float precedentCount, antecedentCount, classCount;
        int index, index1, classIndex, maxClassCount = 0, precedentCount1, tempCount, tempClassCount;
        double hY, hYX, tempProbability, IG, IG1;
        boolean indexRemoved = false, isStUsed = false;
        ArrayList precedentAL, precedentAL1;
        try {

            file_reader = new FileReader("CAR_" + fileSerialNumber + "_" + attribute + ".txt");
            buf_reader = new BufferedReader(file_reader);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }

                //System.out.println("line: " + line);
                st = new StringTokenizer(line, "~~~~~");
                subLine = st.nextToken().trim();
                subLine1 = st.nextToken().trim();

                st1 = new StringTokenizer(subLine, " ");
                precedent = st1.nextToken();
                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                }
                precedentCount = Float.parseFloat(st1.nextToken());
                System.out.println(precedent + "..." + precedentCount);
                st1 = new StringTokenizer(subLine1, " ");

                antecedent = st1.nextToken();
                antecedentCount = Float.parseFloat(st1.nextToken());
                System.out.println(antecedent + "..." + antecedentCount);

                classCount = (Float) classCountMap.get(antecedent);
                tempProbability = (double) classCount / (double) trainingDataSize;
                hY = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));

                tempProbability = (double) antecedentCount / (double) precedentCount;
                hYX = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));
                tempProbability = (double) precedentCount / (double) trainingDataSize;
                hYX *= tempProbability;

                rules.add(precedent + "~~~~~" + antecedent);
                ruleIG.put(precedent + "~~~~~" + antecedent, (hY - hYX));
            }

            file_reader.close();
            buf_reader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        if (isPrune) {
            System.out.println("Before prune rules.size(): " + rules.size());


            for (index = 0; index < rules.size() - 1; index++) {
                subLine = (String) rules.get(index);
                st = new StringTokenizer(subLine, "~~~~~");
                precedent = st.nextToken();
                antecedent = st.nextToken();
                precedentAL = Utils.convertStringToArrayList(precedent, ",");

                st = new StringTokenizer(precedent, ",");
                precedentCount = st.countTokens();
                IG = (Double) ruleIG.get(subLine);
                indexRemoved = false;

                System.out.println("index: " + index + "..." + Calendar.getInstance().getTime());

                for (index1 = index + 1; index1 < rules.size(); index1++) {
                    if (isStUsed) {
                        st = new StringTokenizer(precedent, ",");
                        isStUsed = false;
                    }
                    subLine1 = (String) rules.get(index1);
                    st1 = new StringTokenizer(subLine1, "~~~~~");
                    precedent1 = st1.nextToken();
                    antecedent1 = st1.nextToken();
                    precedentAL1 = Utils.convertStringToArrayList(precedent1, ",");

                    st1 = new StringTokenizer(precedent1, ",");
                    precedentCount1 = st1.countTokens();
                    IG1 = (Double) ruleIG.get(subLine1);

                    /*if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount < precedentCount1) {
                    isStUsed = true;
                    while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    if (precedent1.contains(token) && !st.hasMoreTokens()) {
                    System.out.println("Pruned 1: " + rules.get(index));

                    rules.remove(index);
                    ruleIG.remove(subLine);
                    index--;
                    indexRemoved = true;
                    } else if (!precedent1.contains(token)) {
                    break;
                    }
                    }
                    } else if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount >= precedentCount1) {
                    while (st1.hasMoreTokens()) {
                    token = st1.nextToken();
                    if (precedent.contains(token) && !st1.hasMoreTokens()) {
                    System.out.println("Pruned 2: " + rules.get(index1));

                    rules.remove(index1);
                    ruleIG.remove(subLine1);
                    index1--;
                    } else if (!precedent.contains(token)) {
                    break;
                    }
                    }
                    }*/
                    if ((IG <= IG1 && precedentCount <= precedentCount1) || (!antecedent.equals(antecedent1) && precedentCount < precedentCount1)) {
                        isStUsed = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            if (precedentAL1.contains(token) && !st.hasMoreTokens()) {
                                System.out.println("Pruned FirstPhase 1: " + rules.get(index) + "........" + ruleIG.get(subLine));
                                System.out.println("Pruner FirstPhase 1: " + rules.get(index1) + "........" + ruleIG.get(subLine1));

                                rules.remove(index);
                                ruleIG.remove(subLine);
                                index--;
                                indexRemoved = true;
                            } else if (!precedentAL1.contains(token)) {
                                break;
                            }
                        }
                    } else if ((IG > IG1 && precedentCount > precedentCount1) || (!antecedent.equals(antecedent1) && precedentCount > precedentCount1)) {
                        while (st1.hasMoreTokens()) {
                            token = st1.nextToken();
                            if (precedentAL.contains(token) && !st1.hasMoreTokens()) {
                                System.out.println("Pruned FirstPhase 2: " + rules.get(index1) + "........" + ruleIG.get(subLine1));
                                System.out.println("Pruner FirstPhase 2: " + rules.get(index) + "........" + ruleIG.get(subLine));
                                rules.remove(index1);
                                ruleIG.remove(subLine1);
                                index1--;
                            } else if (!precedentAL.contains(token)) {
                                break;
                            }
                        }
                    }

                    if (indexRemoved) {
                        break;
                    }
                }
            }
            System.out.println("After prune rules.size(): " + rules.size());
        }

        for (index = 0; index < rules.size(); index++) {
            subLine = (String) rules.get(index);
            st = new StringTokenizer(subLine, "~~~~~");
            st.nextToken();
            antecedent = st.nextToken();

            if (ruleClassCountMap.containsKey(antecedent)) {
                tempCount = (Integer) ruleClassCountMap.get(antecedent);
                tempCount++;
                ruleClassCountMap.remove(antecedent);
                ruleClassCountMap.put(antecedent, tempCount);
            } else {
                ruleClassCountMap.put(antecedent, 1);
            }
        }

        Collections.sort(rules, new ClassifierComparator(ruleIG));

        if(!ruleClassCountMap.isEmpty()) {
            maxClassCount = (Integer) Collections.max(ruleClassCountMap.values());
            System.out.println("maxClassCount: " + maxClassCount);
        }
        classes = new ArrayList(classCountMap.keySet());
        secondPhaseClasses = new ArrayList();

        for (index = 0; index < classes.size(); index++) {
            antecedent = (String) classes.get(index);
            if (ruleClassCountMap.containsKey(antecedent)) {
                tempClassCount = (Integer) ruleClassCountMap.get(antecedent);
                System.out.println("antecedent, tempClassCount: " + antecedent + "..." + tempClassCount);
                if (((float) tempClassCount <= (float) maxClassCount * localARMCutOff) || allClassesARMInSecondPhase) {
                    secondPhaseClasses.add(antecedent);
                    System.out.println("secondPhaseClasses: " + tempClassCount + " <= " + ((float) maxClassCount * localARMCutOff));
                }
            }
        }

        try {
            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));
            secondPhaseClassesBufferedReaders = new HashMap();
            line = trainingFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");
            for (index = 0; index < secondPhaseClasses.size(); index++) {
                antecedent = (String) secondPhaseClasses.get(index);
                pw = new PrintWriter("fuzzyTrainingData_" + fileSerialNumber + "_" + antecedent + ".txt");
                secondPhaseClassesBufferedReaders.put(antecedent, pw);
            }

            for (classIndex = 0; st.hasMoreTokens(); classIndex++) {
                if (claSS.equals(st.nextToken())) {
                    break;
                } else {
                    continue;
                }
            }
            while (true) {
                line = trainingFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (token.contains(claSS)) {
                        st1 = new StringTokenizer(token, "^");
                        antecedent = st1.nextToken();

                        if (secondPhaseClasses.contains(antecedent)) {
                            pw = (PrintWriter) secondPhaseClassesBufferedReaders.get(antecedent);
                            pw.println(line);
                            pw.flush();
                        }
                    }
                }
            }

            pw1 = new PrintWriter("classifierParametersSecondPhase" + fileSerialNumber + ".txt");
            pw1.println(trainingFile);
            pw1.flush();
            pw1.println(claSS);
            pw1.flush();
            pw1.println(trainingDataSize);
            pw1.flush();

            for (index = 0; index < secondPhaseClasses.size(); index++) {
                antecedent = (String) secondPhaseClasses.get(index);
                pw = (PrintWriter) secondPhaseClassesBufferedReaders.get(antecedent);
                pw.close();

                classCount = (Float) classCountMap.get(antecedent);
                pw1.println(antecedent + "," + classCount);
                pw1.flush();
            }
            pw1.println("END");
            pw1.flush();

            for (index = 0; index < classes.size(); index++) {
                pw1.println(classes.get(index));
                pw1.flush();
            }

            pw1.close();
            pw1 = new PrintWriter("classifierRules" + fileSerialNumber + ".txt");

            for (index = 0; index < rules.size(); index++) {
                token = (String) rules.get(index);
                pw1.println(token + ";" + ruleIG.get(token));
                pw1.flush();
            }
            pw1.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
    
    public void fuzzyPreProcessPhase2(int fileSerialNumber, String attribute) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        String line, token, subLine, precedent;
        StringTokenizer st = null, st1 = null;
        int index, indexCount = 0;
        ArrayList precedents = null, tempAL = null;
        PrintWriter pw = null;
        float minMembershipValue, membershipValue, tempCount;
        HashMap precedentMembershipMap = new HashMap();

        try {

            // Ascertain precedents - Start
            file_reader = new FileReader("CAR_" + fileSerialNumber + "_" + attribute + ".txt");
            buf_reader = new BufferedReader(file_reader);
            precedents = new ArrayList();

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }
                //System.out.println("line: " + line);
                st = new StringTokenizer(line, "~~~~~");

                subLine = st.nextToken().trim();
                //System.out.println("subLine before: " + subLine);
                st1 = new StringTokenizer(subLine, " ");

                precedent = st1.nextToken();
                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                    //System.out.println(precedent);
                }
                //System.out.println(precedent);
                precedents.add(precedent);
            }

            file_reader.close();
            buf_reader.close();
            System.out.println("Precedents: \n" + precedents + "\nsize = " + precedents.size());

            // Ascertain precedents - End

            // Ascertain precedent counts - Start
            trainingFileBufferedReader = new BufferedReader(new FileReader("fuzzyTrainingData" + fileSerialNumber + ".txt"));
            precedentCountMap = new HashMap();

            while (true) {
                line = trainingFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                
                if (indexCount % 1000 == 0) {
                    System.out.println("Precedent indexCount: " + indexCount + "..." + Calendar.getInstance().getTime());
                }
                indexCount++;
                st = new StringTokenizer(line, ",");
                precedentMembershipMap.clear();
                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    st1 = new StringTokenizer(token, "^");
                    token = st1.nextToken();
                    membershipValue = Float.parseFloat(st1.nextToken());
                    precedentMembershipMap.put(token, membershipValue);
                }

                for (index = 0; index < precedents.size(); index++) {
                    precedent = (String) precedents.get(index);
                    st1 = new StringTokenizer(precedent, ",");
                    minMembershipValue = 1;
                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        if (precedentMembershipMap.containsKey(token)) {
                            membershipValue = (Float) precedentMembershipMap.get(token);
                            if (membershipValue < minMembershipValue) {
                                minMembershipValue = membershipValue;
                            }
                            //minMembershipValue *= membershipValue;
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                if (precedentCountMap.containsKey(precedent)) {
                                    tempCount = (Float) precedentCountMap.get(precedent);
                                    tempCount += minMembershipValue;
                                    precedentCountMap.remove(precedent);
                                    precedentCountMap.put(precedent, tempCount);
                                } else {
                                    precedentCountMap.put(precedent, minMembershipValue);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            // Ascertain precedent counts - End
            pw = new PrintWriter("precedentCounts" + fileSerialNumber + ".txt");
            tempAL = new ArrayList(precedentCountMap.keySet());

            for (index = 0; index < tempAL.size(); index++) {
                precedent = (String) tempAL.get(index);
                pw.println(precedent + ";" + precedentCountMap.get(precedent));
                pw.flush();
            }
            pw.close();
            System.out.println("precedentCountMap size: " + precedentCountMap.size());
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public void fuzzyPhase2(int fileSerialNumber, String attribute, boolean pruneRulesOnTrainingData, boolean isPrune) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null;
        PrintWriter pw = null;
        String line, subLine, subLine1, precedent, antecedent, antecedent1, precedent1, token;
        StringTokenizer st = null, st1 = null;
        int index, index1, precedentCount1, precedentCount;
        double hY, hYX, tempProbability, IG, IG1;//, maxClassIG;
        boolean indexRemoved = false, isStUsed = false;
        float precedentCountFloat, antecedentCount, classCount;
        ArrayList precedentAL, precedentAL1;

        try {

            //Get precedentCounts - Start
            if (precedentCountMap == null || precedentCountMap.isEmpty()) {
                precedentCountMap = new HashMap();

                file_reader = new FileReader("precedentCounts" + fileSerialNumber + ".txt");
                buf_reader = new BufferedReader(file_reader);

                while (true) {
                    line = buf_reader.readLine();
                    if (line == null) {
                        break;
                    }
                    st = new StringTokenizer(line, ";");
                    precedentCountMap.put(st.nextToken(), Float.parseFloat(st.nextToken()));
                }
                file_reader.close();
                buf_reader.close();
            }

            //Get precedentCounts - End
            //Get rules from Phase 1 - Start

            if (rules == null || rules.isEmpty()) {
                rules = new ArrayList();
                ruleIG = new HashMap();

                file_reader = new FileReader("classifierRules" + fileSerialNumber + ".txt");

                buf_reader = new BufferedReader(file_reader);

                while (true) {
                    line = buf_reader.readLine();
                    if (line == null) {
                        break;
                    }
                    st = new StringTokenizer(line, ";");
                    token = st.nextToken();
                    rules.add(token);
                    ruleIG.put(token, Double.parseDouble(st.nextToken()));
                }

                file_reader.close();
                buf_reader.close();
            }
            //Get rules from Phase 1 - End

            //Get classifierParametersSecondPhase and classCountMap (if required) - Start

            file_reader = new FileReader("classifierParametersSecondPhase" + fileSerialNumber + ".txt");
            buf_reader = new BufferedReader(file_reader);

            trainingFile = buf_reader.readLine();
            claSS = buf_reader.readLine();
            trainingDataSize = Integer.parseInt(buf_reader.readLine());

            if (classCountMap == null || classCountMap.isEmpty()) {
                classCountMap = new HashMap();
                classes = new ArrayList();

                while (true) {
                    line = buf_reader.readLine();
                    if (line.equals("END")) {
                        break;
                    }
                    st = new StringTokenizer(line, ",");
                    token = st.nextToken();
                    classCountMap.put(token, Float.parseFloat(st.nextToken()));
                }
                while (true) {
                    line = buf_reader.readLine();
                    if (line == null) {
                        break;
                    }
                    classes.add(line);
                }
            }
            file_reader.close();
            buf_reader.close();

            //Get classifierParametersSecondPhase and classCountMap (if required) - End
            //Actual Processing - Start

            file_reader = new FileReader("CAR_" + fileSerialNumber + "_" + attribute + ".txt");
            buf_reader = new BufferedReader(file_reader);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }

                st = new StringTokenizer(line, "~~~~~");
                subLine = st.nextToken().trim();
                subLine1 = st.nextToken().trim();
                st1 = new StringTokenizer(subLine, " ");
                precedent = st1.nextToken();

                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                }
                System.out.println("precedent: " + precedent);

                precedentCountFloat = (Float) precedentCountMap.get(precedent);
                System.out.println("precedent: " + precedent + "..." + precedentCountFloat);
                st1 = new StringTokenizer(subLine1, " ");
                antecedent = st1.nextToken();

                if (rules.contains(precedent + "~~~~~" + antecedent)) {
                    System.out.println("Ignoring rule: " + precedent + "~~~~~" + antecedent);
                    continue; // Do not process this rule if it has been already added in phase 1
                    //System.out.println("Duplicate Rule: " + precedent + "~~~~~" + antecedent + " --- " + ruleIG.get(precedent + "~~~~~" + antecedent));
                }

                antecedentCount = Float.parseFloat(st1.nextToken());
                System.out.println("antecedent: " + antecedent + "..." + antecedentCount);

                classCount = (Float) classCountMap.get(antecedent);
                tempProbability = (double) classCount / (double) trainingDataSize;
                hY = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));

                tempProbability = (double) antecedentCount / (double) precedentCountFloat;
                hYX = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));
                tempProbability = (double) precedentCountFloat / (double) trainingDataSize;
                hYX *= tempProbability;

                rules.add(precedent + "~~~~~" + antecedent);
                ruleIG.put(precedent + "~~~~~" + antecedent, (hY - hYX));

                System.out.println("Rule: " + precedent + "~~~~~" + antecedent + " --- " + (hY - hYX));
            }

            file_reader.close();
            buf_reader.close();

            if (isPrune) {
                System.out.println("Before prune rules.size(): " + rules.size());
                for (index = 0; index < rules.size() - 1; index++) {
                    subLine = (String) rules.get(index);
                    System.out.println("subLine: " + subLine);
                    st = new StringTokenizer(subLine, "~~~~~");
                    precedent = st.nextToken();
                    antecedent = st.nextToken();
                    precedentAL = Utils.convertStringToArrayList(precedent, ",");

                    st = new StringTokenizer(precedent, ",");
                    precedentCount = st.countTokens();
                    //System.out.println("subLine: " + subLine);
                    IG = (Double) ruleIG.get(subLine);

                    indexRemoved = false;

                    System.out.println("index: " + index + "..." + Calendar.getInstance().getTime());

                    for (index1 = index + 1; index1 < rules.size(); index1++) {
                        if (isStUsed) {
                            st = new StringTokenizer(precedent, ",");
                            isStUsed = false;
                        }
                        subLine1 = (String) rules.get(index1);
                        st1 = new StringTokenizer(subLine1, "~~~~~");
                        precedent1 = st1.nextToken();
                        antecedent1 = st1.nextToken();
                        precedentAL1 = Utils.convertStringToArrayList(precedent1, ",");

                        st1 = new StringTokenizer(precedent1, ",");
                        precedentCount1 = st1.countTokens();
                        IG1 = (Double) ruleIG.get(subLine1);

                        /*if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount < precedentCount1) {
                        isStUsed = true;
                        while (st.hasMoreTokens()) {
                        token = st.nextToken();
                        if (precedent1.contains(token) && !st.hasMoreTokens()) {
                        System.out.println("Pruned SecondPhase 1: " + rules.get(index) + "........" + ruleIG.get(subLine));
                        System.out.println("Pruner SecondPhase 1: " + rules.get(index1) + "........" + ruleIG.get(subLine1));

                        rules.remove(index);
                        ruleIG.remove(subLine);
                        index--;
                        indexRemoved = true;
                        } else if (!precedent1.contains(token)) {
                        break;
                        }
                        }
                        } else if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount >= precedentCount1) {
                        while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        if (precedent.contains(token) && !st1.hasMoreTokens()) {
                        System.out.println("Pruned SecondPhase 2: " + rules.get(index1) + "........" + ruleIG.get(subLine1));
                        System.out.println("Pruner SecondPhase 2: " + rules.get(index) + "........" + ruleIG.get(subLine));

                        rules.remove(index1);
                        ruleIG.remove(subLine1);
                        index1--;
                        } else if (!precedent.contains(token)) {
                        break;
                        }
                        }
                        }*/

                        if ((IG <= IG1 && precedentCount <= precedentCount1) || (!antecedent.equals(antecedent1) && precedentCount < precedentCount1)) {
                            isStUsed = true;
                            while (st.hasMoreTokens()) {
                                token = st.nextToken();
                                if (precedentAL1.contains(token) && !st.hasMoreTokens()) {
                                    System.out.println("Pruned SecondPhase 1: " + rules.get(index) + "........" + ruleIG.get(subLine));
                                    System.out.println("Pruner SecondPhase 1: " + rules.get(index1) + "........" + ruleIG.get(subLine1));

                                    rules.remove(index);
                                    ruleIG.remove(subLine);
                                    index--;
                                    indexRemoved = true;

                                } else if (!precedentAL1.contains(token)) {
                                    break;
                                }
                            }
                        } else if ((IG >= IG1 && precedentCount >= precedentCount1) || (!antecedent.equals(antecedent1) && precedentCount > precedentCount1)) {
                            while (st1.hasMoreTokens()) {
                                token = st1.nextToken();
                                if (precedentAL.contains(token) && !st1.hasMoreTokens()) {
                                    System.out.println("Pruned SecondPhase 2: " + rules.get(index1) + "........" + ruleIG.get(subLine1));
                                    System.out.println("Pruner SecondPhase 2: " + rules.get(index) + "........" + ruleIG.get(subLine));
                                    rules.remove(index1);
                                    ruleIG.remove(subLine1);
                                    index1--;
                                } else if (!precedentAL.contains(token)) {
                                    break;
                                }
                            }
                        }
                        if (indexRemoved) {
                            break;
                        }
                    }
                }
                System.out.println("After prune rules.size(): " + rules.size());
            }
            Collections.sort(rules, new ClassifierComparator(ruleIG));

            if(pruneRulesOnTrainingData) {
                System.out.println("Before pruneRulesOnTrainingData rules.size(): " + rules.size());
                pruneRulesOnTrainingData();
                System.out.println("After pruneRulesOnTrainingData rules.size(): " + rules.size());
            }

            pw = new PrintWriter("classifierRulesFinal" + fileSerialNumber + ".txt");

            for (index = 0; index < rules.size(); index++) {
                token = (String) rules.get(index);
                pw.println(token + ";" + ruleIG.get(token));
                pw.flush();
            }
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        //Actual Processing - End
        System.out.println("rules size:  " + rules.size());
    }

    private void pruneRulesOnTrainingData() {

        BufferedReader bufReader = null;
        String line = null, token, actualAntecedent = null, subLine, precedent, antecedent;
        ArrayList lineFromTestFileAL = new ArrayList();
        StringTokenizer st = null, st1;
        float minMembershipValue, membershipValue, ruleTotal, ruleAccuracyTotal;
        int index;
        HashMap ruleAccuracyMap = new HashMap(), ruleTotalMap = new HashMap();

        try {
            bufReader = new BufferedReader(new FileReader(trainingFile));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                lineFromTestFileAL.clear();
                st = new StringTokenizer(line, ",");
                minMembershipValue = 1;
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    token = st1.nextToken();
                    if (!token.contains(claSS)) {
                        lineFromTestFileAL.add(token);
                        membershipValue = Float.parseFloat(st1.nextToken());
                        if (minMembershipValue > membershipValue) {
                            minMembershipValue = membershipValue;
                        }
                    } else {
                        actualAntecedent = token;
                    }
                }

                for (index = 0; index < rules.size(); index++) {
                    subLine = (String) rules.get(index);
                    //System.out.println("subLine: " + subLine);
                    st = new StringTokenizer(subLine, "~~~~~");
                    precedent = st.nextToken();
                    antecedent = st.nextToken();
                    st1 = new StringTokenizer(precedent, ",");

                    while (st1.hasMoreTokens()) {
                        if (lineFromTestFileAL.contains(st1.nextToken())) {
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                if (ruleTotalMap.containsKey(subLine)) {
                                    ruleTotal = (Float) ruleTotalMap.get(subLine);
                                    ruleTotal += minMembershipValue;
                                    ruleTotalMap.remove(subLine);
                                } else {
                                    ruleTotal = minMembershipValue;
                                }
                                ruleTotalMap.put(subLine, ruleTotal);

                                if (antecedent.equals(actualAntecedent)) {
                                    if (ruleAccuracyMap.containsKey(subLine)) {
                                        ruleAccuracyTotal = (Float) ruleAccuracyMap.get(subLine);
                                        ruleAccuracyTotal += minMembershipValue;
                                        ruleAccuracyMap.remove(subLine);
                                    } else {
                                        ruleAccuracyTotal = minMembershipValue;
                                    }
                                    ruleAccuracyMap.put(subLine, ruleAccuracyTotal);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            bufReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        for (index = 0; index < rules.size(); index++) {
            subLine = (String) rules.get(index);
            ruleTotal = (Float) ruleTotalMap.get(subLine);
            ruleAccuracyTotal = 0;
            if (ruleAccuracyMap.containsKey(subLine)) {
                ruleAccuracyTotal = (Float) ruleAccuracyMap.get(subLine);
            } else {
                rules.remove(subLine);
                ruleIG.remove(subLine);
                index--;
                System.out.println("Pruned on training data1: " + subLine + ": " + ruleAccuracyTotal + "..." + ruleTotal);
                continue;
            }

            if ((ruleAccuracyTotal / ruleTotal) < 0.5) {
                rules.remove(subLine);
                ruleIG.remove(subLine);
                System.out.println("Pruned on training data: " + subLine + ": " + ruleAccuracyTotal + "..." + ruleTotal);
                index--;
            }
        }
    }

    public float fuzzyTestBestKWithTopNLengthRules(int fileSerialNumber, boolean isClassFinal) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null, bestAntecedent = null;
        StringTokenizer st = null, st1 = null;
        int index, index1, index2, maxindex = 0, correctCount = 0, totalCount = 0, count, kCutOffAbsolute = 0, ruleLengthCutOffAbsolute = 0, actualRuleLengthCutOffAbsolute, maxRuleLengthCutOffAbsolute, ruleLength, currentRuleLength, maxCount, totalTestPoints = 0, classLabelCount = 0;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, countAL = null, averageIGAL = null, ruleAntecedentIGAL = new ArrayList();
        double kCutOffRealtive = 0.05, tempTotalIG, IG, maxAverageIG, maxTotalIG = 0, tempAverageIG, tempAverageIG1, defaultClassCutOff = 0, defaultClassCutOffLower = 0;
        float membershipValue, minMembershipValue = 1;
        HashMap globalClassificationIGMap = new HashMap(), globalClassificationIGIndexCountMap = new HashMap(), globalClassificationCountMap = new HashMap(), lineFromTestFileMap = new HashMap(), ruleAntecedentIGALMap = new HashMap();
        PrintWriter pw1 = null;
        boolean isFirstTime, isTestAgain;

        lineFromTestFileMap = new HashMap();
        tempAL = new ArrayList();
        countAL = new ArrayList();
        averageIGAL = new ArrayList();

        try {
            pw1 = new PrintWriter("classificationResultsHistogram.txt");

            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            if (line != null) {
                kCutOffRealtive = Double.parseDouble(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffAbsolute = Integer.parseInt(line);
            }
            file_reader.close();
            buf_reader.close();

            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            if(isClassFinal) {
                file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
            }
            else if(!isClassFinal) {
                file_reader = new FileReader("classifierRules" + fileSerialNumber + ".txt");
            }
            buf_reader = new BufferedReader(file_reader);

            classifierPrecedents = new ArrayList();
            classifierAntecedents = new ArrayList();
            classifierIG = new ArrayList();
            classifierRuleLength = new ArrayList();

            System.out.println("START of CLASSIFICATION");

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ";");
                st1 = new StringTokenizer(st.nextToken(), "~~~~~");
                classifierIG.add(Double.parseDouble(st.nextToken()));
                precedent = st1.nextToken();
                classifierPrecedents.add(precedent);
                antecedent = st1.nextToken();
                classifierAntecedents.add(antecedent);
                st1 = new StringTokenizer(precedent, ",");
                classifierRuleLength.add(st1.countTokens());

            }
            file_reader.close();
            buf_reader.close();

            kCutOffAbsolute = (int) Math.round(kCutOffRealtive * classifierPrecedents.size());
            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);
            actualRuleLengthCutOffAbsolute = ruleLengthCutOffAbsolute;

            //actualKCutOffAbsolute = kCutOffAbsolute;

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), kCutOffAbsolute, kCutOffRealtive, ruleLengthCutOffAbsolute ..." + classifierPrecedents.size() + "..." + kCutOffAbsolute + "..." + kCutOffRealtive + "......." + ruleLengthCutOffAbsolute);
            System.out.println("classifierPrecedents: " + classifierPrecedents);
            // Actual processing - Start

            for (totalTestPoints = 0; true;) {
                System.out.println("kCutOffAbsolute, ruleLengthCutOffAbsolute" + "..." + kCutOffAbsolute + "..." + ruleLengthCutOffAbsolute);
                line = testFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }
                if (line.equals("END OF DATA POINT")) {
                    totalTestPoints++;
                    maxAverageIG = Double.NEGATIVE_INFINITY;
                    maxCount = Integer.MIN_VALUE;
                    countAL.clear();
                    averageIGAL.clear();

                    tempAL = new ArrayList(globalClassificationCountMap.keySet());
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        totalCount = (Integer) globalClassificationCountMap.get(antecedent);

                        if (totalCount > maxCount) {
                            countAL.clear();
                            countAL.add(antecedent);
                            maxCount = totalCount;
                        } else if (totalCount == maxCount) {
                            countAL.add(antecedent);
                        }
                    }

                    if (countAL.size() > 1) {
                        maxAverageIG = Double.NEGATIVE_INFINITY;
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            tempTotalIG = (Double) globalClassificationIGMap.get(antecedent);
                            index1 = (Integer) globalClassificationIGIndexCountMap.get(antecedent);
                            //tempAverageIG = tempTotalIG / (double) index1;
                            tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                            if (tempAverageIG > maxAverageIG) {
                                averageIGAL.clear();
                                averageIGAL.add(antecedent);
                                maxAverageIG = tempAverageIG;
                            } else if (tempAverageIG == maxAverageIG) {
                                averageIGAL.add(antecedent);
                            }
                        }
                        if (averageIGAL.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL IG");
                            System.out.println(averageIGAL + "~~~~~" + actualAntecedent);
                            pw1.println(globalClassificationCountMap + ", correct");
                        } else {
                            System.out.println("Wrong classfication GLOBAL");
                            System.out.println(countAL + "~~~~~" + averageIGAL + "~~~~~" + actualAntecedent);
                            pw1.println(globalClassificationCountMap + ", wrong");
                        }
                    } else if (countAL.size() == 1) {
                        if (countAL.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL Count");
                            System.out.println(countAL + "~~~~~" + actualAntecedent);
                            pw1.println(globalClassificationCountMap + ", correct");
                        } else {
                            System.out.println("Wrong classfication GLOBAL");
                            System.out.println(countAL + "~~~~~" + actualAntecedent);
                            pw1.println(globalClassificationCountMap + ", wrong");
                        }
                    }
                    else {
                        //totalTestPoints--;
                        correctCount++;
                        System.out.println("NOT CLASSIFIED");
                    }
                    System.out.println("From decision block: globalClassificationIGMap: " + globalClassificationIGMap);
                    System.out.println("From decision block: globalClassificationIGIndexCountMap: " + globalClassificationIGIndexCountMap);
                    System.out.println("From decision block: globalClassificationCountMap: " + globalClassificationCountMap);
                    globalClassificationCountMap.clear();
                    globalClassificationIGMap.clear();
                    globalClassificationIGIndexCountMap.clear();
                    continue;
                }

                lineFromTestFileMap.clear();
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    token = st1.nextToken();
                    if (!token.contains(claSS)) {
                        membershipValue = Float.parseFloat(st1.nextToken());
                        lineFromTestFileMap.put(token, membershipValue);
                    } else {
                        actualAntecedent = token;
                        classLabelCount = Integer.parseInt(st1.nextToken());
                    }
                }
                System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap);
                System.out.println("classLabelCount: " + classLabelCount);

                isTestAgain = true;
                isFirstTime = true;
                ruleLengthCutOffAbsolute = actualRuleLengthCutOffAbsolute;
                while (isTestAgain) {
                    if (!isFirstTime) {
                        ruleLengthCutOffAbsolute--;
                        //kCutOffAbsolute *= 2;
                        System.out.println("line from test file retested: " + line + "..." + totalCount);
                        System.out.println("kCutOffAbsolute, ruleLengthCutOffAbsolute" + "..." + kCutOffAbsolute + "..." + ruleLengthCutOffAbsolute);
                    }
                    if(ruleLengthCutOffAbsolute < 1) {
                        break;
                    }
                    //ruleAntecedentIGAL.clear();
                    ruleAntecedentIGALMap.clear();
                    for (index = 0; index < classifierPrecedents.size(); index++) {
                        IG = (Double) classifierIG.get(index);
                        precedent = (String) classifierPrecedents.get(index);
                        antecedent = (String) classifierAntecedents.get(index);
                        ruleLength = (Integer) classifierRuleLength.get(index);

                        if (ruleLength < ruleLengthCutOffAbsolute) {
                            continue;
                        }
                        st1 = new StringTokenizer(precedent, ",");
                        //minMembershipValue = Float.MAX_VALUE;
                        //minMembershipValue = Float.MIN_VALUE;
                        minMembershipValue = 1;
                        //minMembershipValue = 0;
                        count = 0;

                        while (st1.hasMoreTokens()) {
                            token = st1.nextToken();
                            count++;
                            if (lineFromTestFileMap.containsKey(token)) {
                                membershipValue = (Float) lineFromTestFileMap.get(token);
                                if (minMembershipValue > membershipValue) {
                            //if (minMembershipValue < membershipValue) {
                                    minMembershipValue = membershipValue;
                                }
                                ////minMembershipValue *= membershipValue;
                                //minMembershipValue += membershipValue;
                                //minMembershipValue = 1 / minMembershipValue;

                                if (st1.countTokens() > 0) {
                                    continue;
                                } else {
                                    //minMembershipValue = minMembershipValue / count;
                                    //IG *= (minMembershipValue / (double) classLabelCount);
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue + "..." + (IG * minMembershipValue));
                                    IG *= minMembershipValue;
                                    if (!ruleAntecedentIGALMap.containsKey(antecedent)) {
                                        ruleAntecedentIGAL = new ArrayList();
                                        ruleAntecedentIGALMap.put(antecedent, ruleAntecedentIGAL);
                                    }
                                    else {
                                        ruleAntecedentIGAL = (ArrayList) ruleAntecedentIGALMap.get(antecedent);
                                    }
                                    ruleAntecedentIGAL.add(IG);
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    //System.out.println("ruleAntecedentIGALMap: " + ruleAntecedentIGALMap);
                    tempAL = new ArrayList(ruleAntecedentIGALMap.keySet());
                    maxAverageIG = Double.NEGATIVE_INFINITY;
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        ruleAntecedentIGAL = (ArrayList) ruleAntecedentIGALMap.get(antecedent);
                        Collections.sort(ruleAntecedentIGAL);

                        tempTotalIG = 0;
                        for (index2 = ruleAntecedentIGAL.size() - 1, index1 = 0; index1 < kCutOffAbsolute && index2 >= 0; index2--, index1++) {
                            IG = (Double) ruleAntecedentIGAL.get(index2);
                            tempTotalIG += IG;
                            System.out.println("antecedent, actualAntecedent, IG, tempTotalIG: " + antecedent + "..." + actualAntecedent + "..." + IG + "..." + tempTotalIG);
                        }
                        //tempAverageIG = tempTotalIG / (double) (index1);
                        tempAverageIG = tempTotalIG / (double) (kCutOffAbsolute);
                        if(maxAverageIG < tempAverageIG) {
                            maxAverageIG = tempAverageIG;
                            bestAntecedent = antecedent;
                            maxTotalIG = tempTotalIG;
                            maxindex = index1;
                        }
                        System.out.println("antecedent, actualAntecedent, tempAverageIG, (tempTotalIG / (double) (kCutOffAbsolute)): " + antecedent + "..." + actualAntecedent + "..." + tempAverageIG + "..." + tempTotalIG + "/" + (double) (kCutOffAbsolute));
                    }
                    if (!ruleAntecedentIGALMap.isEmpty()) {
                        System.out.println("LOCAL bestAntecedent, actualAntecedent, maxAverageIG: " + bestAntecedent + "..." + actualAntecedent + "..." + maxAverageIG);
                        if (!globalClassificationIGMap.containsKey(bestAntecedent)) {
                            globalClassificationIGMap.put(bestAntecedent, maxTotalIG);
                            globalClassificationIGIndexCountMap.put(bestAntecedent, maxindex);
                            globalClassificationCountMap.put(bestAntecedent, 1);
                        } else {
                            maxTotalIG += (Double) globalClassificationIGMap.get(bestAntecedent);
                            globalClassificationIGMap.remove(bestAntecedent);
                            globalClassificationIGMap.put(bestAntecedent, maxTotalIG);

                            maxindex += (Integer) globalClassificationIGIndexCountMap.get(bestAntecedent);
                            globalClassificationIGIndexCountMap.remove(bestAntecedent);
                            globalClassificationIGIndexCountMap.put(bestAntecedent, maxindex);

                            count = (Integer) globalClassificationCountMap.get(bestAntecedent) + 1;
                            globalClassificationCountMap.remove(bestAntecedent);
                            globalClassificationCountMap.put(bestAntecedent, count);
                        }
                        System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);
                        System.out.println("globalClassificationIGIndexCountMap: " + globalClassificationIGIndexCountMap);
                        System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                        isTestAgain = false;
                    }
                    else {
                        isTestAgain = true;
                        isFirstTime = false;
                        System.out.println("TESTING AGAIN");
                    }
                }
            }
            testFileBufferedReader.close();
            pw1.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + ((float) correctCount / (float) (totalTestPoints)) * 100 + "%");
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + correctCount + "/" + totalTestPoints);
        return ((float) correctCount / (float) (totalTestPoints));
    }
}

