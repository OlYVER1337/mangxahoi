package com.example.myapplication.utilities;


import android.os.AsyncTask;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
public class SendEmailTask extends AsyncTask<Void, Void, Void> {

    private String recipientEmail;
    private String otp;

    public SendEmailTask(String recipientEmail, String otp) {
        this.recipientEmail = recipientEmail;
        this.otp = otp;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("minh1234564567@gmail.com", "evuq hwod ratx duco");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("minh1234564567@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp);

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}