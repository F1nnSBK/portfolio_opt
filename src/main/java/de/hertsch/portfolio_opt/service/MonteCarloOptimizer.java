package de.hertsch.portfolio_opt.service;

import de.hertsch.portfolio_opt.configuration.FinancialMathCore;
import de.hertsch.portfolio_opt.model.OptimizationResult;
import de.hertsch.portfolio_opt.model.PriceSeries;

public class MonteCarloOptimizer implements PortfolioOptimizer {
    
    private int simulationCount;
    private FinancialMathCore mathCore;

    public OptimizationResult findOptimalWeights(PriceSeries data) {
        return null;
    }
}
