
public class FSEACImagesTest {

    public static void main(String[] args) {
        FARMORBzip2ByteArraySubPartitionZipSparse farmor = null;
        ClassifierImages classifier = null;
        CollateSecondPhaseClasses collateSecondPhaseClasses = null;
        int index;
        float confidence = (float) 0;
        float accuracy = 0;
        long startTime, endTime, cumulativeTime = 0;

        /*for (index = 0; index < 1; index++) {
            farmor = new FARMORBzip2ByteArraySubPartitionZipSparse();
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
            farmor.generateLocalCARs(index, "GLOBAL", confidence);
        }

        System.out.println("Reached ARM GLOBAL");

        for (index = 0; index < 1; index++) {
            classifier = new ClassifierImages();
            classifier.fuzzyPreProcess(index, "GLOBAL");
            classifier.fuzzyPhase1(index, "GLOBAL", false, true);
        }

        System.out.println("Reached CLASSIFIER GLOBAL");

        for (index = 0; index < 1; index++) {
            collateSecondPhaseClasses = new CollateSecondPhaseClasses();
            collateSecondPhaseClasses.collateClasses(index, "ARMParametersLocal.txt", confidence);
        }

        System.out.println("Reached ARM LOCAL"); */

        for (index = 0; index < 1; index++) {
            classifier = new ClassifierImages();
            //classifier.fuzzyPreProcessPhase2(index, "LOCAL");
            classifier.fuzzyPhase2(index, "LOCAL", true, false);
        }

        System.out.println("Reached CLASSIFIER LOCAL");

        /*for (index = 0; index < 1; index++) {
            classifier = new ClassifierImages();
            //accuracy += classifier.fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRules(index);
            accuracy += classifier.fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRulesMultiClassRanking(index);
        }
        System.out.println("OVERALL Accuracy: " + (accuracy / (float) index));*/
        System.out.println("Reached END");

    }
}
