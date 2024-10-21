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
package io.ballerina.lib.ldap.ssl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class that encapsulates SSLContext configuration.
 */

public class SSLConfig {

    private File trustStore;
    private String trustStorePass;
    private String tlsStoreType;
    private Boolean verifyHostnames;
    private List<String> tlsVersions;

    public SSLConfig() {}

    public File getTrustStore() {
        return trustStore;
    }

    public SSLConfig setTrustStore(File trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStore = new File(trustStoreFile);
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public SSLConfig setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
        return this;
    }

    public String getTLSStoreType() {
        return tlsStoreType;
    }

    public void setTLSStoreType(String tlsStoreType) {
        this.tlsStoreType = tlsStoreType;
    }

    public Boolean getVerifyHostnames() {
        return verifyHostnames;
    }

    public void setVerifyHostnames(Boolean verifyHostnames) {
        this.verifyHostnames = verifyHostnames;
    }

    public List<String> getTLSVersions() {
        return Collections.unmodifiableList((List<String>) tlsVersions);
    }

    public void setTLSVersions(List<String> tlsVersions) {
        this.tlsVersions = new ArrayList<>(tlsVersions);
    }
}

