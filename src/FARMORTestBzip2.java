
public class FARMORTestBzip2 {

    public static void main(String[] args) {
        int index;
        FARMORBzip2ByteArraySubPartitionZipLocal facme = null;

        for(index = 0; index < 10; index++) {
            facme = new FARMORBzip2ByteArraySubPartitionZipLocal();
            facme.generateFuzzyTestDataFileSystem(index);
            //facme.generateFuzzyTrainingDataFileSystem(index);
            //facme.generateAssociationRulesLocal(index);
            //facme.generateAssociationRulesSecondPhaseLocal(index);
            //facme.removeDuplicateItemsets(index);
        } 

        /*for(index = 100; index < 101; index++) {
            facme = new FARMORBzip2ByteArraySubPartitionZipLocal();
            //facme.generateFuzzyTestDataFileSystem(index);
            //facme.generateFuzzyTrainingDataFileSystem(index);
            facme.generateAssociationRulesLocal(index);
            facme.generateAssociationRulesSecondPhaseLocal(index);
            //facme.removeDuplicateItemsets(index);
        }

        facme1 = new FARMORBzip2ByteArraySubPartitionZip();
        facme1.generateCARs(100);*/
    }
}
