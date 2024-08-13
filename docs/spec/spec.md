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
4. [The `ldap:LdapResponse` type](#4-the-ldapldapresponse-type)
5. [The `ldap:Error` type](#5-the-ldaperror-type)

## 1. Overview

The Ballerina LDAP module provides support for interacting with directory servers which support LDAP protocol. This module provided various directory operations by establishing a connection to an LDAP server, allowing for tasks such as adding, modifying, deleting, and searching directory entries. It focus on ease of use and integration, making it straightforward for developers to perform directory-based operations within their Ballerina applications.

## 2. LDAP client

The `ldap:Client` instance needs to be initialized before performing the functionalities. When initializing it connects to a directory server and performs various operations on directories. Currently, it supports the generic LDAP operations; `add`, `modify`, `modifyDN`, `compare`, `search`, `searchWithType`, `delete`, and `close`.

### 2.1 The `init` method

The `init` method initializes the `ldap:Client` instance using the parameters `hostName`, `port`, `domainName`, and `password`. The `hostName` and `port` parameters are used to bind the request and authenticate clients with the directory server, while the `domainName` and `password` parameters establish the connection to the server for performing LDAP operations. In case of failure, the method returns an `avro:Error`."

```ballerina
ldap:Client ldap = check new ({
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
    "cn": "New User"
};
ldap:LdapResponse addResult = check ldap->add(userDn, user);
```

#### 3.1.1 DNs and RDNs

The distinguished name (`DN`) of an entry is used to uniquely identify the entry and its location within the directory information tree (`DIT`) hierarchy. It's similar to how a file path specifies the location of a file in a filesystem.

A `DN` consists of one or more comma-separated components known as relative distinguished names (`RDN`s). Typically, the leftmost component in the `DN` is considered the `RDN` for that entry. [Learn more](https://ldap.com/ldap-dns-and-rdns/)

### 3.2 Modify operation

Updates information of an entry in a directory server.

```ballerina
anydata user = {
    "sn": "user",
    "givenName": "updated user",
    "displayName": "updated user"
};
ldap:LdapResponse modifyResult = check ldap->modify(dN, user);
```

### 3.3 ModifyDn operation

Renames an entry in a directory server.

```ballerina
ldap:LdapResponse modifyResult = check ldap->modifyDn(dN, "CN=user1", true);
```

### 3.4 Compare operation

Determines whether a given entry has a specified attribute value.

```ballerina
ldap:LdapResponse compareResult = check ldap->compare(dN, "givenName", "user1");
```

### 3.5 Search operation

Returns a record containing search result entries and references that match the given search parameters.

```ballerina
ldap:SearchResult searchResult = check ldap->search("dc=example,dc=com", "(givenName=user1)", ldap:SUB);
```

### 3.6 Search with type operation

Returns a list of entries that match the given search parameters.

```ballerina
anydata[] searchResult = check ldap->searchWithType("dc=example,dc=com", "(givenName=user1)", ldap:SUB);
```

### 3.6.1 Search Scope

The search scope defines the part of the target subtree that should be included in the search.

**BASE** : Indicates that only the entry specified by the base DN should be considered

**ONE** : Indicates that only entries that are immediate subordinates of the entry specified by the base DN (but not the base entry itself) should be considered

**SUB** : Indicates that the base entry itself and any subordinate entries (to any depth) should be considered

**SUBORDINATE_SUBTREE** : Indicates that any subordinate entries (to any depth) below the entry specified by the base DN should be considered, but the base entry itself should not be considered, as described in draft-sermersheim-ldap-subordinate-scope.

### 3.6.2 Search Filter

Filters are essential for specifying the criteria used to locate entries in search requests. [Learn more](https://ldap.com/ldap-filters/).

### 3.7 Delete operation

Removes an entry from a directory server.

```ballerina
ldap:LdapResponse deleteResult = check ldap->delete(userDN);
```

### 3.8 Close operation

Unbinds from the server and closes the LDAP connection.

```ballerina
ldapClient->close();
```

## 4. The `ldap:LdapResponse` type

The `ldap:LdapResponse` type defines a data structure used to encapsulate common elements found in most LDAP responses.

**Result Code**: An integer indicating the status of the operation.

**Diagnostic Message**: This can provide extra details about the operation, such as reasons for any failure. This field is often missing in successful operations and may or may not be present in failed ones.

**Matched DN**: An optional DN that denotes the entry most closely matching the DN of a non-existent entry. For example, if an operation fails due to a missing entry, this field may specify the DN of the closest existing ancestor.

**Operation Type**: Indicates the type of the LDAP operation

**Referral URLs**: An optional collection of LDAP URLs that direct to other directories or locations within the DIT where the operation might be carried out. All provided URLs should be treated as equally valid for performing the operation.

## 5. The `ldap:Error` type

The `ldap:Error` type represents all the errors related to the LDAP module. This is a subtype of the Ballerina `error` type.
