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
import com.unboundid.ldap.sdk.AsyncResultListener;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import io.ballerina.runtime.api.Future;

import static io.ballerina.lib.ldap.Ldap.generateLdapResponse;

/**
 * Callback class to handle asynchronous operations.
 */
public class CustomAsyncResultListener implements AsyncResultListener {
    private final Future future;

    public CustomAsyncResultListener(Future future) {
        this.future = future;
    }

    @Override
    public void ldapResultReceived(AsyncRequestID requestID, LDAPResult ldapResult) {
        if (ldapResult.getResultCode().equals(ResultCode.SUCCESS)) {
            future.complete(generateLdapResponse(ldapResult));
        } else {
            LDAPException ldapException = new LDAPException(ldapResult);
            future.complete(Utils.createError(ldapException.getMessage(), ldapException));
        }
    }
}
