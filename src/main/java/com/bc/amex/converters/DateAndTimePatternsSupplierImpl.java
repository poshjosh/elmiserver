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

import com.bc.amex.form.FormPropertyAccess;
import com.bc.amex.form.FormPropertyAccessImpl;
import com.bc.amex.jpa.TypeFromNameResolver;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.core.env.Environment;

/**
 * @author Chinomso Bassey Ikwuagwu on May 1, 2019 6:56:28 PM
 */
public class DateAndTimePatternsSupplierImpl 
        extends FormPropertyAccessImpl 
        implements DateAndTimePatternsSupplier{

    private final Set<String> datetimePatterns;
    private final Set<String> datePatterns;
    private final Set<String> timePatterns;

    public DateAndTimePatternsSupplierImpl(Environment env, TypeFromNameResolver tnr) {
        super(env, tnr);
        datetimePatterns = addPropertiesWithSuffix(FormPropertyAccess.FORMATS_DATETIME);
        datePatterns = addPropertiesWithSuffix(FormPropertyAccess.FORMATS_DATE);
        timePatterns = addPropertiesWithSuffix(FormPropertyAccess.FORMATS_TIME);
    }
    
    private Set<String> addPropertiesWithSuffix(String suffix) {
        return addPropertiesWithSuffix(new LinkedHashSet<>(), suffix);
    }

    private Set<String> addPropertiesWithSuffix(Set<String> c, String suffix) {
        final String arr = getPropertyWithSuffix(suffix).orElse(null);
        if(arr != null && !arr.isEmpty()) {
            Arrays.asList(arr.split(",")).stream()
                    .map((e) -> e == null ? null : e.trim())
                    .filter((e) -> !e.isEmpty() && !c.contains(e))
                    .forEach((e) -> c.add(e));
        }        
        return c;
    }

    @Override
    public Set<String> getDatetimePatterns() {
        return datetimePatterns;
    }

    @Override
    public Set<String> getDatePatterns() {
        return datePatterns;
    }

    @Override
    public Set<String> getTimePatterns() {
        return timePatterns;
    }
}
