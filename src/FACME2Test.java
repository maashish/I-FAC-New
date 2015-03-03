
import java.util.ArrayList;


public class FACME2Test {

    public static void main(String[] args) {
        int index, correctCount = 0, totalCount = 0;
        FACME2 facme2 = null;
        ArrayList accuracyAL = null;
        
        for (index = 7; index < 10; index++) {
            facme2 = new FACME2();
            
            System.out.println("START of index: " + index);
            facme2.setIsIsolatedInitialize(true);
            facme2.initialize(index);
            facme2.setIsReadFromIsolatedInitializeFile(true);
            //facme2.setIsBigDecimal(true);
            facme2.trainFACME2(index);
            System.out.println();
            System.out.println("TRAINING END");
            System.out.println();
            
            /*accuracyAL = facme2.testFACME2(index);
            correctCount += (Integer) accuracyAL.get(0);
            totalCount += (Integer) accuracyAL.get(1);*/
            
            //facme2.generateRecordsFromAllSingletonsFileSystem(index);
        }
        
        //System.out.println("Final correctCount, totalCount, accuracy: " +  correctCount + "..." + totalCount + "..." + ((double) correctCount/(double) totalCount));

    }

}
