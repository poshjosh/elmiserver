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

import com.bc.elmi.pu.entities.Permission;
import com.bc.elmi.pu.entities.Role;
import com.looseboxes.mswordbox.security.LoginManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 1:43:01 PM
 */
@Service("securityService")
public class SecurityServiceImpl implements SecurityService{

//    private static final Logger LOG = LoggerFactory.getLogger(SecurityServiceImpl.class);
    
    @Override
    public UserDetails createUser(String username, String password, 
            boolean enabled, List<String> rolesAndPrivileges) {
        return new org.springframework.security.core.userdetails.User(
          username, password, enabled, true, true, 
          true, SecurityServiceImpl.this.getGrantedAuthorities(rolesAndPrivileges));
    }
    
    @Override
    public UserDetails createDefaultUser() {
        return new org.springframework.security.core.userdetails.User(
              LoginManager.USERNAME_IF_NONE, LoginManager.USERNAME_IF_NONE, 
                true, true, true, true, Collections.EMPTY_LIST);
    }
    
    @Override
    public Optional<UserDetails> getLoggedInUser() {
        return Optional.ofNullable(getLoggedInUser(null));
    }

    @Override
    public UserDetails getLoggedInUser(UserDetails resultIfNone) {
        final Object userObj = SecurityContextHolder.getContext().getAuthentication().getDetails();
        final UserDetails userDetails = userObj instanceof UserDetails ?
                (UserDetails)userObj : null;
        return userDetails == null ? resultIfNone : userDetails;
    }

    public Collection<? extends GrantedAuthority> getGrantedAuthoritiesForRoles(Collection<Role> roles) {
        return SecurityServiceImpl.this.getGrantedAuthorities(getRolesAndPrivileges(roles));
    }
 
    public List<String> getRolesAndPrivileges(Collection<Role> roles) {
        final List<String> privileges = new ArrayList<>();
        final List<Permission> entities = new ArrayList<>();
        for(Role role : roles) {
            privileges.add(role.getRolename());
            entities.addAll(role.getPermissionList());
        }
        for(Permission item : entities) {
            privileges.add(item.getPermissionname());
        }
        return privileges;
    }
 
    public List<String> getPrivileges(Collection<Role> roles) {
        final List<String> privileges = new ArrayList<>();
        final List<Permission> entities = new ArrayList<>();
        for(Role role : roles) {
            entities.addAll(role.getPermissionList());
        }
        for(Permission item : entities) {
            privileges.add(item.getPermissionname());
        }
        return privileges;
    }

    @Override
    public List<GrantedAuthority> getGrantedAuthorities(List<String> rolesAndPrivileges) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : rolesAndPrivileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
}