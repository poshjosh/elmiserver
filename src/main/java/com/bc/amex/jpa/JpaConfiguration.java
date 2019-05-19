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

package com.bc.amex.jpa;

import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.amex.jpa.repository.EntityRepositoryFactoryImpl;
import com.bc.amex.jpa.repository.UserRepository;
import com.bc.amex.jpa.selectors.SelectorFactory;
import com.bc.amex.jpa.selectors.SelectorFactoryImpl;
import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.MetaDataAccessImpl;
import com.bc.elmi.pu.PersistenceUnit;
import com.bc.elmi.pu.entities.Role;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.JpaObjectFactoryImpl;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreator;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreatorImpl;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.jpa.dao.sql.SQLDateTimePatterns;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2019 11:31:20 AM
 */
@Configuration
public class JpaConfiguration {
    
    @Bean public SelectorFactory selectorFactory() {
        return new SelectorFactoryImpl();
    }
    
    @Lazy @Bean @Scope("prototype") public DefaultRoleSupplier defaultRoleSupplier(
            EntityRepositoryFactory entityRepoFactory) {
        return new DefaultRoleSupplierImpl(entityRepoFactory.forEntity(Role.class));
    }
    
    @Bean @Scope("prototype") public UserRepository userRepository(JpaObjectFactory jpa,
            PasswordEncoder passwordEncoder, DefaultRoleSupplier defaultRoleSupplier) {
        return new UserRepository(jpa, passwordEncoder, defaultRoleSupplier);
    }

    @Bean public GetMimetypesForFilename getMimetypesForFilename() {
        return new GetMimetypesForFilenameImpl();
    }
    
    @Bean @Scope("prototype") public TypeFromNameResolver entityTypeResolver() {
        return new TypeFromNameResolverComposite(
                new TypeFromNameResolverUsingPersistenceXmlFile(),
                //@todo make thix a property
                new TypeFromNameResolverUsingPackageNames("com.bc.amex.entities", "com.bc.docusys.pu.forms")
        );
    }
    
    @Bean public EntityRepositoryFactory entityServiceProvider(JpaObjectFactory jpa) {
        return new EntityRepositoryFactoryImpl(jpa);
    }
    
    @Bean @Scope("prototype") public MetaDataAccess metaDataAccess(JpaObjectFactory jpa) {
        return new MetaDataAccessImpl(jpa.getEntityManagerFactory());
    }
    
    @Bean public JpaObjectFactory jpaObjectFactory(
            EntityManagerFactoryCreator emfCreator,
            SQLDateTimePatterns sqlDateTimePatterns) {
        //@todo make a property
        return new JpaObjectFactoryImpl(PersistenceUnit.NAME, emfCreator, sqlDateTimePatterns);
    }
    
    @Bean @Scope("prototype") public SQLDateTimePatterns sqlDateTimePatterns(JdbcProperties jdbcProperties) {
        // @todo parse properties and determine if driver is: myql, postgresql etc
        // and then return the corresponding datetimepatterns object
        return new MySQLDateTimePatterns();
    }
    
    @Bean public EntityManagerFactoryCreator entityManagerFactoryCreator(
            JdbcPropertiesProvider jdbcPropertiesProvider) {
        return new EntityManagerFactoryCreatorImpl(jdbcPropertiesProvider);
    }
    
    @Bean public JdbcPropertiesProvider JdbcPropertiesProvider(JdbcProperties jdbcProperties) {
        return new JdbcPropertiesProvider(jdbcProperties);
    }
}
