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

package com.bc.amex.controllers;

import static com.bc.amex.controllers.CrudController.CREATE;
import static com.bc.amex.controllers.CrudController.DELETE;
import static com.bc.amex.controllers.CrudController.READ;
import static com.bc.amex.controllers.CrudController.UPDATE;
import com.bc.amex.form.FormConfig;
import com.bc.amex.jpa.TypeFromNameResolver;
import com.bc.amex.jpa.repository.EntityRepository;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 9:23:51 PM
 */
public class OnCrudFormSubmitted implements CrudController.OnFormSubmitted{

//    private static final Logger LOG = LoggerFactory.getLogger(OnCrudFormSubmitted.class.getName());

    private final TypeFromNameResolver entityTypeResolver;
    private final EntityRepositoryFactory entityRepositoryFactory;
            
    public OnCrudFormSubmitted(
            @Autowired TypeFromNameResolver entityTypeResolver, 
            @Autowired EntityRepositoryFactory entityRepositoryFactory) {
        this.entityTypeResolver = Objects.requireNonNull(entityTypeResolver);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public void onFormSubmitted(FormConfig formConfig) {
                    
        final Class entityType = entityTypeResolver.getType(formConfig.getModelname());
        final EntityRepository repo = entityRepositoryFactory.forEntity(entityType);
        switch(formConfig.getAction()) {
            case CREATE:
                final Object modelobject = formConfig.getModelobject();
                repo.create(modelobject);
                break;
            case READ:
                break;
            case UPDATE:
                repo.update(getModelObject(repo, formConfig));
                break;
            case DELETE:
                final Object id = formConfig.getModelid();
                repo.deleteById(id);
                break;
            default:
                throw new IllegalArgumentException("Unexpected action: " + formConfig.getAction());
        }   
    }
    
    public Object getModelObject(EntityRepository repo, FormConfig formConfig) {
        return formConfig.getModelobject() != null ? 
                formConfig.getModelobject() :
                repo.find(formConfig.getModelid());
    }
}
