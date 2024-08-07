// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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

import ballerina/ldap;
import ballerina/io;

configurable string hostName = ?;
configurable int port = ?;
configurable string domainName = ?;
configurable string password = ?;

public function main() returns error? {
    ldap:Client ldapClient = check new ({
        hostName,
        port,
        domainName,
        password
    });

    ldap:Entry user = {
        "objectClass": "inetOrgPerson",
        "sn": "Alice",
        "cn": "Alice",
        "uid": "alice",
        "displayName": "Alice Parker"
    };

    // Add a new user to the library system.
    _ = check ldapClient->add("uid=alice,ou=Users,dc=library,dc=org", user);

    // Search for a book.
    ldap:SearchResult result = check ldapClient->search("ou=Books,dc=library,dc=org", "(cn=Dracula)", ldap:SUB);
    io:println(result.entries);

    ldap:Entry updateBook = {
        "member": "uid=alice,ou=Users,dc=library,dc=org"
    };

    // Updates the book.
    _ = check ldapClient->modify("cn=Dracula,ou=Books,dc=library,dc=org", updateBook);

    result = check ldapClient->search("ou=Books,dc=library,dc=org", "(cn=Dracula)", ldap:SUB);
    io:println(result.entries);
}
