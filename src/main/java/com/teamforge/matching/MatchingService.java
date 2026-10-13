package com.teamforge.matching;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.entity.UserSkill;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingService {

    public double calculateMatchScore(
            List<ProjectSkillRequirement> requirements,
            List<UserSkill> userSkills) {

        double totalScore = 0;
        double maximumScore = 0;

        for (ProjectSkillRequirement requirement : requirements) {

            double weight = getWeight(requirement.getPriority());

            maximumScore += weight * requirement.getRequiredProficiency();

            for (UserSkill userSkill : userSkills) {

                if (userSkill.getSkill().getId()
                        .equals(requirement.getSkill().getId())) {

                    int matchedProficiency = Math.min(
                            userSkill.getProficiency(),
                            requirement.getRequiredProficiency()
                    );

                    totalScore += weight * matchedProficiency;
                    break;
                }
            }
        }

        if (maximumScore == 0) {
            return 0;
        }

        return (totalScore / maximumScore) * 100;
    }

    private double getWeight(String priority) {

        if (priority.equalsIgnoreCase("MANDATORY")) {
            return 3.0;
        }

        if (priority.equalsIgnoreCase("PREFERRED")) {
            return 2.0;
        }

        return 1.0;
    }
}