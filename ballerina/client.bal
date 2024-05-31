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

# Consists of APIs to integrate with Avro Schema Registry.
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

    remote isolated function modify(string distinguishedName, record {|anydata...;|} user)
        returns error? = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    remote isolated function getEntry(string distinguishedName, typedesc<anydata> targetType = <>)
        returns targetType|error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;
}
