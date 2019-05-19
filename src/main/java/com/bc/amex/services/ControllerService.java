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

package com.bc.amex.services;

import com.bc.amex.controllers.ModelAttributes;
import com.bc.amex.controllers.Params;
import com.bc.amex.controllers.ResponseBuilder;
import com.bc.amex.exceptions.LoginException;
import com.bc.amex.form.DefaultParametersProvider;
import com.bc.amex.form.EntityFormProvider;
import com.bc.amex.form.FormConfig;
import com.bc.amex.form.validators.FormValidators;
import com.bc.amex.jpa.TypeFromNameResolver;
import com.bc.amex.jpa.repository.EntityRepository;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.amex.jpa.repository.UserRepository;
import com.bc.amex.jpa.selectors.Selector;
import com.bc.amex.jpa.selectors.SelectorFactory;
import com.bc.amex.searchresults.SearchresultBuilder;
import com.bc.amex.searchresults.SearchresultPage;
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import com.bc.jpa.dao.JpaObjectFactory;
import com.bc.jpa.dao.Select;
import com.bc.jpa.dao.functions.GetColumnNamesOfType;
import com.bc.jpa.dao.search.BaseSearchResults;
import com.bc.jpa.dao.search.SearchResults;
import com.bc.jpa.dao.search.SingleSearchResult;
import com.bc.web.form.Form;
import com.looseboxes.mswordbox.functions.GetUserMessage;
import com.looseboxes.mswordbox.net.Response;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 20, 2019 1:51:35 PM
 */
@Service
public class ControllerService {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerService.class);
    
    @Autowired private TypeFromNameResolver typeFromNameResolver;
    @Autowired private EntityRepositoryFactory entityRepositoryFactory;
    @Autowired private JpaObjectFactory jpaObjectFactory;
    @Autowired private EntityFormProvider entityFormProvider;
    @Autowired private DefaultParametersProvider defaultParametersProvider;
    @Autowired private FormValidators formValidators;
    @Autowired private SearchresultBuilder searchresultBuilder;
    @Autowired private SearchresultPage.Builder searchresultPageBuilder;
    @Autowired private ResponseBuilder responseBuilder;
    @Autowired private SelectorFactory selectorFactory;
    @Autowired private LoginService loginService;
    
    public User validateLoggedin(HttpServletRequest request) {
        return getLoggedinUser(request).orElseThrow(() -> new ValidationException(
                "You must be logged in before peforming the requested operation."));
    }
    
    public Optional<User> getLoggedinUser(HttpServletRequest request) {
        final Principal principal = request.getUserPrincipal();
        final UserRepository userRepo = (UserRepository)entityRepositoryFactory.forEntity(User.class);
        final User user = principal == null ? null : userRepo.getUser(principal);
        return Optional.ofNullable(user);
    }

    public Optional<User> loginIfNeed(
            String username, String password, HttpServletRequest request) 
            throws LoginException {
        
        User user = this.getLoggedinUser(request).orElse(null);

        if(user == null) {

            if((username == null || username.isEmpty()) || 
                    (password == null || password.isEmpty())) {

                throw new LoginException("Invalid login credentials");

            }else{

                if(loginService.login(username, password)) {

                    LOG.debug("Login OK for: {}", username);

                    final UserRepository userRepo = (UserRepository)entityRepositoryFactory.forEntity(User.class);

                    user = userRepo.findSingleBy(User_.username.getName(), username, null);

                    LOG.debug("For username: {}, found: {}", username, user);

                }else{

                    throw new LoginException("Login failed");
                }
            }
        }
        
        return Optional.ofNullable(user);
    }
    
    public Response logAndBuildErrorResponse(Exception e) {
        
        String m = "Unexpected Exception";

        LOG.warn(m, e);

        m = new GetUserMessage().apply(e, m);

        final Response r = this.responseBuilder.buildErrorResponse(m);
        
        if(e instanceof AuthenticationException) {
            r.setMessage("Authentication Failed");
            r.setCode(HttpServletResponse.SC_UNAUTHORIZED);
        }
        
        return r;
    }
    
    public SearchresultPage buildSearchresultPage(SearchResults searchResults, int pageNumber) {
        
        final SearchresultPage output = searchresultPageBuilder
                .searchResults(searchResults)
                .page(pageNumber)
                .resultBuilder(searchresultBuilder).build();
        
        return output;
    }
    
    public SearchResults search(String modelname, 
            String modelid, String query, String selectorName) {
    
        return search(modelname, modelid, query, selectorName, (select) -> {});
    }
    
    public SearchResults search(String modelname, 
            String modelid, String query, String selectorName, Consumer<Select<?>> format) {
    
        LOG.debug("Modelname: {}, modelid: {}, query: {}, selectorname: {}, formatter: {}",
                modelname, modelid, query, selectorName, format);
        
        final Object modelobject = modelid == null ? null : 
                this.fetchModelobjectFromDatabase(modelname, modelid);
        
        final SearchResults output;
        if(modelobject != null) {
            output = new SingleSearchResult(modelobject);
        }else{
            
            final Class<?> modeltype = typeFromNameResolver.getType(modelname);
            
            LOG.debug("Model type to search: {}", modeltype);
            
            final Select<?> select = jpaObjectFactory.getDaoForSelect(modeltype);
            
            select.from(modeltype);

            select.distinct(true);
            
            if(query != null && !query.isEmpty()) {
                final Collection<String> columns = getColumnsSelector().apply(modeltype, String.class);
                select.search(query, columns);
            }
            
            final Selector selector = selectorName == null ? Selector.NO_OP : selectorFactory.combo(selectorName, modeltype);
            
            if(selector.isApplicable(modeltype)) {
                selector.accept(select);
            }
            
            format.accept(select);

            output = new BaseSearchResults(select);
        }        

        return output;
    }
    
    public BiFunction<Class, Class, Collection<String>> getColumnsSelector() {
        return new GetColumnNamesOfType(jpaObjectFactory.getEntityManagerFactory());
    }
    
    public List<Validator> getValidators(FormConfig formConfig) {
        return formValidators.get(formConfig);
    }
    
    public Form buildForm(FormConfig formConfig) {
        final com.bc.web.form.Form form = entityFormProvider.apply(formConfig);
        LOG.debug("Form fields:\n{}", 
                form.getFormFields().stream()
                        .map((formField) -> formField.getName() + '=' + formField.getValue())
                        .collect(Collectors.joining("\n")));
        return form;
    }
    
    public DefaultParametersProvider getDefaultParametersProvider() {
        return defaultParametersProvider;
    }

    public void collectErrorsIntoModel(BindingResult bindingResult, Model model) {
    
        if (bindingResult.hasErrors()) {
            
            final List<ObjectError> errors = bindingResult.getAllErrors();
            if(errors != null) {
                for(ObjectError err : errors) {
                    LOG.warn("ObjectError:: {}", err);
                }
            }
            
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            if(fieldErrors != null) {
                
                final Function<FieldError, String> mapper = (fieldErr) -> {
                    
                    return fieldErr.getField() + (fieldErr.getDefaultMessage() == null ? "" : ": " + fieldErr.getDefaultMessage());
                };

                final List<String> errorMessages = fieldErrors.stream()
                        .map(mapper).collect(Collectors.toCollection(() -> new ArrayList()));

                if(errorMessages.size() > 1) {
                    errorMessages.add(0, "The following field(s) have errors");
                }
                
                addErrorMessages(model, errorMessages);
                
            }else{
                
                addErrorMessage(model, "Unexpeted error occured while processing");
            }
        }
    }
    
    public void addInfoMessage(Object model, Object value) {
        this.addCollectionAttribute(model, ModelAttributes.MESSAGES, value);
    }

    public void addInfoMessages(Object model, Object... messages) {
        this.addInfoMessages(model, Arrays.asList(messages));
    }
    
    public void addInfoMessages(Object model, Collection messages) {
        this.addCollectionAttribute(model, ModelAttributes.MESSAGES, messages);
    }

    public void addErrorMessage(Object model, Object value) {
        this.addCollectionAttribute(model, ModelAttributes.ERRORS, value);
    }

    public void addErrorMessages(Object model, Object... messages) {
        this.addErrorMessages(model, Arrays.asList(messages));
    }
    
    public void addErrorMessages(Object model, Collection messages) {
        this.addCollectionAttribute(model, ModelAttributes.ERRORS, messages);
    }

    public void addCollectionAttribute(Object model, String name, Object value) {
        final Map m;
        if(model instanceof Model) {
            m = ((Model)model).asMap();
        }else if(model instanceof ModelMap){
            m = (ModelMap)model;
        }else{
            throw new IllegalArgumentException("Found type: " + model.getClass() + 
                    ", expected any of: " + Arrays.toString(new Class[]{Model.class, ModelMap.class}));
        }
        
        Collection c = (Collection)m.get(name);
        
        LOG.trace("Existing: {} = {}", name, c);
        
        boolean added = false;
        
        if(c == null) {
            if(value instanceof Collection) {
                c = (Collection)value;
            }else{
                c = new ArrayList();
            }
            if(model instanceof Model) {
                ((Model)model).addAttribute(name, c);
                added = value instanceof Collection;
            }else if(model instanceof ModelMap){
                ((ModelMap)model).addAttribute(name, c);
                added = value instanceof Collection;
            }    
            if(added) {
                LOG.trace("For: {}, added: {}", name, value);
            }
        }
        
        if( ! added) {
            
            LOG.trace("For: {}, adding value: {} to: {}", name, value, c);
            
            c.add(value);
        }
    }

    /**
    * Returns the viewName to return to the referer url which brought the page to
    * the form initially.
    * @param FormConfig
    * @param request Instance of {@link HttpServletRequest} or use an injected instance
    * @return Optional with the view name. Recomended to use an alternativa url with
    * {@link Optional#orElse(java.lang.Object)}
    */
    public Optional<String> getRedirectToFormReferer(FormConfig formConfig, HttpServletRequest request) {
        final String referer = this.getFormReferer(formConfig, request);
        return Optional.ofNullable(referer).map(requestUrl -> "redirect:" + requestUrl);
    }

    /**
    * Returns the viewName to return for coming back to the sender url
    *
    * @param request Instance of {@link HttpServletRequest} or use an injected instance
    * @return Optional with the view name. Recomended to use an alternativa url with
    * {@link Optional#orElse(java.lang.Object)}
    */
    public Optional<String> getRedirectToReferer(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("referer")).map(requestUrl -> "redirect:" + requestUrl);
    }

    public Object getOrCreateModelobject(ModelMap model, String modelname,
            final Map<String, Object> defaultValues, 
            HttpServletRequest request, HttpServletResponse response) {
        
        Object modelobject = getModelobject(model, modelname, request, response).orElse(null);
        
        if(modelobject == null) {

            modelobject = typeFromNameResolver.newInstance(modelname);

            LOG.debug("Newly created modelobject: {}", modelobject);

            this.addValues(modelname, modelobject, defaultValues);
        }
        
        return modelobject;
    }

    public Optional<Object> getModelobject(ModelMap model, String modelname,
            HttpServletRequest request, HttpServletResponse response) {
        
        Object modelobject = model.get(ModelAttributes.MODELOBJECT);
        
        LOG.debug("Modelobject from ModelMap: {}", modelobject);
        
        if(modelobject == null) {
            
            modelobject = request.getSession().getAttribute(ModelAttributes.MODELOBJECT);
        }
        
        LOG.debug("Modelobject from HttpSession: {}", modelobject);
        
        if(modelobject != null) {
            
            final Class type = typeFromNameResolver.getType(modelname);
            
            if( ! type.equals(modelobject.getClass())) {
            
                modelobject = null;
                
                request.getSession().removeAttribute(ModelAttributes.MODELOBJECT);
            }
        }
        
        return Optional.ofNullable(modelobject);
    }

    public Object fetchModelobjectFromDatabase(String modelname, String modelid) {
    
        final Class modeltype = this.typeFromNameResolver.getType(modelname);
        
        final EntityRepository entityService = this.entityRepositoryFactory.forEntity(modeltype);
        
        final Object modelobject = entityService.find(modelid);

        LOG.debug("{} {} = {};", modeltype.getName(), modelname, modelobject);

        return modelobject;
    }
    
    public void addValues(String modelname, Object modelobject, Map<String, Object> values) {

        if(modelobject == null) {
            return;
        }
        
        if(values == null || values.isEmpty()) {
            return;
        }
        
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(modelobject);
        
        for(Map.Entry<String, Object> entry : values.entrySet()) {
            final String col = entry.getKey();
            if(bean.isReadableProperty(col) && bean.isWritableProperty(col)) {
                final Object val = bean.getPropertyValue(col);
                if(val == null) {
                    bean.setPropertyValue(col, entry.getValue());
                }
            }
        }
    }

    public void checkAttributes(String action, String modelname, Object modelobject){
        
        Objects.requireNonNull(action);
        Objects.requireNonNull(modelname);
        Objects.requireNonNull(modelobject);
        
        final String foundname = this.typeFromNameResolver.getName(modelobject.getClass());
        
        if( ! modelname.equals(foundname)) {
            
            LOG.warn("Expected name: {}, found name: {} from type: {}", modelname, foundname, modelobject.getClass());
        
            //@todo handle properly
            throw new IllegalArgumentException("Invalid access. May be caused by filling multiple forms simultaenously");
        }
    }

    public void setFormReferer(FormConfig formConfig, HttpServletRequest request) {
        request.getSession().setAttribute(getFormRefererAttributeName(formConfig), request.getHeader("referer"));
    }
    
    public String getFormReferer(FormConfig formConfig, HttpServletRequest request) {
        return (String)request.getSession().getAttribute(getFormRefererAttributeName(formConfig));
    }
    
    private String getFormRefererAttributeName(FormConfig formConfig) {
        return "form."+formConfig.getModelname()+"."+formConfig.getAction()+".referer";
    }

    public <T> Optional<T> findAttributeOptional(Object model, HttpServletRequest request, 
            String name, Class<T> type) {
        Object value = null;
        if(request != null) {
            value = request.getAttribute(name);
            if(value != null && !type.isAssignableFrom(value.getClass())) {
                value = null;
            }
            if(value == null) {
                value = request.getSession().getAttribute(name);
                if(value != null && !type.isAssignableFrom(value.getClass())) {
                    value = null;
                }
            }
        }
        if(value == null) {
            if(model instanceof Model) {
                final Model m = (Model)model;
                value = m.asMap().get(name);
            }else if(model instanceof ModelMap) {
                final ModelMap m = (ModelMap)model;
                value = m.get(name);
            }
        }
        if(value != null && !type.isAssignableFrom(value.getClass())) {
            value = null;
        }
        
        return value == null ? Optional.empty() : Optional.ofNullable((T)value);
    }
    
    public void addAttributes(Object model, FormConfig formConfig, Form form,
            HttpServletRequest request, HttpServletResponse response) {
        
        if(request != null) {

            setFormReferer(formConfig, request);

            final HttpSession session = request.getSession();
            session.setAttribute(ModelAttributes.MODELOBJECT, formConfig.getModelobject());
            session.setAttribute(ModelAttributes.FORM, form);
        }
        
        if(model instanceof Model) {
            final Model m = (Model)model;
            m.addAttribute(Params.ACTION, formConfig.getAction());
            m.addAttribute(Params.MODELNAME, formConfig.getModelname());
            m.addAttribute(Params.MODELID, formConfig.getModelid());
            m.addAttribute(ModelAttributes.MODELOBJECT, formConfig.getModelobject());
            m.addAttribute(Params.MODELFIELDS, formConfig.getModelfields());

            m.addAttribute(ModelAttributes.FORM, form);
        }else if(model instanceof ModelMap) {
            final ModelMap m = (ModelMap)model;
            m.addAttribute(Params.ACTION, formConfig.getAction());
            m.addAttribute(Params.MODELNAME, formConfig.getModelname());
            m.addAttribute(Params.MODELID, formConfig.getModelid());
            m.addAttribute(ModelAttributes.MODELOBJECT, formConfig.getModelobject());
            m.addAttribute(Params.MODELFIELDS, formConfig.getModelfields());

            m.addAttribute(ModelAttributes.FORM, form);
        }
    }

    public void removeAttributes(HttpServletRequest request, HttpServletResponse response) {
        final HttpSession session = request.getSession();
        session.removeAttribute(ModelAttributes.MODELOBJECT);
        session.removeAttribute(ModelAttributes.FORM);
    }

    public EntityRepositoryFactory getEntityRepositoryFactory() {
        return entityRepositoryFactory;
    }

    public final TypeFromNameResolver getTypeFromNameResolver() {
        return typeFromNameResolver;
    }

    public EntityFormProvider getEntityFormProvider() {
        return entityFormProvider;
    }
}
