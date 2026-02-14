package com.example.pi_dev.user.models;


public class TFASecret {
    private long userId;
    private String secretKey;
    private String qrCode;

    public TFASecret() {}

    public TFASecret(long userId, String secretKey, String qrCode) {
        this.userId = userId;
        this.secretKey = secretKey;
        this.qrCode = qrCode;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
