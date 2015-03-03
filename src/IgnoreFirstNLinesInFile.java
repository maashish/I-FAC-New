
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class IgnoreFirstNLinesInFile {

    public void ignoreFirstNLinesInFile(String fileName, int n) {
        BufferedReader bufReader = null;
        PrintWriter pw = null;
        String line = null;
        int index = 0;
        try {
            bufReader = new BufferedReader(new FileReader(fileName));
            pw = new PrintWriter(new FileWriter("dummy.txt", false), true);
            while(true) {
                line = bufReader.readLine();
                if(line == null) {
                    break;
                }
                if(index < n) {
                    index++;
                    continue;
                }
                line = line.trim().replace(' ', ',');
                pw.println(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new IgnoreFirstNLinesInFile().ignoreFirstNLinesInFile(args[0], Integer.parseInt(args[1]));
    }
}
