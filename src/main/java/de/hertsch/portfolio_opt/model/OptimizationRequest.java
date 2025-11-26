package de.hertsch.portfolio_opt.model;

import java.util.List;

public record OptimizationRequest(
    List<String> tickers,
    double riskFreeRate,
    int horizonDays
) {}
