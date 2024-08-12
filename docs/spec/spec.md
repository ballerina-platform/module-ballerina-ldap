# Specification: Ballerina LDAP Library

_Authors_: @Nuvindu \
_Reviewers_:  \
_Created_: 2024/08/11 \
_Updated_: 2024/08/11 \
_Edition_: Swan Lake

## Introduction

LDAP (Lightweight Directory Access Protocol) is a vendor-neutral software protocol for accessing and maintaining distributed directory information services. It allows users to locate organizations, individuals, and other resources such as files and devices in a network. LDAP is used in various applications for directory-based authentication and authorization.

The Ballerina LDAP module provides the capability to efficiently connect, authenticate, and interact with directory servers. It allows users to perform operations such as searching for entries, and modifying entries in an LDAP directory, providing better support for directory-based operations.

If you have any feedback or suggestions about the library, start a discussion via a [GitHub issue](https://github.com/ballerina-platform/ballerina-library/issues) or in the [Discord server](https://discord.gg/ballerinalang). Based on the outcome of the discussion, the specification and implementation can be updated. Community feedback is always welcome. Any accepted proposal, which affects the specification is stored under `/docs/proposals`. Proposals under discussion can be found with the label `type/proposal` in GitHub.

The conforming implementation of the specification is released and included in the distribution. Any deviation from the specification is considered a bug.

## Contents

1. [Overview](#1-overview)
2. [LDAP Client](#2-ldap-client)
    * 2.1 [The `init` method](#21-the-init-method)
3. [Operation Types](#3-operation-types)
    * 3.1 [Add operation](#31-add-operation)
    * 3.2 [Modify operation](#32-modify-operation)
    * 3.3 [ModifyDN operation](#33-modifydn-operation)
    * 3.4 [Compare operation](#34-compare-operation)
    * 3.5 [Search operation](#35-search-operation)
    * 3.6 [Search with type operation](#36-search-with-type-operation)
    * 3.7 [Delete operation](#37-delete-operation)
    * 3.8 [Close operation](#38-close-operation)
4. [The `ldap:Error` type](#4-the-ldaperror-type)

## 1. Overview

The Ballerina LDAP module provides support for interacting with directory servers which support LDAP protocol. This module provided various directory operations by establishing a connection to an LDAP server, allowing for tasks such as adding, modifying, deleting, and searching directory entries. It focus on ease of use and integration, making it straightforward for developers to perform directory-based operations within their Ballerina applications.

## 2. LDAP client

The `ldap:Client` instance needs to be initialized before performing the functionalities. When initializing it connects to a directory server and performs various operations on directories. Currently, it supports the generic LDAP operations; `add`, `modify`, `modifyDN`, `compare`, `search`, `searchWithType`, `delete`, and `close`.

### 2.1 The `init` method

The `init` method initializes the `ldap:Client` instance using the parameters `hostName`, `port`, `domainName`, and `password`. The `hostName` and `port` parameters are used to bind the request and authenticate clients with the directory server, while the `domainName` and `password` parameters establish the connection to the server for performing LDAP operations. In case of failure, the method returns an `avro:Error`."

```ballerina
ldap:Client ldapClient = check new ({
   hostName,
   port,
   domainName,
   password
});
```

## 3. Operation types

The main operation types in LDAP are listed here.

### 3.1 Add operation

Creates an entry in a directory server.

```ballerina
anydata user = {
    "objectClass": "user",
    "sn": "New User",
    "cn": "New User",
    "givenName": "New User",
    "displayName": "New User",
    "userPrincipalName": "newuser@example.com",
    "userAccountControl": "544"
};
ldap:LdapResponse addResult = check ldapClient->add(userDN, user);
```

### 3.2 Modify operation

Updates information of an entry in a directory server.

```ballerina
anydata user = {
    "sn": "User",
    "givenName": "Updated User",
    "displayName": "Updated User"
};
ldap:LdapResponse modifyResult = check ldapClient->modify(userDN, user);
```

### 3.3 ModifyDN operation

Renames an entry in a directory server.

```ballerina
ldap:LdapResponse modifyResult = check ldapClient->modifyDN(userDN, "CN=Test User2", true);
```

### 3.4 Compare operation

Determines whether a given entry has a specified attribute value.

```ballerina
ldap:LdapResponse compareResult = check ldapClient->compare(userDN, "givenName", "Test User1");
```

### 3.5 Search operation

Returns a record containing search result entries and references that match the given search parameters.

```ballerina
ldap:SearchResult searchResult = check ldapClient->search("DC=ad,DC=windows", "(givenName=Test User1)", ldap:SUB);
```

### 3.6 Search with type operation

Returns a list of entries that match the given search parameters.

```ballerina
anydata[] searchResult = check ldapClient->searchWithType("DC=ad,DC=com", "(givenName=Test User1)", ldap:SUB);
```

### 3.7 Delete operation

Removes an entry from a directory server.

```ballerina
ldap:LdapResponse deleteResult = check ldapClient->delete(userDN);
```

### 3.8 Close operation

Unbinds from the server and closes the LDAP connection.

```ballerina
ldapClient->close();
```

## 4. The `ldap:Error` type

The `ldap:Error` type represents all the errors related to the LDAP module. This is a subtype of the Ballerina `error` type.
