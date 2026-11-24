# Sim AI (Android)

Projet Android natif **Kotlin + Jetpack Compose**, prêt pour le Google Play Store.

Application **Sim AI** (package `com.fabrice.simai`) : un chat unique qui s'appuie sur un serveur **OpenCode**
(via son API HTTP), avec historique des conversations, style Vert & Noir, moderne et mobile-first.

## Architecture

```
app/src/main/java/com/fabrice/simai/
├── MainActivity.kt                 # Navigation : écran d'accueil -> Chat -> Paramètres
├── SimAiApplication.kt
├── ui/
│   ├── screens/                    # WelcomeScreen, ChatScreen, SettingsScreen
│   ├── components/                 # MarkdownText (code + bouton Copier), ChatBubble, ConversationItem, ModelPicker
│   └── theme/                      # Palette Vert & Noir (Color.kt, Theme.kt)
├── viewmodel/                      # ChatViewModel, SettingsViewModel
├── data/
│   ├── api/OpenCodeApiClient.kt    # Client HTTP vers l'API OpenCode (/api/model, /api/session, /api/session/{id}/prompt, ...)
│   ├── db/                         # Room : ConversationEntity, MessageEntity, DAOs, SimAiDatabase
│   ├── SettingsRepository.kt       # URL du serveur + modèle sélectionné
│   └── model/                      # Conversation, Message, ModelInfo
└── util/SecureStorage.kt           # Clé API stockée dans EncryptedSharedPreferences (Tink)
```

## Fonctionnalités

- **Chat unique** : posez une question, Sim AI répond (via l'API OpenCode).
- **Réponses en français** (fichiers `strings.xml`).
- **Historique** : drawer (gauche) avec la liste des conversations, "Nouvelle conversation" et
  "Supprimer la conversation".
- **Paramètres** : coller la clé API OpenCode, adresse du serveur (`http://localhost:4096` par défaut),
  sélection du modèle (liste récupérée depuis `GET /api/model`).
- **Code** : le Markdown rendu affiche les blocs de code avec un bouton **Copier**.

## Prérequis

- Serveur OpenCode démarré avec `opencode --server` (ou localhost:4096), si vous voulez utiliser
  l'appli ; l'appli fonctionne aussi sans envoi d'erreur la première fois.
- Android Studio / JDK 17, Android SDK (API 34).
- clé API OpenCode dans l'écran Paramètres (stockée de façon sécurisée sur l'appareil).

## Build

```bash
./gradlew wrapper        # une seule fois : génère le wrapper Gradle
./gradlew assembleDebug  # APK debug
./gradlew bundleRelease   # AAB pour le Google Play Store (signedRelease via keystore)
```

Notes Play Store :
- VersionName `1.0`, VersionCode `1` — à incrémenter à chaque upload.
- L'icône utilise l'icône adaptative (`res/mipmap-anydpi-v26/`).
- Historique sauvegardé avec les règles de `res/xml/backup_rules.xml`.

## Sécurité de la clé API

La clé API est stockée dans `EncryptedSharedPreferences` (nom : `simai_secure_settings`,
fichier `shared_prefs/simai_secure_settings.xml`), chiffrée par Tink avec un `MasterKey`
(AES256_GCM_HKDF_SHA1). Elle n'est jamais écrite en clair sur le disque.
