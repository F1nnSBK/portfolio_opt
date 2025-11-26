package de.hertsch.portfolio_opt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.hertsch.portfolio_opt.configuration.TiingoFinanceAdapter;
import de.hertsch.portfolio_opt.model.PriceSeries;
import tools.jackson.databind.ObjectMapper;

public class TiingoAdapterTest {

    private TiingoFinanceAdapter adapter;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adapter = new TiingoFinanceAdapter(
                mockHttpClient,
                objectMapper,
                "test-key",
                "http://fake-url",
                LocalDate.of(2019, 1, 1));
    }

    @Test
    @DisplayName("Should parse JSON and order chronologically")
    void shouldParseAndSortJsonCorrectly() throws IOException, InterruptedException {

        String json = Files.readString(Path.of("src/test/resources/tiingo-mock.json"));

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);
        when(mockHttpClient.<String>send(any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);

        PriceSeries result = adapter.fetchHistory("AAPL");

        assertNotNull(result);
        assertEquals("AAPL", result.ticker());
        assertEquals(3, result.closingPrices().length);

        double[] prices = result.closingPrices();
        System.out.println(Arrays.toString(result.closingPrices()));

        assertEquals(145.0, prices[0], 0.001, "First price most be price of 25th");
        assertEquals(152.5, prices[2], 0.001, "First price most be price of 27th");

        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    @DisplayName("Should throw API Error")
    void shouldThrowExceptionOn4040() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("Not Found");
        when(mockHttpClient.<String>send(any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            adapter.fetchHistory("INVALID");
        });

        assertTrue(exception.getMessage().contains("Tiingo Error 404"));
    }

    @Test
    @Tag("integration")
    void liveIntegrationTest() {

        HttpClient realClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ObjectMapper realMapper = new ObjectMapper();

        TiingoFinanceAdapter liveAdapter = new TiingoFinanceAdapter(
                realClient,
                realMapper,
                "#",
                "https://api.tiingo.com/tiingo/daily",
                LocalDate.of(2023, 1, 1));

        PriceSeries series = liveAdapter.fetchHistory("NVDA");

        System.out.println("Ticker: " + series.ticker());
        System.out.println("Datenpunkte: " + series.closingPrices().length);
        System.out.println("Letzter Preis: " + series.closingPrices()[series.closingPrices().length - 1]);

        assertTrue(series.closingPrices().length > 0);
    }
}
