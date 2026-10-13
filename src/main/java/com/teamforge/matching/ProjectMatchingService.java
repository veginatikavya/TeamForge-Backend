package com.teamforge.matching;

import com.teamforge.entity.ProjectSkillRequirement;
import com.teamforge.entity.User;
import com.teamforge.entity.UserSkill;
import com.teamforge.repository.ProjectSkillRequirementRepository;
import com.teamforge.repository.UserRepository;
import com.teamforge.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProjectMatchingService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProjectSkillRequirementRepository requirementRepository;
    private final CandidateEvaluationService candidateEvaluationService;

    public ProjectMatchingService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            ProjectSkillRequirementRepository requirementRepository,
            CandidateEvaluationService candidateEvaluationService) {

        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.requirementRepository = requirementRepository;
        this.candidateEvaluationService = candidateEvaluationService;
    }

    public List<CandidateMatchResult> getCandidateMatches(Long projectId) {

        List<ProjectSkillRequirement> requirements =
                requirementRepository.findByProjectId(projectId);

        List<User> users = userRepository.findAll();

        List<CandidateMatchResult> results = new ArrayList<>();

        for (User user : users) {

            if (Boolean.FALSE.equals(user.getAvailable())) {
                continue;
            }

            List<UserSkill> userSkills =
                    userSkillRepository.findByUserId(user.getId());

            CandidateMatchResult result =
                    candidateEvaluationService.evaluateCandidate(
                            user,
                            requirements,
                            userSkills
                    );

            results.add(result);
        }

        results.sort(
                Comparator.comparingDouble(CandidateMatchResult::getMatchScore)
                        .reversed()
        );

        return results;
    }
}