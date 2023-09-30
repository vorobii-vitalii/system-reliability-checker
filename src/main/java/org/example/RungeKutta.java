package org.example;

/**
 * @author Vitalii Vorobii
 */
public class RungeKutta {

    public static double[][] solve(double[] x0, double t0, double t1, double h, Function f) {
        double[] t = new double[(int) ((t1 - t0) / h) + 1];
        double[][] x = new double[t.length + 1][x0.length];
        t[0] = t0;
        x[0] = x0;
        for (int i = 1; i < t.length; i++) {
            double[] k1 = times(f.apply(x[i - 1], t[i - 1]), h);
            double[] k2 = times(f.apply(sum(x[i - 1], times(k1, 0.5)), t[i - 1] + h / 2), h);
            double[] k3 = times(f.apply(sum(x[i - 1], times(k2, 0.5)), t[i - 1] + h / 2), h);
            double[] k4 = times(f.apply(sum(x[i - 1], k3), t[i - 1] + h), h);
            x[i] = sum(x[i - 1],
                    times((sum(sum(sum(k1, times(k2, 2)), times(k3, 2)), k4)), 1D / 6));
            t[i] = t[i - 1] + h;
        }
        return x;
    }

    private static double[] sum(double[] arr1, double[] arr2) {
        double[] res = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            res[i] = arr1[i] + arr2[i];
        }
        return res;
    }

    private static double[] times(double[] arr, double scalar) {
        double[] newArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i] * scalar;
        }
        return newArr;
    }

    public interface Function {
        double[] apply(double[] x, double t);
    }
}
