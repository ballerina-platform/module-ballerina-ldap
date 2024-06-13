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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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
    public static final String RESULT_STATUS = "resultCode";
    public static final String ERROR_MESSAGE = "message";
    public static final String ENTRY_NOT_FOUND = "LDAP entry is not found for DN: ";
    public static final String SID_REVISION_ERROR = "objectSid revision must be 1";

    public static BError createError(String message, Throwable throwable) {
        BError cause = (throwable == null) ? null : ErrorCreator.createError(throwable);
        BMap<BString, Object> errorDetails = getErrorDetails(throwable);
        return ErrorCreator.createError(getModule(), ERROR_TYPE, fromString(message), cause, errorDetails);
    }

    private static BMap<BString, Object> getErrorDetails(Throwable throwable) {
        if (throwable instanceof LDAPException) {
            String resultCode = ((LDAPException) throwable).getResultCode().getName().toUpperCase();
            String message = ((LDAPException) throwable).getExceptionMessage();
            return ValueCreator.createRecordValue(getModule(), ERROR_DETAILS,
                                                  Map.of(RESULT_STATUS, resultCode, ERROR_MESSAGE, message));
        }
        return ValueCreator.createRecordValue(getModule(), ERROR_DETAILS);
    }

    public static String[] convertToStringArray(Object[] objectArray) {
        if (objectArray == null) {
            return null;
        }
        return Arrays.stream(objectArray)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toArray(String[]::new);
    }

    public static String convertObjectSidToString(byte[] objectSid) {
        int offset, size;
        if (objectSid[0] != 1) {
            throw new IllegalArgumentException(SID_REVISION_ERROR);
        }
        StringBuilder stringSidBuilder = new StringBuilder("S-1-");
        int subAuthorityCount = objectSid[1] & 0xFF;
        long identifierAuthority = 0;
        offset = 2;
        size = 6;
        for (int i = 0; i < size; i++) {
            identifierAuthority |= (long) (objectSid[offset + i] & 0xFF) << (8 * (size - 1 - i));
        }
        if (identifierAuthority < Math.pow(2, 32)) {
            stringSidBuilder.append(identifierAuthority);
        } else {
            stringSidBuilder.append("0x").append(
                    Long.toHexString(identifierAuthority).toUpperCase());
        }
        offset = 8;
        size = 4;
        for (int i = 0; i < subAuthorityCount; i++, offset += size) {
            long subAuthority = 0;
            for (int j = 0; j < size; j++) {
                subAuthority |= (long) (objectSid[offset + j] & 0xFF) << (8 * j);
            }
            stringSidBuilder.append("-").append(subAuthority);
        }

        return stringSidBuilder.toString();
    }

    public static String convertObjectGUIDToString(byte[] objectGUID) {
        if (objectGUID == null || objectGUID.length != 16) {
            throw new IllegalArgumentException("objectGUID must be a 16-byte array");
        }
        return String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                objectGUID[3], objectGUID[2], objectGUID[1], objectGUID[0],
                objectGUID[5], objectGUID[4],
                objectGUID[7], objectGUID[6],
                objectGUID[8], objectGUID[9],
                objectGUID[10], objectGUID[11], objectGUID[12], objectGUID[13], objectGUID[14], objectGUID[15]);
    }
}
