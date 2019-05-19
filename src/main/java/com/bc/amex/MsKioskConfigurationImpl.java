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

import com.bc.amex.jpa.repository.UserRepository;
import com.bc.amex.services.LoginService;
import com.bc.socket.io.messaging.SaveFileMessages;
import com.looseboxes.mswordbox.FileNames;
import com.looseboxes.mswordbox.MsKioskConfiguration;
import com.looseboxes.mswordbox.MsKioskSetup;
import com.looseboxes.mswordbox.config.ConfigFactory;
import com.looseboxes.mswordbox.functions.SaveFileMessagesNotifyUser;
import com.looseboxes.mswordbox.security.Login;
import java.io.File;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 27, 2019 5:13:45 PM
 */
@Lazy
@Configuration
public class MsKioskConfigurationImpl extends MsKioskConfiguration{

    private static final Logger LOG = LoggerFactory.getLogger(MsKioskConfigurationImpl.class);
    
    @Lazy @Autowired private LoginService loginService;
    
    @Lazy @Autowired private UserRepository userRepo;
    
    @Autowired private MsKioskSetup setup;
    
    @Override
    @Bean public SaveFileMessages saveFileMessages( ConfigFactory configFactory) {
        
//        final File file = Paths.get(dirs.getUploads()).toAbsolutePath().normalize().toFile();
        
        final Charset charset = getCharset(configFactory);
        
        final File file = setup.getDir(FileNames.DIR_INBOX).toFile();
        
        LOG.info("Dir to save incoming files sent via sockets: {}", file);
        
        return new SaveFileMessagesNotifyUser(file, 8192, charset);
    }
    
    @Override
    @Bean @Scope("prototype") public Login login() {
        final Login login = new LoginToLocalServer(loginService, userRepo);
        return login;
    }
}
