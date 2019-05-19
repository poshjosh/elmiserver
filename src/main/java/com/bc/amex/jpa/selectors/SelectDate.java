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

import com.bc.elmi.pu.entities.Appointment_;
import com.bc.jpa.dao.Criteria;
import com.bc.jpa.dao.Select;
import com.bc.jpa.dao.functions.GetColumnNamesOfType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 8:59:20 PM
 */
public class SelectDate implements Selector{

    private final String selector; 

    public SelectDate(String selector) {
        this.selector = Objects.requireNonNull(selector);
    }
    
    @Override
    public boolean isApplicable(Class entityType) {
        return true;
    }

    @Override
    public void accept(Select select) {

        final Criteria.ComparisonOperator dateCriteria;
        if(Selectors.FUTURE.equals(selector)) {
            dateCriteria = Criteria.GREATER_THAN;
        }else if(Selectors.PAST.equals(selector)){
            dateCriteria = Criteria.LESS_THAN;
        }else{
            dateCriteria = null;
        }

        if(dateCriteria != null) {
            
            final Collection<String> columns = getColumnsLess(select, Date.class, 
                    Appointment_.timecreated.getName(), Appointment_.timemodified.getName());
            
            final Iterator<String> iter = columns.iterator();
            
            while(iter.hasNext()) {
            
                final String col = iter.next();

                select.where(col, dateCriteria, new Date());
                
                if(iter.hasNext()) {
                    select.and();
                }
            }
        }    
    }

    private List<String> getColumnsLess(Select select, Class columntype, String... exclude) {
        
        final List<String> excludeList = Arrays.asList(exclude);
        
        final java.util.function.Predicate<String> colFilter = (col) -> ! excludeList.contains(col);
        
        final EntityManagerFactory emf = select.getEntityManager().getEntityManagerFactory();
        
        final List<Class> list = select.getEntityTypeList();

        final Class entityType = list.get(list.size() - 1);
        
        return getColumnsSelector(emf).apply(entityType, columntype)
                .stream().filter(colFilter).collect(Collectors.toList());
    }
    
    public BiFunction<Class, Class, Collection<String>> getColumnsSelector(EntityManagerFactory emf) {
        return new GetColumnNamesOfType(emf);
    }
}
