package io.teamcode.config;

import io.teamcode.web.internal.GroupAndProjectInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;

/**
 * @Valid Annotation 을 사용하려면 @EnableWebMvc Annotation 을 여기에 붙여야 한다.
 *
 * <p>아래는 주의할 점.</p>
 * <p>When you're adding @EnableWebMvc, you're turning off all of the spring boot's web auto configurations,
 * which automatically configures such a resolver for you. By removing the annotation,
 * auto configuration will turn on again and the auto configurated ViewResolver solves the problem.</p>
 *
 * Created by chiang on 16. 4. 1..
 */
@Configuration
//@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/", "classpath:/resources/",
            "classpath:/static/", "classpath:/public/" };

    //@Autowired
    //MilestoneConverter milestoneConverter;


    @PostConstruct
    public void init() {
        /*velocityViewResolver.getAttributesMap().put("springSecurityTool", new VelocitySecurityTools());
        velocityViewResolver.getAttributesMap().put("terpStringTool", new VelocityStringTools());
        velocityViewResolver.getAttributesMap().put("localeTool", new VelocityLocaleTools());
        velocityViewResolver.getAttributesMap().put("stringUtils", new StringUtils());*/
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repositoryInterceptor()).addPathPatterns("/**");
    }

    /**
     * Configure Converter to be used.
     * In our example, we need a converter to convert string values[Roles] to UserProfiles in newUser.jsp
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //registry.addConverter(milestoneConverter);
    }

    @Bean
    public GroupAndProjectInterceptor repositoryInterceptor() {

        return new GroupAndProjectInterceptor();
    }

    /*@Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.KOREA);

        return slr;
    }*/

    /*@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }*/


}
