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

    # Gets invoked to initialize the LDAP client.
    #
    # + config - The configurations to be used when initializing the client
    # + return - A `ldap:Error` if client initialization failed
    public isolated function init(*ConnectionConfig config) returns Error? {
        check self.initLdapConnection(config);
    }

    private isolated function initLdapConnection(ConnectionConfig config) returns Error? = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Creates an entry in a directory server.
    #
    # + distinguishedName - The distinguished name of the entry
    # + entry - The information to add
    # + return - A `ldap:Error` if the operation fails or `ldap:LdapResponse` if successfully created
    remote isolated function add(string distinguishedName, record {|anydata...;|} entry)
        returns LdapResponse|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Removes an entry in a directory server.
    #
    # + distinguishedName - The distinguished name of the entry to remove
    # + return - A `ldap:Error` if the operation fails or `ldap:LdapResponse` if successfully removed
    remote isolated function delete(string distinguishedName) returns LdapResponse|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Updates information of an entry.
    # 
    # + distinguishedName - The distinguished name of the entry
    # + entry - The information to update
    # + return - A `ldap:Error` if the operation fails or `LdapResponse` if successfully updated
    remote isolated function modify(string distinguishedName, record {|anydata...;|} entry)
        returns LdapResponse|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Renames an entry in a directory server.
    #
    # + currentDN - The current distinguished name of the entry
    # + newRDN - The new relative distinguished name
    # + deleteOldRDN - A boolean value to determine whether to delete the old RDN
    # + return - A `ldap:Error` if the operation fails or `ldap:LdapResponse` if successfully renamed
    remote isolated function modifyDN(string currentDN, string newRDN, boolean deleteOldRDN = true)
        returns LdapResponse|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Determines whether a given entry has a specified attribute value.
    # 
    # + distinguishedName - The distinguished name of the entry
    # + attribiteName - The name of the target attribute for which the comparison is to be performed
    # + assertionValue - The assertion value to verify within the entry
    # + return - A `ldap:Error` if the operation fails or `ldap:LdapResponse` if successfully executed
    remote isolated function compare(string distinguishedName, string attribiteName, string assertionValue)
        returns LdapResponse|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Gets information of an entry.
    # 
    # + distinguishedName - The distinguished name of the entry
    # + targetType - Default parameter use to infer the user specified type
    # + return - An entry result with the given type or else `ldap:Error`
    remote isolated function getEntry(string distinguishedName, typedesc<anydata> targetType = <>)
        returns targetType|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Returns a list of entries that match the given search parameters.
    # 
    # + baseDN - The base distinguished name of the entry
    # + filter - The filter to be used in the search
    # + scope - The scope of the search
    # + targetType - Default parameter use to infer the user specified type
    # + return - An array of entries with the given type or else `ldap:Error`
    remote isolated function searchWithType(string baseDN, string filter, 
                                            SearchScope scope, typedesc<anydata[]> targetType = <>)
        returns targetType|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Returns a record containing search result entries and references that match the given search parameters.
    # 
    # + baseDN - The base distinguished name of the entry
    # + filter - The filter to be used in the search
    # + scope - The scope of the search
    # + return - An `ldap:SearchResult` if successful, or else `ldap:Error`
    remote isolated function search(string baseDN, string filter, SearchScope scope)
        returns SearchResult|Error = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Unbinds from the server and closes the LDAP connection. 
    # 
    remote isolated function close() = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;

    # Determines whether the client is connected to the server.
    # 
    # + return - A boolean value indicating the connection status
    remote isolated function isConnected() returns boolean = @java:Method {
        'class: "io.ballerina.lib.ldap.Ldap"
    } external;
}
