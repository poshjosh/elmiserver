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

import com.bc.amex.jpa.repository.EntityRepository;
import com.bc.elmi.pu.entities.Role;
import com.bc.elmi.pu.entities.Role_;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 10:42:45 PM
 */
public class DefaultRoleSupplierImpl implements DefaultRoleSupplier{

    private final EntityRepository<Role> roleRepo;

    public DefaultRoleSupplierImpl(EntityRepository<Role> roleRepo) {
        this.roleRepo = Objects.requireNonNull(roleRepo);
    }
    
    @Override
    public Role get() {

        // @todo make a property
        final Role role = roleRepo.findSingleBy(Role_.rolename, "USER");
        
        return Objects.requireNonNull(role);
    }
}
