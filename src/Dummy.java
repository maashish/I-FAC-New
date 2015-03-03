
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Dummy {

    public static void main(String[] args) {
        //new Dummy().RapLeafDataSplit(args[0]);
        new Dummy().MIRAnnotation();
    }

    private void MIRAnnotation() {
        HashMap annotationImageMap = new HashMap();
        int index, index1, imageNumber;
        ArrayList tempAL = null, tempAL1;
        String imageNumberString, fileName, objectClass;
        BufferedReader bufReader = null;
        PrintWriter pw = null;
        StringTokenizer st = null, st1 = null;
        File dir = new File("./");
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        };
        String[] children = children = dir.list(filter);;
        if (children == null) {
            // Either dir does not exist or is not a directory
        } else {
            try {
                pw = new PrintWriter(new FileWriter("MIRAnnotations.txt", false), true);
                for (index = 0; index < children.length; index++) {
                    // Get filename of file or directory
                    fileName = children[index];
                    st = new StringTokenizer(fileName, ".");
                    objectClass = st.nextToken();
                    bufReader = new BufferedReader(new FileReader(fileName));
                    while (true) {
                        imageNumberString = bufReader.readLine();
                        if (imageNumberString == null) {
                            break;
                        }
                        imageNumber = Integer.parseInt(imageNumberString);
                        if(annotationImageMap.containsKey(imageNumber)) {
                            tempAL = (ArrayList) annotationImageMap.get(imageNumber);
                        }
                        else {
                            tempAL = new ArrayList();
                            annotationImageMap.put(imageNumber, tempAL);
                        }
                        tempAL.add(objectClass);
                    }
                }

                tempAL1 = new ArrayList(annotationImageMap.keySet());
                Collections.sort(tempAL1);
                for(index = 0; index < tempAL1.size(); index++) {
                    imageNumber = (Integer) tempAL1.get(index);
                    tempAL = (ArrayList) annotationImageMap.get(imageNumber);
                    pw.print(imageNumber + "\t");
                    for(index1 = 0; index1 < (tempAL.size()-1); index1++) {
                        pw.print(tempAL.get(index1) + ",");
                    }
                    pw.println(tempAL.get(index1));
                }
                pw.close();

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void RapLeafDataSplit(String fileName) {
        String token, line;
        BufferedReader bufReader = null;
        PrintWriter pw = null;
        StringTokenizer st = null, st1 = null;
        boolean isLine;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("fuzzytrainingdata0.txt", false), true);
            line = bufReader.readLine();
            while(true) {
                line = bufReader.readLine();
                if(line == null) {
                    break;
                }
                isLine = false;
                st = new StringTokenizer(line, ",");
                st.nextToken();
                token = st.nextToken();
                if(!token.equals(" ")) {
                    pw.print("Age="+token.trim()+"^1,");
                    isLine = true;
                    System.out.println("Age="+token+"^1,");
                }

                token = st.nextToken();
                if(!token.equals(" ")) {
                    pw.print("Gender="+token.trim()+"^1,");
                    isLine = true;
                    System.out.println("Gender="+token+"^1,");
                }

                token = st.nextToken();
                if(!token.equals(" ")) {
                    st1 = new StringTokenizer(token.trim(), ";");
                    if(st1.countTokens() == 3) {
                        pw.print("City="+st1.nextToken().trim()+"^1,");
                        isLine = true;
                    }
                    if(st1.countTokens() == 2) {
                        pw.print("State="+st1.nextToken().trim()+"^1,");
                        isLine = true;
                    }
                    pw.print("Country="+st1.nextToken().trim()+"^1");
                    isLine = true;
                }
                if(isLine) {
                    pw.println();
                }
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }
}
