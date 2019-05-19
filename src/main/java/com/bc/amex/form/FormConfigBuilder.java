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

package com.bc.amex.form;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 21, 2019 5:11:48 PM
 */
public class FormConfigBuilder implements Serializable, FormConfig {

    private String action;
    private String modelname;
    private String modelid;
    private Object modelobject;
    
    private List<String> modelfields = Collections.EMPTY_LIST;
    
    private boolean buildAttempted;

    public FormConfigBuilder() { }
    
    public FormConfig build() {
        
        if(buildAttempted) {
            throw new IllegalStateException("build() method may only be called once");
        }
        
        buildAttempted = true;
        
        Objects.requireNonNull(action);
        Objects.requireNonNull(modelname);
        
        return this;
    }
    
    public FormConfigBuilder action(String arg) {
        this.action = arg;
        return this;
    }

    public FormConfigBuilder modelname(String arg) {
        this.modelname = arg;
        return this;
    }

    public FormConfigBuilder modelid(String arg) {
        this.modelid = arg;
        return this;
    }

    public FormConfigBuilder modelobject(Object arg) {
        this.modelobject = arg;
        return this;
    }

    public FormConfigBuilder modelfields(String arg) {
        return modelfields(Collections.singletonList(Objects.requireNonNull(arg)));
    }
    
    public FormConfigBuilder modelfields(String... arg) {
        if(arg == null) {
            return modelfields((List)null);
        }else if(arg.length == 0) {
            return modelfields(Collections.EMPTY_LIST);
        }else{
            return modelfields(Arrays.asList(arg));
        }
    }
    public FormConfigBuilder modelfields(List<String> arg) {
        this.modelfields = arg == null ? null : Collections.unmodifiableList(arg);
        return this;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getModelname() {
        return modelname;
    }

    @Override
    public String getModelid() {
        return modelid;
    }

    @Override
    public Object getModelobject() {
        return modelobject;
    }

    @Override
    public List<String> getModelfields() {
        return modelfields;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.action);
        hash = 41 * hash + Objects.hashCode(this.modelname);
        hash = 41 * hash + Objects.hashCode(this.modelid);
        hash = 41 * hash + Objects.hashCode(this.modelobject);
        hash = 41 * hash + Objects.hashCode(this.modelfields);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FormConfigBuilder other = (FormConfigBuilder) obj;
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.modelname, other.modelname)) {
            return false;
        }
        if (!Objects.equals(this.modelid, other.modelid)) {
            return false;
        }
        if (!Objects.equals(this.modelobject, other.modelobject)) {
            return false;
        }
        if (!Objects.equals(this.modelfields, other.modelfields)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FormConfigBuilder{" + "action=" + action + ", modelname=" + modelname + ", modelid=" + modelid + ", modelobject=" + modelobject + ", modelfields=" + modelfields + '}';
    }
}
