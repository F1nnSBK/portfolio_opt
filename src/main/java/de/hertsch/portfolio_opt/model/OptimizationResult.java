package de.hertsch.portfolio_opt.model;

import java.util.Map;

public record OptimizationResult(
    Map<String, Double> optimalWeights,
    double sharpeRatio,
    double expectedReturn,
    double volatility
) {
    
}
