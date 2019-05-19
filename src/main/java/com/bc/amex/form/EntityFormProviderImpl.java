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

import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.elmi.pu.entities.Test;
import com.bc.web.form.DefaultForm;
import com.bc.web.form.Form;
import com.bc.web.form.PreferMandatoryField;
import com.bc.web.form.functions.FormFieldsCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 7:13:36 PM
 */
public class EntityFormProviderImpl implements EntityFormProvider{

    private static final Logger LOG = LoggerFactory.getLogger(EntityFormProviderImpl.class);

    @Autowired private EntityRepositoryFactory entityRepositoryFactory;
    @Autowired private FormPropertyAccess formPropertyAccess;
    @Autowired private IsTestCoordinatorsForm isTestCoordinatorsForm;
    
    @Override
    public Form apply(FormConfig formConfig) {
        final String entityName = formConfig.getModelname();
        final Object entity = formConfig.getModelobject();
        final Form form = new Form.Builder()
                .apply(new DefaultForm(entityName))
                .fieldsCreator(getFormFieldsCreator(formConfig))
                .fieldsComparator(new PreferMandatoryField())
                .fieldDataSource(format(entity))
                .build();
        LOG.debug("Entity name: {}, form: {}", entityName, form);
        return form;
    }
    
    public FormFieldsCreator getFormFieldsCreator(FormConfig formConfig) {
        
        final Object entity = formConfig.getModelobject();
//        if(entity instanceof User) {
//            return new FormFieldsCreatorForUser(entityRepositoryFactory, getSelectionContext(formConfig), formPropertyAccess);
//        }

        if(entity instanceof Test) {
              return new FormFieldsCreatorForTest(entityRepositoryFactory, 
                      getSelectionContext(formConfig), formPropertyAccess);  
        }
        
        return new FormFieldsCreatorImpl(
                entityRepositoryFactory, 
                getSelectionContext(formConfig), 
                formPropertyAccess);
    }
    
    public SelectionContext getSelectionContext(FormConfig formConfig) {
        
        if(isTestCoordinatorsForm.test(formConfig)) {
            
            return new SelectionContextForTestCoordinators(entityRepositoryFactory, (Test)formConfig.getModelobject());
        }
        
        return new SelectionContextImpl(entityRepositoryFactory);
    }
    
    public Object format(Object entity) {
        return entity;
    }
}
