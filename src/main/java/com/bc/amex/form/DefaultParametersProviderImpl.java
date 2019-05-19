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

import com.bc.elmi.pu.entities.Appointment_;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 9:37:41 PM
 */
//@todo add to FormConfiguration
public class DefaultParametersProviderImpl implements DefaultParametersProvider{

    @Override
    public Map<String, Object> apply(String action, String modelname) {
        final Map<String, Object> output = new HashMap(4, 0.75f);
        final Date date = new Date();
        output.put(Appointment_.timecreated.getName(), date);
        output.put(Appointment_.timemodified.getName(), date);
        return output;
    }
}
