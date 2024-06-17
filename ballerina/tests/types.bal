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

record {} user = {
    "employeeID": "56111",
    "userPrincipalName": "testuser1@ad.windows",
    "givenName": "Test User1",
    "middleName": null,
    "sn": "Timothy",
    "displayName": "Test User1",
    "mobile": null,
    "telephoneNumber": null,
    "mail": "testuser1@hotmail.com",
    "department": null,
    "company": null,
    "streetAddress": null,
    "co": null,
    "st": null,
    "l": null,
    "objectClass": ["top","person","organizationalPerson","user"],
    "userAccountControl": "544"
};

record {} updateUser = {
    "objectClass": ["user", organizationalPerson, "person", "top"],
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
