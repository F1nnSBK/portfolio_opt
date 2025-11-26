package de.hertsch.portfolio_opt.service;

import java.util.List;

import de.hertsch.portfolio_opt.model.OptimizationResult;

public class OptimizationService {

    private MarketDataProvider marketDataProvider;
    private PortfolioOptimizer pOptimizer;

    public OptimizationResult performOptimization(List<String> tickers) {

        OptimizationResult res = null;
        return res;
    }
}
