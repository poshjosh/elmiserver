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

import com.bc.amex.controllers.TestController;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.elmi.pu.entities.Test;
import com.bc.elmi.pu.entities.Testsetting;
import com.bc.web.form.DefaultFormField;
import com.bc.web.form.Form;
import com.bc.web.form.FormField;
import com.bc.web.form.FormFieldBuilder;
import com.bc.web.form.StandardFormFieldTypes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on May 13, 2019 10:47:11 AM
 */
public class FormFieldsCreatorForTest extends FormFieldsCreatorImpl {

    public FormFieldsCreatorForTest(
            EntityRepositoryFactory entityRepositoryFactory, 
            SelectionContext selectionContext, 
            FormPropertyAccess formPropertyAccess) {
        super(entityRepositoryFactory, selectionContext, formPropertyAccess);
    }

    public FormFieldsCreatorForTest(
            EntityRepositoryFactory entityRepositoryFactory, SelectionContext selectionContext, 
            FormPropertyAccess formPropertyAccess, Predicate<Field> isFormField, int maxDepth) {
        super(entityRepositoryFactory, selectionContext, formPropertyAccess, isFormField, maxDepth);
    }

    @Override
    public List<FormField> apply(Form form, Object object) {
        final List<FormField> output = new ArrayList<>(super.apply(form, object));
        final String field = TestController.TEST_SETTINGS;
        final String label = "Test Settings ";
        final Test t = object == null ? null : (Test)object;
        final List<Testsetting> list = t == null ? Collections.EMPTY_LIST : t.getTestsettingList();
        for(int i=0; i<5; i++) {
            final Testsetting value = list == null || i >= list.size() ? null : list.get(i);
            output.add(createTestSettingFormField(form, field, label + (i + 1), value));
        }
        return Collections.unmodifiableList(output);
    }
    
    public FormField createTestSettingFormField(Form form, String name, String label, Testsetting value) {
    
        final FormFieldBuilder builder = new FormField.Builder()
                .apply(new DefaultFormField(form, name))
                .label(label)
                .value(value == null ? null : value.getTestsetting() == null ? null : value.getTestsetting().getLocation())
                .optional(true)
                .multiChoice(false)
                .multiValue(false)
                .referencedForm(null)
                .type(StandardFormFieldTypes.FILE);
                    
        return builder.build();
    }
}
