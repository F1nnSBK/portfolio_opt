package de.hertsch.portfolio_opt.service;

import java.util.List;

import de.hertsch.portfolio_opt.model.OptimizationResult;
import de.hertsch.portfolio_opt.model.PriceSeries;

public interface PortfolioOptimizer {
    public OptimizationResult optimize(List<PriceSeries> allSeries);
}
