# Employee Directory and Authentication System

This example demonstrates using the Ballerina LDAP module to integrate with a directory server for managing users in a library system. The functionalities include adding new users, searching for books, and updating books.

## Running an Example

### 1. Start the directory server

Run the following docker command to start the LDAP server.

```sh
cd resources
docker-compose up
```

### 2. Run the Ballerina project

Execute the following commands to build an example from the source:

* To build an example:

    ```bash
    bal build
    ```

* To run an example:

    ```bash
    bal run
    ```
