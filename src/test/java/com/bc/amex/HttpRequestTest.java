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

import com.bc.amex.controllers.Params;
import com.bc.amex.form.FormConfiguration;
import com.bc.amex.jpa.JpaConfiguration;
import com.bc.app.spring.BcspringbootConfiguration;
import com.bc.app.spring.lifecycle.LifecycleConfiguration;
import com.bc.app.spring.server.TomcatProperties;
import com.looseboxes.mswordbox.MsKioskConfiguration;
import com.looseboxes.mswordbox.ui.UiConfiguration;
import com.looseboxes.mswordbox.ui.admin.AdminUiConfiguration;
import com.looseboxes.mswordbox.ui.exam.ExamUiConfiguration;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 17, 2019 9:34:12 AM
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {"com.bc.app.spring", "com.bc.amex"})//, "com.looseboxes.mswordbox"})
@ContextConfiguration(classes = {
    LifecycleConfiguration.class, BcspringbootConfiguration.class, 
    AmexConfiguration.class, JpaConfiguration.class, FormConfiguration.class,
    MsKioskConfiguration.class, UiConfiguration.class, AdminUiConfiguration.class, ExamUiConfiguration.class,
    TomcatProperties.class
})
@EnableAutoConfiguration
public class HttpRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    private static final List<String> cookieList = new ArrayList<>(); 
    
    @Test
    public void contextLoads() throws Exception {
        System.out.println("contextLoads()");
        exchange(HttpMethod.POST, "/login", String.class);
    }

    public <T> ResponseEntity<T> exchange(HttpMethod method, String name, Class<T> resType) throws Exception {
        return exchange(method, name, null, resType);
    }
    
    public <T> ResponseEntity<T> exchange(HttpMethod method, String name, Class modelobjectType, Class<T> resType) throws Exception {
        System.out.println(name);
        
        
        final RequestEntity req = RequestEntity.method(method, new URI(name))
                .header("Cookie", cookieList.stream().collect(Collectors.joining(";")))
                .build();
//        System.out.println("RequestEntity: " + req);
        
        final String target = getTarget(name);
        final Map targetParams = getTargetVariables(name, modelobjectType);
        
        final ResponseEntity<T> res = authenticatedRestTemplate()
                .exchange(target, method, req, resType, targetParams);

        if(res != null) {
//            System.out.println("ResponseEntity: " + res);
        
            final HttpHeaders hh = res.getHeaders();
//            System.out.println("ResponseEntity.headers: " + hh);
            
            if(hh != null) {

                final List<String> cookies = hh.get("Set-Cookie");
                if(cookies != null) {
                    cookieList.addAll(cookies);
                }

                final Map map = hh.toSingleValueMap();
//                System.out.println("Headers: " + map);
            }

//            System.out.println("ResponseEntity.body: " + res.getBody());
        }

        return res;
    }

//    @Test
    public void login() throws Exception {
        this.testPostForLocation("/login", null);
    }
    
    public void testPostForLocation(String name, URI expected) {
        final URI result = this.authenticatedRestTemplate().postForLocation(getTarget(name), null, getTargetVariables(name, null));
        System.out.println("Expected: " + expected + "\n   Found: " + result);
        if(expected != null) {
            assertEquals(expected, result);
        }
    }
    
    public TestRestTemplate authenticatedRestTemplate() {
        return this.restTemplate.withBasicAuth(UserDetails.USERNAME_VALUE, UserDetails.PASSWORD_VALUE);
    }

    public Map getTargetVariables(String name, Class modelobjectType) {
        final Map map = new HashMap();
        addTargetVariables(map, name, modelobjectType);
        return map;
    }
    
    public void addTargetVariables(Map map, String name, Class modelobjectType) {
        map.put("username", UserDetails.USERNAME_VALUE);
        map.put("password", UserDetails.PASSWORD_VALUE);
        if(modelobjectType != null) {
            map.put(Params.MODELNAME, modelobjectType.getSimpleName());
        }
    }

    public String getTarget(String name) {
        return "http://localhost:" + port + '/' + name;
    }
}
/**
 * 
 * 
    
//    @Test
    public void forCreateshowForm() throws Exception {
        final String name = "/"+CrudActionNames.CREATE+"/showForm"; 
        System.out.println(name);
        
//        final String output = this.restTemplate.getForObject(getTarget(name), String.class, getTargetVariables(name, User.class));
        final RequestEntity req = RequestEntity.get(new URI(name))
                .header("Cookie", cookieList.stream().collect(Collectors.joining(";")))
                .build();
        System.out.println("RequestEntity: " + req);
        
        final ResponseEntity res = authenticatedRestTemplate().exchange(getTarget(name), HttpMethod.GET, req, String.class, getTargetVariables(name, User.class));
        res.getHeaders().get("Set-Cookie").stream().forEach((cookie) -> cookieList.add(cookie));

        System.out.println("ResponseEntity: " + res);
        final HttpHeaders hh = res.getHeaders();
        System.out.println("ResponseEntity.headers: " + hh);
        final Map map = hh.toSingleValueMap();
        System.out.println("Headers: " + map);
        System.out.println("ResponseEntity.body: " + res.getBody());
    }
 * 
 */