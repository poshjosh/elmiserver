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

import com.bc.amex.controllers.PartParser;
import com.bc.amex.controllers.PartParserImpl;
import com.bc.amex.controllers.ResponseBuilder;
import com.bc.amex.controllers.ResponseBuilderImpl;
import com.bc.amex.searchresults.SearchresultBuilder;
import com.bc.amex.searchresults.SearchresultBuilderImpl;
import com.bc.amex.searchresults.SearchresultPage;
import com.bc.app.spring.functions.GetUniquePathForFilename;
import com.bc.app.spring.functions.GetUniquePathForFilenameImpl;
import com.looseboxes.mswordbox.Mapper;
import java.nio.file.Paths;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 2, 2019 8:16:59 AM
 */
@Lazy
@Configuration
public class AmexConfiguration {
    
    @Bean @Scope("prototype") public GetUniquePathForFilename getUniquePathForFilename(
            DirsProperties dirsProperties) {
        return new GetUniquePathForFilenameImpl(Paths.get(dirsProperties.getUploads()));
    }
    
    @Bean @Scope("prototype") public PartParser partParser(Mapper mapper) {
        return new PartParserImpl(mapper);
    }
    
    @Bean @Scope("prototype") public ResponseBuilder responseBuilder() {
        return new ResponseBuilderImpl();
    }
    
    @Bean @Scope("prototype") SearchresultBuilder searchresultBuilder() {
        return new SearchresultBuilderImpl();
    }
    
    @Bean @Scope("prototype") SearchresultPage.Builder searchresultPageBuilder() {
        return new SearchresultPage.Builder();
    }
    
    /**
     * @see <a href="http://theblasfrompas.blogspot.com/2016/08/httpsessionlistener-with-spring-boot.html">httpsessionlistener-with-spring-boot</a>
     * @return 
     */
    @Bean public ServletListenerRegistrationBean<ServletContextListener> contextListener() {
        return new ServletListenerRegistrationBean<>(new ServletContextListenerImpl());
    }
    
    /**
     * @see <a href="http://theblasfrompas.blogspot.com/2016/08/httpsessionlistener-with-spring-boot.html">httpsessionlistener-with-spring-boot</a>
     * @return 
     */
    @Bean public ServletListenerRegistrationBean<HttpSessionListener> sessionListener() {
        return new ServletListenerRegistrationBean<>(new HttpSessionListenerImpl());
    }
 
//@todo we need to create our intance pecifically with own propertie    
//    @Bean public OkHttpClient okhttpClient(HttpProperties config) {
//        return new OkHttpClient.Builder()
//                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
//                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
//                .retryOnConnectionFailure(true) 
//                .cookieJar(CookieJar.NO_COOKIES).build();    
//    }
    
//    @Bean @Scope("prototype") public OkHttp okhttp(OkHttpClient httpClient, HttpProperties httpConfig) {
//        return new OkHttpImpl(httpClient, httpConfig);
//    }
}
