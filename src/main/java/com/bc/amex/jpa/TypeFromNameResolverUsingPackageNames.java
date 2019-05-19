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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 3:02:44 PM
 */
public class TypeFromNameResolverUsingPackageNames extends AbstractEntityTypeResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TypeFromNameResolverUsingPackageNames.class);
    
    private final List<String> packageNames;

    public TypeFromNameResolverUsingPackageNames(String... packageNames) {
        this(Arrays.asList(packageNames));
    }
    
    public TypeFromNameResolverUsingPackageNames(List<String> packageNames) {
        this.packageNames = Objects.requireNonNull(packageNames);
    }
    
    @Override
    public Class getType(String entityName) {

        final Class type = getType(entityName, (Class)null);
        
        if(type == null) {
            throw new RuntimeException("Failed to find class named: " + entityName + 
                    ", in packages: " + packageNames);
        }
        
        return type;
    }
    
    @Override
    public Class getType(String entityName, Class resultIfNone) {
        
        for(String packageName : packageNames) {
        
            final Class type = getEntityType(packageName, entityName);
            
            if(type != null) {
                
                return type;
            }
        }
        
        return resultIfNone;
    }

    public Class getEntityType(String packageName, String entityName) {
        
        final String className = packageName + '.' + entityName;
        
        try{
            return Class.forName(className);
        }catch(Exception ignored) {
            
            LOG.debug("Failed to load class: {}, which was resolved to package name: {}",
                    className, packageName);
            
            return null;
        }
    }
}
