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

package com.bc.amex.jpa.repository;

import com.bc.elmi.pu.entities.Document;
import com.bc.elmi.pu.entities.Document_;
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import com.bc.jpa.dao.JpaObjectFactory;
import com.looseboxes.mswordbox.AppContext;
import com.looseboxes.mswordbox.test.TestDocKey;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chinomso Bassey Ikwuagwu on May 17, 2019 12:52:32 AM
 */
public class DocumentRepository extends EntityRepositoryImpl<Document> {

//    private static final Logger LOG = LoggerFactory.getLogger(DocumentRepository.class);

    private final AppContext app;
    private final UserRepository userRepo;
    
    public DocumentRepository(
            @Autowired AppContext app,
            @Autowired JpaObjectFactory jpa,
            @Autowired UserRepository userRepo) {
        super(jpa, Document.class);
        this.app = Objects.requireNonNull(app);
        this.userRepo = Objects.requireNonNull(userRepo);
    }

    public List<Document> findByFilename(String filename) {
    
        final TestDocKey key = TestDocKey.decodeFilename(app, filename);

        final User user = userRepo.findSingleBy(User_.username, key.getUsername());
        
        final List<Document> all = this.getJpaObjectFactory().getDaoForSelect(Document.class)
                .where(Document_.documentname, key.getDocumentname())
                .and().where(Document_.author, user).getResultsAndClose();
        
        final String testidStr = key.getTestid().toString();
        final Predicate<Document> filter = (doc) -> {
            return (doc.getTestdocument() != null &&
                    doc.getTestdocument().getDocumenttest().getTestid().toString()
                            .equals(testidStr) ||
                    doc.getTestsetting() != null &&
                    doc.getTestsetting().getSettingtest().getTestid().toString()
                            .equals(testidStr));
        };
        
        final List<Document> output = all.stream().filter(filter).collect(Collectors.toList());
        
        return output;
    }
}
