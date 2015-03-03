
public class CosineSimilarityARM {

    public static float similarity(float x[], float y[]) {
        int dimensions = x.length;
        // Dot/Inner product
        float numerator = 0;
        // Lengths
        float sum1 = 0, sum2 = 0;
        float v1, v2;
        // Across all dimensions...
        for (int featureIndex = 0; featureIndex < dimensions; featureIndex++) {
            v1 = x[featureIndex];
            v2 = y[featureIndex];
            numerator += (v1 * v2);
            sum1 += (v1 * v1);
            sum2 += (v2 * v2);
        }
        float denominator = (float) Math.sqrt(sum1 * sum2);
        return (denominator != 0) ? (numerator / denominator) : 0;
    }

public static float distance(float x[], float y[]) {
        return (1 - similarity(x, y));
//		return 2 * (1.0 - similarity(x, y));
    }
}
