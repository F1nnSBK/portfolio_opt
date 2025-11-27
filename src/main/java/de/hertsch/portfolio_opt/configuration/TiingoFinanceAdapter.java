package de.hertsch.portfolio_opt.configuration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.hertsch.portfolio_opt.model.PriceSeries;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class TiingoFinanceAdapter implements MarketDataProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String API_KEY;
    private String BASE_URL;
    private LocalDate START_DATE;

    public TiingoFinanceAdapter(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${portfolio.provider.tiingo.key}") String API_KEY,
            @Value("${portfolio.provider.tiingo.base-url}") String BASE_URL,
            @Value("${portfolio.provider.tiingo.history-start}") LocalDate START_DATE) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.API_KEY = API_KEY;
        this.BASE_URL = BASE_URL;
        this.START_DATE = START_DATE;
    }

    @Override
    public PriceSeries fetchHistory(String ticker) {

        String url = String.format(
                "%s/%s/prices?startDate=%s&resampleFreq=daily&token=%s",
                BASE_URL, ticker, START_DATE, API_KEY);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Tiingo Error " + response.statusCode() + ": " + response.body());
            }

            return parseResponse(ticker, response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fetch failed for " + ticker, e);
        }
    }

    private PriceSeries parseResponse(String ticker, String jsonBody) throws IOException {
        List<TiingoDay> days = objectMapper.readValue(jsonBody, new TypeReference<List<TiingoDay>>() {
        });

        if (days.isEmpty()) {
            throw new RuntimeException("No data found for ticker: " + ticker);
        }

        double[] prices = days.stream()
                .sorted(Comparator.comparing(TiingoDay::date))
                .mapToDouble(TiingoDay::adjClose)
                .toArray();

        return new PriceSeries(ticker, prices);
    }

    record TiingoDay(String date, double adjClose) {
    }
}
