# Ballerina LDAP Connector

[![Build](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerina-ldap/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerina-ldap)
[![Trivy](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/trivy-scan.yml)
[![GraalVM Check](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/actions/workflows/build-with-bal-test-graalvm.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerina-ldap.svg)](https://github.com/ballerina-platform/module-ballerina-ldap/commits/main)
[![Github issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/ldap.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-standard-library/labels/module%ldap)

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
