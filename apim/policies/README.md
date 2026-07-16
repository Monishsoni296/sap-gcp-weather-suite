# API Management Policies

This directory contains comprehensive API Management policies for the Weather Forecast API. Each policy file is in XML format and can be deployed to your API Management gateway.

## Policies Overview

### Rate Limiting & Throttling

#### 1. Spike Arrest (`spike-arrest.xml`)
**Purpose**: Request throttling to prevent API overload
- **Rate**: 30 requests per minute
- **Identifier**: Uniquely identifies applications/clients via `request.header.some-header-name`
- **Message Weight**: Adjustable impact calculation via `request.header.weight` header
- **Status**: Enabled with async processing

#### 2. Quota (`quota.xml`) / Quota_Policy (`Quota_Policy.xml`)
**Purpose**: Request quota management over calendar periods
- **Allow**: 60 requests per quota period
- **Time Unit**: Minute-based quota
- **Distributed**: Synchronized across all message processors
- **Start Time**: Begins from 2015-02-11 12:00:00
- **Type**: Calendar-based quota tracking

### Security & Authentication

#### 3. Authorization (`authorization.xml`)
**Purpose**: Basic authentication credential encoding
- **Operation**: Encode credentials in Base64
- **User**: Reference from `sapapim.clientid`
- **Password**: Reference from `sapapim.clientsecret`
- **Output**: Assigns encoded credentials to `sapapim.Authorization` variable

#### 4. Get Credential (`getcredential.xml`)
**Purpose**: Extract and assign credentials from secure storage
- **Operation**: AssignMessage policy
- **Client ID**: Retrieves from `private.client_id`
- **Client Secret**: Retrieves from `private.CPI_Runtime_Credentials.client_secret`
- **Variables Assigned**: `sapapim.clientid`, `sapapim.clientsecret`

#### 5. Key-Value Operations (`Key_Value_Ops.xml`)
**Purpose**: Retrieve credentials from centralized key-value store
- **Map**: CPI_Runtime_Credentials
- **Get Operations**:
  - `client_id` → `private.client_id`
  - `client_secret` → `private.client_secret`
- **Scope**: Environment-level access

### OAuth Token Management

#### 6. Get OAuth Token (`getoauthtoken.xml`)
**Purpose**: Request OAuth 2.0 access tokens
- **Flow**: Client credentials grant
- **Auth Header**: Uses `sapapim.Authorization`
- **Endpoint**: `https://_/oauth/token?grant_type=client_credentials`
- **Timeout**: 30 seconds
- **Output**: Stores response in `sapapim.tokenresponse`

#### 7. Read Access Token (`readaccesstoken.xml`)
**Purpose**: Extract access token from OAuth response
- **Method**: ExtractVariables with JSONPath
- **Extracts**:
  - `access_token` → `sapapim.accessToken`
  - `expires_in` → `sapapim.expiresIn`
- **Source**: `sapapim.tokenresponse`

#### 8. Read Cached Token (`readcachedtoken.xml`)
**Purpose**: Retrieve previously cached access token
- **Cache Lookup**: LookupCache policy
- **Key**: accesstoken
- **Scope**: Exclusive
- **Output Variable**: `sapapim.accessToken`

#### 9. Cache Access Token (`cacheaccesstoken.xml`)
**Purpose**: Store access token for reuse
- **Cache Operation**: PopulateCache
- **Key**: accesstoken
- **Scope**: Exclusive
- **TTL**: 3600 seconds (1 hour)
- **Source**: `sapapim.accessToken`

#### 10. Raise Token Error (`raisetokenerror.xml`)
**Purpose**: Handle and return token request errors
- **Response**: Fault response from token service
- **Payload**: Token error response content
- **Status Code**: From token service response

### Performance & Caching

#### 11. Response Cache (`response-cache.xml`)
**Purpose**: Cache API responses to improve performance
- **Cache Key**: Built from destination header, path suffix, and query string
- **TTL**: 3600 seconds (1 hour)
- **Skip Logic**: Can be bypassed with `InvalidateCache` header
- **Status**: Enabled with synchronous processing

## OAuth 2.0 Flow Sequence

The policies work together to implement a complete OAuth 2.0 client credentials flow:

1. **getcredential.xml** - Retrieve client credentials
2. **Key_Value_Ops.xml** - Load credentials from secure store
3. **authorization.xml** - Encode credentials for auth
4. **getoauthtoken.xml** - Request access token
5. **readaccesstoken.xml** - Extract token from response (on success)
6. **raisetokenerror.xml** - Handle errors (on failure)
7. **readcachedtoken.xml** - Check for cached token (subsequent requests)
8. **cacheaccesstoken.xml** - Cache token for reuse

## Deployment

Deploy these policies to your API Management gateway according to your deployment process. Ensure all referenced variables and headers are properly configured in your environment.

## Configuration

Before deploying, configure the following based on your environment:
- **OAuth Endpoint**: Update URL in `getoauthtoken.xml`
- **Client Credentials**: Update secure variables in `Key_Value_Ops.xml`
- **Rate Limits**: Adjust in `spike-arrest.xml` and `quota.xml`
- **Cache TTL**: Modify in `response-cache.xml` and `cacheaccesstoken.xml`
- **Header Names**: Update custom header references as needed
