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

import com.bc.amex.jpa.GetMimetypesForFilename;
import com.bc.amex.jpa.repository.EntityRepository;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.app.spring.entities.UploadFileResponse;
import com.bc.elmi.pu.entities.Document;
import com.bc.elmi.pu.entities.Document_;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.Test_;
import com.bc.elmi.pu.entities.Testdocument;
import com.bc.elmi.pu.entities.Testsetting;
import com.bc.elmi.pu.entities.User;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.Select;
import com.looseboxes.mswordbox.AppContext;
import com.looseboxes.mswordbox.test.TestDocKey;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chinomso Bassey Ikwuagwu on May 13, 2019 1:41:02 PM
 */
@Service
public class DocumentService extends ControllerService{

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);
    
    @Autowired private AppContext app;
    @Autowired private GetMimetypesForFilename getMimetypesForFilename;
    @Autowired private EntityRepositoryFactory entityRepositoryFactory;
//    @Autowired private UserRepository userRepo;
    @Autowired private JpaObjectFactory jpa;
    
    public Document findByFilename(String filename) {
        
        final TestDocKey key = TestDocKey.decodeFilename(app, filename);

        final String subject = key.getSummary();

        final Document doc = entityRepositoryFactory.forEntity(Document.class).findSingleBy(Document_.subject, subject);

        LOG.debug("For filename: {}, found: {}", filename, doc);
        
        return doc;
    }
    
    public <T> T persistRef(String testid, Document doc, Class<T> ref) {
        
        final EntityRepository<Test> testService = entityRepositoryFactory.forEntity(Test.class);

        final Test test = testService.find(testid);
        
        return persistRef(test, doc, ref);
    }
    
    public <T> T persistRef(Test test, Document doc, Class<T> ref) {
        
        if(test.getTestid() == null) {
        
            final Select<Test> select = jpa.getDaoForSelect(Test.class);
            
            test = select.where(Test_.testname, test.getTestname())
                        .and().where(Test_.starttime, test.getStarttime())
                        .getSingleResultAndClose();
        }
        
        final T output;
        
        if(Testdocument.class.equals(ref)) {

            final Testdocument entity = new Testdocument();
            entity.setDocumenttest(test);
            entity.setTestdocument(doc);

            final EntityRepository<Testdocument> entityRepo = entityRepositoryFactory.forEntity(Testdocument.class);
            entityRepo.create(entity);
            
            LOG.debug("Persisted {} for {}", entity, doc);
            
            output = (T)entity;

        }else if(Testsetting.class.equals(ref)) {

            final Testsetting entity = new Testsetting();
            entity.setSettingtest(test);
            entity.setTestsetting(doc);

            final EntityRepository<Testsetting> entityRepo = entityRepositoryFactory.forEntity(Testsetting.class);
            entityRepo.create(entity);

            LOG.debug("Persisted {} for {}", entity, doc);

            output = (T)entity;

        }else {
            throw new UnsupportedOperationException("Expected: " + Testsetting.class + 
                    " or " + Testdocument.class + ", found: " + ref);
        }

        return output;
    }
    
    public void persistDocAndRef(UploadFileResponse ufr, HttpServletRequest request, Test test, Class type) {
    
        final Document doc = persist(ufr, request);

        LOG.debug("Persisting {} for: {}", type.getSimpleName(), doc);

        persistRef(test, doc, type);
    }

    public Document persist(UploadFileResponse ufr, HttpServletRequest request) {
        
        return DocumentService.this.persist(ufr.getOriginalFileName(), ufr.getFileName(), request);
    }
    
    public Document persist(String originalFilename, String savedTo, HttpServletRequest request) {
        final User user = this.validateLoggedin(request);
        final Document doc = new Document();
        doc.setAuthor(user);
        return DocumentService.this.persist(doc, originalFilename, savedTo, request);
    }
    
    public Document persist(Document doc, UploadFileResponse ufr, HttpServletRequest request) {
     
        return DocumentService.this.persist(doc, ufr.getOriginalFileName(), ufr.getFileName(), request);
    }
    
    public Document persist(Document doc, String originalFilename, 
            String savedTo, HttpServletRequest request) {
        
        final User user = this.validateLoggedin(request);

        final User docAuthor = doc.getAuthor();
        if(docAuthor == null) {
            throw new ValidationException("User uploading document could not be validated");
        }
        
        final boolean authorIsCurrentUser = 
                (user.getUsername().equals(docAuthor.getUsername()) ||
                user.getUserid().equals(docAuthor.getUserid()));
        if( ! authorIsCurrentUser) {
            throw new java.security.AccessControlException("You are not permitted to perform the requested operation");
        }
        
        doc.setAuthor(user);
        doc.setLocation(savedTo);

        getMimetypesForFilename.apply(originalFilename).stream().findFirst().ifPresent((mimetype) -> {
            doc.setMimetype(mimetype);
        });
        
        final EntityRepository<Document> docuService = entityRepositoryFactory.forEntity(Document.class);
       
        docuService.create(doc);

        final Document persisted = docuService.findSingleBy(Document_.location, doc.getLocation(), null);
        
        if(persisted == null) {
            LOG.warn("Could not find in database, recently created Document with Location: {}", doc.getLocation());
        }
        
        LOG.debug("Persisted document: {}, for: {}", doc, savedTo);
        
        return persisted == null ? doc : persisted;
    }
}
