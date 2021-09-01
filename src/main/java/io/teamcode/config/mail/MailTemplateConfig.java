package io.teamcode.config.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Collections;

//for thymeleat 2.5?
@Configuration
public class MailTemplateConfig {

    private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";

    //TODO i18n?
    /*@Bean
    public ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("mail/MailMessages");
        return messageSource;
    }*/

    @Bean(name = "emailTemplateEngine", initMethod = "initialize")
    public TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        // Resolver for TEXT emails
        //templateEngine.addTemplateResolver(textTemplateResolver());
        // Resolver for HTML emails (except the editable one)
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        // Resolver for HTML editable emails (which will be treated as a String)
//        templateEngine.addTemplateResolver(stringTemplateResolver());
        /// Message source, internationalization specific to emails
        //templateEngine.setTemplateEngineMessageSource(emailMessageSource());

        //templateEngine.initialize();

        return templateEngine;
    }

    //for thymeleaf 3
    private ITemplateResolver textTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(1));
        templateResolver.setResolvablePatterns(Collections.singleton("text/*"));
        templateResolver.setPrefix("/mail/");
        templateResolver.setSuffix(".txt");
        //templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setName("email-template-resolver");
        templateResolver.setOrder(Integer.valueOf(1));
        //templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
        templateResolver.setPrefix("META-INF/templates/email/");//끝에 무조건 / 가 들어가야 디렉터리로 인식!
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(StandardTemplateModeHandlers.XHTML.getTemplateModeName());
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(true);

        return templateResolver;
    }

    //for thymeleaf 3
    /*private ITemplateResolver stringTemplateResolver() {
        final StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(3));
        // No resolvable pattern, will simply process as a String template everything not previously matched
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        return templateResolver;
    }*/


}
