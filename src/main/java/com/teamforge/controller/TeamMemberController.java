package com.teamforge.controller;

import com.teamforge.entity.TeamMember;
import com.teamforge.service.TeamMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-members")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    public TeamMemberController(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
    }

    @PostMapping
    public TeamMember addTeamMember(@RequestBody TeamMember teamMember) {
        return teamMemberService.addTeamMember(teamMember);
    }

    @GetMapping("/project/{projectId}")
    public List<TeamMember> getTeamMembers(@PathVariable Long projectId) {
        return teamMemberService.getTeamMembers(projectId);
    }

    @DeleteMapping("/{id}")
    public void removeTeamMember(@PathVariable Long id) {
        teamMemberService.removeTeamMember(id);
    }
}