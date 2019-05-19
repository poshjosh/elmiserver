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

import com.bc.amex.jpa.DefaultRoleSupplier;
import com.bc.elmi.pu.entities.User;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.functions.GetTableNameFromAnnotation;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 2:05:27 PM
 */
public class EntityRepositoryFactoryImpl implements EntityRepositoryFactory{

    private static final Logger LOG = LoggerFactory.getLogger(EntityRepositoryFactoryImpl.class);
    
    private final JpaObjectFactory jpa;
    
    @Lazy @Autowired private PasswordEncoder passwordEncoder;
    @Lazy @Autowired private DefaultRoleSupplier defaultRoleSupplier;

    public EntityRepositoryFactoryImpl(JpaObjectFactory jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }
    
    @Override
    public boolean isSupported(Class entityType) {
        try{
            new GetTableNameFromAnnotation().apply(entityType);
            return true;
        }catch(RuntimeException ignored) {
            LOG.trace("Failed to resolve table name from type: {}", entityType);
            return false;
        }
    }
    
    @Override
    public EntityRepository forEntity(Class entityType) {
        
        if(User.class.isAssignableFrom(entityType)) {
            return new UserRepository(jpa, passwordEncoder, defaultRoleSupplier);
        }else{
            return new EntityRepositoryImpl(jpa, entityType);
        }
    }
}
