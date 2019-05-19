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
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 26, 2019 4:04:45 PM
 */
public class ResponseBuilderImpl implements ResponseBuilder {

    @Override
    public Response<String> buildSuccessResponse() {
        final Response<String> data = new Response<>();
        data.setCode(HttpServletResponse.SC_OK);
        appendSuccessResponse(data);
        return data;
    }
    
    @Override
    public Response<String> buildErrorResponse(Object msg) {
        final Response<String> data = new Response<>();
        data.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        appendErrorResponse(data, msg);
        return data;
    }

    @Override
    public <T> Response<T> buildResponse(T value, Object msg, boolean error) {
        final Response data = new Response();
        data.setCode(error ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : HttpServletResponse.SC_OK);
        appendResponse(data, value, msg, error);
        return data;
    }

    @Override
    public void appendSuccessResponse(Response appendTo) {
        appendResponse(appendTo, "success", "success", false);
    }

    @Override
    public void appendErrorResponse(Response appendTo, Object msg) {
        if("error".equals(msg)) {
            throw new IllegalArgumentException("'error' is a reserved word when build response via this api");
        }
        appendResponse(appendTo, msg, msg, true);
    }

    @Override
    public <T> void appendResponse(Response<T> appendTo, T value, Object msg, boolean error) {
        appendTo.setBody(value);
        appendResponseHeaders(appendTo, msg, error);
    }

    @Override
    public void appendResponseHeaders(Response appendTo, Object msg, boolean error) {
        appendTo.setError(error);
        if(msg != null) {
            appendTo.setMessage(msg.toString());
        }
    }
}
