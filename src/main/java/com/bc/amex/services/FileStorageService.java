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

package com.bc.amex.services;

import com.bc.app.spring.functions.GetUniquePathForFilename;
import com.bc.app.spring.services.FileStorage;
import com.bc.app.spring.services.FileStorageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2019 9:39:05 PM
 */
@Service
public class FileStorageService extends FileStorageHandler{

    public static final String DOWNLOAD_PATH_CONTEXT = "/downloadFile";
    
    public FileStorageService(
            @Autowired GetUniquePathForFilename getUniquePathForFilename, 
            @Autowired FileStorage fileStorage) {
        super(getUniquePathForFilename, fileStorage, DOWNLOAD_PATH_CONTEXT);
    }
}
