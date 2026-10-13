package com.teamforge.controller;

import com.teamforge.matching.CandidateMatchResult;
import com.teamforge.matching.ProjectMatchingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final ProjectMatchingService projectMatchingService;

    public MatchingController(ProjectMatchingService projectMatchingService) {
        this.projectMatchingService = projectMatchingService;
    }

    @GetMapping("/project/{projectId}")
    public List<CandidateMatchResult> getCandidateMatches(
            @PathVariable Long projectId) {

        return projectMatchingService.getCandidateMatches(projectId);
    }
}