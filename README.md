# Kernvox

<img width="1672" height="941" alt="Kernvox Android application screenshot" src="https://github.com/user-attachments/assets/b851cbda-e268-4c82-8e62-1e30db06bfc4" />

<a id="language"></a>
## Language / Язык

Choose the documentation language before reading / Выберите язык документации перед чтением:

- 🇷🇺 [Русская версия](#readme-ru)
- 🇬🇧 [English version](#readme-en)

---

<a id="readme-ru"></a>
# Kernvox — Android-клиент для мониторинга серверов

Kernvox — Android-приложение для системных администраторов и DevOps-инженеров. Приложение подключается к серверному хабу KernvoxHub, получает данные через REST API и показывает состояние серверной инфраструктуры в мобильном интерфейсе.

Проект разработан в рамках Samsung Academy.

**Связанные репозитории:**

- [Kernvox](https://github.com/Vennilay/Kernvox) — Android-клиент, текущий репозиторий.
- [KernvoxHub](https://github.com/Vennilay/KernvoxHub) — серверный хаб для сбора метрик и предоставления API.

## Содержание

- [О проекте](#ru-about)
- [Задача проекта](#ru-purpose)
- [Возможности](#ru-features)
- [Архитектура](#ru-architecture)
- [API KernvoxHub](#ru-api)
- [Технологии](#ru-tech-stack)
- [Установка и запуск](#ru-setup)
- [Сборка и тесты](#ru-build)
- [Безопасность](#ru-security)
- [Статус и ограничения](#ru-status)
- [Лицензия](#ru-license)
- [Контакты](#ru-contacts)
- 🇬🇧 [English version](#readme-en)

<a id="ru-about"></a>
## О проекте

Kernvox помогает быстро оценить состояние серверов без открытия полноценной системы мониторинга на рабочем компьютере. Основной сценарий: администратор открывает приложение, видит список узлов, их доступность и ключевые метрики, а затем при необходимости переходит к деталям конкретного сервера.

Приложение не собирает метрики самостоятельно. Этим занимается KernvoxHub. Android-клиент отвечает за подключение к хабу, получение данных, отображение статусов и выполнение защищенных действий через API.

<a id="ru-purpose"></a>
## Задача проекта

Проект решает практическую задачу мобильного наблюдения за небольшой серверной инфраструктурой:

- показать, какие серверы доступны прямо сейчас;
- отобразить CPU, RAM, диск, сеть, аптайм и время последней метрики;
- дать доступ к списку процессов и истории метрик;
- позволить отправить команду перезагрузки через KernvoxHub;
- хранить ключи подключения и настройки приложения безопасно на устройстве.

<a id="ru-features"></a>
## Возможности

- Экран приветствия для первого запуска.
- Настройка адреса KernvoxHub, API-ключа и отдельного ключа действия.
- Список серверов с онлайн/оффлайн-статусом и краткими метриками.
- Pull-to-refresh для ручного обновления данных.
- Детальный экран сервера с вкладками "Обзор", "Процессы" и "История".
- Отображение CPU, RAM, диска, сети, аптайма, адреса и пользователя.
- Просмотр процессов сервера, если данные доступны через KernvoxHub.
- История метрик сервера.
- Отправка команды перезагрузки сервера с подтверждением.
- Светлая, темная и системная темы.
- Локальная блокировка приложения паролем.
- Биометрическая разблокировка, если она доступна на устройстве.
- Автоблокировка после возврата в приложение.
- Режим скрытности для маскирования IP-адресов и имен пользователей.
- Пользовательские сообщения об ошибках сети, авторизации и API.

<a id="ru-architecture"></a>
## Архитектура

Проект состоит из двух компонентов: серверного хаба и Android-клиента.

```text
┌─────────────────┐        REST / JSON        ┌─────────────────┐
│   KernvoxHub    │ ────────────────────────> │     Kernvox     │
│  server backend │                           │ Android client  │
└─────────────────┘                           └─────────────────┘
        │                                             │
        │ collects metrics                            │ displays data
        │ and runs actions                            │ and user actions
        ▼                                             ▼
 monitored servers                              mobile interface
```

Внутри Android-приложения используется MVVM-подход:

- `ui` — экраны и компоненты Jetpack Compose;
- `viewmodel` — состояние экранов и обработка пользовательских действий;
- `data/repository` — слой доступа к данным;
- `data/network` — Ktor-клиент и вызовы API;
- `data/storage` — DataStore, настройки и безопасное хранение секретов;
- `auth` — блокировка приложения и биометрия.

<a id="ru-api"></a>
## API KernvoxHub

Android-клиент ожидает, что KernvoxHub предоставляет следующие эндпоинты:

| Метод | Эндпоинт | Назначение |
| --- | --- | --- |
| `GET` | `/api/v1/android/dashboard` | Сводка по всем серверам |
| `GET` | `/api/v1/android/servers/{id}/details` | Детальная информация о сервере |
| `GET` | `/api/v1/android/servers/{id}/processes?limit=50` | Список процессов сервера |
| `GET` | `/api/v1/android/servers/{id}/metrics/history?limit=100` | История метрик |
| `POST` | `/api/v1/servers/{id}/actions/reboot` | Команда перезагрузки сервера |

Для чтения данных используется заголовок `X-API-Key`. Для опасных действий, например перезагрузки, используется отдельный заголовок `X-Action-Key`.

<a id="ru-tech-stack"></a>
## Технологии

| Область | Используется |
| --- | --- |
| Язык | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Навигация | Navigation Compose |
| Архитектура | MVVM |
| Сеть | Ktor Client, OkHttp |
| JSON | kotlinx.serialization |
| Хранение настроек | Preferences DataStore |
| Безопасность | Android Keystore, AES/GCM, PBKDF2 |
| Биометрия | AndroidX Biometric |
| Сборка | Gradle Kotlin DSL |
| Минимальная версия Android | API 24, Android 7.0 |
| Target / Compile SDK | API 36 |
| JVM bytecode | Java 11 |

Основные версии зависимостей находятся в `gradle/libs.versions.toml`.

<a id="ru-setup"></a>
## Установка и запуск

### Требования

- Android Studio с поддержкой актуального Android Gradle Plugin.
- JDK 17 для сборки проекта.
- Android SDK Platform 36.
- Эмулятор или устройство с Android 7.0, API 24, или новее.
- Запущенный KernvoxHub с настроенным API-ключом.

### Клонирование

```bash
git clone https://github.com/Vennilay/Kernvox.git
cd Kernvox
```

### Запуск в Android Studio

1. Откройте проект в Android Studio.
2. Дождитесь Gradle Sync.
3. Запустите приложение на эмуляторе или физическом устройстве.
4. На экране настроек укажите URL KernvoxHub и `X-API-Key`.
5. При необходимости укажите `X-Action-Key` для перезагрузки серверов.

Для локального KernvoxHub в Android Emulator обычно используется адрес вида `http://10.0.2.2:<port>`. HTTP разрешен для debug-сборок и локальных адресов разработки. В release-сборке для обычных серверов требуется HTTPS.

<a id="ru-build"></a>
## Сборка и тесты

| Команда | Назначение |
| --- | --- |
| `./gradlew assembleDebug` | Собрать debug APK |
| `./gradlew installDebug` | Установить debug APK на подключенное устройство |
| `./gradlew assembleRelease` | Собрать release APK с R8/ProGuard |
| `./gradlew test` | Запустить unit-тесты |
| `./gradlew connectedAndroidTest` | Запустить instrumentation-тесты на устройстве или эмуляторе |
| `./gradlew clean` | Очистить build-артефакты |

Release-сборка включает минификацию и shrink resources.

<a id="ru-security"></a>
## Безопасность

- API-ключ и ключ действия сохраняются в Preferences DataStore только в зашифрованном виде.
- Шифрование секретов выполняется через AES/GCM/NoPadding.
- AES-ключ создается и хранится в Android Keystore.
- Пароль локальной блокировки не хранится открытым текстом: сохраняются случайный salt и PBKDF2-хеш.
- Биометрия используется только как дополнительный способ разблокировки после включения пароля.
- Перезагрузка сервера требует отдельного ключа действия и дополнительного подтверждения.
- В release-сборке обычные подключения к KernvoxHub должны использовать HTTPS.
- Android backup отключен, чтобы настройки и секреты приложения не попадали в резервные копии.

<a id="ru-status"></a>
## Статус и ограничения

Текущая версия готова для демонстрации как Android-клиент мониторинга: приложение подключается к KernvoxHub, отображает список серверов, детали, процессы, историю метрик и поддерживает защищенную отправку команды перезагрузки.

Ограничения текущей версии:

- серверы добавляются и редактируются на стороне KernvoxHub, а не в Android-приложении;
- графики, группировка серверов и экспорт данных не реализованы;
- для полноценной работы требуется развернутый KernvoxHub.

<a id="ru-license"></a>
## Лицензия

Проект распространяется по лицензии MIT. Текст лицензии находится в файле [LICENSE](LICENSE).

<a id="ru-contacts"></a>
## Контакты

- GitHub: [Vennilay/Kernvox](https://github.com/Vennilay/Kernvox)
- Связанный проект: [Vennilay/KernvoxHub](https://github.com/Vennilay/KernvoxHub)

🇷🇺 [К выбору языка](#language)

---

<a id="readme-en"></a>
# Kernvox — Android Server Monitoring Client

Kernvox is an Android application for system administrators and DevOps engineers. The app connects to the KernvoxHub backend, reads server data through a REST API, and displays infrastructure status in a mobile interface.

The project was developed as part of Samsung Academy.

**Related repositories:**

- [Kernvox](https://github.com/Vennilay/Kernvox) — Android client, this repository.
- [KernvoxHub](https://github.com/Vennilay/KernvoxHub) — backend hub that collects metrics and exposes the API.

## Contents

- [About](#en-about)
- [Project Purpose](#en-purpose)
- [Features](#en-features)
- [Architecture](#en-architecture)
- [KernvoxHub API](#en-api)
- [Tech Stack](#en-tech-stack)
- [Setup and Run](#en-setup)
- [Build and Tests](#en-build)
- [Security](#en-security)
- [Status and Limitations](#en-status)
- [License](#en-license)
- [Contacts](#en-contacts)
- 🇷🇺 [Русская версия](#readme-ru)

<a id="en-about"></a>
## About

Kernvox helps administrators quickly check server health without opening a full monitoring dashboard on a workstation. The main workflow is simple: open the app, review the server list, check availability and key metrics, then open server details when more context is needed.

The app does not collect metrics directly. KernvoxHub handles metric collection. The Android client is responsible for hub connection, data loading, status visualization, and protected API actions.

<a id="en-purpose"></a>
## Project Purpose

The project solves a practical mobile monitoring task for small server infrastructures:

- show which servers are currently available;
- display CPU, RAM, disk, network, uptime, and the last metric timestamp;
- provide access to server processes and metric history;
- send a reboot command through KernvoxHub;
- store connection keys and app settings safely on the device.

<a id="en-features"></a>
## Features

- First-launch welcome screen.
- Configuration for KernvoxHub URL, API key, and separate action key.
- Server list with online/offline status and short metrics.
- Pull-to-refresh for manual data updates.
- Server detail screen with Overview, Processes, and History tabs.
- Display of CPU, RAM, disk, network, uptime, address, and username.
- Server process list when available through KernvoxHub.
- Server metric history.
- Reboot command with confirmation.
- Light, dark, and system theme modes.
- Local app lock with password.
- Biometric unlock when supported by the device.
- Auto-lock after returning to the app.
- Privacy mode for masking IP addresses and usernames.
- User-friendly handling of network, authorization, and API errors.

<a id="en-architecture"></a>
## Architecture

The project consists of two components: the backend hub and the Android client.

```text
┌─────────────────┐        REST / JSON        ┌─────────────────┐
│   KernvoxHub    │ ────────────────────────> │     Kernvox     │
│  server backend │                           │ Android client  │
└─────────────────┘                           └─────────────────┘
        │                                             │
        │ collects metrics                            │ displays data
        │ and runs actions                            │ and user actions
        ▼                                             ▼
 monitored servers                              mobile interface
```

The Android app follows an MVVM-style structure:

- `ui` — Jetpack Compose screens and components;
- `viewmodel` — screen state and user action handling;
- `data/repository` — data access layer;
- `data/network` — Ktor client and API calls;
- `data/storage` — DataStore, settings, and secure secret storage;
- `auth` — app lock and biometric authentication.

<a id="en-api"></a>
## KernvoxHub API

The Android client expects KernvoxHub to expose these endpoints:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/android/dashboard` | Dashboard summary for all servers |
| `GET` | `/api/v1/android/servers/{id}/details` | Detailed server information |
| `GET` | `/api/v1/android/servers/{id}/processes?limit=50` | Server process list |
| `GET` | `/api/v1/android/servers/{id}/metrics/history?limit=100` | Metric history |
| `POST` | `/api/v1/servers/{id}/actions/reboot` | Server reboot command |

Read requests use the `X-API-Key` header. Dangerous actions, such as reboot, use a separate `X-Action-Key` header.

<a id="en-tech-stack"></a>
## Tech Stack

| Area | Used |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Architecture | MVVM |
| Networking | Ktor Client, OkHttp |
| JSON | kotlinx.serialization |
| Settings storage | Preferences DataStore |
| Security | Android Keystore, AES/GCM, PBKDF2 |
| Biometrics | AndroidX Biometric |
| Build system | Gradle Kotlin DSL |
| Minimum Android version | API 24, Android 7.0 |
| Target / Compile SDK | API 36 |
| JVM bytecode | Java 11 |

Main dependency versions are defined in `gradle/libs.versions.toml`.

<a id="en-setup"></a>
## Setup and Run

### Requirements

- Android Studio with support for the current Android Gradle Plugin.
- JDK 17 for building the project.
- Android SDK Platform 36.
- Emulator or physical device with Android 7.0, API 24, or newer.
- Running KernvoxHub instance with a configured API key.

### Clone

```bash
git clone https://github.com/Vennilay/Kernvox.git
cd Kernvox
```

### Run in Android Studio

1. Open the project in Android Studio.
2. Wait for Gradle Sync to finish.
3. Run the app on an emulator or a physical device.
4. Open Settings and enter the KernvoxHub URL and `X-API-Key`.
5. Optionally enter `X-Action-Key` to enable server reboot actions.

For a local KernvoxHub running from the Android Emulator, use a URL like `http://10.0.2.2:<port>`. HTTP is allowed for debug builds and local development hosts. Release builds require HTTPS for normal server URLs.

<a id="en-build"></a>
## Build and Tests

| Command | Purpose |
| --- | --- |
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew installDebug` | Install debug APK on a connected device |
| `./gradlew assembleRelease` | Build release APK with R8/ProGuard |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumentation tests on a device or emulator |
| `./gradlew clean` | Clean build artifacts |

Release builds enable code minification and resource shrinking.

<a id="en-security"></a>
## Security

- The API key and action key are stored in Preferences DataStore only in encrypted form.
- Secret encryption uses AES/GCM/NoPadding.
- The AES key is generated and stored in Android Keystore.
- The local app-lock password is never stored as plain text: the app stores a random salt and a PBKDF2 hash.
- Biometrics are used only as an additional unlock method after password lock is enabled.
- Server reboot requires a separate action key and an extra confirmation step.
- Release builds require HTTPS for regular KernvoxHub connections.
- Android backup is disabled so app settings and secrets are not included in backups.

<a id="en-status"></a>
## Status and Limitations

The current version is ready to demonstrate as an Android monitoring client: it connects to KernvoxHub, shows the server list, details, processes, metric history, and supports protected reboot commands.

Current limitations:

- servers are added and edited through KernvoxHub, not inside the Android app;
- charts, server grouping, and data export are not implemented;
- a deployed KernvoxHub instance is required for full functionality.

<a id="en-license"></a>
## License

The project is distributed under the MIT License. See [LICENSE](LICENSE) for details.

<a id="en-contacts"></a>
## Contacts

- GitHub: [Vennilay/Kernvox](https://github.com/Vennilay/Kernvox)
- Related project: [Vennilay/KernvoxHub](https://github.com/Vennilay/KernvoxHub)

🇬🇧 [Back to language selection](#language)
