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

import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.web.form.Form;
import com.bc.web.form.FormField;
import com.bc.web.form.StandardFormFieldTypes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 10, 2019 12:47:02 PM
 */
public class FormFieldsCreatorForUser extends FormFieldsCreatorImpl {

    public FormFieldsCreatorForUser(EntityRepositoryFactory entityRepositoryFactory,
            SelectionContext selectionContext, FormPropertyAccess formPropertyAccess) {
        super(entityRepositoryFactory, selectionContext, formPropertyAccess);
    }

    protected FormFieldsCreatorForUser(EntityRepositoryFactory entityRepositoryFactory,
            SelectionContext selectionContext, FormPropertyAccess formPropertyAccess, 
            Predicate<Field> isFormField, int maxDepth) {
        super(entityRepositoryFactory, selectionContext, formPropertyAccess, isFormField, maxDepth);
    }

    @Override
    public List<FormField> apply(Form form, Object object) {
        final List<FormField> fields = super.apply(form, object);
        return this.placeConfirmPasswordAfterPassword(fields);
    }

    public List<FormField> placeConfirmPasswordAfterPassword(final List<FormField> fields) {
        int passPos = -1;
        int cfmPassPos = -1;
        for(int i=0; i<fields.size(); i++) {
            final FormField ff = fields.get(i);
            if("password".equals(ff.getName()) || StandardFormFieldTypes.PASSWORD.equals(ff.getType())) {
                passPos = i;
            }else if("confirmPassword".equals(ff.getName())) {
                cfmPassPos = i;
            }
        }
        final List<FormField> result = new ArrayList(fields);
        if(passPos != -1 && cfmPassPos != -1) {
            final FormField cfmPassField = result.remove(cfmPassPos);
            result.add(passPos + 1, cfmPassField);
        }
        return Collections.unmodifiableList(result);
    }
}
