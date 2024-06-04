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

# Provides a set of configurations to control the behaviours when communicating with a schema registry.
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
# + resultStatus - The operation status of the response
# + operationType - The protocol operation type
public type LDAPResponse record {|
    string matchedDN;
    Status resultStatus;
    string operationType;
|};

# Represents the status of the operation
public enum Status {
    SUCCESS,
    OPERATIONS_ERROR,
    PROTOCOL_ERROR,
    TIME_LIMIT_EXCEEDED,
    SIZE_LIMIT_EXCEEDED,
    COMPARE_FALSE,
    COMPARE_TRUE,
    AUTH_METHOD_NOT_SUPPORTED,
    STRONGER_AUTH_REQUIRED,
    REFERRAL,
    ADMIN_LIMIT_EXCEEDED,
    UNAVAILABLE_CRITICAL_EXTENSION,
    CONFIDENTIALITY_REQUIRED,
    SASL_BIND_IN_PROGRESS,
    NO_SUCH_ATTRIBUTE,
    UNDEFINED_ATTRIBUTE_TYPE,
    INAPPROPRIATE_MATCHING,
    CONSTRAINT_VIOLATION,
    ATTRIBUTE_OR_VALUE_EXISTS,
    INVALID_ATTRIBUTE_SYNTAX,
    NO_SUCH_OBJECT,
    ALIAS_PROBLEM,
    INVALID_DN_SYNTAX,
    ALIAS_DEREFERENCING_PROBLEM,
    INAPPROPRIATE_AUTHENTICATION,
    INVALID_CREDENTIALS,
    INSUFFICIENT_ACCESS_RIGHTS,
    BUSY,
    UNAVAILABLE,
    UNWILLING_TO_PERFORM,
    LOOP_DETECT,
    NAMING_VIOLATION,
    OBJECT_CLASS_VIOLATION,
    NOT_ALLOWED_ON_NON_LEAF,
    NOT_ALLOWED_ON_RDN,
    ENTRY_ALREADY_EXISTS,
    OBJECT_CLASS_MODS_PROHIBITED,
    AFFECTS_MULTIPLE_DSAS,
    OTHER
};
