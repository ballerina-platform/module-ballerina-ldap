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

type UserConfig record {
    *Person;
    string userPrincipalName?;
    string givenName?;
    string company?;
    string co?;
    string streetAddress?;
    string mobile?;
    string displayName?;
    string middleName?;
    string employeeId?;
    string extensionAttribute11?;
    string extensionAttribute10?;
    string postalCode?;
    string mail?;
    string l?;
    string telephoneNumber?;
    string department?;
    string st?;
    string title?;
    string distinguishedName?;
    string manager?;
    string userAccountControl?;
};

record {|EntryMember...;|} user = {
    "employeeID": "56111",
    "userPrincipalName": "testuser1@ad.windows",
    "givenName": "Test User1",
    "sn": "Timothy",
    "displayName": "Test User1",
    "mail": "testuser1@hotmail.com",
    "objectClass": ["top","person","organizationalPerson","user"],
    "userAccountControl": "544"
};

record {|EntryMember...;|} updateUser = {
    "objectClass": ["user", organizationalPerson, "person", "top"],
    "employeeID":"30896",
    "givenName": "Updated User",
    "sn": "User",
    "company":"Grocery Co. USA",
    "displayName": "Updated User",
    "department":"Produce",
    "title":"Clerk",
    "manager": "CN=New User,OU=People,DC=ad,DC=windows"
};
