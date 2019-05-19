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

import com.bc.diskcache.DiskLruCacheContext;
import com.bc.jpa.dao.JpaObjectFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 22, 2019 2:51:34 PM
 */
public class ServletContextListenerImpl implements ServletContextListener{

    private static final Logger LOG = LoggerFactory.getLogger(ServletContextListenerImpl.class);
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        LOG.info("ServletContext destroyed");
        
        final ServletContext servletContext = sce.getServletContext();

        final ApplicationContext ctx = WebApplicationContextUtils.
                    getWebApplicationContext(servletContext);
        
        if(ctx != null) {
            
            final JpaObjectFactory jpa = ctx.getBean(JpaObjectFactory.class);

            if(jpa != null && jpa.isOpen()) {

                jpa.close();

                LOG.info("Closed instance of: {}", JpaObjectFactory.class);
            }
            
            final DiskLruCacheContext dcc = ctx.getBean(DiskLruCacheContext.class);
            
            if(dcc != null) {
            
                dcc.closeAndRemoveAll();

                LOG.info("Closed instance of: {}", DiskLruCacheContext.class);
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        LOG.info("ServletContext initialized");
    }
}
