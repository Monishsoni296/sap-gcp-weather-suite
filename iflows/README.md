## 🔄 Integration Flows (iFlows)

This directory contains the SAP Integration Suite (Cloud Integration) artifacts used to orchestrate and process data flows within the weather suite application.

---

### 📁 Directory Structure

```text
iflows/
├── main-iflows/         # Core end-to-end orchestration iFlows
|   ├── bulk_weather_processing/         # /weather/bulk
|   ├── get_current_weather/             # /weather/current
|   ├── get_current_weather_summary/     # /weather/summary
|   ├── get_upcoming_forecast/           # /weather/forecast
|   ├── get_weather_alert/               # /weather/alert
|   ├── get_weather_dashboard/           # /weather/dashboard
├── modular-iflows/      # Reusable sub-processes and utility iFlows
|   ├── fetch_address/                   # Calls GCP Geocode API (Reverse lookup)
|   ├── fetch_coordinates/               # Calls GCP Geocode API (Forward lookup)
|   ├── fetch_gemini_response/           # Calls GCP Gemini AI (Generative summaries)
|   ├── fetch_weather_alert/             # Calls GCP Weather API (Severe weather alerts)
|   ├── gcp_weather_fetch/               # Calls GCP Weather API (Real-time data)     
└────── gcp_weather_forecast/            # Calls GCP Weather API (Paginated multi-day loop)
```
