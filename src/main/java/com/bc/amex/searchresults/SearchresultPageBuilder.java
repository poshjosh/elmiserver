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

package com.bc.amex.searchresults;

import com.bc.jpa.dao.search.SearchResults;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 22, 2019 2:21:41 PM
 */
public class SearchresultPageBuilder extends AbstractList<Searchresult>
        implements SearchresultPage{

    private boolean imageType;
    
    private int page;
    
    private SearchResults searchResults;
    
    private SearchresultBuilder resultBuilder;
    
    private List<Searchresult> delegate = Collections.EMPTY_LIST;
           
    public SearchresultPageBuilder() { }
    
    public SearchresultPage build() {
        
        Objects.requireNonNull(this.searchResults);
        Objects.requireNonNull(this.resultBuilder);
        if(page < 0) {
            throw new IllegalArgumentException("Page: "+ page + " < 0");
        }
        if(page >= this.searchResults.getPageSize()) {
            throw new IllegalArgumentException("Page: "+ page + " >= " + this.searchResults.getPageSize());
        }

        delegate = new ArrayList(this.searchResults.getPageSize());

        if(this.searchResults.getSize() > 0) { 
            
            if(this.searchResults.getPageNumber() != page) {
                this.searchResults.setPageNumber(page);
            }

            final List currentPage = this.searchResults.getCurrentPage();

            imageType = true;

            for(Object model : currentPage) {

                if( ! resultBuilder.isImageType(model.getClass())) {
                    imageType = false;
                }

                delegate.add(resultBuilder.apply(model));
            }
        }

        return this;
    }

    public SearchresultPageBuilder searchResults(SearchResults searchResults) {
        this.searchResults = searchResults;
        return this;
    }

    public SearchresultPageBuilder page(int page) {
        this.page = page;
        if(page < 0) {
            throw new IllegalArgumentException("Page: "+ page + " < 0");
        }
        return this;
    }

    public SearchresultPageBuilder resultBuilder(SearchresultBuilder resultBuilder) {
        this.resultBuilder = resultBuilder;
        return this;
    }
    
    @Override
    public int getOffset() {
        return getPageNumber() * this.searchResults.getPageSize();
    }
    
    @Override
    public int getStart() {
        return getOffset() + 1;
    }
    
    @Override
    public int getEnd() {
        return getOffset() + this.size();
    }
    
    @Override
    public int getTotal() {
        return this.searchResults.getSize();
    }
    
    @Override
    public boolean isHasNext() {
        return this.getPageNumber() < this.searchResults.getPageCount() - 1;
    }
    
    @Override
    public boolean isHasPrevious() {
        return this.getPageNumber() > 0;
    }
    
    @Override
    public int getPageNumber() {
        return this.searchResults.getPageNumber();
    }

    @Override
    public boolean isImageType() {
        return imageType;
    }
    
    @Override
    public SearchResults getSearchResults() {
        return this.searchResults;
    }
    
    @Override
    public Searchresult get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }
}
