package com.teamforge.matching;

import com.teamforge.entity.User;

public class TeamContributionResult {

    private User user;
    private double contributionScore;
    private String explanation;

    public TeamContributionResult(
            User user,
            double contributionScore,
            String explanation) {

        this.user = user;
        this.contributionScore = contributionScore;
        this.explanation = explanation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getContributionScore() {
        return contributionScore;
    }

    public void setContributionScore(double contributionScore) {
        this.contributionScore = contributionScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}