package com.sagefolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sagefolio.model.Centroid;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class ClusterAssignmentService {

    private List<Centroid> centroids;

    @PostConstruct
    public void loadCentroids() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/static/data/centroids.json");
        if (is == null) throw new RuntimeException("centroids.json not found at /static/data/centroids.json");
        centroids = mapper.readValue(is, new TypeReference<List<Centroid>>() {});
        System.out.println("✅ Loaded " + centroids.size() + " K-Means centroids");
    }

    public String assignCluster(double stddev, double atr, double drawdown) {
        // Guard: if drawdown is 0 (insufficient data), warn and use stddev only for assignment
        if (drawdown == 0.0) {
            System.out.println("⚠️ Drawdown is 0 — possibly insufficient price history. Cluster may be less accurate.");
        }
        // Normalization ranges calibrated to BSE Indian stocks (rupee-denominated ATR)
        // stddev:   daily return % — typical range 0.3% to 4%
        // atr:      rupee ATR — typical range ₹1 to ₹300 across BSE stocks
        // drawdown: 30-day max drawdown % — typical range 0.5% to 25%
        double stddevN   = normalize(stddev,   0.3,  4.0);
        double atrN      = normalize(atr,      1.0,  300.0);
        double drawdownN = normalize(drawdown, 0.5,  25.0);

        double minDist = Double.MAX_VALUE;
        String label = "Medium"; // safe default

        for (Centroid c : centroids) {
            double dist = Math.sqrt(
                Math.pow(stddevN   - c.getStddevN(),   2) +
                Math.pow(atrN      - c.getAtrN(),      2) +
                Math.pow(drawdownN - c.getDrawdownN(), 2)
            );
            if (dist < minDist) {
                minDist = dist;
                label = c.getVolatilityLabel();
            }
        }

        // Log for debugging during demo
        System.out.printf("Cluster assignment: stddev=%.4f(n=%.4f) atr=%.4f(n=%.4f) drawdown=%.4f(n=%.4f) → %s%n",
            stddev, stddevN, atr, atrN, drawdown, drawdownN, label);

        return label;
    }

    private double normalize(double val, double min, double max) {
        return Math.max(0, Math.min(1, (val - min) / (max - min)));
    }
}
