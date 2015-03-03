
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CollateSecondPhaseClasses {
    
    public void collateClasses(int fileSerialNumber, String parametersFile, float confidence, float supportReductionFactor, int minCARsInEachClassLocal, boolean isOnlyClass) {
        try {
            BufferedReader trainingFileBufferedReader = null, CARFile = null, bufReader = null;
            File file = null;
            ArrayList secondPhaseClases = new ArrayList();
            String line = null, attribute = "GLOBAL", attributeNew = null;
            PrintWriter pw = null;
            int index, index1, numberOfCARs;
            FARMORBzip2ByteArraySubPartitionZipSparse farmor = null;
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

            for (index = 0; index < secondPhaseClases.size(); index++) {
                attribute = (String) secondPhaseClases.get(index);
                file = new File("CAR_" + fileSerialNumber + "_" + attribute + ".txt");
                if (file.exists()) {
                    System.out.println(file + " deleted: " + file.delete());
                }
                for (index1 = 0; true; index1++) {
                    file = new File("CAR_" + fileSerialNumber + "_" + attribute + ".txt");
                    numberOfCARs = 0;
                    if(file.exists()) {
                        bufReader = new BufferedReader(new FileReader("CAR_" + fileSerialNumber + "_" + attribute + ".txt"));
                        while (true) {
                            line = bufReader.readLine();
                            if (line == null) {
                                break;
                            } else {
                                numberOfCARs++;
                            }
                        }
                    }
                    if (numberOfCARs <= minCARsInEachClassLocal) {
                        farmor = new FARMORBzip2ByteArraySubPartitionZipSparse();
                        farmor.generateAssociationRules(fileSerialNumber, attribute, parametersFile, (((float) index1) * supportReductionFactor), isOnlyClass);
                        farmor.generateAssociationRulesSecondPhase(fileSerialNumber, attribute);
                        farmor.generateLocalCARs(fileSerialNumber, attribute, confidence);
                    } else {
                        break;
                    }
                }
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
