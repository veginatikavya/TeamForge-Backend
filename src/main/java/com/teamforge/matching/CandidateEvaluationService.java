package com.teamforge.matching;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.entity.User;
import com.teamforge.entity.UserSkill;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateEvaluationService {

    private final MatchingService matchingService;

    public CandidateEvaluationService(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    public CandidateMatchResult evaluateCandidate(
            User user,
            List<ProjectSkillRequirement> requirements,
            List<UserSkill> userSkills) {

        boolean eligible = true;
        StringBuilder explanation = new StringBuilder();

        for (ProjectSkillRequirement requirement : requirements) {

            if (!requirement.getPriority().equalsIgnoreCase("MANDATORY")) {
                continue;
            }

            boolean mandatoryRequirementMet = false;

            for (UserSkill userSkill : userSkills) {

                if (userSkill.getSkill().getId()
                        .equals(requirement.getSkill().getId())
                        && userSkill.getProficiency()
                        >= requirement.getRequiredProficiency()) {

                    mandatoryRequirementMet = true;
                    break;
                }
            }

            if (!mandatoryRequirementMet) {
                eligible = false;

                explanation.append("Missing mandatory requirement: ")
                        .append(requirement.getSkill().getName())
                        .append(". ");
            }
        }

        double matchScore =
                matchingService.calculateMatchScore(requirements, userSkills);

        if (eligible) {
            explanation.append("Candidate satisfies all mandatory requirements.");
        }

        return new CandidateMatchResult(
                user,
                matchScore,
                eligible,
                explanation.toString()
        );
    }
}