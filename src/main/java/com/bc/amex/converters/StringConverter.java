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

import org.springframework.core.convert.converter.Converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 19, 2019 4:16:04 PM
 */
public class StringConverter implements Converter<String, String>{

    @Override
    public String convert(String source) {
        final String output;
        if(source != null && source.isEmpty()) {
            output = null;
        }else{
            output = source;
        }
        return output;
    }
}
