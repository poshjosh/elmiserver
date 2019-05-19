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

import com.bc.amex.jpa.selectors.Selectors;
import com.bc.amex.services.ControllerService;
import com.bc.amex.services.FileStorageService;
import com.bc.elmi.pu.entities.Message;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 7:41:02 PM
 */
@Controller
public class SearchController extends CrudController{
    
    public static final String SEARCH_INBOX = "/search/Message/my/" + Selectors.INBOX;
    public static final String SEARCH_OUTBOX = "/search/Message/my/" + Selectors.OUTBOX;

    public SearchController(
            @Autowired ControllerService controllerService, 
            @Autowired FileStorageService fileStorageService) {
        super(controllerService, fileStorageService);
    }

    @RequestMapping(SEARCH_INBOX)
    public String searchMessagesMyInbox(ModelMap model, 
            @RequestParam(value=Params.QUERY, required=false) String query,
            HttpServletRequest request, HttpServletResponse response) {
        
        return super.search(model, Message.class.getSimpleName(), null, 
                query, Selectors.INBOX, request, response);
    }

    @RequestMapping(SEARCH_OUTBOX)
    public String searchMessagesMyOutbox(ModelMap model, 
            @RequestParam(value=Params.QUERY, required=false) String query,
            HttpServletRequest request, HttpServletResponse response) {
        
        return super.search(model, Message.class.getSimpleName(), null, 
                query, Selectors.OUTBOX, request, response);
    }

    @RequestMapping("/search/{"+Params.MODELNAME+"}")
    public String searchModel(ModelMap model, 
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.QUERY, required=false) String query,
            @RequestParam(value=Params.SELECTOR, required=false) String selector,
            HttpServletRequest request, HttpServletResponse response) {
        
        return super.search(model, modelname, modelid, query, selector, request, response);
    }
    
    @RequestMapping("/search")
    @Override
    public String search(ModelMap model, 
            @RequestParam(value=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.QUERY, required=false) String query,
            @RequestParam(value=Params.SELECTOR, required=false) String selector,
            HttpServletRequest request, HttpServletResponse response) {
        
        return super.search(model, modelname, modelid, query, selector, request, response);
    }

    @GetMapping("/searchresults/{"+Params.MODELNAME+"}/page/{"+Params.PAGE+"}")
    public String searchresultsForModel(ModelMap model, 
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @PathVariable(name=Params.PAGE, required=true) Integer page,
            HttpServletRequest request, HttpServletResponse response) {
        
        return super.searchresults(model, modelname, page, request, response);
    }
    
    @GetMapping("/searchresults")
    @Override
    public String searchresults(ModelMap model, 
            @RequestParam(value=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.PAGE, required=true) Integer page,
            HttpServletRequest request, HttpServletResponse response) {
        
        return super.searchresults(model, modelname, page, request, response);
    }
}
