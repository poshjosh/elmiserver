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

import com.bc.amex.exceptions.LoginException;
import com.bc.amex.services.ControllerService;
import com.bc.amex.services.DocumentService;
import com.bc.amex.services.FileStorageService;
import com.bc.app.spring.entities.UploadFileResponse;
import com.bc.config.Config;
import com.bc.elmi.pu.entities.Document;
import com.looseboxes.mswordbox.AppContext;
import com.looseboxes.mswordbox.FileNames;
import com.looseboxes.mswordbox.MsKioskSetup;
import com.looseboxes.mswordbox.config.ConfigService;
import com.looseboxes.mswordbox.functions.UrlBuilder;
import com.looseboxes.mswordbox.functions.admin.OpenFilesNative;
import com.looseboxes.mswordbox.functions.admin.ShowMarkingUi;
import com.looseboxes.mswordbox.functions.admin.SubmitScoreCards;
import com.looseboxes.mswordbox.net.RequestClient;
import com.looseboxes.mswordbox.net.RequestClientProvider;
import com.looseboxes.mswordbox.net.Response;
import com.looseboxes.mswordbox.net.SendTestDocs;
import com.looseboxes.mswordbox.net.TestDocSenderFactory;
import com.looseboxes.mswordbox.security.LoginManager;
import com.looseboxes.mswordbox.test.Tests;
import com.looseboxes.mswordbox.ui.AppUiContext;
import com.looseboxes.mswordbox.ui.selection.SelectionAction;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 27, 2019 7:42:52 PM
 */
@Controller
public class FileController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    @Autowired private AppContext app;
    @Autowired private ControllerService controllerService;
    @Autowired private FileStorageService delegate;
    @Autowired private ResponseBuilder responseBuilder;
    @Autowired private DocumentService documentService;
    @Autowired private MsKioskSetup setup; 
    @Autowired private RequestClientProvider rcp;

    @Autowired private AppUiContext ui;
    @Autowired private Display display;
    @Autowired private Tests tests;
    @Autowired private TestDocSenderFactory testDocSenderFactory;
    @Autowired private LoginManager loginManager;
    
    public FileController() { }
    
    @GetMapping("/open/File")
    @ResponseBody public ResponseEntity<Resource> openFile(
            @RequestParam(value="filename", required=true) String filename, 
            HttpServletRequest request) {
        
        Document doc = null;
        try{
            
            doc = this.documentService.findByFilename(filename);
            
            final File file = delegate.resolve(doc.getLocation()).toFile();

            LOG.debug("{}", file);
            
            final boolean weAreOnServer = file.exists();
            
//            if( ! weAreOnServer) {
            
                if( ! moveToInbox(filename, file)) {
                
                    throw new IOException("Download failed for: " + doc.getLocation());
                }
//            }
            
            LOG.debug("Opening file: {}", file);

            if(new OpenFilesNative().apply(file)) {

//                return this.responseBuilder.buildSuccessResponse();
                return ResponseEntity.ok().build();

            }else{

//                return this.responseBuilder.buildErrorResponse("Failed to open: " + filename);
                return delegate.downloadFile(doc.getLocation(), request);
            }
        }catch(Exception e) {
            
            LOG.warn("Unexpected exception opening file: " + filename, e);

            if(doc != null) {
                
                return delegate.downloadFile(doc.getLocation(), request);
                
            }else{
                
//                return this.responseBuilder.buildErrorResponse("Failed to open file: " + filename);
                return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    @GetMapping("/mark/File")
    public String markFile(
            ModelMap model,
            @RequestParam(value="filename", required=true) String filename) {
        
        LOG.trace("#markFile({})", filename);
        
        try{
            
            final Document doc = this.documentService.findByFilename(filename);
            
            final File file = delegate.resolve(doc.getLocation()).toFile();
            
            LOG.debug("{}", file);
            
            final boolean weAreOnServer = file.exists();
            
//            if( ! weAreOnServer) {
            
                if( ! moveToInbox(filename, file)) {
                
                    throw new IOException("Download failed for: " + doc.getLocation());
                }
//            }

            if(!loginManager.isLoggedIn()) {
            
                final Component parentComponent = ui.getMainWindowOptional().orElse(null);
                final Icon icon = ui.getImageIconOptional().orElse(null);
                JOptionPane.showMessageDialog(parentComponent, 
                        "You must be logged in to the Desktop App to preform the requested operation.\nLogin and try again.", 
                        "Login Required", JOptionPane.WARNING_MESSAGE, icon);
                
                this.controllerService.addErrorMessage(model, "You must be logged in to the Desktop App to preform the requested operation. Login and try again.");
    
                return Templates.ERROR;
            }
            
            LOG.debug("Opening Marking UI for file: {}", file);
            final SendTestDocs sendTestDocs = testDocSenderFactory.get(TestDocSenderFactory.SEND_TO_SERVER);
            final SelectionAction<File> submitAction = new SubmitScoreCards(app, tests, sendTestDocs);
            display.asyncExec(() -> {
                try{
                    new ShowMarkingUi(app, display, submitAction).apply(file);
                }catch(RuntimeException e) {
                    LOG.warn("", e);
                }
            });

            this.controllerService.addInfoMessage(model, 
                    "... Please wait. It may take up to 10 seconds before the Marking User Interface is loaded.");
            
            return Templates.SUCCESS;

        }catch(Exception e) {
            
            LOG.warn("Unexpected exception opening file: " + filename, e);
            
            final String m = this.controllerService.logAndBuildErrorResponse(e).getMessage();
            
            this.controllerService.addErrorMessage(model, m);
            
            return Templates.ERROR;
        }
    }
    
    private boolean moveToInbox(String filename, File file) throws IOException, ParseException{
        
        LOG.debug("Downloading to inbox: {}", filename);
        
        final AtomicBoolean success = new AtomicBoolean(false);
    
        final Document doc = this.documentService.findByFilename(filename);

        final Path target = setup.getDir(FileNames.DIR_INBOX).resolve(filename);
        
        LOG.debug("Will save to: {}, filename: {}", target, filename);
        
        if(file.exists()) {
        
            LOG.trace("Saving read bytes to: {}", target);

            final byte [] bytes = Files.readAllBytes(file.toPath());
            
            Files.write(target, bytes, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            LOG.debug("Saved downloaded bytes to: {}", target);
            
            success.compareAndSet(false, true);
            
        }else{
        
            final Config config = app.getConfig(ConfigService.APP_PROTECTED);

            final URL url = new UrlBuilder().build(config, FileStorageService.DOWNLOAD_PATH_CONTEXT, doc.getLocation()).get(0);

            LOG.debug("Built download URL: {}, for Document.location: {}", url, doc.getLocation());

            final RequestClient<okhttp3.Response> rc = rcp.get((res) -> (okhttp3.Response)res);

            rc.execute("GET", url, (r) -> {

                LOG.trace("Saving downloaded bytes to: {}", target);

                try(okhttp3.ResponseBody body = r.body()) {

                    Files.write(target, body.bytes(), StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE);

                    LOG.debug("Saved downloaded bytes to: {}", target);

                    success.compareAndSet(false, true);

                }catch(IOException e) {

                    LOG.warn("", e);
                }  
            });
        }

        return success.get();
    }

    @PostMapping("/uploadFile")
    @ResponseBody public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        return delegate.uploadFile(file);
    }

    @PostMapping("/uploadFiles")
    @ResponseBody public List<UploadFileResponse> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        return delegate.uploadFiles(files);
    }

    @GetMapping(FileStorageService.DOWNLOAD_PATH_CONTEXT + "/{fileName:.+}")
    @ResponseBody public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName, HttpServletRequest request) {
        
        return delegate.downloadFile(fileName, request);
    }

    @GetMapping("/downloadDocument/{id}")
    @ResponseBody public ResponseEntity<Resource> downloadDocument(
            @PathVariable String id, HttpServletRequest request) {
        
        final Document doc = (Document)this.controllerService
                .fetchModelobjectFromDatabase(Document.class.getSimpleName(), id);

        return delegate.downloadFile(doc.getLocation(), request);
    }

    @RequestMapping(FileStorageService.DOWNLOAD_PATH_CONTEXT + "/1")
    @ResponseBody public Response download(
            @RequestParam(value=Params.FILE_NAMES, required=true) String [] filenames,
            @RequestParam(value=Params.USERNAME, required=false) String username,
            @RequestParam(value=Params.PASSWORD, required=false) String password,
            HttpServletRequest request, HttpServletResponse response) {
        
        try{
            
            this.controllerService.loginIfNeed(username, password, request)
                    .orElseThrow(() -> new LoginException("Unexpected error"));

            final Map<String, String> payload = new HashMap<>(filenames.length, 1.0f);

            final String encoding = getCharacterEncodingOrDefault(response);
            
            final List<String> errors = new ArrayList<>();
            
            for(String filename : filenames) {

                final Path path = delegate.resolve(filename);

                try{
                    
                    final byte [] bytes = Files.readAllBytes(path);

                    payload.put(filename, new String(bytes, encoding));
                    
                }catch(IOException e) {
                    
                    LOG.warn("", e);
                
                    errors.add(e.getLocalizedMessage());
                }
            }  

            return this.responseBuilder.buildResponse(payload, 
                    errors.isEmpty() ? "success" : errors, 
                    errors.size() >= payload.size());
            
        }catch(RuntimeException e) {
            
            return this.controllerService.logAndBuildErrorResponse(e);
        }
    }

    public String getCharacterEncodingOrDefault(HttpServletResponse response) {
        return response.getCharacterEncoding() == null ? StandardCharsets.UTF_8.name() :
                response.getCharacterEncoding();
    }
}