package com.sagefolio.controller;

import com.sagefolio.model.RiskProfile;
import com.sagefolio.model.StockAnalysis;
import com.sagefolio.service.ClusterAssignmentService;
import com.sagefolio.service.CompatibilityService;
import com.sagefolio.service.GeminiInsightService;
import com.sagefolio.service.AlphaVantageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockController {

    @Autowired
    private AlphaVantageService alphaVantageService;

    @Autowired
    private ClusterAssignmentService clusterService;

    @Autowired
    private CompatibilityService compatService;

    @Autowired
    private GeminiInsightService geminiService;

    @GetMapping("/stock-analysis")
    public String stockPage(HttpSession session, Model model) {
        RiskProfile profile = (RiskProfile) session.getAttribute("riskProfile");
        model.addAttribute("profile", profile);
        return "stock-dashboard";
    }

    @GetMapping("/stock/analyse")
    public String analyse(@RequestParam("symbol") String symbol,
                          HttpSession session, Model model) {
        RiskProfile profile = (RiskProfile) session.getAttribute("riskProfile");
        model.addAttribute("profile", profile);

        try {
            // 1. Fetch live data from Alpha Vantage
            StockAnalysis stock = alphaVantageService.fetchStock(symbol);

            // 2. Assign cluster via runtime centroid matching
            String volatilityLabel = clusterService.assignCluster(
                stock.getStddev(), stock.getAtr(), stock.getDrawdown()
            );
            stock.setVolatilityLabel(volatilityLabel);

            // 3. Compatibility (from session risk profile)
            String compatibility = "N/A";
            int compatScore = 0;
            if (profile != null) {
                compatibility = compatService.getLabel(profile.getCategory(), volatilityLabel);
                compatScore   = compatService.getScore(profile.getCategory(), volatilityLabel);
            }

            // 4. Generate AI insight via Gemini
            String insight = geminiService.generateInsight(
                symbol, volatilityLabel, stock.getTrend(),
                stock.getYearReturn(), compatibility,
                profile != null ? profile.getCategory() : "Unknown"
            );

            model.addAttribute("stock", stock);
            model.addAttribute("volatilityLabel", volatilityLabel);
            model.addAttribute("compatibility", compatibility);
            model.addAttribute("compatScore", compatScore);
            model.addAttribute("insight", insight);

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "stock-dashboard";
    }
}
