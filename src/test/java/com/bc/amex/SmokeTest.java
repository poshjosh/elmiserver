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

import com.bc.amex.controllers.CrudControllerImpl;
import com.bc.amex.controllers.DocumentController;
import com.bc.amex.controllers.FileController;
import com.bc.amex.controllers.LoginController;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 17, 2019 9:28:59 AM
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SmokeTest {

    @Autowired private CrudControllerImpl crudController;
    @Autowired private DocumentController docController;
    @Autowired private FileController fileController;
    @Autowired private LoginController loginController;

    @Test
    public void contexLoads() throws Exception {
        assertThat(crudController).isNotNull();
        assertThat(docController).isNotNull();
        assertThat(fileController).isNotNull();
        assertThat(loginController).isNotNull();
    }
}