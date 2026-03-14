package com.sagefolio.controller;

import com.sagefolio.model.GainerStock;
import com.sagefolio.model.RiskProfile;
import com.sagefolio.service.AlphaVantageService;
import com.sagefolio.service.CompatibilityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired AlphaVantageService alphaVantageService;
    @Autowired CompatibilityService compatibilityService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        RiskProfile profile = (RiskProfile) session.getAttribute("riskProfile");

        // No profile yet → redirect to risk form
        if (profile == null) {
            return "redirect:/risk-profile";
        }

        // Fetch top 8 gainers (cached for the day)
        List<GainerStock> gainers = alphaVantageService.fetchTopGainers();

        // Assign volatility + compatibility to each gainer
        // Uses changePercent as volatility proxy (no extra API calls)
        for (GainerStock g : gainers) {
            String vol = g.getChangePercent() > 3.0 ? "High"
                       : g.getChangePercent() > 1.5 ? "Medium" : "Low";
            g.setVolatilityLabel(vol);
            g.setCompatibilityLabel(
                compatibilityService.getLabel(profile.getCategory(), vol)
            );
        }

        // Only show Good Match + Moderate Match stocks
        List<GainerStock> recommended = gainers.stream()
            .filter(g -> !g.getCompatibilityLabel().equals("High Risk Mismatch"))
            .collect(java.util.stream.Collectors.toList());

        // Detect if results are from a weekend (market closed)
        boolean isWeekend = !gainers.isEmpty() && gainers.get(0).isWeekend();
        model.addAttribute("isWeekend", isWeekend);

        model.addAttribute("profile", profile);
        model.addAttribute("recommended", recommended);
        model.addAttribute("tips", getTips(profile.getCategory()));
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    private List<String> getTips(String category) {
        return switch (category) {
            case "Conservative" -> List.of(
                "Start with large-cap stocks — companies like TCS and HDFC have lower price swings.",
                "Consider Nifty 50 index funds before picking individual stocks.",
                "Never invest money you might need within the next 12 months.",
                "A 5–8% annual return is healthy for a conservative portfolio.",
                "Don't put more than 10% of your capital into any single stock."
            );
            case "Moderate" -> List.of(
                "A 60/40 equity-to-debt split is a classic moderate strategy.",
                "Medium volatility stocks can deliver good returns over 2–3 years.",
                "Review your portfolio quarterly — not daily. Daily checking leads to panic.",
                "Avoid stocks with a P/E ratio above 80 unless you understand the business well.",
                "SIPs (Systematic Investment Plans) reduce the risk of bad market timing."
            );
            case "Aggressive" -> List.of(
                "High returns come with high drawdowns — be prepared for temporary -20% dips.",
                "Never invest borrowed money in high-volatility stocks.",
                "Set a stop-loss at 15–20% below your buy price to protect capital.",
                "Research the business, not just the chart, before entering any position.",
                "Even aggressive investors should keep 20% in stable assets as a buffer."
            );
            default -> List.of("Complete your risk profile to get personalised tips.");
        };
    }
    
    @GetMapping("/dev-docs")
    public String devDocs(Model model) {
        model.addAttribute("activePage", "dev-docs");
        return "dev-docs";
    }
}
