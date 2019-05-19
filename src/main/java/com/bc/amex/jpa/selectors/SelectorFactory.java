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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 9:38:08 PM
 */
public interface SelectorFactory extends Selectors{

    default Selector combo(String selectorName) {
        final List<Selector> found = get(selectorName);
        if(found.isEmpty()) {
            return Selector.NO_OP;
        }else{
            Selector combo = null;
            for(Selector selector : found) {
                if(combo == null) {
                    combo = selector;
                }else{
                    combo = combo.andThen(selector);
                }
            }
            return combo;
        }
        
    }

    default Selector combo(String selectorName, Class entityType) {
        final List<Selector> found = get(selectorName, entityType);
        if(found.isEmpty()) {
            return Selector.NO_OP;
        }else{
            Selector combo = null;
            for(Selector selector : found) {
                if(combo == null) {
                    combo = selector;
                }else{
                    combo = combo.andThen(selector);
                }
            }
            return combo;
        }
    }
    
    default List<Selector> get(String selectorName, Class entityType) {
        return get(selectorName).stream()
                .filter((selector) -> selector.isApplicable(entityType))
                .collect(Collectors.toList());
    }

    List<Selector> get(String selector);
}
