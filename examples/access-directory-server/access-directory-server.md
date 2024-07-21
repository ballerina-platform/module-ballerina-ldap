# Employee Directory and Authentication System

This example demonstrates using the Ballerina LDAP module to integrate with a directory server to manage employees in a corporation. The functionalities include authenticating with the directory server, adding a new user, searching for users, updating user details, and deleting a user.

## Running an Example

### 1. Start the directory server

Run the following docker command to start the LDAP server.

```sh
cd resources
docker compose up
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
