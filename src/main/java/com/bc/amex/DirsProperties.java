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

import java.io.Serializable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @see https://www.baeldung.com/configuration-properties-in-spring-boot
 * @author Chinomso Bassey Ikwuagwu on Mar 21, 2019 1:01:53 PM
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "dirs")
public class DirsProperties implements Serializable{

    private String working;
    private String uploads;
    private String networkDevicedetails;

    public String getWorking() {
        return working;
    }

    public void setWorking(String working) {
        this.working = working;
    }

    public String getUploads() {
        return uploads;
    }

    public void setUploads(String uploads) {
        this.uploads = uploads;
    }

    public String getNetworkDevicedetails() {
        return networkDevicedetails;
    }

    public void setNetworkDevicedetails(String networkDevicedetails) {
        this.networkDevicedetails = networkDevicedetails;
    }
    
}
