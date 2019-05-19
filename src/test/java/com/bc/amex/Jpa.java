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

import com.bc.elmi.pu.PersistenceUnit;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.JpaObjectFactoryImpl;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreator;
import com.bc.jpa.dao.functions.EntityManagerFactoryCreatorImpl;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.jpa.dao.sql.SQLDateTimePatterns;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on May 1, 2019 8:28:35 PM
 */
public class Jpa {
    
    private static JpaObjectFactory jpaObjectFactory;
    
    public static JpaObjectFactory getObjectFactory() {
        
        if(jpaObjectFactory == null) {
            final Jpa jpa = new Jpa();
            final Function<String, Properties> pp = jpa.jdbcPropertiesProvider();
            final EntityManagerFactoryCreator emfc = jpa.entityManagerFactoryCreator(pp);

            jpaObjectFactory = jpa.jpaObjectFactory(emfc, jpa.sqlDateTimePatterns());
        }
        
        return jpaObjectFactory;
    }

    public JpaObjectFactory jpaObjectFactory(
            EntityManagerFactoryCreator emfCreator,
            SQLDateTimePatterns sqlDateTimePatterns) {
        //@todo make a property
        return new JpaObjectFactoryImpl(PersistenceUnit.NAME, emfCreator, sqlDateTimePatterns);
    }
    
    public SQLDateTimePatterns sqlDateTimePatterns() {
        // @todo parse properties and determine if driver is: myql, postgresql etc
        // and then return the corresponding datetimepatterns object
        return new MySQLDateTimePatterns();
    }
    
    public EntityManagerFactoryCreator entityManagerFactoryCreator(
            Function<String, Properties> jdbcPropertiesProvider) {
        return new EntityManagerFactoryCreatorImpl(jdbcPropertiesProvider);
    }
    
    public Function<String, Properties> jdbcPropertiesProvider() {
        return new JdbcPropertiesProvider();
    }

    private static class JdbcPropertiesProvider implements Function<String, Properties>{
        public JdbcPropertiesProvider() { }
        @Override
        public Properties apply(String persistenceUnitName) {
            final Properties properties = new Properties();
            //@todo prompt user to enter these on first startup
            properties.setProperty("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/elmi?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC");
            properties.setProperty("javax.persistence.jdbc.user", "root");
            properties.setProperty("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
            properties.setProperty("javax.persistence.jdbc.password", "Jesus4eva-");
            properties.setProperty("eclipselink.logging.level.sql", "FINE");
            properties.setProperty("eclipselink.logging.parameters", "true");
            properties.setProperty("eclipselink.logging.timestamp", "false");
            properties.setProperty("eclipselink.logging.session", "false");
            properties.setProperty("eclipselink.logging.thread", "false");
            return properties;
        }
    }
}
