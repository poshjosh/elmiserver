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

package com.bc.amex;

import com.bc.amex.converters.DateAndTimePatternsSupplier;
import com.bc.amex.converters.DateToStringConverter;
import com.bc.amex.converters.MultipartFileToStringConverter;
import com.bc.amex.converters.IdToEntityConverterFactory;
import com.bc.amex.converters.StringConverter;
import com.bc.amex.converters.StringToDateConverter;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:26:12 AM
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
 
    @Autowired private EntityRepositoryFactory entityRepositoryFactory;
    @Autowired private DateAndTimePatternsSupplier dateAndTimePatternsSupplier;
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringConverter());
        registry.addConverter(new MultipartFileToStringConverter());
        registry.addConverter(new StringToDateConverter(dateAndTimePatternsSupplier));
        registry.addConverter(new DateToStringConverter(dateAndTimePatternsSupplier));
        registry.addConverterFactory(new IdToEntityConverterFactory(entityRepositoryFactory));
    }
}