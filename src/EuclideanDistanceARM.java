
public class EuclideanDistanceARM {
    public static float distance(float xArray[], float yArray[]) {
        int dimensions = xArray.length;
        float sum = 0;
        float x, y;
        for (int featureIndex = 0; featureIndex < dimensions; featureIndex++) {
            x = xArray[featureIndex];
            y = yArray[featureIndex];
            sum += Math.pow((x - y), 2);
        }
        return ((float) Math.sqrt(sum));
    }
}
