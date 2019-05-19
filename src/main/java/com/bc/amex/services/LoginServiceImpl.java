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

package com.bc.amex.services;

import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 2:21:51 PM
 */
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginServiceImpl.class);
    
    @Autowired private UserDetailsService userDetailsService;

    @Autowired private AuthenticationManager authenticationManager;
    
    @Override
    public boolean login(String username, String password) {
        
        if(username == null || username.isEmpty()) {
            throw new ValidationException("You did not enter a username");
        }
        if(password == null || password.isEmpty()) {
            throw new ValidationException("You did not enter a password");
        }
        
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

//        try{
            
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            if (usernamePasswordAuthenticationToken.isAuthenticated()) {

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                LOG.debug("Logged in {} successfully", username);

                return true;

            }else{

                LOG.debug("Failed to Login {}", username);

                return false;
            }
//        }catch(AuthenticationException e) {
        
//            LOG.warn("Failed to Login " + username, e);

//            return false;
//        }
    }
}
