package com.mvmlabs.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationMgr) throws Exception {
        authenticationMgr.inMemoryAuthentication()
            .withUser("mark").password("password123").authorities("ROLE_USER")
            .and()
            .withUser("mmeany").password("password123").authorities("ROLE_ADMIN");
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Override
    protected void configure(final HttpSecurity httpSecurity) throws Exception {

        httpSecurity
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic()
            .and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
            .antMatchers("/api/**").hasAuthority("ROLE_USER")
            .anyRequest().permitAll();
    }

}
