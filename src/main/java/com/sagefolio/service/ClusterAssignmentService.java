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
        double stddevN   = normalize(stddev,   0.5, 5.0);
        double atrN      = normalize(atr,      5.0, 200.0);
        double drawdownN = normalize(drawdown, 0.5, 20.0);

        double minDist = Double.MAX_VALUE;
        String label = "Medium";

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
        return label;
    }

    private double normalize(double val, double min, double max) {
        return Math.max(0, Math.min(1, (val - min) / (max - min)));
    }
}
