package com.endside.email.service;


import com.endside.email.constants.EmailType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

    private final TemplateEngine templateEngine;

    @Value("${email.check.page}")
    private String page;

    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String getSubject(EmailType emailType){
        return switch (emailType) {
            case OTP_TO_JOIN -> "Please verify your email address.";
            case OTP_TO_PASSWORD -> "Here is the code to reset password.";
            case OTP_TO_EMAIL -> "Here is the code to change email.";
            case JOIN_SUCCESS -> "Welcome to Unimewo.";
            default -> "";
        };
    }

    public String buildContentOtp(EmailType emailType, int otp){
        Context context = new Context();
        String templateName = "emailAuthCodeToChange";
        context.setVariable("otp", String.valueOf(otp));
        switch (emailType) {
            case OTP_TO_JOIN -> {
                context.setVariable("title1", "To verify your email, ");
                context.setVariable("title2", "please use the following code");
            }
            case OTP_TO_PASSWORD -> {
                context.setVariable("title1", "To reset your password, ");
                context.setVariable("title2", "please use the following code");
            }
            case OTP_TO_EMAIL -> {
                context.setVariable("title1", "To change your email, ");
                context.setVariable("title2", "please use the following code");
            }
            default -> {
            }
        }
        return templateEngine.process(templateName, context);
    }

    public String buildContent(EmailType emailType, String toEmail){
        Context context = new Context();
        String templateName = "";
        context.setVariable("page", page);
        context.setVariable("email", toEmail);
        switch (emailType) {
            case JOIN_SUCCESS -> templateName = "registerSuccess";
            case AUTH_MAIL -> templateName = "emailToAuth";
            default -> {
            }
        }
        return templateEngine.process(templateName, context);
    }
}
