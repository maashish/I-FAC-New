
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class TabStorageSplitter {
    
    public static void Split(String fileName) {
        BufferedReader bufReader = null;
        String inputFileName = null, line, subLine, attribute;
        StringTokenizer st = null;
        ArrayList attributesAL = new ArrayList(), tempAL = null, tempAL1 = null;
        HashMap numericalAttributesMap = new HashMap();
        PrintWriter pw;
        int index, index1;
        
        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            inputFileName = bufReader.readLine();
            bufReader.readLine();
            
            while(true) {
                line = bufReader.readLine();
                if(line == null) {
                    break;
                }
                
                st = new StringTokenizer(line, ",");
                subLine = st.nextToken();
                attributesAL.add(subLine);
                if(st.nextToken().equals("num")) {
                   numericalAttributesMap.put(subLine, new ArrayList());
                    
                }
            }
            
            bufReader.close();
                       
            bufReader = new BufferedReader(new FileReader(inputFileName + ".txt"));
            while(true) {
                line = bufReader.readLine();
                if(line == null) {
                    break;
                }
                
                st = new StringTokenizer(line, ",");
                
                for(index1 = 0; index1 < attributesAL.size(); index1++) {
                    subLine = st.nextToken();
                    attribute = (String) attributesAL.get(index1);
                    if(numericalAttributesMap.containsKey(attribute)) {
                        tempAL = (ArrayList) numericalAttributesMap.get(attribute);
                        if(!tempAL.contains(subLine)) {
                            tempAL.add(subLine);
                        }
                    }
                }
            }
            
            bufReader.close();
            
            tempAL1 = new ArrayList(numericalAttributesMap.keySet());
            for(index = 0; index < tempAL1.size(); index++) {

                attribute = (String) tempAL1.get(index);
                tempAL = (ArrayList) numericalAttributesMap.get(attribute);

                pw = new PrintWriter(new FileWriter(attribute + "_FCM_input.txt", false), true);
                pw.println(tempAL.size() + "\t1\t" + attribute);

                for (index1 = 0; index1 < tempAL.size(); index1++) {
                    subLine = (String) tempAL.get(index1);
                    pw.println("x" + (index1 + 1) + "\tx" + (index1 + 1) + "\t" + subLine);

                }
                
                pw.close();
            }
            
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TabStorageSplitter.Split("facme.csv");
    }

}
