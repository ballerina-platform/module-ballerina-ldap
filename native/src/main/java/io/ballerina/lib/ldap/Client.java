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
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.unboundid.ldap.sdk.ResultCode.NO_SUCH_OBJECT;
import static com.unboundid.ldap.sdk.ResultCode.OTHER;
import static io.ballerina.lib.ldap.Utils.ENTRY_NOT_FOUND;
import static io.ballerina.lib.ldap.Utils.LDAP_CONNECTION_CLOSED_ERROR;
import static io.ballerina.lib.ldap.Utils.convertObjectGUIDToString;
import static io.ballerina.lib.ldap.Utils.convertObjectSidToString;
import static io.ballerina.lib.ldap.Utils.convertToBArray;
import static io.ballerina.lib.ldap.Utils.convertToStringArray;
import static io.ballerina.lib.ldap.Utils.getSearchScope;

/**
 * This class handles APIs of the LDAP client.
 */
public final class Client {
    public static final BString RESULT_STATUS = StringUtils.fromString("resultCode");
    public static final BString MATCHED_DN = StringUtils.fromString("matchedDN");
    public static final BString DIAGNOSTIC_MESSAGE = StringUtils.fromString("diagnosticMessage");
    public static final BString HOST_NAME =  StringUtils.fromString("hostName");
    public static final BString PORT = StringUtils.fromString("port");
    public static final BString DOMAIN_NAME = StringUtils.fromString("domainName");
    public static final BString PASSWORD = StringUtils.fromString("password");
    public static final String NATIVE_CLIENT = "client";
    public static final String LDAP_RESPONSE = "LdapResponse";
    public static final BString REFERRAL = StringUtils.fromString("referral");
    public static final BString OPERATION_TYPE = StringUtils.fromString("operationType");
    public static final String OBJECT_GUID = "objectGUID";
    public static final String OBJECT_SID = "objectSid";

    private Client() {
    }

    public static BError initLdapConnection(BObject ldapClient, BMap<BString, Object> config) {
        String hostName = ((BString) config.get(HOST_NAME)).getValue();
        int port = Math.toIntExact(config.getIntValue(PORT));
        String domainName = ((BString) config.get(DOMAIN_NAME)).getValue();
        String password = ((BString) config.get(PASSWORD)).getValue();
        try {
            LDAPConnection ldapConnection = new LDAPConnection(hostName, port, domainName, password);
            ldapClient.addNativeData(NATIVE_CLIENT, ldapConnection);
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
        return null;
    }

    public static Object add(Environment env, BObject ldapClient, BString dN, BMap<BString, Object> entry) {
        env.markAsync();
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            AddRequest addRequest = generateAddRequest(dN, entry);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncAdd(addRequest, customAsyncResultListener);
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }

    }

    public static Object modify(Environment env, BObject ldapClient, BString dN, BMap<BString, BString> entry) {
        env.markAsync();
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            ModifyRequest modifyRequest = generateModifyRequest(dN, entry);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncModify(modifyRequest, customAsyncResultListener);
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object modifyDn(Environment env, BObject ldapClient, BString currentDn,
                                  BString newRdn, boolean deleteOldRdn) {
        env.markAsync();
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            ModifyDNRequest modifyRequest = new ModifyDNRequest(currentDn.getValue(), newRdn.getValue(), deleteOldRdn);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncModifyDN(modifyRequest, customAsyncResultListener);
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }

    }

    public static Object delete(Environment env, BObject ldapClient, BString dN) {
        env.markAsync();
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncDelete(new DeleteRequest(dN.getValue()), customAsyncResultListener);
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object compare(Environment env, BObject ldapClient,
                                 BString dN, BString attributeName, BString assertionValue) {
        env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            CompareRequest compareRequest = new CompareRequest(dN.getValue(), attributeName.getValue(),
                                                               assertionValue.getValue());
            CompletableFuture<Object> future = new CompletableFuture<>();
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
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object getEntry(BObject ldapClient, BString dN, BTypedesc typeParam) {
        BMap<BString, Object> entry = ValueCreator.createMapValue();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            SearchResultEntry userEntry = ldapConnection.getEntry(dN.getValue());
            if (Objects.isNull(userEntry)) {
                return Utils.createError(String.format(ENTRY_NOT_FOUND, dN), new LDAPException(NO_SUCH_OBJECT));
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
        env.markAsync();
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            SearchScope searchScope = getSearchScope(scope);
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            SearchResultListener searchResultListener = new CustomSearchResultListener(future, baseDn.getValue());
            SearchRequest searchRequest = new SearchRequest(searchResultListener, baseDn.getValue(),
                                                            searchScope, filter.getValue());
            ldapConnection.asyncSearch(searchRequest);
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object searchWithType(Environment env, BObject ldapClient, BString baseDn,
                                        BString filter, BString scope, BTypedesc typeParam) {
        env.markAsync();
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            SearchScope searchScope = getSearchScope(scope);
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            SearchResultListener searchResultListener = new CustomSearchEntryListener(future, typeParam,
                                                                                      baseDn.getValue());
            SearchRequest searchRequest = new SearchRequest(searchResultListener, baseDn.getValue(),
                                                            searchScope, filter.getValue());
            ldapConnection.asyncSearch(searchRequest);
            return future.get();
        } catch (Throwable e) {
            return Utils.createError(e.getMessage(), e);
        }
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
        response.put(MATCHED_DN, StringUtils.fromString(ldapResult.getMatchedDN()));
        response.put(RESULT_STATUS,
                     StringUtils.fromString(ldapResult.getResultCode().getName().toUpperCase(Locale.ROOT)));
        response.put(DIAGNOSTIC_MESSAGE, StringUtils.fromString(ldapResult.getDiagnosticMessage()));
        response.put(REFERRAL, convertToBArray(ldapResult.getReferralURLs()));
        response.put(OPERATION_TYPE, StringUtils.fromString(ldapResult.getOperationType().name()));
        return response;
    }
}
