
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CollateSecondPhaseClassesRapLeaf {
    
    public void collateClasses(int fileSerialNumber, String parametersFile, float confidence) {
        try {
            BufferedReader trainingFileBufferedReader = null, CARFile = null;
            ArrayList secondPhaseClases = new ArrayList();
            String line = null, attribute = "GLOBAL", attributeNew = null;
            PrintWriter pw = null;
            int index;
            FARMORBzip2ByteArraySubPartitionZipSparseRapLeaf farmor = null;
            trainingFileBufferedReader = new BufferedReader(new FileReader("classifierParametersSecondPhase" + fileSerialNumber + ".txt"));
            trainingFileBufferedReader.readLine();
            trainingFileBufferedReader.readLine();
            trainingFileBufferedReader.readLine();
            while(true) {
                line = trainingFileBufferedReader.readLine();
                if(line == null || line.equals("END")) {
                    break;
                }
                index = line.indexOf(',');
                secondPhaseClases.add(line.substring(0, index));
            }
            trainingFileBufferedReader.close();

            for(index = 0; index < secondPhaseClases.size(); index++) {
                farmor = new FARMORBzip2ByteArraySubPartitionZipSparseRapLeaf();
                attribute = (String) secondPhaseClases.get(index);
                farmor.generateAssociationRules(fileSerialNumber, attribute, parametersFile, true);
                farmor.generateAssociationRulesSecondPhase(fileSerialNumber, attribute);
                farmor.generateLocalCARs(fileSerialNumber, attribute, confidence);
            }
            attributeNew = "LOCAL";

            pw = new PrintWriter(new FileWriter("CAR_" + fileSerialNumber + "_" + attributeNew + ".txt"));

            for(index = 0; index < secondPhaseClases.size(); index++) {
                attribute = (String) secondPhaseClases.get(index);
                CARFile = new BufferedReader(new FileReader("CAR_" + fileSerialNumber + "_" + attribute + ".txt"));
                while(true) {
                    line = CARFile.readLine();
                    if(line == null) {
                        break;
                    }
                    pw.println(line);
                }
                CARFile.close();
            }
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
