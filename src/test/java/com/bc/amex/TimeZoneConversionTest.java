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
import com.bc.jpa.dao.JpaObjectFactory;
import com.looseboxes.mswordbox.functions.GetDateTime;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Chinomso Bassey Ikwuagwu on May 14, 2019 1:44:32 PM
 */
public class TimeZoneConversionTest {

    public void a() {
        
        try(final JpaObjectFactory jpa = Jpa.getObjectFactory()) {
            final Test test = jpa.getDao().findAndClose(Test.class, 9);
            final Date date = test.getStarttime();
            final ZonedDateTime dt = new GetDateTime().apply(date.getTime(), ZoneId.of("+0100"));
            System.out.println("ZDT: " + dt);
            
            
            System.out.println("Date: " + date);
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            df.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
            String dateStr = df.format(date);
            System.out.println("Date: " + dateStr);
            df.setTimeZone(TimeZone.getTimeZone("+0100"));
            dateStr = df.format(date);
            System.out.println("Date: " + dateStr);
        }
    }
}
