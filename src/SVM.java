
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class SVM {

    public void createSVMFormatFromCAR(String CARFile, float datasetSize) {
        BufferedReader bufReader = null;
        String line, token1, token2, classLabel;
        StringTokenizer st = null;
        PrintWriter pw = null;
        float classLabelMembership;
        ArrayList featureAL = new ArrayList();
        int index;

        try {
            pw = new PrintWriter(new FileWriter("trainingSVM.txt", false), true);
            bufReader = new BufferedReader(new FileReader(CARFile));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                //System.out.println("line: " + line);

                st = new StringTokenizer(line, "~~~~~");
                token1 = st.nextToken();
                token2 = st.nextToken();

                st = new StringTokenizer(token2);
                classLabel = st.nextToken();
                classLabelMembership = Float.parseFloat(st.nextToken()) / datasetSize;

                st = new StringTokenizer(classLabel, "=");
                st.nextToken();
                pw.print(st.nextToken() + " ");

                st = new StringTokenizer(token1);
                featureAL.clear();
                while (st.countTokens() > 1) {
                    featureAL.add(Integer.parseInt(st.nextToken()));
                }

                Collections.sort(featureAL);
                for (index = 0; index < (featureAL.size() - 1); index++) {
                    pw.print(featureAL.get(index) + ":" + classLabelMembership + " ");
                }
                pw.println(featureAL.get(index) + ":" + classLabelMembership);
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createSVMFormatFromACFormat(String testFileName, String classIndexMappingFileName, int k, boolean isTest) {
        BufferedReader bufReader = null;
        String line, token;
        StringTokenizer st = null;
        PrintWriter pw = null;
        HashMap<String, Integer> classIndexMap = new HashMap();
        StringBuffer lineBuffer = null;
        int indexClass = -1, index, lineNumber1 = 0, indexLine, featureNumber, maxLimit;

        try {
            bufReader = new BufferedReader(new FileReader(classIndexMappingFileName));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line);
                classIndexMap.put(new String("CLASS=" + st.nextToken() + "^1"), Integer.parseInt(st.nextToken()));
            }
            bufReader.close();

            pw = new PrintWriter(new FileWriter("SVM.txt", false), true);
            bufReader = new BufferedReader(new FileReader(testFileName));
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1);
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                if (line.equals("END OF DATA POINT")) {
                    continue;
                }
                st = new StringTokenizer(line, ",");
                lineBuffer = new StringBuffer(" ");
                if (isTest) {
                    indexClass = -1;
                }
                //System.out.println("NEW LINE");
                if(isTest) {
                    maxLimit = k;
                }
                else {
                    maxLimit = k + 1;
                }
                for (indexLine = 1, token = st.nextToken(); indexLine <= maxLimit; indexLine++) {
                    //System.out.println("indexLineOUTER: " + indexLine + "..." + token);
                    if (!isTest && token.contains("CLASS")) {
                        indexClass = classIndexMap.get(token);
                        System.out.println("indexClass: " + indexClass + "..." + token);
                        if (st.hasMoreTokens()) {
                            token = st.nextToken();
                        }
                        else {
                            token = "";
                        }
                    } else {
                        index = token.indexOf("^");
                        if(index != -1) {
                            featureNumber = Integer.parseInt(token.substring(0, index));
                        }
                        else {
                            featureNumber = -1;
                        }
                        
                        if (featureNumber == indexLine && featureNumber != -1) {
                            //System.out.println("featureNumber: " + indexLine + "..." + token);
                            lineBuffer.append(featureNumber);
                            lineBuffer.append(":");
                            lineBuffer.append(token.substring(index + 1));
                            lineBuffer.append(" ");
                            if (st.hasMoreTokens()) {
                                token = st.nextToken();
                            }
                        } else {
                            //System.out.println("indexLine: " + indexLine + "..." + token);
                            lineBuffer.append(indexLine);
                            lineBuffer.append(":");
                            lineBuffer.append("0 ");
                        }
                    }
                }
                lineBuffer.deleteCharAt(lineBuffer.length() - 1);
                pw.println(indexClass + lineBuffer.toString());
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createACFormatFromSVMFormat(String testFileName, String featureIndexMappingFileName, boolean isTest) {
        BufferedReader bufReader = null;
        String line, token, feature;
        StringTokenizer st = null;
        PrintWriter pw = null;
        HashMap<Integer, String> featureIndexMap = new HashMap();
        StringBuffer lineBuffer = null;
        int indexClass = -1, index, lineNumber1 = 0;

        try {
            bufReader = new BufferedReader(new FileReader(featureIndexMappingFileName));

            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                featureIndexMap.put(Integer.parseInt(st.nextToken()), st.nextToken());
            }
            bufReader.close();
             System.out.println("featureIndexMap: " + featureIndexMap);

            pw = new PrintWriter(new FileWriter("AC.txt", false), true);
            bufReader = new BufferedReader(new FileReader(testFileName));
            while (true) {
                if (lineNumber1++ % 10000 == 0) {
                    System.out.println("lineNumber1: " + lineNumber1 + Calendar.getInstance().getTime());
                }
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }

                //System.out.println("line: " + line);
                st = new StringTokenizer(line);
                indexClass = Integer.parseInt(st.nextToken());
                lineBuffer = new StringBuffer();
                while (st.hasMoreTokens()) {
                    token = st.nextToken();
                    index = token.indexOf(":");
                    feature = featureIndexMap.get(Integer.parseInt(token.substring(0, index)));
                    //lineBuffer.append(token.substring(0, index));
                    lineBuffer.append(feature);
                    lineBuffer.append("^");
                    lineBuffer.append(token.substring(index + 1));
                    lineBuffer.append(",");
                }
                lineBuffer.deleteCharAt(lineBuffer.length() - 1);
                pw.print(lineBuffer.toString());
                if (!isTest) {
                    //pw.print(classIndexMap.get(indexClass));
                    pw.print(",CLASS=" + indexClass + "^1");
                }
                pw.println();
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public float fuzzyApplyBestKWithTopNLengthRules(String inputTrainingFile, String inputClassifierRuleFile, boolean isTest) {
        BufferedReader bufReader = null, testFileBufferedReader = null;
        String line, token = null, actualAntecedent = null, precedent, antecedent = null, claSS;
        StringTokenizer st = null, st1 = null;
        int index, count, ruleLengthCutOffAbsolute = 0, maxRuleLengthCutOffAbsolute, ruleLength, lineNumber = 1, maxNumberOfRulesCutOffAbsolute, antecedentNumberOfRules;
        ArrayList classifierPrecedents = null, classifierAntecedents = null, classifierIG = null, classifierRuleLength = null;
        float membershipValue, minMembershipValue;
        double IG;
        HashMap lineFromTestFileMap = new HashMap(), antecedentNumberOfRulesMap = new HashMap();
        PrintWriter pw = null;
        boolean isClassified, isFirstRule;

        try {
            pw = new PrintWriter("trainingCARSVM.txt");
            bufReader = new BufferedReader(new FileReader("classifierParametersTest.txt"));
            claSS = bufReader.readLine();
            maxNumberOfRulesCutOffAbsolute = Integer.parseInt(bufReader.readLine());
            ruleLengthCutOffAbsolute = Integer.parseInt(bufReader.readLine());
            bufReader.close();


            // Read all rules - Start
            bufReader = new BufferedReader(new FileReader(inputClassifierRuleFile));

            classifierPrecedents = new ArrayList();
            classifierAntecedents = new ArrayList();
            classifierIG = new ArrayList();
            classifierRuleLength = new ArrayList();

            System.out.println("START of CLASSIFICATION");

            while (true) {
                line = bufReader.readLine();
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
            bufReader.close();

            maxRuleLengthCutOffAbsolute = (Integer) Collections.max(classifierRuleLength);
            System.out.println("MAX ruleLengthCutOffAbsolute: " + maxRuleLengthCutOffAbsolute);

            // Read all rules - End
            System.out.println("classifierPrecedents.size(), ruleLengthCutOffAbsolute" + "..." + classifierPrecedents.size() + "..." + ruleLengthCutOffAbsolute);
            // Actual processing - Start
            testFileBufferedReader = new BufferedReader(new FileReader(inputTrainingFile));
            while (true) {
                line = testFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                lineFromTestFileMap.clear();
                antecedentNumberOfRulesMap.clear();
                actualAntecedent = null;
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
                if (actualAntecedent.contains("0") || actualAntecedent.contains("1") || actualAntecedent.contains("2")) {
                    continue;
                }
                isClassified = false;
                isFirstRule = true;
                //System.out.println("lineFromTestFileMap from test file: " + lineFromTestFileMap);
                //System.out.println("classLabelCount: " + classLabelCount);

                for (index = 0; index < classifierPrecedents.size(); index++) {
                    IG = (Double) classifierIG.get(index);
                    precedent = (String) classifierPrecedents.get(index);
                    antecedent = (String) classifierAntecedents.get(index);
                    ruleLength = (Integer) classifierRuleLength.get(index);
                    if (antecedentNumberOfRulesMap.containsKey(antecedent)) {
                        antecedentNumberOfRules = (Integer) antecedentNumberOfRulesMap.get(antecedent);
                    } else {
                        antecedentNumberOfRules = 0;
                        antecedentNumberOfRulesMap.put(antecedent, antecedentNumberOfRules);
                    }

                    if (antecedent.contains("0") || antecedent.contains("1") || antecedent.contains("2") || ruleLength > ruleLengthCutOffAbsolute || antecedentNumberOfRules > maxNumberOfRulesCutOffAbsolute) {
                        continue;
                    }
                    st1 = new StringTokenizer(precedent, ",");
                    //minMembershipValue = 1;
                    minMembershipValue = 0;
                    count = 0;
                    while (st1.hasMoreTokens()) {
                        token = st1.nextToken();
                        count++;
                        if (lineFromTestFileMap.containsKey(token) || (isTest && token.contains(claSS))) {
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
                                isClassified = true;
                                //System.out.println(precedent + "~~~~~" + antecedent + "~~~~~" + actualAntecedent + ";;;;;" + IG + "..." + minMembershipValue);
                                if (isFirstRule) {
                                    isFirstRule = false;
                                    if (actualAntecedent != null && !isTest) {
                                        st = new StringTokenizer(actualAntecedent, "=");
                                        st.nextToken();
                                        pw.print(st.nextToken());
                                    } else {
                                        pw.print("-1");
                                    }
                                }
                                pw.print(" " + (index + 1) + ":" + IG);
                                antecedentNumberOfRulesMap.remove(antecedent);
                                antecedentNumberOfRules++;
                                antecedentNumberOfRulesMap.put(antecedent, antecedentNumberOfRules);
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (isClassified) {
                    pw.println();
                }
                System.out.println("lineNumber: " + lineNumber);
            }
            testFileBufferedReader.close();
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return 0;
    }

    public float fuzzyRanking(String inputFileClassifierResults, String inputFileTest) {
        BufferedReader bufReader = null, testFileBufferedReader = null;
        String line, token = null, prevToken = "", line1 = null;
        StringTokenizer st = null;
        int index, index1, index2, lineNumber = 1;
        ArrayList tempAL = null, ruleAntecedentIGAL = new ArrayList(), alreadyExistingIndex = new ArrayList();
        double membershipValue;
        double IG, IG1;
        PrintWriter pw = null;

        try {
            pw = new PrintWriter("classificationResults.txt");
            testFileBufferedReader = new BufferedReader(new FileReader(inputFileTest));

            // Read all rules - Start
            bufReader = new BufferedReader(new FileReader(inputFileClassifierResults));

            System.out.println("START of RANKING");
            // Actual processing - Start

            while (true) {
                line = testFileBufferedReader.readLine();
                line1 = bufReader.readLine();

                if (line != null && line.trim().equals("")) {
                    continue;
                }

                if (line != null) {
                    index = line.indexOf(',');
                    if (index != -1) {
                        token = line.substring(0, index);
                        //line = line.substring(index + 1);
                    } else {
                        token = line;
                    }
                }

                if ((line == null || !token.equals(prevToken)) && !prevToken.equals("")) {
                    tempAL = new ArrayList(ruleAntecedentIGAL);
                    for (index = 0; index < tempAL.size() - 1; index++) {
                        IG = (Double) Collections.max(ruleAntecedentIGAL);
                        ruleAntecedentIGAL.remove(IG);
                        index1 = tempAL.indexOf(IG);
                        if (alreadyExistingIndex.contains(index1)) {
                            System.out.println("Clash lineNumber: " + lineNumber);
                            for (index2 = 0; index2 < tempAL.size(); index2++) {
                                IG1 = (Double) tempAL.get(index2);
                                if (IG == IG1 & !alreadyExistingIndex.contains(index2)) {
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
                    if (alreadyExistingIndex.contains(index1)) {
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

                st = new StringTokenizer(line1);
                membershipValue = Double.parseDouble(st.nextToken());
                ruleAntecedentIGAL.add(membershipValue);
                System.out.println("lineNumber: " + lineNumber + " ... membershipValue: " + membershipValue);
            }

            testFileBufferedReader.close();
            bufReader.close();
            pw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        SVM svm = new SVM();
        //svm.createSVMFormatFromCAR(args[0], Float.parseFloat(args[1]));
        //svm.createSVMFormatFromACFormat(args[0], args[1], Integer.parseInt(args[2]), false);
        svm.createACFormatFromSVMFormat(args[0], args[1], Boolean.parseBoolean(args[2]));
        //svm.fuzzyApplyBestKWithTopNLengthRules(args[0], args[1], false);
        //svm.fuzzyRanking(args[0], args[1]);
    }
}
