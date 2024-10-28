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
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
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
import com.unboundid.util.ssl.AggregateTrustManager;
import com.unboundid.util.ssl.HostNameSSLSocketVerifier;
import com.unboundid.util.ssl.JVMDefaultTrustManager;
import com.unboundid.util.ssl.PEMFileTrustManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import io.ballerina.lib.ldap.ssl.SSLConfig;
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

import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    public static final BString CLIENT_SECURE_SOCKET = StringUtils.fromString("clientSecureSocket");
    public static final String NATIVE_CLIENT = "client";
    public static final String LDAP_RESPONSE = "LdapResponse";
    public static final BString REFERRAL = StringUtils.fromString("referral");
    public static final BString OPERATION_TYPE = StringUtils.fromString("operationType");
    public static final String OBJECT_GUID = "objectGUID";
    public static final String OBJECT_SID = "objectSid";

    //Socket config
    private static final BString SECURE_SOCKET_CONFIG_ENABLE_TLS = StringUtils.fromString("enable");
    private static final BString VERIFY_HOSTNAME = StringUtils.fromString("verifyHostName");
    private static final BString TLS_VERSIONS = StringUtils.fromString("tlsVersions");
    private static final BString SECURE_SOCKET_CONFIG_TRUSTSTORE_FILE_PATH = StringUtils.fromString("path");
    private static final BString SECURE_SOCKET_CONFIG_TRUSTSTORE_PASSWORD = StringUtils.fromString("password");
    private static final BString SECURE_SOCKET_CONFIG_CERT = StringUtils.fromString("cert");
    public static final String PKCS_12 = "PKCS12";
    public static final String PEM = "PEM";
    public static final String TRUST_STORE_INITIALIZATION_ERROR = "Error occurred while initializing trust store";
    public static final String UNSUPPORTED_TRUST_STORE_TYPE_ERROR = "Unsupported trust store type";
    public static final String EMPTY_TRUST_STORE_FILE_PATH_ERROR = "Truststore file path cannot be empty";
    public static final String EMPTY_TRUST_STORE_PASSWORD_ERROR = "Truststore password cannot be empty";
    public static final String EMPTY_CERTIFICATE_FILE_PATH_ERROR = "Certificate file path cannot be empty";

    private Client() {
    }

    public static BError initLdapConnection(BObject ldapClient, BMap<BString, Object> config) {
        String hostName = ((BString) config.get(HOST_NAME)).getValue();
        int port = Math.toIntExact(config.getIntValue(PORT));
        String domainName = ((BString) config.get(DOMAIN_NAME)).getValue();
        String password = ((BString) config.get(PASSWORD)).getValue();
        BMap<BString, Object> secureSocketConfig = (BMap<BString, Object>) config 
                .getMapValue(CLIENT_SECURE_SOCKET);
        try {
            if (Objects.nonNull(secureSocketConfig) && isClientSecurityConfigured(secureSocketConfig)) {
                SSLConfig sslConfig = populateSSLConfig(secureSocketConfig);
                AggregateTrustManager trustManager = buildAggregatedTrustManager(sslConfig);

                SSLUtil sslUtil = new SSLUtil(trustManager);

                if (sslConfig.getTLSVersions().isEmpty()) {
                    SSLUtil.setDefaultSSLProtocol(SSLUtil.SSL_PROTOCOL_TLS_1_2);
                } else {
                    SSLUtil.setEnabledSSLProtocols(sslConfig.getTLSVersions());
                }

                LDAPConnectionOptions connectionOptions = new LDAPConnectionOptions();

                connectionOptions.setSSLSocketVerifier(
                        new HostNameSSLSocketVerifier(sslConfig.getVerifyHostnames()));

                LDAPConnection ldapConnection = new LDAPConnection(sslUtil.createSSLSocketFactory(),
                        connectionOptions, hostName, port, domainName, password);

                ldapClient.addNativeData(NATIVE_CLIENT, ldapConnection);

            } else {
                LDAPConnection ldapConnection = new LDAPConnection(hostName, port, domainName, password);
                ldapClient.addNativeData(NATIVE_CLIENT, ldapConnection);
            }
        } catch (LDAPException | GeneralSecurityException e) {
            return Utils.createError(e.getMessage(), e);
        }
        return null;
    }

    private static SSLConfig populateSSLConfig(BMap<BString, Object> secureSocketConfig) {
        SSLConfig sslConfig = new SSLConfig();

        Object cert = secureSocketConfig.get(SECURE_SOCKET_CONFIG_CERT);
        evaluateCertField(cert, sslConfig);

        sslConfig.setVerifyHostnames(secureSocketConfig.getBooleanValue(VERIFY_HOSTNAME));

        BArray tlsVersions = (BArray) secureSocketConfig.get(TLS_VERSIONS);

        if (Objects.nonNull(tlsVersions)) {
            List<String> tlsVersionsList =  Arrays.stream(tlsVersions.getStringArray())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            sslConfig.setTLSVersions(tlsVersionsList);
        }
        return sslConfig;
    }

    private static boolean isClientSecurityConfigured(BMap<BString, Object> secureSocketConfig) {
        return secureSocketConfig.get(Client.SECURE_SOCKET_CONFIG_ENABLE_TLS) != null;
    }

    private static void evaluateCertField(Object cert, SSLConfig sslConfiguration) {
        if (cert instanceof BMap) {
            BMap<BString, BString> trustStore = (BMap<BString, BString>) cert;
            String trustStoreFile = trustStore.getStringValue(SECURE_SOCKET_CONFIG_TRUSTSTORE_FILE_PATH).getValue();
            String trustStorePassword = trustStore.getStringValue(SECURE_SOCKET_CONFIG_TRUSTSTORE_PASSWORD).getValue();
            if (trustStoreFile.isBlank()) {
                throw new IllegalArgumentException(EMPTY_TRUST_STORE_FILE_PATH_ERROR);
            }
            if (trustStorePassword.isBlank()) {
                throw new IllegalArgumentException(EMPTY_TRUST_STORE_PASSWORD_ERROR);
            }
            sslConfiguration.setTrustStoreFile(trustStoreFile);
            sslConfiguration.setTrustStorePass(trustStorePassword);
            sslConfiguration.setTLSStoreType(PKCS_12);
        } else {
            String certFile = ((BString) cert).getValue();
            if (certFile.isBlank()) {
                throw new IllegalArgumentException(EMPTY_CERTIFICATE_FILE_PATH_ERROR);
            }
            sslConfiguration.setTrustStoreFile(certFile);
            sslConfiguration.setTLSStoreType(PEM);
        }
    }

    private static AggregateTrustManager buildAggregatedTrustManager(SSLConfig sslConfiguration) {
        if (sslConfiguration.getTLSStoreType().equals(PEM)) {
            try {
                PEMFileTrustManager pemFileTrustManager = new PEMFileTrustManager(
                        sslConfiguration.getTrustStore());
                return new AggregateTrustManager(false,
                        JVMDefaultTrustManager.getInstance(),
                        pemFileTrustManager);
            } catch (KeyStoreException e) {
                throw new IllegalArgumentException(TRUST_STORE_INITIALIZATION_ERROR + e.getMessage());
            }
        } else if (sslConfiguration.getTLSStoreType().equals(PKCS_12)) {
            TrustStoreTrustManager trustStoreManager = new TrustStoreTrustManager(sslConfiguration.getTrustStore(),
                    sslConfiguration.getTrustStorePass().toCharArray(),
                    sslConfiguration.getTLSStoreType(), true);
            return new AggregateTrustManager(false,
                    JVMDefaultTrustManager.getInstance(),
                    trustStoreManager);
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_TRUST_STORE_TYPE_ERROR);
        }
    }

    public static Object add(Environment env, BObject ldapClient, BString dN, BMap<BString, Object> entry) {
        Future future = env.markAsync();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(NATIVE_CLIENT);
            validateConnection(ldapConnection);
            AddRequest addRequest = generateAddRequest(dN, entry);
            CustomAsyncResultListener customAsyncResultListener = new CustomAsyncResultListener(future);
            ldapConnection.asyncAdd(addRequest, customAsyncResultListener);
        } catch (LDAPException e) {
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
        } catch (LDAPException e) {
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
        response.put(MATCHED_DN, StringUtils.fromString(ldapResult.getMatchedDN()));
        response.put(RESULT_STATUS,
                     StringUtils.fromString(ldapResult.getResultCode().getName().toUpperCase(Locale.ROOT)));
        response.put(DIAGNOSTIC_MESSAGE, StringUtils.fromString(ldapResult.getDiagnosticMessage()));
        response.put(REFERRAL, convertToBArray(ldapResult.getReferralURLs()));
        response.put(OPERATION_TYPE, StringUtils.fromString(ldapResult.getOperationType().name()));
        return response;
    }
}
