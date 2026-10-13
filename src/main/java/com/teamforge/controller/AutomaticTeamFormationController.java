package com.teamforge.controller;

import com.teamforge.entity.User;
import com.teamforge.matching.AutomaticTeamFormationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/automatic-team")
public class AutomaticTeamFormationController {

    private final AutomaticTeamFormationService automaticTeamFormationService;

    public AutomaticTeamFormationController(
            AutomaticTeamFormationService automaticTeamFormationService) {

        this.automaticTeamFormationService = automaticTeamFormationService;
    }

    @GetMapping("/project/{projectId}")
    public List<User> suggestTeam(@PathVariable Long projectId) {

        return automaticTeamFormationService.suggestTeam(projectId);
    }
}