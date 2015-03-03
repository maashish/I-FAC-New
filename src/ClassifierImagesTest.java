
public class ClassifierImagesTest {

    public static void main(String[] args) {
        ClassifierImages classifier = new ClassifierImages();

        if (Integer.parseInt(args[0]) == 1) {
            //classifier.uncertainPhase(Integer.parseInt(args[1]), true);
            classifier.fuzzyPreProcess(0, "GLOBAL");
            classifier.fuzzyPhase1(0, "GLOBAL", false, true);
            classifier.fuzzyPreProcessPhase2(0, "LOCAL");
            classifier.fuzzyPhase2(0, "LOCAL", true, false);
        } else if (Integer.parseInt(args[0]) == 2) {
            classifier.fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRules(Integer.parseInt(args[1]));
            //classifier.fuzzyTestBestKWithTopNLengthRulesNoNegativeClassRulesRanking(Integer.parseInt(args[1]));
        }
    }
}
