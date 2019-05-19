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

import com.bc.elmi.pu.entities.User;
import com.bc.objectgraph.MapBuilderImpl;
import com.bc.socket.io.messaging.data.Designations;
import com.bc.socket.io.messaging.data.Devicedetails;
import com.bc.util.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.looseboxes.mswordbox.net.OkHttpRequestClient;
import com.looseboxes.mswordbox.net.Rest;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import org.json.simple.JSONValue;
import com.looseboxes.mswordbox.Mapper;
import com.looseboxes.mswordbox.MapperJackson;
import com.looseboxes.mswordbox.net.DefaultHeaders;
import com.looseboxes.mswordbox.net.RequestClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import okhttp3.ResponseBody;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 27, 2019 10:41:23 PM
 */
public class Main {

    public static void main(String... args) {
        try{
            final OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true) 
                    .cookieJar(CookieJar.NO_COOKIES).build(); 
            
            final Function<okhttp3.Response, String> converter = (r) -> {
                try(final ResponseBody body = r.body()) {
                    return body.string();
                }catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }; 

            final RequestClient<String> reqClient = new OkHttpRequestClient(httpClient, converter);
            reqClient.headers(new DefaultHeaders(StandardCharsets.UTF_8, "Me", "application/json"));

            final Map<String, String> params = new HashMap<>(2, 1.0f);
            params.put("username", "STUDENT");
            params.put("password", "1234567");

            final Devicedetails dd = new Devicedetails();
//            dd.setDescription(description);
            dd.setDesignation(Designations.CLIENT);
            dd.setIpaddress("localhost");
            dd.setName("Nonny");
            dd.setPort(8778);
            dd.setUsername("Guest");

            final String json = new ObjectMapper().writeValueAsString(dd);
            System.out.println("Devicedetails: " + json);

            final Map<String, String> parts = Collections.singletonMap("Devicedetails", json);

            final URL url = new URL("http://localhost:8080" + Rest.ENDPOINT_COMBO);

            reqClient.params(params, false).bodyParts(parts).execute("POST", url, (data) -> {
                
                try{
                    
                    final com.looseboxes.mswordbox.net.Response r = new MapperJackson()
                            .toObject(data, com.looseboxes.mswordbox.net.Response.class);
                    
                    if(r.isError()) {
                        System.out.println(r);
                    }else{
                        final Map body = (Map)r.getBody();

                        System.out.println(body.get(Rest.RESULTNAME_USER));
                        System.out.println(body.get(Rest.RESULTNAME_TESTS));
                        System.out.println(body.get(Rest.RESULTNAME_DEVICEDETAILS));

                        System.out.println(Rest.RESULTNAME_USER + " = " + body.get(Rest.RESULTNAME_USER).getClass());
                        System.out.println(Rest.RESULTNAME_TESTS + " = " + body.get(Rest.RESULTNAME_TESTS).getClass());
                        System.out.println(Rest.RESULTNAME_DEVICEDETAILS + " = " + body.get(Rest.RESULTNAME_DEVICEDETAILS).getClass());
//                tempJack(data);
                
//                temp(data);
                    }
                }catch(Throwable t) {
                    t.printStackTrace();
                }
            });
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    private static void tempJack(String content) {
        try{
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode node = mapper.readTree(content);
            if(node.hasNonNull(Rest.RESULTNAME_USER)) {
                final JsonNode userNode = node.get(Rest.RESULTNAME_USER);
                final String json = mapper.writeValueAsString(userNode);
                final User user = mapper.readValue(json, User.class);
                print(user);
            }
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private static void temp(String content) {
        try{
            
            final Map responseData = (Map)JSONValue.parseWithException(content);
            
            System.out.println(new JsonFormat(true, true, "    ").toJSONString(responseData));

            final Map userMap = (Map)responseData.getOrDefault(Rest.RESULTNAME_USER, null);

            if(userMap != null && !userMap.isEmpty()) {
                try{ 
                

                    final Mapper mapConverter = new MapperJackson();
                    
                    final User u = mapConverter.toObject(userMap, User.class);

                    print(u);
                
                }catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }catch(Throwable t) {
            t.printStackTrace();
        }    
    }
    
    private static void print(User u) {
        System.out.println("User: " + u);
        if(u.getAppointment() != null){
            final Map appt = new MapBuilderImpl().maxCollectionSize(100).maxDepth(3)
                    .nullsAllowed(false).source(u.getAppointment()).build();
            System.out.println("Appointment: " + appt);
        }
        System.out.println("Date of birth: " + u.getDateofbirth());
        System.out.println("Email: " + u.getEmailaddress());
        System.out.println("Username: " + u.getUsername());
        if(u.getGender() != null) {
            final Map gender = new MapBuilderImpl().maxCollectionSize(100).maxDepth(3)
                    .nullsAllowed(false).source(u.getGender()).build();
            System.out.println("Gender: " + gender);
        }
        if(u.getUnit() != null) {
            final Map unit = new MapBuilderImpl().maxCollectionSize(100).maxDepth(3)
                    .nullsAllowed(false).source(u.getUnit()).build();
            System.out.println("Unit: " + unit);
        }
        final List roleList = u.getRoleList().stream().map((role) -> 
                    new MapBuilderImpl().maxCollectionSize(100).maxDepth(3)
                .nullsAllowed(false).source(role).build())
                .collect(Collectors.toList());
        for(Object o : roleList) {
            System.out.println("Role: " + o);
        }
    }
}
