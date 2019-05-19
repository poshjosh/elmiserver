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
import java.util.function.Consumer;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 9:33:45 PM
 */
public interface Selector extends Consumer<Select<?>> {
    
    Selector NO_OP = new Selector(){
        @Override
        public void accept(Select<?> select) { }
        @Override
        public boolean isApplicable(Class entityType) { return false; }
    };

    @Override
    void accept(Select<?> select);

    boolean isApplicable(Class entityType);

    /**
     * @param after the operation to perform after this operation
     * @return a composed {@code Selector} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     * @see java.util.function.Consumer
     */
    default Selector andThen(Selector after) {
        return new SelectorComposite(this, after);
    }
}
