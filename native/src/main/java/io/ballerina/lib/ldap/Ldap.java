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

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.util.Base64;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.lib.ldap.ModuleUtils.LDAP_RESPONSE;
import static io.ballerina.lib.ldap.ModuleUtils.MATCHED_DN;
import static io.ballerina.lib.ldap.ModuleUtils.OBJECT_GUID;
import static io.ballerina.lib.ldap.ModuleUtils.OBJECT_SID;
import static io.ballerina.lib.ldap.ModuleUtils.OPERATION_TYPE;
import static io.ballerina.lib.ldap.ModuleUtils.RESULT_STATUS;
import static io.ballerina.lib.ldap.Utils.ENTRY_NOT_FOUND;

/**
 * This class handles APIs of the LDAP client.
 */
public class Ldap {
    private Ldap() {
    }

    public static BError initLdapConnection(BObject ldapClient, BMap<BString, Object> config) {
        String hostName = ((BString) config.get(ModuleUtils.HOST_NAME)).getValue();
        int port = Math.toIntExact(config.getIntValue(ModuleUtils.PORT));
        String domainName = ((BString) config.get(ModuleUtils.DOMAIN_NAME)).getValue();
        String password = ((BString) config.get(ModuleUtils.PASSWORD)).getValue();
        try {
            LDAPConnection ldapConnection = new LDAPConnection(hostName, port, domainName, password);
            ldapClient.addNativeData(ModuleUtils.NATIVE_CLIENT, ldapConnection);
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
        return null;
    }

    public static Object modify(BObject ldapClient, BString distinguishedName, BMap<BString, BString> entry) {
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(ModuleUtils.NATIVE_CLIENT);
            List<Modification> modificationList = entry.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .map(e -> new Modification(ModificationType.REPLACE,
                                               e.getKey().getValue(), e.getValue().getValue()))
                    .collect(Collectors.toList());
            ModifyRequest modifyRequest = new ModifyRequest(distinguishedName.getValue(), modificationList);
            LDAPResult ldapResult = ldapConnection.modify(modifyRequest);
            return generateLdapResponse(ldapResult);
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object getEntry(BObject ldapClient, BString distinguishedName, BTypedesc typeParam) {
        BMap<BString, Object> entry = ValueCreator.createMapValue();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(ModuleUtils.NATIVE_CLIENT);
            SearchResultEntry userEntry = ldapConnection.getEntry(distinguishedName.getValue());
            if (userEntry == null) {
                return Utils.createError(ENTRY_NOT_FOUND + distinguishedName, null);
            }
            for (Attribute attribute : userEntry.getAttributes()) {
                processAttribute(attribute, entry);
            }
            return ValueUtils.convert(entry, typeParam.getDescribingType());
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    private static void processAttribute(Attribute attribute, BMap<BString, Object> entry) {
        BString attributeName = StringUtils.fromString(attribute.getName());
        if (attribute.needsBase64Encoding()) {
            String readableString = encodeAttributeValue(attribute);
            entry.put(attributeName, StringUtils.fromString(readableString));
        } else {
            entry.put(attributeName, StringUtils.fromString(attribute.getValue()));
        }
    }

    private static String encodeAttributeValue(Attribute attribute) {
        byte[] valueBytes = attribute.getValueByteArray();
        return switch (attribute.getName()) {
            case OBJECT_GUID -> convertObjectGUIDToString(valueBytes);
            case OBJECT_SID -> convertObjectSidToString(valueBytes);
            default -> Base64.encode(valueBytes);
        };
    }

    private static BMap<BString, Object> generateLdapResponse(LDAPResult ldapResult) {
        BMap<BString, Object> response = ValueCreator.createRecordValue(ModuleUtils.getModule(), LDAP_RESPONSE);
        response.put(StringUtils.fromString(MATCHED_DN), StringUtils.fromString(ldapResult.getMatchedDN()));
        response.put(StringUtils.fromString(RESULT_STATUS),
                StringUtils.fromString(ldapResult.getResultCode().getName().toUpperCase()));
        response.put(StringUtils.fromString(OPERATION_TYPE),
                StringUtils.fromString(ldapResult.getOperationType().name()));
        return response;
    }

    public static String convertObjectSidToString(byte[] objectSid) {
        int offset, size;
        if (objectSid[0] != 1) {
            throw new IllegalArgumentException("objectSid revision must be 1");
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
        StringBuilder displayStr = new StringBuilder();
        displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF));
        return displayStr.toString();
    }


    private static String prefixZeros(int value) {
        if (value <= 0xF) {
            StringBuilder sb = new StringBuilder("0");
            sb.append(Integer.toHexString(value));

            return sb.toString();

        } else {
            return Integer.toHexString(value);
        }
    }
}
