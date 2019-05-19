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

package com.bc.amex.controllers;

import com.bc.amex.jpa.repository.EntityRepository;
import com.bc.amex.jpa.repository.EntityRepositoryFactory;
import com.bc.amex.services.ControllerService;
import com.bc.config.Config;
import com.bc.elmi.pu.entities.Message;
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import com.bc.elmi.pu.enums.MessagestatusEnum;
import com.bc.elmi.pu.enums.MessagetypeEnum;
import com.bc.socket.io.messaging.data.Devicedetails;
import com.looseboxes.mswordbox.config.ConfigFactory;
import com.looseboxes.mswordbox.config.ConfigService;
import com.looseboxes.mswordbox.functions.GetUserMessage;
import com.looseboxes.mswordbox.functions.UrlBuilder;
import com.looseboxes.mswordbox.functions.admin.GetDevicedetailsForUser;
import com.looseboxes.mswordbox.messaging.MessageSender;
import com.looseboxes.mswordbox.net.Response;
import com.looseboxes.mswordbox.net.Rest;
import com.looseboxes.mswordbox.ui.AppUiContext;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Chinomso Bassey Ikwuagwu on May 10, 2019 1:07:13 PM
 */
@Controller
public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

    @Autowired private EntityRepositoryFactory entityRepositoryFactory;
    @Autowired private GetDevicedetailsForUser getDevicedetailsForUser;
    @Autowired private MessageSender messageSender;
    @Autowired private ConfigFactory configFactory;
    @Autowired private ResponseBuilder responseBuilder;
    @Autowired private AppUiContext uiContext;
    @Autowired private PartParser partParser;
    @Autowired private ControllerService controllerService;
    
    private final String separator = "\n";
    
    @PostMapping(Rest.ENDPOINT_TRANSFER_MESSAGE)
    @ResponseBody public Response transferMessage(
            @RequestParam(value="usernames", required=true) Set<String> usernames,
            HttpServletRequest request, HttpServletResponse response) {
        
        final Map<String, String> errors = new LinkedHashMap<>();
        final StringBuilder builder = new StringBuilder();

        final AtomicInteger sent = new AtomicInteger(0);
        final AtomicInteger returned = new AtomicInteger(0);
        final Set<User> receivedBy = new LinkedHashSet<>();
        try{
            
            LOG.debug("Usernames: {}", usernames);
            
            final Message m = partParser.parse(request, "Message", Message.class);
            
            LOG.debug("{}", m);

            m.setStatus(MessagestatusEnum.Unsent.getEntity());
            m.setType(MessagetypeEnum.Generic.getEntity());
            
            final EntityRepository<User> uRepo = entityRepositoryFactory.forEntity(User.class);

            User sender = m.getSender();
            
            if(sender == null || sender.getUserid() == null) {
                throw new ValidationException("Message sender not defined");
            }
            
            sender = uRepo.findOrDefault(sender.getUserid(), null);
            if(sender == null) {
                throw new ValidationException("Invalid message sender");
            }
            
            m.setSender(sender);

            final EntityRepository<Message> mRepo = entityRepositoryFactory.forEntity(Message.class);
            
            mRepo.create(m);
            
            LOG.debug("Created: {}", m);
            
            final UrlBuilder urlBuilder = new UrlBuilder();

            final Config config = configFactory.getConfig(ConfigService.APP_PROTECTED);

            for(String username : usernames) {

                final Devicedetails dd = getDevicedetailsForUser.apply(username).orElse(null);

                LOG.trace("username: {}, device details: {}", username, dd);

                if(dd == null) {
                    errors.put("Username="+username, "Message recipient is not online");
                    continue;
                }

                final User recipient = uRepo.findSingleBy(User_.username, username, null);
                if(recipient == null) {
                    errors.put("Username="+username, "Invalid recipient. Not authorized or does not exist");
                    continue;
                }

                try{

                    final URL url = urlBuilder.buildFromIp(config, dd.getIpaddress(), Rest.ENDPOINT_SEND_MESSAGE);

                    LOG.debug("Sending message to recipient: {} @ {}\nmessage: {}", username, url, m);

                    messageSender.send(m, Collections.EMPTY_MAP, url, (r) -> {

                        if(r == null) {
                        
                            errors.put("Username="+username, "Message sent, delivery status unknown");

                            LOG.warn("Response is null");
                            
                            return;
                        }
                        
                        returned.incrementAndGet();

                        if( ! r.isError()) {

                            LOG.debug("Sent message received by recipient: {} @ {}\nmessage: {}", username, url, m);

                            if(recipient != null) {
                                
                                receivedBy.add(recipient);

                                List<User> uList = m.getUserList();
                                
                                if(uList == null) {
                                    uList = new ArrayList();
                                    m.setUserList(uList);
                                }
                                
                                uList.add(recipient);
                            }
                            
                            final MessagestatusEnum e = getMessagestatusEnum(usernames, sent.get(), receivedBy);
                
                            m.setStatus(e.getEntity());
                            
                            mRepo.update(m);
                            
                        }else{
                            
                            errors.put("Username="+username, "Message sent, but not delivered");
                        
                            LOG.debug("Not Delivered, message sent to recipient: {} @ {}\nmessage: {}", username, url, m);                        
                        }
                    });

                    sent.incrementAndGet();

                }catch(IOException | ParseException e) {

                    LOG.warn("Unexpected error", e);

                    errors.put("Username="+username, "Unexpected error");
                }
            }

            final MessagestatusEnum e = getMessagestatusEnum(usernames, sent.get(), receivedBy);

            m.setStatus(e.getEntity());

            builder.append(e.getValue());

            mRepo.update(m);

            return buildResponse(builder, errors);
            
        }catch(RuntimeException e) {
            
            final String m = new GetUserMessage().apply(e, "Unexpected exception");
            
            errors.put("failed", m);
            
            LOG.warn("Unexpected exception", e);
            
            return buildResponse(builder, errors);
        }
    }
    
    private MessagestatusEnum getMessagestatusEnum(Collection recipients, int sent, Collection receivedBy) {
        final MessagestatusEnum e;
        if(receivedBy.size() >= recipients.size()) {
            e = MessagestatusEnum.Delivered;
        }else if(receivedBy.size() < recipients.size()) {
            e = MessagestatusEnum.Partially_Delivered;
        }else if(sent >= recipients.size()) {
            e = MessagestatusEnum.Sent;
        }else if(sent > 0 && sent < recipients.size()) {
            e = MessagestatusEnum.Partially_Sent;
        }else{
            e = MessagestatusEnum.Send_Failed;
        }
        return e;
    }
    
    private Response buildResponse(StringBuilder builder, Map<String, String> errors) {
    
        for(String name : errors.keySet()) {
            if(builder.length() != 0) {
                builder.append(separator);
            }
            builder.append(name).append(": ").append(errors.getOrDefault(name, null));
        }

        return responseBuilder.buildResponse(errors, builder, !errors.isEmpty());
    }

    @PostMapping(Rest.ENDPOINT_SEND_MESSAGE)
    @ResponseBody public Response sendMessage(
            HttpServletRequest request, HttpServletResponse response) {

        LOG.debug("#sendMessage");

        final Message message = partParser.parse(request, "Message", Message.class);

        final User user = controllerService.getLoggedinUser(request).orElse(null);
        
        LOG.debug("Logged in user: {}", user);
        
        if(user == null || ! message.getUserList().contains(user)) {
        
            return responseBuilder.buildResponse(
                    MessagestatusEnum.Partially_Delivered.getEntity(), "Recipient not online", true);
        }
        
        LOG.debug("Received message: {}", message);
        
        new Thread("PromptUserViewIncomingMessage_Thread"){
            @Override
            public void run() {
                promptUserViewMessage(message);
            }
        }.start();
        
        return responseBuilder.buildResponse(
                MessagestatusEnum.Partially_Delivered.getEntity(), "success", false);
    }
    
    @RequestMapping(Rest.ENDPOINT_OPEN_MESSAGE)
    public String openMessage(ModelMap model,
            @RequestParam(value="modelid", required=true) Integer modelid,
            HttpServletRequest request, HttpServletResponse response) {
        
        LOG.debug("#openMessage({})", modelid);

        final Message message = this.entityRepositoryFactory.forEntity(Message.class).find(modelid);
        
        model.addAttribute("message", message);
        
        return Templates.MESSAGE;
    }
    
    private void promptUserViewMessage(Message message) {
        JButton browseBtn = null;
        final URI uri = getUri(message, null);
        if(uri != null && Desktop.isDesktopSupported()) {
            final Desktop dt = Desktop.getDesktop();
            if(dt.isSupported(Desktop.Action.BROWSE)) {
                browseBtn = new JButton("Open in Browser");
                browseBtn.addActionListener((ae) -> {
                    try{
                        dt.browse(uri);
                    }catch(Exception e) {
                        final String m = "Unexpected Exception";
                        LOG.warn(m, e);
                        final String um = new GetUserMessage().apply(e, m);
                        uiContext.getMessageDialog().showWarningMessage(um);
                    }
                });
            }
        }
        
        final Font font = uiContext.getAwtFont();
        final JComponent comp;
        if(browseBtn != null) {
            comp = browseBtn;
        }else{
            comp = new JLabel("Login to the Web Portal to view the Message");
        }
        comp.setFont(font);
        
        final JPanel panel = new JPanel();
        panel.setFont(font);
        panel.setLayout(new GridLayout(2, 1));
        panel.add(new JLabel("Subject: " + truncate(message.getSubject(), 100)));
        panel.add(comp);
        final JScrollPane pane = new JScrollPane(panel);
        
        //@todo Add Message to cache
        JOptionPane.showMessageDialog(uiContext.getMainWindowOptional().orElse(null), 
                pane, "Incoming Message from: " + message.getSender().getUsername(),
                JOptionPane.PLAIN_MESSAGE, 
                uiContext.getImageIconOptional().orElse(null));
    }
    
    private String truncate(String s, int max){
        return s == null ? null : s.length() <= max ? s : s.substring(0, max);
    }
    
    private URI getUri(Message m, URI outputIfNone) {
        final Integer id = m.getMessageid();
        final Config config = configFactory.getConfig(ConfigService.APP_PROTECTED);
        URI u = null;
        try{
            if(id == null) {
                u = new UrlBuilder().build(config, SearchController.SEARCH_INBOX).get(0).toURI();
                return u == null ? outputIfNone : u;
            }else{
                u = new UrlBuilder().build(config, Collections.singletonMap("modelid", id), 
                        false, Rest.ENDPOINT_OPEN_MESSAGE).get(0).toURI();
            }
        }catch(MalformedURLException | URISyntaxException e) {
            LOG.warn("Failed to build URI for Message: " + m, e);
        }
        return u == null ? outputIfNone : u;
    }
}
