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

final Client ldap = check new ({
    hostName,
    port,
    domainName,
    password
});

function validateClient(Client ldapClient) returns Client|error {
    boolean isConnected = ldapClient->isConnected();
    return isConnected ? ldapClient : new ({
            hostName,
            port,
            domainName,
            password
        });
}

@test:Config {}
public function testAddUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    record {|AttributeType...;|} user = {
        "objectClass": ["top", "person"],
        "sn": "User",
        "cn": "User"
    };
    LdapResponse addResponse = check ldapClient->add("cn=User,dc=mycompany,dc=com", user);
    test:assertEquals(addResponse.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testAddUser]
}
public function testAddSecondaryUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    record {|AttributeType...;|} user = {
        "objectClass": ["top", "person"],
        "sn": "New User",
        "cn": "New User"
    };
    LdapResponse addResult = check ldapClient->add("CN=New User,dc=mycompany,dc=com", user);
    test:assertEquals(addResult.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testGetUser]
}
public function testDeleteUserHavingManager() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->delete("CN=New User,dc=mycompany,dc=com");
    test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testDeleteUserHavingManager]
}
public function testDeleteUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->delete("CN=User,dc=mycompany,dc=com");
    test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testAddSecondaryUser]
}
public function testAddAlreadyExistingUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    Entry user = {
        "objectClass": ["top", "person"],
        "sn": "New User",
        "cn": "New User"
    };
    LdapResponse|Error response = ldapClient->add("CN=New User,dc=mycompany,dc=com", user);
    test:assertTrue(response is Error);
    if response is Error {
        ErrorDetails errorDetails = response.detail();
        test:assertEquals(errorDetails.resultCode, "ENTRY ALREADY EXISTS");
    }
}

@test:Config {
    dependsOn: [testAddAlreadyExistingUser]
}
public function testUpdateUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    record {|AttributeType...;|} user = {
        "sn": "Updated User"
    };
    LdapResponse response = check ldapClient->modify(userDN, user);
    test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {
    dependsOn: [testUpdateUserWithNullValues]
}
public function testGetUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    UserConfig value = check ldapClient->getEntry(userDN);
    test:assertEquals(value?.sn, "Updated User");
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
    Client ldapClient = check validateClient(ldap);
    UserConfig|Error value = ldapClient->getEntry("CN=Invalid User,dc=mycompany,dc=com");
    test:assertTrue(value is Error);
}

@test:Config {
    dependsOn: [testUpdateUser]
}
public function testUpdateUserWithNullValues() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->modify(userDN, updateUser);
    test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {}
public function testAddUserWithNullValues() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testCompareAttributeValues() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    boolean compare = check ldapClient->compare("CN=Test User1,dc=mycompany,dc=com", "sn", "Timothy");
    test:assertEquals(compare, true);

    LdapResponse modifyDN = check ldapClient->modifyDn("CN=Test User1,dc=mycompany,dc=com", "CN=Test User2", true);
    test:assertEquals(modifyDN.resultCode, SUCCESS);

    LdapResponse delete = check ldapClient->delete("CN=Test User2,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithType() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    UserConfig[] value = check ldapClient->searchWithType("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
    test:assertEquals(value.length(), 1);
    test:assertEquals(value[0].objectClass, ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    SearchResult value = check ldapClient->search("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
    test:assertEquals(value.resultCode, SUCCESS);
    test:assertEquals((<Entry[]>value.entries).length(), 1);
    test:assertEquals((<Entry[]>value.entries)[0]["objectClass"], ["person", "top"]);
    test:assertTrue(value?.searchReferences is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchNonExistingUsers() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    SearchResult|Error value = ldapClient->search("dc=mycompany,dc=com", "(givenName=Non existent)", SUB);
    test:assertTrue(value is Error);
    if value is Error {
        ErrorDetails errorDetails = value.detail();
        test:assertEquals(errorDetails.resultCode, OTHER);
    }

    LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
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
    LdapResponse|Error response = ldapClient1->add("CN=Test User12,dc=mycompany,dc=com", user);
    test:assertTrue(response is Error);
    if response is Error {
        ErrorDetails errorDetails = response.detail();
        test:assertEquals(errorDetails.resultCode, OTHER);
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
    LdapResponse|Error response = ldapClient1->modify(userDN, updateUser);
    test:assertTrue(response is Error);
    if response is Error {
        ErrorDetails errorDetails = response.detail();
        test:assertEquals(errorDetails.resultCode, OTHER);
    }
}

@test:Config {}
public function testModifyDN() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    LdapResponse modifyDN = check ldapClient->modifyDn("CN=Test User1,dc=mycompany,dc=com", "CN=Test User2", true);
    test:assertEquals(modifyDN.resultCode, SUCCESS);

    LdapResponse delete = check ldapClient->delete("CN=Test User2,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testModifyDnInNonExistingUser() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse|Error modifyDN = ldapClient->modifyDn("CN=Non Existing User,dc=mycompany,dc=com", "CN=Test User2", true);
    test:assertTrue(modifyDN is Error);
    if modifyDN is Error {
        ErrorDetails errorDetails = modifyDN.detail();
        test:assertEquals(errorDetails.resultCode, NO_SUCH_OBJECT);
    }
}

@test:Config {}
public function testSearchWithInvalidType() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    record {|string id;|}[]|Error value = ldapClient->searchWithType("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
    test:assertTrue(value is Error);
    test:assertEquals((<Error>value).message(), "{ballerina}ConversionError");
    LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testTlsConnection() returns error? {
    ClientSecureSocket clientSecureSocket = {
        cert: "tests/resources/server/certs/server.crt",
        enable: true
    };

    Client ldapClient = check new ({
        port: 636,
        hostName,
        password,
        domainName,
        clientSecureSocket
    });

    boolean isConnected = ldapClient->isConnected();
    test:assertTrue(isConnected);
}

@test:Config {}
public function testTlsConnectionWithInvalidCert() returns error? {
    ClientSecureSocket clientSecureSocket = {
        cert: "tests/resources/server/certs/invalid.crt",
        enable: true
    };

    Client|Error ldapClient = new ({
        port: 636,
        hostName,
        password,
        domainName,
        clientSecureSocket
    });

    test:assertTrue(ldapClient is Error);
}

@test:Config {}
public function testTlsConnectionWithTrustStore() returns error? {
    ClientSecureSocket clientSecureSocket = {
        cert: {
            path: "tests/resources/server/certs/truststore.p12",
            password: "password"
        }
    };

    Client ldapClient = check new ({
        port: 636,
        hostName,
        password,
        domainName,
        clientSecureSocket
    });

    boolean isConnected = ldapClient->isConnected();
    test:assertTrue(isConnected);
}

@test:Config {}
public function testGetEntryWithSelectiveAttributes() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User3,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry with selective attributes (only sn and cn)
    UserConfig value = check ldapClient->getEntry("CN=Test User3,dc=mycompany,dc=com", ["sn", "cn"]);
    test:assertEquals(value?.sn, "Timothy");
    test:assertEquals(value?.cn, "Test User3");
    // objectClass should be nil as it was not requested
    test:assertTrue(value?.objectClass is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User3,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testGetEntryWithAllAttributes() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User4,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry with all attributes (passing empty array or nil)
    UserConfig value = check ldapClient->getEntry("CN=Test User4,dc=mycompany,dc=com");
    test:assertEquals(value?.sn, "Timothy");
    test:assertEquals(value?.cn, "Test User4");
    test:assertEquals(value?.objectClass, ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User4,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testGetEntryWithSingleAttribute() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User5,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry with only one attribute
    UserConfig value = check ldapClient->getEntry("CN=Test User5,dc=mycompany,dc=com", ["sn"]);
    test:assertEquals(value?.sn, "Timothy");
    // Other attributes should be nil
    test:assertTrue(value?.cn is ());
    test:assertTrue(value?.objectClass is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User5,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithTypeAutoAttributeExtraction() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User6,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Validates that searchWithType automatically extracts and retrieves attributes from UserConfig type
    record{|string sn; string cn;|}[] value = check ldapClient->searchWithType("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
    test:assertTrue(value.length() > 0);
    test:assertEquals(value[0].sn, "Timothy");
    test:assertEquals(value[0].cn, "Test User6");

    LdapResponse delete = check ldapClient->delete("CN=Test User6,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithTypeValidateAllFields() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User7,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Validates that all fields defined in UserConfig type are correctly populated by automatic attribute extraction
    UserConfig[] value = check ldapClient->searchWithType("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
    test:assertTrue(value.length() > 0);
    test:assertEquals(value[0].sn, "Timothy");
    test:assertEquals(value[0].objectClass, ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User7,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithSelectiveAttributes() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User8,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Search with selective attributes
    SearchResult value = check ldapClient->search("dc=mycompany,dc=com", "(sn=Timothy)", SUB, ["sn", "cn"]);
    test:assertEquals(value.resultCode, SUCCESS);
    test:assertTrue((<Entry[]>value.entries).length() > 0);
    Entry firstEntry = (<Entry[]>value.entries)[0];
    test:assertTrue(firstEntry["sn"] is string);
    test:assertTrue(firstEntry["cn"] is string);
    // objectClass should not be present as it was not requested
    test:assertTrue(firstEntry["objectClass"] is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User8,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithAllAttributes() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User9,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Search with all attributes
    SearchResult value = check ldapClient->search("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
    test:assertEquals(value.resultCode, SUCCESS);
    test:assertTrue((<Entry[]>value.entries).length() > 0);
    Entry firstEntry = (<Entry[]>value.entries)[0];
    test:assertEquals(firstEntry["objectClass"], ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User9,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testGetEntryWithInvalidAttribute() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User10,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry with invalid attribute name - LDAP should not return an error, just won't include that attribute
    UserConfig value = check ldapClient->getEntry("CN=Test User10,dc=mycompany,dc=com", ["sn", "invalidAttribute"]);
    test:assertEquals(value?.sn, "Timothy");
    test:assertTrue(value?.cn is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User10,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testGetEntryWithTypeIntrospection() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User11,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry without explicit attributes - should use type introspection to determine attributes
    record {| string sn; string cn; string[] objectClass;|} value = check ldapClient->getEntry("CN=Test User11,dc=mycompany,dc=com");
    test:assertEquals(value.sn, "Timothy");
    test:assertEquals(value.cn, "Test User11");
    test:assertEquals(value.objectClass, ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User11,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithTypeExplicitAttributes() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User12,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Search with explicit attributes parameter in searchWithType - should override type-inferred attributes
    UserConfig[] value = check ldapClient->searchWithType("dc=mycompany,dc=com", "(cn=Test User12)", SUB, ["sn", "cn"]);
    test:assertEquals(value.length(), 1);
    test:assertEquals(value[0].sn, "Timothy");
    test:assertEquals(value[0].cn, "Test User12");
    // objectClass should be nil as it was not in the explicit attributes list
    test:assertTrue(value[0].objectClass is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User12,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testGetEntryWithRecordHavingRestField() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User13,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry with a record type that has a rest field - should retrieve all attributes
    Entry value = check ldapClient->getEntry("CN=Test User13,dc=mycompany,dc=com");
    test:assertEquals(value["sn"], "Timothy");
    test:assertEquals(value["cn"], "Test User13");
    test:assertEquals(value["objectClass"], ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User13,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testSearchWithEmptyAttributesArray() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User14,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Search with empty array should retrieve all attributes
    SearchResult value = check ldapClient->search("dc=mycompany,dc=com", "(cn=Test User14)", SUB, []);
    test:assertEquals(value.resultCode, SUCCESS);
    test:assertTrue((<Entry[]>value.entries).length() > 0);
    Entry firstEntry = (<Entry[]>value.entries)[0];
    test:assertEquals(firstEntry["objectClass"], ["person", "top"]);

    LdapResponse delete = check ldapClient->delete("CN=Test User14,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testGetEntryExplicitAttributesOverrideType() returns error? {
    Client ldapClient = check validateClient(ldap);
    LdapResponse response = check ldapClient->add("CN=Test User15,dc=mycompany,dc=com", user);
    test:assertEquals(response.resultCode, SUCCESS);

    // Get entry with explicit attributes that differ from UserConfig type fields
    // Should use explicit attributes and ignore type-inferred attributes
    UserConfig value = check ldapClient->getEntry("CN=Test User15,dc=mycompany,dc=com", ["cn"]);
    test:assertEquals(value?.cn, "Test User15");
    // sn and objectClass should be nil as they were not in the explicit attributes list
    test:assertTrue(value?.sn is ());
    test:assertTrue(value?.objectClass is ());

    LdapResponse delete = check ldapClient->delete("CN=Test User15,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testMultiValueAttributeWithNonAscii() returns error? {
    Client ldapClient = check validateClient(ldap);

    Entry userWithNonAscii = {
        "objectClass": ["top", "person"],
        "sn": "Müller",
        "cn": "Test User Non-ASCII",
        "description": ["日本語テスト", "Ελληνικά", "中文测试"]
    };
    LdapResponse addResponse = check ldapClient->add("CN=Test User Non-ASCII,dc=mycompany,dc=com", userWithNonAscii);
    test:assertEquals(addResponse.resultCode, SUCCESS);

    Entry value = check ldapClient->getEntry("CN=Test User Non-ASCII,dc=mycompany,dc=com");
    anydata description = value["description"];
    if description !is string[] {
        test:assertFail("Expected description to be of type string[]");
    }

    // Expected descriptions are Base64 encoded values
    string[] expectedDescriptions = ["5pel5pys6Kqe44OG44K544OI", "zpXOu867zrfOvc65zrrOrA==", "5Lit5paH5rWL6K+V"];
    test:assertEquals(description, expectedDescriptions, "Multi-value attribute with non-ASCII characters did not match expected values");
    LdapResponse delete = check ldapClient->delete("CN=Test User Non-ASCII,dc=mycompany,dc=com");
    test:assertEquals(delete.resultCode, SUCCESS);
}
