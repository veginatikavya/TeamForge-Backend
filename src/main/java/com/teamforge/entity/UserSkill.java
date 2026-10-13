package com.teamforge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    // Proficiency claimed by the candidate
    @Column(nullable = false)
    private Integer proficiency;

    // Whether the admin has verified this skill
    @Column(nullable = false)
    private Boolean verified = false;

    // Proficiency approved by the admin
    private Integer approvedProficiency;

    // Whether a new or updated skill needs admin review
    @Column(nullable = false)
    private Boolean pendingReview = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Integer getProficiency() {
        return proficiency;
    }

    public void setProficiency(Integer proficiency) {
        this.proficiency = proficiency;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Integer getApprovedProficiency() {
        return approvedProficiency;
    }

    public void setApprovedProficiency(Integer approvedProficiency) {
        this.approvedProficiency = approvedProficiency;
    }

    public Boolean getPendingReview() {
        return pendingReview;
    }

    public void setPendingReview(Boolean pendingReview) {
        this.pendingReview = pendingReview;
    }
}