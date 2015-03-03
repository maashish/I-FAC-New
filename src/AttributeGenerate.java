
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.StringTokenizer;


public class AttributeGenerate {
    
    private String fileName = null;
    private HashMap attributeNames = new HashMap();
    private HashMap attributeFileWriters = new HashMap();
    private String delim = "\t";


    public static void main(String[] args) {
        AttributeGenerate attributeGenerate = new AttributeGenerate();
        attributeGenerate.readFileAll();
        attributeGenerate.generateCrispTrainingDatasetAll();
     }
    
    public void readFileAll() {
        File file = new File("attributegenerateall.csv");
        FileReader file_reader = null;
        BufferedReader buf_reader = null;

        StringTokenizer st = null;
        String line = null, attributeName;
        int attributeIndex;

        try {
            
            file_reader = new FileReader(file);
            buf_reader = new BufferedReader(file_reader);

            fileName = buf_reader.readLine();

            while (true) {
                line = buf_reader.readLine();

                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                attributeIndex = Integer.parseInt(st.nextToken());
                attributeName = st.nextToken();

                attributeNames.put(attributeIndex, attributeName);
                /*pw = new PrintWriter(new FileWriter(attributeName + ".txt", false));
                attributeFileWriters.put(attributeIndex, pw);*/
            }

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

    }
    
    public void generateCrispTrainingDatasetAll () {
        File file = new File(fileName);
        FileReader file_reader = null;
        BufferedReader buf_reader = null;
        //FileWriter fw = null;
        PrintWriter pw = null;
        int index = 0, index1;
        String line = null, linePruned, token;
        StringTokenizer st = null;
        long tokenLong;
        double tokenDouble;
        
        
        try {
            file_reader = new FileReader(file);
            buf_reader = new BufferedReader(file_reader);
            pw = new PrintWriter(new FileWriter("crisptrainingdata.txt", false));

            for (index = 1; true; index++) {
                line = buf_reader.readLine();
                if(line==null)
                    break;
                
                st = new StringTokenizer(line, delim);
                linePruned = new String("");
                
                for(index1 = 1; st.hasMoreTokens(); index1++){
                    if(attributeNames.containsKey(index1)) {
                        
                        token = st.nextToken();

                        try {
                            tokenLong = Long.parseLong(token);
                            if (linePruned.equals("")) {
                                linePruned += tokenLong;
                            } else {
                                linePruned += "," + tokenLong;
                            }
                        } catch (NumberFormatException e) {

                            try {
                                tokenDouble = Double.parseDouble(token);
                                if (linePruned.equals("")) {
                                    linePruned += tokenDouble;
                                } else {
                                    linePruned += "," + tokenDouble;
                                }
                            } catch (NumberFormatException e1) {

                                if (linePruned.equals("")) {
                                    linePruned += token;
                                } else {
                                    linePruned += "," + token;
                                }
                            }
                        }
                    }
                    else
                        st.nextToken();
                }
                pw.println(linePruned);
                pw.flush();
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }

    
    public void readFile() {
        File file = new File("attributegenerate.csv");
        FileReader file_reader = null;
        BufferedReader buf_reader = null;
        
        PrintWriter pw = null;

        StringTokenizer st = null;
        String line = null, attributeName;
        int attributeIndex;

        try {
            
            file_reader = new FileReader(file);
            buf_reader = new BufferedReader(file_reader);

            fileName = buf_reader.readLine();

            while (true) {
                line = buf_reader.readLine();

                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");
                attributeIndex = Integer.parseInt(st.nextToken());
                attributeName = st.nextToken();

                attributeNames.put(attributeIndex, attributeName);
                pw = new PrintWriter(new FileWriter(attributeName + ".txt", false));
                attributeFileWriters.put(attributeIndex, pw);
            }

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }

    }
    
    public void generateCrispTrainingDataset () {
        File file = new File(fileName);
        FileReader file_reader = null;
        BufferedReader buf_reader = null;
        //FileWriter fw = null;
        PrintWriter pw = null;
        int index = 0, fileSize = 0, index1, indexAttribute;
        String line = null, token;
        StringTokenizer st = null;
        HashMap attributeAlreadyPresent = new HashMap();
        HashMap tempHashMap = null;
        HashMap attributeIndex = new HashMap();
        
        
        try {
            //pwCrispTraining = new PrintWriter(new FileWriter("crisptrainingdata.csv", false));
            /*file_reader = new FileReader(file);
            buf_reader = new BufferedReader(file_reader);
            
            for (fileSize = 0; true; fileSize++) {
                line = buf_reader.readLine();
                if(line==null)
                    break;
            }
            
            file_reader.close();
            buf_reader.close();
            
            System.out.println("fileSize check done..." + fileSize);*/
            
            
            
            file_reader = new FileReader(file);
            buf_reader = new BufferedReader(file_reader);

            for (index = 1; true; index++) {
                line = buf_reader.readLine();
                if(line==null)
                    break;
                
                st = new StringTokenizer(line, delim);
                
                for(index1 = 1; st.hasMoreTokens(); index1++){
                    if(attributeNames.containsKey(index1)) {
                        
                        pw = (PrintWriter) attributeFileWriters.get(index1);
                        
                        if(index==1){
                            pw.println(fileSize + "\t1" + "\t" + attributeNames.get(index1));
                            pw.flush();
                            
                            tempHashMap = new HashMap();
                            attributeAlreadyPresent.put((String) attributeNames.get(index1), tempHashMap);
                            tempHashMap = null;
                            
                            attributeIndex.put((String) attributeNames.get(index1), index);
                        }
                        
                        token = st.nextToken();
                        tempHashMap = (HashMap) attributeAlreadyPresent.get((String) attributeNames.get(index1));
                        
                        if (!tempHashMap.containsKey(token)) {
                            indexAttribute = ((Integer) attributeIndex.get((String) attributeNames.get(index1))).intValue();
                            
                            pw.println("x" + indexAttribute + "\tx" + indexAttribute + "\t" + token);
                            pw.flush();
                            pw = null;
                            
                            tempHashMap.put(token, null);
                            
                            indexAttribute++;
                            attributeIndex.remove((String) attributeNames.get(index1));
                            attributeIndex.put((String) attributeNames.get(index1), indexAttribute);
                        }
                        tempHashMap = null;
                        
                    }
                    else
                        st.nextToken();
                }
                
            }
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
            e.printStackTrace();
        }
    }
}


