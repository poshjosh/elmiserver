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

package com.bc.amex.jpa;

import com.bc.app.spring.functions.GetExtension;
import com.bc.elmi.pu.entities.Mimetype;
import com.bc.jpa.dao.JpaObjectFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2019 11:18:17 AM
 */
public class GetMimetypesForFilenameImpl implements GetMimetypesForFilename{
    
    @Autowired private JpaObjectFactory jpa;

    @Override
    public List<Mimetype> apply(String filename) {
        
        final String ext = new GetExtension().apply(filename, filename);
        //@todo use Mimetype_.extension
        final List<Mimetype> types = jpa.getDaoForSelect(Mimetype.class)
                .where(Mimetype.class, "extension", ext)
                .getResultsAndClose();
        
        return types;
    }
}
