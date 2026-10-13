package com.teamforge.service;

import com.teamforge.entity.Skill;
import com.teamforge.repository.SkillRepository;
import com.teamforge.repository.UserSkillRepository;
import com.teamforge.repository.ProjectSkillRequirementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProjectSkillRequirementRepository projectSkillRequirementRepository;

    public SkillService(
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            ProjectSkillRequirementRepository projectSkillRequirementRepository) {

        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.projectSkillRequirementRepository = projectSkillRequirementRepository;
    }

    // CREATE SKILL WITH VALIDATION
    public Skill saveSkill(Skill skill) {

        if (skill == null) {
            throw new IllegalArgumentException(
                    "Skill information is required."
            );
        }

        String rawName = skill.getName();

        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Skill name cannot be empty."
            );
        }

        final String name = rawName.trim().replaceAll("\\s+", " ");

        if (name.length() < 2 || name.length() > 60) {
            throw new IllegalArgumentException(
                    "Skill name must contain between 2 and 60 characters."
            );
        }

        if (!name.matches(".*\\p{L}.*")) {
            throw new IllegalArgumentException(
                    "Skill name must contain at least one letter."
            );
        }

        if (!name.matches("[\\p{L}\\p{N} .+#/_-]+")) {
            throw new IllegalArgumentException(
                    "Skill name contains unsupported characters."
            );
        }

        boolean duplicateExists = skillRepository.findAll()
                .stream()
                .anyMatch(existingSkill ->
                        existingSkill.getName() != null
                                && existingSkill.getName()
                                .trim()
                                .equalsIgnoreCase(name)
                );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "A skill with this name already exists."
            );
        }

        skill.setName(name);

        return skillRepository.save(skill);
    }

    // GET ALL SKILLS
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    // FIND SKILL BY NAME
    public Optional<Skill> getSkillByName(String name) {
        return skillRepository.findByName(name);
    }

    // DELETE SKILL SAFELY
    @Transactional
    public void deleteSkill(Long skillId) {

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Skill not found."
                        )
                );

        // Check whether any candidate uses this skill
        if (userSkillRepository.existsBySkillId(skillId)) {
            throw new IllegalStateException(
                    "Cannot delete '" + skill.getName()
                            + "' because it is assigned to one or more candidates."
            );
        }

        // Check whether any project requires this skill
        if (projectSkillRequirementRepository.existsBySkillId(skillId)) {
            throw new IllegalStateException(
                    "Cannot delete '" + skill.getName()
                            + "' because it is required by one or more projects."
            );
        }

        // Delete only when the skill has no dependencies
        skillRepository.delete(skill);
    }
}