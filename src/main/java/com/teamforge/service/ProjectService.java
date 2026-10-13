package com.teamforge.service;

import com.teamforge.entity.Project;
import com.teamforge.repository.ProjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // CREATE PROJECT
    @Transactional
    public Project saveProject(Project project) {

        if (project == null) {
            throw new IllegalArgumentException(
                    "Project information is required."
            );
        }

        validateProject(project);

        if (project.getId() != null) {
            throw new IllegalArgumentException(
                    "Use the update endpoint to edit an existing project."
            );
        }

        checkDuplicateProjectName(project.getName(), null);

        return projectRepository.save(project);
    }

    // UPDATE EXISTING PROJECT
    @Transactional
    public Project updateProject(Long id, Project updatedProject) {

        if (updatedProject == null) {
            throw new IllegalArgumentException(
                    "Project information is required."
            );
        }

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found with ID: " + id
                        )
                );

        validateProject(updatedProject);

        checkDuplicateProjectName(updatedProject.getName(), id);

        // Update editable project details
        existingProject.setName(updatedProject.getName());
        existingProject.setDescription(updatedProject.getDescription());
        existingProject.setTeamSize(updatedProject.getTeamSize());

        // Keep the original project ID and creator unchanged
        return projectRepository.save(existingProject);
    }

    // VALIDATE PROJECT DETAILS
    private void validateProject(Project project) {

        if (project.getName() == null
                || project.getName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Project name cannot be empty."
            );
        }

        String name = project.getName()
                .trim()
                .replaceAll("\\s+", " ");

        if (name.length() < 3 || name.length() > 120) {
            throw new IllegalArgumentException(
                    "Project name must contain between 3 and 120 characters."
            );
        }

        project.setName(name);

        if (project.getDescription() != null) {

            String description = project.getDescription().trim();

            if (description.length() > 2000) {
                throw new IllegalArgumentException(
                        "Project description cannot exceed 2000 characters."
                );
            }

            project.setDescription(description);
        }

        if (project.getTeamSize() == null
                || project.getTeamSize() < 1
                || project.getTeamSize() > 100) {

            throw new IllegalArgumentException(
                    "Team size must be between 1 and 100 members."
            );
        }
    }

    // PREVENT DUPLICATE PROJECT NAMES
    private void checkDuplicateProjectName(
            String name,
            Long currentProjectId
    ) {

        List<Project> projects = projectRepository.findAll();

        for (Project existing : projects) {

            if (existing.getName() != null
                    && existing.getName().equalsIgnoreCase(name)
                    && !existing.getId().equals(currentProjectId)) {

                throw new IllegalArgumentException(
                        "A project with this name already exists."
                );
            }
        }
    }

    // GET ALL PROJECTS
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // GET PROJECT BY ID
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    // DELETE PROJECT
    @Transactional
    public void deleteProject(Long id) {

        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Project not found with ID: " + id
            );
        }

        projectRepository.deleteById(id);
    }
}