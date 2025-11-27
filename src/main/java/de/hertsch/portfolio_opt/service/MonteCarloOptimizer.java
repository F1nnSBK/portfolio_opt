package de.hertsch.portfolio_opt.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import de.hertsch.portfolio_opt.configuration.FinancialMathCore;
import de.hertsch.portfolio_opt.model.OptimizationResult;
import de.hertsch.portfolio_opt.model.PortfolioMetrics;
import de.hertsch.portfolio_opt.model.PriceSeries;

@Service
public class MonteCarloOptimizer implements PortfolioOptimizer {

    private static final int SIMULATIONS = 250_000;
    private static final double RISK_FREE_RATE = 0.03;

    private final FinancialMathCore mathCore;

    public MonteCarloOptimizer() {
        this.mathCore = new FinancialMathCore();
    }

    public OptimizationResult optimize(List<PriceSeries> allSeries) {

        if (allSeries == null || allSeries.isEmpty()) {
            throw new IllegalArgumentException("Portfolio must contain at least one asset");
        }

        // Sync Time Series
        int minLength = allSeries.stream()
                .mapToInt(s -> s.closingPrices().length)
                .min()
                .orElse(0);

        if (minLength < 10) {
            throw new IllegalArgumentException(
                    "Not enough data overlap to calculate correlation. Min length: " + minLength);
        }

        int assetCount = allSeries.size();
        int timeSteps = minLength;

        double[][] rawPrices = new double[assetCount][timeSteps];
        for (int i = 0; i < assetCount; i++) {
            double[] source = allSeries.get(i).closingPrices();
            int offset = source.length - minLength;
            System.arraycopy(source, offset, rawPrices[i], 0, minLength);
        }

        double[][] returnsMatrix = new double[assetCount][];
        for (int i = 0; i < assetCount; i++) {
            returnsMatrix[i] = mathCore.calculateLogReturns(rawPrices[i]);
        }

        double[] expectedReturns = new double[assetCount];
        for (int i = 0; i < assetCount; i++) {
            expectedReturns[i] = calculateMean(returnsMatrix[i]) * 252;
        }

        double[][] covMatrix = mathCore.calculateCovMatrix(returnsMatrix);
        scaleMatrix(covMatrix, 252.0);

        OptimizationResult bestResult = new OptimizationResult(new double[0], -Double.MAX_VALUE, 0, 0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Callable<PortfolioMetrics>> tasks = new ArrayList<>(SIMULATIONS);
            for (int i = 0; i < SIMULATIONS; i++) {
                tasks.add(new SimulationTask(assetCount, expectedReturns, covMatrix));
            }

            List<Future<PortfolioMetrics>> futures = executor.invokeAll(tasks);
            for (var future : futures) {
                PortfolioMetrics metrics = future.get();

                if (metrics.sharpeRatio() > bestResult.sharpeRatio()) {
                    bestResult = new OptimizationResult(
                            metrics.weights(),
                            metrics.sharpeRatio(),
                            metrics.expectedReturn(),
                            metrics.volatility());
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Optimization simulation failed", e);
        }

        return bestResult;
    }

    // HELPER
    private double calculateMean(double[] data) {
        double sum = 0.0;
        for (double d : data)
            sum += d;
        return sum / data.length;
    }

    private void scaleMatrix(double[][] matrix, double scalar) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] *= scalar;
            }
        }
    }

    /*
     * Single simulation-task.
     */
    private static class SimulationTask implements Callable<PortfolioMetrics> {
        // TODO: Implement 20% Rule
        private final int n;
        private final double[] means;
        private final double[][] cov;

        public SimulationTask(int n, double[] means, double[][] cov) {
            this.n = n;
            this.means = means;
            this.cov = cov;
        }

        @Override
        public PortfolioMetrics call() {
            double[] weights = new double[n];
            double sum = 0.0;
            var random = ThreadLocalRandom.current();

            for (int i = 0; i < n; i++) {
                weights[i] = random.nextDouble();
                sum += weights[i];
            }

            for (int i = 0; i < n; i++)
                weights[i] /= sum;

            double portReturn = 0.0;
            for (int i = 0; i < n; i++) {
                portReturn += weights[i] * means[i];
            }

            double portVar = 0.0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    portVar += weights[i] * weights[j] * cov[i][j];
                }
            }

            double portVol = Math.sqrt(portVar);

            double sharpe = (portVol == 0) ? 0 : (portReturn - RISK_FREE_RATE) / portVol;

            return new PortfolioMetrics(weights, sharpe, portReturn, portVol);
        }
    }

}
