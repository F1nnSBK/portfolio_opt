package de.hertsch.portfolio_opt.model;

public record PortfolioMetrics(
        double[] weights,
        double sharpeRatio,
        double expectedReturn,
        double volatility) {

}
