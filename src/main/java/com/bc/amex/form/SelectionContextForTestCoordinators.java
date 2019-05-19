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
import com.bc.amex.jpa.repository.UserRepository;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:38:45 PM
 */
public class SelectionContextForTestCoordinators extends SelectionContextImpl{

    private final Test test;
    
    public SelectionContextForTestCoordinators(EntityRepositoryFactory entityRepositoryFactory, Test test) {
        super(entityRepositoryFactory);
        this.test = Objects.requireNonNull(test);
    }

    @Override
    public List getEntityList(Class type) {
        
        if(User.class.isAssignableFrom(type)) {
            
            final UserRepository userRepo = (UserRepository)this.getEntityRepositoryFactory().forEntity(type);
            
            return userRepo.getPossibleTestCoordinators(test);
        }
        
        return super.getEntityList(type); 
    }

    @Override
    public List<Class> getSelectionTypes() {
        final List<Class> output = new ArrayList(super.getSelectionTypes());
        output.add(User.class);
        return output;
    }

}
