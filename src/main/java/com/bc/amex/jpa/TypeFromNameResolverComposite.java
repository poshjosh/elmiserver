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

package com.bc.amex.jpa;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 6, 2019 5:32:12 PM
 */
public class TypeFromNameResolverComposite extends AbstractEntityTypeResolver {

    private final List<TypeFromNameResolver> delegates;

    public TypeFromNameResolverComposite(TypeFromNameResolver... delegates) {
        this(Arrays.asList(delegates));
    }
    
    public TypeFromNameResolverComposite(List<TypeFromNameResolver> delegates) {
        this.delegates = Objects.requireNonNull(delegates);
    }

    @Override
    public Class getType(String entityName, Class resultIfNone) {
        Class output = null;
        for(TypeFromNameResolver delegate : delegates) {
            output = delegate.getType(entityName, null);
            if(output != null) {
                break;
            }
        }
        return output == null ? resultIfNone : output;
    }
}
