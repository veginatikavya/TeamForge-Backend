package com.teamforge.controller;

import com.teamforge.entity.Account;
import com.teamforge.entity.Role;
import com.teamforge.entity.Skill;
import com.teamforge.entity.User;
import com.teamforge.entity.UserSkill;

import com.teamforge.repository.AccountRepository;
import com.teamforge.service.SkillService;
import com.teamforge.service.UserSkillService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final AccountRepository accountRepository;
    private final UserSkillService userSkillService;
    private final SkillService skillService;

    public CandidateController(
            AccountRepository accountRepository,
            UserSkillService userSkillService,
            SkillService skillService
    ) {
        this.accountRepository = accountRepository;
        this.userSkillService = userSkillService;
        this.skillService = skillService;
    }

    // IDENTIFY THE LOGGED-IN CANDIDATE FROM THEIR JWT
    private User getLoggedInCandidate(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException(
                    "Authentication is required."
            );
        }

        Account account = accountRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found."
                        )
                );

        if (account.getRole() != Role.CANDIDATE
                || account.getCandidate() == null) {
            throw new IllegalArgumentException(
                    "No candidate profile is linked to this account."
            );
        }

        return account.getCandidate();
    }

    // CANDIDATE: VIEW OWN PROFILE
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication
    ) {

        try {
            User candidate = getLoggedInCandidate(authentication);

            return ResponseEntity.ok(
                    Map.of(
                            "id", candidate.getId(),
                            "name", candidate.getName(),
                            "email", candidate.getEmail(),
                            "preferredRole",
                            candidate.getPreferredRole() == null
                                    ? "" : candidate.getPreferredRole(),
                            "experienceYears", candidate.getExperienceYears(),
                            "available", candidate.getAvailable()
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // CANDIDATE: VIEW AVAILABLE SKILLS
    @GetMapping("/available-skills")
    public ResponseEntity<?> getAvailableSkills(
            Authentication authentication
    ) {

        try {
            getLoggedInCandidate(authentication);

            List<Skill> skills = skillService.getAllSkills();

            return ResponseEntity.ok(skills);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // CANDIDATE: VIEW OWN SKILLS
    @GetMapping("/skills")
    public ResponseEntity<?> getMySkills(
            Authentication authentication
    ) {

        try {
            User candidate = getLoggedInCandidate(authentication);

            List<UserSkill> skills =
                    userSkillService.getSkillsByUserId(
                            candidate.getId()
                    );

            return ResponseEntity.ok(skills);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // CANDIDATE: ADD OR UPDATE OWN SKILL
    @PostMapping("/skills")
    public ResponseEntity<?> submitMySkill(
            Authentication authentication,
            @RequestBody Map<String, Integer> request
    ) {

        try {
            User candidate = getLoggedInCandidate(authentication);

            Integer skillId = request.get("skillId");
            Integer proficiency = request.get("proficiency");

            if (skillId == null || proficiency == null) {
                throw new IllegalArgumentException(
                        "Skill and proficiency are required."
                );
            }

            UserSkill userSkill = new UserSkill();

            userSkill.setUser(candidate);

            Skill skill = new Skill();
            skill.setId(skillId.longValue());

            userSkill.setSkill(skill);
            userSkill.setProficiency(proficiency);

            UserSkill savedSkill =
                    userSkillService.saveUserSkill(userSkill);

            return ResponseEntity.ok(savedSkill);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}