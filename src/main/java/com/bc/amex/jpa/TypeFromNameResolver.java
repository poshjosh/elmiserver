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

import java.lang.reflect.InvocationTargetException;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 3:03:11 PM
 */
public interface TypeFromNameResolver {
    
    default Class getType(String name) {

        final Class type = getType(name, (Class)null);
        
        if(type == null) {
            throw new RuntimeException("Failed to resolve name: " + 
                    name + ", to a Class");
        }
        
        return type;
    }
    
    Class getType(String name, Class resultIfNone);

    default Object newInstance(String name) {
        return newInstance(TypeFromNameResolver.this.getType(name)); 
    }

    default String getName(Class type) {
        return type.getSimpleName();
    }

    default Object newInstance(Class type) {
        try{
            return type.getConstructor().newInstance();
        }catch(NoSuchMethodException | SecurityException | InstantiationException | 
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
