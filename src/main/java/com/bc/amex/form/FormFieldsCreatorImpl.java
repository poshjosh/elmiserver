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
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.web.form.Form;
import com.bc.web.form.FormField;
import com.bc.web.form.FormFieldBuilder;
import com.bc.web.form.StandardFormFieldTypes;
import com.bc.web.form.functions.CreateFormFieldFromAnnotatedPersistenceEntity;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 8, 2019 4:17:44 PM
 */
public class FormFieldsCreatorImpl extends CreateFormFieldFromAnnotatedPersistenceEntity{

    private static final Logger LOG = LoggerFactory.getLogger(FormFieldsCreatorImpl.class);
    
    private final SelectionContext selectionContext;

    private final FormPropertyAccess formPropertyAccess;
    
    private final EntityRepositoryFactory entityRepositoryFactory;

    // @todo autowire 
    // @dateformat property
    private final SimpleDateFormat dateFormat;
    
    public FormFieldsCreatorImpl(EntityRepositoryFactory entityRepositoryFactory,
            SelectionContext selectionContext, FormPropertyAccess formPropertyAccess) {
        this(entityRepositoryFactory, selectionContext, 
                formPropertyAccess, new IsFormField(selectionContext), -1);
    }

    protected FormFieldsCreatorImpl(EntityRepositoryFactory entityRepositoryFactory,
            SelectionContext selectionContext, FormPropertyAccess formPropertyAccess,
            Predicate<Field> isFormField, int maxDepth) {
        super(isFormField, maxDepth);
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
        this.selectionContext = Objects.requireNonNull(selectionContext);
        this.formPropertyAccess = Objects.requireNonNull(formPropertyAccess);
        this.dateFormat = new SimpleDateFormat();
    }

    @Override
    protected FormField buildFormField(Form form, Field field, FormFieldBuilder builder) {
        formPropertyAccess.getProperty(field, "label").ifPresent((label) -> builder.label(label));
        formPropertyAccess.getProperty(field, "advice").ifPresent((advice) -> builder.advice(advice));
        return super.buildFormField(form, field, builder); 
    }

    @Override
    public Object getValue(Form form, Field field) {
        
        Object value = super.getValue(form, field);
        
        final Temporal temporal;
        
        if(value instanceof Date && (temporal = field.getAnnotation(Temporal.class)) != null) {
            final TemporalType tt = temporal.value();
            if(tt != null) {
                final String suffix;
                switch(tt) {
                    case DATE:
                        suffix = "formats.date"; break;
                    case TIME:
                        suffix = "formats.time"; break;
                    default:
                        suffix = "formats.datetime";
                }
                final String val = formPropertyAccess.getProperty(field, suffix).orElse(null);
                if(val != null && !val.isEmpty()) {
                    final String fmt = Arrays.asList(val.split(",")).stream()
                            .filter((e) -> !e.trim().isEmpty())
                            .map((e) -> e.trim()).findFirst().orElse(null);
                    if(fmt != null && !fmt.isEmpty()) {
                        dateFormat.applyLocalizedPattern(fmt);
                        final String dateStr = dateFormat.format((Date)value);
                        LOG.trace("DATES Pattern: {}, input: {}, output: {}", fmt, value, dateStr);
                        value = dateStr;
                    }else{
                        LOG.warn("DATES Date format not configured for {} field: {}", form.getName(), field);
                    }
                }else{
                    LOG.warn("DATES Date format not configured for {} field: {}", form.getName(), field);
                }
            }
        }else{
            if(this.isMultiChoice(form, field)) {
                if(this.isMultiValue(form, field)) {
                    if(value instanceof Collection) {
                        final Class genericTypeArgument = new GetGenericTypeArgument().apply(field, null);
                        if(genericTypeArgument != null && this.entityRepositoryFactory.isSupported(genericTypeArgument)) {
                            final Collection c = (Collection)value;
                            if( ! c.isEmpty()) {
                                value = c.stream().map((entity) -> getIdForEntity(entity, entity)).collect(Collectors.toList());
                                LOG.trace("For field: {}\nConverted: {}\n       To: {}", field, c, value);
                            }
                        }
                    }
                }else{
                    final Class type = field.getType();
                    if(this.entityRepositoryFactory.isSupported(type)) {
                        final Object entity = super.getValue(form, field);
                        value = this.getIdForEntity(entity, entity);
                        LOG.trace("For field: {}\nConverted: {} to: {}", field, entity, value);
                    }
                }
            }
        }
        
        return value;
    }
    
    public Object getIdForEntity(Object entity, Object resultIfNone) {
        if(entity == null) {
            return resultIfNone;
        }else{
            return this.entityRepositoryFactory.forEntity(entity.getClass()).getIdOptional(entity).orElse(resultIfNone);
        }
    }

    @Override
    public boolean isMultiChoice(Form form, Field field) {

        final boolean output;
        
        final Class type = field.getType();
        
        if(Collection.class.isAssignableFrom(type)) {
        
            final Class genericTypeArgument = new GetGenericTypeArgument().apply(field, null);

            if(genericTypeArgument != null) {
                output = this.selectionContext.isSelectionType(genericTypeArgument);
            }else{
                output = false;
            }    
        }else{
        
            output = this.selectionContext.isSelectionType(field.getType());
        }
        LOG.trace("Multichoice: {}, field: {}", output, field);
        return output;
    }

    @Override
    public boolean isMultiValue(Form form, Field field) {
        final boolean output = super.isMultiValue(form, field);
        LOG.trace("Multivalue: {}, field: {}", output, field);
        return output;
    }
    
    @Override
    public Map getChoices(Form form, Field field) {

        final Class type = field.getType();
        
        if(Collection.class.isAssignableFrom(type)) {
        
            final Class genericTypeArgument = new GetGenericTypeArgument().apply(field, null);

            if(genericTypeArgument != null) {
                return getChoices(genericTypeArgument);
            }else{
                return Collections.EMPTY_MAP;
            }    
        }else{
        
            return getChoices(type);
        }
    }
    
    public Map getChoices(Class type) {
        
        if(this.selectionContext.isSelectionType(type)) {
            
            final Map result = this.selectionContext.getSelectionOptions(type);

            return result;
            
        }else{
            
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public String getType(Form form, Field field) {
        if(field.getName().toLowerCase().contains("image")) {
            return StandardFormFieldTypes.FILE;
        }else{
            return super.getType(form, field);
        }
    }    
}
