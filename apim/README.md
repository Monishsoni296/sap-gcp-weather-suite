## API Management Configuration

This repository contains comprehensive API Management policies and specifications for the Weather Forecast API integration.

### 📁 Directory Structure

```
apim/
├── api-specs/                           # OpenAPI specifications
│   └── RequestWeatherForecast.yaml
├── policies/                            # API Management policies (11 total)
│   ├── authorization.xml                # Basic authentication encoding
│   ├── cacheaccesstoken.xml             # OAuth token caching
│   ├── getcredential.xml                # Credential extraction
│   ├── getoauthtoken.xml                # OAuth 2.0 token request
│   ├── Key_Value_Ops.xml                # Secure credential retrieval
│   ├── quota.xml                        # Request quota limiting
│   ├── raisetokenerror.xml              # OAuth error handling
│   ├── readaccesstoken.xml              # Token extraction from response
│   ├── readcachedtoken.xml              # Cached token lookup
│   ├── response-cache.xml               # API response caching
│   ├── spike-arrest.xml                 # Traffic throttling
│   └── README.md                        # Policy documentation
├── README.md                            # This file
└── .gitignore                           # Git ignore rules
```

### 🎯 Overview

#### API Specifications
- **Weather Forecast API** (`api-specs/RequestWeatherForecast.yaml`)
  - OpenAPI 3.0.1 specification with 6 endpoints
  - Supports location-based and coordinate-based queries
  - Includes current weather, forecasts, alerts, and bulk processing

#### Policies (11 Total)

**Rate Limiting & Throttling:**
- `spike-arrest.xml` - Throttles requests (30 req/min)
- `quota.xml` - Calendar-based quota enforcement (60 req/min)

**Security & Authentication:**
- `authorization.xml` - Basic authentication credential encoding
- `getcredential.xml` - Credential extraction and assignment
- `Key_Value_Ops.xml` - Secure credential store retrieval

**OAuth 2.0 Token Management:**
- `getoauthtoken.xml` - Request OAuth 2.0 access tokens (client credentials flow)
- `readaccesstoken.xml` - Extract token from OAuth response
- `readcachedtoken.xml` - Retrieve cached tokens for reuse
- `cacheaccesstoken.xml` - Cache tokens with 1-hour TTL
- `raisetokenerror.xml` - Handle and return OAuth errors

**Performance & Caching:**
- `response-cache.xml` - Cache API responses (1-hour TTL)

See `policies/README.md` for detailed policy documentation and OAuth flow sequence.

### 🚀 Quick Start

1. Review the API specification:
   ```
   api-specs/RequestWeatherForecast.yaml
   ```

2. Configure policies:
   ```
   policies/
   ```

3. Deploy to your API Management gateway

### 📋 API Endpoints

- **GET `/current`** - Current weather forecast
- **GET `/summary`** - Weather forecast summary
- **GET `/forecast`** - Daily or hourly forecast
- **GET `/alert`** - Weather alerts
- **GET `/dashboard`** - All forecast information
- **POST `/bulk`** - Bulk forecast processing for multiple locations

### ⚙️ Configuration

All policies support environment-specific configuration:
- **OAuth Endpoint**: Update URL in `getoauthtoken.xml`
- **Rate Limits**: Adjust limits in `spike-arrest.xml` and `quota.xml`
- **Cache TTL**: Modify timeout values in `response-cache.xml` and `cacheaccesstoken.xml`
- **Credentials**: Update references in `Key_Value_Ops.xml` and `getcredential.xml`
- **Header Names**: Update custom header references as needed

Refer to `policies/README.md` for detailed configuration guidance for each policy.

## 📝 License

Add your license information here.

## 🤝 Contributing

Add contribution guidelines here.

5c2b35241
