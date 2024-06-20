## Overview

LDAP (Lightweight Directory Access Protocol) is a vendor-neutral software protocol for accessing and maintaining distributed directory information services. It allows users to locate organizations, individuals, and other resources such as files and devices in a network. LDAP is used in various applications for directory-based authentication and authorization.

The Ballerina LDAP module provides the capability to efficiently connect, authenticate, and interact with directory servers. It allows users to perform operations such as searching for entries, and modifying entries in an LDAP directory, providing better support for directory-based operations.

### APIs associated with LDAP

- **add**: Creates an entry in a directory server.
- **modify**: Updates information of an entry in a directory server.
- **modifyDN**: Renames an entry in a directory server.
- **compare**: Determines whether a given entry has a specified attribute value.
- **search**: Returns a record containing search result entries and references that match the given search parameters.
- **searchWithType**: Returns a list of entries that match the given search parameters.
- **delete**: Removes an entry from a directory server.
- **close**: Unbinds from the server and closes the LDAP connection.

#### `add` API

Creates an entry in a directory server.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    anydata user = {
        "objectClass": "user",
        "sn": "New User",
        "cn": "New User",
        "givenName": "New User",
        "displayName": "New User",
        "userPrincipalName": "newuser@ad.windows",
        "userAccountControl": "544"
    };
    ldap:LdapResponse val = check ldapClient->add(userDN, user);
}
```

#### `modify` API

Updates information of an entry.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    anydata user = {
        "sn": "User",
        "givenName": "Updated User",
        "displayName": "Updated User"
    };
    _ = check ldapClient->modify(userDN, user);
}
```

#### `modifyDN` API

Renames an entry in a directory server.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:LdapResponse modifyDN = check ldapClient->modifyDN(userDN, "CN=Test User2", true);
}
```

#### `compare` API

Determines whether a given entry has a specified attribute value.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:LdapResponse compare = check ldapClient->compare(userDN, "givenName", "Test User1");
}
```

#### `search` API

Returns a record containing search result entries and references that match the given search parameters.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:SearchResult value = check ldapClient->search("DC=ad,DC=windows", "(givenName=Test User1)", SUB);
}
```

#### `searchWithType` API

Returns a list of entries that match the given search parameters.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    anydata[] value = check ldapClient->searchWithType("DC=ad,DC=com", "(givenName=Test User1)", ldap:SUB);
}
```

#### `getEntry` API

Gets information about an entry in a directory server.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    anydata value = check ldapClient->getEntry(userDN);
}
```

#### `delete` API

Removes an entry from a directory server.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:LdapResponse val = check ldapClient->delete(userDN);
}
```

#### `close` API

Unbinds from the server and closes the LDAP connection.

```ballerina
import ballerina/ldap;

public function main() {
    ldapClient->close();
}
```
