package org.eclipse.tractusx.sde.core.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.core.model.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

@Service
@Slf4j
public class EmailManager {
    public static final String CCEMAIL = "ccemail";
    @Autowired
    private MimeMessage mimeMessage;

    @Value("${mail.from.address}")
    private String fromEmail;

    @Value("${mail.replyto.address}")
    private String replyTo;

    final Configuration configuration;

    public EmailManager(Configuration configuration) {
        this.configuration = configuration;
    }

    public String sendEmail(Map<String, Object> emailContent, String subject, String templateFileName) throws ServiceException {
        try {

            EmailRequest emailRequest = EmailRequest.builder().emailContent(emailContent)
                    .toEmail(emailContent.get("toemail").toString())
                    .subject(subject)
                    .templateFileName(templateFileName)
                    .build();

            mimeMessage.setFrom(new InternetAddress(fromEmail));
            if (replyTo != null && !replyTo.isEmpty()) {
                String[] mailAddressTo = replyTo.split(",");
                InternetAddress[] mailAddressTO = new InternetAddress[mailAddressTo.length];
                for (int i = 0; i < mailAddressTo.length; i++) {
                    mailAddressTO[i] = new InternetAddress(mailAddressTo[i]);
                }
                mimeMessage.setReplyTo(mailAddressTO);
            }
            mimeMessage.setSubject(emailRequest.getSubject());

            if (emailRequest.getToEmail() != null && !emailRequest.getToEmail().isEmpty()) {
                String[] split = emailRequest.getToEmail().split(",");
                InternetAddress[] addressTo = new InternetAddress[split.length];
                int i = 0;
                for (String emailString : split) {
                    addressTo[i] = new InternetAddress(StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeJava(emailString)));
                    i++;
                }
                mimeMessage.setRecipients(Message.RecipientType.TO, addressTo);
            } else {
                throw new ValidationException("To email is null");
            }

            InternetAddress[] addressCC = new InternetAddress[1];
            int i = 0;
            if (emailContent.containsKey(CCEMAIL) && emailContent.get(CCEMAIL) != null
                    && !emailContent.get(CCEMAIL).toString().isEmpty()) {
                String[] split = emailContent.get(CCEMAIL).toString().split(",");
                addressCC = new InternetAddress[split.length + 1];
                for (String string : split) {
                    addressCC[i] = new InternetAddress(StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeJava(string)));
                    i++;
                }
            }
            addressCC[i] = new InternetAddress(StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeJava(fromEmail)));

            mimeMessage.setRecipients(Message.RecipientType.CC, addressCC);

            String data = getEmailContent(emailRequest);
            mimeMessage.setContent(data, "text/html; charset=utf-8"); // as "text/plain"
            mimeMessage.setSentDate(new Date());
            Transport.send(mimeMessage);
            return "Email Sent Success";
        } catch (MessagingException | IOException | TemplateException e) {
            log.error("Error in email sending :{}", e.getMessage());
            throw new ServiceException("Error in email sending :" + e.getMessage());
        }
    }

    String getEmailContent(EmailRequest emailRequest) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        configuration.getTemplate(emailRequest.getTemplateFileName()).process(emailRequest.getEmailContent(),
                stringWriter);
        return stringWriter.getBuffer().toString();
    }
}
