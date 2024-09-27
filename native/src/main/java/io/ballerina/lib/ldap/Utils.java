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

import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultReference;
import com.unboundid.ldap.sdk.SearchScope;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    public static final String SEARCH_RESULT = "SearchResult";
    public static final String SEARCH_REFERENCE = "SearchReference";
    public static final String SEARCH_REFERENCES = "searchReferences";
    public static final String ENTRIES = "entries";
    public static final String ENTRY = "Entry";
    public static final String RESULT_STATUS = "resultCode";
    public static final String MESSAGE_ID = "messageId";
    public static final String URIS = "uris";
    public static final String CONTROLS = "controls";
    public static final String CONTROL = "Control";
    public static final String OID = "oid";
    public static final String IS_CRITICAL = "isCritical";
    public static final String VALUE = "value";
    public static final String ENTRY_NOT_FOUND = "Entry is not found for DN: '%s'";
    public static final String SID_REVISION_ERROR = "objectSid revision must be 1";
    public static final String OBJECT_GUID_LENGTH_ERROR = "objectGUID must be a 16-byte array";
    public static final String LDAP_CONNECTION_CLOSED_ERROR = "LDAP Connection has been closed";

    public static BError createError(String message, Throwable throwable) {
        if (throwable.getCause() instanceof LDAPException ldapException) {
            return createError(ldapException.getMessage(), ldapException.getCause());
        }
        BError cause = Objects.isNull(throwable) ? null : ErrorCreator.createError(throwable);
        return ErrorCreator.createError(getModule(), ERROR_TYPE, fromString(message), cause, null);
    }

    public static BError createError(String message, LDAPException ldapException) {
        BError cause = Objects.isNull(ldapException) ? null : ErrorCreator.createError(ldapException);
        BMap<BString, Object> errorDetails = getErrorDetails(Objects.requireNonNull(ldapException));
        return ErrorCreator.createError(getModule(), ERROR_TYPE, fromString(message), cause, errorDetails);
    }

    private static BMap<BString, Object> getErrorDetails(LDAPException ldapException) {
        String resultCode = ldapException.getResultCode().getName().toUpperCase(Locale.ROOT);
        return ValueCreator.createRecordValue(getModule(), ERROR_DETAILS, Map.of(RESULT_STATUS, resultCode));
    }

    public static BMap<BString, Object> createSearchResultRecord(SearchResult searchResult,
                                                                 List<BMap<BString, Object>> references,
                                                                 List<BMap<BString, Object>> entries) {
        String resultCode = searchResult.getResultCode().getName().toUpperCase(Locale.ROOT);
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(RESULT_STATUS, resultCode);
        if (!references.isEmpty()) {
            ArrayType referenceType = TypeCreator.createArrayType(TypeUtils.getType(references.get(0)));
            valueMap.put(SEARCH_REFERENCES, ValueCreator.createArrayValue(references.toArray(), referenceType));
        }
        if (!entries.isEmpty()) {
            ArrayType entriesType = TypeCreator.createArrayType(TypeUtils.getType(entries.get(0)));
            valueMap.put(ENTRIES, ValueCreator.createArrayValue(entries.toArray(), entriesType));
        }
        return ValueCreator.createRecordValue(getModule(), SEARCH_RESULT, valueMap);
    }

    public static BMap<BString, Object> createEntryRecord() {
        return ValueCreator.createRecordValue(getModule(), ENTRY, (BMap<BString, Object>) null);
    }

    public static BMap<BString, Object> createSearchReferenceRecord(SearchResultReference searchResultReference) {
        int messageId = searchResultReference.getMessageID();
        String[] uris = searchResultReference.getReferralURLs();
        List<BMap<BString, Object>> controls = new ArrayList<>();
        ArrayType controlArrayType = null;
        for (Control control: searchResultReference.getControls()) {
            controlArrayType = TypeCreator.createArrayType(TypeUtils.getType(control));
            controls.add(createControlRecord(control));
        }
        BArray referralUris = ValueCreator.createArrayValue(convertToBStringArray(uris));
        if (controlArrayType != null) {
            BArray controlElements = ValueCreator.createArrayValue(controls.toArray(), controlArrayType);
            return ValueCreator.createRecordValue(getModule(), SEARCH_REFERENCE,
                    Map.of(MESSAGE_ID, messageId, URIS, referralUris, CONTROLS, controlElements));
        }
        return ValueCreator.createRecordValue(getModule(), SEARCH_REFERENCE,
                Map.of(MESSAGE_ID, messageId, URIS, referralUris));
    }

    public static BMap<BString, Object> createControlRecord(Control control) {
        String oid = control.getOID();
        boolean isCritical = control.isCritical();
        String value = control.getValue().stringValue();
        return ValueCreator.createRecordValue(getModule(), CONTROL,
                                              Map.of(OID, oid, IS_CRITICAL, isCritical, VALUE, value));
    }

    public static BString[] convertToBStringArray(String[] stringArray) {
        BString[] bStringArray = new BString[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            bStringArray[i] = StringUtils.fromString(stringArray[i]);
        }
        return bStringArray;
    }

    public static String[] convertToStringArray(Object[] objectArray) {
        if (Objects.isNull(objectArray)) {
            return new String[]{};
        }
        return Arrays.stream(objectArray)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toArray(String[]::new);
    }

    public static BArray convertToBArray(Object[] objectArray) {
        if (Objects.isNull(objectArray)) {
            return ValueCreator.createArrayValue(new BString[]{});
        }
        BString[] array =  Arrays.stream(objectArray)
                .filter(Objects::nonNull)
                .map(object -> StringUtils.fromString(object.toString()))
                .toArray(BString[]::new);
        return ValueCreator.createArrayValue(array);
    }

    public static SearchScope getSearchScope(BString scope) {
        return switch (scope.getValue()) {
            case "SUB" -> SearchScope.SUB;
            case "ONE" -> SearchScope.ONE;
            case "BASE" -> SearchScope.BASE;
            case "SUBORDINATE_SUBTREE" -> SearchScope.SUBORDINATE_SUBTREE;
            default -> throw new IllegalArgumentException("Invalid scope value: " + scope.getValue());
        };
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
                    Long.toHexString(identifierAuthority).toUpperCase(Locale.ROOT));
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
        if (Objects.isNull(objectGUID) || objectGUID.length != 16) {
            throw new IllegalArgumentException(OBJECT_GUID_LENGTH_ERROR);
        }
        return String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                objectGUID[3], objectGUID[2], objectGUID[1], objectGUID[0],
                objectGUID[5], objectGUID[4],
                objectGUID[7], objectGUID[6],
                objectGUID[8], objectGUID[9],
                objectGUID[10], objectGUID[11], objectGUID[12], objectGUID[13], objectGUID[14], objectGUID[15]);
    }
}
