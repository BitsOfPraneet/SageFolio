package com.sagefolio.controller;

import com.sagefolio.model.RiskProfile;
import com.sagefolio.service.RiskScoringService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RiskController {

    @Autowired
    private RiskScoringService riskScoringService;

    @GetMapping("/risk-profile")
    public String showRiskForm(HttpSession session, Model model) {
        RiskProfile existing = (RiskProfile) session.getAttribute("riskProfile");
        if (existing != null) {
            model.addAttribute("profile", existing);
        }
        return "risk-profile";
    }

    @PostMapping("/calculate-risk")
    public String calculateRisk(
            @RequestParam("name") String name,
            @RequestParam("horizon") int horizon,
            @RequestParam("income") int income,
            @RequestParam("experience") int experience,
            @RequestParam("tolerance") int tolerance,
            HttpSession session,
            Model model) {

        int score = riskScoringService.calculateScore(horizon, income, experience, tolerance);
        String category = riskScoringService.getCategory(score);

        RiskProfile profile = new RiskProfile();
        profile.setName(name);
        profile.setScore(score);
        profile.setCategory(category);
        profile.setHorizon(horizon);
        profile.setIncome(income);
        profile.setExperience(experience);
        profile.setTolerance(tolerance);

        session.setAttribute("riskProfile", profile);
        model.addAttribute("profile", profile);

        return "redirect:/dashboard";
    }
}
