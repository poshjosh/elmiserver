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

import com.bc.amex.form.FormConfig;
import com.bc.amex.services.ControllerService;
import com.bc.amex.form.IsTestCoordinatorsForm;
import com.bc.amex.services.DocumentService;
import com.bc.amex.services.FileStorageService;
import com.bc.app.spring.entities.UploadFileResponse;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.Test_;
import com.bc.elmi.pu.entities.Testsetting;
import com.bc.web.form.FormBean;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 8:20:33 PM
 */
@Controller
@SessionAttributes({ModelAttributes.MODELOBJECT, ModelAttributes.FORM}) //@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
public class TestController extends CrudController{

    private static final Logger LOG = LoggerFactory.getLogger(CrudControllerImpl.class);
    
    public static final String TEST_SETTINGS = "testsettings";
    
    private final String modelname = Test.class.getSimpleName();
    
    @Autowired private IsTestCoordinatorsForm isTestCoordinatorsForm;
    @Autowired private DocumentService documentService;
    
    private final Map<Object, Map<String, UploadFileResponse>> cache = new HashMap<>();
    private final Map<Object, Map<String, List<UploadFileResponse>>> cache_multi = new HashMap<>();
    
    public TestController(
            @Autowired ControllerService controllerService, 
            @Autowired FileStorageService fileStorageService) {
        super(controllerService, 
                fileStorageService,
                new OnCrudFormSubmitted(
                        controllerService.getTypeFromNameResolver(),
                        controllerService.getEntityRepositoryFactory()
                )
        );
    }

    @Override
    public Map<String, UploadFileResponse> uploadSingleFiles(
            String modelname, Object modelobject, Map<String, MultipartFile> fileMap) {
        
        final Test test = (Test)modelobject;
        
        final Map<String, UploadFileResponse> output = 
                super.uploadSingleFiles(modelname, modelobject, fileMap); 
        
        LOG.debug("Adding to singles cache: {} values: {}", output.size(), output);

        cache.put(getKey(test), output);

        return output;
    }

    @Override
    public Map<String, List<UploadFileResponse>> uploadMultipleFiles(
            String modelname, Object modelobject, MultiValueMap<String, MultipartFile> fileMap) {

        final Test test = (Test)modelobject;
        
        final Map<String, List<UploadFileResponse>> output = 
                super.uploadMultipleFiles(modelname, modelobject, fileMap); 
        
        LOG.debug("Adding to multi cache: {} values: {}", output.size(), output);
        
        cache_multi.put(getKey(test), output);
        
        return output;
    }

    @Override
    public String onFormSubmitSuccessful(Model model, FormConfig formConfig, 
            HttpServletRequest request, HttpServletResponse response) {
        
        persistIfPresent(request, Testsetting.class, cache);
        
        persistMultiIfPresent(request, Testsetting.class, cache_multi);
        
        return super.onFormSubmitSuccessful(model, formConfig, request, response);
    }

    public void persistIfPresent(HttpServletRequest request, Class type, 
            Map<Object, Map<String, UploadFileResponse>> cache) {
        
        final Test test = (Test)request.getSession().getAttribute(ModelAttributes.MODELOBJECT);

        final Object key = getKey(test);

        try{
            
            final Map<String, UploadFileResponse> map = cache.get(key);

            if(map != null && !map.isEmpty()) {

                for(String name : map.keySet()) {

                    final UploadFileResponse ufr = map.get(name);

                    documentService.persistDocAndRef(ufr, request, test, type);
                }
            }
        }finally{

            cache.remove(key);
        }
    }
    
    public void persistMultiIfPresent(HttpServletRequest request, Class type, 
            Map<Object, Map<String, List<UploadFileResponse>>> cache) {
        
        final Test test = (Test)request.getSession().getAttribute(ModelAttributes.MODELOBJECT);

        final Object key = getKey(test);

        try{
            
            final Map<String, List<UploadFileResponse>> map = cache.get(key);
        
            if(map != null && !map.isEmpty()) {

                for(String name : map.keySet()) {

                    final List<UploadFileResponse> arr = map.get(name);

                    for(UploadFileResponse ufr : arr) {

                        documentService.persistDocAndRef(ufr, request, test, type);
                    }
                }
            }
        }finally{

            cache.remove(key);
        }
    }

    public Object getKey(Test test) {
        return test.getTestname() + test.getStarttime();
    }

    @GetMapping("/{"+Params.ACTION+"}/Test/showForm")
    public String showForm(ModelMap model, 
            @PathVariable(name=Params.ACTION, required=true) String action,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) {
        
        final String template = super.showForm(model, action, modelname, modelid, modelfields, request, response);
        
        final Test modelobject = this.getControllerService()
                .findAttributeOptional(model, request, ModelAttributes.MODELOBJECT, Test.class)
                .orElseThrow(() -> new EntityNotFoundException("Entity named Test with id: " + modelid));
        
        if(modelfields != null && isTestCoordinatorsForm.test(modelobject, Arrays.asList(modelfields))) {
            
            Object formobj = model.get(ModelAttributes.FORM);
            if(formobj == null) {
                formobj = request.getSession().getAttribute(ModelAttributes.FORM);
            }
            if(formobj instanceof FormBean) {
                ((FormBean)formobj).setDisplayName("Test Coordinator(s)");
            }else{
                this.getControllerService().addInfoMessage(model, "*** Add Test Coordinators ***");
            }
            
            final Object name = modelobject.getTestname();
            final Object starttime = modelobject.getStarttime();
            LOG.debug("Test name: {}, starttime: {}", name, starttime);
            
            final Object timestr = starttime == null ? "TBD" : new SimpleDateFormat("HH:mm dd MMM yy").format(starttime);
            this.getControllerService().addInfoMessage(model, "Test Name: " + name);
            this.getControllerService().addInfoMessage(model, "Date-Time: " + timestr);
        }
        
        return template;
    }

    @PostMapping("/{"+Params.ACTION+"}/Test/validateForm")
    public String validateForm(
            @Valid @ModelAttribute(ModelAttributes.MODELOBJECT) Object modelobject,
            BindingResult bindingResult,
            Model model,
            @PathVariable(name=Params.ACTION, required=true) String action,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            MultipartHttpServletRequest request, HttpServletResponse response) {
            
        return super.validateForm(modelobject, bindingResult, model, action, modelname, modelfields, request, response);
    }    

    @RequestMapping("/{"+Params.ACTION+"}/Test/submitForm")
    public String submitForm(
            Model model,
            @PathVariable(name=Params.ACTION, required=true) String action,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) {

        return super.submitForm(model, action, modelname, modelid, modelfields, request, response);
    } 

    @Override
    public Optional<String> getTargetAfterSubmit(FormConfig formConfig) {
        
        final String output;
        
        final String action = formConfig.getAction();
        
        if(CrudController.CREATE.equals(action) || CrudController.UPDATE.equals(action)) {
            
            final Object modelobject = formConfig.getModelobject();
            
            if(modelobject != null && Test.class.isAssignableFrom(modelobject.getClass())) {

                final Object id = formConfig.getModelid() != null ? formConfig.getModelid() : 
                        this.getEntityRepositoryFactory().forEntity(modelobject.getClass())
                                .getIdOptional(modelobject).orElse(null);
                
                if(id != null) {
                
                    output = "redirect:/" + CrudController.UPDATE + '/' + Test.class.getSimpleName() + 
                            "/showForm?modelid=" + id + "&modelfields=" + Test_.userList.getName();
                    
                }else{
                    
                    LOG.warn("Failed to resolve id for {}", formConfig);
                    
                    output = null;
                }
            }else{
                
                output = null;
            }
        }else{
    
            output = null;
        }
        
        return Optional.ofNullable(output);
    }    
}
