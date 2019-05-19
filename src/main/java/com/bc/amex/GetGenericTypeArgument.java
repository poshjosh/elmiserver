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

import com.bc.reflection.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 17, 2019 1:55:53 AM
 */
public class GetGenericTypeArgument {

    private static final Logger LOG = LoggerFactory.getLogger(GetGenericTypeArgument.class);

    public Class apply(Field field, Class resultIfNone) {
        
        final Class type = field.getType();
    
        if(Collection.class.isAssignableFrom(type)) {
        
            final ReflectionUtil reflection = new ReflectionUtil();
            
            final Type [] typeArgs = reflection.getGenericTypeArguments(field);
            
            if(typeArgs != null && typeArgs.length == 1) {
            
                final Class genericTypeArgument = (Class)typeArgs[0];
                
                LOG.trace("Class: {}, generic type argument:{}", type, genericTypeArgument);
                
                return genericTypeArgument;
                
            }else{
                
                return resultIfNone;
            }
        }else{
        
            return resultIfNone;
        }
    }
}
