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

@test:Config {}
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
    LdapResponse val = check ldapClient->modify(userDN, updateUser);
    test:assertEquals(val.resultCode, SUCCESS);
}

@test:Config {}
public function testAddUserWithNullValues() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    LdapResponse delete = check ldapClient->delete("CN=Test User1,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testCompareAttributeValues() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    LdapResponse compare = check ldapClient->compare("CN=Test User1,OU=People,DC=ad,DC=windows", "givenName", "Test User1");
    test:assertEquals(compare.resultCode, COMPARE_TRUE);

    LdapResponse modifyDN = check ldapClient->modifyDN("CN=Test User1,OU=People,DC=ad,DC=windows", "CN=Test User2", true);
    test:assertEquals(modifyDN.resultCode, SUCCESS);

    LdapResponse delete = check ldapClient->delete("CN=Test User2,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithType() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    UserConfig[] value = check ldapClient->searchWithType("DC=ad,DC=windows", "(givenName=Test User1)", SUB);
    test:assertEquals(value.length(), 1);
    test:assertEquals(value[0].objectClass, ["top","person","organizationalPerson","user"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User1,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchUser() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    SearchResult value = check ldapClient->search("DC=ad,DC=windows", "(givenName=Test User1)", SUB);
    test:assertEquals(value.resultCode, SUCCESS);
    test:assertEquals((<Entry[]>value.entries).length(), 1);
    test:assertEquals((<Entry[]>value.entries)[0]["objectClass"], ["top","person","organizationalPerson","user"]);
    test:assertTrue((<SearchReference[]>value.searchReferences).length() > 0);

    LdapResponse delete = check ldapClient->delete("CN=Test User1,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {
}
public function testSearchNonExistingUsers() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    SearchResult|Error value = ldapClient->search("DC=ad,DC=windows", "(givenName=Non existent)", SUB);
    test:assertTrue(value is Error);
    if value is Error {
        ErrorDetails errorDetails = value.detail();
        test:assertEquals(errorDetails.resultCode, OTHER);
    }

    LdapResponse delete = check ldapClient->delete("CN=Test User1,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testAddingUserWithClosedClient() returns error? {
    Client ldapClient1 = check new ({
        hostName: hostName,
        port: port,
        domainName: domainName,
        password: password
    });
    ldapClient1->close();
    boolean isConnected = ldapClient1->isConnected();
    test:assertTrue(!isConnected);
    LdapResponse|Error val = ldapClient1->add("CN=Test User12,OU=People,DC=ad,DC=windows", user);
    test:assertTrue(val is Error);
    if val is Error {
        ErrorDetails errorDetails = val.detail();
        test:assertEquals(errorDetails.resultCode, OTHER);
        test:assertEquals(errorDetails.message, "LDAP Connection has been closed");
    }
}

@test:Config {}
public function testModifyingUserWithClosedClient() returns error? {
    Client ldapClient1 = check new ({
        hostName: hostName,
        port: port,
        domainName: domainName,
        password: password
    });
    ldapClient1->close();
    boolean isConnected = ldapClient1->isConnected();
    test:assertTrue(!isConnected);
    LdapResponse|Error val = ldapClient1->modify(userDN, updateUser);
    test:assertTrue(val is Error);
    if val is Error {
        ErrorDetails errorDetails = val.detail();
        test:assertEquals(errorDetails.resultCode, OTHER);
        test:assertEquals(errorDetails.message, "LDAP Connection has been closed");
    }
}

@test:Config {}
public function testModifyDN() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    LdapResponse modifyDN = check ldapClient->modifyDN("CN=Test User1,OU=People,DC=ad,DC=windows", "CN=Test User2", true);
    test:assertEquals(modifyDN.resultCode, SUCCESS);

    LdapResponse delete = check ldapClient->delete("CN=Test User2,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testModifyDnInNonExistingUser() {
    LdapResponse|Error modifyDN = ldapClient->modifyDN("CN=Non Existing User,OU=People,DC=ad,DC=windows", "CN=Test User2", true);
    test:assertTrue(modifyDN is Error);
    if modifyDN is Error {
        ErrorDetails errorDetails = modifyDN.detail();
        test:assertEquals(errorDetails.resultCode, NO_SUCH_OBJECT);
    }
}

@test:Config {}
public function testSearchWithInvalidType() returns error? {
    LdapResponse val = check ldapClient->add("CN=Test User1,OU=People,DC=ad,DC=windows", user);
    test:assertEquals(val.resultCode, SUCCESS);

    record {|string id;|}[]|Error value = ldapClient->searchWithType("DC=ad,DC=windows", "(givenName=Test User1)", SUB);
    test:assertTrue(value is Error);
    test:assertEquals((<Error>value).message(), "{ballerina}ConversionError");
    LdapResponse delete = check ldapClient->delete("CN=Test User1,OU=People,DC=ad,DC=windows");
    test:assertEquals(delete.resultCode, SUCCESS);
}
