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

import com.bc.elmi.pu.entities.Gender;
import com.bc.elmi.pu.entities.Unit;
import com.bc.elmi.pu.entities.Messagestatus;
import com.bc.elmi.pu.entities.Messagetype;
import com.bc.elmi.pu.entities.Mimetype;
import com.bc.elmi.pu.entities.Permission;
import com.bc.elmi.pu.entities.Role;
import com.bc.elmi.pu.entities.Appointment;
import com.bc.elmi.pu.entities.Userstatus;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.elmi.pu.entities.Course;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 12:51:14 AM
 */
public class SelectionContextImpl implements SelectionContext{

    private static final Logger LOG = LoggerFactory.getLogger(SelectionContextImpl.class);

    private final EntityRepositoryFactory entityRepositoryFactory;
    
    public SelectionContextImpl(EntityRepositoryFactory entityRepositoryFactory) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }

    @Override
    public List<Class> getSelectionTypes() {
        return Arrays.asList(Appointment.class, Course.class, Gender.class, Messagestatus.class, Messagetype.class,
                Mimetype.class, Permission.class, Role.class, Unit.class, Userstatus.class);
    }

    @Override
    public Object getValueForSelection(Class type, Object selectedOption) {
        return entityRepositoryFactory.forEntity(type).find(selectedOption);
    }

    @Override
    public Map getSelectionOptions(Class type) {
        
        if(this.isSelectionType(type)) {
            
            final List entityList = getEntityList(type);
            
            final Map result = new LinkedHashMap(entityList.size());

//            final UnaryOperator<String> toTitleCase = (s) -> Character.toTitleCase(s.charAt(0)) + s.substring(1);

            final String simpleName = type.getSimpleName();
  
            for(Object entity : entityList) {

                try{

                    // gender -> getGenderid()
                    final Method idmethod = entity.getClass().getMethod(
                            "get" + simpleName + "id");
                    final Object key = idmethod.invoke(entity);

                    // gender -> getGendername()
                    final Method namemethod = entity.getClass().getMethod(
                            "get" + simpleName + "name");
                    final Object val = namemethod.invoke(entity);

                    result.put(key, val);

                }catch(Exception e) {

                    LOG.warn(null, e);
                }
            }

            return result;
            
        }else{
            
            return Collections.EMPTY_MAP;
        }
    }
    
    public List getEntityList(Class type) {
        return entityRepositoryFactory.forEntity(type).findAll();
    }

    public EntityRepositoryFactory getEntityRepositoryFactory() {
        return entityRepositoryFactory;
    }
}
