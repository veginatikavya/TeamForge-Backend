package com.teamforge.service;

import com.teamforge.entity.User;
import com.teamforge.entity.Skill;
import com.teamforge.entity.UserSkill;

import com.teamforge.repository.UserRepository;
import com.teamforge.repository.SkillRepository;
import com.teamforge.repository.UserSkillRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public UserSkillService(
            UserSkillRepository userSkillRepository,
            UserRepository userRepository,
            SkillRepository skillRepository
    ) {
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    // ADD OR UPDATE A CANDIDATE SKILL
    @Transactional
    public UserSkill saveUserSkill(UserSkill userSkill) {

        if (userSkill == null
                || userSkill.getUser() == null
                || userSkill.getSkill() == null
                || userSkill.getUser().getId() == null
                || userSkill.getSkill().getId() == null) {

            throw new IllegalArgumentException(
                    "Candidate and skill are required."
            );
        }

        Long userId = userSkill.getUser().getId();
        Long skillId = userSkill.getSkill().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate not found."
                        )
                );

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Skill not found."
                        )
                );

        Integer proficiency = userSkill.getProficiency();

        validateProficiency(proficiency);

        List<UserSkill> existingSkills =
                userSkillRepository.findByUserId(userId);

        for (UserSkill existing : existingSkills) {

            if (existing.getSkill().getId().equals(skillId)) {

                // Nothing changed: do not create a new review request.
                if (existing.getProficiency().equals(proficiency)) {
                    return existing;
                }

                // Update only the candidate's claimed level.
                existing.setProficiency(proficiency);

                // Preserve existing verification and approved level.
                // Mark the updated claim for administrator review.
                existing.setPendingReview(true);

                return userSkillRepository.save(existing);
            }
        }

        // New skill: requires administrator verification.
        UserSkill newSkill = new UserSkill();

        newSkill.setUser(user);
        newSkill.setSkill(skill);
        newSkill.setProficiency(proficiency);

        newSkill.setVerified(false);
        newSkill.setApprovedProficiency(null);
        newSkill.setPendingReview(true);

        return userSkillRepository.save(newSkill);
    }

    // ADMIN APPROVES A CANDIDATE'S SKILL
    @Transactional
    public UserSkill verifyUserSkill(
            Long userSkillId,
            Integer approvedProficiency
    ) {

        validateProficiency(approvedProficiency);

        UserSkill existing = userSkillRepository.findById(userSkillId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate skill assignment not found."
                        )
                );

        existing.setApprovedProficiency(approvedProficiency);
        existing.setVerified(true);

        // Admin has reviewed the latest claim.
        existing.setPendingReview(false);

        return userSkillRepository.save(existing);
    }

    // ADMIN REVOKES A SKILL'S VERIFICATION
    @Transactional
    public UserSkill revokeVerification(Long userSkillId) {

        UserSkill existing = userSkillRepository.findById(userSkillId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate skill assignment not found."
                        )
                );

        existing.setVerified(false);
        existing.setApprovedProficiency(null);
        existing.setPendingReview(true);

        return userSkillRepository.save(existing);
    }

    // GET SKILLS ASSIGNED TO A CANDIDATE
    public List<UserSkill> getSkillsByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "Candidate not found."
            );
        }

        return userSkillRepository.findByUserId(userId);
    }

    // GET ALL SKILLS AWAITING VERIFICATION OR REVIEW
    public List<UserSkill> getPendingSkills() {

        return userSkillRepository.findAll()
                .stream()
                .filter(userSkill ->
                        Boolean.TRUE.equals(userSkill.getPendingReview())
                                || !Boolean.TRUE.equals(userSkill.getVerified())
                )
                .toList();
    }

    // REMOVE A SKILL FROM A CANDIDATE
    @Transactional
    public void deleteUserSkill(Long userSkillId) {

        UserSkill existing = userSkillRepository.findById(userSkillId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate skill assignment not found."
                        )
                );

        userSkillRepository.delete(existing);
    }

    // VALIDATE PROFICIENCY LEVEL
    private void validateProficiency(Integer proficiency) {

        if (proficiency == null
                || proficiency < 1
                || proficiency > 5) {

            throw new IllegalArgumentException(
                    "Proficiency must be between 1 and 5."
            );
        }
    }
}