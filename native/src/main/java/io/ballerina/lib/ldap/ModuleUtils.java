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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Utility functions of the LDAP module.
 *
 * @since 0.1.0
 */
public final class ModuleUtils {

    public static final BString HOST_NAME =  StringUtils.fromString("hostName");
    public static final BString PORT = StringUtils.fromString("port");
    public static final BString DOMAIN_NAME = StringUtils.fromString("domainName");
    public static final BString PASSWORD = StringUtils.fromString("password");

    public static final String NATIVE_CLIENT = "client";
    public static final String LDAP_RESPONSE = "LDAPResponse";
    public static final String RESULT_STATUS = "resultStatus";
    public static final String MATCHED_DN = "matchedDN";
    public static final String OPERATION_TYPE = "operationType";

    private ModuleUtils() {}

    private static Module ldapModule = null;

    public static Module getModule() {
        return ldapModule;
    }

    public static void setModule(Environment env) {
        ldapModule = env.getCurrentModule();
    }
}
