package com.zuche.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
                // 完全公开的接口（不需要登录）- 放在最前面优先匹配
                .antMatchers("/auth/login", "/auth/register").permitAll()
                .antMatchers("/cars", "/cars/**").permitAll()
                .antMatchers("/stores", "/stores/**").permitAll()
                .antMatchers("/brands/**", "/categories/**", "/recommend/**").permitAll()
                .antMatchers("/reviews/car/**").permitAll()
                // 管理员接口 - 允许匿名访问，由Controller自己处理权限
                .antMatchers("/admin/**").permitAll()
                // 需要登录的接口
                .antMatchers("/auth/info", "/auth/password").authenticated()
                .antMatchers("/orders/**", "/reviews/**", "/chat/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .formLogin().disable()
            .httpBasic().disable();
    }
}
