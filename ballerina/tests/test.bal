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

@test:Config {}
public function testAddUser() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse response = check ldapClient->delete("CN=New User,dc=mycompany,dc=com");
   test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {
   dependsOn: [testDeleteUserHavingManager]
}
public function testDeleteUser() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse response = check ldapClient->delete("CN=User,dc=mycompany,dc=com");
   test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {
   dependsOn: [testAddSecondaryUser]
}
public function testAddAlreadyExistingUser() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   UserConfig|Error value = ldapClient->getEntry("CN=Invalid User,dc=mycompany,dc=com");
   test:assertTrue(value is Error);
}

@test:Config {
   dependsOn: [testUpdateUser]
}
public function testUpdateUserWithNullValues() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse response = check ldapClient->modify(userDN, updateUser);
   test:assertEquals(response.resultCode, SUCCESS);
}

@test:Config {}
public function testAddUserWithNullValues() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
   test:assertEquals(response.resultCode, SUCCESS);

   LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
   test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testCompareAttributeValues() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
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
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
   test:assertEquals(response.resultCode, SUCCESS);

   LdapResponse modifyDN = check ldapClient->modifyDn("CN=Test User1,dc=mycompany,dc=com", "CN=Test User2", true);
   test:assertEquals(modifyDN.resultCode, SUCCESS);

   LdapResponse delete = check ldapClient->delete("CN=Test User2,dc=mycompany,dc=com");
   test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config {}
public function testModifyDnInNonExistingUser() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse|Error modifyDN = ldapClient->modifyDn("CN=Non Existing User,dc=mycompany,dc=com", "CN=Test User2", true);
   test:assertTrue(modifyDN is Error);
   if modifyDN is Error {
       ErrorDetails errorDetails = modifyDN.detail();
       test:assertEquals(errorDetails.resultCode, NO_SUCH_OBJECT);
   }
}

@test:Config {}
public function testSearchWithInvalidType() returns error? {
   Client ldapClient = check new ({
      hostName,
      port,
      domainName,
      password
   });
   LdapResponse response = check ldapClient->add("CN=Test User1,dc=mycompany,dc=com", user);
   test:assertEquals(response.resultCode, SUCCESS);

   record {|string id;|}[]|Error value = ldapClient->searchWithType("dc=mycompany,dc=com", "(sn=Timothy)", SUB);
   test:assertTrue(value is Error);
   test:assertEquals((<Error>value).message(), "{ballerina}ConversionError");
   LdapResponse delete = check ldapClient->delete("CN=Test User1,dc=mycompany,dc=com");
   test:assertEquals(delete.resultCode, SUCCESS);
}

@test:Config{}
public function testTlsConnection() returns error? {
   ClientSecureSocket clientSecureSocket = {
      cert: "tests/resources/server/certs/server.crt",
      enable: true
   };

   Client ldapClient =  check new ({
      port: 636,
      hostName,
      password,
      domainName,
      clientSecureSocket}
   );

   boolean isConnected = ldapClient->isConnected();
   test:assertTrue(isConnected);
}

@test:Config{}
public function testTlsConnectionWithInvalidCert() returns error? {
   ClientSecureSocket clientSecureSocket = {
      cert: "tests/resources/server/certs/invalid.crt",
      enable: true
   };

   Client|Error ldapClient =  new ({
      port: 636,
      hostName,
      password,
      domainName,
      clientSecureSocket}
   );

   test:assertTrue(ldapClient is Error);
}

@test:Config{}
public function testTlsConnectionWithTrustStore() returns error? {
   ClientSecureSocket clientSecureSocket = {
         cert: {
               path: "tests/resources/server/certs/truststore.p12",
               password: "password"
         }
   };

   Client ldapClient =  check new ({
      port: 636,
      hostName,
      password,
      domainName,
      clientSecureSocket}
   );

   boolean isConnected = ldapClient->isConnected();
   test:assertTrue(isConnected);
}
