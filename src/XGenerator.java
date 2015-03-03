
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;


public class XGenerator {

    public static void main(String[] args) {
        /*int index, lastIndex = Integer.parseInt(args[0]);

        for(index = 1; index <= lastIndex; index++) {
        System.out.println("x" + index + "\tx" + index + "\t");
        }

        for(index = 1; index <= 64; index++) {
        System.out.print(index + "\t");
        }
        }*/
        try {
            BufferedReader bufReader = new BufferedReader(new FileReader(args[0]));
            //PrintWriter pw = new PrintWriter(new FileWriter("training_100_cars.txt", false), true);
            PrintWriter pw = new PrintWriter(new FileWriter("CAR_0_LOCAL.txt", false), true);
            String line, support;

            StringTokenizer st, st1;

            while(true) {
                line = bufReader.readLine();
                if(line==null) {
                    break;
                }
                st = new StringTokenizer(line, ":");
                pw.print(st.nextToken().trim());
                support = st.nextToken().trim();
                pw.println(" " + support + "~~~~~" + args[1] + " " + support);
            }
            bufReader.close();
            pw.close();



            /*int cutOff = Integer.parseInt(args[1]);
            for (int index = 0; index < cutOff; index++) {
                pw.println(bufReader.readLine());
            }
            pw.close();

            pw = new PrintWriter(new FileWriter("training_100_bg.txt", false), true);
            while (true) {
                line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                pw.println(line);
            }
            pw.close();
            bufReader.close();*/

            /*BufferedReader trainingFileBufferedReader = new BufferedReader(new FileReader(args[0]));
            HashMap secondPhaseClassesBufferedReaders = new HashMap();
            int index, indexOuter;
            String antecedent, token;
            StringTokenizer st, st1;
            for(indexOuter = 0; indexOuter < 20; indexOuter++)
            for (index = (indexOuter * 1000) + 1; index <= (indexOuter + 1) * 1000; index++) {
                antecedent = "CLASS="+index;
                pw = new PrintWriter("fuzzyTrainingData_" + index + "_" + antecedent + ".txt");
                secondPhaseClassesBufferedReaders.put(antecedent, pw);
            }
            while (true) {
                line = trainingFileBufferedReader.readLine();
                if (line == null) {
                    break;
                }
                st = new StringTokenizer(line, ",");

                for (index = 0; st.hasMoreTokens(); index++) {
                    token = st.nextToken();
                    if (token.contains("CLASS")) {
                        st1 = new StringTokenizer(token, "^");
                        antecedent = st1.nextToken();
                        pw = (PrintWriter) secondPhaseClassesBufferedReaders.get(antecedent);
                        pw.println(line);
                        pw.flush();
                    }
                }
            }

            for (index = (indexOuter * 1000) + 1; index <= (indexOuter + 1) * 1000; index++) {
                antecedent = "CLASS="+index;
                pw = (PrintWriter) secondPhaseClassesBufferedReaders.get(antecedent);
                pw.close();

                /*classCount = (Float) classCountMap.get(antecedent);
                pw1.println(antecedent + "," + classCount);
                pw1.flush();*/
            //}

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
