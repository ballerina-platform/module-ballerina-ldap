# Changelog

This file contains all the notable changes done to the Ballerina LDAP package through the releases.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- [Add support for selective attribute retrieval in LDAP client's get and search methods](https://github.com/ballerina-platform/ballerina-library/issues/8563)
  - Added optional `attributes` parameter to `getEntry()` method to allow specifying which attributes to retrieve
  - Added optional `attributes` parameter to `search()` method to allow selective attribute retrieval in search operations
  - Enhanced `searchWithType()` method to automatically extract and retrieve only the attributes defined in the target record type, providing type-safe selective attribute retrieval
  - This improvement is particularly useful for Active Directory use cases where retrieving specific attributes is more efficient than fetching all attributes

### Changed

### Fixed
