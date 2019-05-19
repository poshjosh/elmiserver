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
import com.bc.amex.form.EntityFormProvider;
import com.bc.amex.form.FormConfig;
import com.bc.amex.jpa.TypeFromNameResolver;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.app.spring.entities.UploadFileResponse;
import com.bc.app.spring.services.FileStorageHandler;
import com.bc.reflection.ReflectionUtil;
import com.bc.web.form.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 19, 2019 9:52:57 PM
 */
public class AbstractFormController {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFormController.class);
    
    private final ControllerService controllerService;
    private final FileStorageHandler fileStorageHandler;
    
    public AbstractFormController(
            ControllerService controllerService,
            FileStorageHandler fileStorageHandler) {
        this.controllerService = Objects.requireNonNull(controllerService);
        this.fileStorageHandler = fileStorageHandler;
    }
    
    public String onFormSubmitSuccessful(
            Model model, FormConfig formConfig,
            HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("SUCCESS: {}", formConfig);
            
        final Object m = "Successfully completed action: " + 
                formConfig.getAction() + ' ' + formConfig.getModelname();
        
        controllerService.addInfoMessage(model, m);
        
        request.setAttribute(ModelAttributes.MESSAGES, Collections.singletonList(m));
        
//        return getTargetAfterSubmit(formConfig)
//                .orElse(controllerService.getRedirectToFormReferer(formConfig, request).orElse(Templates.SUCCESS));
        return getTargetAfterSubmit(formConfig).orElse(Templates.SUCCESS);
    }
    
    public String onFormSubmitFailed(
            Model model, FormConfig formConfig, Exception e,
            HttpServletRequest request, HttpServletResponse response) {
    
        LOG.warn("Failed to process: " + formConfig, e);

        controllerService.addErrorMessages(model, 
                "Unexpected error occured while processing action: " + 
                        formConfig.getAction() + ' ' + formConfig.getModelname());

        return Templates.ERROR;
    }

    public Optional<String> getTargetAfterSubmit(FormConfig formConfig) {
        return Optional.empty();
    }    
    
    public void upload(String modelname, Object modelobject, MultipartHttpServletRequest request) {
        
        final Map<String, List<UploadFileResponse>> multiFileUploadOutput = uploadMultipleFiles(
                modelname, modelobject, request.getMultiFileMap());
        
        final Map<String, MultipartFile> fileMap = request.getFileMap() == null ? 
                Collections.EMPTY_MAP : new HashMap<>(request.getFileMap());
        
        fileMap.keySet().removeAll(multiFileUploadOutput.keySet());

        if( ! fileMap.isEmpty()) {
            
            final Map<String, UploadFileResponse> singleFileUploadOutput = uploadSingleFiles(
                    modelname, modelobject, fileMap);
        }
    }
    
    public Map<String, UploadFileResponse> uploadSingleFiles(
            String modelname, Object modelobject, Map<String, MultipartFile> fileMap) {
        
        LOG.debug("Uploading single value multipart file(s): {}", (fileMap == null ? null : fileMap.keySet()));

        final Map<String, UploadFileResponse> output;
        
        if(fileMap == null || fileMap.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{    
            output = new HashMap<>(fileMap.size(), 1.0f);
            
            final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(modelobject);

            for(String name : fileMap.keySet()) {

                final MultipartFile multipartFile = fileMap.get(name);
                
                LOG.debug("{} has MultipartFile = {}", name, multipartFile);
                
                final UploadFileResponse fur = upload(multipartFile).orElse(null);
                
                if(fur == null) {
                    continue;
                }
                
                output.put(name, fur);
                
                final String propertyName = getPropertyName(
                        modelname, modelobject, fur, bean, name).orElse(null);
                
                if(propertyName == null) {
                    continue;
                }
                
                final Object propertyValue = getPropertyValue(
                        modelname, modelobject, fur, bean, propertyName).orElse(null);
                
                LOG.debug("After uploading multipart file, setting {} = {}", propertyName, propertyValue);
    
                if(bean.isWritableProperty(propertyName)) { 
                    bean.setPropertyValue(propertyName, propertyValue);
                }else{
                    LOG.warn("Not writable: " + propertyName + " = " + propertyValue + " on model: " + modelname);
                }
            }
        }
        
        return output == null || output.isEmpty() ? Collections.EMPTY_MAP : 
                Collections.unmodifiableMap(output);
    }  
    
    public Optional<String> getPropertyName(String modelname, Object modelobject, 
            UploadFileResponse fur, BeanWrapper bean, String propertyName) {
        return Optional.of(propertyName);
    }
    public Optional<Object> getPropertyValue(String modelname, Object modelobject, 
            UploadFileResponse fur, BeanWrapper bean, String propertyName) {
        final String relativePath = fur.getFileName();
        return Optional.of(relativePath);
    }
    
    public Map<String, List<UploadFileResponse>> uploadMultipleFiles(
            String modelname, Object modelobject, MultiValueMap<String, MultipartFile> mvm) {
        
        LOG.debug("Uploading multi-value multipart file(s): {}", (mvm == null ? null : mvm.keySet()));

        final Map<String, List<UploadFileResponse>> output;
        
        if(mvm == null || mvm.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{    
            output = new HashMap<>(mvm.size(), 1.0f);
            
            final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(modelobject);

            for(String name : mvm.keySet()) {

                final List<MultipartFile> multipartFiles = mvm.get(name);
    
                LOG.debug("{} has {} MultipartFile(s)", name, (multipartFiles == null ? null : multipartFiles.size()));
                
                if( multipartFiles == null || multipartFiles.isEmpty()) {
                    continue;
                }
                
                final List<UploadFileResponse> l = new ArrayList<>(multipartFiles.size()) ;
                
                for(MultipartFile multipartFile : multipartFiles) {
                
                    final UploadFileResponse fur = upload(multipartFile).orElse(null);

                    if(fur == null) {
                        continue;
                    }
                    
                    l.add(fur);
                }
                
                if( ! l.isEmpty()) {
                    output.put(name, l);
                }
                
                final String propertyName = getPropertyName(
                        modelname, modelobject, l, bean, name).orElse(null);
                
                if(propertyName == null) {
                    continue;
                }
                
                final Object propertyValue = getPropertyValue(
                        modelname, modelobject, l, bean, propertyName).orElse(null);
                
                LOG.debug("After uploading {} multipart files, setting {} = {}", 
                        l.size(), propertyName, propertyValue);
    
                if(bean.isWritableProperty(propertyName)) { 
                    bean.setPropertyValue(propertyName, propertyValue);
                }else{
                    LOG.warn("Not writable: " + propertyName + " = " + propertyValue + " on model: " + modelname);
                }
            }
        }
        
        return output == null || output.isEmpty() ? Collections.EMPTY_MAP : 
                Collections.unmodifiableMap(output);
    }    

    public Optional<String> getPropertyName(String modelname, Object modelobject, 
            List<UploadFileResponse> fur, BeanWrapper bean, String propertyName) {
        return Optional.of(propertyName);
    }
    public Optional<Object> getPropertyValue(String modelname, Object modelobject, 
            List<UploadFileResponse> furList, BeanWrapper bean, String propertyName) {

        final Class type = bean.getPropertyType(propertyName);

        final TypeDescriptor td = bean.getPropertyTypeDescriptor(propertyName);
        
        final TypeDescriptor etd = td == null ? null : td.getElementTypeDescriptor();
        
        final Class elementType = etd == null ? null : etd.getType();

        Object output = null;
        
        if(String.class.equals(elementType)) {
            
            if(Collection.class.isAssignableFrom(type)) {
                
                Collection c = (Collection)bean.getPropertyValue(propertyName);
                
                if(c == null) {
                    c = (Collection)new ReflectionUtil().newInstanceForCollectionType(type);
                }
                
                for(UploadFileResponse fur : furList) {
                
                    final String relativePath = fur.getFileName();
                    
                    c.add(relativePath);
                }
                
                output = c;
            }
        }

        return Optional.ofNullable(output);
    }

    public Optional<UploadFileResponse> upload(MultipartFile multipartFile) {
        
        final String fname = multipartFile.getOriginalFilename();

        final UploadFileResponse fur;
        
        if(fname == null || fname.isEmpty() || multipartFile.isEmpty()) {
            fur = null;
        }else{
            fur = fileStorageHandler.uploadFile(multipartFile);
        }    

        return Optional.ofNullable(fur);
    }

    public Object getOrCreateModelobject(ModelMap model, String action, String modelname,
            HttpServletRequest request, HttpServletResponse response) {
        
        final Map<String, Object> defaultValues = controllerService
                .getDefaultParametersProvider().apply(action, modelname);
        
        return controllerService.getOrCreateModelobject(model, modelname, defaultValues, request, response);
    }

    public void addAttributes(Object model, FormConfig formConfig,
            HttpServletRequest request, HttpServletResponse response) {
        
        LOG.debug("FormConfig: {}", formConfig);
        
        controllerService.checkAttributes(formConfig.getAction(), formConfig.getModelname(), formConfig.getModelobject());
        
        final Form form = this.controllerService.buildForm(formConfig);
        
        controllerService.addAttributes(model, formConfig, form, request, response);
    }
    
    public ControllerService getControllerService() {
        return controllerService;
    }

    public EntityRepositoryFactory getEntityRepositoryFactory() {
        return controllerService.getEntityRepositoryFactory();
    }

    public FileStorageHandler getFileStorageHandler() {
        return fileStorageHandler;
    }

    public final TypeFromNameResolver getTypeFromNameResolver() {
        return controllerService.getTypeFromNameResolver();
    }

    public final EntityFormProvider getEntityFormProvider() {
        return controllerService.getEntityFormProvider();
    }
}
