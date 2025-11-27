package de.hertsch.portfolio_opt.service;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

import org.springframework.stereotype.Service;

import de.hertsch.portfolio_opt.configuration.MarketDataProvider;
import de.hertsch.portfolio_opt.model.OptimizationResult;
import de.hertsch.portfolio_opt.model.PriceSeries;

@Service
public class PortfolioService {

    private final MarketDataProvider marketDataProvider;
    private final PortfolioOptimizer optimizer;

    public PortfolioService(
            MarketDataProvider marketDataProvider,
            PortfolioOptimizer optimizer) {
        this.marketDataProvider = marketDataProvider;
        this.optimizer = optimizer;
    }

    public OptimizationResult performOptimization(List<String> tickers) {

        try (StructuredTaskScope<PriceSeries, Void> scope = StructuredTaskScope.open()) {

            List<StructuredTaskScope.Subtask<PriceSeries>> tasks = tickers.stream()
                    .map(ticker -> scope.fork(() -> marketDataProvider.fetchHistory(ticker)))
                    .toList();

            scope.join();

            List<PriceSeries> allData = tasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();

            return optimizer.optimize(allData);

        } catch (StructuredTaskScope.FailedException e) {
            throw new RuntimeException("Market data fetch failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Portfolio optimization interrupted", e);
        }
    }
}
