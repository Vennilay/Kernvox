<a id="top"></a>

<div align="center">

# Kernvox

**Мобильный Android-клиент для мониторинга и управления серверной инфраструктурой**  
*Mobile Android client for server infrastructure monitoring and control*

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android API 24+" />
  <img src="https://img.shields.io/badge/Samsung_Academy-Project-1428A0?style=flat-square" alt="Samsung Academy" />
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-2EA44F?style=flat-square" alt="MIT License" /></a>
</p>

<p>
  <a href="#readme-ru"><strong>🇷🇺 Русская версия</strong></a>
  &nbsp;•&nbsp;
  <a href="#readme-en"><strong>🇬🇧 English version</strong></a>
  &nbsp;•&nbsp;
  <a href="https://github.com/Vennilay/KernvoxHub"><strong>🔗 KernvoxHub (Backend)</strong></a>
</p>

<br />

<img width="1672" height="941" alt="Kernvox Android application screenshot" src="https://github.com/user-attachments/assets/b851cbda-e268-4c82-8e62-1e30db06bfc4" />

</div>

---

<a id="readme-ru"></a>
# 🇷🇺 Kernvox — Android-клиент для мониторинга серверов

**Kernvox** — нативное Android-приложение для системных администраторов и DevOps-инженеров, которое позволяет за пару секунд проверить состояние серверов с телефона, не открывая полноценный веб-дашборд или SSH-терминал на компьютере.

Проект разработан в рамках **Samsung Academy** и работает в паре с серверным хабом [**KernvoxHub**](https://github.com/Vennilay/KernvoxHub).

<p align="center">
  <a href="#ru-how-it-works">Как это работает</a> ·
  <a href="#ru-features">Возможности</a> ·
  <a href="#ru-architecture">Архитектура и стек</a> ·
  <a href="#ru-security">Безопасность и API</a> ·
  <a href="#ru-setup">Запуск и сборка</a> ·
  <a href="#ru-status">Статус</a> ·
  <a href="#ru-authors">Авторы</a> ·
  <a href="#readme-en">🇬🇧 English</a>
</p>

<a id="ru-how-it-works"></a>
## Как это работает

Приложению не нужны прямые SSH-доступы к каждому серверу — вся инфраструктура разделена на два независимых слоя:

```text
┌──────────────────────┐       сбор метрик       ┌──────────────────┐       REST / JSON       ┌──────────────────┐
│  Ваши серверы / VPS  │ ◄─────────────────────► │    KernvoxHub    │ ──────────────────────► │     Kernvox      │
│  (CPU, RAM, процессы)│    выполнение команд    │  серверный хаб   │ ◄────────────────────── │  Android-клиент  │
└──────────────────────┘                         └──────────────────┘   X-API-Key / Action    └──────────────────┘
```

1. **[KernvoxHub](https://github.com/Vennilay/KernvoxHub)** выступает единой точкой сбора: он опрашивает подключённые серверы, агрегирует метрики, хранит их историю и выполняет административные команды.
2. **Kernvox** (это приложение) подключается к хабу по REST API, отображает живое состояние узлов в интерфейсе Material 3 и отправляет защищённые действия (например, перезагрузку сервера).

Такая схема избавляет телефон от хранения чувствительных ключей ко всем узлам и позволяет централизованно управлять списком серверов на стороне бэкенда.

<a id="ru-features"></a>
## Возможности

**Мониторинг и управление**
- **Сводка по инфраструктуре:** список всех серверов с онлайн/оффлайн-статусами, ключевыми показателями и ручным обновлением (pull-to-refresh).
- **Детальная карточка узла:** три вкладки — **«Обзор»** (CPU, RAM, диск, сеть, аптайм, адрес, пользователь и время последней метрики), **«Процессы»** и **«История»**.
- **Безопасная перезагрузка:** отправка команды reboot через KernvoxHub с отдельным ключом доступа и дополнительным диалогом подтверждения.

**Интерфейс и приватность**
- **Режим скрытности (Privacy Mode):** маскирует IP-адреса и имена пользователей в интерфейсе, если приложением приходится пользоваться в публичном месте или на демонстрации.
- **Комфортный UX:** экран первичной настройки при первом запуске, поддержка светлой, тёмной и системной тем, понятные сообщения об ошибках сети, авторизации и API.

**Локальная защита**
- **Блокировка приложения:** локальный пароль, опциональная биометрическая разблокировка (отпечаток / Face ID) и автоматическая блокировка при возврате в приложение.

<a id="ru-architecture"></a>
## Архитектура и технологии

Приложение построено по паттерну **MVVM** с чётким разделением слоёв:

```text
app/src/main/java/com/vennilay/kernvox/
├── ui/              # Экраны и переиспользуемые компоненты Jetpack Compose
├── viewmodel/       # Состояние экранов (UI State) и обработка действий пользователя
├── data/
│   ├── network/     # Ktor-клиент (OkHttp), сериализация и вызовы API
│   ├── repository/  # Слой доступа к данным и маппинг моделей
│   └── storage/     # Preferences DataStore и шифрование секретов
└── auth/            # Локальная блокировка, проверка пароля и биометрия
```

| Область | Технологии |
| :--- | :--- |
| **Язык и UI** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Сеть и данные** | Ktor Client (OkHttp), `kotlinx.serialization`, Preferences DataStore |
| **Безопасность** | Android Keystore, AES/GCM/NoPadding, PBKDF2, AndroidX Biometric |
| **Сборка и SDK** | Gradle Kotlin DSL, Min SDK 24 (Android 7.0), Target/Compile SDK 36, Java 11 bytecode |

> Актуальные версии всех зависимостей перечислены в `gradle/libs.versions.toml`.

<a id="ru-security"></a>
## Безопасность и API KernvoxHub

### Модель защиты данных
- **Два раздельных ключа API:** для чтения метрик используется заголовок `X-API-Key`, а для опасных действий (перезагрузки) — отдельный `X-Action-Key`. Приложение может работать в режиме «только чтение», если ключ действия не задан.
- **Шифрование секретов:** оба ключа хранятся в Preferences DataStore строго в зашифрованном виде (`AES/GCM/NoPadding`) с мастер-ключом внутри **Android Keystore**.
- **Хранение пароля:** локальный пароль блокировки не сохраняется в открытом виде — используются случайная соль (salt) и хэш **PBKDF2**. Биометрия работает только поверх включённого пароля.
- **Защита транспорта и копий:** в release-сборке для обычных серверов требуется **HTTPS**, а системный **Android Backup отключён**, чтобы секреты и настройки не попадали в облачные бэкапы.

### Ожидаемые эндпоинты KernvoxHub

| Метод | Эндпоинт | Заголовок | Назначение |
| :---: | :--- | :---: | :--- |
| `GET` | `/api/v1/android/dashboard` | `X-API-Key` | Сводка по всем серверам |
| `GET` | `/api/v1/android/servers/{id}/details` | `X-API-Key` | Подробная информация об узле |
| `GET` | `/api/v1/android/servers/{id}/processes?limit=50` | `X-API-Key` | Список активных процессов |
| `GET` | `/api/v1/android/servers/{id}/metrics/history?limit=100` | `X-API-Key` | История собранных метрик |
| `POST` | `/api/v1/servers/{id}/actions/reboot` | `X-Action-Key` | Перезагрузка сервера |

<a id="ru-setup"></a>
## Установка, запуск и сборка

### Требования
- **Android Studio** с поддержкой актуального Android Gradle Plugin
- **JDK 17** и **Android SDK Platform 36**
- Устройство или эмулятор с **Android 7.0 (API 24)** или выше
- Развёрнутый экземпляр **[KernvoxHub](https://github.com/Vennilay/KernvoxHub)** с настроенным `X-API-Key`

### Быстрый старт

```bash
git clone https://github.com/Vennilay/Kernvox.git
cd Kernvox
```

1. Откройте папку проекта в **Android Studio** и дождитесь завершения Gradle Sync.
2. Запустите приложение на эмуляторе или физическом устройстве.
3. На экране настроек укажите URL вашего **KernvoxHub** и `X-API-Key` (и при необходимости `X-Action-Key` для перезагрузки серверов).

> [!TIP]
> Для подключения к локальному KernvoxHub из Android Emulator используйте адрес `http://10.0.2.2:<port>`. HTTP разрешён для debug-сборок и локальных адресов разработки; в release-сборке для внешних серверов обязателен HTTPS.

### Команды Gradle

| Команда | Назначение |
| :--- | :--- |
| `./gradlew assembleDebug` | Собрать debug APK |
| `./gradlew installDebug` | Установить debug APK на подключённое устройство |
| `./gradlew assembleRelease` | Собрать release APK (включены R8/ProGuard и shrink resources) |
| `./gradlew test` | Запустить unit-тесты |
| `./gradlew connectedAndroidTest` | Запустить instrumentation- и Compose UI-тесты на устройстве/эмуляторе |
| `./gradlew clean` | Очистить артефакты сборки |

<a id="ru-status"></a>
## Статус и ограничения

Приложение полностью работоспособно как мобильный клиент мониторинга для **KernvoxHub**: доступны просмотр сводки, деталей узлов, процессов, истории метрик, режим приватности, локальная защита и перезагрузка серверов.

**Текущие ограничения:**
- добавление и редактирование серверов выполняется на стороне **KernvoxHub**, а не в мобильном приложении;
- графики, группировка серверов и экспорт данных пока не реализованы;
- для работы требуется запущенный сервер **KernvoxHub**.

<a id="ru-authors"></a>
## Авторы и участники

Проект создан и поддерживается в рамках **Samsung Academy**:

<table>
  <tr>
    <td align="center" width="220">
      <a href="https://github.com/Vennilay">
        <img src="https://github.com/Vennilay.png?size=100" width="90" height="90" alt="Vennilay" /><br />
        <strong>Vennilay</strong>
      </a><br />
      <sub>Архитектура · Безопасность & Keystore · Интеграция API · CI/CD · Документация</sub>
    </td>
    <td align="center" width="220">
      <a href="https://github.com/BAALOC">
        <img src="https://github.com/BAALOC.png?size=100" width="90" height="90" alt="Makar Leonardov" /><br />
        <strong>Makar Leonardov</strong>
      </a><br />
      <sub>UI-компоненты Compose · Рефакторинг · Unit & Compose UI-тесты</sub>
    </td>
  </tr>
</table>

<p>
  <a href="https://github.com/Vennilay/Kernvox/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=Vennilay/Kernvox" alt="Contributors" />
  </a>
</p>

## Лицензия

Проект распространяется под лицензией **MIT** — подробности в файле [LICENSE](LICENSE).

[⬆️ Наверх](#top) · [🇬🇧 Перейти к английской версии](#readme-en)

---

<a id="readme-en"></a>
# 🇬🇧 Kernvox — Android Server Monitoring Client

**Kernvox** is a native Android application for system administrators and DevOps engineers that lets you check server health in seconds right from your phone—without opening a full monitoring dashboard or SSH client on a workstation.

Developed as part of **Samsung Academy**, Kernvox works alongside the [**KernvoxHub**](https://github.com/Vennilay/KernvoxHub) backend service.

<p align="center">
  <a href="#en-how-it-works">How It Works</a> ·
  <a href="#en-features">Features</a> ·
  <a href="#en-architecture">Architecture & Stack</a> ·
  <a href="#en-security">Security & API</a> ·
  <a href="#en-setup">Setup & Build</a> ·
  <a href="#en-status">Status</a> ·
  <a href="#en-authors">Authors</a> ·
  <a href="#readme-ru">🇷🇺 Русский</a>
</p>

<a id="en-how-it-works"></a>
## How It Works

Your phone never needs direct SSH access to every server. The system is intentionally split into two decoupled components:

```text
┌──────────────────────┐     collects metrics    ┌──────────────────┐       REST / JSON       ┌──────────────────┐
│  Your Servers / VPS  │ ◄─────────────────────► │    KernvoxHub    │ ──────────────────────► │     Kernvox      │
│  (CPU, RAM, procs)   │      runs actions       │  server backend  │ ◄────────────────────── │  Android client  │
└──────────────────────┘                         └──────────────────┘   X-API-Key / Action    └──────────────────┘
```

1. **[KernvoxHub](https://github.com/Vennilay/KernvoxHub)** acts as the central hub: it polls your servers, collects and stores metrics, and executes server actions.
2. **Kernvox** (this app) connects to the hub over a REST API, renders real-time server status in a Material 3 interface, and triggers protected actions such as server reboots.

This keeps infrastructure credentials off your mobile device and lets you manage monitored nodes centrally on the backend.

<a id="en-features"></a>
## Features

**Monitoring & Control**
- **Infrastructure dashboard:** live server list with online/offline statuses, key metrics at a glance, and pull-to-refresh.
- **Detailed server view:** three tabs — **Overview** (CPU, RAM, disk, network, uptime, host address, username, and last metric timestamp), **Processes**, and **History**.
- **Protected reboot:** trigger server reboots via KernvoxHub guarded by a separate action key and an explicit confirmation dialog.

**Interface & Privacy**
- **Privacy Mode:** masks IP addresses and usernames on screen so you can safely check servers in public or during demos.
- **Clean UX:** guided first-launch onboarding, light/dark/system themes, and clear user-facing error handling for network, auth, and API failures.

**On-Device Protection**
- **App lock:** local password protection, optional biometric unlock (fingerprint / face), and automatic lock when returning to the app.

<a id="en-architecture"></a>
## Architecture & Tech Stack

The Android app follows a clean **MVVM** layout:

```text
app/src/main/java/com/Vennilay/kernvox/
├── ui/              # Jetpack Compose screens and reusable Material 3 components
├── viewmodel/       # Screen UI state and user action handling
├── data/
│   ├── network/     # Ktor HTTP client (OkHttp), serialization, and API calls
│   ├── repository/  # Data access layer and domain mappers
│   └── storage/     # Preferences DataStore and encrypted secret storage
└── auth/            # Local app lock, password verification, and biometrics
```

| Layer | Technologies |
| :--- | :--- |
| **Language & UI** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Networking & Storage** | Ktor Client (OkHttp), `kotlinx.serialization`, Preferences DataStore |
| **Security** | Android Keystore, AES/GCM/NoPadding, PBKDF2, AndroidX Biometric |
| **Build & SDK** | Gradle Kotlin DSL, Min SDK 24 (Android 7.0), Target/Compile SDK 36, Java 11 bytecode |

> Dependency versions are centrally managed in `gradle/libs.versions.toml`.

<a id="en-security"></a>
## Security & KernvoxHub API

### Security Model
- **Separated API keys:** read-only requests authenticate with `X-API-Key`, while sensitive actions (like rebooting) require a dedicated `X-Action-Key`. Leave the action key empty to run the client strictly in read-only mode.
- **Encrypted secret storage:** both keys are encrypted with `AES/GCM/NoPadding` using a key generated and held inside **Android Keystore** before being written to Preferences DataStore.
- **Password & biometrics:** the local lock password is never stored in plain text—only a random salt and **PBKDF2** hash are persisted. Biometrics act strictly as an unlock convenience on top of password lock.
- **Transport & backup hardening:** release builds enforce **HTTPS** for standard server hosts, and **Android Backup is disabled** so keys and settings never leak into cloud backups.

### Expected KernvoxHub Endpoints

| Method | Endpoint | Header | Purpose |
| :---: | :--- | :---: | :--- |
| `GET` | `/api/v1/android/dashboard` | `X-API-Key` | Dashboard summary for all servers |
| `GET` | `/api/v1/android/servers/{id}/details` | `X-API-Key` | Detailed server metrics & metadata |
| `GET` | `/api/v1/android/servers/{id}/processes?limit=50` | `X-API-Key` | Running server processes |
| `GET` | `/api/v1/android/servers/{id}/metrics/history?limit=100` | `X-API-Key` | Historical server metrics |
| `POST` | `/api/v1/servers/{id}/actions/reboot` | `X-Action-Key` | Server reboot action |

<a id="en-setup"></a>
## Setup, Run & Build

### Requirements
- **Android Studio** supporting the current Android Gradle Plugin
- **JDK 17** and **Android SDK Platform 36**
- Physical device or emulator running **Android 7.0 (API 24)** or newer
- A running **[KernvoxHub](https://github.com/Vennilay/KernvoxHub)** instance with a configured `X-API-Key`

### Quick Start

```bash
git clone https://github.com/Vennilay/Kernvox.git
cd Kernvox
```

1. Open the project in **Android Studio** and wait for Gradle Sync to complete.
2. Run the app on an emulator or physical device.
3. Open **Settings** and provide your **KernvoxHub** URL and `X-API-Key` (plus `X-Action-Key` if you want to enable server reboots).

> [!TIP]
> When connecting to a local KernvoxHub instance from the Android Emulator, use `http://10.0.2.2:<port>`. HTTP is permitted for debug builds and local development hosts; release builds require HTTPS for regular server URLs.

### Gradle Commands

| Command | Description |
| :--- | :--- |
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew installDebug` | Install debug APK on a connected device |
| `./gradlew assembleRelease` | Build release APK (R8/ProGuard minification & resource shrinking enabled) |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumentation & Compose UI tests on a device/emulator |
| `./gradlew clean` | Clean build artifacts |

<a id="en-status"></a>
## Status & Limitations

Kernvox is ready for demonstration and everyday monitoring with **KernvoxHub**, covering dashboard status, server details, processes, metric history, privacy mode, local lock, and protected reboots.

**Current limitations:**
- servers are added and managed on the **KernvoxHub** side rather than inside the mobile app;
- charts, server grouping, and data export are not yet implemented;
- a deployed **KernvoxHub** instance is required for full functionality.

<a id="en-authors"></a>
## Authors & Contributors

Created and maintained as part of **Samsung Academy**:

<table>
  <tr>
    <td align="center" width="220">
      <a href="https://github.com/Vennilay">
        <img src="https://github.com/Vennilay.png?size=100" width="90" height="90" alt="Vennilay" /><br />
        <strong>Vennilay</strong>
      </a><br />
      <sub>Architecture · Security & Keystore · API Integration · CI/CD · Docs</sub>
    </td>
    <td align="center" width="220">
      <a href="https://github.com/BAALOC">
        <img src="https://github.com/BAALOC.png?size=100" width="90" height="90" alt="Makar Leonardov" /><br />
        <strong>Makar Leonardov</strong>
      </a><br />
      <sub>Compose UI Components · Refactoring · Unit & Compose UI Tests</sub>
    </td>
  </tr>
</table>

<p>
  <a href="https://github.com/Vennilay/Kernvox/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=Vennilay/Kernvox" alt="Contributors" />
  </a>
</p>

## License

Distributed under the **MIT License** — see [LICENSE](LICENSE) for details.

[⬆️ Back to top](#top) · [🇷🇺 Switch to Russian](#readme-ru)
