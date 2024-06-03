## Overview

LDAP (Lightweight Directory Access Protocol) is an vendor-neutral software protocol for accessing and maintaining distributed directory information services. It allows users to locate organizations, individuals, and other resources such as files and devices in a network. LDAP is used in various applications for directory-based authentication and authorization.

The Ballerina LDAP module provides the capability to efficiently connect, authenticate, and interact with LDAP servers. It allows users to perform operations such as searching for entries, modifying entries in an LDAP directory, providing better support for directory-based operations.

### APIs associated with LDAP

- **modify**: Updates information of an entry in a directory server.
- **getEntry**: Gets information of an entry in a directory server.

#### `modify` API

Updates information of an entry.

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:UserConfig user = {
        sn: "User",
        givenName: "Updated User",
        displayName: "Updated User"
    };
    _ = check ldapClient->modify(userDN, user);
}
```

#### `getEntry` API

Gets information of an entry

```ballerina
import ballerina/ldap;

public function main() returns error? {
    ldap:UserConfig value = check ldapClient->getEntry(userDN);
}
```
