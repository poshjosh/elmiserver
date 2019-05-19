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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 4:56:21 PM
 */
public class TypeFromNameResolverUsingClassNames extends AbstractEntityTypeResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TypeFromNameResolverUsingClassNames.class);

    private final List<String> classNames;

    public TypeFromNameResolverUsingClassNames(List<String> classNames) {
        this.classNames = Objects.requireNonNull(classNames);
        LOG.trace("Class names: {0}", classNames);
    }
    
    @Override
    public Class getType(String entityName) {

        final Class type = getType(entityName, (Class)null);
        
        if(type == null) {
            throw new RuntimeException("Failed to find class named: " + 
                    entityName + ", in: " + this.classNames);
        }
        
        return type;
    }
    
    @Override
    public Class getType(String entityName, Class resultIfNone) {
        
        final Set<String> foundNames = this.classNames.stream().filter((name) -> name.endsWith("." + entityName)).collect(Collectors.toSet());
        
        LOG.debug("For name: {}, found matching class names: {}", entityName, foundNames);
        
        final String typeName;
        if(foundNames.isEmpty()) {
            typeName = null;
        }else if(foundNames.size() > 1) {
            LOG.warn("For: {}, multiple class names found: {}", entityName, foundNames);
            typeName = null;
        }else{
            typeName = foundNames.iterator().next();
        }
        Class output = null;
        if(typeName != null) {
            try{
                output = Class.forName(typeName);
            }catch(Exception e) {
                LOG.warn("Exception loading: " + typeName, e);
            }
        }
        return output == null ? resultIfNone : output;
    }
}
