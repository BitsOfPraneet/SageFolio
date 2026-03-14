package com.sagefolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sagefolio.model.StockAnalysis;
import com.sagefolio.model.GainerStock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@CacheConfig(cacheNames = {"stockCache", "gainersCache"})
public class AlphaVantageService {

    private final RestTemplate restTemplate;
    
    @Value("${alphavantage.api.key}")
    private String apiKey;

    private static final String API_URL =
        "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol={symbol}&outputsize=compact&apikey={apikey}";

    public AlphaVantageService() {
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(value = "stockCache", key = "#a0")
    public StockAnalysis fetchStock(String symbol) {
        // Alpha Vantage supports Indian stocks predominantly via the BSE exchange (e.g., RELIANCE.BSE)
        String avSymbol = symbol.toUpperCase() + ".BSE";

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/122.0.0.0");
            headers.set("Accept", "application/json");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class,
                Map.of("symbol", avSymbol, "apikey", apiKey)
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            if (root.has("Error Message")) {
                 throw new RuntimeException("Invalid symbol: " + symbol + " not found on BSE.");
            }
            if (root.has("Information") || root.has("Note")) {
                 String msg = root.has("Information") ? root.path("Information").asText() : root.path("Note").asText();
                 if (msg.toLowerCase().contains("rate limit") || msg.toLowerCase().contains("call frequency")) {
                     throw new RuntimeException("Alpha Vantage API rate limit exceeded. Please try again later. API permits 25 requests per day.");
                 }
            }

            JsonNode timeSeries = root.path("Time Series (Daily)");
            if (timeSeries.isMissingNode() || timeSeries.isEmpty()) {
                throw new RuntimeException("No price data returned for symbol: " + symbol + ". Check if it is listed on BSE.");
            }

            List<String> dates  = new ArrayList<>();
            List<Double> closes = new ArrayList<>();
            List<Double> highs  = new ArrayList<>();
            List<Double> lows   = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
            int daysCollected = 0;
            
            // Limit to roughly 1 year of trading days (using outputsize=full gives up to 20 years!)
            while (fields.hasNext() && daysCollected < 252) {
                Map.Entry<String, JsonNode> field = fields.next();
                String date = field.getKey();
                JsonNode dayData = field.getValue();
                
                double close = dayData.path("4. close").asDouble();
                double high  = dayData.path("2. high").asDouble();
                double low   = dayData.path("3. low").asDouble();
                
                dates.add(date);
                closes.add(close);
                highs.add(high);
                lows.add(low);
                daysCollected++;
            }

            // Alpha Vantage returns newest -> oldest. Reverse to make chronological for charting and moving averages.
            Collections.reverse(dates);
            Collections.reverse(closes);
            Collections.reverse(highs);
            Collections.reverse(lows);

            if (closes.isEmpty()) {
                throw new RuntimeException("No valid price data for symbol: " + symbol);
            }

            double currentPrice = closes.get(closes.size() - 1);
            double firstPrice   = closes.get(0);
            double yearReturn   = ((currentPrice - firstPrice) / firstPrice) * 100;

            List<Double> ma20 = computeMA(closes, 20);
            double lastMA = 0;
            for (int i = ma20.size() - 1; i >= 0; i--) {
                if (ma20.get(i) != null) { lastMA = ma20.get(i); break; }
            }
            String trend = currentPrice > lastMA ? "Uptrend" : "Downtrend";

            List<Double> dailyReturns = new ArrayList<>();
            for (int i = 1; i < closes.size(); i++) {
                dailyReturns.add(((closes.get(i) - closes.get(i - 1)) / closes.get(i - 1)) * 100);
            }
            double stddev = computeStdDev(dailyReturns);
            double atr    = computeATR(closes, highs, lows, 14);
            double drawdown = computeMaxDrawdown(closes, 30);

            StockAnalysis analysis = new StockAnalysis();
            // Store original requested symbol (e.g. RELIANCE) instead of Alpha Vantage's BSE format
            analysis.setSymbol(symbol.toUpperCase());
            analysis.setCurrentPrice(currentPrice);
            analysis.setYearReturn(yearReturn);
            analysis.setTrend(trend);
            analysis.setStddev(stddev);
            analysis.setAtr(atr);
            analysis.setDrawdown(drawdown);
            analysis.setDates(dates);
            analysis.setClosePrices(closes);
            analysis.setMa20(ma20);
            return analysis;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not fetch Alpha Vantage data for symbol: " + symbol, e);
        }
    }

    private List<Double> computeMA(List<Double> prices, int window) {
        List<Double> ma = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            if (i < window - 1) { ma.add(null); continue; }
            double sum = 0;
            for (int j = i - window + 1; j <= i; j++) sum += prices.get(j);
            ma.add(sum / window);
        }
        return ma;
    }

    private double computeStdDev(List<Double> values) {
        if (values.isEmpty()) return 0;
        double mean = values.stream().mapToDouble(d -> d).average().orElse(0);
        double variance = values.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    private double computeATR(List<Double> closes, List<Double> highs, List<Double> lows, int period) {
        List<Double> tr = new ArrayList<>();
        for (int i = 1; i < closes.size(); i++) {
            double hl  = highs.get(i) - lows.get(i);
            double hpc = Math.abs(highs.get(i) - closes.get(i - 1));
            double lpc = Math.abs(lows.get(i)  - closes.get(i - 1));
            tr.add(Math.max(hl, Math.max(hpc, lpc)));
        }
        if (tr.isEmpty()) return 0;
        return tr.subList(Math.max(0, tr.size() - period), tr.size())
                 .stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double computeMaxDrawdown(List<Double> closes, int window) {
        double maxDrawdown = 0;
        for (int i = window; i < closes.size(); i++) {
            double peak = closes.subList(i - window, i).stream().mapToDouble(d -> d).max().orElse(0);
            if (peak == 0) continue;
            double dd = (closes.get(i) - peak) / peak * 100;
            maxDrawdown = Math.min(maxDrawdown, dd);
        }
        return Math.abs(maxDrawdown);
    }

    @Cacheable(value = "gainersCache")
    public List<GainerStock> fetchTopGainers() {

        // 8 stocks only — respects 25 calls/day free tier limit
        List<String> watchlist = List.of(
            "RELIANCE.BSE", "TCS.BSE", "INFY.BSE", "SBIN.BSE",
            "WIPRO.BSE", "AXISBANK.BSE", "TATAMOTORS.BSE", "HCLTECH.BSE"
        );

        List<GainerStock> results = new ArrayList<>();

        for (String sym : watchlist) {
            try {
                String url = "https://www.alphavantage.co/query"
                    + "?function=GLOBAL_QUOTE"
                    + "&symbol={symbol}"
                    + "&apikey={apiKey}";

                ResponseEntity<String> resp = restTemplate.getForEntity(
                    url, String.class,
                    Map.of("symbol", sym, "apiKey", apiKey)
                );

                JsonNode root   = new ObjectMapper().readTree(resp.getBody());
                JsonNode quote  = root.path("Global Quote");

                if (!quote.isMissingNode() && !quote.isEmpty()) {
                    double changePercent = Double.parseDouble(
                        quote.path("10. change percent")
                             .asText("0%").replace("%", "").trim()
                    );
                    GainerStock g = new GainerStock();
                    g.setSymbol(sym.replace(".BSE", ""));
                    g.setPrice(quote.path("05. price").asDouble());
                    g.setChangePercent(changePercent);
                    results.add(g);
                }

                Thread.sleep(1200); // respect 5 calls/min rate limit

            } catch (Exception e) {
                System.out.println("Skipping " + sym + ": " + e.getMessage());
            }
        }

        // Return only gainers (positive change), sorted best first
        return results.stream()
            .filter(g -> g.getChangePercent() > 0)
            .sorted((a, b) -> Double.compare(b.getChangePercent(), a.getChangePercent()))
            .collect(java.util.stream.Collectors.toList());
    }
}
