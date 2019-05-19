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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 1:42:11 PM
 */
public interface SecurityService {
    
    UserDetails createUser(String username, String password, 
            boolean enabled, List<String> rolesAndPrivileges);
    
    UserDetails createDefaultUser();

//    Collection<? extends GrantedAuthority> getAuthoritiesForRoles(Collection<Role> roles);

    default List<GrantedAuthority> getGrantedAuthorities(String... rolesOrPrivileges) {
        return rolesOrPrivileges == null ? Collections.EMPTY_LIST : getGrantedAuthorities(rolesOrPrivileges);
    }
    
    List<GrantedAuthority> getGrantedAuthorities(List<String> rolesOrPrivileges);

    Optional<UserDetails> getLoggedInUser();

    UserDetails getLoggedInUser(UserDetails resultIfNone);

//    List<String> getPrivileges(Collection<Role> roles);

//    List<String> getRolesAndPrivileges(Collection<Role> roles);
}