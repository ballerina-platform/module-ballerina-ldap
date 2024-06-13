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

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.util.Base64;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.List;
import java.util.stream.Collectors;

import static com.unboundid.ldap.sdk.ResultCode.NO_SUCH_OBJECT;
import static io.ballerina.lib.ldap.ModuleUtils.LDAP_RESPONSE;
import static io.ballerina.lib.ldap.ModuleUtils.MATCHED_DN;
import static io.ballerina.lib.ldap.ModuleUtils.NATIVE_CLIENT;
import static io.ballerina.lib.ldap.ModuleUtils.OBJECT_GUID;
import static io.ballerina.lib.ldap.ModuleUtils.OBJECT_SID;
import static io.ballerina.lib.ldap.ModuleUtils.OPERATION_TYPE;
import static io.ballerina.lib.ldap.ModuleUtils.RESULT_STATUS;
import static io.ballerina.lib.ldap.Utils.ENTRY_NOT_FOUND;
import static io.ballerina.lib.ldap.Utils.convertObjectGUIDToString;
import static io.ballerina.lib.ldap.Utils.convertObjectSidToString;
import static io.ballerina.lib.ldap.Utils.convertToStringArray;

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
            ldapClient.addNativeData(NATIVE_CLIENT, ldapConnection);
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
        return null;
    }

    public static Object modify(BObject ldapClient, BString distinguishedName, BMap<BString, BString> entry) {
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            ModifyRequest modifyRequest = generateModifyRequest(distinguishedName, entry);
            LDAPResult ldapResult = ldapConnection.modify(modifyRequest);
            return generateLdapResponse(ldapResult);
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object add(BObject ldapClient, BString distinguishedName, BMap<BString, Object> entry) {
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            AddRequest addRequest = generateAddRequest(distinguishedName, entry);
            LDAPResult ldapResult = ldapConnection.add(addRequest);
            return generateLdapResponse(ldapResult);
        } catch (Exception e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object delete(BObject ldapClient, BString distinguishedName) {
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            LDAPResult ldapResult = ldapConnection.delete(new DeleteRequest(distinguishedName.getValue()));
            return generateLdapResponse(ldapResult);
        } catch (Exception e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object getEntry(BObject ldapClient, BString distinguishedName, BTypedesc typeParam) {
        BMap<BString, Object> entry = ValueCreator.createMapValue();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            SearchResultEntry userEntry = ldapConnection.getEntry(distinguishedName.getValue());
            if (userEntry == null) {
                return Utils.createError(ENTRY_NOT_FOUND + distinguishedName, new LDAPException(NO_SUCH_OBJECT));
            }
            for (Attribute attribute : userEntry.getAttributes()) {
                processAttribute(attribute, entry);
            }
            return ValueUtils.convert(entry, typeParam.getDescribingType());
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    private static AddRequest generateAddRequest(BString distinguishedName, BMap<BString, Object> entry) {
        Entry newEntry = new Entry(distinguishedName.getValue());
        for (BString key: entry.getKeys()) {
            if (entry.get(key) == null) {
                continue;
            }
            if (TypeUtils.getType(entry.get(key)).getTag() == TypeTags.ARRAY_TAG) {
                BArray arrayValue = (BArray) entry.get(key);
                String[] stringArray = arrayValue.getElementType().getTag() == TypeTags.STRING_TAG
                        ? convertToStringArray(arrayValue.getStringArray())
                        : convertToStringArray(arrayValue.getValues());
                newEntry.addAttribute(key.getValue(), stringArray);
            } else {
                newEntry.addAttribute(key.getValue(), entry.get(key).toString());
            }
        }
        return new AddRequest(newEntry);
    }

    private static ModifyRequest generateModifyRequest(BString distinguishedName, BMap<BString, BString> entry) {
        List<Modification> modificationList = entry.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> new Modification(ModificationType.REPLACE,
                        e.getKey().getValue(), e.getValue().getValue()))
                .collect(Collectors.toList());
        ModifyRequest modifyRequest = new ModifyRequest(distinguishedName.getValue(), modificationList);
        return modifyRequest;
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
}
