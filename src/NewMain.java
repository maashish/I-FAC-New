
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.StringTokenizer;

public class NewMain {

    public static void main(String[] args) {
        //for(int i=1; i<=63756; i++)
        //System.out.println("x"+i);
        try {
            BufferedReader bufReader = new BufferedReader(new FileReader(args[0]));
            PrintWriter pw = new PrintWriter(new FileWriter("final.txt", false), true);
            String line = null, token;
            StringTokenizer st = null, st1 = null;
            int index = 0, randomNumber;
            Random random = null, random1;
            float fuzzyMembership;

            while (true) {
                line = bufReader.readLine();
                if(line == null) {
                    break;
                }
                random = new Random();
                random1 = new Random();
                st = new StringTokenizer(line, ",");
                for(index = 0; index < 100; index++) {
                    token = st.nextToken();
                    st1 = new StringTokenizer(token, "^");
                    pw.print(st1.nextToken() + "^");
                    randomNumber = random.nextInt(5);
                    while(true) {
                        if(randomNumber == 0) {
                            randomNumber = random.nextInt(5);
                        }
                        else {
                            break;
                        }
                    }
                    fuzzyMembership = Float.parseFloat(st1.nextToken());
                    
                    if (random1.nextBoolean()) {
                        if (fuzzyMembership > 1) {
                            System.out.println("+ " + fuzzyMembership);
                            fuzzyMembership = 1;
                        }
                        else {
                            fuzzyMembership *= randomNumber;
                        }
                    } else {
                        if (fuzzyMembership < 0) {
                            System.out.println(" " + fuzzyMembership);
                            fuzzyMembership = 0;
                        }
                        else {
                            fuzzyMembership = fuzzyMembership / randomNumber;
                        }
                    }
                    pw.print(fuzzyMembership + ",");
                }
                pw.println(st.nextToken());
            }
            pw.close();
            bufReader.close();
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        }

    }
}
