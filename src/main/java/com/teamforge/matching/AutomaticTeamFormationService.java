package com.teamforge.matching;

import com.teamforge.entity.Project;
import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.entity.User;
import com.teamforge.entity.UserSkill;

import com.teamforge.repository.ProjectRepository;
import com.teamforge.repository.ProjectSkillRequirementRepository;
import com.teamforge.repository.UserRepository;
import com.teamforge.repository.UserSkillRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AutomaticTeamFormationService {

    private final ProjectRepository projectRepository;
    private final ProjectSkillRequirementRepository requirementRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    public AutomaticTeamFormationService(
            ProjectRepository projectRepository,
            ProjectSkillRequirementRepository requirementRepository,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository) {

        this.projectRepository = projectRepository;
        this.requirementRepository = requirementRepository;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<User> suggestTeam(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found: " + projectId
                        )
                );

        List<ProjectSkillRequirement> requirements =
                requirementRepository.findByProjectId(projectId);

        List<User> availableCandidates = new ArrayList<>();

        for (User user : userRepository.findAll()) {

            if (Boolean.TRUE.equals(user.getAvailable())) {
                availableCandidates.add(user);
            }
        }

        Map<Long, Map<Long, Integer>> candidateSkills = new HashMap<>();

        for (User candidate : availableCandidates) {

            Map<Long, Integer> skills = new HashMap<>();

            List<UserSkill> userSkills =
                    userSkillRepository.findByUserId(candidate.getId());

            for (UserSkill userSkill : userSkills) {

                // Ignore skills that have not been verified.
                if (!Boolean.TRUE.equals(userSkill.getVerified())) {
                    continue;
                }

                // Ignore skills without an approved proficiency.
                Integer approvedProficiency =
                        userSkill.getApprovedProficiency();

                if (approvedProficiency == null
                        || approvedProficiency < 1
                        || approvedProficiency > 5) {
                    continue;
                }

                Long skillId = userSkill.getSkill().getId();

                // Use admin-approved proficiency.
                skills.merge(
                        skillId,
                        approvedProficiency,
                        Math::max
                );
            }

            candidateSkills.put(candidate.getId(), skills);
        }

        List<User> suggestedTeam = new ArrayList<>();

        Set<Long> coveredSkillIds = new HashSet<>();
        Set<Long> selectedCandidateIds = new HashSet<>();

        int maximumTeamSize = project.getTeamSize();

        while (suggestedTeam.size() < maximumTeamSize) {

            User bestCandidate = null;
            double bestContribution = 0;

            for (User candidate : availableCandidates) {

                if (selectedCandidateIds.contains(candidate.getId())) {
                    continue;
                }

                Map<Long, Integer> skills =
                        candidateSkills.get(candidate.getId());

                double contribution = 0;

                for (ProjectSkillRequirement requirement : requirements) {

                    Long skillId = requirement.getSkill().getId();

                    if (coveredSkillIds.contains(skillId)) {
                        continue;
                    }

                    int candidateProficiency =
                            skills.getOrDefault(skillId, 0);

                    if (candidateProficiency >=
                            requirement.getRequiredProficiency()) {

                        contribution += getWeight(
                                requirement.getPriority()
                        );
                    }
                }

                if (contribution > bestContribution) {
                    bestContribution = contribution;
                    bestCandidate = candidate;
                }
            }

            if (bestCandidate == null || bestContribution <= 0) {
                break;
            }

            suggestedTeam.add(bestCandidate);
            selectedCandidateIds.add(bestCandidate.getId());

            Map<Long, Integer> selectedSkills =
                    candidateSkills.get(bestCandidate.getId());

            for (ProjectSkillRequirement requirement : requirements) {

                Long skillId = requirement.getSkill().getId();

                int proficiency =
                        selectedSkills.getOrDefault(skillId, 0);

                if (proficiency >= requirement.getRequiredProficiency()) {
                    coveredSkillIds.add(skillId);
                }
            }
        }

        return suggestedTeam;
    }

    private double getWeight(String priority) {

        if ("MANDATORY".equalsIgnoreCase(priority)) {
            return 3.0;
        }

        if ("PREFERRED".equalsIgnoreCase(priority)) {
            return 2.0;
        }

        return 1.0;
    }
}