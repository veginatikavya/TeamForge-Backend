package com.teamforge.matching;

import com.teamforge.entity.User;

public class CandidateMatchResult {

    private User user;
    private double matchScore;
    private boolean eligible;
    private String explanation;

    public CandidateMatchResult(
            User user,
            double matchScore,
            boolean eligible,
            String explanation) {

        this.user = user;
        this.matchScore = matchScore;
        this.eligible = eligible;
        this.explanation = explanation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}