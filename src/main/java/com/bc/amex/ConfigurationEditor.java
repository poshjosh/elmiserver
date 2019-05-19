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

import com.looseboxes.mswordbox.MsKioskConfiguration;
import com.looseboxes.mswordbox.ui.UiConfiguration;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 31, 2019 6:19:36 PM
 */
public class ConfigurationEditor implements UnaryOperator<Class>{

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationEditor.class);

    @Override
    public Class apply(Class aClass) {
        
        if(MsKioskConfiguration.class.equals(aClass)) {
        
            LOG.info("Replacing {} with: {}", aClass, MsKioskConfigurationImpl.class);
            
            return MsKioskConfigurationImpl.class;
            
        }else if(UiConfiguration.class.equals(aClass)) {
        
            LOG.info("Replacing {} with: {}", aClass, UiConfigurationImpl.class);

            return UiConfigurationImpl.class;
        }
        
        return aClass;
    }
}
