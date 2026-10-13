package com.teamforge.controller;

import com.teamforge.matching.TeamContributionResult;
import com.teamforge.matching.TeamReplacementService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/team-replacement")
public class TeamReplacementController {

    private final TeamReplacementService teamReplacementService;

    public TeamReplacementController(
            TeamReplacementService teamReplacementService) {

        this.teamReplacementService = teamReplacementService;
    }

    @GetMapping("/project/{projectId}/member/{memberId}")
    public List<TeamContributionResult> getReplacements(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {

        return teamReplacementService.recommendReplacements(
                projectId,
                memberId
        );
    }
}