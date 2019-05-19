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

package com.bc.amex.form;

import com.bc.amex.jpa.TypeFromNameResolver;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 9:15:32 AM
 */
public class FormPropertyAccessImpl implements Serializable, FormPropertyAccess {

    private static final Logger LOG = LoggerFactory.getLogger(FormPropertyAccessImpl.class);

    private final Environment env;

    private final TypeFromNameResolver tnr;
    
    public FormPropertyAccessImpl(Environment env, TypeFromNameResolver tnr) {
        this.env = Objects.requireNonNull(env);
        this.tnr = Objects.requireNonNull(tnr);
    }
    
    @Override
    public Optional<String> getProperty(Field field, String suffix) {
        final Optional<String> output = this.getProperty(field.getDeclaringClass(), field.getName(), suffix);
        LOG.trace("Suffix: {}, property: {}, field: {}", suffix, output, field);
        return output;
    }
    
    @Override
    public Optional<String> getProperty(Class type, String name, String suffix) {
        final String prefix = "form." + suffix + '.';
        String key = prefix + type.getName() + '.' + name;
        String val = getProperty(key);
        if(val == null) {
            key = prefix + tnr.getName(type) + '.' + name;
            val = getProperty(key);
        }
        if(val == null) {
            key = prefix + type.getSimpleName() + '.' + name;
            val = getProperty(key);
        }
        if(val == null) {
            return this.getProperty(name, suffix);
        }else{
            return Optional.ofNullable(val);
        }
    }

    @Override
    public Optional<String> getProperty(String name, String suffix) {
        final String key = "form." + suffix + '.' + name;
        final String val = getProperty(key);
        if(val == null) {
            return getPropertyWithSuffix(suffix);
        }else{
            return Optional.ofNullable(val);
        }
    }

    @Override
    public Optional<String> getPropertyWithSuffix(String suffix) {
        final String key = "form." + suffix;
        final String val = getProperty(key);
        return Optional.ofNullable(val);
    }

    public String getProperty(String key) {
        final String val = env.getProperty(key);
        LOG.trace("{} = {}", key, val);
        return val;
    }
}
