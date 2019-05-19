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

import com.bc.reflection.ReflectionUtil;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 4:57:45 PM
 */
public abstract class AbstractEntityTypeResolver implements TypeFromNameResolver{

    @Override
    public Class getType(String entityName) {

        final Class type = getType(entityName, (Class)null);
        
        if(type == null) {
            throw new RuntimeException("Failed to resolve name: " + 
                    entityName + ", to a Class");
        }
        
        return type;
    }

    @Override
    public Object newInstance(String entityName) {
        return newInstance(getType(entityName)); 
    }

    @Override
    public String getName(Class entityType) {
        return entityType.getSimpleName();
    }
    
    @Override
    public Object newInstance(Class entityType) {
        return new ReflectionUtil().newInstance(entityType);
    }
}
