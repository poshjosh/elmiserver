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

package com.bc.amex.controllers;

import com.bc.amex.exceptions.LoginException;
import com.bc.amex.jpa.selectors.Selectors;
import com.bc.amex.services.ControllerService;
import com.bc.config.Config;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.User;
import com.bc.jpa.dao.search.SearchResults;
import com.bc.socket.io.messaging.data.Devicedetails;
import com.looseboxes.mswordbox.Cache;
import com.looseboxes.mswordbox.net.GetDevicedetails;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.looseboxes.mswordbox.net.Rest;
import java.util.ArrayList;
import java.util.function.Function;
import org.springframework.core.env.Environment;
import com.looseboxes.mswordbox.Mapper;
import com.looseboxes.mswordbox.MsKioskConfiguration;
import com.looseboxes.mswordbox.config.ConfigFactory;
import com.looseboxes.mswordbox.config.ConfigService;
import com.looseboxes.mswordbox.net.Response;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 22, 2019 10:50:04 PM
 */
@RestController
public class RestControllerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(RestControllerImpl.class);
    
    @Autowired private Environment env;
    
    @Autowired private ControllerService controllerService;
    
    @Qualifier(MsKioskConfiguration.DEFAULT_CACHE_NAME) 
    @Autowired private Cache cache;
    
    @Lazy @Autowired private GetDevicedetails getDevicedetails;
    
    @Autowired private Mapper mapper;
    
    @Autowired private ResponseBuilder responseBuilder;
    
    @Autowired private ConfigFactory configFactory;
    
    @Autowired private PartParser partParser;
    
    @RequestMapping(Rest.ENDPOINT_COMBO)
    public Response combo(
            @RequestParam(value=Params.USERNAME, required=false) String username,
            @RequestParam(value=Params.PASSWORD, required=false) String password,
            HttpServletRequest request) {
        
        try{
            
            final Devicedetails dd = partParser.parse(
                    request, Devicedetails.class.getSimpleName(), Devicedetails.class);

            LOG.debug("Executing {} with:\nusername: {}, password: *****, {}", 
                    Rest.ENDPOINT_COMBO, username, dd);
            
            Object loginError = null;
            
            User user = null;
            try{
                user = this.controllerService.loginIfNeed(username, password, request).orElse(null);
            }catch(LoginException e) {
                loginError = e.getLocalizedMessage();
            }

            final Response output;

            if(user == null) {
                
                if(loginError == null) {
                    loginError = "Unexpected Exception";
                }
                
                output = this.responseBuilder.buildErrorResponse(loginError);

            }else{

                output = new Response();

                final Map body = new HashMap();

                final Map userMap = mapper.toMap(user);

                body.put(Rest.RESULTNAME_USER, userMap);

                final StringBuilder errors = new StringBuilder();

                try{
                    
                    dd.setUsername(username);
                    dd.setTimestamp(System.currentTimeMillis());

                    final List<Devicedetails> ddList = addDevicedetails(dd);

                    body.put(Rest.RESULTNAME_DEVICEDETAILS, toListOfMaps(ddList));

                }catch(IOException e) {
                    final String msg = "Failed to access Devicedetails from store"; 
                    errors.append(msg).append("\n");
                    LOG.warn(msg, e);
                }

                final List<Test> testList = this.getTestList(null, Selectors.FUTURE);
                body.put(Rest.RESULTNAME_TESTS, toListOfMaps(testList));

                output.setBody(body);

                this.responseBuilder.appendResponseHeaders(output, 
                        errors.length() == 0 ? "success" : errors.toString(), 
                        errors.length() > 0);
            }

            LOG.debug("For {}, output: {}", Rest.ENDPOINT_COMBO, output);

            return output;
            
        }catch(RuntimeException e) {

            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }
    
    @GetMapping(Rest.ENDPOINT_GET_TESTS)
    public Response getTests(
            @RequestParam(value=Params.QUERY, required=false) String query,
            @RequestParam(value=Params.SELECTOR, required=false) String selector) {
    
        try{
            
            LOG.debug("Executing {}, wth query: {}, selector: {}", 
                    Rest.ENDPOINT_GET_TESTS, query, selector);

            final List<Test> list = getTestList(query, selector);

            final Response output = responseBuilder.buildResponse(list, 
                    list.size() + " Tests returned for query: " + query + ", selector: " + selector, false);

            LOG.debug("For {}, output: {}", Rest.ENDPOINT_GET_TESTS, output);

            return output;
            
        }catch(RuntimeException e) {

            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }

    @GetMapping(Rest.ENDPOINT_GET_TEST)
    public Response getTest(@RequestParam(value=Params.MODELID, required=true) String modelid) {

        try{
            LOG.debug("Executing {} with: {} = {}", Rest.ENDPOINT_GET_TESTS, Params.MODELID, modelid);

            final Test test = (Test)controllerService.fetchModelobjectFromDatabase("Test", modelid);

            final Response output = responseBuilder.buildResponse(
                    test, "1 Test returned for id: " + modelid, false);

            LOG.debug("For {}, output: {}", Rest.ENDPOINT_GET_TESTS, output);

            return output;
            
        }catch(RuntimeException e) {

            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }
    
    @RequestMapping(Rest.UPDATE_DEVICE_DETAILS)
    public Response updateDevicedetails(HttpServletRequest request) {

        try{
            
            final Devicedetails dd = partParser.parse(
                    request, Devicedetails.class.getSimpleName(), Devicedetails.class);

            putNetworkClientDevicedetails(dd);

            return getNetworkClientDevicedetails();

        }catch(RuntimeException e) {

            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }
    
    @PutMapping(Rest.ENDPOINT_PUT_DEVICEDETAILS)
    public Response putNetworkClientDevicedetails(HttpServletRequest request) {

        try{
            final Devicedetails dd = partParser.parse(
                    request, Devicedetails.class.getSimpleName(), Devicedetails.class);

            return putNetworkClientDevicedetails(dd);

        }catch(RuntimeException e) {

            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }
    
    public Response putNetworkClientDevicedetails(Devicedetails dd) {
        
        LOG.debug("Executing {} width: {}", Rest.ENDPOINT_PUT_DEVICEDETAILS, dd);

        Response output;
        try{
            
            addDevicedetails(dd);
            
            output = responseBuilder.buildSuccessResponse();

        }catch(IOException e) {

            final String msg = "Unexpected exception";
            
            LOG.warn(msg, e);

            output = responseBuilder.buildErrorResponse(msg);
        }
        
        LOG.debug("For {}, output: {}", Rest.ENDPOINT_PUT_DEVICEDETAILS, output);

        return output;
    }

    @GetMapping(Rest.ENDPOINT_GET_DEVICEDETAILS)
    public Response getNetworkClientDevicedetails() {
        
        LOG.debug("Executing {}", Rest.ENDPOINT_GET_DEVICEDETAILS);

        try{

            final List ddList = getDevicedetailsList();
            
            final Response output = responseBuilder.buildResponse(ddList, 
                    ddList.size() + " Devicedetails returned", false);

            LOG.debug("For {}, output: {}", Rest.ENDPOINT_GET_DEVICEDETAILS, output);
            
            return output;
            
        }catch(IOException | RuntimeException e) {

            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }
    
    public List<Test> getTestList(String query, String selector) {
    
        final SearchResults<Test> searchResults = controllerService.search(
                Test.class.getSimpleName(), null, query, selector);
        
        final List<Test> output = new ArrayList<>(searchResults.stream().collect(Collectors.toSet()));

        LOG.debug("For query: {}, selector: {}, returning:\n{}", query, selector, output);
        
        return output;
    }
    
    public List<Devicedetails> addDevicedetails(Devicedetails dd) throws IOException{
        final List<Devicedetails> ddList = this.getDevicedetailsList();
        if(dd != null) {
            final boolean added = this.addToCache(ddList, dd);
            LOG.debug("Added to cache: {}, device details: {}", added, dd);
        }
        return ddList;
    }
    
    public List<Devicedetails> getDevicedetailsList() throws IOException{
    
        final String key = Rest.RESULTNAME_DEVICEDETAILS;
        
        List<Devicedetails> output = null;
        try{
            output = cache.getListFromJson(key, Devicedetails.class, null);
        }catch(IOException | ParseException e) {
            LOG.warn("Failed to get `" + key + "` from cache", e);
        }
        
        if(output == null) {
            output = new ArrayList<>();
        }
        
        final Config config = configFactory.getConfig(ConfigService.APP_PROTECTED);
        final Devicedetails dd = getDevicedetails.apply(config);
        
        try{
            this.addToCache(output, dd);
        }catch(IOException e) {
            LOG.warn("Failed to add own `" + key + "` to cache", e);
        }
        
        LOG.debug("{} = {}", key, output);
        
        return output;
    }
    
    public boolean addToCache(List<Devicedetails> output, Devicedetails dd) throws IOException{

        final String u = dd.getUsername();
        if(isNullOrEmpty(u)) {
            return false;
        }
        
        final String ip = dd.getIpaddress();
        if(isNullOrEmpty(ip)) {
            return false;
        }
        
        try{
            InetAddress.getByName(ip);
        }catch(Throwable ignored) { 
            return false;
        }
        
        final String key = Rest.RESULTNAME_DEVICEDETAILS;
        
        this.removeAll(output, dd);
        output.add(dd);

        return cache.putAsJson(key, output) != null;
    }
    
    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    private List<Map> toListOfMaps(List<?> list) {
        
        final Function<Object, Map> toMap = (object) -> {
            try{
//                final boolean test = (object instanceof Test);
//                if(test) {
//                    LOG.debug("Before converting to map, starttime: {}", ((Test)object).getStarttime());
//                }
                final Map testMap = mapper.toMap(object);
//                if(test) {
//                    LOG.debug(" After converting to map, starttime: {}", testMap.get("starttime"));
//                }
                return testMap;
            }catch(RuntimeException e) {
                LOG.warn("Unexpected exception", e);
                throw e;
            }
        };
        
        return list == null || list.isEmpty() ? Collections.EMPTY_LIST : 
                    list.stream().map(toMap).collect(Collectors.toList());
    }
    
    public boolean removeAll(List<Devicedetails> ddList, Devicedetails candidate) {
        final List<Devicedetails> toRemove = new ArrayList<>();
        for(Devicedetails dd : ddList) {
            if(Objects.equals(dd.getUsername(), candidate.getUsername())) {
                toRemove.add(dd);
            }
        }
        return toRemove.isEmpty() ? false : ddList.removeAll(toRemove);
    }
    
    public boolean contains(List<Devicedetails> ddList, Devicedetails candidate) {
        for(Devicedetails dd : ddList) {
            if(Objects.equals(dd.getUsername(), candidate.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
