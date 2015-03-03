
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class VectorRandomGenerator {

    public void generateVectorRandom(String fileName, int reductionFactor) {
        long randomNumber;
        BufferedReader bufReader;
        PrintWriter pw;
        String line = null;
        Random randomNumberGenerator = new Random();
        int lineNumber = 0;

        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("vectorIndexMapReduced.txt", false), true);
            while(true) {
                if(lineNumber++ % 10000 == 0) {
                    System.out.println("lineNumber " + lineNumber);
                }
                line = bufReader.readLine();
                if(line == null) {
                    break;
                }
                randomNumber = randomNumberGenerator.nextLong();
                if(randomNumber % ((long) (reductionFactor)) == 0) {
                    pw.println(line);
                }
            }
            bufReader.close();
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new VectorRandomGenerator().generateVectorRandom(args[0], Integer.parseInt(args[1]));
    }

}
