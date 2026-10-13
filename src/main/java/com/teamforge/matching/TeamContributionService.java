package com.teamforge.matching;

import com.teamforge.entity.User;
import com.teamforge.entity.UserSkill;
import com.teamforge.entity.TeamMember;
import com.teamforge.entity.ProjectSkillRequirement;

import com.teamforge.repository.UserRepository;
import com.teamforge.repository.UserSkillRepository;
import com.teamforge.repository.TeamMemberRepository;
import com.teamforge.repository.ProjectSkillRequirementRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TeamContributionService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectSkillRequirementRepository requirementRepository;
    private final GapAnalysisService gapAnalysisService;

    public TeamContributionService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            TeamMemberRepository teamMemberRepository,
            ProjectSkillRequirementRepository requirementRepository,
            GapAnalysisService gapAnalysisService) {

        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.requirementRepository = requirementRepository;
        this.gapAnalysisService = gapAnalysisService;
    }

    public List<TeamContributionResult> getRecommendations(Long projectId) {

        List<SkillGapResult> gaps =
                gapAnalysisService.analyzeProjectGaps(projectId);

        List<ProjectSkillRequirement> requirements =
                requirementRepository.findByProjectId(projectId);

        List<TeamMember> teamMembers =
                teamMemberRepository.findByProjectId(projectId);

        Set<Long> selectedUserIds = new HashSet<>();

        for (TeamMember member : teamMembers) {
            selectedUserIds.add(member.getUser().getId());
        }

        // Find the highest ADMIN-APPROVED proficiency
        // already present in the assigned team.
        Map<Long, Integer> currentTeamProficiency = new HashMap<>();

        for (TeamMember member : teamMembers) {

            List<UserSkill> memberSkills =
                    userSkillRepository.findByUserId(
                            member.getUser().getId()
                    );

            for (UserSkill memberSkill : memberSkills) {

                if (!isApprovedSkill(memberSkill)) {
                    continue;
                }

                Long skillId = memberSkill.getSkill().getId();

                currentTeamProficiency.merge(
                        skillId,
                        memberSkill.getApprovedProficiency(),
                        Math::max
                );
            }
        }

        List<TeamContributionResult> results = new ArrayList<>();

        for (User user : userRepository.findAll()) {

            // Only consider available candidates.
            if (!Boolean.TRUE.equals(user.getAvailable())) {
                continue;
            }

            // Do not recommend existing team members.
            if (selectedUserIds.contains(user.getId())) {
                continue;
            }

            List<UserSkill> userSkills =
                    userSkillRepository.findByUserId(user.getId());

            Map<Long, Integer> candidateProficiency = new HashMap<>();

            for (UserSkill userSkill : userSkills) {

                if (!isApprovedSkill(userSkill)) {
                    continue;
                }

                candidateProficiency.merge(
                        userSkill.getSkill().getId(),
                        userSkill.getApprovedProficiency(),
                        Math::max
                );
            }

            double totalImprovement = 0;
            double maximumPossibleImprovement = 0;

            StringBuilder explanation = new StringBuilder();

            for (SkillGapResult gap : gaps) {

                if (gap.isCovered()) {
                    continue;
                }

                Long skillId = gap.getSkillId();
                int required = gap.getRequiredProficiency();

                int current = Math.min(
                        currentTeamProficiency.getOrDefault(skillId, 0),
                        required
                );

                int candidate = Math.min(
                        candidateProficiency.getOrDefault(skillId, 0),
                        required
                );

                double weight = getWeight(gap.getPriority());

                int possibleImprovement = required - current;

                maximumPossibleImprovement +=
                        weight * possibleImprovement;

                // A candidate improves a skill only when their
                // APPROVED proficiency exceeds the team's current best.
                int improvement = Math.max(0, candidate - current);

                if (improvement > 0) {

                    totalImprovement += weight * improvement;

                    if (candidate >= required) {

                        explanation.append("Fully covers ")
                                .append(gap.getSkillName())
                                .append(". ");

                    } else {

                        explanation.append("Improves ")
                                .append(gap.getSkillName())
                                .append(" proficiency from ")
                                .append(current)
                                .append(" to ")
                                .append(candidate)
                                .append(", but does not fully cover it. ");
                    }
                }
            }

            double contributionScore =
                    maximumPossibleImprovement == 0
                            ? 0
                            : (totalImprovement
                                    / maximumPossibleImprovement) * 100;

            // Exclude candidates who add no measurable coverage.
            if (contributionScore <= 0) {
                continue;
            }

            results.add(
                    new TeamContributionResult(
                            user,
                            Math.round(contributionScore * 100.0) / 100.0,
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

    // Only verified skills with valid approved levels count.
    private boolean isApprovedSkill(UserSkill userSkill) {

        if (!Boolean.TRUE.equals(userSkill.getVerified())) {
            return false;
        }

        Integer approvedProficiency =
                userSkill.getApprovedProficiency();

        return approvedProficiency != null
                && approvedProficiency >= 1
                && approvedProficiency <= 5;
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