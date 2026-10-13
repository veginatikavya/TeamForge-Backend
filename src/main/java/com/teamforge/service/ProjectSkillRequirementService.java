package com.teamforge.service;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.repository.ProjectSkillRequirementRepository;
import com.teamforge.repository.ProjectRepository;
import com.teamforge.repository.SkillRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectSkillRequirementService {

    private final ProjectSkillRequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    public ProjectSkillRequirementService(
            ProjectSkillRequirementRepository requirementRepository,
            ProjectRepository projectRepository,
            SkillRepository skillRepository) {

        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
    }

    // CREATE PROJECT SKILL REQUIREMENT
    @Transactional
    public ProjectSkillRequirement saveRequirement(
            ProjectSkillRequirement requirement) {

        if (requirement == null) {
            throw new IllegalArgumentException(
                    "Requirement information is required."
            );
        }

        validateRequirement(requirement);

        if (requirement.getId() != null) {
            throw new IllegalArgumentException(
                    "Use the update endpoint to edit an existing requirement."
            );
        }

        Long projectId = requirement.getProject().getId();
        Long skillId = requirement.getSkill().getId();

        checkDuplicateSkill(projectId, skillId, null);

        return requirementRepository.save(requirement);
    }

    // UPDATE EXISTING PROJECT SKILL REQUIREMENT
    @Transactional
    public ProjectSkillRequirement updateRequirement(
            Long id,
            ProjectSkillRequirement updatedRequirement) {

        if (updatedRequirement == null) {
            throw new IllegalArgumentException(
                    "Requirement information is required."
            );
        }

        ProjectSkillRequirement existingRequirement =
                requirementRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Requirement not found with ID: " + id
                                )
                        );

        validateRequirement(updatedRequirement);

        Long projectId = updatedRequirement.getProject().getId();
        Long skillId = updatedRequirement.getSkill().getId();

        // Do not allow moving a requirement to another project
        if (!existingRequirement.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException(
                    "A requirement cannot be moved to another project."
            );
        }

        checkDuplicateSkill(projectId, skillId, id);

        existingRequirement.setSkill(updatedRequirement.getSkill());
        existingRequirement.setRequiredProficiency(
                updatedRequirement.getRequiredProficiency()
        );
        existingRequirement.setPriority(updatedRequirement.getPriority());

        return requirementRepository.save(existingRequirement);
    }

    // VALIDATE REQUIREMENT
    private void validateRequirement(
            ProjectSkillRequirement requirement) {

        if (requirement.getProject() == null
                || requirement.getProject().getId() == null) {

            throw new IllegalArgumentException(
                    "A valid project must be selected."
            );
        }

        Long projectId = requirement.getProject().getId();

        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException(
                    "Project not found with ID: " + projectId
            );
        }

        if (requirement.getSkill() == null
                || requirement.getSkill().getId() == null) {

            throw new IllegalArgumentException(
                    "A valid skill must be selected."
            );
        }

        Long skillId = requirement.getSkill().getId();

        if (!skillRepository.existsById(skillId)) {
            throw new IllegalArgumentException(
                    "Skill not found with ID: " + skillId
            );
        }

        Integer proficiency = requirement.getRequiredProficiency();

        if (proficiency == null || proficiency < 1 || proficiency > 5) {
            throw new IllegalArgumentException(
                    "Required proficiency must be between 1 and 5."
            );
        }

        if (requirement.getPriority() == null) {
            throw new IllegalArgumentException(
                    "Requirement priority is mandatory."
            );
        }
    }

    // PREVENT DUPLICATE SKILLS IN THE SAME PROJECT
    private void checkDuplicateSkill(
            Long projectId,
            Long skillId,
            Long currentRequirementId) {

        List<ProjectSkillRequirement> requirements =
                requirementRepository.findByProjectId(projectId);

        for (ProjectSkillRequirement existing : requirements) {

            if (existing.getSkill().getId().equals(skillId)
                    && !existing.getId().equals(currentRequirementId)) {

                throw new IllegalArgumentException(
                        "This skill is already required for the selected project."
                );
            }
        }
    }

    // GET ALL REQUIREMENTS FOR A PROJECT
    public List<ProjectSkillRequirement> getRequirementsByProject(
            Long projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException(
                    "Project not found with ID: " + projectId
            );
        }

        return requirementRepository.findByProjectId(projectId);
    }

    // GET REQUIREMENT BY ID
    public ProjectSkillRequirement getRequirementById(Long id) {

        return requirementRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Requirement not found with ID: " + id
                        )
                );
    }

    // DELETE REQUIREMENT
    @Transactional
    public void deleteRequirement(Long id) {

        if (!requirementRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Requirement not found with ID: " + id
            );
        }

        requirementRepository.deleteById(id);
    }
}