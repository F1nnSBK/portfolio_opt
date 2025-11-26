package de.hertsch.portfolio_opt.model;

public record PriceSeries(
    String ticker, double[] closingPrices
) {}
