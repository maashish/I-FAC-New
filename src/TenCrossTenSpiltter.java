
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;

public class TenCrossTenSpiltter {

    public static void ImageSplit(int numberOfImages, int numberOfCrosses) {
        int index, nextTest, indexImage;
        PrintWriter pw = null;
        StringBuffer sb = null;
        try {

            pw = new PrintWriter(new FileWriter("ImageSplit.txt", false), true);

            for (nextTest = (numberOfCrosses - 1), indexImage = 1; indexImage <= numberOfImages; indexImage++, nextTest--) {
                //indexLastComma = line.lastIndexOf(',');
                //decisionClass = line.substring(indexLastComma + 1);

                if(nextTest < 0/* || !decisionClass.equals(prevDecisionClass)*/) {
                    nextTest = (numberOfCrosses - 1);
                }

                sb = new StringBuffer(indexImage + "\t");

                for(index = 0; index < numberOfCrosses; index++) {
                    if(nextTest != index) {
                        sb.append(index + ",");
                    }
                }
                //System.out.println(sb);
                sb.deleteCharAt(sb.length()-1);
                //System.out.println(sb);
                sb.append("," + nextTest);
                pw.println(sb);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void Split(String fileName, int numberOfFolds) {
        BufferedReader bufReader = null;
        int lineNumber = 0, index, nextTest;
        String line, decisionClass = null, prevDecisionClass = null;
        HashMap printWritersTrain = new HashMap(), printWritersTest = new HashMap();
        PrintWriter pw = null;
        try {
            
            for(index = 0; index < numberOfFolds; index++) {
                printWritersTrain.put(index, new PrintWriter(new FileWriter("fuzzyTrainingData" + index + ".txt", false), true));
                printWritersTest.put(index, new PrintWriter(new FileWriter("fuzzyTestData" + index + ".txt", false), true));
            }
            bufReader = new BufferedReader(new FileReader(fileName));
            for (nextTest = numberOfFolds-1; true; nextTest--) {
                line = bufReader.readLine();
                if (lineNumber % 10000 == 0) {
                    System.out.println("lineNumber: " + lineNumber + "\t" + Calendar.getInstance().getTime());
                }
                lineNumber++;
                if (line == null) {
                    break;
                }
                //indexLastComma = line.lastIndexOf(',');
                //decisionClass = line.substring(indexLastComma + 1);
                if(nextTest < 0/* || !decisionClass.equals(prevDecisionClass)*/) {
                    nextTest = numberOfFolds-1;
                }
                for(index = 0; index < numberOfFolds; index++) {
                    if(nextTest == index) {
                        pw = (PrintWriter) printWritersTest.get(index);
                    }
                    else {
                        pw = (PrintWriter) printWritersTrain.get(index);
                    }
                    pw.println(line);
                }
                //prevDecisionClass = decisionClass;
            }
            for(index = 0; index < numberOfFolds; index++) {
                ((PrintWriter) printWritersTest.get(index)).close();
                ((PrintWriter) printWritersTrain.get(index)).close();
            }
            bufReader.close();
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void SplitSingle(String fileName, int trainTestRatio) {
        BufferedReader bufReader = null;
        int lineNumber = 0;
        String line;
        PrintWriter pwTrain = null, pwTest = null;
        int trainSize = trainTestRatio + 1;

        try {
            pwTrain = new PrintWriter(new FileWriter("train.tsv", false), true);
            pwTest = new PrintWriter(new FileWriter("test.tsv", false), true);
            bufReader = new BufferedReader(new FileReader(fileName));
            while (true) {
                line = bufReader.readLine();
                if (lineNumber % 10000 == 0) {
                    System.out.println("lineNumber: " + lineNumber + "\t" + Calendar.getInstance().getTime());
                }
                if (line == null) {
                    break;
                }
                if (lineNumber % trainSize == 0) {
                    pwTest.println(line);
                } else {
                    pwTrain.println(line);
                }
                lineNumber++;
            }
            
            pwTrain.close();
            pwTest.close();
            bufReader.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        TenCrossTenSpiltter.SplitSingle(args[0], Integer.parseInt(args[1]));
        //TenCrossTenSpiltter.Split(args[0], Integer.parseInt(args[1]));
        //TenCrossTenSpiltter.ImageSplit(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
}
