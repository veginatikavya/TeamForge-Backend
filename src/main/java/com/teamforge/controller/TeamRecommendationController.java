package com.teamforge.controller;

import com.teamforge.matching.TeamContributionResult;
import com.teamforge.matching.TeamContributionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-recommendations")
public class TeamRecommendationController {

    private final TeamContributionService teamContributionService;

    public TeamRecommendationController(
            TeamContributionService teamContributionService) {

        this.teamContributionService = teamContributionService;
    }

    @GetMapping("/project/{projectId}")
    public List<TeamContributionResult> getRecommendations(
            @PathVariable Long projectId) {

        return teamContributionService.getRecommendations(projectId);
    }
}