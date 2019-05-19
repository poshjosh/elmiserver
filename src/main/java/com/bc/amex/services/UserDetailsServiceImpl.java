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

import com.bc.amex.jpa.repository.UserRepository;
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 10:39:32 AM
 */
@Service("userDetailsService")
//@Transactional
public class UserDetailsServiceImpl extends SecurityServiceImpl implements UserDetailsService{

    private static final Logger LOG = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired UserRepository userRepo;
    
    @Override
//    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
  
        if(username == null || username.isEmpty()) {
            throw new ValidationException("You did not enter a username");
        }
        
        LOG.debug("For username: {}", username);

        final User user = userRepo.findSingleBy(User_.username, username, null);
        
        LOG.debug("For username: {}, found entity: {}", username, user);
        
        if (user == null) {
            return this.createDefaultUser();
        }
 
        return this.createUser(user.getUsername(), user.getPassword(), 
                userRepo.isEnabled(user), this.getRolesAndPrivileges(user.getRoleList()));
    }
}