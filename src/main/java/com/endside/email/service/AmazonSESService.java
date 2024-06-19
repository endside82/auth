package com.endside.email.service;

import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.EmailRestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


@Service
@Transactional
@Slf4j
public class AmazonSESService {

    @Value("${aws.ses.aws_key_id}")
    private String AWS_KEY_ID;

    @Value("${aws.ses.aws_key_password}")
    private String AWS_KEY_PASSWORD;

    @Value("${aws.ses.host}")
    private String HOST;

    @Value("${aws.ses.port}")
    private String PORT;

    @Value("${aws.ses.configuration}")
    private String CONFIG;

    @Value("${aws.ses.from}")
    private String FROM;

    @Value("${aws.ses.fromName}")
    private String FROM_NAME;

    public void send(String toEmail, String subject, String content) {
        Session session = Session.getDefaultInstance(getProperties());
        Transport transport = null;

        try {
            transport = session.getTransport();
            MimeMessage msg = setMimeMessage(session, toEmail, subject, content);
            transport.connect(HOST, AWS_KEY_ID, AWS_KEY_PASSWORD);
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (MessagingException e) {
            throw new EmailRestException(ErrorCode.AWS_MAIL_SERVER);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    log.error("transport closing fail");
                }
            }
        }
    }

    private Properties getProperties() {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }

    private MimeMessage setMimeMessage(Session session, String toEmail, String subject, String content) {
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(FROM, FROM_NAME));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            msg.setContent(content, "text/html;charset=UTF-8");
            msg.setHeader("X-SES-CONFIGURATION-SET", CONFIG);
        } catch (MessagingException e) {
            throw new EmailRestException(ErrorCode.AWS_MAIL_SERVER);
        } catch (UnsupportedEncodingException e) {
            throw new EmailRestException(ErrorCode.ERROR_ENCODING);
        }
        return msg;
    }


}
