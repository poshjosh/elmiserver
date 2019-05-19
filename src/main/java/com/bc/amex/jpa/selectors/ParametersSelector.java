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

package com.bc.amex.jpa.selectors;

import com.bc.jpa.dao.Select;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on May 13, 2019 4:37:38 PM
 */
public class ParametersSelector implements Selector{

    private static final Logger LOG = LoggerFactory.getLogger(ParametersSelector.class);

    private final Map<String, String[]> params;

    public ParametersSelector(Map<String, String[]> params) {
        this.params = Objects.requireNonNull(params);
    }
    
    @Override
    public void accept(Select<?> select) {
        
        for(String name : params.keySet()) {
        
            final String [] values = params.get(name);
            
            if(values == null || values.length == 0) {
                
                LOG.warn("Does not have value(s), parameter: {}", name);
                
                continue;
            }
            
            for(int i = 0; i < values.length; i++) {

                if(i == 0) {
                    select.and();
                }else{
                    select.or();
                }
                
                select.where(name, values[i]);
            }
        }
    }

    @Override
    public boolean isApplicable(Class entityType) { return true; }
}
