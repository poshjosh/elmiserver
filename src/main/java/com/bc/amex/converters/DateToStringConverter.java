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

package com.bc.amex.converters;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 11, 2019 1:40:47 AM
 */
public class DateToStringConverter implements Converter<Date, String> {

    private static final Logger LOG = LoggerFactory.getLogger(DateToStringConverter.class);

    private final SimpleDateFormat dateFormat;
    
    public DateToStringConverter(DateAndTimePatternsSupplier dtp) {
        this(dtp.getDatetimePatterns().iterator().next());
    }
    
    public DateToStringConverter(String datePattern) {
        this.dateFormat = new SimpleDateFormat(datePattern);
//        this.dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    @Override
    public String convert(Date from) {
        LOG.trace("DATES To convert to String, date: {}", from);
        final String output = dateFormat.format(from);
        LOG.trace("DATES Pattern: {}, date: {}", dateFormat.toPattern(), output);
        return output;
    }
}
