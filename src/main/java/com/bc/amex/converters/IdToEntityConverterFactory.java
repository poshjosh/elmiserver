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

package com.bc.amex.converters;

import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:47:27 AM
 */
public class IdToEntityConverterFactory implements ConverterFactory<Object, Object> {

//    private static final Logger LOG = LoggerFactory.getLogger(IdToEntityConverterFactory.class);
 
    private final EntityRepositoryFactory entityRepositoryFactory;
    
    public IdToEntityConverterFactory(EntityRepositoryFactory entityRepositoryFactory) {
        this.entityRepositoryFactory = Objects.requireNonNull(entityRepositoryFactory);
    }
    
    @Override
    public Converter<Object, Object> getConverter(Class targetType) {
        if(this.entityRepositoryFactory.isSupported(targetType)) {
            return new ConvertIdToEntity(entityRepositoryFactory.forEntity(targetType));
        }else{
            return (toConvert) -> toConvert;
        }
    }
}