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

import ballerina/test;

configurable string hostName = ?;
configurable int port = ?;
configurable string domainName = ?;
configurable string password = ?;
configurable string userDN = ?;

Client ldapClient = check new ({
    hostName: hostName,
    port: port,
    domainName: domainName,
    password: password
});

@test:Config {
}
public function testAddUser() returns error? {
    UserConfig user = {
        "objectClass": ["user", organizationalPerson, "person", "top"],
        sn: "User",
        cn: "User",
        givenName: "User",
        displayName: "User",
        userPrincipalName: "user@ad.windows",
        userAccountControl: "544"
    };
    LdapResponse val = check ldapClient->add("CN=User,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testAddUser]
}
public function testAddUserWithManager() returns error? {
    record {} user = {
        "objectClass": "user",
        "sn": "New User",
        "cn": "New User",
        "givenName": "New User",
        "displayName": "New User",
        "userPrincipalName": "newuser@ad.windows",
        "userAccountControl": "544",
        "manager": "CN=User,OU=People,DC=ad,DC=windows"
    };
    LdapResponse addResult = check ldapClient->add("CN=New User,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(addResult.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testGetUser]
}
public function testDeleteUserHavingManager() returns error? {
    LdapResponse val = check ldapClient->delete("CN=New User,OU=People,DC=ad,DC=windows");
    test:assertEquals(val.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testDeleteUserHavingManager]
}
public function testDeleteUser() returns error? {
    LdapResponse val = check ldapClient->delete("CN=User,OU=People,DC=ad,DC=windows");
    test:assertEquals(val.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testAddUserWithManager]
}
public function testAddAlreadyExistingUser() returns error? {
    UserConfig user = {
        objectClass: "user",
        sn: "New User",
        cn: "New User",
        givenName: "New User",
        displayName: "New User",
        userPrincipalName: "newuser@ad.windows"
    };
    LdapResponse|Error val = ldapClient->add("CN=New User,OU=People,DC=ad,DC=windows", user);
    test:assertTrue(val is Error);
    if val is Error {
        ErrorDetails errorDetails = val.detail();
        test:assertEquals(errorDetails.resultCode, "ENTRY ALREADY EXISTS");
    }
}

@test:Config {
    dependsOn: [testAddAlreadyExistingUser]
}
public function testUpdateUser() returns error? {
    record {} user = {
        "sn": "User",
        "givenName": "Updated User",
        "displayName": "Updated User",
        "manager": "CN=New User,OU=People,DC=ad,DC=windows"
    };
    LdapResponse val = check ldapClient->modify(userDN, user);
    test:assertEquals(val.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testUpdateUserWithNullValues]
}
public function testGetUser() returns error? {
    UserConfig value = check ldapClient->getEntry(userDN);
    test:assertEquals(value?.givenName, "Updated User");
}

@test:Config {}
public function testInvalidClient() returns error? {
    Client|Error ldapClient = new ({
        hostName: "111.111.11.111",
        port: port,
        domainName: domainName,
        password: password
    });
    test:assertTrue(ldapClient is Error);
}

@test:Config {}
public function testInvalidDomainInClient() {
    Client|Error ldapClient = new ({
        hostName: hostName,
        port: port,
        domainName: "invalid@ad.invalid",
        password: password
    });
    test:assertTrue(ldapClient is Error);
}

@test:Config {}
public function testGetInvalidUser() returns error? {
    UserConfig|Error value = ldapClient->getEntry("CN=Invalid User,OU=People,DC=ad,DC=windows");
    test:assertTrue(value is Error);
}

@test:Config {
    dependsOn: [testUpdateUser]
}
public function testUpdateUserWithNullValues() returns error? {
    record {} user = {
        "employeeID":"30896",
        "givenName": "Updated User",
        "sn": "User",
        "company":"Grocery Co. USA",
        "co":null,
        "streetAddress":null,
        "mobile":null,
        "displayName": "Updated User",
        "middleName":null,
        "mail":null,
        "l":null,
        "telephoneNumber":null,
        "department":"Produce",
        "st":null,
        "title":"Clerk",
        "manager": "CN=New User,OU=People,DC=ad,DC=windows"
    };
    LdapResponse val = check ldapClient->modify(userDN, user);
    test:assertEquals(val.resultCode, SUCCESS);
}
