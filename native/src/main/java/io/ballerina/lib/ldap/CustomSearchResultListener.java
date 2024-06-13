/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.ldap;

import com.unboundid.ldap.sdk.AsyncRequestID;
import com.unboundid.ldap.sdk.AsyncSearchResultListener;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultReference;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.lib.ldap.Ldap.processAttribute;
import static io.ballerina.lib.ldap.Utils.ENTRY_NOT_FOUND;
import static io.ballerina.lib.ldap.Utils.createEntryRecord;
import static io.ballerina.lib.ldap.Utils.createError;

/**
 * Callback class to handle search results asynchronously.
 */
public class CustomSearchResultListener implements AsyncSearchResultListener {

    Future future;
    List<BMap<BString, Object>> references;
    List<BMap<BString, Object>> entries;
    String distinguishedName;

    public CustomSearchResultListener(Future future, String distinguishedName) {
        this.distinguishedName = distinguishedName;
        this.future = future;
        this.references = new ArrayList<>();
        this.entries = new ArrayList<>();
    }

    @Override
    public void searchResultReceived(AsyncRequestID requestID, SearchResult searchResult) {
        if (entries.isEmpty()) {
            String errorMessage = ENTRY_NOT_FOUND + distinguishedName;
            LDAPException ldapException = new LDAPException(ResultCode.OTHER, errorMessage);
            future.complete(createError(ldapException.getMessage(), ldapException));
        } else {
            future.complete(Utils.createSearchResultRecord(searchResult, references, entries));
        }
    }

    @Override
    public void searchEntryReturned(SearchResultEntry searchEntry) {
        BMap<BString, Object> entry = createEntryRecord();
        for (Attribute attribute : searchEntry.getAttributes()) {
            processAttribute(attribute, entry);
        }
        entries.add(entry);
    }

    @Override
    public void searchReferenceReturned(SearchResultReference searchReference) {
        references.add(Utils.createSearchReferenceRecord(searchReference));
    }
}
