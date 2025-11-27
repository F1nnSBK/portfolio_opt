package de.hertsch.portfolio_opt.configuration;

import de.hertsch.portfolio_opt.model.PriceSeries;

public interface MarketDataProvider {
    public PriceSeries fetchHistory(String ticker);
}
