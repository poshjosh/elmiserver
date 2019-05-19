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

import com.bc.amex.services.ControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 12, 2019 2:36:10 PM
 */
@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);
    
    @Autowired private ControllerService controllerService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        
        LOG.debug("Error: {}, logout: {}", error, logout);
        
        if (error != null) {
            
            if(error.isEmpty()) {
                controllerService.addErrorMessage(model, "An unexpected error occured");
            }else{
                controllerService.addErrorMessage(model, "You provided invalid login credentials.");
            }
        }    
        
        if (logout != null) {
            
            controllerService.addInfoMessage(model, "You have been logged out successfully.");
        }    
        
        return "login";
    }
}
