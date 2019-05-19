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

import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 1:24:41 PM
 */
public class JdbcPropertiesProvider implements Function<String, Properties>{

    private final JdbcProperties jdbcProperties;

    public JdbcPropertiesProvider(JdbcProperties jdbcProperties) {
        this.jdbcProperties = Objects.requireNonNull(jdbcProperties);
    }

    @Override
    public Properties apply(String persistenceUnitName) {
        final Properties properties = new Properties();
        //@todo prompt user to enter these on first startup
        properties.setProperty("javax.persistence.jdbc.url", this.jdbcProperties.getUrl());
        properties.setProperty("javax.persistence.jdbc.user", this.jdbcProperties.getUser());
        properties.setProperty("javax.persistence.jdbc.driver", this.jdbcProperties.getDriver());
        properties.setProperty("javax.persistence.jdbc.password", this.jdbcProperties.getPassword());
        properties.setProperty("eclipselink.logging.level.sql", "FINE");
        properties.setProperty("eclipselink.logging.parameters", "true");
        properties.setProperty("eclipselink.logging.timestamp", "false");
        properties.setProperty("eclipselink.logging.session", "false");
        properties.setProperty("eclipselink.logging.thread", "false");
        return properties;
    }
}
