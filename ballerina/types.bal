// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Provides a set of configurations to control the behaviours when communicating with a directory server.
#
# + hostName - The host name of the Active Directory server
# + port -  The port of the Active Directory server
# + domainName -  The domain name of the Active Directory
# + password - The password of the Active Directory
public type ConnectionConfig record {|
    string hostName;
    int port;
    string domainName;
    string password;
|};

# LDAP response type.
#
# + matchedDN - The matched DN from the response
# + resultCode - The operation status of the response
# + diagnosticMessage - The diagnostic message from the response
# + operationType - The protocol operation type
# + referral - The referral URIs
public type LdapResponse record {|
    string? matchedDN;
    Status resultCode;
    string? diagnosticMessage;
    string? operationType;
    string[]? referral;
|};

# LDAP search result type.
#
# + resultCode - The result status of the response
# + searchReferences - search references
# + entries - The entries returned from the search
public type SearchResult record {|
    Status resultCode;
    SearchReference[] searchReferences?;
    Entry[] entries?;
|};

# LDAP search reference type.
#
# + messageId - The message ID
# + uris - The referral URIs
# + controls - The controls
public type SearchReference record {|
    int messageId;
    string[] uris;
    Control[] controls;
|};

# LDAP control type.
#
# + oid - The OID of the control
# + isCritical - The criticality of the control
# + value - The value of the control
public type Control record {|
    string oid;
    boolean isCritical;
    string value;
|};

# Scope of the search operation.
#
public enum SearchScope {
    # Indicates that only the entry specified by the base DN should be considered
    BASE,
    # Indicates that only entries that are immediate subordinates of the entry specified by the base DN (but not the base entry itself) should be considered
    ONE,
    # Indicates that the base entry itself and any subordinate entries (to any depth) should be considered
    SUB,
    # Indicates that any subordinate entries (to any depth) below the entry specified by the base DN should be considered, but the base entry itself should not be considered, as described in [draft-sermersheim-ldap-subordinate-scope](https://docs.ldap.com/specs/draft-sermersheim-ldap-subordinate-scope-02.txt)
    SUBORDINATE_SUBTREE
};

# Attribute type of an LDAP entry.
public type AttributeType boolean|int|float|decimal|string|string[];

# LDAP entry type.
public type Entry record{|AttributeType...;|};

# A record for an entry that represents a person.
#
# + objectClass - object class of the person
# + sn - surname of the person
# + cn - common name of the person
# + userPassword - password of the person
# + telephoneNumber - telephone number of the person
public type Person record {
    string|string[]|ObjectClass|ObjectClass[] objectClass?;
    string sn?;
    string cn?;
    string userPassword?;
    string telephoneNumber?;
};

# Standard values for ObjectClass attribute type.
public enum ObjectClass {
    top,
    person,
    organizationalPerson,
    inetOrgPerson,
    organizationalRole,
    groupOfNames,
    groupOfUniqueNames,
    country,
    locality,
    organization,
    organizationalUnit,
    domainComponent,
    dcObject
};

# A record for an entry to contain domain component information
#
# + dc - name of the domain component
public type DcObject record {
    string dc;
};

# Represents the status of the operation
public enum Status {
    # The operation was successful
    SUCCESS,
    # The operation failed due to an unexpected order relative to other operations on the same connection
    OPERATIONS_ERROR = "OPERATIONS ERROR",
    # Represents the LDAP protocol error
    PROTOCOL_ERROR = "PROTOCOL ERROR",
    # Represents the time limit exceeded error
    TIME_LIMIT_EXCEEDED = "TIME LIMIT EXCEEDED",
    # Represents the error for exceeding the upper bound for the number of response entries
    SIZE_LIMIT_EXCEEDED = "SIZE LIMIT EXCEEDED",
    # Entry exists, attribute found, but no matching value
    COMPARE_FALSE = "COMPARE FALSE",
    # Entry exists, attribute found, and matched the provided value
    COMPARE_TRUE = "COMPARE TRUE",
    # Represents the error for unsupported or disallowed authentication mechanism
    AUTH_METHOD_NOT_SUPPORTED = "AUTH METHOD NOT SUPPORTED",
    # Requires a stronger form of authentication
    STRONGER_AUTH_REQUIRED = "STRONGER AUTH REQUIRED",
    # The request can't be processed as issued. Try a different server or update the target location in the DIT
    REFERRAL,
    # Administrative limit exceeded
    ADMIN_LIMIT_EXCEEDED = "ADMIN LIMIT EXCEEDED",
    # Critical control could not be fulfilled
    UNAVAILABLE_CRITICAL_EXTENSION = "UNAVAILABLE CRITICAL EXTENSION",
    # The server requires a secure connection to process the requested operation
    CONFIDENTIALITY_REQUIRED = "CONFIDENTIALITY REQUIRED",
    # The server has completed a portion of the processing for the provided SASL bind request, 
    # but that it needs additional information from the client to complete the authentication
    SASL_BIND_IN_PROGRESS = "SASL BIND IN PROGRESS",
    # Attribute that does not exist in the specified entry
    NO_SUCH_ATTRIBUTE = "NO SUCH ATTRIBUTE",
    # The request attempted to provide one or more values for an attribute type that is not defined in the server schema
    UNDEFINED_ATTRIBUTE_TYPE = "UNDEFINED ATTRIBUTE TYPE",
    # The search request attempted a type of matching that is not supported for the specified attribute type
    INAPPROPRIATE_MATCHING = "INAPPROPRIATE MATCHING",
    # The requested operation would have resulted in an entry that violates some constraint defined within the server
    CONSTRAINT_VIOLATION = "CONSTRAINT VIOLATION",
    # The requested operation would have resulted in an attribute in which the same value appeared more than once
    ATTRIBUTE_OR_VALUE_EXISTS = "ATTRIBUTE OR VALUE EXISTS",
    # The requested operation would have resulted in an entry that had at least one attribute value 
    # that does not conform to the constraints of the associated attribute syntax.
    INVALID_ATTRIBUTE_SYNTAX = "INVALID ATTRIBUTE SYNTAX",
    # The requested operation targeted an entry that does not exist within the DIT (Directory Information Tree).
    NO_SUCH_OBJECT = "NO SUCH OBJECT",
    # Error occurred while attempting to dereference an alias during search processing
    ALIAS_PROBLEM = "ALIAS PROBLEM",
    # The request included a malformed entry DN
    INVALID_DN_SYNTAX = "INVALID DN SYNTAX",
    # The server encountered an alias while processing the request and that there was some problem related to that alias.
    ALIAS_DEREFERENCING_PROBLEM = "ALIAS DEREFERENCING PROBLEM",
    # Attempt to bind in an inappropriate manner that is inappropriate for the target account
    INAPPROPRIATE_AUTHENTICATION = "INAPPROPRIATE AUTHENTICATION",
    # Attempt to bind with a set of credentials that cannot be used to authenticate
    INVALID_CREDENTIALS = "INVALID CREDENTIALS",
    # Requested operation does not have the necessary access control permissions
    INSUFFICIENT_ACCESS_RIGHTS = "INSUFFICIENT ACCESS RIGHTS",
    # The requested operation cannot be processed because the server is currently too busy
    BUSY,
    # The server is currently not available to process the requested operation
    UNAVAILABLE,
    # The server is not willing to process the requested operation for some reason
    UNWILLING_TO_PERFORM = "UNWILLING TO PERFORM",
    # The server detected some kind of circular reference in the course of processing an operation
    LOOP_DETECT = "LOOP DETECT",
    # The requested operation would have resulted in an entry that violates some naming constraint within the server
    NAMING_VIOLATION = "NAMING VIOLATION",
    # The requested operation would have resulted in an entry that has an inappropriate set of object classes,
    # or whose attributes violate the constraints associated with its set of object classes
    OBJECT_CLASS_VIOLATION = "OBJECT CLASS VIOLATION",
    # The requested operation is only supported for leaf entries, but the targeted entry has one or more subordinates
    NOT_ALLOWED_ON_NON_LEAF = "NOT ALLOWED ON NON LEAF",
    # the requested modify operation would have resulted in an entry that does not include all of the attributes used in its RDN
    NOT_ALLOWED_ON_RDN = "NOT ALLOWED ON RDN",
    # The requested operation would have resulted in an entry with the same DN as an entry that already exists in the server
    ENTRY_ALREADY_EXISTS = "ENTRY ALREADY EXISTS",
    # The requested modify operation would have altered the target entry’s set of object classes in a way that is not supported
    OBJECT_CLASS_MODS_PROHIBITED = "OBJECT CLASS MODS PROHIBITED",
    # The requested operation would have required manipulating information in multiple servers in a way that is not supported
    AFFECTS_MULTIPLE_DSAS = "AFFECTS MULTIPLE DSAS",
    # Represents other errors.
    OTHER
}
