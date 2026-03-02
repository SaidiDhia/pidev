package com.example.pi_dev.blog.Services;

public class ModerationResult {
    public final double  score;
    public final String  reason;
    public final boolean shouldHide;

    public ModerationResult(double score, String reason, boolean shouldHide) {
        this.score      = score;
        this.reason     = reason;
        this.shouldHide = shouldHide;
    }
}