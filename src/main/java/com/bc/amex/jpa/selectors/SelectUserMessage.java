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

package com.bc.amex.jpa.selectors;

import com.bc.elmi.pu.entities.Message;
import com.bc.elmi.pu.entities.Message_;
import com.bc.elmi.pu.entities.User;
import com.bc.elmi.pu.entities.User_;
import com.bc.jpa.dao.BuilderForCriteriaDao;
import com.bc.jpa.dao.Select;
import java.util.Objects;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

/**
 * @author Chinomso Bassey Ikwuagwu on May 12, 2019 9:26:40 PM
 */
public class SelectUserMessage implements Selector{

    private final Attribute attributeJoiningMessageToUser;

    public SelectUserMessage(String selector) {
        this(Selectors.INBOX.equals(selector) ? Message_.userList :
                Selectors.OUTBOX.equals(selector) ? Message_.sender : null);
    }
    
    public SelectUserMessage(Attribute attributeJoiningMessageToUser) {
        this.attributeJoiningMessageToUser = Objects.requireNonNull(attributeJoiningMessageToUser);
    }
    
    @Override
    public boolean isApplicable(Class entityType) {
        return Message.class.isAssignableFrom(entityType);
    }
    
    @Override
    public void accept(Select select) {
        final CriteriaBuilder cb = select.getCriteriaBuilder();
        select.join(Message.class, attributeJoiningMessageToUser, User.class); 
        final Join join = ((BuilderForCriteriaDao)select).getJoin();
        final Predicate messageidEqualsUserid = cb.equal(join.get(Message_.messageid), join.get(User_.userid)); 
        cb.and(messageidEqualsUserid);
    }
}
