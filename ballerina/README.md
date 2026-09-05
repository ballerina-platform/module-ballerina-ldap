## Overview

This module provides the capability to connect, authenticate, and interact with LDAP (Lightweight Directory Access Protocol) directory servers, supporting authentication, authorization, and directory-based operations such as searching, adding, and modifying entries.

## Key Features

- Connect and authenticate to LDAP directory servers
- Add, modify, and delete directory entries
- Search directory entries with configurable search scope
- Compare attribute values and rename entries via `modifyDN`

## Client

The `ldap:Client` connects to a directory server and performs various operations on directories. Currently, it supports the generic LDAP operations; `add`, `modify`, `modifyDN`, `compare`, `search`, `searchWithType`, `delete`, and `close`.

### Instantiate a new LDAP client

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:Client ldapClient = check new ({
        hostName,
        port,
        domainName,
        password
    });
}
```

### Remote methods in `ldap:Client`

- **add**: Creates an entry in a directory server.
- **modify**: Updates information of an entry in a directory server.
- **modifyDN**: Renames an entry in a directory server.
- **compare**: Determines whether a given entry has a specified attribute value.
- **search**: Returns a record containing search result entries and references that match the given search parameters.
- **searchWithType**: Returns a list of entries that match the given search parameters.
- **delete**: Removes an entry from a directory server.
- **close**: Unbinds from the server and closes the LDAP connection.

#### Add a new entry in the directory server

Creates an entry in a directory server.

```ballerina
anydata user = {
    "objectClass": "user",
    "sn": "New User",
    "cn": "New User",
    "givenName": "New User",
    "displayName": "New User",
    "userPrincipalName": "newuser@ad.windows",
    "userAccountControl": "544"
};
ldap:LdapResponse addResult = check ldapClient->add("DC=ldap,DC=com", user);
```

#### Search for an entry in the directory server

Returns a record containing search result entries and references that match the given search parameters.

```ballerina
ldap:SearchResult searchResult = check ldapClient->search("DC=ldap,DC=com", "(givenName=Test User1)", ldap:SUB);
```

#### Modify a new entry in the directory server

Updates information of an entry.

```ballerina
anydata user = {
    "sn": "User",
    "givenName": "Updated User",
    "displayName": "Updated User"
};
ldap:LdapResponse modifyResult = check ldapClient->modify("DC=ldap,DC=com", user);
```

#### Delete an entry in the directory server

Removes an entry from a directory server.

```ballerina
ldap:LdapResponse deleteResult = check ldapClient->delete("DC=ldap,DC=com");
```

## Examples

The Ballerina Ldap library provides practical examples illustrating usage in various scenarios. Explore these [examples](https://github.com/ballerina-platform/module-ballerina-ldap/tree/master/examples).

1. [Access directory server](https://github.com/ballerina-platform/module-ballerina-ldap/tree/master/examples/access-directory-server)
    This example shows how to integrate with a directory server to manage employees in a corporation.

2. [Manage entries in a library](https://github.com/ballerina-platform/module-ballerina-ldap/tree/master/examples/library-managment-system)
    This example demonstrates how to integrate with a directory server for managing users and books in a library.
