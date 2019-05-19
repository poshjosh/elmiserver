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

package com.bc.amex.searchresults;

import com.bc.elmi.pu.entities.Appointment;
import com.bc.elmi.pu.entities.Course;
import com.bc.elmi.pu.entities.Document;
import com.bc.elmi.pu.entities.Message;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.Testdocument;
import com.bc.elmi.pu.entities.Testsetting;
import com.bc.elmi.pu.entities.User;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 22, 2019 3:54:14 PM
 */
public class SearchresultBuilderImpl implements SearchresultBuilder{

    // @todo make a property
    // @dateformat property
    private final SimpleDateFormat dateFormat;
    
    private final int maxPrefixLen = 50;

    public SearchresultBuilderImpl() {
        dateFormat = new SimpleDateFormat("dd-MMM-2019 HH:mm");
    }

    @Override
    public boolean isImageType(Class type) {
        return User.class.isAssignableFrom(type);
    }

    @Override
    public boolean isSupported(Class type) {
        return User.class.isAssignableFrom(type) ||
                Test.class.isAssignableFrom(type) ||
                Document.class.isAssignableFrom(type) ||
                Message.class.isAssignableFrom(type) ||
                Course.class.isAssignableFrom(type);
    }

    @Override
    public Searchresult apply(Object model) {
        final SearchresultImpl output = new SearchresultImpl();
        output.setModelobject(model);
        if(model instanceof User) {
            final User entity = (User)model;
            output.setId(entity.getUserid());
            output.setImageLink(entity.getProfileimage());
            output.setName(entity.getUsername());
            final Appointment a = entity.getAppointment();
            final String summary = this.buildSummary(entity.getUsername(), 
                    "(" + (a == null ? "[NO APPOINTMENT]" : a.getAppointmentname()) + ")");
            output.setSummary(summary);
        }else if(model instanceof Test) {
            final Test entity = (Test)model;
            output.setId(entity.getTestid());
//            output.setImageLink();
            output.setName(entity.getTestname());
            final String summary = this.buildSummary(entity.getTestname(), 
                    "<small> scheduled for: " + (entity.getStarttime() == null ? "" : dateFormat.format(entity.getStarttime())) + "</small>");
            output.setSummary(summary);
        }else if(model instanceof Document) {
            final Document entity = (Document)model;
            output.setId(entity.getDocumentid());
//            output.setImageLink();
            final String name = getName(entity);
            output.setName(name);
            final Date date = getDated(entity);
            final String summary = this.buildSummary(entity.getSubject() == null ? name : entity.getSubject(), 
                    "<small> dated: " + dateFormat.format(date) + "</small>");
            output.setSummary(summary);
        }else if(model instanceof Testdocument) {
            final Testdocument entity = (Testdocument)model;
            output.setId(entity.getTestdocumentid());
//            output.setImageLink();
            final String name = truncate(entity.getDocumenttest().getTestname(), maxPrefixLen) + " - " + getName(entity.getTestdocument());
            output.setName(name);
            final Date date = getDated(entity.getTestdocument());
            final Document doc = entity.getTestdocument();
            final String summary = this.buildSummary(doc.getSubject() == null ? name : doc.getSubject(), 
                    "<small> dated: " + dateFormat.format(date) + "</small>");
            output.setSummary(summary);
        }else if(model instanceof Testsetting) {
            final Testsetting entity = (Testsetting)model;
            output.setId(entity.getTestsettingid());
//            output.setImageLink();
            final String name = truncate(entity.getSettingtest().getTestname(), maxPrefixLen) + " - " + getName(entity.getTestsetting());
            output.setName(name);
            final Date date = getDated(entity.getTestsetting());
            final Document doc = entity.getTestsetting();
            final String summary = this.buildSummary(doc.getSubject() == null ? name : doc.getSubject(), 
                    "<small> dated: " + dateFormat.format(date) + "</small>");
            output.setSummary(summary);
        }else if(model instanceof Message) {
            final Message entity = (Message)model;
            output.setId(entity.getMessageid());
            output.setName(entity.getType().getMessagetypename());
            final String summary = this.buildSummary(entity.getSubject(), 
                    "<small> dated: " + dateFormat.format(entity.getTimecreated()) + "</small>");
            output.setSummary(summary);
        }else if(model instanceof Course) {
            final Course entity = (Course)model;
            output.setId(entity.getCourseid());
            output.setName(entity.getCoursename());
            final String summary = this.buildSummary(entity.getCoursename(), 
                    "<small> start date: " + dateFormat.format(entity.getStartdate()) + "</small>");
            output.setSummary(summary);
        }else{
            throw new UnsupportedOperationException("Unsupported argument type: " + model.getClass() + 
                    ". Use method isSupported(Class) to verify support for each type before attempting conversion");
        }
        return output;
    }
    
    private String buildSummary(String pri, String other) {
        return new StringBuilder().append(pri).append("&emsp;<tt>").append(other).append("</tt>").toString();
    }
    
    private String getName(Document d) {
        return d.getDocumentname() != null ? d.getDocumentname() : 
                Paths.get(d.getLocation()).getFileName().toString();
    }
    
    private Date getDated(Document d) {
        return d.getDatesigned() == null ? d.getTimecreated() : d.getDatesigned();
    }
    
    private String truncate(String arg, int n) {
        return arg == null ? null : arg.length() <= n ? arg : arg.substring(0, n);
    }
}
