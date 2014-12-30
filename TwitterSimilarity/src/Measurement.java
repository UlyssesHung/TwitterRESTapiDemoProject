public class Measurement {
	public static double similarity(double[][] features, String Type, double[] para) {
		if (features[0].length != features[1].length)
			System.out.print("features's length inconsistent");
		double[] zeroarray = new double[features[0].length];
		double result = 0;
		switch (Type) {
		case "RBF":
			result = Math.exp(-EuclideanDistance(features[0], features[1]) / (2 * Math.pow(para[0], 2)));
			break;
		case "Traditional":
			result = CountSame(features[0], features[1], 0.05) / features[0].length;
			break;
		case "Consine":
			double sum = innerproduct(features[0], features[1]);
			double vectorlength = EuclideanDistance(zeroarray, features[0]) * EuclideanDistance(zeroarray, features[1]);
			result = vectorlength == 0 ? 0 : sum / vectorlength;
			break;
		default:
			result = Math.exp(-EuclideanDistance(features[0], features[1]));
			break;
		}
		return result;
	}

	public static double EuclideanDistance(double[] array1, double[] array2) {
		double Sum = 0.0;
		for (int i = 0; i < array1.length; i++) {
			Sum = Sum + Math.pow((array1[i] - array2[i]), 2.0);
		}
		return Math.sqrt(Sum);
	}

	public static double[][] ScaletoUniVector(double[][] features) {
		double[][] featurevectors = new double[2][];
		for (int i = 0; i < features.length; i++) {
			double powersum = 0, sqrtpowersum = 0;
			for (double featurevalue : features[i]) {
				powersum += Math.pow(featurevalue, 2.0);
			}
			sqrtpowersum = Math.sqrt(powersum);
			featurevectors[i] = new double[features[i].length];
			for (int j = 0; j < features[i].length; j++) {
				featurevectors[i][j] = powersum == 0 ? features[i][j] : features[i][j] / sqrtpowersum;
			}
		}
		return featurevectors;
	}

	public static double CountSame(double[] array1, double[] array2, double threshold) {
		double Sum = 0.0;
		for (int i = 0; i < array1.length; i++) {
			if (Math.abs(array1[i] - array2[i]) < threshold)
				Sum++;
		}
		return Sum;
	}

	public static double innerproduct(double[] array1, double[] array2) {
		double sum = 0;
		for (int i = 0; i < array1.length; i++) {
			sum += array1[i] * array2[i];
		}
		return sum;
	}
}
