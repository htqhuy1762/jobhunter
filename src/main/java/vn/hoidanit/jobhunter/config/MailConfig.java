package vn.hoidanit.jobhunter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/**
 * Mail Configuration
 * This configuration ensures JavaMailSender bean is explicitly available
 */
@Configuration
public class MailConfig {

    /**
     * JavaMailSender bean configuration
     * Spring Boot auto-configures this, but we define it explicitly
     * to help IDE detect the bean for autowiring
     */
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }
}

