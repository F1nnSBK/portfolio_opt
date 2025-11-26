package de.hertsch.portfolio_opt.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.hertsch.portfolio_opt.model.OptimizationRequest;
import de.hertsch.portfolio_opt.model.OptimizationResult;


@RestController
public class PortfolioController {
    
    @PostMapping("/")
    public OptimizationResult optimizePortfolio(@RequestBody OptimizationRequest req) {
        //TODO: process POST request
        
        OptimizationResult res = null;
        return res;
    }


}
