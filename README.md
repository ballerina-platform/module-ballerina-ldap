Ballerina LDAP Connector
===================

LDAP (Lightweight Directory Access Protocol) is an vendor-neutral software protocol for accessing and maintaining distributed directory information services. It allows users to locate organizations, individuals, and other resources such as files and devices in a network. LDAP is used in various applications for directory-based authentication and authorization.

The Ballerina LDAP module provides the capability to efficiently connect, authenticate, and interact with directory servers. It allows users to perform operations such as searching for entries, modifying entries in an LDAP directory, providing better support for directory-based operations.

### APIs associated with LDAP

- **add**: Creates an entry in a directory server.
- **modify**: Updates information of an entry in a directory server.
- **getEntry**: Gets information about an entry in a directory server.
- **delete**: Removes an entry from a directory server.

#### `add` API

Creates an entry in a directory server.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    anydata user = {
        objectClass: ["user", "organizationalPerson", "person", "top"],
        sn: "New User",
        cn: "New User",
        givenName: "New User",
        displayName: "New User",
        userPrincipalName: "newuser@ad.windows",
        userAccountControl: "544"
    };
    ldap:LDAPResponse val = check ldapClient->add(userDN, user);
}
```

#### `modify` API

Updates information of an entry.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    anydata user = {
        sn: "User",
        givenName: "Updated User",
        displayName: "Updated User"
    };
    _ = check ldapClient->modify(userDN, user);
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
    ldap:LDAPResponse val = check ldapClient->delete(userDN);
}
```
