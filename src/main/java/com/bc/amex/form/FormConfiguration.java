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

import com.bc.amex.converters.DateAndTimePatternsSupplier;
import com.bc.amex.converters.DateAndTimePatternsSupplierImpl;
import com.bc.amex.form.validators.EntityUniqueColumnsValidator;
import com.bc.amex.form.validators.FormValidators;
import com.bc.amex.form.validators.FormValidatorsImpl;
import com.bc.amex.jpa.TypeFromNameResolver;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.db.meta.access.MetaDataAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 9:53:09 PM
 */
@Lazy
@Configuration
public class FormConfiguration {

    @Autowired private Environment environment;
    @Autowired private TypeFromNameResolver typeNameResolver;
    
    @Bean @Scope("prototype") DateAndTimePatternsSupplier dateAndTimePatternsSupplier() {
        return new DateAndTimePatternsSupplierImpl(environment, typeNameResolver);
    }
    
    @Bean @Scope("prototype") FormValidators formValidators() {
        return new FormValidatorsImpl();
    }
    
    @Bean @Scope("prototype") EntityUniqueColumnsValidator uniqueColumnsValidator(
            MetaDataAccess metaDataAccess, EntityRepositoryFactory entityRepositoryFactory) {
        return new EntityUniqueColumnsValidator(metaDataAccess, entityRepositoryFactory);
    }
    
    @Bean @Scope("prototype") public IsTestCoordinatorsForm isTestCoordinatorsForm() {
        return new IsTestCoordinatorsForm();
    }
    
    @Bean @Scope("prototype") public DefaultParametersProvider defaultParametersProvider() {
        return new DefaultParametersProviderImpl();
    }

    @Bean @Scope("prototype") public EntityFormProvider entityFormProvider() {
        return new EntityFormProviderImpl();
    }

    @Bean @Scope("prototype") public FormPropertyAccess formPropertyAccess() {
        return new FormPropertyAccessImpl(environment, typeNameResolver);
    }
}
