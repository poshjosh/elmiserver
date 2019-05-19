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

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 9:21:18 AM
 */
public interface FormPropertyAccess {

    String FORMATS_DATETIME = "formats.datetime";
    String FORMATS_DATE = "formats.date";
    String FORMATS_TIME = "formats.time";
    
    Optional<String> getProperty(Field field, String suffix);

    Optional<String> getProperty(Class type, String name, String suffix);

    Optional<String> getProperty(String name, String suffix);

    Optional<String> getPropertyWithSuffix(String suffix);
}
