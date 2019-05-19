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

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 3:42:03 PM
 */
public interface PartParser {

    <T> T parse(HttpServletRequest request, String name, Class<T> type);

    <T> T parse(Part part, HttpServletRequest request, Class<T> type);

    <T> T parse(Part part, String encoding, Class<T> type);

    <T> T parse(String name, InputStream is, String encoding, Class<T> type);

    <T> T parse(String name, byte[] bytes, String encoding, Class<T> type);

    <T> T parse(MultipartFile multipartFile, HttpServletRequest request, Class<T> type);

    <T> T parse(MultipartFile multipartFile, String encoding, Class<T> type);
}
