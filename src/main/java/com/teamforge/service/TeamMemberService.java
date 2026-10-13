package com.teamforge.service;

import com.teamforge.entity.Project;
import com.teamforge.entity.TeamMember;
import com.teamforge.entity.User;

import com.teamforge.repository.ProjectRepository;
import com.teamforge.repository.TeamMemberRepository;
import com.teamforge.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TeamMemberService(
            TeamMemberRepository teamMemberRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.teamMemberRepository = teamMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamMember addTeamMember(TeamMember teamMember) {

        // 1. Validate project information
        if (teamMember == null ||
                teamMember.getProject() == null ||
                teamMember.getProject().getId() == null) {

            throw new IllegalArgumentException(
                    "Please select a valid project."
            );
        }

        // 2. Validate candidate information
        if (teamMember.getUser() == null ||
                teamMember.getUser().getId() == null) {

            throw new IllegalArgumentException(
                    "Please select a valid candidate."
            );
        }

        Long projectId = teamMember.getProject().getId();
        Long userId = teamMember.getUser().getId();

        // 3. Check whether the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found."
                ));

        // 4. Check whether the candidate exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Candidate not found."
                ));

        // 5. Check candidate availability
        if (!Boolean.TRUE.equals(user.getAvailable())) {

            throw new IllegalArgumentException(
                    "Cannot add team member: Candidate is not available."
            );
        }

        // 6. Get the current project team
        List<TeamMember> existingMembers =
                teamMemberRepository.findByProjectId(projectId);

        // 7. Prevent duplicate team assignments
        for (TeamMember existingMember : existingMembers) {

            if (existingMember.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException(
                        "Cannot add team member: Candidate is already assigned to this project."
                );
            }
        }

        // 8. Enforce maximum team size
        Integer maximumTeamSize = project.getTeamSize();

        if (maximumTeamSize == null || maximumTeamSize <= 0) {

            throw new IllegalArgumentException(
                    "Project must have a valid maximum team size."
            );
        }

        if (existingMembers.size() >= maximumTeamSize) {

            throw new IllegalArgumentException(
                    "Cannot add team member: Project team size limit of "
                            + maximumTeamSize
                            + " has been reached."
            );
        }

        // 9. Save the validated assignment
        teamMember.setId(null);
        teamMember.setProject(project);
        teamMember.setUser(user);

        return teamMemberRepository.save(teamMember);
    }

    public List<TeamMember> getTeamMembers(Long projectId) {

        return teamMemberRepository.findByProjectId(projectId);
    }

    public void removeTeamMember(Long id) {

        teamMemberRepository.deleteById(id);
    }
}