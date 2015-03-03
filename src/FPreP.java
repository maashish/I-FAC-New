
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

public class FPreP {

    private ArrayList classLabel = new ArrayList();
    private ArrayList classType = new ArrayList();
    private HashMap classFilename = new HashMap();
    private HashMap numAttribute = new HashMap();
    private HashMap numValue = new HashMap();
    private String decisionClassLabel = null;
    private String trainingFilename = null;
    private float muCutOff;

    public void FCMOnDataset(int k, float m, boolean isHeaderRequired) {

        File file = new File("facme.csv");
        StringTokenizer st = null;

        String label, type, filename = null, line, token;
        int index = 0, commaIndex = 0;

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(fileReader);

            muCutOff = Float.parseFloat(bufReader.readLine());
            trainingFilename = bufReader.readLine();
            decisionClassLabel = bufReader.readLine();

            for (index = 1; true; index++) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                label = st.nextToken();
                type = st.nextToken();
                
                if (type.equalsIgnoreCase("num")) {
                    new FCM().generateFuzzyClustersFloatInMemoryOneDimensional(trainingFilename, k, m, index, label, 0, 1000000, isHeaderRequired);
                }
            }
            bufReader.close();
            fileReader.close();

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public void readFiles() {

        File file = new File("facme.csv");
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        StringTokenizer st = null;

        String label, type, filename = null, line, token;
        int index = 0, commaIndex = 0;

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(fileReader);

            muCutOff = Float.parseFloat(bufReader.readLine());
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
            fileReader.close();

            /*file = new File(trainingFilename);
            fileReader = new FileReader(file);
            bufReader = new BufferedReader(fileReader);*/

            /*while (true) {
            line = bufReader.readLine();
            if (line == null) {
            break;
            }
            trainingData.add(line);
            }*/

            //System.out.println("trainingData: " + trainingData);

            //bufReader.close();
            //fileReader.close();
            for (index = 0; index < classType.size(); index++) {
                label = (String) classLabel.get(index);
                type = (String) classType.get(index);
                if (type.equalsIgnoreCase("num")) {
                    file = new File((String) classFilename.get(label));

                    fileReader = new FileReader(file);
                    bufReader = new BufferedReader(fileReader);

                    line = bufReader.readLine();

                    st = new StringTokenizer(line, ",");
                    token = st.nextToken();
                    if (!token.equals(label)) {
                        System.out.println("Fatal Error, label mismatch... Exiting: " + label + "..." + token);
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

                        numValueTemp.put(Float.parseFloat(line.substring(0, commaIndex)), line.substring(commaIndex + 1));
                    }

                    numValue.put(label, numValueTemp);

                    //System.out.println("numValueTemp: " + numValueTemp);
                    //System.out.println("numValue: " + numValue);
                    bufReader.close();
                    fileReader.close();
                }
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public void generateFuzzyTrainingData(int indexFile, boolean isTest) {
        int index, indexFuzzy, caretIndex = 0, commaIndex = 0, commaIndex1 = 0, numberOfTokens = 0;
        StringTokenizer st = null, st1 = null;
        String line = null, attribute, valueString, fuzzyValue, fuzzyMembership, fuzzyMembershipString, type, rule, oldRule = null, tempRule, tempRule1, preRule, currentRule, postRule;
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        //ArrayList fuzzyTrainingData = new ArrayList();
        File file = new File(trainingFilename), oldFile = null;
        FileReader fileReader = null;
        BufferedReader bufReader = null;
        FileWriter fw = null;
        PrintWriter pw = null;
        boolean isFirst;

        attribute = (String) classLabel.get(0);
        type = (String) classType.get(0);

        System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

        try {
            fileReader = new FileReader(file);
            bufReader = new BufferedReader(fileReader);
            if(isTest) {
                fw = new FileWriter("fuzzytestdata" + indexFile + ".txt", false);
            }
            else {
                fw = new FileWriter("fuzzytrainingdata" + indexFile + ".txt", false);
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
        pw = new PrintWriter(fw, true);
        ///loop to generate 1st iteration rules

        isFirst = true;

        while (true) {
            try {
                line = bufReader.readLine();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            st = new StringTokenizer(line, ",");
            if(isFirst) {
                isFirst = false;
                numberOfTokens = st.countTokens();
            }

            commaIndex = line.indexOf(',');
            postRule = line.substring(commaIndex);
            valueString = st.nextToken();

            if(valueString.equals("?")) {
                pw.println(postRule.substring(1));
                pw.flush();
                if (isTest) {
                    pw.println("END OF DATA POINT");
                }
                continue;
            }

            if (type.equalsIgnoreCase("nom")) {
                rule = attribute + "=" + valueString + "^1" + postRule;
                //fuzzyTrainingData.add(rule);
                pw.println(rule);
                pw.flush();
                //System.out.println("rule: " + rule);
            } else if (type.equalsIgnoreCase("num")) {
                numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                numValueTemp = (HashMap) numValue.get(attribute);
                if(!numValueTemp.containsKey(Float.parseFloat(valueString)) && isTest) {
                    interpolate(Float.parseFloat(valueString), numValueTemp);
                }
                fuzzyMembershipString = (String) numValueTemp.get(Float.parseFloat(valueString));
                //System.out.println("fuzzyMembershipString, value: " + fuzzyMembershipString + "..." + value);
                st1 = new StringTokenizer(fuzzyMembershipString, ",");

                for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                    fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                    fuzzyMembership = st1.nextToken();
                    if (Float.parseFloat(fuzzyMembership) >= muCutOff) {
                        rule = attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                        //fuzzyTrainingData.add(rule);
                        pw.println(rule);
                        pw.flush();
                        //System.out.println("Fuzzy rule: " + rule);
                    }
                }
            }
            if(isTest) {
                pw.println("END OF DATA POINT");
            }
        }
        ///loop to generate 1st iteration rules - END
        try {
            fileReader.close();
            bufReader.close();
            pw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
        //System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);

        ///loop to generate next iteration - after 1st - rules

        for (index = 1; index < classLabel.size() && index < numberOfTokens; index++) {
            attribute = (String) classLabel.get(index);
            type = (String) classType.get(index);
            /*trainingDataSize = fuzzyTrainingData.size();
            oldfuzzyTrainingData=null;
            
            oldfuzzyTrainingData = fuzzyTrainingData;
            fuzzyTrainingData = new ArrayList();*/

            if(isTest) {
                file = new File("fuzzytestdata" + indexFile + ".txt");
                oldFile = new File("oldfuzzytestdata" + indexFile + ".txt");
            }
            else {
                file = new File("fuzzytrainingdata" + indexFile + ".txt");
                oldFile = new File("oldfuzzytrainingdata" + indexFile + ".txt");
            }

            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);

            if(isTest) {
                file = new File("oldfuzzytestdata" + indexFile + ".txt");
            }
            else {
                file = new File("oldfuzzytrainingdata" + indexFile + ".txt");
            }
            oldFile = null;

            try {
                fileReader = new FileReader(file);
                bufReader = new BufferedReader(fileReader);

                if (isTest) {
                    fw = new FileWriter("fuzzytestdata" + indexFile + ".txt", false);
                } else {
                    fw = new FileWriter("fuzzytrainingdata" + indexFile + ".txt", false);
                }
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }

            pw = new PrintWriter(fw, true);
            System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

            while (true) {
                //oldRule = (String) oldfuzzyTrainingData.get(indexRule);
                try {
                    oldRule = bufReader.readLine();
                } catch (IOException e) {
                    System.out.println("IO exception = " + e);
                    e.printStackTrace();
                }
                if (oldRule == null) {
                    break;
                }
                if(oldRule.equals("END OF DATA POINT")) {
                    pw.println("END OF DATA POINT");
                    continue;
                }
                caretIndex = oldRule.lastIndexOf('^');
                if (caretIndex > -1) {
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
                } else {
                    st = new StringTokenizer(oldRule, ",");
                    commaIndex = oldRule.indexOf(',');
                    postRule = oldRule.substring(commaIndex);
                    currentRule = st.nextToken();
                    preRule = "";
                }

                if (currentRule.equals("?")) {
                    System.out.println("postRule: " + postRule);
                    if (!postRule.isEmpty()) {
                        pw.println(preRule + postRule.substring(1));
                    } else {
                        pw.println(preRule);
                    }
                    pw.flush();
                    continue;
                }
                if (type.equalsIgnoreCase("nom")) {
                    rule = preRule + attribute + "=" + currentRule + "^1" + postRule;
                    //fuzzyTrainingData.add(rule);
                    pw.println(rule);
                    pw.flush();
                    //System.out.println("rule: " + rule);
                } else if (type.equalsIgnoreCase("num")) {
                    numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                    numValueTemp = (HashMap) numValue.get(attribute);
                    if (!numValueTemp.containsKey(Float.parseFloat(currentRule)) && isTest) {
                        interpolate(Float.parseFloat(currentRule), numValueTemp);
                    }
                    fuzzyMembershipString = (String) numValueTemp.get(Float.parseFloat(currentRule));
                    st1 = new StringTokenizer(fuzzyMembershipString, ",");

                    for (indexFuzzy = 0; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                        fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                        fuzzyMembership = st1.nextToken();
                        if (Float.parseFloat(fuzzyMembership) >= muCutOff) {
                            rule = preRule + attribute + "=" + fuzzyValue + "^" + fuzzyMembership + postRule;
                            //fuzzyTrainingData.add(rule);
                            pw.println(rule);
                            pw.flush();
                            //System.out.println("Fuzzy rule: " + rule);
                        }
                    }
                }
            }
            System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
            try {
                fileReader.close();
                bufReader.close();
                pw.close();
                fw.close();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
        }
        if (isTest) {
            oldFile = new File("oldfuzzytestdata" + indexFile + ".txt");
        } else {
            oldFile = new File("oldfuzzytrainingdata" + indexFile + ".txt");
        }
        if (oldFile.exists()) {
            oldFile.delete();
        }
        ///loop to generate next iteration - after 1st - rules --- END
    }

    public void interpolate(float value, HashMap numValueTemp) {
        StringBuffer fuzzyMembershipString = new StringBuffer();
        float valueLower = value, valueUpper = value, temp, mu, deltaLower=Float.POSITIVE_INFINITY, deltaUpper=Float.NEGATIVE_INFINITY;
        ArrayList tempAL = new ArrayList(numValueTemp.keySet());
        int index;
        String fuzzyMembershipLowerString = null, fuzzyMembershipUpperString = null;
        //String valueLowerString, valueUpperString,
        StringTokenizer st1, st2;

        System.out.println("value from interpolate(): " + value);
        System.out.println("numValueTemp: " + numValueTemp);

        for(index = 0; index < tempAL.size(); index++) {
            temp = (Float) tempAL.get(index);
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

        //valueLowerString = Float.toString(valueLower);
        //valueUpperString = Float.toString(valueUpper);
        System.out.println("valueLower: " + valueLower);
        System.out.println("valueUpper: " + valueUpper);

        fuzzyMembershipLowerString = (String) numValueTemp.get(valueLower);
        fuzzyMembershipUpperString = (String) numValueTemp.get(valueUpper);
        System.out.println("fuzzyMembershipLowerString: " + fuzzyMembershipLowerString);
        System.out.println("fuzzyMembershipUpperString: " + fuzzyMembershipUpperString);

        if (fuzzyMembershipLowerString == null) {
            deltaUpper = Float.NEGATIVE_INFINITY;
            valueLower = valueUpper;
            for (index = 0; index < tempAL.size(); index++) {
                temp = (Float) tempAL.get(index);
                //System.out.println("value - temp, deltaLower, deltaUpper: " + (value-temp) + "..." + deltaLower + "..." + deltaUpper);
                if (valueLower - temp < 0 && valueLower - temp > deltaUpper) {
                    valueUpper = temp;
                    deltaUpper = valueLower - temp;
                }
            }

            //valueLowerString = Float.toString(valueLower);
            //valueUpperString = Float.toString(valueUpper);
            System.out.println("valueLower Amended for Extrapolation1: " + valueLower);
            System.out.println("valueUpper Amended for Extrapolation1: " + valueUpper);

            if(!numValueTemp.containsKey(valueUpper)) {
                valueUpper = calculateNearestNeighbour(valueUpper, tempAL, valueLower);
            }
            fuzzyMembershipLowerString = (String) numValueTemp.get(valueUpper);
            fuzzyMembershipUpperString = (String) numValueTemp.get(valueLower);
            System.out.println("fuzzyMembershipLowerString1: " + fuzzyMembershipLowerString);
            System.out.println("fuzzyMembershipUpperString1: " + fuzzyMembershipUpperString);


        } else if (fuzzyMembershipUpperString == null) {
            deltaLower = Float.POSITIVE_INFINITY;
            valueUpper = valueLower;
            for (index = 0; index < tempAL.size(); index++) {
                temp = (Float) tempAL.get(index);
                //System.out.println("value - temp, deltaLower, deltaUpper: " + (value-temp) + "..." + deltaLower + "..." + deltaUpper);
                if (valueUpper - temp > 0 && valueUpper - temp < deltaLower) {
                    valueLower = temp;
                    deltaLower = valueUpper - temp;
                }
            }

            //valueLowerString = Float.toString(valueLower);
            //valueUpperString = Float.toString(valueUpper);
            System.out.println("valueLower Amended for Extrapolation2: " + valueLower);
            System.out.println("valueUpper Amended for Extrapolation2: " + valueUpper);

            if(!numValueTemp.containsKey(valueLower)) {
                valueLower = calculateNearestNeighbour(valueLower, tempAL, valueUpper);
            }
            fuzzyMembershipLowerString = (String) numValueTemp.get(valueLower);
            fuzzyMembershipUpperString = (String) numValueTemp.get(valueUpper);
        }
        System.out.println("valueLowerString Final: " + valueLower);
        System.out.println("valueUpperString Final: " + valueUpper);
        System.out.println("fuzzyMembershipLowerString Final: " + fuzzyMembershipLowerString);
        System.out.println("fuzzyMembershipUpperString Final: " + fuzzyMembershipUpperString);

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
        numValueTemp.put(value, fuzzyMembershipString.toString());
        System.out.println("fuzzyMembershipString: " + fuzzyMembershipString);
    }

    float cosineInterpolate(float y1, float y2, float mu) {
        float mu2;

        mu2 = (float)(1 - Math.cos(mu * Math.PI)) / 2;
        return (y1 * (1 - mu2) + y2 * mu2);
    }

    float calculateNearestNeighbour(float value, ArrayList valueAL, float valueOther) {
        float valueNearestNeighbour = Float.POSITIVE_INFINITY, temp;
        int index;
        for(index = 0; index < valueAL.size(); index++) {
            temp = Float.parseFloat((String) valueAL.get(index));
            if(Math.abs(value - temp) < Math.abs(value - valueNearestNeighbour) && temp != value && temp != valueOther) {
                valueNearestNeighbour = temp;
            }
        }
        System.out.println("calculateNearestNeighbour(): " + valueNearestNeighbour);
        return valueNearestNeighbour;
    }

    public void generateFuzzyTrainingDataOldPrep(int indexFile, boolean isTest) {
        int index, indexFuzzy, caretIndex = 0, commaIndex = 0, commaIndex1 = 0, numberOfTokens = 0;
        StringTokenizer st = null, st1 = null;
        String line = null, attribute, valueString, fuzzyValue = null, fuzzyMembership = null, fuzzyMembershipString, type, rule, oldRule = null, tempRule, tempRule1, preRule, currentRule, postRule;
        ArrayList numAttributeTemp = null;
        HashMap numValueTemp = null;
        float minFuzzyMembership;
        boolean isFirst;
        //ArrayList fuzzyTrainingData = new ArrayList();

        File file = new File(trainingFilename), oldFile = null;
        FileReader fileReader = null;
        BufferedReader bufReader = null;
        FileWriter fw = null;
        PrintWriter pw = null;
        attribute = (String) classLabel.get(0);
        type = (String) classType.get(0);

        System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

        try {
            fileReader = new FileReader(file);
            bufReader = new BufferedReader(fileReader);
            if(isTest) {
                fw = new FileWriter("fuzzytestdata" + indexFile + ".txt", false);
            }
            else {
                fw = new FileWriter("fuzzytrainingdata" + indexFile + ".txt", false);
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        pw = new PrintWriter(fw, true);
        isFirst = true;
        ///loop to generate 1st iteration rules
        while (true) {
            try {
                line = bufReader.readLine();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            st = new StringTokenizer(line, ",");
            if(isFirst) {
                isFirst = false;
                numberOfTokens = st.countTokens();
            }
            commaIndex = line.indexOf(',');

            postRule = line.substring(commaIndex);
            valueString = st.nextToken();
            
            if (valueString.equals("?")) {
                pw.println(postRule.substring(1));
                pw.flush();
                if (isTest) {
                    pw.println("END OF DATA POINT");
                }
                continue;
            }
            if (type.equalsIgnoreCase("nom")) {
                rule = attribute + "=" + valueString + "^1" + postRule;
                //fuzzyTrainingData.add(rule);
                pw.println(rule);
                pw.flush();
                //System.out.println("rule: " + rule);
            } else if (type.equalsIgnoreCase("num")) {
                numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                numValueTemp = (HashMap) numValue.get(attribute);
                if(!numValueTemp.containsKey(Float.parseFloat(valueString)) && isTest) {
                    interpolate(Float.parseFloat(valueString), numValueTemp);
                }
                fuzzyMembershipString = (String) numValueTemp.get(Float.parseFloat(valueString));
                st1 = new StringTokenizer(fuzzyMembershipString, ",");

                for (indexFuzzy = 0, minFuzzyMembership = Float.NEGATIVE_INFINITY; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                    fuzzyMembership = st1.nextToken();
                    if (Float.parseFloat(fuzzyMembership) > minFuzzyMembership) {
                        fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                        minFuzzyMembership = Float.parseFloat(fuzzyMembership);
                    }
                }
                rule = attribute + "=" + fuzzyValue + "^" + minFuzzyMembership + postRule;
                pw.println(rule);
                pw.flush();
            }
            if(isTest) {
                pw.println("END OF DATA POINT");
            }
        }
        ///loop to generate 1st iteration rules - END
        try {
            fileReader.close();
            bufReader.close();
            pw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

        System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
        //System.out.println("\n\n\n\n\nattribute End: " + attribute + "fuzzyTrainingData: " + fuzzyTrainingData);

        ///loop to generate next iteration - after 1st - rules

        for (index = 1; index < classLabel.size() && index < numberOfTokens; index++) {
            attribute = (String) classLabel.get(index);
            type = (String) classType.get(index);
            /*trainingDataSize = fuzzyTrainingData.size();
            oldfuzzyTrainingData=null;

            oldfuzzyTrainingData = fuzzyTrainingData;
            fuzzyTrainingData = new ArrayList();*/

            if(isTest) {
                file = new File("fuzzytestdata" + indexFile + ".txt");
                oldFile = new File("oldfuzzytestdata" + indexFile + ".txt");
            }
            else {
                file = new File("fuzzytrainingdata" + indexFile + ".txt");
                oldFile = new File("oldfuzzytrainingdata" + indexFile + ".txt");
            }

            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);

            if(isTest) {
                file = new File("oldfuzzytestdata" + indexFile + ".txt");
            }
            else {
                file = new File("oldfuzzytrainingdata" + indexFile + ".txt");
            }
            oldFile = null;

            try {
                fileReader = new FileReader(file);
                bufReader = new BufferedReader(fileReader);

                if (isTest) {
                    fw = new FileWriter("fuzzytestdata" + indexFile + ".txt", false);
                } else {
                    fw = new FileWriter("fuzzytrainingdata" + indexFile + ".txt", false);
                }
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }

            pw = new PrintWriter(fw, true);
            System.out.println("attribute Start: " + attribute + "\tTime: " + Calendar.getInstance().getTime());

            while (true) {
                //oldRule = (String) oldfuzzyTrainingData.get(indexRule);
                try {
                    oldRule = bufReader.readLine();
                } catch (IOException e) {
                    System.out.println("IO exception = " + e);
                    e.printStackTrace();
                }
                if (oldRule == null) {
                    break;
                }
                if(oldRule.equals("END OF DATA POINT")) {
                    pw.println("END OF DATA POINT");
                    continue;
                }
                caretIndex = oldRule.lastIndexOf('^');
                if (caretIndex > -1) {
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
                } else {
                    st = new StringTokenizer(oldRule, ",");
                    commaIndex = oldRule.indexOf(',');
                    postRule = oldRule.substring(commaIndex);
                    currentRule = st.nextToken();
                    preRule = "";
                }

                if (currentRule.equals("?")) {
                    System.out.println("postRule: " + postRule);
                    if (!postRule.isEmpty()) {
                        pw.println(preRule + postRule.substring(1));
                    } else {
                        pw.println(preRule);
                    }
                    pw.flush();
                    continue;
                }

                if (type.equalsIgnoreCase("nom")) {
                    rule = preRule + attribute + "=" + currentRule + "^1" + postRule;
                    //fuzzyTrainingData.add(rule);
                    pw.println(rule);
                    pw.flush();
                    //System.out.println("rule: " + rule);
                } else if (type.equalsIgnoreCase("num")) {
                    numAttributeTemp = (ArrayList) numAttribute.get(attribute);
                    numValueTemp = (HashMap) numValue.get(attribute);
                    if (!numValueTemp.containsKey(Float.parseFloat(currentRule)) && isTest) {
                        interpolate(Float.parseFloat(currentRule), numValueTemp);
                    }
                    fuzzyMembershipString = (String) numValueTemp.get(Float.parseFloat(currentRule));
                    st1 = new StringTokenizer(fuzzyMembershipString, ",");

                    for (indexFuzzy = 0, minFuzzyMembership = Float.NEGATIVE_INFINITY; indexFuzzy < numAttributeTemp.size(); indexFuzzy++) {
                        fuzzyMembership = st1.nextToken();
                        if (Float.parseFloat(fuzzyMembership) > minFuzzyMembership) {
                            fuzzyValue = (String) numAttributeTemp.get(indexFuzzy);
                            minFuzzyMembership = Float.parseFloat(fuzzyMembership);
                        }
                    }
                    rule = preRule + attribute + "=" + fuzzyValue + "^" + minFuzzyMembership + postRule;
                    pw.println(rule);
                    pw.flush();
                }
            }

            System.out.println("\n\n\n\n\nattribute End: " + attribute + "\tTime: " + Calendar.getInstance().getTime());
            try {
                fileReader.close();
                bufReader.close();
                pw.close();
                fw.close();
            } catch (IOException e) {
                System.out.println("IO exception = " + e);
                e.printStackTrace();
            }
        }
        ///loop to generate next iteration - after 1st - rules --- END
        if (isTest) {
            oldFile = new File("oldfuzzytestdata" + indexFile + ".txt");
        } else {
            oldFile = new File("oldfuzzytrainingdata" + indexFile + ".txt");
        }
        if (oldFile.exists()) {
            oldFile.delete();
        }
    }
}
