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

import java.util.Collections;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 9:35:54 PM
 */
public class SelectorFactoryImpl implements SelectorFactory{

    @Override
    public List<Selector> get(String selector) {
        List<Selector> output;
        switch(selector){
            case Selectors.FUTURE:
            case Selectors.PAST:
                output = Collections.singletonList(new SelectDate(selector));
                break;
            case Selectors.INBOX:
            case Selectors.OUTBOX:
                output = Collections.singletonList(new SelectUserMessage(selector));
                break;
            default:
                output = Collections.EMPTY_LIST;
        }
        return output;
    }
}
