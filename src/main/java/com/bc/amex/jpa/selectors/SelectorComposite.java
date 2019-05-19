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
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 9:48:20 PM
 */
public class SelectorComposite implements Selector{
    
    private final Selector lhs;
    
    private final Selector rhs;

    public SelectorComposite(Selector lhs, Selector rhs) {
        this.lhs = Objects.requireNonNull(lhs);
        this.rhs = Objects.requireNonNull(rhs);
    }

    @Override
    public void accept(Select select) {
        lhs.accept(select); rhs.accept(select);
    }

    @Override
    public boolean isApplicable(Class entityType) {
        return lhs.isApplicable(entityType) && rhs.isApplicable(entityType);
    }
}
