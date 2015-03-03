
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CreateFuzzyDataset {

    public void collateGroundTruth() {
        try {
            File folder = new File(".");
            File[] listOfFiles = folder.listFiles();
            PrintWriter pwGroundTruth = new PrintWriter(new FileWriter("groundTruth.txt", false), true);
            BufferedReader bufReader = null;
            int index;
            String line = null, fileName;

            for (index = 0; index < listOfFiles.length; index++) {
                if (listOfFiles[index].isFile()) {
                    fileName = listOfFiles[index].getName();
                    System.out.println("File " + fileName);
                    bufReader = new BufferedReader(new FileReader(fileName));
                    while(true) {
                        line = bufReader.readLine();
                        if(line == null) {
                            break;
                        }
                        /*if(fileName.contains("good")) {
                            pwGroundTruth.println(line + "," + "good");
                        }
                        else if(fileName.contains("ok")) {
                            pwGroundTruth.println(line + "," + "ok");
                        }
                        else if(fileName.contains("junk")) {
                            pwGroundTruth.println(line + "," + "junk");
                        }*/

                        if(fileName.contains("cars")) {
                            pwGroundTruth.println(line + "," + "cars");
                        }
                        else if(fileName.contains("bg")) {
                            pwGroundTruth.println(line + "," + "bg");
                        }
                    }
                    bufReader.close();
                } else if (listOfFiles[index].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[index].getName());
                }
            }
            pwGroundTruth.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }


    public static void main(String[] args) {
        CreateFuzzyDataset createFuzzyDataset = new CreateFuzzyDataset();
        createFuzzyDataset.collateGroundTruth();
    }

}
