# EcoMind AI
**Intelligent Sustainability Coach for a Greener Future**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

---

<p align="center">
  <img src="docs/screenshots/poster.png" width="700" alt="EcoMind AI Overview">
</p>

## 📋 Project Overview

EcoMind AI is a production-grade Android application designed to transform how individuals interact with sustainability. By leveraging cutting-edge Artificial Intelligence, the platform provides a seamless, personalized experience for habit tracking, carbon analytics, and environmental education.

Traditional sustainability apps often feel like chores; EcoMind AI changes this by introducing an **Ambient AI Coach** that engages users through natural voice and intelligent insights, making the journey to a zero-emission lifestyle intuitive and rewarding.

---

## 🎯 Why EcoMind AI?

Climate change is an complex challenge, and individual impact often feels invisible. EcoMind AI bridges this gap by:
- **Quantifying Impact**: Turning abstract carbon data into actionable daily metrics.
- **Lowering Friction**: Using Voice AI to make logging and learning as natural as a conversation.
- **Ensuring Consistency**: Utilizing gamification and smart automation to turn one-time actions into lifelong habits.

---

## 🚀 Key Features

### 🤖 AI Sustainability Coach
A sophisticated assistant powered by Google Gemini that analyzes your lifestyle patterns and provides high-context habit recommendations.

### 🎙️ Voice AI Assistant
Hands-free interaction using ElevenLabs Conversational AI, allowing users to log activities and ask questions through natural speech.

### 📊 Carbon Footprint Analytics
Real-time tracking of emissions across transport, food, and energy sectors with visual data breakdowns and historical comparisons.

### 🏆 Gamification (XP & Badges)
A tiered progression system (Starter to Planet Guardian) that rewards sustainable choices with XP and collectible metallic badges.

### ⚙️ Smart Automation
Integration with n8n workflows to automate eco-reminders and coordinate with external smart-home or workspace ecosystems.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin (100%) |
| **UI Framework** | Jetpack Compose (Modern Declarative UI) |
| **Architecture** | MVVM + Clean Architecture |
| **Local Database** | Room Persistence Library |
| **Networking** | Retrofit + OkHttp + WebSockets |
| **AI (LLM)** | Google Gemini Pro |
| **AI (Voice)** | ElevenLabs Conversational AI |
| **Automation** | n8n Webhook Architecture |
| **Concurrency** | Kotlin Coroutines & Flow |

---

## 🏗️ System Architecture

The following diagram illustrates the high-level data flow and component interaction within the EcoMind AI ecosystem:

```mermaid
graph TD
    User([User]) -->|Voice/Text| UI[Jetpack Compose Layer]
    UI --> VM[ViewModel]
    VM --> Repo[Repository Pattern]
    
    subgraph AI_Engine [Intelligence Layer]
        Repo --> Gemini[Google Gemini LLM]
        Repo --> Eleven[ElevenLabs Voice AI]
    end
    
    subgraph Data_Layer [Persistence & Network]
        Repo --> Room[(Local SQLite / Room)]
        Repo --> N8N[n8n Automation Engine]
    end
    
    Gemini -->|Insights| VM
    Eleven -->|Audio Stream| UI
    N8N -->|Webhooks| External[External Services]
```

---

## 📂 Project Structure

The project follows a modularized package-by-feature structure to ensure scalability and maintainability:

```text
com.example.ui/
├── screens/         # Feature-specific UI (Home, Chat, Dashboard)
├── viewmodel/       # UI Logic and State Management
├── speech/          # Voice AI Integration (ElevenLabs)
└── theme/           # Design System (Colors, Typography, Shapes)

com.example.data/
├── local/           # Room DB, Entities, and DAOs
├── model/           # DTOs and Domain Models
├── network/         # Retrofit Services and API Definitions
└── repository/      # Single Source of Truth for data
```

---

## ⚙️ Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/pradhotkumar/EcoMind_AI.git
cd EcoMind_AI
```

### 2. Environment Variable Setup
EcoMind AI uses the [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin). Create a `.env` file in the root directory:

```env
# Required for Voice Interaction
ELEVENLABS_API_KEY=your_elevenlabs_api_key
ELEVENLABS_AGENT_ID=your_agent_id

# Required for AI Coaching
GEMINI_API_KEY=your_gemini_api_key
```

### 3. Build & Run
1. Open the project in **Android Studio Ladybug (or newer)**.
2. Sync the project with Gradle files.
3. Select an emulator or physical device (Min SDK 24).
4. Click **Run**.

---

## 🔄 AI & Automation Workflow

```mermaid
sequenceDiagram
    participant U as User
    participant V as Voice Engine
    participant G as Gemini AI
    participant D as Dashboard
    
    U->>V: Voice Input ("I took the bus today")
    V->>G: Process Intent
    G->>D: Update Carbon Metrics
    G-->>U: Voice Feedback ("Great! You saved 2kg of CO2")
    G->>U: Suggest relevant Eco-Challenge
```

---

## 📱 App Screenshots

| Home Interface | AI Coach Call |
| :---: | :---: |
| <img src="docs/screenshots/home.jpeg" width="300"> | <img src="docs/screenshots/coach.jpeg" width="300"> |

| Carbon Dashboard | Achievement System |
| :---: | :---: |
| <img src="docs/screenshots/analytics.jpeg" width="300"> | <img src="docs/screenshots/badges.jpeg" width="300"> |

---

## 🗺️ Roadmap

- [x] Initial MVP with Carbon Tracking
- [x] ElevenLabs Voice Integration
- [x] Google Gemini Coaching Engine
- [ ] **Phase 2**: Smart Home IoT Integration (Philips Hue/Nest)
- [ ] **Phase 3**: Community Leaderboards and Social Challenges
- [ ] **Phase 4**: Wear OS Companion App for micro-logging
- [ ] **Phase 5**: AI-powered waste detection via Camera API

---

## 🤝 Contributing

We welcome contributions to EcoMind AI. To ensure a professional environment, please follow these steps:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes using [Conventional Commits](https://www.conventionalcommits.org/).
4. Push to the branch.
5. Open a Pull Request.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <b>Think Smarter. Live Greener. Protect Tomorrow.</b><br>
  Made with ❤️ for a cleaner and more intelligent future.
</p>
