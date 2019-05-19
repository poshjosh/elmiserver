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

import com.bc.socket.io.CopyStream;
import com.looseboxes.mswordbox.Mapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chinomso Bassey Ikwuagwu on May 11, 2019 12:46:22 PM
 */
public class PartParserImpl implements PartParser {

    private static final Logger LOG = LoggerFactory.getLogger(PartParserImpl.class);
    
    private final Mapper mapper;

    public PartParserImpl(Mapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }
    
    @Override
    public <T> T parse(MultipartFile multipartFile, HttpServletRequest request, Class<T> type) {
    
        return parse(multipartFile, getCharacterEncodingOrDefault(request), type);
    }
    
    @Override
    public <T> T parse(MultipartFile multipartFile, String encoding, Class<T> type) {
        try{
            
            return parse(multipartFile.getName(), multipartFile.getBytes(), encoding, type);
            
        }catch(IOException e) {
            
            LOG.warn("Failed to convert to "+type+".\nPart: " + multipartFile, e);
        
            throw new ValidationException("Invalid request", e);
        }
    }

    @Override
    public <T> T parse(HttpServletRequest request, String name, Class<T> type) {
        try{
            return parse(request.getPart(name), request, type);
        }catch(IOException | ServletException e) {
            
            LOG.warn("Failed to convert to "+type+".\nPart: " + name, e);
        
            throw new ValidationException("Invalid request", e);
        }
    }
    
    @Override
    public <T> T parse(Part part, HttpServletRequest request, Class<T> type) {
    
        return parse(part, getCharacterEncodingOrDefault(request), type);
    }

    @Override
    public <T> T parse(Part part, String encoding, Class<T> type) {
        try{
            return parse(part.getName(), part.getInputStream(), encoding, type);
        }catch(IOException e) {
            
            LOG.warn("Failed to convert to "+type+".\nPart: " + part, e);
        
            throw new ValidationException("Invalid request", e);
        }
    }
    
    @Override
    public <T> T parse(String name, InputStream is, String encoding, Class<T> type) {
        try{
            final ByteArrayOutputStream b = new ByteArrayOutputStream(8192);
            new CopyStream().execute(is, b);
            return parse(name, b.toByteArray(), encoding, type);
        }catch(IOException e) {
            
            LOG.warn("Failed to convert to "+type+".\nPart: " + name, e);
        
            throw new ValidationException("Invalid request", e);
        }
    }
    
    @Override
    public <T> T parse(String name, byte [] bytes, String encoding, Class<T> type) {
        
        encoding = encoding == null || encoding.isEmpty() ? StandardCharsets.UTF_8.name() : encoding;
        
        try {
        
            final String str = new String(bytes, encoding);
            
            LOG.debug("String: {}", str);
            
            return mapper.toObject(str, type);
            
        }catch(IOException | java.text.ParseException e) {
            
            LOG.warn("Failed to convert to "+type+".\nPart: " + name, e);
        
            throw new ValidationException("Invalid request", e);
        }
    }

    public String getCharacterEncodingOrDefault(HttpServletRequest request) {
        return request.getCharacterEncoding() == null ? StandardCharsets.UTF_8.name() :
                request.getCharacterEncoding();
    }
}
