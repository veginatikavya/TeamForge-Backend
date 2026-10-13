package com.teamforge.repository;

import com.teamforge.entity.ProjectSkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectSkillRequirementRepository
        extends JpaRepository<ProjectSkillRequirement, Long> {

    // Retrieve all skill requirements for a project
    List<ProjectSkillRequirement> findByProjectId(Long projectId);

    // Check whether a skill is required by any project
    boolean existsBySkillId(Long skillId);

}