package io.teamcode.config;

import io.teamcode.web.security.FlashAuthenticationFailureHandler;
import io.teamcode.web.security.TeamcodeAuthenticationProvider;
import io.teamcode.web.security.TeamcodeAuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
	
	//@Autowired
	//private SecurityProperties security;
	
	//@Autowired
	//private ConsoleSecurityConfig consoleSecurityConfig;

    @Autowired
	private UserDetailsService tcUserDetailsService;

	@Autowired
	TeamcodeAuthenticationSuccessHandler teamcodeAuthenticationSuccessHandler;

	@Autowired
	FlashAuthenticationFailureHandler flashAuthenticationFailureHandler;

	@Autowired
    TeamcodeAuthenticationProvider teamcodeAuthenticationProvider;

    //@Autowired
    //private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private TcConfig tcConfig;
    

	@Override
    protected void configure(HttpSecurity http) throws Exception {

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
		//http.authenticationProvider(ipAuthenticationProvider);
		//http.addFilterAfter(ipRangeFilter, FilterSecurityInterceptor.class);
        http.addFilterAfter(switchUserFilter(), FilterSecurityInterceptor.class);
		http.authorizeRequests()
                .antMatchers("/ln").access("hasAnyRole('ROLE_ANONYMOUS')")
                .antMatchers("/update-password").access("hasAnyRole('ROLE_ANONYMOUS')")
                .antMatchers("/api/v1/**").permitAll()
				.antMatchers("/assets/**").permitAll()
				.antMatchers("/hooks/**").permitAll()
				.antMatchers("/admin/**").access("hasAnyRole('ROLE_ADMIN')")
                .antMatchers("/**").access("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')");

        http
            .formLogin()
            	.loginProcessingUrl("/ln")
                .loginPage("/login")//지정해 주어야 한다. 그래야 form based auth가 동작한다.
                .successHandler(teamcodeAuthenticationSuccessHandler)
				.failureHandler(flashAuthenticationFailureHandler)
						//default value가 다르다!
                .usernameParameter("j_username")
				.passwordParameter("j_password")
                .permitAll()
            .and()
            	.logout()
                	.permitAll()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.logoutSuccessUrl("/login")
			.and()
				.exceptionHandling().accessDeniedPage("/error/403")
            .and()
            	.csrf()
            		.disable();

        //http.addFilterBefore(configureRestSecurityFilter(), WebAsyncManagerIntegrationFilter.class);


        /*http.authorizeRequests().antMatchers("/css/**").permitAll().anyRequest()
		.fullyAuthenticated().and().formLogin().loginPage("/login")
		.failureUrl("/login?error").permitAll();*/
    }
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		//auth.userDetailsService(tcUserDetailsService).passwordEncoder(passwordEncoder());
        auth.authenticationProvider(teamcodeAuthenticationProvider);
	}

	@Bean("tcPasswordEncoder")
	public BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SwitchUserFilter switchUserFilter() {
		SwitchUserFilter filter = new SwitchUserFilter();
		filter.setUserDetailsService(tcUserDetailsService);
		filter.setSuccessHandler(teamcodeAuthenticationSuccessHandler);
		//filter.setFailureHandler(authenticationFailureHandler());

		return filter;
	}
	
}
