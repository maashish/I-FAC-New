
public class FAprioriTest {

    public static void main(String[] args) {
        FApriori fapriori = new FApriori();
        long startTime, endTime, cumulativeTime = 0;
        float confidence = (float) 0;
        //fapriori.ReadFiles();
        //fapriori.generateFuzzyTrainingData();
        startTime = System.currentTimeMillis();
        fapriori.generateAssociationRules();
        endTime = System.currentTimeMillis();
        cumulativeTime += (endTime - startTime);
        System.out.println("TOTAL TIME: " + cumulativeTime);
        fapriori.generateCARs("GLOBAL", confidence);
    }
}
