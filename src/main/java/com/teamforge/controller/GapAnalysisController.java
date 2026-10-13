package com.teamforge.controller;

import com.teamforge.matching.GapAnalysisService;
import com.teamforge.matching.SkillGapResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gaps")
public class GapAnalysisController {

    private final GapAnalysisService gapAnalysisService;

    public GapAnalysisController(GapAnalysisService gapAnalysisService) {
        this.gapAnalysisService = gapAnalysisService;
    }

    @GetMapping("/project/{projectId}")
    public List<SkillGapResult> getProjectGaps(
            @PathVariable Long projectId) {

        return gapAnalysisService.analyzeProjectGaps(projectId);
    }
}