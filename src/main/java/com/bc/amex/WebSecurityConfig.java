/*
 * Copyright 2019 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.amex;

import com.bc.amex.controllers.CrudActionNames;
import com.bc.amex.controllers.Templates;
import com.bc.amex.services.FileStorageService;
import com.looseboxes.mswordbox.net.Rest;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:51:22 PM
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource(name = "userDetailsService")
    private UserDetailsService userDetailsService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@todo use xml
        http
            .csrf().disable()    
            .authorizeRequests()
                .antMatchers("/", "/" + Templates.HOME, "/" + Templates.LOGIN, "/resources/**",
                        Rest.ENDPOINT_COMBO,
                        "/transfer/*",
                        "/send/*",
                        "/downloadDocument/*",
                        "/open/*",
                        "/mark/*",
                        "/uploadDocs/*")
                .permitAll()
                .anyRequest().authenticated()
                .and()
            .authorizeRequests()
                .antMatchers("/"+CrudActionNames.CREATE+"/*", 
                      "/"+CrudActionNames.UPDATE+"/*", 
                      "/"+CrudActionNames.DELETE+"/*")
                .hasAnyRole("SYSTEM", "ROLE_SYSTEM", "IT-ADMIN", "ROLE_IT-ADMIN")
//                .access("hasAnyRole('ROLE_SYSTEM', 'ROLE_IT-ADMIN')")
                .and()
            .authorizeRequests()
                .antMatchers("/"+CrudActionNames.READ+"/*",
                        "/"+CrudActionNames.SEARCH+"/*",
                        "/uploadFile**", 
                        FileStorageService.DOWNLOAD_PATH_CONTEXT + "/*",
                        "/form**",
                        "/get**", "/put**")
                .access("hasAnyRole('ROLE_SYSTEM', 'ROLE_IT-ADMIN', 'ROLE_USER')")
                .and()
            .formLogin()
                .loginPage("/" + Templates.LOGIN)
                .defaultSuccessUrl("/" + Templates.HOME)
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Bean public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }    

    @Bean public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //@todo use xml
        web.ignoring().antMatchers("/*.css");
        web.ignoring().antMatchers("/*.js");
        web.ignoring().antMatchers("/*.jpg", "/*.jpeg", "/*.gif", "/*.png", "/*.tiff");
//        web.ignoring().antMatchers("/*.doc", "/*.docx", "/*.xls", "/*.xlsx");
    }
//    @Bean
//    @Override
//    public UserDetailsService userDetailsService() {
//        UserDetails user = 
//             User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
}