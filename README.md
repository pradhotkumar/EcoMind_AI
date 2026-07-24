# EcoMind AI - Intelligent Sustainability Coach 🌿🤖

EcoMind AI is a futuristic, "Cyber-Eco" Android application that combines advanced AI coaching with real-time carbon analytics. It empowers users to transition towards zero-emission lifestyles through natural voice interaction, intelligent calm technology, and gamified habit building.

## ✨ Key Features

- **🎙️ Natural Voice Interaction**: Engage in seamless voice-to-voice eco check-ins powered by **ElevenLabs Conversational AI**.
- **🧠 AI Sustainability Coach**: A floating AI companion that monitors energy balance and provides conversational wisdom.
- **📊 Deep Carbon Analytics**: Machine learning models that track food, transit, and utility emissions to identify "carbon leakages."
- **🎛️ n8n Integration**: Adaptive alerts and automated carbon offsets triggered by environmental events via n8n webhooks.
- **🏆 Eco-Badges & XP**: Gamified progression system with 5 metallic badge tiers: Starter, Conscious, Impactful, Eco Hero, and Planet Guardian.
- **🎨 Elite Dark Aesthetic**: A premium "Cyber-Eco" UI with glassmorphic elements, deep navy gradients, and emerald/cyan neural glows.

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- ElevenLabs API Key and Agent ID.
- (Optional) Gemini AI API Key for enhanced textual coaching.

### Configuration

1. Clone the repository.
2. Create a `.env` file in the root directory (refer to `.env.example`).
3. Add your keys:
   ```env
   ELEVENLABS_API_KEY=your_key_here
   ELEVENLABS_AGENT_ID=your_agent_id_here
   GEMINI_API_KEY=your_gemini_key_here
   ```
4. Sync the project with Gradle.

## 🛠️ Tech Stack

- **UI**: Jetpack Compose with custom Canvas animations.
- **State Management**: Kotlin Flow & ViewModel.
- **Database**: Room for local telemetry and chat history.
- **Network**: OkHttp & Retrofit for WebSocket streaming and API calls.
- **AI**: ElevenLabs (Voice) & Google Gemini (Textual Intelligence).
- **Automation**: n8n Webhook architecture.

## 🎨 Redesigned Visual Identity

The app features a completely redesigned icon and theme:
- **Background**: Deep Navy Radial Gradient.
- **Icon**: Glowing Neural Leaf representing the fusion of Ecology and Intelligence.
- **Typography**: Clean, high-contrast Slate & Soft White.

---
*Built for the future of our planet. Let\'s optimize our footprint together.*
