
public class FPrePTest {
    public static void main(String[] args) {
        FPreP fprep = new FPreP();
        boolean isTest = false;
        if(args[2].equalsIgnoreCase("test")) {
            isTest = true;
        }
        else if(args[2].equalsIgnoreCase("train")) {
            isTest = false;
            fprep.FCMOnDataset(Integer.parseInt(args[0]), Float.parseFloat(args[1]), true);
        }
        fprep.readFiles();
        if(args[3].equalsIgnoreCase("new")) {
            fprep.generateFuzzyTrainingData(0, isTest);
        }
        else if(args[3].equalsIgnoreCase("old")) {
            fprep.generateFuzzyTrainingDataOldPrep(0, isTest);
        }
    }
}
