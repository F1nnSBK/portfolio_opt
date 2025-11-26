package de.hertsch.portfolio_opt.service;

import de.hertsch.portfolio_opt.model.OptimizationResult;
import de.hertsch.portfolio_opt.model.PriceSeries;

public interface PortfolioOptimizer {
    public OptimizationResult findOptimalWeights(PriceSeries data);
}
