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

import com.bc.amex.GetGenericTypeArgument;
import com.bc.elmi.pu.entities.Appointment_;
import com.bc.elmi.pu.entities.Test_;
import com.bc.web.form.functions.AnnotatedPersistenceFieldIsFormFieldTest;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 17, 2019 12:22:42 AM
 */
public class IsFormField extends AnnotatedPersistenceFieldIsFormFieldTest{

    private static final Logger LOG = LoggerFactory.getLogger(IsFormField.class);
    
    private final SelectionContext selectionContext;
    
    public IsFormField(SelectionContext selectionContext) {
        this.selectionContext = Objects.requireNonNull(selectionContext);
    }
    
    @Override
    public boolean test(Field field) {

        final boolean output;

        final Object fieldName = field.getName();
        if(Test_.otherdetails.getName().equals(fieldName)) {
            output = false;
        }else if(Appointment_.timecreated.getName().equals(fieldName) ||
                Appointment_.timemodified.getName().equals(fieldName)) {
            output = false;
        }else{
            final Class type = field.getType();
            if(Collection.class.isAssignableFrom(type)) {
            
                final Class genericTypeArgument = new GetGenericTypeArgument().apply(field, null);
                
                if(genericTypeArgument != null) {
                    
                    if(genericTypeArgument.equals(field.getDeclaringClass())) {
                        
                        output = false; 
                        
                    }else{
                    
                        output = this.selectionContext.isSelectionType(genericTypeArgument);
                    }
                }else{
                    output = false;
                }    
            }else{
                
                output = super.test(field);
            }
        }

        LOG.trace("Is form field: {}, field: {}", output, field);

        return output;
    }
}
