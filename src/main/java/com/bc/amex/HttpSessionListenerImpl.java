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

import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see http://theblasfrompas.blogspot.com/2016/08/httpsessionlistener-with-spring-boot.html
 * @author Chinomso Bassey Ikwuagwu on Apr 22, 2019 1:38:36 PM
 */
public class HttpSessionListenerImpl implements HttpSessionListener{
    
    private static final Logger LOG = LoggerFactory.getLogger(HttpSessionListenerImpl.class);
 
    @Override
    public void sessionCreated(HttpSessionEvent event)  {
        
        LOG.debug("Session created, id: {}", event.getSession().getId());
    }
 
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        
        final HttpSession session = event.getSession();
        
        LOG.debug("Session destroyed, id: {}", session.getId());
        
        final Enumeration<String> attrNames = session.getAttributeNames();
        
        if(attrNames != null) {
        
            while(attrNames.hasMoreElements()) {
                
                final String attrName = attrNames.nextElement();
                
                final Object attr = session.getAttribute(attrName);
                
                if(attr instanceof AutoCloseable) {
                
                    try{
                        
                        ((AutoCloseable)attr).close();
                        
                        LOG.debug("Closed attribute named: {}", attrName);
                        
                    }catch(Exception e) {
                        LOG.error("Exception closing attribute named: " + attrName, e);
                    }
                }
            }
        }
    }
}
