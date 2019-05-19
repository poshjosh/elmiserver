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

import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.amex.services.FileStorageService;
import com.bc.app.spring.entities.UploadFileResponse;
import com.bc.elmi.pu.entities.Document;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.bc.amex.services.DocumentService;
import com.bc.elmi.pu.entities.Testdocument;
import com.looseboxes.mswordbox.AppContext;
import com.looseboxes.mswordbox.functions.admin.ScoreFile;
import com.looseboxes.mswordbox.net.Response;
import com.looseboxes.mswordbox.test.Tests;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ValidationException;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2019 10:59:12 AM
 */
@RestController
public class DocumentController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    @Autowired private FileStorageService delegate;
    @Autowired private DocumentService documentService;
    @Autowired private ResponseBuilder responseBuilder;
    @Autowired private PartParser partParser;
    @Autowired private AppContext app;
    @Autowired private EntityRepositoryFactory erf;

    public DocumentController() { }
    
    @PostMapping("/uploadDocs")
    public Response uploadDocs(
            @RequestParam(value="testid", required=false) String testid,
            @RequestParam(value="Documents", required=true) List<MultipartFile> docParts,
            @RequestParam(value="files", required=true) List<MultipartFile> fileParts,
            HttpServletRequest request, HttpServletResponse response) {
        
        LOG.debug("Uploading {} documents, {} files", 
                (docParts == null ? null : docParts.size()), 
                (fileParts == null ? null : fileParts.size()));
        
        int errors = 0;

        int len = 0;

        List<Document> createdDocs;
        List<UploadFileResponse> output;
        try{
            
            len = docParts.size();
            
            createdDocs = new ArrayList(len);
            output = new ArrayList(len);

            if(docParts.size() != fileParts.size()) {
                throw new ValidationException("Invalid request");
            }
            
            int parentIndex = -1;
            Document parentDoc = null;
            UploadFileResponse parentRes = null;
            for(int i=0; i<len; i++) {
                
                final MultipartFile docPart = docParts.get(i);
                final MultipartFile filePart = fileParts.get(i);

                if(Tests.MAIN_DOCUS_DEFAULT_NAME.equals(docPart.getOriginalFilename())) {

                    try{
                        final Map.Entry<Document, UploadFileResponse> entry = 
                                upload(testid, null, docPart, filePart, request, response);

                        parentDoc = entry.getKey();
                        parentRes = entry.getValue();

                        parentIndex = i;
                        
                    }catch(RuntimeException e) {

                        ++errors;

                        LOG.warn("Unexpected Error", e);

                        parentRes = getUnexpectedExceptionResponse(filePart);
                    }    
                }
            }

            for(int i=0; i<len; i++) {
                
                if(i == parentIndex) {
                    
                    createdDocs.add(parentDoc);
                    output.add(parentRes);
                    
                    continue;
                }
                
                final MultipartFile docPart = docParts.get(i);
                final MultipartFile filePart = fileParts.get(i);
                try{

                    final Map.Entry<Document, UploadFileResponse> entry = 
                            upload(testid, parentDoc, docPart, filePart, request, response);

                    createdDocs.add(entry.getKey());
                    output.add(entry.getValue());

                }catch(RuntimeException e) {

                    ++errors;
                    
                    LOG.warn("Unexpected Error", e);

                    output.add(getUnexpectedExceptionResponse(filePart));
                }    
            }
        }catch(RuntimeException e) {
            
            len = 0;
            output = Collections.EMPTY_LIST;
            
            LOG.warn("Unexpected Error", e);
        }
        
        return responseBuilder.buildResponse(output, "Success rate: " + errors + "/" + len, errors >= len);
    }
    
    @PostMapping("/uploadDoc")
    public Response uploadDoc(
            @RequestParam(value="testid", required=false) String testid,
            @RequestParam(value="Document", required=true) MultipartFile docPart,
            @RequestParam(value="file", required=true) MultipartFile filePart,
            HttpServletRequest request, HttpServletResponse response) {
        
        try{
            
            final Map.Entry<Document, UploadFileResponse> output = upload(
                    testid, null, docPart, filePart, request, response);

            final UploadFileResponse ufr = output.getValue();
            
            return responseBuilder.buildResponse(ufr, "success", false);
            
        }catch(RuntimeException e) {
            
            LOG.warn("Unexpected Error", e);
        
            final UploadFileResponse ufr = getUnexpectedExceptionResponse(filePart);
            
            return responseBuilder.buildResponse(ufr, "Unexpected Error", true);
        }
    }

    public Map.Entry<Document, UploadFileResponse> upload(
            String testid, Document parent, MultipartFile docPart, MultipartFile filePart,
            HttpServletRequest request, HttpServletResponse response) {
        
        LOG.trace("Uploading, testid: {}, parent document: {}, documentPart: {}, filePart: {}", 
                testid, parent, docPart, filePart);
        
        final UploadFileResponse ufr = delegate.uploadFile(filePart);

        try{
            
            final Document document = createDocument(testid, 
                    parent, docPart, filePart, ufr, request);

            final Map.Entry<Document, UploadFileResponse> output = new HashMap.SimpleImmutableEntry<>(document, ufr);
            
            LOG.debug("Upload successful. Output: {}", output);
            
            return output;
            
        }catch(RuntimeException e) {
            
            final Path path = delegate.resolve(ufr.getFileName());
            
            if(Files.exists(path)) {
                try{
                    Files.delete(path);
                    LOG.debug("Deleted failed upload: {}", path);
                }catch(IOException ioe) {
                    LOG.warn("{}. Will be deleted on exit, failed upload: {}", ioe.toString(), path);
                    path.toFile().deleteOnExit();
                }
            }
            
            throw e;
        }
    }

    public UploadFileResponse getUnexpectedExceptionResponse(MultipartFile file) {
        return new UploadFileResponse(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected Error", 
                file, "", "");
    }

//    @GetMapping(FileStorageService.DOWNLOAD_PATH_CONTEXT + "/{fileName:.+}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
//        return delegate.downloadFile(fileName, request);
//    }

    public Document createDocument(String testid, 
            Document parent, MultipartFile docPart, MultipartFile file, 
            UploadFileResponse ufr, HttpServletRequest request) {
        
        LOG.debug("Creating document: {}, for: {}", docPart, file);
        
        final Document doc = partParser.parse(docPart, request, Document.class);
        
        if(parent != null) {
            doc.setParentdocument(parent);
        }
        
        final Document output = documentService.persist(doc, ufr, request);
        
        final ScoreFile sf = new ScoreFile(app);
        
        if(sf.isScoreFilename(file.getOriginalFilename())) {
            
            final String testFilename = sf.toTestFile(file.getOriginalFilename());
            
            LOG.debug("Updating {} instance having filename: {} with Document result: {} having filename: {}",
                    Testdocument.class.getSimpleName(), testFilename, output, file.getOriginalFilename());
            
            final Document testDoc = this.documentService.findByFilename(testFilename);
            
            LOG.debug("For test filename: {}, found: {}", testFilename, testDoc);
            
            testDoc.getTestdocument().setDocumentresult(output);
            
            erf.forEntity(Document.class).update(testDoc);
            
        }else{
            
            LOG.debug("Persisting {} reference for {}", Testdocument.class.getSimpleName(), output);
            
            documentService.persistRef(testid, output, Testdocument.class);
        }
        
        return output;
    }
}
