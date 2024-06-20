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
import com.unboundid.ldap.sdk.CompareRequest;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyDNRequest;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultListener;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.Base64;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unboundid.ldap.sdk.ResultCode.NO_SUCH_OBJECT;
import static com.unboundid.ldap.sdk.ResultCode.OTHER;
import static io.ballerina.lib.ldap.ModuleUtils.DIAGNOSTIC_MESSAGE;
import static io.ballerina.lib.ldap.ModuleUtils.LDAP_RESPONSE;
import static io.ballerina.lib.ldap.ModuleUtils.MATCHED_DN;
import static io.ballerina.lib.ldap.ModuleUtils.NATIVE_CLIENT;
import static io.ballerina.lib.ldap.ModuleUtils.OBJECT_GUID;
import static io.ballerina.lib.ldap.ModuleUtils.OBJECT_SID;
import static io.ballerina.lib.ldap.ModuleUtils.OPERATION_TYPE;
import static io.ballerina.lib.ldap.ModuleUtils.REFERRAL;
import static io.ballerina.lib.ldap.ModuleUtils.RESULT_STATUS;
import static io.ballerina.lib.ldap.Utils.ENTRY_NOT_FOUND;
import static io.ballerina.lib.ldap.Utils.LDAP_CONNECTION_CLOSED_ERROR;
import static io.ballerina.lib.ldap.Utils.convertObjectGUIDToString;
import static io.ballerina.lib.ldap.Utils.convertObjectSidToString;
import static io.ballerina.lib.ldap.Utils.convertToStringArray;
import static io.ballerina.lib.ldap.Utils.getSearchScope;

/**
 * This class handles APIs of the LDAP client.
 */
public class Client {
    private Client() {
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

    public static Object add(Environment env, BObject ldapClient, BString dN, BMap<BString, Object> entry) {
        Future future = env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            AddRequest addRequest = generateAddRequest(dN, entry);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncAdd(addRequest, customAsyncResultListener);
        } catch (Exception e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static Object modify(Environment env, BObject ldapClient, BString dN, BMap<BString, BString> entry) {
        Future future = env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            ModifyRequest modifyRequest = generateModifyRequest(dN, entry);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncModify(modifyRequest, customAsyncResultListener);
        } catch (LDAPException e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static Object modifyDn(Environment env, BObject ldapClient, BString currentDn,
                                  BString newRdn, boolean deleteOldRdn) {
        Future future = env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            ModifyDNRequest modifyRequest = new ModifyDNRequest(currentDn.getValue(), newRdn.getValue(), deleteOldRdn);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncModifyDN(modifyRequest, customAsyncResultListener);
        } catch (LDAPException e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static Object delete(Environment env, BObject ldapClient, BString dN) {
        Future future = env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncDelete(new DeleteRequest(dN.getValue()), customAsyncResultListener);
        } catch (Exception e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static Object compare(Environment env, BObject ldapClient,
                                 BString dN, BString attributeName, BString assertionValue) {
        Future future = env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            CompareRequest compareRequest = new CompareRequest(dN.getValue(), attributeName.getValue(),
                                                               assertionValue.getValue());
            ldapConnection.asyncCompare(compareRequest, (requestID, compareResult) -> {
                if (compareResult.getResultCode().equals(ResultCode.COMPARE_TRUE)) {
                    future.complete(true);
                } else if (compareResult.getResultCode().equals(ResultCode.COMPARE_FALSE)) {
                    future.complete(false);
                } else {
                    LDAPException ldapException = new LDAPException(compareResult);
                    future.complete(Utils.createError(ldapException.getMessage(), ldapException));
                }
            });
        } catch (LDAPException e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static Object getEntry(BObject ldapClient, BString dN, BTypedesc typeParam) {
        BMap<BString, Object> entry = ValueCreator.createMapValue();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            SearchResultEntry userEntry = ldapConnection.getEntry(dN.getValue());
            if (userEntry == null) {
                return Utils.createError(ENTRY_NOT_FOUND + dN, new LDAPException(NO_SUCH_OBJECT));
            }
            for (Attribute attribute : userEntry.getAttributes()) {
                processAttribute(attribute, entry);
            }
            return ValueUtils.convert(entry, typeParam.getDescribingType());
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object search(Environment env, BObject ldapClient, BString baseDn, BString filter, BString scope) {
        Future future = env.markAsync();
        try {
            SearchScope searchScope = getSearchScope(scope);
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            SearchResultListener searchResultListener = new CustomSearchResultListener(future, baseDn.getValue());
            SearchRequest searchRequest = new SearchRequest(searchResultListener, baseDn.getValue(),
                                                            searchScope, filter.getValue());
            ldapConnection.asyncSearch(searchRequest);
        } catch (LDAPException e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static Object searchWithType(Environment env, BObject ldapClient, BString baseDn,
                                        BString filter, BString scope, BTypedesc typeParam) {
        Future future = env.markAsync();
        try {
            SearchScope searchScope = getSearchScope(scope);
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            SearchResultListener searchResultListener = new CustomSearchEntryListener(future, typeParam,
                                                                                      baseDn.getValue());
            SearchRequest searchRequest = new SearchRequest(searchResultListener, baseDn.getValue(),
                                                            searchScope, filter.getValue());
            ldapConnection.asyncSearch(searchRequest);
        } catch (LDAPException e) {
            future.complete(Utils.createError(e.getMessage(), e));
        }
        return null;
    }

    public static void close(BObject ldapClient) {
        LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
        ldapConnection.close();
    }

    public static boolean isConnected(BObject ldapClient) {
        LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
        return ldapConnection.isConnected();
    }

    public static void validateConnection(LDAPConnection ldapConnection) throws LDAPException {
        if (!ldapConnection.isConnected()) {
            throw new LDAPException(OTHER, LDAP_CONNECTION_CLOSED_ERROR);
        }
    }

    private static AddRequest generateAddRequest(BString dN, BMap<BString, Object> entry) {
        Entry newEntry = new Entry(dN.getValue());
        for (BString key: entry.getKeys()) {
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

    private static ModifyRequest generateModifyRequest(BString dN, BMap<BString, BString> entry) {
        List<Modification> modificationList = new ArrayList<>();
        for (BString key: entry.getKeys()) {
            if (TypeUtils.getType(entry.get(key)).getTag() == TypeTags.ARRAY_TAG) {
                BArray arrayValue = (BArray) entry.get(key);
                String[] stringArray = arrayValue.getElementType().getTag() == TypeTags.STRING_TAG
                        ? convertToStringArray(arrayValue.getStringArray())
                        : convertToStringArray(arrayValue.getValues());
                modificationList.add(new Modification(ModificationType.REPLACE, key.getValue(), stringArray));
            } else {
                modificationList.add(new Modification(ModificationType.REPLACE,
                                                      key.getValue(), entry.get(key).toString()));
            }
        }
        return new ModifyRequest(dN.getValue(), modificationList);
    }

    static void processAttribute(Attribute attribute, BMap<BString, Object> entry) {
        BString attributeName = StringUtils.fromString(attribute.getName());
        if (attribute.needsBase64Encoding()) {
            String readableString = encodeAttributeValue(attribute);
            entry.put(attributeName, StringUtils.fromString(readableString));
        } else {
            if (attribute.getValues().length > 1) {
                String[] values = attribute.getValues();
                BString[] stringValues = Arrays.stream(values).map(StringUtils::fromString).toArray(BString[]::new);
                entry.put(attributeName, ValueCreator.createArrayValue(stringValues));
            } else {
                entry.put(attributeName, StringUtils.fromString(attribute.getValue()));
            }
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

    public static BMap<BString, Object> generateLdapResponse(LDAPResult ldapResult) {
        BMap<BString, Object> response = ValueCreator.createRecordValue(ModuleUtils.getModule(), LDAP_RESPONSE);
        response.put(StringUtils.fromString(MATCHED_DN), StringUtils.fromString(ldapResult.getMatchedDN()));
        response.put(StringUtils.fromString(RESULT_STATUS),
                StringUtils.fromString(ldapResult.getResultCode().getName().toUpperCase()));
        response.put(StringUtils.fromString(DIAGNOSTIC_MESSAGE),
                StringUtils.fromString(ldapResult.getDiagnosticMessage()));
        response.put(StringUtils.fromString(REFERRAL), convertToStringArray(ldapResult.getReferralURLs()));
        response.put(StringUtils.fromString(OPERATION_TYPE),
                StringUtils.fromString(ldapResult.getOperationType().name()));
        return response;
    }
}
