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

package com.bc.amex.jpa.repository;

import com.bc.amex.exceptions.LoginException;
import com.bc.amex.jpa.DefaultRoleSupplier;
import com.bc.elmi.pu.entities.Appointment;
import com.bc.elmi.pu.entities.Appointment_;
import com.bc.elmi.pu.entities.Role;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.Unit;
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import com.bc.elmi.pu.enums.UserstatusEnum;
import com.bc.jpa.dao.Criteria;
import com.bc.jpa.dao.JpaObjectFactory;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 10:44:56 AM
 */
public class UserRepository extends EntityRepositoryImpl<User> {

    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

    private final PasswordEncoder passwordEncoder;
    private final DefaultRoleSupplier defaultRoleSupplier;
    
    public UserRepository(
            @Autowired JpaObjectFactory jpa, 
            @Autowired PasswordEncoder passwordEncoder,
            @Autowired DefaultRoleSupplier defaultRoleSupplier) {
        super(jpa, User.class);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.defaultRoleSupplier = Objects.requireNonNull(defaultRoleSupplier);
    }

    @Override
    protected void preCreate(User user) {
        
        final String password = user.getPassword();
        Objects.requireNonNull(password);
        user.setPassword(passwordEncoder.encode(password));
        
        if(user.getRoleList() == null || user.getRoleList().isEmpty()) {
            final List<Role> userRoles = new ArrayList<>(1);
            userRoles.add(defaultRoleSupplier.get());
            user.setRoleList(userRoles);
        }
        
        super.preCreate(user);
    }
    
    @Override
    protected void preUpdate(User user) {
        final String password = user.getPassword();
        Objects.requireNonNull(password);
        
        final Integer id = user.getUserid();
        Objects.requireNonNull(id);
        
        final User found = this.find(id);
        if( ! found.getPassword().equals(password)) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        super.preUpdate(user);
   }

//    public User findByUsername(String username) {
//        return this.getDaoForSelect().where(User_.username, username).getSingleResultAndClose();
//    }

//    public User findByEmailaddress(String emailaddress) {
//        return this.getDaoForSelect().where(User_.emailaddress, emailaddress).getSingleResultAndClose();
//    }
    
    public List<User> getPossibleTestCoordinators(Test test) {
        
        final List<Unit> unitList = test.getUnitList();
        
        final JpaObjectFactory jpa = this.getJpaObjectFactory();
        
        //@todo make this a property and provide form for changing it appropriately
        final List<Appointment> apptList = jpa.getDaoForSelect(Appointment.class)
                .where(Appointment_.appointmentname, Criteria.LIKE, "TC")
                .or().where(Appointment_.appointmentname, Criteria.LIKE, "DS")
                .getResultsAndClose();

        LOG.debug("Test: {}\nApplicable to units/groups: {}\nAppointments that may coordinate: {}",
                test.getTestname(), 
                (unitList == null ? null : unitList.stream().map((unit) -> unit.getUnitname()).collect(Collectors.joining(", "))),
                (apptList == null ? null : apptList.stream().map((appt) -> appt.getAppointmentname()).collect(Collectors.joining(", ")))
        );
        
        final List<User> userList;
        
        if((unitList == null || unitList.isEmpty()) && 
                (apptList == null || apptList.isEmpty())) {
        
            userList = Collections.EMPTY_LIST;
            
        }else{

            userList = jpa.getDaoForSelect(User.class)
                .where(User_.unit, unitList)
                .and().where(User_.appointment, apptList)
                .getResultsAndClose();
        }
        
        LOG.debug("Test: {}, Users who may coordinate: {}", test.getTestname(),
                (userList == null ? null : userList.stream().map((user) -> user.getUsername()).collect(Collectors.joining(", ")))
        );
        
        return userList;
    }

    public User getUser(Principal principal) throws LoginException{

        final User user = getUser(principal, null);
        
        if(user == null) {
            throw new LoginException("You must be logged in to perform the requested operation");
        }
        
        return user;
    }
    
    public User getUser(Principal principal, User outputIfNone) {
    
        final String currentUsername = principal == null ? null : principal.getName();

        final User output;
        if(currentUsername == null || currentUsername.isEmpty()) {
            output = null;
        }else{
            output = findSingleBy(User_.username, currentUsername, null);
        }

        return output == null ? outputIfNone : output;
    }

    public boolean isLoggedIn(Principal principal) {
    
        final String currentUsername = principal == null ? null : principal.getName();

        return currentUsername != null && ! currentUsername.isEmpty();
    }

    public boolean isEnabled(User user) {
//        final Userstatus userstatus = this.getJpaObjectFactory().getDao()
//                .find(Userstatus.class, UserstatusEnum.Activated.getId());
//        return userstatus.equals(user.getUserstatus());
        return user.getUserstatus().getUserstatusid().equals(UserstatusEnum.Activated.getId());
    }
}
