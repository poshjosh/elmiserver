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
import com.bc.amex.form.FormConfig;
import com.bc.amex.services.FileStorageService;
import com.bc.elmi.pu.entities.Document;
import com.bc.elmi.pu.entities.Message;
import com.bc.elmi.pu.entities.Testdocument;
import com.bc.elmi.pu.entities.Testsetting;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 7, 2019 10:54:36 PM
 */
@Controller
@SessionAttributes({ModelAttributes.MODELOBJECT, ModelAttributes.FORM}) //@see https://stackoverflow.com/questions/30616051/how-to-post-generic-objects-to-a-spring-controller
public class CrudControllerImpl extends CrudController{

    private static final Logger LOG = LoggerFactory.getLogger(CrudControllerImpl.class);
    
    public CrudControllerImpl(
            @Autowired ControllerService controllerService, 
            @Autowired FileStorageService fileStorageService) {
        super(controllerService, fileStorageService);
    }

    @GetMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/showForm")
    @Override
    public String showForm(ModelMap model, 
            @PathVariable(name=Params.ACTION, required=true) String action,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) {

        final String template = super.showForm(
                model, action, modelname, modelid, modelfields, request, response);
        
        if(Message.class.getSimpleName().equals(modelname)){
        
            final Object modelobject = this.getControllerService().getModelobject(
                    model, modelname, request, response).orElse(null);
            
            if(modelobject instanceof Message) {
            
                model.addAttribute("message", (Message)modelobject);

                return Templates.MESSAGE;
            }
        }
        
        return template;
    }

    @PostMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/validateForm")
    @Override
    public String validateForm(
            @Valid @ModelAttribute(ModelAttributes.MODELOBJECT) Object modelobject,
            BindingResult bindingResult,
            Model model,
            @PathVariable(name=Params.ACTION, required=true) String action,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            MultipartHttpServletRequest request, HttpServletResponse response) {

        return super.validateForm(modelobject, bindingResult, model, action, modelname, modelfields, request, response);
    }    

    @RequestMapping("/{"+Params.ACTION+"}/{"+Params.MODELNAME+"}/submitForm")
    @Override
    public String submitForm(
            Model model,
            @PathVariable(name=Params.ACTION, required=true) String action,
            @PathVariable(name=Params.MODELNAME, required=true) String modelname,
            @RequestParam(value=Params.MODELID, required=false) String modelid,
            @RequestParam(value=Params.MODELFIELDS, required=false) String [] modelfields,
            HttpServletRequest request, HttpServletResponse response) {

        return super.submitForm(model, action, modelname, modelid, modelfields, request, response);
    } 

    @Override
    public String onFormSubmitSuccessful(Model model, FormConfig formConfig, 
            HttpServletRequest request, HttpServletResponse response) {
        
        //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity
        final String modelname = formConfig.getModelname();
        
        if(CrudActionNames.DELETE.equals(formConfig.getAction()) && isDocumentType(modelname)) {

            try{
                deleteRelatedDocumentEntity(model, formConfig);
            }catch(RuntimeException e) {
                LOG.warn("", e);
            }
            try{
                deleteDocumentFile(model, formConfig);
            }catch(RuntimeException e) {
                LOG.warn("", e);
            }
        }
        
        return super.onFormSubmitSuccessful(model, formConfig, request, response);
    }

    public boolean deleteRelatedDocumentEntity(Model model, FormConfig formConfig) {
        
        final Class type = this.getTypeFromNameResolver().getType(formConfig.getModelname());
        
        final Object modelobject = formConfig.getModelobject();
        
        if(modelobject != null) {
            final Document doc;
            if(Testsetting.class.equals(type)) {
                doc = ((Testsetting)modelobject).getTestsetting();
            }else if(Testdocument.class.equals(type)) {
                doc = ((Testdocument)modelobject).getTestdocument();
            }else{
                doc = null;
            }
            
            if(doc != null) {
                
                this.getEntityRepositoryFactory().forEntity(Document.class).deleteById(doc.getDocumentid());
                
                LOG.debug("Deleted document: {} of {}", doc, modelobject);
                
                return true;
            }
            
            if(doc != null) {
                LOG.debug("Failed to delete {} of {}", doc, modelobject);
            }else{
                LOG.debug("Document is NULL for ", modelobject);
            }
        }
        
        return false;
    }
    
    public boolean deleteDocumentFile(Model model, FormConfig formConfig) {
        
        //@Todo periodic job to check uploads dir for orphan files and deleteManagedEntity
        final String modelname = formConfig.getModelname();

        if(CrudActionNames.DELETE.equals(formConfig.getAction()) && isDocumentType(modelname)) {

            String location = null;
            try{
                
                final String modelid = formConfig.getModelid();

                final Object entity = formConfig.getModelobject() != null ? formConfig.getModelobject() :
                        this.getControllerService()
                        .fetchModelobjectFromDatabase(formConfig.getModelname(), modelid);
                
                final Document document = getDocument(entity);
               
                if(document == null) {
                    LOG.warn("Failed to find recently persisted document, documentid: {}", modelid);
                    return false;
                }

                location = document.getLocation();
                Objects.requireNonNull(location, "Document.location == null for Document: " + document);

                final Resource re = this.getFileStorageHandler().loadFileAsResource(location);
                
                if(re != null && re.exists()) {
                    
                    final File file = re.getFile();

                    LOG.trace("Deleting: {}", file);

                    final Path path = file.toPath();

                    if( ! Files.deleteIfExists(path)) {

                        file.deleteOnExit();
                        
                        return false;
                        
                    }else{
                        
                        LOG.debug("Deleted: {}", file);
                    
                        return true;
                    }
                }
            }catch(IOException e) {
            
                LOG.debug("Failed to delete: " + location, e);
            }
        }
        
        return false;
    }
    
    public Document getDocument(Object doctype) {
        if(doctype instanceof Document) {
            return (Document)doctype;
        }else if(doctype instanceof Testdocument) {
            return ((Testdocument)doctype).getTestdocument();
        }else if(doctype instanceof Testsetting) {
            return ((Testsetting)doctype).getTestsetting();
        }else{
            throw new UnsupportedOperationException();
        }
    }
    
    public boolean isDocumentType(String modelname) {
        final Class type = this.getTypeFromNameResolver().getType(modelname);
        return isDocumentType(type);
    }

    public boolean isDocumentType(Class type) {
        return Document.class.getSimpleName().equals(type.getSimpleName()) ||
                Testdocument.class.getSimpleName().equals(type.getSimpleName()) ||
                Testsetting.class.getSimpleName().equals(type.getSimpleName());
    }
}
