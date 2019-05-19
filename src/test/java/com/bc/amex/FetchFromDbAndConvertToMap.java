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

import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.Testsetting;
import com.bc.elmi.pu.entities.User;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.util.JsonFormat;
import com.looseboxes.mswordbox.MapperJackson;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import static org.junit.Assert.fail;

/**
 * @author Chinomso Bassey Ikwuagwu on May 14, 2019 1:40:14 PM
 */
public class FetchFromDbAndConvertToMap {

    private final JpaObjectFactory jpa = Jpa.getObjectFactory();
    
    private static class TestsettingHasLocation implements BiPredicate<Test, Map>{
        @Override
        public boolean test(Test t, Map m) {
            final List<Testsetting> l = t.getTestsettingList();
            if(l == null || l.isEmpty()) {
                return false;
            }
            final Testsetting tt = l.get(0);
            final Map ttMap = (Map)((List)m.get("testsettingList")).get(0);
            final Map docMap = (Map)ttMap.get("testsetting");
            final Object loc = docMap.get("location");
            return loc != null;
        }
    }
    
    @org.junit.Test
    public void test() {
    
        test(User.class, 3, (u, m) -> true);
        
        test(Test.class, 28, new TestsettingHasLocation());
    }

    public <T> void test(Class<T> type, Object id, BiPredicate<T, Map> test) {
    
        final T t = jpa.getDao().find(type, id);
        
        final Map m = new MapperJackson().toMap(t);
        
        System.out.println("Printing json for: " + t);
        System.out.println(new JsonFormat(true, true, "  ").toJSONString(m));
        
        if( ! test.test(t, m)) {
            
            fail("Test failed on: " + m);
        }
    }
}
