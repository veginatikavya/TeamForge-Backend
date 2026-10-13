package com.teamforge.controller;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.service.ProjectSkillRequirementService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-requirements")
public class ProjectSkillRequirementController {

    private final ProjectSkillRequirementService requirementService;

    public ProjectSkillRequirementController(
            ProjectSkillRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    // CREATE PROJECT SKILL REQUIREMENT
    @PostMapping
    public ResponseEntity<?> addRequirement(
            @RequestBody ProjectSkillRequirement requirement) {

        try {
            ProjectSkillRequirement savedRequirement =
                    requirementService.saveRequirement(requirement);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedRequirement);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // GET ALL REQUIREMENTS FOR A PROJECT
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectRequirements(
            @PathVariable Long projectId) {

        try {
            List<ProjectSkillRequirement> requirements =
                    requirementService.getRequirementsByProject(projectId);

            return ResponseEntity.ok(requirements);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // GET ONE REQUIREMENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequirementById(@PathVariable Long id) {

        try {
            ProjectSkillRequirement requirement =
                    requirementService.getRequirementById(id);

            return ResponseEntity.ok(requirement);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // UPDATE AN EXISTING REQUIREMENT
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequirement(
            @PathVariable Long id,
            @RequestBody ProjectSkillRequirement updatedRequirement) {

        try {
            ProjectSkillRequirement savedRequirement =
                    requirementService.updateRequirement(
                            id,
                            updatedRequirement
                    );

            return ResponseEntity.ok(savedRequirement);

        } catch (IllegalArgumentException e) {

            HttpStatus status = e.getMessage().startsWith("Requirement not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity
                    .status(status)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE A REQUIREMENT
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequirement(@PathVariable Long id) {

        try {
            requirementService.deleteRequirement(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Project skill requirement deleted successfully."
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}