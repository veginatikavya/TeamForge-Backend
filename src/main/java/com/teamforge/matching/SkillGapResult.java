package com.teamforge.matching;

public class SkillGapResult {

    private Long skillId;
    private String skillName;
    private Integer requiredProficiency;
    private String priority;
    private boolean covered;

    public SkillGapResult(
            Long skillId,
            String skillName,
            Integer requiredProficiency,
            String priority,
            boolean covered) {

        this.skillId = skillId;
        this.skillName = skillName;
        this.requiredProficiency = requiredProficiency;
        this.priority = priority;
        this.covered = covered;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Integer getRequiredProficiency() {
        return requiredProficiency;
    }

    public void setRequiredProficiency(Integer requiredProficiency) {
        this.requiredProficiency = requiredProficiency;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCovered() {
        return covered;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }
}