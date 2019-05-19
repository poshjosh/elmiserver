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

import com.bc.amex.controllers.CrudActionNames;
import com.bc.amex.form.FormConfiguration;
import com.bc.amex.jpa.JpaConfiguration;
import com.bc.app.spring.BcspringbootConfiguration;
import com.bc.app.spring.lifecycle.LifecycleConfiguration;
import com.bc.app.spring.server.TomcatProperties;
import com.looseboxes.mswordbox.MsKioskConfiguration;
import com.looseboxes.mswordbox.ui.UiConfiguration;
import com.looseboxes.mswordbox.ui.admin.AdminUiConfiguration;
import com.looseboxes.mswordbox.ui.exam.ExamUiConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.Cookie;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 17, 2019 6:30:05 PM
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = {"com.bc.app.spring", "com.bc.amex"})//, "com.looseboxes.mswordbox"})
@ContextConfiguration(classes = {
    LifecycleConfiguration.class, BcspringbootConfiguration.class, 
    AmexConfiguration.class, JpaConfiguration.class, FormConfiguration.class,
    MsKioskConfiguration.class, UiConfiguration.class, AdminUiConfiguration.class, ExamUiConfiguration.class,
    TomcatProperties.class
})
@EnableAutoConfiguration
public class ApplicationTest {

    @Autowired private MockMvc mockMvc;
    
    private static final List<Cookie> cookieList = new ArrayList<>(); 

    @Test
    public void loginThenForCreateShowForm() throws Exception {
//        login();
        forCreateShowForm();
    }

//    @Test
    public void login() throws Exception {
        System.out.println("\nlogin()");
        System.out.println("-------");
        final Map<String, String> paramMap = new HashMap();
        paramMap.put("username", UserDetails.USERNAME_VALUE);
        paramMap.put("password", UserDetails.PASSWORD_VALUE);
        final RequestBuilder reqBuilder = requestBuilder("post", "/login", paramMap);
        perform(reqBuilder, -1, true, true);
    }
    
//    @Test
    public void forCreateShowForm() throws Exception {
        System.out.println("\n/"+CrudActionNames.CREATE+"/showForm");
        System.out.println("-------------------");
        perform(requestBuilder("get", "/"+CrudActionNames.CREATE+"/showForm?modelname=User"), -1, true, true);
    }
    
    public RequestBuilder requestBuilder(String method, String url) {
        return requestBuilder(method, url, Collections.EMPTY_MAP);
    }
    
    public RequestBuilder requestBuilder(String method, String url, Map<String, String> paramMap) {
        final MockHttpServletRequestBuilder reqBuilder;
        switch(method) {
            case "GET":
            case "get":
                reqBuilder = MockMvcRequestBuilders.get(url);
                break;
            case "POST":
            case "post":
                reqBuilder = MockMvcRequestBuilders.post(url);
                break;
            default:
                throw new IllegalArgumentException("Request method not yet supported: " + method);
        }
        
        if( ! cookieList.isEmpty()) {
            reqBuilder.cookie(cookieList.toArray(new Cookie[0]));
        }
        
        for(Entry<String, String> entry : paramMap.entrySet()) {
            reqBuilder.param(entry.getKey(), entry.getValue());
        }
        
        return reqBuilder;
    }

    public void perform(RequestBuilder requestBuilder, int expectedStatus, boolean print, boolean trace) 
            throws Exception {

        ResultActions ra = this.mockMvc.perform(requestBuilder);
        if(print) {
            ra.andDo(print());
        }
        if(expectedStatus > -1) {
            ra.andExpect(status().is(expectedStatus));
        }
        final MvcResult result = ra.andReturn();
       
//        final MockHttpServletRequest req = result.getRequest();
//        if(req != null) {
//            System.out.println("MockHttpServletRequest.userPrincipal: " + req.getUserPrincipal());
//        }
        
        final ModelAndView mv = result.getModelAndView();
//        System.out.println("ModelAndView: " + mv);
        if(trace && mv != null) {
            //sxSX
            System.out.println("ModelAndView.modelMap: " + mv.getModelMap());
            System.out.println("ModelAndView.httpStatus: " + mv.getStatus());
            System.out.println("ModelAndView.viewName: " + mv.getViewName());
        }
        final MockHttpServletResponse res = result.getResponse();
//        System.out.println("MockHttpServletResponse: " + res);
        if(trace && res != null) {
            System.out.println("MockHttpServletResponse.contentType: " + res.getContentType());
            System.out.println("MockHttpServletResponse.errorMessage: " + res.getErrorMessage());
            for(String header : res.getHeaderNames()) {
                System.out.println("MockHttpServletResponse.header(" + header + "): " + res.getHeaderValues(header));
            }
        }
        
        final Cookie [] cookies = res.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                cookieList.add(cookie);
                System.out.println("MockHttpServletResponse.Cookie: " + cookie.getName() + "=" + cookie.getValue());
            }
        }
    }
}

