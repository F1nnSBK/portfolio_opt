package de.hertsch.portfolio_opt.controller;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hertsch.portfolio_opt.model.OptimizationRequest;
import de.hertsch.portfolio_opt.model.OptimizationResult;
import de.hertsch.portfolio_opt.model.PortfolioResponse;
import de.hertsch.portfolio_opt.service.PortfolioService;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/optimize")
    public PortfolioResponse optimizePortfolio(@RequestBody OptimizationRequest request) {
        long start = System.currentTimeMillis();

        OptimizationResult result = portfolioService.performOptimization(request.tickers());

        long duration = System.currentTimeMillis() - start;

        Map<String, Double> weightMap = IntStream.range(0, request.tickers().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> request.tickers().get(i),
                        i -> Math.round(result.weights()[i] * 10000.0) / 100.0));

        return new PortfolioResponse(
                weightMap,
                result.sharpeRatio(),
                result.returnPa(),
                result.volatility(),
                duration + "ms");
    }

}
