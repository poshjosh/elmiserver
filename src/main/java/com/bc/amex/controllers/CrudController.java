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

import com.bc.amex.exceptions.SearchPageNotFoundEception;
import com.bc.amex.services.ControllerService;
import com.bc.amex.form.FormConfig;
import com.bc.amex.jpa.selectors.ParametersSelector;
import com.bc.amex.jpa.selectors.Selector;
import com.bc.amex.searchresults.SearchresultPage;
import com.bc.amex.services.FileStorageService;
import com.bc.app.spring.services.FileStorageHandler;
import com.bc.jpa.dao.search.SearchResults;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.bc.web.form.Form;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 7, 2019 10:49:00 PM
 */
public class CrudController extends AbstractFormController implements CrudActionNames{

    private static final Logger LOG = LoggerFactory.getLogger(CrudController.class);

    @FunctionalInterface
    public static interface OnFormSubmitted{
        void onFormSubmitted(FormConfig formConfig);
    }
    
    private final OnFormSubmitted onFormSubmitted;
    
    public CrudController(
            @Autowired ControllerService controllerService, 
            @Autowired FileStorageService fileStorageService) {
        this(controllerService, 
                fileStorageService,
                new OnCrudFormSubmitted(
                        controllerService.getTypeFromNameResolver(),
                        controllerService.getEntityRepositoryFactory()
                )
        );
    }

    public CrudController(
            ControllerService controllerService,
            FileStorageHandler fileStorageHandler,
            OnFormSubmitted onSubmitted) {
        super(controllerService, fileStorageHandler);
        this.onFormSubmitted = Objects.requireNonNull(onSubmitted);
    }

    public String search(ModelMap model, 
            String modelname, String modelid, String query, String selectorName,
            HttpServletRequest request, HttpServletResponse response) {
        
        final Selector format = new ParametersSelector(request.getParameterMap());
        
        final SearchResults searchResults =
                this.getControllerService().search(modelname, modelid, query, selectorName, format);
        
        this.prepareModel(model, modelname, searchResults, 0);
                
        request.getSession().setAttribute(modelname + ModelAttributes.SEARCHRESULTS, searchResults);
        
        return Templates.SEARCHRESULTS;
    }
    
    public String searchresults(ModelMap model, String modelname, Integer page,
            HttpServletRequest request, HttpServletResponse response) {
        
        final SearchResults searchResults = (SearchResults)request
                .getSession().getAttribute(modelname + ModelAttributes.SEARCHRESULTS);
        
        if(searchResults == null) {
            throw new SearchPageNotFoundEception();
        }
        
        searchResults.setPageNumber(page);
        
        this.prepareModel(model, modelname, searchResults, page);
        
        return Templates.SEARCHRESULTS;
    }
    
    public void prepareModel(ModelMap model, String modelname, SearchResults searchResults, int pageNumber) {
    
        final SearchresultPage resultPage = this.getControllerService()
                .buildSearchresultPage(searchResults, pageNumber);
        
        model.addAttribute(Params.MODELNAME, modelname);
        model.addAttribute(Params.PAGE, pageNumber);
        model.addAttribute(ModelAttributes.SEARCHRESULT_PAGE, resultPage);
    }

    public String showForm(ModelMap model, String action, String modelname, 
            String modelid, String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) {

        final Object modelobject = fetchOrCreateModelObject(model, action, modelname, modelid, request, response);
        
        final FormConfig formConfig = new FormConfig.Builder()
                .action(action).modelname(modelname)
                .modelid(modelid).modelobject(modelobject)
                .modelfields(modelfields == null ? Collections.EMPTY_LIST :Arrays.asList(modelfields)).build();
        
        LOG.debug("{}", formConfig);
        
        final boolean addToReq = ! CrudController.READ.equals(action);

        this.addAttributes(model, formConfig, addToReq ? request : null, response);

        return this.getTemplateForShowingForm(formConfig);
    }
    
    public Object fetchOrCreateModelObject(ModelMap model, String action, String modelname, 
            String modelid, HttpServletRequest request, HttpServletResponse response) {
        return modelid != null ? 
                this.getControllerService().fetchModelobjectFromDatabase(modelname, modelid) :
                this.getOrCreateModelobject(model, action, modelname, request, response);
    }

    public String validateForm(Object modelobject, BindingResult bindingResult, Model model,
            String action, String modelname, String [] modelfields,
            MultipartHttpServletRequest request, HttpServletResponse response) {
        
        final FormConfig formConfig = new FormConfig.Builder()
                .action(action).modelname(modelname)
                .modelobject(modelobject)
                .modelfields(modelfields == null ? Collections.EMPTY_LIST :Arrays.asList(modelfields)).build();
        
        LOG.debug("{}", formConfig);
        
        final List<Validator> validators = this.getControllerService().getValidators(formConfig);

        for(Validator validator : validators) {

            ValidationUtils.invokeValidator(validator, modelobject, bindingResult);
        }
        
        this.getControllerService().checkAttributes(action, modelname, modelobject);
  
        final String target;
        
        if (bindingResult.hasErrors()) {
            
            this.getControllerService().collectErrorsIntoModel(bindingResult, model);
            
            target = Templates.FORM;
            
        }else{
            
            this.upload(modelname, modelobject, request);
            
            final Form form = this.getControllerService().buildForm(formConfig);
        
            this.getControllerService().addAttributes(model, formConfig, form, request, response);
            
            target = Templates.FORM_CONFIRMATION;
        }

        return target;
    }    

    public String submitForm(Model model, 
            String action, String modelname, 
            String modelid, String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) {
        
        final Object modelobject = request.getSession().getAttribute(ModelAttributes.MODELOBJECT);
                
        final FormConfig formConfig = new FormConfig.Builder()
                .action(action).modelname(modelname)
                .modelid(modelid).modelobject(modelobject)
                .modelfields(modelfields == null ? Collections.EMPTY_LIST : Arrays.asList(modelfields)).build();
        
        LOG.debug("{}", formConfig);
        
        this.getControllerService().checkAttributes(action, modelname, modelobject);

        String target;
        try{
        
            this.onFormSubmitted.onFormSubmitted(formConfig);
            
            target = this.onFormSubmitSuccessful(model, formConfig, request, response);
            
        }catch(RuntimeException e) {
            
            target = this.onFormSubmitFailed(model, formConfig, e, request, response);
            
        }finally{
        
            this.getControllerService().removeAttributes(request, response);
        }
        
        LOG.debug("Target: {}", target);
        
        return target;
    } 

    public String getTemplateForShowingForm(FormConfig formConfig) {
        return this.getTemplateForShowingForm(formConfig.getAction());
    }
    
    public String getTemplateForShowingForm(String action) {
        final String template;
        switch(action) {
            case CREATE:
                template = Templates.FORM; break;
            case READ:
                template = Templates.FORM_DATA; break;
            case UPDATE:
                template = Templates.FORM; break;
            case DELETE:
                template = Templates.FORM_CONFIRMATION; break;
            default:
                throw new IllegalArgumentException("Unsupported action: " + action);
        }
        return template;
    }
}
