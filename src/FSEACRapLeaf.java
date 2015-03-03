
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FSEACRapLeaf {

    public static void main(String[] args) {
        ClassifierRapLeaf classifier = null;
        int index;
        float confidence = (float) 0.6;
        float accuracy = 0;

        if (args[0].equalsIgnoreCase("train")) {
            CollateSecondPhaseClassesRapLeaf collateSecondPhaseClasses = null;
            FARMORBzip2ByteArraySubPartitionZipSparseRapLeaf farmor = null;
            for (index = 0; index < 1; index++) {
                farmor = new FARMORBzip2ByteArraySubPartitionZipSparseRapLeaf();
                farmor.generateAssociationRules(index, "GLOBAL", "ARMParameters.txt", false);
                farmor.generateAssociationRulesSecondPhase(index, "GLOBAL");
                farmor.generateCARs(index, "GLOBAL", confidence);
            }

            System.out.println("Reached ARM GLOBAL");

            for (index = 0; index < 1; index++) {
                classifier = new ClassifierRapLeaf();
                classifier.fuzzyPreProcess(index, "GLOBAL");
                classifier.fuzzyPhase1(index, "GLOBAL", false, false);
            }

            System.out.println("Reached CLASSIFIER GLOBAL");

            for (index = 0; index < 1; index++) {
                collateSecondPhaseClasses = new CollateSecondPhaseClassesRapLeaf();
                collateSecondPhaseClasses.collateClasses(index, "ARMParametersLocal.txt", confidence);
            }

            System.out.println("Reached ARM LOCAL");

            for (index = 0; index < 1; index++) {
                classifier = new ClassifierRapLeaf();
                classifier.fuzzyPreProcessPhase2(index, "LOCAL");
                classifier.fuzzyPhase2(index, "LOCAL", false, false);
            }

            System.out.println("Reached CLASSIFIER LOCAL");
        } else if (args[0].equalsIgnoreCase("test")) {

            for (index = 0; index < 1; index++) {
                classifier = new ClassifierRapLeaf();
                accuracy += classifier.fuzzyTestBestKWithTopNLengthRules(index, true);
            }
            System.out.println("Accuracy: " + (accuracy / (float) index));
            System.out.println("Reached END");
            try {
                PrintWriter pw = new PrintWriter(new FileWriter("accuracyNumber.txt", false), true);
                pw.println((accuracy / (float) index));
                pw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
