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

package com.bc.amex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 25, 2019 6:24:13 PM
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SearchPageNotFoundEception extends RuntimeException {

    /**
     * Creates a new instance of <code>SearchPageNotFoundEception</code> without detail message.
     */
    public SearchPageNotFoundEception() {
    }


    /**
     * Constructs an instance of <code>SearchPageNotFoundEception</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SearchPageNotFoundEception(String msg) {
        super(msg);
    }
}
