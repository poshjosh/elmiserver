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
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import com.looseboxes.mswordbox.security.Login;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on May 1, 2019 2:09:23 PM
 */
public class LoginToLocalServer implements Login{

    private static final Logger LOG = LoggerFactory.getLogger(LoginToLocalServer.class);

    private final LoginService loginService;
    
    private final UserRepository userRepo;

    public LoginToLocalServer(LoginService loginService, UserRepository userRepo) {
        this.loginService = Objects.requireNonNull(loginService);
        this.userRepo = Objects.requireNonNull(userRepo);
    }

    @Override
    public User login(String usr, String pwd) throws LoginException {

        LOG.debug("Logging in user: {}", usr);

        final boolean loggedIn = loginService.login(usr, pwd);

        LOG.debug("Logged in: {}", loggedIn);

        if(loggedIn) {
            final User user = userRepo.findSingleBy(User_.username, usr);
            return user;
        }else{
            return null;
        }
    }
}
