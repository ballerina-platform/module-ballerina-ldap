// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

# Consists of APIs to integrate with LDAP.
public isolated client class Client {

    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + return - An error if connector initialization failed
    public isolated function init(*ConnectionConfig config) returns error? {
        self.generateLdapClient(config);
    }

    private isolated function generateLdapClient(ConnectionConfig config) = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Updates information of an entry.
    # 
    # + distinguishedName - The distinguished name of the entry
    # + entry - The information to update
    # + return - `error` if the operation fails or `()` if successfully updated
    remote isolated function modify(string distinguishedName, record {|anydata...;|} entry)
        returns error? = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Gets information of an entry.
    # 
    # + distinguishedName - The distinguished name of the entry
    # + targetType - Default parameter use to infer the user specified type
    # + return - An entry result with the given type or else an error
    remote isolated function getEntry(string distinguishedName, typedesc<anydata> targetType = <>)
        returns targetType|error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;
}
