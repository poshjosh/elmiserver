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

import com.bc.amex.jpa.repository.EntityRepository;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.db.meta.access.MetaDataAccess;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 5:07:34 PM
 */
public class EntityUniqueColumnsValidator implements Validator {

    private static final Logger LOG = LoggerFactory.getLogger(EntityUniqueColumnsValidator.class);

    private final MetaDataAccess metaDataAccess;
    private final EntityRepositoryFactory entityRepositoryFactory;

    public EntityUniqueColumnsValidator(@Autowired MetaDataAccess metaDataAccess, 
            @Autowired EntityRepositoryFactory entityRepositoryFactory) {
        this.metaDataAccess = Objects.requireNonNull(metaDataAccess);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return this.entityRepositoryFactory.isSupported(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {

        final Class entityType = object.getClass();
        
        final EntityRepository repo = entityRepositoryFactory.forEntity(entityType);
        
        final String tableName = repo.getTableName();

        final Collection<String> uniqueColumns = getUniqueColumns(tableName);
        
        for(String uniqueCol : uniqueColumns) {

            final Object columnValue = errors.getFieldValue(uniqueCol);

            if(columnValue != null && !columnValue.toString().isEmpty() && 
                    repo.existsBy(uniqueCol, columnValue)) {
                //@todo provide messages.properties or validation_messages.properties
                errors.rejectValue(uniqueCol, "Duplicate." + uniqueCol, "`" + uniqueCol + "` already exists");
                
            }
        }
    }
    
    public Collection<String> getUniqueColumns(String tableName) {

        final Set<String> uniqueColumns;

        final List<String> indexNames = metaDataAccess.fetchStringIndexInfo(tableName, true, MetaDataAccess.INDEX_NAME);
        if(indexNames == null || indexNames.isEmpty()) {
            uniqueColumns = Collections.EMPTY_SET;
        }else{
            final List<String> columnNames = metaDataAccess.fetchStringMetaData(tableName, MetaDataAccess.COLUMN_NAME);
            uniqueColumns = indexNames.stream().filter((name) -> columnNames.contains(name)).collect(Collectors.toSet());
        }

        LOG.trace("Table: {}, unique columns: {}", tableName, uniqueColumns);

        return uniqueColumns;
    }
}
