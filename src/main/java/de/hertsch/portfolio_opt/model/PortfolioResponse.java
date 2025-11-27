package de.hertsch.portfolio_opt.model;

import java.util.Map;

public record PortfolioResponse(
        Map<String, Double> optimalWeights,
        double sharpeRatio,
        double expectedReturn,
        double volatility,
        String calculationTime) {
}
