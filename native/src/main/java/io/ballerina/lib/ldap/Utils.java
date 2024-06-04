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

import com.unboundid.ldap.sdk.LDAPException;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.Map;

import static io.ballerina.lib.ldap.ModuleUtils.getModule;
import static io.ballerina.runtime.api.utils.StringUtils.fromString;

/**
 * This class contains utility methods for the Ballerina LDAP module.
 */
public final class Utils {

    private Utils() {
    }

    public static final String ERROR_TYPE = "Error";
    public static final String ERROR_DETAILS = "ErrorDetails";
    public static final String RESULT_STATUS = "resultStatus";
    public static final String ERROR_MESSAGE = "message";
    public static final String ENTRY_NOT_FOUND = "LDAP entry is not found for DN: ";

    public static BError createError(String message, Throwable throwable) {
        BError cause = (throwable == null) ? null : ErrorCreator.createError(throwable);
        BMap<BString, Object> errorDetails = getErrorDetails(throwable);
        return ErrorCreator.createError(getModule(), ERROR_TYPE, fromString(message), cause, errorDetails);
    }

    private static BMap<BString, Object> getErrorDetails(Throwable throwable) {
        if (throwable instanceof LDAPException) {
            String resultStatus = ((LDAPException) throwable).getResultCode().getName();
            String message = ((LDAPException) throwable).getExceptionMessage();
            return ValueCreator.createRecordValue(getModule(), ERROR_DETAILS,
                                                  Map.of(RESULT_STATUS, resultStatus, ERROR_MESSAGE, message));
        }
        return ValueCreator.createRecordValue(getModule(), ERROR_DETAILS);
    }
}
