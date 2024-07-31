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
configurable string userDN = ?;

type Employee record {
    string[] objectClass;
    string sn;
};

public function main() returns error? {
    // Authenticate using the directory server credentials.
    ldap:Client ldapClient = check new ({
        hostName,
        port,
        domainName,
        password
    });

    ldap:Entry employee = {
        "objectClass": ["top", "person"],
        "sn": "New User"
    };

    // Add a new employee to the directory server.
    ldap:LdapResponse addResponse = check ldapClient->add("cn=User,dc=mycompany,dc=com", employee);
    io:println(addResponse);

    // Search for the employee.
    Employee[] result = check ldapClient->searchWithType("dc=mycompany,dc=com", "(sn=New User)", ldap:SUB);
    io:println(result);

    // Update the employee.
    ldap:LdapResponse updateResponse = check ldapClient->modify("cn=User,dc=mycompany,dc=com", { "sn": "Updated User" });
    io:println(updateResponse);

    // Delete the employee.
    ldap:LdapResponse response = check ldapClient->delete("CN=User,dc=mycompany,dc=com");
    io:println(response);
}
