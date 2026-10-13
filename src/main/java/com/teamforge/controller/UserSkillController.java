package com.teamforge.controller;

import com.teamforge.entity.UserSkill;
import com.teamforge.service.UserSkillService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-skills")
public class UserSkillController {

    private final UserSkillService userSkillService;

    public UserSkillController(UserSkillService userSkillService) {
        this.userSkillService = userSkillService;
    }

    // ADMIN: ADD OR UPDATE A CANDIDATE SKILL
    @PostMapping
    public ResponseEntity<?> addUserSkill(@RequestBody UserSkill userSkill) {

        try {
            UserSkill savedSkill = userSkillService.saveUserSkill(userSkill);

            return ResponseEntity.ok(savedSkill);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ADMIN: GET SKILLS ASSIGNED TO A CANDIDATE
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getSkillsByUser(@PathVariable Long userId) {

        try {
            List<UserSkill> skills =
                    userSkillService.getSkillsByUserId(userId);

            return ResponseEntity.ok(skills);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ADMIN: VIEW ALL PENDING SKILLS
    @GetMapping("/pending")
    public ResponseEntity<List<UserSkill>> getPendingSkills() {

        return ResponseEntity.ok(
                userSkillService.getPendingSkills()
        );
    }

    // ADMIN: VERIFY A CANDIDATE SKILL
    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifySkill(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request
    ) {

        try {
            Integer approvedProficiency =
                    request.get("approvedProficiency");

            UserSkill verifiedSkill =
                    userSkillService.verifyUserSkill(
                            id,
                            approvedProficiency
                    );

            return ResponseEntity.ok(verifiedSkill);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ADMIN: REVOKE SKILL VERIFICATION
    @PutMapping("/{id}/revoke")
    public ResponseEntity<?> revokeVerification(@PathVariable Long id) {

        try {
            UserSkill updatedSkill =
                    userSkillService.revokeVerification(id);

            return ResponseEntity.ok(updatedSkill);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ADMIN: REMOVE A CANDIDATE SKILL
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserSkill(@PathVariable Long id) {

        try {
            userSkillService.deleteUserSkill(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Candidate skill removed successfully."
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}