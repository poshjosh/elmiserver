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

import com.looseboxes.mswordbox.net.Response;
import java.util.Collections;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 26, 2019 4:09:29 PM
 */
public interface ResponseBuilder {

    default <T> void appendResponse(Response<Map<String, T>> esponse, 
            String name, T value, Object msg, boolean error) {
        appendResponse(esponse, Collections.singletonMap(name, value), msg, error);
    }

    <T> void appendResponse(Response<T> appendTo, T body, Object msg, boolean error);

    Response<String> buildErrorResponse(Object msg);

    default <T> Response<Map<String, T>> buildResponse(String name, T value, Object msg, boolean error) {
        return buildResponse(Collections.singletonMap(name, value), msg, error);
    }

    <T> Response<T> buildResponse(T body, Object msg, boolean error);

    Response<String> buildSuccessResponse();

    void appendErrorResponse(Response appendTo, Object msg);

    void appendSuccessResponse(Response appendTo);

    void appendResponseHeaders(Response appendTo, Object msg, boolean error);
}
