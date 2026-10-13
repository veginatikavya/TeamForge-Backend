package com.teamforge.matching;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.entity.TeamMember;
import com.teamforge.entity.UserSkill;
import com.teamforge.repository.ProjectSkillRequirementRepository;
import com.teamforge.repository.TeamMemberRepository;
import com.teamforge.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GapAnalysisService {

    private final ProjectSkillRequirementRepository requirementRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserSkillRepository userSkillRepository;

    public GapAnalysisService(
            ProjectSkillRequirementRepository requirementRepository,
            TeamMemberRepository teamMemberRepository,
            UserSkillRepository userSkillRepository) {

        this.requirementRepository = requirementRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<SkillGapResult> analyzeProjectGaps(Long projectId) {

        List<ProjectSkillRequirement> requirements =
                requirementRepository.findByProjectId(projectId);

        List<TeamMember> teamMembers =
                teamMemberRepository.findByProjectId(projectId);

        List<SkillGapResult> results = new ArrayList<>();

        for (ProjectSkillRequirement requirement : requirements) {

            boolean covered = false;

            for (TeamMember member : teamMembers) {

                List<UserSkill> memberSkills =
                        userSkillRepository.findByUserId(
                                member.getUser().getId()
                        );

                for (UserSkill userSkill : memberSkills) {

                    if (userSkill.getSkill().getId()
                            .equals(requirement.getSkill().getId())
                            && userSkill.getProficiency()
                            >= requirement.getRequiredProficiency()) {

                        covered = true;
                        break;
                    }
                }

                if (covered) {
                    break;
                }
            }

            results.add(
                    new SkillGapResult(
                            requirement.getSkill().getId(),
                            requirement.getSkill().getName(),
                            requirement.getRequiredProficiency(),
                            requirement.getPriority(),
                            covered
                    )
            );
        }

        return results;
    }
}