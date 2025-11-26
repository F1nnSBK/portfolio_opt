package de.hertsch.portfolio_opt.configuration;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class FinancialMathCore {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    /**
     * Berechnet die Log-Returns einer Preisreihe.
     * $r_t = ln(\frac{p_t}{p_{t-1}})$
     * Das Array wird um 1 kürzer (n Preise -> $n-1$ Renditen)
     */
    public double[] calculateLogReturns(double[] prices) {
        if (prices.length < 2)
            return new double[0];

        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            returns[i - 1] = Math.log(prices[i] / prices[i - 1]);
        }
        return returns;
    }

    /**
     * Berechnet die Kovarianz-Matrix für n Assets.
     * Nutzt SIMD für den Dot-Product Schritt.
     * 
     * @param returnsMatrix Ein 2D-Array [Asset][Time]. Jede Zeile ist ein Asset.
     * @return Kovarianzmatrix [Asset][Asset]
     */
    public double[][] calculateCovMatrix(double[][] returnsMatrix) {
        int assetCount = returnsMatrix.length;
        int timeSteps = returnsMatrix[0].length;

        // Cov(X, Y) = E[(X - mean(X))(Y - mean(Y))
        double[][] centeredReturns = new double[assetCount][timeSteps];
        for (int i = 0; i < assetCount; i++) {
            double mean = calculateMean(returnsMatrix[i]);
            for (int t = 0; t < timeSteps; t++) {
                centeredReturns[i][t] = returnsMatrix[i][t] - mean;
            }
        }

        double[][] covMatrix = new double[assetCount][assetCount];

        for (int i = 0; i < assetCount; i++) {
            for (int j = i; j < assetCount; j++) {
                double dotProduct = vectorDotProduct(centeredReturns[i], centeredReturns[j]);

                double covariance = dotProduct / (timeSteps - 1);

                covMatrix[i][j] = covariance;
                if (i != j) {
                    covMatrix[j][i] = covariance;
                }
            }
        }

        return covMatrix;
    }

    /**
     * High-Performance Dot Product mittels SIMD.
     * Berechnet sum(a[i] * b[i])
     */
    public double vectorDotProduct(double[] a, double[] b) {
        int length = a.length;
        int i = 0;

        DoubleVector sumVector = DoubleVector.zero(SPECIES);

        for (; i < SPECIES.loopBound(length); i += SPECIES.length()) {
            var va = DoubleVector.fromArray(SPECIES, a, i);
            var vb = DoubleVector.fromArray(SPECIES, b, i);
            sumVector = sumVector.add(va.mul(vb));
        }

        double result = sumVector.reduceLanes(VectorOperators.ADD);

        for (; i < length; i++) {
            result += a[i] * b[i];
        }

        return result;
    }

    private double calculateMean(double[] data) {
        double sum = 0.0;
        for (double d : data)
            sum += d;
        return sum / data.length;
    }
}
