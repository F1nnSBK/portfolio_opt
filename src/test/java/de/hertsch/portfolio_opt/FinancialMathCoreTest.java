package de.hertsch.portfolio_opt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.hertsch.portfolio_opt.configuration.FinancialMathCore;

public class FinancialMathCoreTest {

    private final FinancialMathCore mathCore = new FinancialMathCore();

    @Test
    void testLogReturns() {
        double[] prices = { 100.0, 101.0, 99.0 };
        double[] returns = mathCore.calculateLogReturns(prices);

        assertEquals(2, returns.length);
        assertEquals(Math.log(1.01), returns[0], 0.00001);
        assertEquals(Math.log(99.0 / 101.0), returns[1], 0.00001);
    }

    @Test
    void testCovarianceMatrixSIMD() {
        double[] assetA = { 1.0, 2.0, 3.0, 4.0, 5.0 };
        double[] assetB = { 2.0, 4.0, 6.0, 8.0, 10.0 };
        double[] assetC = { 5.0, 4.0, 3.0, 2.0, 1.0 };

        double[][] returnsMatrix = { assetA, assetB, assetC };

        double[][] covMatrix = mathCore.calculateCovMatrix(returnsMatrix);

        assertTrue(covMatrix[0][0] > 0);
        assertEquals(covMatrix[0][1], covMatrix[1][0], 0.00001);
        assertTrue(covMatrix[0][1] > 0);
        assertTrue(covMatrix[0][2] < 0);
    }

}
