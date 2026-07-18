package net.zephyrlink.zephyrmail.security.threat;

import org.springframework.stereotype.Service;

@Service
public class ThreatLevelService {

    public enum DefconLevel {
        ONE, TWO, THREE, FOUR, FIVE
    }

    private DefconLevel currentLevel = DefconLevel.ONE;
    private int failedLoginScore = 0;
    private int threatGradeScore = 0;
    private int trackerScore = 0;

    public void recordFailedLogins(int count) {
        this.failedLoginScore = count;
        recalculate();
    }

    public void recordThreatGradeHit() {
        this.threatGradeScore++;
        recalculate();
    }

    public void recordTrackerDetected() {
        this.trackerScore++;
        recalculate();
    }

    public void reset() {
        this.failedLoginScore = 0;
        this.threatGradeScore = 0;
        this.trackerScore = 0;
        this.currentLevel = DefconLevel.ONE;
    }

    public DefconLevel getCurrentLevel() {
        return currentLevel;
    }

    private void recalculate() {
        int total = failedLoginScore + threatGradeScore + trackerScore;

        if (total >= 20) {
            currentLevel = DefconLevel.FIVE;
        } else if (total >= 15) {
            currentLevel = DefconLevel.FOUR;
        } else if (total >= 10) {
            currentLevel = DefconLevel.THREE;
        } else if (total >= 5) {
            currentLevel = DefconLevel.TWO;
        } else {
            currentLevel = DefconLevel.ONE;
        }
    }
}