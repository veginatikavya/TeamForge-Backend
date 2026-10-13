package com.teamforge.matching;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.entity.TeamMember;
import com.teamforge.entity.User;
import com.teamforge.entity.UserSkill;

import com.teamforge.repository.ProjectSkillRequirementRepository;
import com.teamforge.repository.TeamMemberRepository;
import com.teamforge.repository.UserRepository;
import com.teamforge.repository.UserSkillRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TeamReplacementService {

    private final ProjectSkillRequirementRepository requirementRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    public TeamReplacementService(
            ProjectSkillRequirementRepository requirementRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository) {

        this.requirementRepository = requirementRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<TeamContributionResult> recommendReplacements(
            Long projectId,
            Long memberId) {

        List<TeamMember> teamMembers =
                teamMemberRepository.findByProjectId(projectId);

        // Find the member being replaced.
        TeamMember memberToReplace = null;

        for (TeamMember member : teamMembers) {

            if (member.getId().equals(memberId)) {
                memberToReplace = member;
                break;
            }
        }

        if (memberToReplace == null) {
            throw new IllegalArgumentException(
                    "Team member not found in this project."
            );
        }

        List<ProjectSkillRequirement> requirements =
                requirementRepository.findByProjectId(projectId);

        // Keep all team members except the member being replaced.
        Set<Long> remainingMemberIds = new HashSet<>();

        for (TeamMember member : teamMembers) {

            if (!member.getId().equals(memberId)) {
                remainingMemberIds.add(member.getUser().getId());
            }
        }

        // Find requirements already covered by remaining members.
        Set<Long> coveredSkillIds = new HashSet<>();

        for (Long userId : remainingMemberIds) {

            List<UserSkill> userSkills =
                    userSkillRepository.findByUserId(userId);

            for (ProjectSkillRequirement requirement : requirements) {

                for (UserSkill userSkill : userSkills) {

                    if (userSkill.getSkill().getId()
                            .equals(requirement.getSkill().getId())
                            && userSkill.getProficiency()
                            >= requirement.getRequiredProficiency()) {

                        coveredSkillIds.add(
                                requirement.getSkill().getId()
                        );

                        break;
                    }
                }
            }
        }

        List<TeamContributionResult> results = new ArrayList<>();

        for (User candidate : userRepository.findAll()) {

            // Candidate must be available.
            if (!Boolean.TRUE.equals(candidate.getAvailable())) {
                continue;
            }

            // Cannot recommend the member being replaced.
            if (candidate.getId()
                    .equals(memberToReplace.getUser().getId())) {
                continue;
            }

            // Cannot recommend someone already on the remaining team.
            if (remainingMemberIds.contains(candidate.getId())) {
                continue;
            }

            List<UserSkill> candidateSkills =
                    userSkillRepository.findByUserId(candidate.getId());

            double contribution = 0;
            double maximumContribution = 0;

            StringBuilder explanation = new StringBuilder();

            for (ProjectSkillRequirement requirement : requirements) {

                Long skillId = requirement.getSkill().getId();

                // Skip skills already covered by remaining team.
                if (coveredSkillIds.contains(skillId)) {
                    continue;
                }

                double weight = getWeight(requirement.getPriority());

                maximumContribution += weight;

                for (UserSkill candidateSkill : candidateSkills) {

                    if (candidateSkill.getSkill().getId().equals(skillId)
                            && candidateSkill.getProficiency()
                            >= requirement.getRequiredProficiency()) {

                        contribution += weight;

                        explanation.append("Restores ")
                                .append(requirement.getSkill().getName())
                                .append(" coverage. ");

                        break;
                    }
                }
            }

            double contributionScore =
                    maximumContribution == 0
                            ? 0
                            : (contribution / maximumContribution) * 100;

            // Only include candidates who restore missing coverage.
            if (contributionScore <= 0) {
                continue;
            }

            results.add(
                    new TeamContributionResult(
                            candidate,
                            contributionScore,
                            explanation.toString()
                    )
            );
        }

        results.sort(
                Comparator.comparingDouble(
                        TeamContributionResult::getContributionScore
                ).reversed()
        );

        return results;
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