public class ClassifierTest {

    public static void main(String[] args) {
        Classifier classifier = new Classifier();
        /*classifier.fuzzyPreProcess();
        classifier.fuzzyPhase1();*/
        /*classifier.fuzzyPreProcessPhase2();
        classifier.fuzzyPhase2();*/
        //classifier.fuzzyTestBestKWithTopNLengthRules(0);

        if(Integer.parseInt(args[0]) == 1) {
            classifier.preProcess();
            classifier.phase1();
        }
        else if(Integer.parseInt(args[0]) == 2) {
            classifier.preProcessPhase2();
            classifier.phase2();
        }
        if(Integer.parseInt(args[0]) == 3) {
            classifier.testBestKWithTopNLengthRules();
        }
    }
}
