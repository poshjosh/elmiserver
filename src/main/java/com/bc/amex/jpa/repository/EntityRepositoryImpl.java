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

package com.bc.amex.jpa.repository;

import com.bc.db.meta.access.MetaDataAccess;
import com.bc.db.meta.access.MetaDataAccessImpl;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.Delete;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.Select;
import com.bc.jpa.dao.Update;
import com.bc.jpa.dao.functions.GetTableNameFromAnnotation;
import com.bc.jpa.dao.sql.SQLUtils;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 1:54:23 PM
 */
public class EntityRepositoryImpl<E> implements EntityRepository<E> {

    private static final Logger LOG = LoggerFactory.getLogger(EntityRepositoryImpl.class);

    private final JpaObjectFactory jpaObjectFactory;
    
    private final Class<E> entityType;
    
    private final String tableName;
    
    private final List<String> columnNames;
            
    private final String primaryColumnName;
    
    private final Class primaryColumnType;

    public EntityRepositoryImpl(JpaObjectFactory jpa, Class<E> entityType) {
        this.jpaObjectFactory = Objects.requireNonNull(jpa);
        this.entityType = Objects.requireNonNull(entityType);
        this.tableName = new GetTableNameFromAnnotation().apply(entityType);
        final MetaDataAccess mda = new MetaDataAccessImpl(this.jpaObjectFactory.getEntityManagerFactory());
        //@todo primary column may not be the first column
        final int primaryColumnIndex = 0;
        this.columnNames = mda.fetchStringMetaData(tableName, MetaDataAccess.COLUMN_NAME);
        this.primaryColumnName = columnNames.get(primaryColumnIndex);
        final int idType = mda.fetchColumnDataTypes(tableName)[primaryColumnIndex];
        this.primaryColumnType = SQLUtils.getClass(idType, Object.class);
        
        LOG.debug("Entity type: {}, primary column type: {}", entityType, primaryColumnType);
    }

    protected void preCreate(E entity){
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        final Date date = new Date();
        String key = com.bc.elmi.pu.entities.Document_.timecreated.getName();
        if(bean.isReadableProperty(key) && bean.isWritableProperty(key)) {
            LOG.debug("Setting {} = {}", key, date);
            bean.setPropertyValue(key, date);
        }
        key = com.bc.elmi.pu.entities.Document_.timemodified.getName();
        if(bean.isReadableProperty(key) && bean.isWritableProperty(key)) {
            LOG.debug("Setting {} = {}", key, date);
            bean.setPropertyValue(key, date);
        }
    }
    
    protected void preUpdate(E entity){
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        final Date date = new Date();
        final String key = com.bc.elmi.pu.entities.Document_.timemodified.getName();
        if(bean.isReadableProperty(key) && bean.isWritableProperty(key)) {
            LOG.debug("Setting {} = {}", key, date);
            bean.setPropertyValue(key, date);
        }
    }

    @Override
    public String getTableName() {
        return tableName;
    }
    
    @Override
    public List<E> search(String query) {
        return jpaObjectFactory.getTextSearch().search(entityType, query);
    }
    
    @Override
    public Optional getIdOptional(Object entity) {

        Object id = getBeanIdValue(entity);
        
        LOG.debug("Id: {}, from entity: {}", id, entity);
        
        if(id == null) {
        
            final Object fromDb = jpaObjectFactory.getDao().merge(entity);
            
            LOG.debug("Merged entity: {}", fromDb);
            
            id = getBeanIdValue(fromDb);

            LOG.debug("Id: {}, from merged entity: {}", id, fromDb);
        }
        
        if(id == null) {
            
            final Collection found = jpaObjectFactory.getTextSearch().searchEntityRecords(
                    entity, Number.class, CharSequence.class, Date.class);
            
            if(found.size() == 1) {
                
                final Object obj = found.iterator().next();
                
                id = getBeanIdValue(obj);

                LOG.debug("Id: {}, from searched entity: {}", id, obj);
            }
        }
        
        return Optional.ofNullable(id);
    }
    
    public Object getBeanIdValue(Object entity) {

        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);

        final Object id = bean.isReadableProperty(primaryColumnName)
                ? bean.getPropertyValue(primaryColumnName) : null;
        
        return id;
    }

    @Override
    public boolean exists(Object id) {
        try{
            final Object found = jpaObjectFactory.getDao().find(entityType, id);
            return found != null;
        }catch(NonUniqueResultException ignored) {
            return true;
        }catch(EntityNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public boolean existsBy(String name, Object value) {
        final List found = jpaObjectFactory.getDaoForSelect(value.getClass())
                .where(entityType, name, value)
                .select(name).getResultsAndClose(0, 1);
        return found != null  && ! found.isEmpty();
    }

    @Override
    public void create(E entity) {
        this.preCreate(entity);
        try(final Dao dao = jpaObjectFactory.getDao()) {
            dao.begin().persist(entity);
            dao.commit();
        }
    }
    
    @Override
    public List<E> findAllBy(String key, Object value) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true).getResultsAndClose();
    }
    
    @Override
    public List<E> findAllBy(String key, Object value, int offset, int limit) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true)
                .getResultsAndClose(offset, limit);
    }

    @Override
    public E findSingleBy(String key, Object value) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .where(key, value).distinct(true).getSingleResultAndClose();
    }

    @Override
    public List<E> findAll() {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .distinct(true).findAllAndClose(entityType);
    }

    @Override
    public List<E> findAll(int offset, int limit) {
        return jpaObjectFactory.getDaoForSelect(entityType)
                .distinct(true).findAllAndClose(entityType, offset, limit);
    }

    @Override
    public E findOrDefault(Object id, E resultIfNone) {
        E found;
        try{
            found = find(id);
        }catch(EntityNotFoundException e) {
            found = null;
        }
        return found == null ? resultIfNone : found;
    }

    @Override
    public E find(Object id) throws EntityNotFoundException {
        final E found = (E)jpaObjectFactory.getDaoForSelect(entityType).find(entityType, toPrimaryColumnType(id));
        if(found == null) {
            throw new EntityNotFoundException("Not found. " + entityType.getName() + " with id: " + id);
        }
        return found;
    }

    @Override
    public void deleteById(Object id) {
        final Dao dao = jpaObjectFactory.getDao();
        dao.removeAndClose(dao.find(entityType, toPrimaryColumnType(id)));
    }

    @Override
    public void deleteManagedEntity(E entity) {
        jpaObjectFactory.getDao().removeAndClose(entity);
    }

    @Override
    public void update(E entity) {
        this.preUpdate(entity);
        jpaObjectFactory.getDao().mergeAndClose(entity);
    }
    
    private Object toPrimaryColumnType(Object id) {
        final Object result;
        if(primaryColumnType.equals(Short.class)) {
            if(id instanceof Short) {
                result = (Short)id;
            }else{
                result = Short.parseShort(id.toString());
            }
        }else if(primaryColumnType.equals(Integer.class)) {
            if(id instanceof Integer) {
                result = (Integer)id;
            }else{
                result = Integer.parseInt(id.toString());
            }
        }else if(primaryColumnType.equals(Long.class)) {
            if(id instanceof Long) {
                result = (Long)id;
            }else{
                result = Long.parseLong(id.toString());
            }
        }else if(primaryColumnType.equals(String.class)) {
            result = id.toString();
        }else{
            result = id;
        }
        return result;
    }

    public Select<E> getDaoForSelect() {
        return jpaObjectFactory.getDaoForSelect(entityType).from(entityType);
    }
    
    public <T> Select<T> getDaoForSelect(Class<T> resultType) {
        return jpaObjectFactory.getDaoForSelect(resultType).from(entityType);
    }

    public Update<E> getDaoForUpdate() {
        return jpaObjectFactory.getDaoForUpdate(entityType);
    }

    public Delete<E> getDaoForDelete() {
        return jpaObjectFactory.getDaoForDelete(entityType);
    }

    public JpaObjectFactory getJpaObjectFactory() {
        return jpaObjectFactory;
    }

    public Class getEntityType() {
        return entityType;
    }

    public Class getPrimaryColumnType() {
        return primaryColumnType;
    }
}
