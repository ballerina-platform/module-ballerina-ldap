# Ballerina LDAP Library

[![Build](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerina-ldap/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerina-ldap)
[![Trivy](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/trivy-scan.yml)
[![GraalVM Check](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-with-bal-test-graalvm.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerina-ldap.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/commits/master)
[![Github issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/ldap.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-standard-library/labels/module%ldap)

LDAP (Lightweight Directory Access Protocol) is a vendor-neutral software protocol for accessing and maintaining distributed directory information services. It allows users to locate organizations, individuals, and other resources such as files and devices in a network. LDAP is used in various applications for directory-based authentication and authorization.

The Ballerina LDAP module provides the capability to efficiently connect, authenticate, and interact with directory servers. It allows users to perform operations such as searching for entries, and modifying entries in an LDAP directory, providing better support for directory-based operations.

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

## Issues and projects

The **Issues** and **Projects** tabs are disabled for this repository as this is part of the Ballerina library. To report bugs, request new features, start new discussions, view project boards, etc., visit the Ballerina library [parent repository](https://github.com/ballerina-platform/ballerina-library).

This repository only contains the source code for the package.

## Building from the source

### Prerequisites

1. Download and install Java SE Development Kit (JDK) version 17. You can download it from either of the following sources:

   - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   - [OpenJDK](https://adoptium.net/)

    > **Note:** After installation, remember to set the `JAVA_HOME` environment variable to the directory where JDK was installed.

2. Download and install [Ballerina Swan Lake](https://ballerina.io/).

3. Download and install [Docker](https://www.docker.com/get-started).

    > **Note**: Ensure that the Docker daemon is running before executing any tests.

4. Generate a Github access token with read package permissions, then set the following `env` variables:

    ```bash
   export packageUser=<Your GitHub Username>
   export packagePAT=<GitHub Personal Access Token>
    ```

### Build options

Execute the commands below to build from the source.

1. To build the package:

   ```bash
   ./gradlew clean build
   ```

2. To run the tests:

   ```bash
   ./gradlew clean test
   ```

3. To build the without the tests:

   ```bash
   ./gradlew clean build -x test
   ```

4. To debug package with a remote debugger:

   ```bash
   ./gradlew clean build -Pdebug=<port>
   ```

5. To debug with Ballerina language:

   ```bash
   ./gradlew clean build -PbalJavaDebug=<port>
   ```

6. Publish the generated artifacts to the local Ballerina central repository:

   ```bash
   ./gradlew clean build -PpublishToLocalCentral=true
   ```

7. Publish the generated artifacts to the Ballerina central repository:

   ```bash
   ./gradlew clean build -PpublishToCentral=true
   ```

## Contributing to Ballerina

As an open source project, Ballerina welcomes contributions from the community.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

- Discuss code changes of the Ballerina project in [ballerina-dev@googlegroups.com](mailto:ballerina-dev@googlegroups.com).
- Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
- Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
