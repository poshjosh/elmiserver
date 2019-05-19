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

package com.bc.amex;

import com.looseboxes.mswordbox.AppContext;
import com.looseboxes.mswordbox.FileNames;
import com.looseboxes.mswordbox.MsKioskSetup;
import com.looseboxes.mswordbox.ui.UiConfiguration;
import java.io.File;
import java.nio.file.Paths;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Chinomso Bassey Ikwuagwu on May 8, 2019 3:50:20 AM
 */
@Lazy
@Configuration
public class UiConfigurationImpl extends UiConfiguration{

    @Override
    public File getAdminUiConfigurerDir(ApplicationContext applicationContext) {
        //@todo From database not dir
//        final File dir = setup.getDir(FileNames.DIR_INBOX).toFile();
        final AppContext app = applicationContext.getBean(AppContext.class);
        if(app.isServer()) {
            final DirsProperties dirs = applicationContext.getBean(DirsProperties.class);
            return Paths.get(dirs.getUploads()).toFile();
        }else{
            final MsKioskSetup setup = applicationContext.getBean(MsKioskSetup.class);
            return setup.getDir(FileNames.DIR_INBOX).toFile();
        }
    }
}
