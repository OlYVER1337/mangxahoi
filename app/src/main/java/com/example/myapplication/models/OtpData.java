package com.example.myapplication.models;

public class OtpData {
    public String email;
    public String otp;

    public OtpData() {
        // Default constructor required for calls to DataSnapshot.getValue(OtpData.class)
    }

    public OtpData(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}
