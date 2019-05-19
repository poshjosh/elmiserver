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

package com.bc.amex.form.validators;

import static com.bc.amex.controllers.CrudActionNames.CREATE;
import com.bc.amex.form.FormConfig;
import com.bc.amex.form.IsTestCoordinatorsForm;
import com.bc.elmi.pu.entities.Test_;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Validator;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 9:49:31 PM
 */
public class FormValidatorsImpl implements FormValidators {

    @Autowired private EntityUniqueColumnsValidator uniqueColumnsValidator;
    @Autowired private IsTestCoordinatorsForm isTestCoordinatorsForm;
    
    @Override
    public List<Validator> get(FormConfig formConfig) {
        
        final List<Validator> output = new ArrayList<>(2);
        
        if(CREATE.equals(formConfig.getAction())) {
            
            output.add(uniqueColumnsValidator);
            
            if(isTestCoordinatorsForm.test(formConfig)) {
            
                final Validator validator = new NonEmptyCollectionValidator(Test_.userList.getName());
                
                output.add(validator);
            }
        }
        
        return Collections.unmodifiableList(output);
    }
}
