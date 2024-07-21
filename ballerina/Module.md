## Overview

LDAP (Lightweight Directory Access Protocol) is a vendor-neutral software protocol for accessing and maintaining distributed directory information services. It allows users to locate organizations, individuals, and other resources such as files and devices in a network. LDAP is used in various applications for directory-based authentication and authorization.

The Ballerina LDAP module provides the capability to efficiently connect, authenticate, and interact with directory servers. It allows users to perform operations such as searching for entries, and modifying entries in an LDAP directory, providing better support for directory-based operations.

## Quickstart

To use the LDAP connector in your Ballerina project, modify the `.bal` file as follows.

### Step 1: Import the module

Import the `ballerinax/ldap` module into your Ballerina project.

```ballerina
import ballerinax/ldap;
```

### Step 2: Instantiate a new connector

```ballerina
configurable string baseUrl = ?;
configurable int identityMapCapacity = ?;
configurable map<anydata> originals = ?;
configurable map<string> headers = ?;

cregistry:Client schemaRegistryClient = check new ({
    baseUrl,
    identityMapCapacity,
    originals,
    headers
});
```

### Step 3: Invoke the connector operation

You can now utilize the operations available within the connector.

```ballerina
public function main() returns error? {
    string schema = string `
        {
            "type": "int",
            "name" : "value", 
            "namespace": "data"
        }`;

    int registerResult = check schemaRegistryClient.register("subject-name", schema);
}
```

### Step 4: Run the Ballerina application

Use the following command to compile and run the Ballerina program.

```bash
bal run
```

## Examples

The DocuSign eSignature connector provides practical examples illustrating usage in various scenarios. Explore these [examples](https://github.com/ballerina-platform/module-ballerina-ldap/tree/master/examples).

1. [Access directory server](https://github.com/ballerina-platform/module-ballerina-ldap/tree/master/examples/access-directory-server)
    This example shows how to integrate with a directory server to manage employees in a corporation.

2. [Manage entries in a library](https://github.com/ballerina-platform/module-ballerina-ldap/tree/master/examples/library-managment-system)
    This example demonstrates how to integrate with a directory server for managing users and books in a library.
