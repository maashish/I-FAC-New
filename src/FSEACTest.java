
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FSEACTest {

    public static void main(String[] args) {
        ClassifierHack classifier = null;
        int index, minCARsInEachClassLocal;
        float confidence = (float) 0.6;
        float supportReductionFactor;
        float accuracy, accuracySum = 0;

        if (args[0].equalsIgnoreCase("train")) {
            System.err.println("STDERR");
            System.out.println("args: " + args[1] + "..." + args[2]);
            supportReductionFactor = Float.parseFloat(args[1]);
            minCARsInEachClassLocal = Integer.parseInt(args[2]);
            CollateSecondPhaseClasses collateSecondPhaseClasses = null;
            FARMORBzip2ByteArraySubPartitionZipSparse farmor = null;
            for (index = 0; index < 1; index++) {
                farmor = new FARMORBzip2ByteArraySubPartitionZipSparse();
                farmor.generateAssociationRules(index, "GLOBAL", "ARMParameters.txt", 0, false);
                farmor.generateAssociationRulesSecondPhase(index, "GLOBAL");
                farmor.generateCARs(index, "GLOBAL", confidence);
            }

            System.out.println("Reached ARM GLOBAL");

            for (index = 0; index < 1; index++) {
                classifier = new ClassifierHack();
                //classifier.fuzzyPreProcess(index, "GLOBAL");
                classifier.fuzzyPhase1(index, "GLOBAL", true, true);
            }

            System.out.println("Reached CLASSIFIER GLOBAL");

            for (index = 0; index < 1; index++) {
                collateSecondPhaseClasses = new CollateSecondPhaseClasses();
                collateSecondPhaseClasses.collateClasses(index, "ARMParametersLocal.txt", confidence, supportReductionFactor, minCARsInEachClassLocal, true);
            }

            System.out.println("Reached ARM LOCAL");

            for (index = 0; index < 1; index++) {
                classifier = new ClassifierHack();
                classifier.fuzzyPreProcessPhase2(index, "LOCAL");
                classifier.fuzzyPhase2(index, "LOCAL", false, true);
            }
            
            System.out.println("Reached CLASSIFIER LOCAL");
        } else if (args[0].equalsIgnoreCase("test")) {
            PrintWriter pw = null;

            try {
                 pw = new PrintWriter(new FileWriter("accuracyNumber.txt", false), true);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            for (index = 0; index < 1; index++) {
                classifier = new ClassifierHack();
                int topK = Integer.parseInt(args[1]);
                accuracy = classifier.fuzzyTestHack(index, topK, Boolean.parseBoolean(args[2]));
                accuracySum += accuracy;
                pw.println(index + "\t" + accuracy);
            }
            System.out.println("Overall\t" + (accuracySum / (float) index));
            System.out.println("Reached END");

            pw.println("Overall\t" + (accuracySum / (float) index));
            pw.close();
        }
    }

    /*public static void main(String[] args) {
        //FARMORBzip2ByteArraySubPartitionZipSparse farmor = null;
        FARMORBzip2ByteArraySubPartitionZip farmor = null;
        ClassifierImages classifier = null;
        CollateSecondPhaseClasses collateSecondPhaseClasses = null;
        int index;
        float confidence = (float) 0;
        float accuracy = 0;
        long startTime, endTime, cumulativeTime = 0;

        for (index = 0; index < 1; index++) {
            farmor = new FARMORBzip2ByteArraySubPartitionZip();
            startTime = System.currentTimeMillis();
            farmor.generateAssociationRules(index, "GLOBAL", "ARMParameters.txt");
            endTime = System.currentTimeMillis();
            cumulativeTime += (endTime - startTime);
            System.out.println("FIRST PHASE TIME: " + (endTime - startTime));
            startTime = System.currentTimeMillis();
            farmor.generateAssociationRulesSecondPhase(index, "GLOBAL");
            endTime = System.currentTimeMillis();
            cumulativeTime += (endTime - startTime);
            System.out.println("SECOND PHASE TIME: " + (endTime - startTime));
            System.out.println("TOTAL TIME: " + cumulativeTime);
            farmor.generateCARs(index, "GLOBAL", confidence);
        }

        System.out.println("Reached ARM GLOBAL");

        System.out.println("Reached END");

    }*/
}
