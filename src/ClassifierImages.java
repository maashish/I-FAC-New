
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ClassifierImages {

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

    public void preProcess() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        String line, token;
        StringTokenizer st = null;
        int classIndex, index, tempCount;
        try {

            file_reader = new FileReader("classifierParameters.txt");
            buf_reader = new BufferedReader(file_reader);

            trainingFile = buf_reader.readLine();

            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));
            claSS = buf_reader.readLine();
            localARMCutOff = Float.parseFloat(buf_reader.readLine());
            //marginForTopRules = Float.parseFloat(buf_reader.readLine());
            file_reader.close();
            buf_reader.close();

            line = trainingFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");

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
                if (line.contains("a,a,a")) {
                    break;
                }
                trainingDataSize++;
                st = new StringTokenizer(line, ",");

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (index == classIndex) {
                        if (classCountMap.containsKey(claSS + "=" + token)) {
                            tempCount = (Integer) classCountMap.get(claSS + "=" + token);
                            tempCount++;
                            classCountMap.remove(claSS + "=" + token);
                            classCountMap.put(claSS + "=" + token, tempCount);
                        } else {
                            classCountMap.put(claSS + "=" + token, 1);
                        }
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println(classCountMap);
    }

    public void fuzzyPreProcess(int fileSerialNumber, String attribute) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        String line, token, antecedent = null;
        StringTokenizer st = null, st1 = null;
        int index, count;
        float tempCount, membershipValue, minMembershipValue = 0;
        try {
            file_reader = new FileReader("classifierParameters.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();
            localARMCutOff = Float.parseFloat(buf_reader.readLine());
            file_reader.close();
            buf_reader.close();

            trainingFile = "fuzzyTrainingData" + fileSerialNumber + ".txt";

            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));

            while (true) {
                line = trainingFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }

                trainingDataSize++;
                st = new StringTokenizer(line, ",");
                //minMembershipValue = 1;


                //Modifed for YLR

                minMembershipValue = 0;
                antecedent = null;

                for (index = 0, count = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (token.contains(claSS)) {
                        st1 = new StringTokenizer(token, "^");
                        antecedent = st1.nextToken();
                    } else {
                        st1 = new StringTokenizer(token, "^");
                        st1.nextToken();
                        membershipValue = Float.parseFloat(st1.nextToken());
                        //if (minMembershipValue > membershipValue) {
                        //if (minMembershipValue < membershipValue) {
                            //minMembershipValue = membershipValue;
                        //}
                        //minMembershipValue *= membershipValue;
                        minMembershipValue += membershipValue;
                        count++;
                    }
                }

                /*for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (token.contains(claSS)) {
                        st1 = new StringTokenizer(token, "^");
                        antecedent = st1.nextToken();
                        minMembershipValue = Float.parseFloat(st1.nextToken());
                    } else {
                        continue;
                    }
                }*/

                if (classCountMap.containsKey(antecedent)) {
                    tempCount = (Float) classCountMap.get(antecedent);
                    tempCount += minMembershipValue;
                    tempCount += minMembershipValue / (float) count;
                    classCountMap.remove(antecedent);
                    classCountMap.put(antecedent, tempCount);
                } else {
                    classCountMap.put(antecedent, minMembershipValue);
                    //classCountMap.put(antecedent, minMembershipValue / (float) count);
                }

                /*for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (token.contains(claSS)) {
                        st1 = new StringTokenizer(token, "^");
                        antecedent = st1.nextToken();
                        break;
                    }
                }

                if (classCountMap.containsKey(antecedent)) {
                    tempCount = (Float) classCountMap.get(antecedent);
                    tempCount += 1;
                    classCountMap.remove(antecedent);
                    classCountMap.put(antecedent, tempCount);
                } else {
                    classCountMap.put(antecedent, (float) 1);
                }*/
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        //System.out.println("classCountMap: " + classCountMap);
    }

    public void fuzzyPhase1(int fileSerialNumber, String attribute, boolean isMainPrune, boolean allClassesARMInSecondPhase) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        PrintWriter pw = null, pw1 = null;
        String line, subLine, subLine1, precedent, antecedent, precedent1, antecedent1, token;
        StringTokenizer st = null, st1 = null;
        HashMap secondPhaseClassesBufferedReaders = null;
        float precedentCount, antecedentCount, classCount;
        int index, index1, maxClassCount = 0, precedentCount1, tempCount, tempClassCount;
        double hY, hYX, tempProbability, IG, IG1;
        boolean indexRemoved = false, isStUsed = false;
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

        if (isMainPrune) {
            System.out.println("Before prune rules.size(): " + rules.size());

            for (index = 0; index < rules.size() - 1; index++) {
                subLine = (String) rules.get(index);
                st = new StringTokenizer(subLine, "~~~~~");
                precedent = st.nextToken();
                antecedent = st.nextToken();

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
                    if ((/*!antecedent.equals(antecedent1) ||*/IG <= IG1) && precedentCount <= precedentCount1) {
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
                    } else if ((/*!antecedent.equals(antecedent1) ||*/IG >= IG1) && precedentCount >= precedentCount1) {
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

        Collections.sort(rules, new ClassifierComparatorImages(ruleIG));

        if (!allClassesARMInSecondPhase) {
            maxClassCount = (Integer) Collections.max(ruleClassCountMap.values());
            System.out.println("maxClassCount: " + maxClassCount);
        }
        classes = new ArrayList(classCountMap.keySet());
        secondPhaseClasses = new ArrayList();

        for (index = 0; index < classes.size(); index++) {
            antecedent = (String) classes.get(index);
            if (allClassesARMInSecondPhase) {
                secondPhaseClasses.add(antecedent);
            } else if (ruleClassCountMap.containsKey(antecedent)) {
                tempClassCount = (Integer) ruleClassCountMap.get(antecedent);
                System.out.println("antecedent, tempClassCount: " + antecedent + "..." + tempClassCount);
                if (((float) tempClassCount <= (float) maxClassCount * localARMCutOff)) {
                    secondPhaseClasses.add(antecedent);
                }
            }
            else {
                secondPhaseClasses.add(antecedent);
            }
        }

        try {
            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));
            secondPhaseClassesBufferedReaders = new HashMap();
            for (index = 0; index < secondPhaseClasses.size(); index++) {
                antecedent = (String) secondPhaseClasses.get(index);
                pw = new PrintWriter("fuzzyTrainingData_" + fileSerialNumber + "_" + antecedent + ".txt");
                secondPhaseClassesBufferedReaders.put(antecedent, pw);
            }

            /*for (classIndex = 0; st.hasMoreTokens(); classIndex++) {
            if (claSS.equals(st.nextToken())) {
            break;
            } else {
            continue;
            }
            }*/
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

    public void phase1() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        PrintWriter pw = null, pw1 = null;
        String line, subLine, subLine1, precedent, antecedent, precedent1, antecedent1, token;
        StringTokenizer st = null, st1 = null;
        HashMap secondPhaseClassesBufferedReaders = null;
        int precedentCount, antecedentCount, precedentCount1, classCount, tempCount, index, index1, maxClassCount, tempClassCount, classIndex;
        double hY, hYX, tempProbability, IG, IG1;
        boolean indexRemoved = false, isStUsed = false;
        try {

            file_reader = new FileReader("globalCARs0.txt");

            buf_reader = new BufferedReader(file_reader);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }

                //System.out.println(line);
                System.out.println("line: " + line);
                st = new StringTokenizer(line, "~~~~~");
                //st = new StringTokenizer(line, "~~~~~");
                subLine = st.nextToken().trim();
                subLine1 = st.nextToken().trim();

                st1 = new StringTokenizer(subLine, " ");

                /*System.out.println(subLine);
                System.out.println(subLine1);*/

                precedent = st1.nextToken();
                //System.out.println(precedent);
                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                    //System.out.println(precedent);
                }
                precedentCount = Integer.parseInt(st1.nextToken());

                System.out.println(precedent + "..." + precedentCount);

                st1 = new StringTokenizer(subLine1, " ");

                antecedent = st1.nextToken();
                antecedentCount = Integer.parseInt(st1.nextToken());

                System.out.println(antecedent + "..." + antecedentCount);

                classCount = (Integer) classCountMap.get(antecedent);
                tempProbability = (double) classCount / (double) trainingDataSize;
                hY = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));

                tempProbability = (double) antecedentCount / (double) precedentCount;
                hYX = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));
                tempProbability = (double) precedentCount / (double) trainingDataSize;
                hYX *= tempProbability;

                rules.add(precedent + "~~~~~" + antecedent);
                ruleIG.put(precedent + "~~~~~" + antecedent, (hY - hYX));

                /*if (ruleClassCountMap.containsKey(antecedent)) {
                tempCount = (Integer) ruleClassCountMap.get(antecedent);
                tempCount++;
                ruleClassCountMap.remove(antecedent);
                ruleClassCountMap.put(antecedent, tempCount);
                } else {
                ruleClassCountMap.put(antecedent, 1);
                }*/
            }

            file_reader.close();
            buf_reader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("Before prune rules.size(): " + rules.size());

        for (index = 0; index < rules.size() - 1; index++) {
            subLine = (String) rules.get(index);
            //st = new StringTokenizer(subLine, "~~~~~");
            st = new StringTokenizer(subLine, "~~~~~");
            precedent = st.nextToken();
            antecedent = st.nextToken();

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
                //st1 = new StringTokenizer(subLine1, "~~~~~");
                st1 = new StringTokenizer(subLine1, "~~~~~");
                precedent1 = st1.nextToken();
                antecedent1 = st1.nextToken();

                st1 = new StringTokenizer(precedent1, ",");
                precedentCount1 = st1.countTokens();
                //System.out.println("subLine1: " + subLine1);
                IG1 = (Double) ruleIG.get(subLine1);

                /*System.out.println("IG, IG1: " + IG + "..." + IG1);
                System.out.println("antecedent, antecedent1: " + antecedent + "..." + antecedent1);
                System.out.println("precedentCount, precedentCount1: " + precedentCount + "..." + precedentCount1);*/

                if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount < precedentCount1) {
                    isStUsed = true;
                    while (st.hasMoreTokens()) {
                        token = st.nextToken();
                        //System.out.println("token: " + token);
                        if (precedent1.contains(token) && !st.hasMoreTokens()) {
                            /*    continue;
                            }
                            else {*/
                            rules.remove(index);
                            //System.out.println("subLine remove: " + subLine);
                            ruleIG.remove(subLine);
                            index--;
                            indexRemoved = true;
                            //}
                        } else if (!precedent1.contains(token)) {
                            //System.out.println("Does not contain token: " + token);
                            break;
                        }
                    }
                } else if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount >= precedentCount1) {
                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        //System.out.println("token1: " + token);
                        if (precedent.contains(token) && !st1.hasMoreTokens()) {
                            //if(!st1.hasMoreTokens()) {
                                /*    continue;
                            }
                            else {*/
                            rules.remove(index1);
                            //System.out.println("subLine1 remove1: " + subLine1);
                            ruleIG.remove(subLine1);
                            index1--;
                            //}
                        } else if (!precedent.contains(token)) {
                            //System.out.println("Does not contain token1: " + token);
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

        for (index = 0; index < rules.size(); index++) {
            subLine = (String) rules.get(index);
            //st = new StringTokenizer(subLine, "~~~~~");
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

        //System.out.println("rules: \n" + rules);
        maxClassCount = (Integer) Collections.max(ruleClassCountMap.values());
        System.out.println("maxClassCount: " + maxClassCount);
        classes = new ArrayList(classCountMap.keySet());
        secondPhaseClasses = new ArrayList();

        for (index = 0; index < classes.size(); index++) {
            antecedent = (String) classes.get(index);
            if (ruleClassCountMap.containsKey(antecedent)) {
                tempClassCount = (Integer) ruleClassCountMap.get(antecedent);
                System.out.println("antecedent, tempClassCount: " + antecedent + "..." + tempClassCount);
                if ((float) tempClassCount >= (float) maxClassCount * localARMCutOff) {
                    continue;
                }
            }

            secondPhaseClasses.add(antecedent);
        }

        try {
            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));

            secondPhaseClassesBufferedReaders = new HashMap();

            line = trainingFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");

            for (index = 0; index < secondPhaseClasses.size(); index++) {
                antecedent = (String) secondPhaseClasses.get(index);
                pw = new PrintWriter(antecedent + ".csv");
                secondPhaseClassesBufferedReaders.put(antecedent, pw);
                pw.println(line);
                pw.flush();
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
                    if (index == classIndex) {
                        if (secondPhaseClasses.contains(claSS + "=" + token)) {
                            pw = (PrintWriter) secondPhaseClassesBufferedReaders.get(claSS + "=" + token);
                            pw.println(line);
                            pw.flush();
                        }
                    }
                }
            }

            pw1 = new PrintWriter("classifierParametersSecondPhase.txt");

            pw1.println(trainingFile);
            pw1.flush();
            pw1.println(claSS);
            pw1.flush();
            pw1.println(trainingDataSize);
            pw1.flush();
            /*pw1.println(marginForTopRules);
            pw1.flush();*/

            for (index = 0; index < secondPhaseClasses.size(); index++) {
                antecedent = (String) secondPhaseClasses.get(index);
                pw = (PrintWriter) secondPhaseClassesBufferedReaders.get(antecedent);
                pw.close();

                classCount = (Integer) classCountMap.get(antecedent);
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

            pw1 = new PrintWriter("classifierRules.txt");

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

    public void preProcessPhase2() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        String line, token, subLine, precedent;
        StringTokenizer st = null, st1 = null;
        int index, tempCount, index1 = 0;
        ArrayList precedents = null, secondPhaseClassNames = null, tempAL = null, tempAL1 = null;
        PrintWriter pw = null;

        try {

            // Ascertain precedents - Start 
            file_reader = new FileReader("wekaInputPhase2.txt");

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


                System.out.println("line: " + line);
                st = new StringTokenizer(line, "~~~~~");

                subLine = st.nextToken().trim();

                System.out.println("subLine before: " + subLine);

                st1 = new StringTokenizer(subLine, " ");

                precedent = st1.nextToken();
                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                    //System.out.println(precedent);
                }

                System.out.println(precedent);
                precedents.add(precedent);

            }

            file_reader.close();
            buf_reader.close();
            //System.out.println("Precedents: \n" + precedents + "\nsize = " + precedents.size());

            // Ascertain precedents - End 

            // Ascertain precedent counts - Start

            file_reader = new FileReader("classifierParametersSecondPhase.txt");
            buf_reader = new BufferedReader(file_reader);

            trainingFile = buf_reader.readLine();

            trainingFileBufferedReader = new BufferedReader(new FileReader(trainingFile));

            file_reader.close();
            buf_reader.close();

            secondPhaseClassNames = new ArrayList();

            line = trainingFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");

            while (st.hasMoreTokens()) {
                secondPhaseClassNames.add(st.nextToken());
            }

            precedentCountMap = new HashMap();

            while (true) {
                line = trainingFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }

                System.out.println("index1: " + index1++ + "..." + Calendar.getInstance().getTime());
                st = new StringTokenizer(line, ",");
                tempAL = new ArrayList();

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    tempAL.add(secondPhaseClassNames.get(index) + "=" + token);
                }

                tempAL1 = new ArrayList();

                for (index = 0; index < precedents.size(); index++) {

                    precedent = (String) precedents.get(index);

                    //System.out.println(precedent + "... ENTER");

                    if (tempAL1.contains(precedent)) {
                        continue;
                    } else {
                        tempAL1.add(precedent);
                    }

                    //System.out.println(precedent + "... ENTER INNNNN");

                    st1 = new StringTokenizer(precedent, ",");

                    while (st1.hasMoreTokens()) {

                        if (tempAL.contains(st1.nextToken())) {
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                if (precedentCountMap.containsKey(precedent)) {
                                    tempCount = (Integer) precedentCountMap.get(precedent);
                                    tempCount++;
                                    precedentCountMap.remove(precedent);
                                    precedentCountMap.put(precedent, tempCount);
                                } else {
                                    precedentCountMap.put(precedent, 1);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                tempAL = null;
                tempAL1 = null;
            }
            // Ascertain precedent counts - End
            pw = new PrintWriter("precedentCounts.txt");
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

    public void fuzzyPreProcessPhase2(int fileSerialNumber, String attribute) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, trainingFileBufferedReader = null;
        String line, token, subLine, precedent;
        StringTokenizer st = null, st1 = null;
        int index;
        ArrayList precedents = null, tempAL = null, tempAL1 = null;
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

    public void phase2() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null;
        PrintWriter pw = null;
        String line, subLine, subLine1, precedent, antecedent, precedent1, antecedent1, token;
        StringTokenizer st = null, st1 = null;
        int precedentCount, antecedentCount, classCount, index, index1, precedentCount1;
        double hY, hYX, tempProbability, IG, IG1;
        boolean indexRemoved = false, isStUsed = false;

        try {

            //Get precedentCounts - Start
            if (precedentCountMap == null || precedentCountMap.isEmpty()) {
                precedentCountMap = new HashMap();

                file_reader = new FileReader("precedentCounts.txt");

                buf_reader = new BufferedReader(file_reader);

                while (true) {
                    line = buf_reader.readLine();
                    if (line == null) {
                        break;
                    }
                    st = new StringTokenizer(line, ";");
                    precedentCountMap.put(st.nextToken(), Integer.parseInt(st.nextToken()));
                }
                file_reader.close();
                buf_reader.close();
            }

            //Get precedentCounts - End

            //Get rules from Phase 1 - Start

            if (rules == null || rules.isEmpty()) {
                rules = new ArrayList();
                ruleIG = new HashMap();

                file_reader = new FileReader("classifierRules.txt");

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

            file_reader = new FileReader("classifierParametersSecondPhase.txt");
            buf_reader = new BufferedReader(file_reader);

            trainingFile = buf_reader.readLine();
            claSS = buf_reader.readLine();
            trainingDataSize = Integer.parseInt(buf_reader.readLine());
            //marginForTopRules = Float.parseFloat(buf_reader.readLine());

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
                    classCountMap.put(token, Integer.parseInt(st.nextToken()));
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

            file_reader = new FileReader("wekaInputPhase2.txt");

            buf_reader = new BufferedReader(file_reader);

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }

                // System.out.println("line: " + line);

                st = new StringTokenizer(line, "~~~~~");

                subLine = st.nextToken().trim();
                subLine1 = st.nextToken().trim();

                st1 = new StringTokenizer(subLine, " ");

                /*System.out.println("subLine: " + subLine);
                System.out.println("subLine1: " + subLine1);*/

                precedent = st1.nextToken();
                //System.out.println(precedent);
                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                }

                precedentCount = (Integer) precedentCountMap.get(precedent); 

                System.out.println("precedent: " + precedent + "..." + precedentCount);

                st1 = new StringTokenizer(subLine1, " ");

                antecedent = st1.nextToken();

                if (rules.contains(precedent + "~~~~~" + antecedent)) {
                    System.out.println("Ignoring rule: " + precedent + "~~~~~" + antecedent);
                    continue; // Do not process this rule if it has been already added in phase 1
                    //System.out.println("Duplicate Rule: " + precedent + "~~~~~" + antecedent + " --- " + ruleIG.get(precedent + "~~~~~" + antecedent));
                }

                antecedentCount = Integer.parseInt(st1.nextToken());

                System.out.println("antecedent: " + antecedent + "..." + antecedentCount);

                classCount = (Integer) classCountMap.get(antecedent);
                tempProbability = (double) classCount / (double) trainingDataSize;
                hY = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));

                tempProbability = (double) antecedentCount / (double) precedentCount;
                hYX = -(tempProbability * (Math.log(tempProbability) / Math.log(2)));
                tempProbability = (double) precedentCount / (double) trainingDataSize;
                hYX *= tempProbability;

                rules.add(precedent + "~~~~~" + antecedent);
                ruleIG.put(precedent + "~~~~~" + antecedent, (hY - hYX));

                System.out.println("Rule: " + precedent + "~~~~~" + antecedent + " --- " + (hY - hYX));
            }

            file_reader.close();
            buf_reader.close();


            for (index = 0; index < rules.size() - 1; index++) {
                subLine = (String) rules.get(index);
                System.out.println("subLine: " + subLine);
                st = new StringTokenizer(subLine, "~~~~~");
                precedent = st.nextToken();
                antecedent = st.nextToken();

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

                    st1 = new StringTokenizer(precedent1, ",");
                    precedentCount1 = st1.countTokens();
                    //System.out.println("subLine1: " + subLine1);
                    IG1 = (Double) ruleIG.get(subLine1);

                    /*System.out.println("IG, IG1: " + IG + "..." + IG1);
                    System.out.println("antecedent, antecedent1: " + antecedent + "..." + antecedent1);
                    System.out.println("precedentCount, precedentCount1: " + precedentCount + "..." + precedentCount1);*/

                    if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount < precedentCount1) {
                        isStUsed = true;
                        while (st.hasMoreTokens()) {
                            token = st.nextToken();
                            //System.out.println("token: " + token);
                            if (precedent1.contains(token) && !st.hasMoreTokens()) {
                                /*    continue;
                                }
                                else {*/
                                rules.remove(index);
                                //System.out.println("subLine remove: " + subLine);
                                ruleIG.remove(subLine);
                                index--;
                                indexRemoved = true;
                                //}
                            } else if (!precedent1.contains(token)) {
                                //System.out.println("Does not contain token: " + token);
                                break;
                            }
                        }
                    } else if ((!antecedent.equals(antecedent1) || IG == IG1) && precedentCount >= precedentCount1) {
                        while (st1.hasMoreTokens()) {
                            token = st1.nextToken();
                            //System.out.println("token1: " + token);
                            if (precedent.contains(token) && !st1.hasMoreTokens()) {
                                //if(!st1.hasMoreTokens()) {
                                /*    continue;
                                }
                                else {*/
                                rules.remove(index1);
                                //System.out.println("subLine1 remove1: " + subLine1);
                                ruleIG.remove(subLine1);
                                index1--;
                                //}
                            } else if (!precedent.contains(token)) {
                                //System.out.println("Does not contain token1: " + token);
                                break;
                            }
                        }
                    }
                    if (indexRemoved) {
                        break;
                    }

                }
            }
            //tempClasses = new ArrayList(classCountMap.keySet());
            /****topRules = new ArrayList();
            topRuleIG = new HashMap();
            
            
            for(index = 0; index < classes.size(); index++) {
            antecedent = (String) classes.get(index);
            System.out.println("antecedent: " + antecedent);

            tempRules = new ArrayList();
            for(index1 = 0; index1 < rules.size(); index1++) {
            rule = (String) rules.get(index1);
            if(rule.contains(antecedent)) {
            tempRules.add(rule);
            }
            }

            rule = (String) Collections.max(tempRules, new ClassifierClassMaxComparator(ruleIG));
            maxClassIG = (Double) ruleIG.get(rule);
            System.out.println("maxClassIG: " + rule + "..." + maxClassIG);

            for(index1 = 0; index1 < tempRules.size(); index1++) {
            rule = (String) tempRules.get(index1);
            //if(rule.contains(antecedent)) {
            System.out.println("rule: " + rule);//
            IG = (Double) ruleIG.get(rule);
            if(IG >= ((double) marginForTopRules * maxClassIG)) {
            topRules.add(rule);
            rules.remove(rule);
            topRuleIG.put(rule, IG);
            ruleIG.remove(rule);
            }
            //}
            }
            tempRules = null;
            }****/
            Collections.sort(rules, new ClassifierComparator(ruleIG));
            /****Collections.sort(topRules, new ClassifierComparator(topRuleIG));****/
            pw = new PrintWriter("classifierRulesFinal.txt");

            /****for(index = 0; index < topRules.size(); index++) {
            token = (String) topRules.get(index);
            pw.println(token + ";" + topRuleIG.get(token));
            pw.flush();
            }****/
            for (index = 0; index < rules.size(); index++) {
                token = (String) rules.get(index);
                pw.println(token + ";" + ruleIG.get(token));
                pw.flush();
            }
            /*pw.println("END");
            pw.flush();
            pw.println(trainingDataSize);
            pw.flush();*/
            pw.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        //Actual Processing - End
        System.out.println("rules size:  " + rules.size());
    }

    public void fuzzyPhase2(int fileSerialNumber, String attribute, boolean isMainPrune, boolean pruneRulesOnTrainingData) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null;
        PrintWriter pw = null;
        String line, subLine, subLine1, precedent, antecedent, precedent1, token;
        StringTokenizer st = null, st1 = null;
        int index, index1, precedentCount1, precedentCount;
        double hY, hYX, tempProbability, IG, IG1;//, maxClassIG;
        boolean indexRemoved = false, isStUsed = false;
        float precedentCountFloat, antecedentCount, classCount;

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
                //antecedentCount = Float.parseFloat(st1.nextToken()) * trainingDataSize;
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

            if (isMainPrune) {

                System.out.println("Before prune rules.size(): " + rules.size());
                for (index = 0; index < rules.size() - 1; index++) {
                    subLine = (String) rules.get(index);
                    System.out.println("subLine: " + subLine);
                    st = new StringTokenizer(subLine, "~~~~~");
                    precedent = st.nextToken();
                    antecedent = st.nextToken();

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
                        //antecedent1 = st1.nextToken();

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

                        if ((/*!antecedent.equals(antecedent1) ||*/IG <= IG1) && precedentCount <= precedentCount1) {
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
                        } else if ((/*!antecedent.equals(antecedent1) ||*/IG >= IG1) && precedentCount >= precedentCount1) {
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
                        }
                        if (indexRemoved) {
                            break;
                        }
                    }
                }
                System.out.println("After prune rules.size(): " + rules.size());
            }
            Collections.sort(rules, new ClassifierComparatorImages(ruleIG));

            if (pruneRulesOnTrainingData) {
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

    public void uncertainPhase(int fileSerialNumber, boolean isMainPrune) {
        BufferedReader bufReader = null;
        PrintWriter pw = null;
        String line, subLine, subLine1, precedent, precedent1, token;
        StringTokenizer st = null, st1 = null;
        int index, index1, precedentCount1, precedentCount;
        double entropy, entropy1;
        boolean indexRemoved = false, isStUsed = false;
        float frequentnessProbability;

        try {

            bufReader = new BufferedReader(new FileReader("CAR_" + fileSerialNumber + ".txt"));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                //System.out.println("line: " + line);
                st1 = new StringTokenizer(line);
                precedent = st1.nextToken();
                while (st1.countTokens() > 1) {
                    precedent += "," + st1.nextToken();
                }
                frequentnessProbability = Float.parseFloat(st1.nextToken());
                System.out.println(precedent + "..." + frequentnessProbability);

                entropy = -(frequentnessProbability * (Math.log(frequentnessProbability) / Math.log(2)));
                rules.add(precedent);
                ruleIG.put(precedent, entropy);
            }

            bufReader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        if (isMainPrune) {

            System.out.println("Before prune rules.size(): " + rules.size());
            for (index = 0; index < rules.size() - 1; index++) {
                subLine = (String) rules.get(index);
                System.out.println("subLine: " + subLine);
                precedent = subLine;

                st = new StringTokenizer(precedent, ",");
                precedentCount = st.countTokens();
                //System.out.println("subLine: " + subLine);
                entropy = (Double) ruleIG.get(subLine);

                indexRemoved = false;

                System.out.println("index: " + index + "..." + Calendar.getInstance().getTime());

                for (index1 = index + 1; index1 < rules.size(); index1++) {
                    if (isStUsed) {
                        st = new StringTokenizer(precedent, ",");
                        isStUsed = false;
                    }
                    subLine1 = (String) rules.get(index1);
                    precedent1 = subLine1;

                    st1 = new StringTokenizer(precedent1, ",");
                    precedentCount1 = st1.countTokens();
                    entropy1 = (Double) ruleIG.get(subLine1);

                    if (entropy < entropy1 && precedentCount <= precedentCount1) {
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
                    } else if (entropy >= entropy1 && precedentCount >= precedentCount1) {
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
                    }
                    if (indexRemoved) {
                        break;
                    }
                }
            }
            System.out.println("After prune rules.size(): " + rules.size());
        }
        Collections.sort(rules, new ClassifierComparatorImages(ruleIG));

        try {
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
        StringTokenizer st = null, st1;
        float minMembershipValue, membershipValue, ruleTotal, ruleAccuracyTotal;
        int index;
        HashMap ruleAccuracyMap = new HashMap(), ruleTotalMap = new HashMap(), lineFromTestFileMap = new HashMap();
        double IG;

        try {
            bufReader = new BufferedReader(new FileReader(trainingFile));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                lineFromTestFileMap.clear();
                st = new StringTokenizer(line, ",");

                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    token = st1.nextToken();
                    if (!token.contains(claSS)) {
                        /*lineFromTestFileAL.add(token);
                        membershipValue = Float.parseFloat(st1.nextToken());
                        if (minMembershipValue > membershipValue) {
                        minMembershipValue = membershipValue;
                        }*/
                        membershipValue = Float.parseFloat(st1.nextToken());
                        lineFromTestFileMap.put(token, membershipValue);
                    } else {
                        actualAntecedent = token;
                    }
                }

                for (index = 0; index < rules.size(); index++) {
                    subLine = (String) rules.get(index);
                    IG = (Double) ruleIG.get(subLine);
                    //System.out.println("subLine: " + subLine);
                    st = new StringTokenizer(subLine, "~~~~~");
                    precedent = st.nextToken();
                    antecedent = st.nextToken();
                    st1 = new StringTokenizer(precedent, ",");
                    minMembershipValue = 1;

                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        membershipValue = (Float) lineFromTestFileMap.get(token);
                        if (lineFromTestFileMap.containsKey(token)) {
                            if (minMembershipValue > membershipValue) {
                                minMembershipValue = membershipValue;
                            }
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                minMembershipValue *= IG;
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

        System.out.println("ruleAccuracyMap: " + ruleAccuracyMap);
        System.out.println("ruleTotalMap: " + ruleTotalMap);

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

    public void test() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent;
        StringTokenizer st = null, st1 = null;
        int classIndex = 0, index, correctCount = 0, totalCount = 0;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classNames = null, tempAL = null;
        boolean isClassified;
        try {

            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);

            testFileBufferedReader = new BufferedReader(new FileReader(buf_reader.readLine()));
            claSS = buf_reader.readLine();

            file_reader.close();
            buf_reader.close();

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal.txt");
            buf_reader = new BufferedReader(file_reader);

            classifierPrecedents = new ArrayList();
            classifierAntecedents = new ArrayList();

            while (true) {
                line = buf_reader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ";");
                st1 = new StringTokenizer(st.nextToken(), "~~~~~");
                classifierPrecedents.add(st1.nextToken());
                classifierAntecedents.add(st1.nextToken());
            }
            file_reader.close();
            buf_reader.close();
            // Read all rules - End

            // Actaul processing - Start

            classNames = new ArrayList();
            line = testFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");

            for (index = 0; st.hasMoreTokens(); index++) {
                token = st.nextToken();
                classNames.add(token);

                if (claSS.equals(token)) {
                    classIndex = index;
                }
            }

            System.out.println(classNames);

            while (true) {
                line = testFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }

                isClassified = false;
                totalCount++;

                tempAL = new ArrayList();
                st = new StringTokenizer(line, ",");

                System.out.println("line from test file: " + line + "..." + totalCount);

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (index != classIndex) {
                        tempAL.add(classNames.get(index) + "=" + token);
                    } else {
                        actualAntecedent = classNames.get(index) + "=" + token;
                    }
                }

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    if (isClassified) {
                        break;
                    }

                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);

                    st1 = new StringTokenizer(precedent, ",");

                    while (st1.hasMoreTokens()) {

                        if (tempAL.contains(st1.nextToken())) {
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                isClassified = true;
                                if (actualAntecedent.equals(antecedent)) { //correct classfication
                                    correctCount++;
                                    System.out.println("Correct classfication");
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent);
                                } else {
                                    System.out.println("Wrong classfication");
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (!isClassified) {
                    System.out.println("Could not classify");
                    System.out.println("tempAL: " + tempAL);
                }
                tempAL = null;
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("Accuracy: " + ((float) correctCount / (float) totalCount) * 100 + "%");
    }

    public void testBestK() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null;
        StringTokenizer st = null, st1 = null;
        int classIndex = 0, index, correctCount = 0, totalCount = 0, tempTotalCount, kCutOffAbsolute;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classNames = null, tempAL = null, tempAL1 = null;
        boolean isClassified;
        double kCutOffRealtive = 0.05, tempTotalIG, IG, maxAverageIG, tempAverageIG;
        HashMap classifiedTotalIG = null, classifiedTotalNoRules = null;

        try {

            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);

            testFileBufferedReader = new BufferedReader(new FileReader(buf_reader.readLine()));
            claSS = buf_reader.readLine();
            line = buf_reader.readLine();


            file_reader.close();
            buf_reader.close();

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal.txt");
            buf_reader = new BufferedReader(file_reader);

            classifierPrecedents = new ArrayList();
            classifierAntecedents = new ArrayList();
            classifierIG = new ArrayList();

            while (true) {
                line = buf_reader.readLine();
                if (line.equals("END")) {
                    break;
                }
                st = new StringTokenizer(line, ";");
                st1 = new StringTokenizer(st.nextToken(), "~~~~~");
                classifierIG.add(Double.parseDouble(st.nextToken()));
                classifierPrecedents.add(st1.nextToken());
                classifierAntecedents.add(st1.nextToken());
            }
            trainingDataSize = Integer.parseInt(buf_reader.readLine());
            kCutOffAbsolute = (int) (kCutOffRealtive * trainingDataSize);
            file_reader.close();
            buf_reader.close();
            // Read all rules - End
            System.out.println("trainingDataSize, kCutOffAbsolute, kCutOffRealtive" + "..." + trainingDataSize + "..." + kCutOffAbsolute + "..." + kCutOffRealtive);
            // Actaul processing - Start

            classNames = new ArrayList();
            line = testFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");

            for (index = 0; st.hasMoreTokens(); index++) {
                token = st.nextToken();
                classNames.add(token);

                if (claSS.equals(token)) {
                    classIndex = index;
                }
            }

            System.out.println(classNames);

            for (totalCount = 1; true; totalCount++) {
                line = testFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }

                isClassified = false;
                //totalCount++;

                tempAL = new ArrayList();
                st = new StringTokenizer(line, ",");

                System.out.println("line from test file: " + line + "..." + totalCount);

                classifiedTotalIG = new HashMap();
                classifiedTotalNoRules = new HashMap();

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (index != classIndex) {
                        tempAL.add(classNames.get(index) + "=" + token);
                    } else {
                        actualAntecedent = classNames.get(index) + "=" + token;
                    }
                }

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    //if(isClassified)
                    //  break;
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);

                    st1 = new StringTokenizer(precedent, ",");

                    while (st1.hasMoreTokens()) {

                        if (tempAL.contains(st1.nextToken())) {
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                isClassified = true;
                                //if (actualAntecedent.equals(antecedent)) { //correct classfication
                                //correctCount++;
                                //System.out.println("Correct classfication");

                                if (classifiedTotalNoRules.containsKey(antecedent)) {
                                    tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                    if (tempTotalCount >= kCutOffAbsolute) {
                                        continue;
                                    }

                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + tempTotalCount);
                                    tempTotalCount++;
                                    classifiedTotalNoRules.remove(antecedent);
                                    classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                    tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                    tempTotalIG += IG;
                                    classifiedTotalIG.remove(antecedent);
                                    classifiedTotalIG.put(antecedent, tempTotalIG);
                                } else {
                                    classifiedTotalNoRules.put(antecedent, 1);

                                    classifiedTotalIG.put(antecedent, IG);
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG);
                                }
                                //} 
                                /*else {
                                System.out.println("Wrong classfication");
                                System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent);
                                }*/
                            }
                        } else {
                            break;
                        }
                    }
                }
                tempAL = null;

                if (isClassified) {
                    tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                    maxAverageIG = 0;
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                        tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                        tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; //testBestK()
                        //classifiedTotalIG.remove(antecedent);
                        //classifiedTotalIG.put(antecedent, tempAverageIG);

                        if (tempAverageIG > maxAverageIG) {
                            maxAverageIG = tempAverageIG;
                            tempAL1 = new ArrayList();
                            tempAL1.add(antecedent);
                        } else if (tempAverageIG == maxAverageIG) {
                            tempAL1.add(antecedent);
                        }
                    }
                    if (tempAL1.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                    } else {
                        System.out.println("Wrong classfication");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                    }
                } else {
                    System.out.println("Could not classify");
                    //System.out.println("tempAL: " + tempAL);
                }
                tempAL = null;
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("Accuracy: " + ((float) correctCount / (float) totalCount) * 100 + "%");
    }

    public void testBestKWithTopNLengthRules() {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null;
        StringTokenizer st = null, st1 = null;
        int classIndex = 0, index, correctCount = 0, totalCount = 0, tempTotalCount, kCutOffAbsolute, actualRuleLengthCutOffAbsolute, actualKCutOffAbsolute, ruleLengthCutOffAbsolute, maxRuleLengthCutOffAbsolute, ruleLength, currentRuleLength;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, classNames = null, tempAL = null, tempAL1 = null, lineFromTestFileAL = null;
        boolean isClassified, testAgain = false;
        double kCutOffRealtive = 0.05, ruleLengthCutOffRelative = 0.4, tempTotalIG, IG, maxAverageIG, tempAverageIG;
        HashMap classifiedTotalIG = null, classifiedTotalNoRules = null;

        try {

            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);

            testFileBufferedReader = new BufferedReader(new FileReader(buf_reader.readLine()));
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            if (line != null) {
                kCutOffRealtive = Double.parseDouble(line);
            }

            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffRelative = Double.parseDouble(line);
            }

            file_reader.close();
            buf_reader.close();

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal.txt");
            buf_reader = new BufferedReader(file_reader);

            classifierPrecedents = new ArrayList();
            classifierAntecedents = new ArrayList();
            classifierIG = new ArrayList();
            classifierRuleLength = new ArrayList();

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
            //trainingDataSize = Integer.parseInt(buf_reader.readLine());
            file_reader.close();
            buf_reader.close();

            kCutOffAbsolute = (int) (kCutOffRealtive * classifierPrecedents.size());
            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);
            ruleLengthCutOffAbsolute = (int) Math.ceil(ruleLengthCutOffRelative * maxRuleLengthCutOffAbsolute);
            actualRuleLengthCutOffAbsolute = ruleLengthCutOffAbsolute;
            actualKCutOffAbsolute = kCutOffAbsolute;

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), kCutOffAbsolute, kCutOffRealtive, ruleLengthCutOffAbsolute, ruleLengthCutOffRelative" + "..." + classifierPrecedents.size() + "..." + kCutOffAbsolute + "..." + kCutOffRealtive + "......." + ruleLengthCutOffAbsolute + "..." + ruleLengthCutOffRelative);
            // Actual processing - Start

            classNames = new ArrayList();
            line = testFileBufferedReader.readLine();
            st = new StringTokenizer(line, ",");

            for (index = 0; st.hasMoreTokens(); index++) {
                token = st.nextToken();
                classNames.add(token);

                if (claSS.equals(token)) {
                    classIndex = index;
                }
            }

            System.out.println(classNames);

            for (totalCount = 0; true;) {
                if (testAgain) {
                    ruleLengthCutOffAbsolute--;
                    kCutOffAbsolute *= 2;
                    System.out.println("line from test file retested: " + line + "..." + totalCount);
                    System.out.println("kCutOffAbsolute, ruleLengthCutOffAbsolute" + "..." + kCutOffAbsolute + "..." + ruleLengthCutOffAbsolute);
                } else {
                    testAgain = false;
                    ruleLengthCutOffAbsolute = actualRuleLengthCutOffAbsolute;
                    kCutOffAbsolute = actualKCutOffAbsolute;

                    line = testFileBufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().equals("")) {
                        continue;
                    }

                    lineFromTestFileAL = new ArrayList();
                    st = new StringTokenizer(line, ",");
                    for (index = 0; st.hasMoreTokens(); index++) {
                        token = st.nextToken();
                        if (index != classIndex) {
                            lineFromTestFileAL.add(classNames.get(index) + "=" + token);
                        } else {
                            actualAntecedent = classNames.get(index) + "=" + token;
                        }
                    }

                    System.out.println("line from test file: " + line + "..." + totalCount);
                }

                isClassified = false;
                testAgain = false;
                //totalCount++;

                classifiedTotalIG = new HashMap();
                classifiedTotalNoRules = new HashMap();

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    //if(isClassified)
                    //  break;
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (ruleLength < ruleLengthCutOffAbsolute) {
                        continue;
                    }

                    st1 = new StringTokenizer(precedent, ",");

                    while (st1.hasMoreTokens()) {

                        if (lineFromTestFileAL.contains(st1.nextToken())) {
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                isClassified = true;

                                if (classifiedTotalNoRules.containsKey(antecedent)) {
                                    tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                    if (tempTotalCount >= kCutOffAbsolute) {
                                        continue;
                                    }

                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + tempTotalCount);
                                    tempTotalCount++;
                                    classifiedTotalNoRules.remove(antecedent);
                                    classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                    tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                    tempTotalIG += IG;
                                    classifiedTotalIG.remove(antecedent);
                                    classifiedTotalIG.put(antecedent, tempTotalIG);
                                } else {
                                    classifiedTotalNoRules.put(antecedent, 1);

                                    classifiedTotalIG.put(antecedent, IG);
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                //tempAL = null;

                if (isClassified) {
                    tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                    maxAverageIG = 0;
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                        tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                        tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; ///1
                        //tempAverageIG = tempTotalIG / (double) tempTotalCount;///1
                        //classifiedTotalIG.remove(antecedent);
                        //classifiedTotalIG.put(antecedent, tempAverageIG);

                        System.out.println("antecedent ---" + antecedent + "..." + "tempAverageIG --- " + tempAverageIG);

                        if (tempAverageIG > maxAverageIG) {
                            maxAverageIG = tempAverageIG;
                            tempAL1 = new ArrayList();
                            tempAL1.add(antecedent);
                        } else if (tempAverageIG == maxAverageIG) {
                            tempAL1.add(antecedent);
                        }
                    }
                    if (ruleLengthCutOffAbsolute > 0) {
                        for (index = 0; index < tempAL.size(); index++) {

                            antecedent = (String) tempAL.get(index);

                            if (tempAL1.contains(antecedent)) {
                                continue;
                            }
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                            tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                            //System.out.println("antecedent |||" + antecedent + "..." + "tempAverageIG --- " + tempAverageIG);
                            if (tempAverageIG >= 0.99 * maxAverageIG) {
                                testAgain = true;
                                break;
                            }
                        }
                    }
                    if (tempAL1.contains(actualAntecedent) && !testAgain) {
                        correctCount++;
                        System.out.println("Correct classfication");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                    } else if (!testAgain) {
                        System.out.println("Wrong classfication");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                    }
                } else {
                    for (currentRuleLength = ruleLengthCutOffAbsolute - 1; currentRuleLength > 0; currentRuleLength--) {
                        if (isClassified) {
                            break;
                        }
                        for (index = 0; index < classifierPrecedents.size(); index++) {
                            IG = (Double) classifierIG.get(index);
                            precedent = (String) classifierPrecedents.get(index);
                            antecedent = (String) classifierAntecedents.get(index);
                            ruleLength = (Integer) classifierRuleLength.get(index);

                            if (ruleLength != currentRuleLength) {
                                continue;
                            }

                            st1 = new StringTokenizer(precedent, ",");

                            while (st1.hasMoreTokens()) {

                                if (lineFromTestFileAL.contains(st1.nextToken())) {
                                    if (st1.countTokens() > 0) {
                                        continue;
                                    } else {
                                        isClassified = true;
                                        //if (actualAntecedent.equals(antecedent)) { //correct classfication
                                        //correctCount++;
                                        //System.out.println("Correct classfication");

                                        if (classifiedTotalNoRules.containsKey(antecedent)) {
                                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                            if (tempTotalCount >= kCutOffAbsolute) {
                                                continue;
                                            }
                                            System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + tempTotalCount);
                                            tempTotalCount++;
                                            classifiedTotalNoRules.remove(antecedent);
                                            classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                            tempTotalIG += IG;
                                            classifiedTotalIG.remove(antecedent);
                                            classifiedTotalIG.put(antecedent, tempTotalIG);
                                        } else {
                                            classifiedTotalNoRules.put(antecedent, 1);

                                            classifiedTotalIG.put(antecedent, IG);
                                            System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG);
                                        }
                                        //}
                                /*else {
                                        System.out.println("Wrong classfication");
                                        System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent);
                                        }*/
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    //tempAL = null;

                    if (isClassified) {
                        tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                        maxAverageIG = 0;
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                            tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; ////2
                            //tempAverageIG = tempTotalIG / (double) tempTotalCount; ///2
                            //classifiedTotalIG.remove(antecedent);
                            //classifiedTotalIG.put(antecedent, tempAverageIG);

                            System.out.println("antecedent111 ---" + antecedent + "..." + "tempAverageIG111 --- " + tempAverageIG);

                            if (tempAverageIG > maxAverageIG) {
                                maxAverageIG = tempAverageIG;
                                tempAL1 = new ArrayList();
                                tempAL1.add(antecedent);
                            } else if (tempAverageIG == maxAverageIG) {
                                tempAL1.add(antecedent);
                            }
                        }

                        if (ruleLengthCutOffAbsolute > 0) {
                            for (index = 0; index < tempAL.size(); index++) {

                                antecedent = (String) tempAL.get(index);

                                if (tempAL1.contains(antecedent)) {
                                    continue;
                                }
                                tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                                tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                                if (tempAverageIG >= 0.99 * maxAverageIG) {
                                    testAgain = true;
                                    break;
                                }
                            }
                        }
                        if (tempAL1.contains(actualAntecedent) && !testAgain) {
                            correctCount++;
                            System.out.println("Correct classfication");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        } else if (!testAgain) {
                            System.out.println("Wrong classfication");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        }
                    } else {
                        System.out.println("Could not classify");
                    }
                }
                tempAL = null;
                tempAL1 = null;
                if (!testAgain) {
                    lineFromTestFileAL = null;
                    totalCount++;
                }

            }
            testFileBufferedReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("Accuracy: " + ((float) correctCount / (float) (totalCount)) * 100 + "%");
        System.out.println("Accuracy: " + correctCount + "/" + totalCount);
    }

    public float fuzzyTestBestKWithTopNLengthRules(int fileSerialNumber) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null, bestAntecedent = null, defaultClass = null;
        StringTokenizer st = null, st1 = null;
        int index, correctCount = 0, totalCount = 0, tempTotalCount, kCutOffAbsolute, actualRuleLengthCutOffAbsolute, actualKCutOffAbsolute, ruleLengthCutOffAbsolute, maxRuleLengthCutOffAbsolute, ruleLength, currentRuleLength, maxCount, totalTestPoints = 0;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, tempAL1 = null, tempAL2 = null;
        boolean isClassified, testAgain = false;
        double kCutOffRealtive = 0.05, ruleLengthCutOffRelative = 0.4, tempTotalIG, IG, maxAverageIG, tempAverageIG, tempAverageIG1, defaultClassCutOff = 0;
        HashMap classifiedTotalIG = null, classifiedTotalNoRules = null;
        float membershipValue, minMembershipValue = 1;
        HashMap globalClassificationIGMap = new HashMap(), globalClassificationCountMap = new HashMap(), lineFromTestFileMap = new HashMap();
        PrintWriter pw = null;

        lineFromTestFileMap = new HashMap();
        tempAL = new ArrayList();
        tempAL1 = new ArrayList();
        tempAL2 = new ArrayList();

        try {

            pw = new PrintWriter("classificationResults.txt");
            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            if (line != null) {
                kCutOffRealtive = Double.parseDouble(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffRelative = Double.parseDouble(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                defaultClass = claSS + "=" + line;
                line = buf_reader.readLine();
                defaultClassCutOff = Double.parseDouble(line);
            }
            file_reader.close();
            buf_reader.close();

            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
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

            kCutOffAbsolute = (int) (kCutOffRealtive * classifierPrecedents.size());
            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);
            ruleLengthCutOffAbsolute = (int) Math.ceil(ruleLengthCutOffRelative * maxRuleLengthCutOffAbsolute);
            actualRuleLengthCutOffAbsolute = ruleLengthCutOffAbsolute;
            actualKCutOffAbsolute = kCutOffAbsolute;

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), kCutOffAbsolute, kCutOffRealtive, ruleLengthCutOffAbsolute, ruleLengthCutOffRelative" + "..." + classifierPrecedents.size() + "..." + kCutOffAbsolute + "..." + kCutOffRealtive + "......." + ruleLengthCutOffAbsolute + "..." + ruleLengthCutOffRelative);
            // Actual processing - Start

            for (totalTestPoints = 0; true;) {
                if (testAgain) {
                    ruleLengthCutOffAbsolute--;
                    kCutOffAbsolute *= 2;
                    System.out.println("line from test file retested: " + line + "..." + totalCount);
                    System.out.println("kCutOffAbsolute, ruleLengthCutOffAbsolute" + "..." + kCutOffAbsolute + "..." + ruleLengthCutOffAbsolute);
                } else {
                    testAgain = false;
                    ruleLengthCutOffAbsolute = actualRuleLengthCutOffAbsolute;
                    kCutOffAbsolute = actualKCutOffAbsolute;

                    line = testFileBufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().equals("")) {
                        continue;
                    }

                    if (line.equals("END OF DATA POINT")) {
                        pw.println();
                        totalTestPoints++;
                        maxAverageIG = Double.NEGATIVE_INFINITY;
                        maxCount = Integer.MIN_VALUE;
                        tempAL1.clear();
                        tempAL2.clear();

                        tempAL = new ArrayList(globalClassificationCountMap.keySet());
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            totalCount = (Integer) globalClassificationCountMap.get(antecedent);
                            tempAverageIG = (Double) globalClassificationIGMap.get(antecedent);

                            if (totalCount > maxCount) {
                                tempAL1.clear();
                                tempAL1.add(antecedent);
                                maxCount = totalCount;
                            } else if (totalCount == maxCount) {
                                tempAL1.add(antecedent);
                            }
                            if (tempAverageIG > maxAverageIG) {
                                tempAL2.clear();
                                tempAL2.add(antecedent);
                                maxAverageIG = tempAverageIG;
                            } else if (tempAverageIG == maxAverageIG) {
                                tempAL2.add(antecedent);
                            }
                        }

                        System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                        System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);

                        if (tempAL1.size() == 1 && tempAL1.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL Count");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                            /*} else if (tempAL2.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL IG");
                            System.out.println(tempAL2 + "~~~~~" + actualAntecedent);
                            } else if (tempAL1.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL Count Partial");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                             */                        } else {
                            System.out.println("Wrong classfication GLOBAL");
                            System.out.println(tempAL1 + "~~~~~" + tempAL2 + "~~~~~" + actualAntecedent);
                        }

                        globalClassificationCountMap.clear();
                        globalClassificationIGMap.clear();
                        continue;
                    }

                    lineFromTestFileMap.clear();
                    st = new StringTokenizer(line, ",");
                    while (st.hasMoreTokens()) {

                        st1 = new StringTokenizer(st.nextToken(), "^");
                        token = st1.nextToken();
                        if (!token.contains(claSS)) {
                            /*lineFromTestFileAL.add(token);
                            membershipValue = Float.parseFloat(st1.nextToken());
                            if (minMembershipValue > membershipValue) {
                            minMembershipValue = membershipValue;
                            }*/
                            membershipValue = Float.parseFloat(st1.nextToken());
                            lineFromTestFileMap.put(token, membershipValue);
                        } else {
                            actualAntecedent = token;
                        }
                    }
                    System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap + "..." + totalCount);
                }

                isClassified = false;
                testAgain = false;

                classifiedTotalIG = new HashMap();
                classifiedTotalNoRules = new HashMap();

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    //IG = ((Double) classifierIG.get(index)) * minMembershipValue;
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (ruleLength < ruleLengthCutOffAbsolute) {
                        continue;
                    }

                    st1 = new StringTokenizer(precedent, ",");
                    minMembershipValue = 1;

                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        if (lineFromTestFileMap.containsKey(token)) {
                            membershipValue = (Float) lineFromTestFileMap.get(token);
                            if (minMembershipValue > membershipValue) {
                                minMembershipValue = membershipValue;
                            }

                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                isClassified = true;

                                if (classifiedTotalNoRules.containsKey(antecedent)) {
                                    tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                    if (tempTotalCount >= kCutOffAbsolute) {
                                        continue;
                                    }

                                    IG *= minMembershipValue;
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue + "..." + tempTotalCount);
                                    tempTotalCount++;
                                    classifiedTotalNoRules.remove(antecedent);
                                    classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                    tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                    tempTotalIG += IG;
                                    classifiedTotalIG.remove(antecedent);
                                    classifiedTotalIG.put(antecedent, tempTotalIG);
                                } else {
                                    classifiedTotalNoRules.put(antecedent, 1);
                                    IG *= minMembershipValue;
                                    classifiedTotalIG.put(antecedent, IG);
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue + "... 0");
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (isClassified) {
                    tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                    System.out.println("classifiedTotalNoRules :" + classifiedTotalNoRules);
                    System.out.println("classifiedTotalIG :" + classifiedTotalIG);
                    maxAverageIG = Double.NEGATIVE_INFINITY;
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                        tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                        tempAverageIG = tempTotalIG / (double) (tempTotalCount);
                        //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; ///1

                        System.out.println("antecedent111 ---" + antecedent + "..." + "tempAverageIG --- " + tempAverageIG);

                        if (tempAverageIG > maxAverageIG) {
                            maxAverageIG = tempAverageIG;
                            tempAL1 = new ArrayList();
                            tempAL1.add(antecedent);
                            bestAntecedent = antecedent;
                        } else if (tempAverageIG == maxAverageIG) {
                            tempAL1.add(antecedent);
                            bestAntecedent = antecedent;
                        }
                    }
                    if (ruleLengthCutOffAbsolute > 0) {
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            if (tempAL1.contains(antecedent)) {
                                continue;
                            }
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                            //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                            tempAverageIG = tempTotalIG / (double) (tempTotalCount);
                            //System.out.println("antecedent |||" + antecedent + "..." + "tempAverageIG --- " + tempAverageIG);
                            if (tempAverageIG > 0.99 * maxAverageIG && tempAverageIG == maxAverageIG) {
                                testAgain = true;
                                System.out.println("testAgain: " + testAgain);
                                System.out.println("tempAverageIG: " + tempAverageIG);
                                System.out.println("maxAverageIG: " + maxAverageIG);
                                break;
                            }

                            if (antecedent.equals(defaultClass) && !bestAntecedent.equals(defaultClass) && maxAverageIG - tempAverageIG <= defaultClassCutOff * maxAverageIG) {
                                tempAL1.clear();
                                tempAL1.add(defaultClass);
                                bestAntecedent = defaultClass;
                                System.out.println("Default class clause activated...");
                                System.out.println("antecedent, maxAverageIG, tempAverageIG, maxAverageIG - tempAverageIG <= defaultClassCutOff * maxAverageIG: " + antecedent + "... " + maxAverageIG + "..." + tempAverageIG + "..." + (maxAverageIG - tempAverageIG) + " <= " + (defaultClassCutOff * maxAverageIG));
                                break;
                            }
                        }
                    }

                    System.out.println("tempAL1: " + tempAL1);
                    System.out.println("maxAverageIG: " + maxAverageIG);

                    if (tempAL1.contains(actualAntecedent)) {
                        bestAntecedent = actualAntecedent;
                    }

                    tempTotalIG = (Double) classifiedTotalIG.get(bestAntecedent);
                    tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                    if (globalClassificationCountMap.containsKey(bestAntecedent)) {
                        totalCount = (Integer) globalClassificationCountMap.get(bestAntecedent);
                        totalCount++;
                        globalClassificationCountMap.remove(bestAntecedent);
                        globalClassificationCountMap.put(bestAntecedent, totalCount);

                        tempAverageIG1 = (Double) globalClassificationIGMap.get(bestAntecedent);
                        tempAverageIG1 += tempAverageIG;
                        globalClassificationIGMap.remove(bestAntecedent);
                        globalClassificationIGMap.put(bestAntecedent, tempAverageIG1);
                    } else {
                        globalClassificationCountMap.put(bestAntecedent, 1);
                        globalClassificationIGMap.put(bestAntecedent, tempAverageIG);

                    }

                    System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                    System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);

                    if (tempAL1.contains(actualAntecedent) && !testAgain) {
                        //correctCount++;
                        /*tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                        tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                        if (globalClassificationCountMap.containsKey(actualAntecedent)) {
                        totalCount = (Integer) globalClassificationCountMap.get(actualAntecedent);
                        totalCount++;
                        globalClassificationCountMap.remove(actualAntecedent);
                        globalClassificationCountMap.put(actualAntecedent, totalCount);

                        tempAverageIG1 = (Double) globalClassificationIGMap.get(actualAntecedent);
                        tempAverageIG1 += tempAverageIG;
                        globalClassificationIGMap.remove(actualAntecedent);
                        globalClassificationIGMap.put(actualAntecedent, tempAverageIG1);
                        } else {
                        globalClassificationCountMap.put(actualAntecedent, 1);
                        globalClassificationIGMap.put(actualAntecedent, tempAverageIG);

                        }*/
                        System.out.println("Correct classfication local");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        pw.println("correct");
                    } else if (!testAgain) {
                        System.out.println("Wrong classfication local");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        pw.println("wrong");
                    }
                } else {
                    for (currentRuleLength = ruleLengthCutOffAbsolute - 1; currentRuleLength > 0; currentRuleLength--) {
                        if (isClassified) {
                            break;
                        }
                        System.out.println("Reducing rule-length threshold to: " + currentRuleLength);
                        for (index = 0; index < classifierPrecedents.size(); index++) {
                            IG = (Double) classifierIG.get(index);
                            precedent = (String) classifierPrecedents.get(index);
                            antecedent = (String) classifierAntecedents.get(index);
                            ruleLength = (Integer) classifierRuleLength.get(index);

                            if (ruleLength != currentRuleLength) {
                                continue;
                            }

                            st1 = new StringTokenizer(precedent, ",");
                            minMembershipValue = 1;

                            while (st1.hasMoreTokens()) {
                                token = st1.nextToken();
                                if (lineFromTestFileMap.containsKey(token)) {

                                    membershipValue = (Float) lineFromTestFileMap.get(token);
                                    if (minMembershipValue > membershipValue) {
                                        minMembershipValue = membershipValue;
                                    }
                                    if (st1.countTokens() > 0) {
                                        continue;
                                    } else {
                                        isClassified = true;

                                        if (classifiedTotalNoRules.containsKey(antecedent)) {
                                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                            if (tempTotalCount >= kCutOffAbsolute) {
                                                continue;
                                            }
                                            IG *= minMembershipValue;
                                            System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ":::::" + IG + "..." + minMembershipValue + "..." + tempTotalCount);
                                            tempTotalCount++;
                                            classifiedTotalNoRules.remove(antecedent);
                                            classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                            tempTotalIG += IG;
                                            classifiedTotalIG.remove(antecedent);
                                            classifiedTotalIG.put(antecedent, tempTotalIG);
                                        } else {
                                            classifiedTotalNoRules.put(antecedent, 1);
                                            IG *= minMembershipValue;

                                            classifiedTotalIG.put(antecedent, IG);
                                            System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ":::::" + IG + "..." + minMembershipValue + "... 0");
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    if (isClassified) {
                        tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                        maxAverageIG = Double.NEGATIVE_INFINITY;
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                            tempAverageIG = tempTotalIG / (double) (tempTotalCount);
                            //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; ///2


                            System.out.println("antecedent111 ---" + antecedent + "..." + "tempAverageIG111 --- " + tempAverageIG);

                            if (tempAverageIG > maxAverageIG) {
                                maxAverageIG = tempAverageIG;
                                tempAL1 = new ArrayList();
                                tempAL1.add(antecedent);
                            } else if (tempAverageIG == maxAverageIG) {
                                tempAL1.add(antecedent);
                            }
                        }

                        if (ruleLengthCutOffAbsolute > 0) {
                            for (index = 0; index < tempAL.size(); index++) {

                                antecedent = (String) tempAL.get(index);

                                if (tempAL1.contains(antecedent)) {
                                    continue;
                                }
                                tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                                //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                                tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                tempAverageIG = tempTotalIG / (double) (tempTotalCount);

                                if (tempAverageIG >= 0.99 * maxAverageIG) {
                                    testAgain = true;
                                    break;
                                }
                            }
                        }
                        if (tempAL1.contains(actualAntecedent) && !testAgain) {
                            //correctCount++;
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                            tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                            if (globalClassificationCountMap.containsKey(actualAntecedent)) {
                                totalCount = (Integer) globalClassificationCountMap.get(actualAntecedent);
                                totalCount++;
                                globalClassificationCountMap.remove(actualAntecedent);
                                globalClassificationCountMap.put(actualAntecedent, totalCount);

                                tempAverageIG1 = (Double) globalClassificationIGMap.get(actualAntecedent);
                                tempAverageIG1 += tempAverageIG;
                                globalClassificationIGMap.remove(actualAntecedent);
                                globalClassificationIGMap.put(actualAntecedent, tempAverageIG1);
                            } else {
                                globalClassificationCountMap.put(actualAntecedent, 1);
                                globalClassificationIGMap.put(actualAntecedent, tempAverageIG);
                            }
                            System.out.println("Correct classfication local redundant");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        } else {
                            System.out.println("Correct classfication local redundant");
                        }
                    }
                }
                if (!isClassified) {
                    System.out.println("Could not classify");
                }
                tempAL = null;
                tempAL1.clear();
                if (!testAgain) {
                    lineFromTestFileMap.clear();

                }
            }
            testFileBufferedReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + ((float) correctCount / (float) (totalTestPoints)) * 100 + "%");
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + correctCount + "/" + totalTestPoints);
        return ((float) correctCount / (float) (totalTestPoints));
    }

    public float fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRules(int fileSerialNumber) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null, bestAntecedent = null, defaultClass = null, firstClass = null;
        StringTokenizer st = null, st1 = null;
        int index, index1, correctCount = 0, totalCount = 0, count, kCutOffAbsolute = 0, actualRuleLengthCutOffAbsolute, actualKCutOffAbsolute, ruleLengthCutOffAbsolute = 0, maxRuleLengthCutOffAbsolute, ruleLength, currentRuleLength, maxCount, totalTestPoints = 0, classLabelCount = 0;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, tempAL1 = null, tempAL2 = null, ruleAntecedentIGAL = new ArrayList();
        double kCutOffRealtive = 0.05, ruleLengthCutOffRelative = 0.4, tempTotalIG, IG, maxAverageIG, tempAverageIG, tempAverageIG1, defaultClassCutOff = 0, defaultClassCutOffLower = 0;
        float membershipValue, minMembershipValue = 1;
        HashMap globalClassificationIGMap = new HashMap(), globalClassificationCountMap = new HashMap(), lineFromTestFileMap = new HashMap();
        PrintWriter pw = null, pw1 = null;

        lineFromTestFileMap = new HashMap();
        tempAL = new ArrayList();
        tempAL1 = new ArrayList();
        tempAL2 = new ArrayList();

        try {

            pw = new PrintWriter("classificationResults.txt");
            pw1 = new PrintWriter("classificationResultsHistogram.txt");
            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            if (line != null) {
                kCutOffAbsolute = Integer.parseInt(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffAbsolute = Integer.parseInt(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                defaultClass = claSS + "=" + line;
                line = buf_reader.readLine();
                defaultClassCutOff = Double.parseDouble(line);
                line = buf_reader.readLine();
                firstClass = claSS + "=" + line;
                line = buf_reader.readLine();
                defaultClassCutOffLower = Double.parseDouble(line);
            }
            file_reader.close();
            buf_reader.close();

            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
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

            //kCutOffAbsolute = (int) (kCutOffRealtive * classifierPrecedents.size());
            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);
            //ruleLengthCutOffAbsolute = (int) Math.ceil(ruleLengthCutOffRelative * maxRuleLengthCutOffAbsolute);
            //actualRuleLengthCutOffAbsolute = ruleLengthCutOffAbsolute;
            //actualKCutOffAbsolute = kCutOffAbsolute;

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), kCutOffAbsolute, kCutOffRealtive, ruleLengthCutOffAbsolute, ruleLengthCutOffRelative" + "..." + classifierPrecedents.size() + "..." + kCutOffAbsolute + "..." + kCutOffRealtive + "......." + ruleLengthCutOffAbsolute + "..." + ruleLengthCutOffRelative);
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
                    pw.println();
                    totalTestPoints++;
                    maxAverageIG = Double.NEGATIVE_INFINITY;
                    maxCount = Integer.MIN_VALUE;
                    tempAL1.clear();
                    tempAL2.clear();

                    tempAL = new ArrayList(globalClassificationCountMap.keySet());
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        totalCount = (Integer) globalClassificationCountMap.get(antecedent);
                        tempAverageIG = 0;
                        /*if(!antecedent.equals(defaultClass)) {
                        tempAverageIG = (Double) globalClassificationIGMap.get(antecedent);
                        }*/
                        if (antecedent.equals("notClassified")) {
                            continue;
                        }

                        if (totalCount > maxCount) {
                            tempAL1.clear();
                            tempAL1.add(antecedent);
                            maxCount = totalCount;
                        } else if (totalCount == maxCount) {
                            tempAL1.add(antecedent);
                        }
                        /*if (!antecedent.equals(defaultClass)) {
                        if (tempAverageIG > maxAverageIG) {
                        tempAL2.clear();
                        tempAL2.add(antecedent);
                        maxAverageIG = tempAverageIG;
                        } else if (tempAverageIG == maxAverageIG) {
                        tempAL2.add(antecedent);
                        }
                        }*/
                    }

                    System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                    System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);

                    if (tempAL1.size() == 1 && tempAL1.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication GLOBAL Count");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        pw1.println(globalClassificationCountMap + ", correct");
                        /*} else if (tempAL2.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication GLOBAL IG");
                        System.out.println(tempAL2 + "~~~~~" + actualAntecedent);
                        } else if (tempAL1.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication GLOBAL Count Partial");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);*/
                    } else {
                        System.out.println("Wrong classfication GLOBAL");
                        System.out.println(tempAL1 + "~~~~~" + tempAL2 + "~~~~~" + actualAntecedent);
                        pw1.println(globalClassificationCountMap + ", wrong");
                    }

                    globalClassificationCountMap.clear();
                    globalClassificationIGMap.clear();
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

                ruleAntecedentIGAL.clear();

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (ruleLength > ruleLengthCutOffAbsolute) {
                        continue;
                    }

                    st1 = new StringTokenizer(precedent, ",");
                    //minMembershipValue = Float.MAX_VALUE;
                    //minMembershipValue = Float.MIN_VALUE;
                    //minMembershipValue = 1;
                    minMembershipValue = 0;
                    count = 0;

                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        count++;
                        if (lineFromTestFileMap.containsKey(token)) {
                            membershipValue = (Float) lineFromTestFileMap.get(token);
                            //if (minMembershipValue > membershipValue) {
                            /*if (minMembershipValue < membershipValue) {
                            minMembershipValue = membershipValue;
                            }*/
                            //minMembershipValue *= membershipValue;
                            minMembershipValue += membershipValue;

                            //minMembershipValue = 1 / minMembershipValue;

                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                minMembershipValue = minMembershipValue / count;
                                //IG *= (minMembershipValue / (double) classLabelCount);
                                IG *= minMembershipValue;
                                System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue);
                                ruleAntecedentIGAL.add(IG);
                            }
                        } else {
                            break;
                        }
                    }
                }

                Collections.sort(ruleAntecedentIGAL);
                tempTotalIG = 0;

                for (index = ruleAntecedentIGAL.size() - 1, index1 = 0; index1 < kCutOffAbsolute && index >= 0; index--, index1++) {
                    IG = (Double) ruleAntecedentIGAL.get(index);
                    tempTotalIG += IG;
                }
                tempTotalIG = tempTotalIG / (double) (index1);

                if (tempTotalIG >= defaultClassCutOff) {
                    if (globalClassificationCountMap.containsKey(firstClass)) {
                        totalCount = (Integer) globalClassificationCountMap.get(firstClass);
                        totalCount++;
                        globalClassificationCountMap.remove(firstClass);
                        globalClassificationCountMap.put(firstClass, totalCount);
                    } else {
                        globalClassificationCountMap.put(firstClass, 1);
                    }
                    System.out.println("Correct classfication local");
                    pw.println("correct");
                    System.out.println("tempTotalIG >= defaultClassCutOff: " + tempTotalIG + " >= " + defaultClassCutOff + " ~~~~~ " + actualAntecedent);

                } else if (tempTotalIG <= defaultClassCutOffLower) {
                    if (globalClassificationCountMap.containsKey(defaultClass)) {
                        totalCount = (Integer) globalClassificationCountMap.get(defaultClass);
                        totalCount++;
                        globalClassificationCountMap.remove(defaultClass);
                        globalClassificationCountMap.put(defaultClass, totalCount);
                    } else {
                        globalClassificationCountMap.put(defaultClass, 1);
                    }
                    System.out.println("Wrong classfication local");
                    System.out.println("tempTotalIG <= defaultClassCutOffLower: " + tempTotalIG + " <= " + defaultClassCutOffLower + " ~~~~~ " + actualAntecedent);
                    pw.println("wrong");
                } else {
                    if (globalClassificationCountMap.containsKey("notClassified")) {
                        totalCount = (Integer) globalClassificationCountMap.get("notClassified");
                        totalCount++;
                        globalClassificationCountMap.remove("notClassified");
                        globalClassificationCountMap.put("notClassified", totalCount);
                    } else {
                        globalClassificationCountMap.put("notClassified", 1);
                    }
                    System.out.println("No classfication local");
                    pw.println("notclassified");
                    System.out.println("defaultClassCutOffLower < tempTotalIG > defaultClassCutOff: " + defaultClassCutOffLower + " < " + tempTotalIG + " > " + defaultClassCutOff + " ~~~~~ " + actualAntecedent);
                }

                System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);
            }
            testFileBufferedReader.close();
            pw.close();
            pw1.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + ((float) correctCount / (float) (totalTestPoints)) * 100 + "%");
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + correctCount + "/" + totalTestPoints);
        return ((float) correctCount / (float) (totalTestPoints));
    }

    public float fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRulesRanking(int fileSerialNumber) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token = null, prevToken = "", actualAntecedent = null, precedent, antecedent = null;
        StringTokenizer st = null, st1 = null;
        int index, index1, index2, count, ruleLengthCutOffAbsolute = 0, maxRuleLengthCutOffAbsolute, ruleLength, lineNumber = 1;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, ruleAntecedentIGAL = new ArrayList(), alreadyExistingIndex = new ArrayList();
        float membershipValue, minMembershipValue;
        double IG, cumulativeIG, IG1;
        HashMap lineFromTestFileMap = new HashMap();
        PrintWriter pw = null;
        lineFromTestFileMap = new HashMap();

        try {
            pw = new PrintWriter("classificationResults.txt");
            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffAbsolute = Integer.parseInt(line);
            }
            file_reader.close();
            buf_reader.close();
            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
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

            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), ruleLengthCutOffAbsolute" + "..." + classifierPrecedents.size() + "..." + ruleLengthCutOffAbsolute);
            // Actual processing - Start

            while(true) {
                line = testFileBufferedReader.readLine();
                
                if (line != null && line.trim().equals("")) {
                    continue;
                }

                if (line != null) {
                    index = line.indexOf(',');
                    token = line.substring(0, index);
                    line = line.substring(index + 1);
                }
                
                if ((line == null || !token.equals(prevToken)) && !prevToken.equals("")) {
                    tempAL = new ArrayList(ruleAntecedentIGAL);
                    for(index = 0; index < tempAL.size() - 1; index++) {
                        IG = (Double) Collections.max(ruleAntecedentIGAL);
                        ruleAntecedentIGAL.remove(IG);
                        index1 = tempAL.indexOf(IG);
                        if(alreadyExistingIndex.contains(index1)) {
                            System.out.println("Clash lineNumber: " + lineNumber);
                            for(index2 = 0; index2 < tempAL.size(); index2++) {
                                IG1 = (Double) tempAL.get(index2);
                                if(IG == IG1 & !alreadyExistingIndex.contains(index2)) {
                                    index1 = index2;
                                    break;
                                }
                            }
                        }
                        pw.print((index1 + 1) + " ");
                        alreadyExistingIndex.add(index1);
                    }

                    IG = (Double) ruleAntecedentIGAL.get(0);
                    index1 = tempAL.indexOf(IG);
                    if(alreadyExistingIndex.contains(index1)) {
                        for (index2 = 0; index2 < tempAL.size(); index2++) {
                            IG1 = (Double) tempAL.get(index2);
                            if (IG == IG1 & !alreadyExistingIndex.contains(index2)) {
                                index1 = index2;
                                break;
                            }
                        }
                    }
                    pw.println((index1 + 1) + " ");
                    ruleAntecedentIGAL.clear();
                    alreadyExistingIndex.clear();
                    lineNumber++;
                }
                if (line == null) {
                    break;
                }
                prevToken = token;
                cumulativeIG = 0;

                lineFromTestFileMap.clear();
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    token = st1.nextToken();
                    membershipValue = Float.parseFloat(st1.nextToken());
                    lineFromTestFileMap.put(token, membershipValue);
                }
                System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap);
                //System.out.println("classLabelCount: " + classLabelCount);

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (antecedent.contains("0") || antecedent.contains("1") || antecedent.contains("2") || ruleLength > ruleLengthCutOffAbsolute) {
                        continue;
                    }

                    st1 = new StringTokenizer(precedent, ",");
                    //minMembershipValue = 1;
                    minMembershipValue = 0;
                    count = 0;

                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        count++;
                        if (lineFromTestFileMap.containsKey(token)) {
                            membershipValue = (Float) lineFromTestFileMap.get(token);
                            //if (minMembershipValue > membershipValue) {
                            /*if (minMembershipValue < membershipValue) {
                            minMembershipValue = membershipValue;
                            }*/
                            //minMembershipValue *= membershipValue;
                            minMembershipValue += membershipValue;

                            //minMembershipValue = 1 / minMembershipValue;

                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                minMembershipValue = minMembershipValue / count;
                                //IG *= (minMembershipValue / (double) classLabelCount);
                                IG *= minMembershipValue;
                                System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue);
                                cumulativeIG += IG;
                            }
                        } else {
                            break;
                        }
                    }
                }
                ruleAntecedentIGAL.add(cumulativeIG);
                System.out.println("lineNumber: " + lineNumber + " ... cumulativeIG: " + cumulativeIG);
            }

            testFileBufferedReader.close();
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return 0;
    }

    public float fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRulesMultiClassRanking(int fileSerialNumber) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token = null, prevToken = "", actualAntecedent = null, precedent, antecedent = null;
        StringTokenizer st = null, st1 = null;
        int index, index1, index2, indexOuter, count, ruleLengthCutOffAbsolute = 0, maxRuleLengthCutOffAbsolute, ruleLength, lineNumber = 1;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, ruleAntecedentIGAL4 = new ArrayList(), ruleAntecedentIGAL3 = new ArrayList(), ruleAntecedentIGAL2 = new ArrayList(), ruleAntecedentIGAL1 = new ArrayList(), ruleAntecedentIGAL0 = new ArrayList(), clashIndex = new ArrayList(), ruleAntecedentIGALTemp = null, alreadyExistingIndex = new ArrayList();
        float membershipValue, minMembershipValue;
        double IG, cumulativeIG4, cumulativeIG3, cumulativeIG2, cumulativeIG1, cumulativeIG0, IG1;
        HashMap lineFromTestFileMap = new HashMap();
        PrintWriter pw = null;
        lineFromTestFileMap = new HashMap();

        try {
            pw = new PrintWriter("classificationResults.txt");
            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffAbsolute = Integer.parseInt(line);
            }
            file_reader.close();
            buf_reader.close();
            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
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

            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), ruleLengthCutOffAbsolute" + "..." + classifierPrecedents.size() + "..." + ruleLengthCutOffAbsolute);
            // Actual processing - Start

            while(true) {
                line = testFileBufferedReader.readLine();
                if (line != null && line.trim().equals("")) {
                    continue;
                }

                if (line != null) {
                    index = line.indexOf(',');
                    token = line.substring(0, index);
                    line = line.substring(index + 1);
                }

                if ((line == null || !token.equals(prevToken)) && !prevToken.equals("")) {
                    tempAL = new ArrayList(ruleAntecedentIGAL4);
                    for (index = 0; index < ruleAntecedentIGAL4.size(); index++) {
                        IG = Double.NEGATIVE_INFINITY;;
                        clashIndex.clear();
                        for (index2 = 0; index2 < ruleAntecedentIGAL4.size(); index2++) {
                            IG1 = (Double) ruleAntecedentIGAL4.get(index2);
                            if (IG < IG1  && !alreadyExistingIndex.contains(index2)) {
                                IG = IG1;
                            }
                        }
                        for (index2 = 0; index2 < ruleAntecedentIGAL4.size(); index2++) {
                            IG1 = (Double) ruleAntecedentIGAL4.get(index2);
                            if (IG == IG1 && !alreadyExistingIndex.contains(index2)) {
                                clashIndex.add(index2);
                            }
                        }
                        /*System.out.println("IG: " + IG);
                        System.out.println("clashIndex.size(): " + clashIndex.size());
                        System.out.println("ruleAntecedentIGAL4: " + ruleAntecedentIGAL4);
                        System.out.println("ruleAntecedentIGAL4.size(): " + ruleAntecedentIGAL4.size());*/
                        if (clashIndex.size() > 1) {
                            for (indexOuter = 3; indexOuter < 0; indexOuter--) {
                                if (indexOuter == 3) {
                                    ruleAntecedentIGALTemp = ruleAntecedentIGAL3;
                                } else if (indexOuter == 2) {
                                    ruleAntecedentIGALTemp = ruleAntecedentIGAL2;
                                } else if (indexOuter == 1) {
                                    ruleAntecedentIGALTemp = ruleAntecedentIGAL1;
                                } else if (indexOuter == 0) {
                                    ruleAntecedentIGALTemp = ruleAntecedentIGAL0;
                                }

                                IG = Double.NEGATIVE_INFINITY;
                                for (index = 0; index < clashIndex.size(); index++) {
                                    index2 = (Integer) clashIndex.get(index);
                                    IG1 = (Double) ruleAntecedentIGALTemp.get(index2);
                                    if (IG < IG1) {
                                        IG = IG1;
                                    }
                                }
                                for (index = 0; index < clashIndex.size(); index++) {
                                    index2 = (Integer) clashIndex.get(index);
                                    IG1 = (Double) ruleAntecedentIGALTemp.get(index2);
                                    if (IG != IG1) {
                                        clashIndex.remove(index);
                                        index--;
                                    }
                                }
                                if(clashIndex.size() == 1) {
                                    break;
                                }
                            }
                        }
                        index2 = (Integer) clashIndex.get(0);
                        pw.print((index2 + 1) + " ");
                        //ruleAntecedentIGAL4.remove(index2);
                        alreadyExistingIndex.add(index2);
                        if (clashIndex.size() != 1) {
                            System.out.println("Clash lineNumber: " + lineNumber);
                        }
                    }
                    pw.println();
                    ruleAntecedentIGAL4.clear();
                    ruleAntecedentIGAL3.clear();
                    ruleAntecedentIGAL2.clear();
                    ruleAntecedentIGAL1.clear();
                    ruleAntecedentIGAL0.clear();
                    alreadyExistingIndex.clear();
                    lineNumber++;
                }
                if (line == null) {
                    break;
                }
                prevToken = token;
                cumulativeIG4 = 0;
                cumulativeIG3 = 0;
                cumulativeIG2 = 0;
                cumulativeIG1 = 0;
                cumulativeIG0 = 0;

                lineFromTestFileMap.clear();
                st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    st1 = new StringTokenizer(st.nextToken(), "^");
                    token = st1.nextToken();
                    membershipValue = Float.parseFloat(st1.nextToken());
                    lineFromTestFileMap.put(token, membershipValue);
                }
                //System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap);
                //System.out.println("classLabelCount: " + classLabelCount);

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (ruleLength > ruleLengthCutOffAbsolute) {
                        continue;
                    }

                    st1 = new StringTokenizer(precedent, ",");
                    //minMembershipValue = 1;
                    minMembershipValue = 0;
                    count = 0;

                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        count++;
                        if (lineFromTestFileMap.containsKey(token)) {
                            membershipValue = (Float) lineFromTestFileMap.get(token);
                            //if (minMembershipValue > membershipValue) {
                            /*if (minMembershipValue < membershipValue) {
                            minMembershipValue = membershipValue;
                            }*/
                            //minMembershipValue *= membershipValue;
                            minMembershipValue += membershipValue;

                            //minMembershipValue = 1 / minMembershipValue;

                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                minMembershipValue = minMembershipValue / count;
                                //IG *= (minMembershipValue / (double) classLabelCount);
                                IG *= minMembershipValue;
                                //System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue);
                                if(antecedent.equals("CLASS=4")) {
                                    cumulativeIG4 += IG;
                                }
                                else if(antecedent.equals("CLASS=3")) {
                                    cumulativeIG3 += IG;
                                }
                                else if(antecedent.equals("CLASS=2")) {
                                    cumulativeIG2 += IG;
                                }
                                else if(antecedent.equals("CLASS=1")) {
                                    cumulativeIG1 += IG;
                                }
                                else if(antecedent.equals("CLASS=0")) {
                                    cumulativeIG0 += IG;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                ruleAntecedentIGAL4.add(cumulativeIG4);
                ruleAntecedentIGAL3.add(cumulativeIG3);
                ruleAntecedentIGAL2.add(cumulativeIG2);
                ruleAntecedentIGAL1.add(cumulativeIG1);
                ruleAntecedentIGAL0.add(cumulativeIG0);
                System.out.println("lineNumber: " + lineNumber + " ... cumulativeIG: " + cumulativeIG4);
            }
            testFileBufferedReader.close();
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return 0;
    }
    
    public float fuzzyTestBestKWithTopNLengthRulesLatest(int fileSerialNumber) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null, bestAntecedent = null, defaultClass = null;
        StringTokenizer st = null, st1 = null;
        int index, correctCount = 0, totalCount = 0, tempTotalCount, kCutOffAbsolute, actualRuleLengthCutOffAbsolute, actualKCutOffAbsolute, ruleLengthCutOffAbsolute, maxRuleLengthCutOffAbsolute, ruleLength, currentRuleLength, maxCount, totalTestPoints = 0;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, tempAL1 = null, tempAL2 = null;
        boolean isClassified, testAgain = false, noResult;
        double kCutOffRealtive = 0.05, ruleLengthCutOffRelative = 0.4, tempTotalIG, IG, maxAverageIG, tempAverageIG, tempAverageIG1, defaultClassCutOff = 0;
        HashMap classifiedTotalIG = null, classifiedTotalNoRules = null;
        float membershipValue, minMembershipValue = 1;
        HashMap globalClassificationIGMap = new HashMap(), globalClassificationCountMap = new HashMap(), lineFromTestFileMap = new HashMap();
        PrintWriter pw = null;

        lineFromTestFileMap = new HashMap();
        tempAL = new ArrayList();
        tempAL1 = new ArrayList();
        tempAL2 = new ArrayList();

        try {

            pw = new PrintWriter("classificationResults.txt");
            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            if (line != null) {
                kCutOffRealtive = Double.parseDouble(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                ruleLengthCutOffRelative = Double.parseDouble(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                defaultClass = claSS + "=" + line;
                line = buf_reader.readLine();
                defaultClassCutOff = Double.parseDouble(line);
            }
            file_reader.close();
            buf_reader.close();

            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
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

            kCutOffAbsolute = (int) (kCutOffRealtive * classifierPrecedents.size());
            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);
            ruleLengthCutOffAbsolute = (int) Math.ceil(ruleLengthCutOffRelative * maxRuleLengthCutOffAbsolute);
            actualRuleLengthCutOffAbsolute = ruleLengthCutOffAbsolute;
            actualKCutOffAbsolute = kCutOffAbsolute;

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), kCutOffAbsolute, kCutOffRealtive, ruleLengthCutOffAbsolute, ruleLengthCutOffRelative" + "..." + classifierPrecedents.size() + "..." + kCutOffAbsolute + "..." + kCutOffRealtive + "......." + ruleLengthCutOffAbsolute + "..." + ruleLengthCutOffRelative);
            // Actual processing - Start

            for (totalTestPoints = 0; true;) {
                if (testAgain) {
                    ruleLengthCutOffAbsolute--;
                    kCutOffAbsolute *= 2;
                    System.out.println("line from test file retested: " + line + "..." + totalCount);
                    System.out.println("kCutOffAbsolute, ruleLengthCutOffAbsolute" + "..." + kCutOffAbsolute + "..." + ruleLengthCutOffAbsolute);
                } else {
                    testAgain = false;
                    ruleLengthCutOffAbsolute = actualRuleLengthCutOffAbsolute;
                    kCutOffAbsolute = actualKCutOffAbsolute;

                    line = testFileBufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().equals("")) {
                        continue;
                    }

                    if (line.equals("END OF DATA POINT")) {
                        pw.println();
                        totalTestPoints++;
                        maxAverageIG = Double.NEGATIVE_INFINITY;
                        maxCount = Integer.MIN_VALUE;
                        tempAL1.clear();
                        tempAL2.clear();

                        tempAL = new ArrayList(globalClassificationCountMap.keySet());
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            totalCount = (Integer) globalClassificationCountMap.get(antecedent);
                            tempAverageIG = (Double) globalClassificationIGMap.get(antecedent);

                            if (totalCount > maxCount) {
                                tempAL1.clear();
                                tempAL1.add(antecedent);
                                maxCount = totalCount;
                            } else if (totalCount == maxCount) {
                                tempAL1.add(antecedent);
                            }
                            if (tempAverageIG > maxAverageIG) {
                                tempAL2.clear();
                                tempAL2.add(antecedent);
                                maxAverageIG = tempAverageIG;
                            } else if (tempAverageIG == maxAverageIG) {
                                tempAL2.add(antecedent);
                            }
                        }

                        System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                        System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);

                        if (tempAL1.size() == 1 && tempAL1.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL Count");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        } else if (tempAL1.size() == 0) {
                            System.out.println("No classfication GLOBAL");
                            System.out.println(tempAL1 + "~~~~~" + tempAL2 + "~~~~~" + actualAntecedent);

                            /*} else if (tempAL2.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL IG");
                            System.out.println(tempAL2 + "~~~~~" + actualAntecedent);
                            } else if (tempAL1.contains(actualAntecedent)) {
                            correctCount++;
                            System.out.println("Correct classfication GLOBAL Count Partial");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                             */                        } else {
                            System.out.println("Wrong classfication GLOBAL");
                            System.out.println(tempAL1 + "~~~~~" + tempAL2 + "~~~~~" + actualAntecedent);
                        }

                        globalClassificationCountMap.clear();
                        globalClassificationIGMap.clear();
                        continue;
                    }

                    lineFromTestFileMap.clear();
                    st = new StringTokenizer(line, ",");
                    while (st.hasMoreTokens()) {

                        st1 = new StringTokenizer(st.nextToken(), "^");
                        token = st1.nextToken();
                        if (!token.contains(claSS)) {
                            /*lineFromTestFileAL.add(token);
                            membershipValue = Float.parseFloat(st1.nextToken());
                            if (minMembershipValue > membershipValue) {
                            minMembershipValue = membershipValue;
                            }*/
                            membershipValue = Float.parseFloat(st1.nextToken());
                            lineFromTestFileMap.put(token, membershipValue);
                        } else {
                            actualAntecedent = token;
                        }
                    }
                    System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap + "..." + totalCount);
                }

                isClassified = false;
                testAgain = false;

                classifiedTotalIG = new HashMap();
                classifiedTotalNoRules = new HashMap();

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    //IG = ((Double) classifierIG.get(index)) * minMembershipValue;
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (ruleLength < ruleLengthCutOffAbsolute) {
                        continue;
                    }

                    st1 = new StringTokenizer(precedent, ",");
                    minMembershipValue = 1;

                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        if (lineFromTestFileMap.containsKey(token)) {
                            membershipValue = (Float) lineFromTestFileMap.get(token);
                            if (minMembershipValue > membershipValue) {
                                minMembershipValue = membershipValue;
                            }

                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                isClassified = true;

                                if (classifiedTotalNoRules.containsKey(antecedent)) {
                                    tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                    if (tempTotalCount >= kCutOffAbsolute) {
                                        continue;
                                    }

                                    IG *= minMembershipValue;
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue + "..." + tempTotalCount);
                                    tempTotalCount++;
                                    classifiedTotalNoRules.remove(antecedent);
                                    classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                    tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                    tempTotalIG += IG;
                                    classifiedTotalIG.remove(antecedent);
                                    classifiedTotalIG.put(antecedent, tempTotalIG);
                                } else {
                                    classifiedTotalNoRules.put(antecedent, 1);
                                    IG *= minMembershipValue;
                                    classifiedTotalIG.put(antecedent, IG);
                                    System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue + "... 0");
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (isClassified) {
                    tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                    System.out.println("classifiedTotalNoRules :" + classifiedTotalNoRules);
                    System.out.println("classifiedTotalIG :" + classifiedTotalIG);
                    maxAverageIG = Double.NEGATIVE_INFINITY;
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                        tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                        tempAverageIG = tempTotalIG / (double) (tempTotalCount);
                        //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; ///1

                        System.out.println("antecedent111 ---" + antecedent + "..." + "tempAverageIG --- " + tempAverageIG);

                        if (tempAverageIG > maxAverageIG) {
                            maxAverageIG = tempAverageIG;
                            tempAL1 = new ArrayList();
                            tempAL1.add(antecedent);
                            bestAntecedent = antecedent;
                        } else if (tempAverageIG == maxAverageIG) {
                            tempAL1.add(antecedent);
                            bestAntecedent = antecedent;
                        }
                    }
                    noResult = false;
                    if (ruleLengthCutOffAbsolute > 0) {
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            if (tempAL1.contains(antecedent)) {
                                continue;
                            }
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                            //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                            tempAverageIG = tempTotalIG / (double) (tempTotalCount);
                            //System.out.println("antecedent |||" + antecedent + "..." + "tempAverageIG --- " + tempAverageIG);
                            if (tempAverageIG > 0.99 * maxAverageIG && tempAverageIG == maxAverageIG) {
                                testAgain = true;
                                System.out.println("testAgain: " + testAgain);
                                System.out.println("tempAverageIG: " + tempAverageIG);
                                System.out.println("maxAverageIG: " + maxAverageIG);
                                break;
                            }

                            if (maxAverageIG - tempAverageIG <= defaultClassCutOff * maxAverageIG) {
                                noResult = true;
                                System.out.println("No classification local");
                                System.out.println("antecedent, maxAverageIG, tempAverageIG, maxAverageIG - tempAverageIG <= defaultClassCutOff * maxAverageIG: " + antecedent + "... " + maxAverageIG + "..." + tempAverageIG + "..." + (maxAverageIG - tempAverageIG) + " <= " + (defaultClassCutOff * maxAverageIG));
                                break;
                            }
                        }
                    }

                    System.out.println("tempAL1: " + tempAL1);
                    System.out.println("maxAverageIG: " + maxAverageIG);

                    if (noResult) {
                        continue;
                    }

                    if (tempAL1.contains(actualAntecedent)) {
                        bestAntecedent = actualAntecedent;
                    }

                    tempTotalIG = (Double) classifiedTotalIG.get(bestAntecedent);
                    tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                    if (globalClassificationCountMap.containsKey(bestAntecedent)) {
                        totalCount = (Integer) globalClassificationCountMap.get(bestAntecedent);
                        totalCount++;
                        globalClassificationCountMap.remove(bestAntecedent);
                        globalClassificationCountMap.put(bestAntecedent, totalCount);

                        tempAverageIG1 = (Double) globalClassificationIGMap.get(bestAntecedent);
                        tempAverageIG1 += tempAverageIG;
                        globalClassificationIGMap.remove(bestAntecedent);
                        globalClassificationIGMap.put(bestAntecedent, tempAverageIG1);
                    } else {
                        globalClassificationCountMap.put(bestAntecedent, 1);
                        globalClassificationIGMap.put(bestAntecedent, tempAverageIG);

                    }

                    System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                    System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);

                    if (tempAL1.contains(actualAntecedent) && !testAgain) {
                        //correctCount++;
                        /*tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                        tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                        if (globalClassificationCountMap.containsKey(actualAntecedent)) {
                        totalCount = (Integer) globalClassificationCountMap.get(actualAntecedent);
                        totalCount++;
                        globalClassificationCountMap.remove(actualAntecedent);
                        globalClassificationCountMap.put(actualAntecedent, totalCount);

                        tempAverageIG1 = (Double) globalClassificationIGMap.get(actualAntecedent);
                        tempAverageIG1 += tempAverageIG;
                        globalClassificationIGMap.remove(actualAntecedent);
                        globalClassificationIGMap.put(actualAntecedent, tempAverageIG1);
                        } else {
                        globalClassificationCountMap.put(actualAntecedent, 1);
                        globalClassificationIGMap.put(actualAntecedent, tempAverageIG);

                        }*/
                        System.out.println("Correct classfication local");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        pw.println("correct");
                    } else if (!testAgain) {
                        System.out.println("Wrong classfication local");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        pw.println("wrong");
                    }
                } else {
                    for (currentRuleLength = ruleLengthCutOffAbsolute - 1; currentRuleLength > 0; currentRuleLength--) {
                        if (isClassified) {
                            break;
                        }
                        System.out.println("Reducing rule-length threshold to: " + currentRuleLength);
                        for (index = 0; index < classifierPrecedents.size(); index++) {
                            IG = (Double) classifierIG.get(index);
                            precedent = (String) classifierPrecedents.get(index);
                            antecedent = (String) classifierAntecedents.get(index);
                            ruleLength = (Integer) classifierRuleLength.get(index);

                            if (ruleLength != currentRuleLength) {
                                continue;
                            }

                            st1 = new StringTokenizer(precedent, ",");
                            minMembershipValue = 1;

                            while (st1.hasMoreTokens()) {
                                token = st1.nextToken();
                                if (lineFromTestFileMap.containsKey(token)) {

                                    membershipValue = (Float) lineFromTestFileMap.get(token);
                                    if (minMembershipValue > membershipValue) {
                                        minMembershipValue = membershipValue;
                                    }
                                    if (st1.countTokens() > 0) {
                                        continue;
                                    } else {
                                        isClassified = true;

                                        if (classifiedTotalNoRules.containsKey(antecedent)) {
                                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                            if (tempTotalCount >= kCutOffAbsolute) {
                                                continue;
                                            }
                                            IG *= minMembershipValue;
                                            System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ":::::" + IG + "..." + minMembershipValue + "..." + tempTotalCount);
                                            tempTotalCount++;
                                            classifiedTotalNoRules.remove(antecedent);
                                            classifiedTotalNoRules.put(antecedent, tempTotalCount);

                                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                                            tempTotalIG += IG;
                                            classifiedTotalIG.remove(antecedent);
                                            classifiedTotalIG.put(antecedent, tempTotalIG);
                                        } else {
                                            classifiedTotalNoRules.put(antecedent, 1);
                                            IG *= minMembershipValue;

                                            classifiedTotalIG.put(antecedent, IG);
                                            System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ":::::" + IG + "..." + minMembershipValue + "... 0");
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    if (isClassified) {
                        tempAL = new ArrayList(classifiedTotalNoRules.keySet());
                        maxAverageIG = Double.NEGATIVE_INFINITY;
                        for (index = 0; index < tempAL.size(); index++) {
                            antecedent = (String) tempAL.get(index);
                            tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                            tempAverageIG = tempTotalIG / (double) (tempTotalCount);
                            //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute; ///2


                            System.out.println("antecedent111 ---" + antecedent + "..." + "tempAverageIG111 --- " + tempAverageIG);

                            if (tempAverageIG > maxAverageIG) {
                                maxAverageIG = tempAverageIG;
                                tempAL1 = new ArrayList();
                                tempAL1.add(antecedent);
                            } else if (tempAverageIG == maxAverageIG) {
                                tempAL1.add(antecedent);
                            }
                        }

                        if (ruleLengthCutOffAbsolute > 0) {
                            for (index = 0; index < tempAL.size(); index++) {

                                antecedent = (String) tempAL.get(index);

                                if (tempAL1.contains(antecedent)) {
                                    continue;
                                }
                                tempTotalIG = (Double) classifiedTotalIG.get(antecedent);

                                //////////tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                                tempTotalCount = (Integer) classifiedTotalNoRules.get(antecedent);
                                tempAverageIG = tempTotalIG / (double) (tempTotalCount);

                                if (tempAverageIG >= 0.99 * maxAverageIG) {
                                    testAgain = true;
                                    break;
                                }
                            }
                        }
                        if (tempAL1.contains(actualAntecedent) && !testAgain) {
                            //correctCount++;
                            tempTotalIG = (Double) classifiedTotalIG.get(antecedent);
                            tempAverageIG = tempTotalIG / (double) kCutOffAbsolute;
                            if (globalClassificationCountMap.containsKey(actualAntecedent)) {
                                totalCount = (Integer) globalClassificationCountMap.get(actualAntecedent);
                                totalCount++;
                                globalClassificationCountMap.remove(actualAntecedent);
                                globalClassificationCountMap.put(actualAntecedent, totalCount);

                                tempAverageIG1 = (Double) globalClassificationIGMap.get(actualAntecedent);
                                tempAverageIG1 += tempAverageIG;
                                globalClassificationIGMap.remove(actualAntecedent);
                                globalClassificationIGMap.put(actualAntecedent, tempAverageIG1);
                            } else {
                                globalClassificationCountMap.put(actualAntecedent, 1);
                                globalClassificationIGMap.put(actualAntecedent, tempAverageIG);
                            }
                            System.out.println("Correct classfication local redundant");
                            System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        } else {
                            System.out.println("Correct classfication local redundant");
                        }
                    }
                }
                if (!isClassified) {
                    System.out.println("Could not classify");
                }
                tempAL = null;
                tempAL1.clear();
                if (!testAgain) {
                    lineFromTestFileMap.clear();

                }
            }
            testFileBufferedReader.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + ((float) correctCount / (float) (totalTestPoints)) * 100 + "%");
        System.out.println("Accuracy classifierRulesFinal" + fileSerialNumber + ".txt: " + correctCount + "/" + totalTestPoints);
        return ((float) correctCount / (float) (totalTestPoints));
    }

    public float fuzzyTestBestKWithTopNLengthRulesWithDerivedIG(int fileSerialNumber) {
        FileReader file_reader = null;
        BufferedReader buf_reader = null, testFileBufferedReader = null;
        String line, token, actualAntecedent = null, precedent, antecedent = null, bestAntecedent = null, defaultClass = null, firstClass = null;
        StringTokenizer st = null, st1 = null;
        int index, index1, indexOuter, correctCount = 0, totalCount = 0, kCutOffAbsolute = 0, actualRuleLengthCutOffAbsolute, actualKCutOffAbsolute, ruleLengthCutOffAbsolute, maxRuleLengthCutOffAbsolute, ruleLength, currentRuleLength, maxCount, totalTestPoints = 0, aboveFirstClass = 0, aboveDefaultClass = 0;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null, tempAL = null, tempAL1 = null, tempAL2 = null, ruleAntecedentIGAL = null;
        double kCutOffRealtive = 0.05, ruleLengthCutOffRelative = 0.4, tempTotalIG, IG, IGFirstClass = 0, IGDefaultClass = 0, maxIG, defaultClassCutOff = 0, firstClassCutOff = 0, thresholdFirstClass = 0, thresholdDefaultClass = 0;
        float membershipValue, minMembershipValue = 1;
        HashMap globalClassificationIGMap = new HashMap(), globalClassificationCountMap = new HashMap(), lineFromTestFileMap = new HashMap(), ruleAllAntecedentsIGMap = new HashMap();
        PrintWriter pw = null, pw1 = null;

        lineFromTestFileMap = new HashMap();
        tempAL = new ArrayList();
        tempAL1 = new ArrayList();
        tempAL2 = new ArrayList();

        try {

            pw = new PrintWriter("classificationResults.txt");
            pw1 = new PrintWriter("classificationResultsHistogram.txt");
            file_reader = new FileReader("classifierParametersTest.txt");
            buf_reader = new BufferedReader(file_reader);
            claSS = buf_reader.readLine();

            line = buf_reader.readLine();
            if (line != null) {
                kCutOffAbsolute = Integer.parseInt(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                firstClassCutOff = Double.parseDouble(line);
            }
            line = buf_reader.readLine();
            if (line != null) {
                defaultClass = claSS + "=" + line;
                line = buf_reader.readLine();
                defaultClassCutOff = Double.parseDouble(line);
                line = buf_reader.readLine();
                firstClass = claSS + "=" + line;
                line = buf_reader.readLine();
                thresholdFirstClass = Double.parseDouble(line);
                line = buf_reader.readLine();
                thresholdDefaultClass = Double.parseDouble(line);
            }
            file_reader.close();
            buf_reader.close();

            testFileBufferedReader = new BufferedReader(new FileReader("fuzzyTestData" + fileSerialNumber + ".txt"));

            // Read all rules - Start
            file_reader = new FileReader("classifierRulesFinal" + fileSerialNumber + ".txt");
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

            //kCutOffAbsolute = (int) (kCutOffRealtive * classifierPrecedents.size());
            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);
            //ruleLengthCutOffAbsolute = (int) Math.ceil(ruleLengthCutOffRelative * maxRuleLengthCutOffAbsolute);
            //actualRuleLengthCutOffAbsolute = ruleLengthCutOffAbsolute;
            //actualKCutOffAbsolute = kCutOffAbsolute;

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), kCutOffAbsolute, kCutOffRealtive, ruleLengthCutOffAbsolute, ruleLengthCutOffRelative" + "..." + classifierPrecedents.size() + "..." + kCutOffAbsolute + "..." + kCutOffRealtive);
            // Actual processing - Start

            for (totalTestPoints = 0; true;) {

                //System.out.println("kCutOffAbsolute, ruleLengthCutOffAbsolute" + "..." + kCutOffAbsolute + "..." + ruleLengthCutOffAbsolute);

                line = testFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().equals("")) {
                    continue;
                }

                if (line.equals("END OF DATA POINT")) {
                    pw.println();
                    totalTestPoints++;
                    maxCount = Integer.MIN_VALUE;
                    tempAL1.clear();
                    tempAL2.clear();

                    tempAL = new ArrayList(globalClassificationCountMap.keySet());
                    for (index = 0; index < tempAL.size(); index++) {
                        antecedent = (String) tempAL.get(index);
                        totalCount = (Integer) globalClassificationCountMap.get(antecedent);
                        /*if(!antecedent.equals(defaultClass)) {
                        tempAverageIG = (Double) globalClassificationIGMap.get(antecedent);
                        }*/
                        if (antecedent.equals("notClassified")) {
                            continue;
                        }

                        if (totalCount > maxCount) {
                            tempAL1.clear();
                            tempAL1.add(antecedent);
                            maxCount = totalCount;
                        } else if (totalCount == maxCount) {
                            tempAL1.add(antecedent);
                        }
                        /*if (!antecedent.equals(defaultClass)) {
                        if (tempAverageIG > maxAverageIG) {
                        tempAL2.clear();
                        tempAL2.add(antecedent);
                        maxAverageIG = tempAverageIG;
                        } else if (tempAverageIG == maxAverageIG) {
                        tempAL2.add(antecedent);
                        }
                        }*/
                    }

                    System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                    System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);

                    if (tempAL1.size() == 1 && tempAL1.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication GLOBAL Count");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);
                        pw1.println(globalClassificationCountMap + ", correct," + aboveFirstClass + "," + aboveDefaultClass);
                        /*} else if (tempAL2.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication GLOBAL IG");
                        System.out.println(tempAL2 + "~~~~~" + actualAntecedent);
                        } else if (tempAL1.contains(actualAntecedent)) {
                        correctCount++;
                        System.out.println("Correct classfication GLOBAL Count Partial");
                        System.out.println(tempAL1 + "~~~~~" + actualAntecedent);*/
                    } else {
                        System.out.println("Wrong classfication GLOBAL");
                        System.out.println(tempAL1 + "~~~~~" + tempAL2 + "~~~~~" + actualAntecedent);
                        pw1.println(globalClassificationCountMap + ", wrong," + aboveFirstClass + "," + aboveDefaultClass);
                    }

                    globalClassificationCountMap.clear();
                    globalClassificationIGMap.clear();
                    aboveFirstClass = 0;
                    aboveDefaultClass = 0;
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
                    }
                }
                System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap);

                ruleAllAntecedentsIGMap.clear();

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);

                    if (!ruleAllAntecedentsIGMap.containsKey(antecedent)) {
                        ruleAntecedentIGAL = new ArrayList();
                        ruleAllAntecedentsIGMap.put(antecedent, ruleAntecedentIGAL);
                    } else {
                        ruleAntecedentIGAL = (ArrayList) ruleAllAntecedentsIGMap.get(antecedent);
                    }

                    st1 = new StringTokenizer(precedent, ",");
                    
                    minMembershipValue = Float.POSITIVE_INFINITY;
                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        if (lineFromTestFileMap.containsKey(token)) {
                            membershipValue = (Float) lineFromTestFileMap.get(token);
                            if (minMembershipValue > membershipValue) {
                                minMembershipValue = membershipValue;
                            }
                            if (st1.countTokens() > 0) {
                                continue;
                            } else {
                                IG *= minMembershipValue;
                                System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue);
                                ruleAntecedentIGAL.add(IG);
                            }
                        } else {
                            break;
                        }
                    }
                }

                tempAL = new ArrayList(ruleAllAntecedentsIGMap.keySet());
                for (indexOuter = 0; indexOuter < tempAL.size(); indexOuter++) {
                    antecedent = (String) tempAL.get(indexOuter);
                    ruleAntecedentIGAL = (ArrayList) ruleAllAntecedentsIGMap.get(antecedent);

                    Collections.sort(ruleAntecedentIGAL);
                    tempTotalIG = 0;

                    for (index = ruleAntecedentIGAL.size() - 1, index1 = 0; index1 < kCutOffAbsolute && index >= 0; index--, index1++) {
                        IG = (Double) ruleAntecedentIGAL.get(index);
                        tempTotalIG += IG;
                    }
                    if (antecedent.equals(defaultClass)) {
                        IGDefaultClass = tempTotalIG / index1;
                        if (IGDefaultClass >= thresholdDefaultClass) {
                            aboveDefaultClass++;
                        }
                    } else {
                        IGFirstClass = tempTotalIG / index1;
                        if (IGFirstClass >= thresholdFirstClass) {
                            aboveFirstClass++;
                        }
                    }
                }
                System.out.println("IGFirstClass: " + IGFirstClass);
                System.out.println("IGDefaultClass: " + IGDefaultClass);
                if (IGFirstClass > IGDefaultClass && (IGFirstClass - IGDefaultClass) > firstClassCutOff * IGFirstClass) {
                    maxIG = IGFirstClass;
                    antecedent = firstClass;
                    System.out.println((IGFirstClass - IGDefaultClass) + " > " + firstClassCutOff * IGFirstClass + " ... " + antecedent + " ~~~~~ " + actualAntecedent);
                } else if ((IGDefaultClass > IGFirstClass && (IGDefaultClass - IGFirstClass) > defaultClassCutOff * IGDefaultClass)) {
                    maxIG = IGDefaultClass;
                    antecedent = defaultClass;
                    System.out.println((IGDefaultClass - IGFirstClass) + " > " + defaultClassCutOff * IGDefaultClass + " ... " + antecedent + " ~~~~~ " + actualAntecedent);
                } else {
                    //antecedent = "notClassified";
                    maxIG = IGDefaultClass;
                    antecedent = defaultClass;
                }

                //if (Math.abs(IGFirstClass - IGDefaultClass) > defaultClassCutOff * maxIG) {
                //if (IGFirstClass - IGDefaultClass > defaultClassCutOff * IGFirstClass) {
                /*if (Math.abs(IGFirstClass - IGDefaultClass) > defaultClassCutOff * maxIG) {
                System.out.println(Math.abs(IGFirstClass - IGDefaultClass) + " > " + defaultClassCutOff * maxIG + " ... " + antecedent + " ~~~~~ " + actualAntecedent);
                } else {
                antecedent = "notClassified";
                System.out.println(Math.abs(IGFirstClass - IGDefaultClass) + " <= " + defaultClassCutOff * maxIG + " ... notClassified: " + " ~~~~~ " + actualAntecedent);
                }*/
                if (globalClassificationCountMap.containsKey(antecedent)) {
                    totalCount = (Integer) globalClassificationCountMap.get(antecedent);
                    totalCount++;
                    globalClassificationCountMap.remove(antecedent);
                    globalClassificationCountMap.put(antecedent, totalCount);
                } else {
                    globalClassificationCountMap.put(antecedent, 1);
                }

                if (antecedent.equals("notClassified")) {
                    System.out.println("No classfication local");
                    pw.println("notclassified");
                } else if (antecedent.equals(actualAntecedent)) {
                    System.out.println("Correct classfication local");
                    pw.println("correct");
                } else {
                    System.out.println("Wrong classfication local");
                    pw.println("wrong");
                }

                System.out.println("globalClassificationCountMap: " + globalClassificationCountMap);
                System.out.println("globalClassificationIGMap: " + globalClassificationIGMap);
            }
            testFileBufferedReader.close();
            pw.close();
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
