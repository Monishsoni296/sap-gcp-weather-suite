## 🔄 Integration Flows (iFlows)

This directory contains the SAP Integration Suite (Cloud Integration) artifacts used to orchestrate and process data flows within the weather suite application.

---

### 📁 Directory Structure

```text
iflows/
├── main-iflows/         # Core end-to-end orchestration iFlows
|   ├── bulk_weather_processing/         # Core end-to-end orchestration iFlows
|   ├── get_current_weather/         # Core end-to-end orchestration iFlows
|   ├── get_current_weather_summary/         # Core end-to-end orchestration iFlows
|   ├── get_upcoming_forecast/         # Core end-to-end orchestration iFlows
|   ├── get_weather_alert/         # Core end-to-end orchestration iFlows
|   ├── get_weather_dashboard/         # Core end-to-end orchestration iFlows
|   ├── main-iflows/         # Core end-to-end orchestration iFlows
├── modular-iflows/      # Reusable sub-processes and utility iFlows
|   ├── fetch_address/         # Core end-to-end orchestration iFlows
|   ├── fetch_coordinates/         # Core end-to-end orchestration iFlows
|   ├── fetch_gemini_response/         # Core end-to-end orchestration iFlows
|   ├── fetch_weather_alert/         # Core end-to-end orchestration iFlows
|   ├── gcp_weather_fetch/         # Core end-to-end orchestration iFlows
└────── gcp_weather_forecast/         # Core end-to-end orchestration iFlows
```
