# Notes

Application de notes Android avec un thème **Liquid Glass** (verre liquide) construite avec Jetpack Compose et la librairie [Backdrop](https://github.com/Kyant0/AndroidLiquidGlass) (`io.github.kyant0:backdrop`).

## Fonctionnalités

- Créer, éditer, épingler, colorer et supprimer des notes
- Recherche instantanée dans les titres et contenus
- Filtres : Toutes / Épinglées
- Sauvegarde automatique (DataStore, persistance locale)
- Thème clair / sombre adaptatif
- Interface entièrement « liquid glass » :
  - Barre de recherche en verre (flou + réfraction + vibrance)
  - Barre d'onglets en verre : glissez le doigt dessus pour changer d'onglet (pilule qui suit le doigt + retour haptique)
  - Bouton « + » en verre teinté séparé pour créer une note
  - Fond dégradé statique réfracté à travers le verre (optimisé pour les performances)

## Stack technique

| Élément | Choix |
|---|---|
| Langage | Kotlin 2.4 |
| UI | Jetpack Compose (BOM 2026.08) + Material 3 |
| Effet verre | [Backdrop 2.0.0](https://kyant.gitbook.io/backdrop) — `drawBackdrop`, `vibrancy`, `blur`, `lens` |
| Formes | `io.github.kyant0:shapes` (Capsule) |
| Persistance | DataStore Preferences (JSON via `org.json`) |
| Architecture | MVVM léger (ViewModel + StateFlow) |
| Build | Gradle 9.6, AGP 9.3, minSdk 26, targetSdk 37 |

## Structure

```
app/src/main/java/com/satanas1275/notes/
├── MainActivity.kt
├── NotesApp.kt              # Scaffold racine + backdrop partagé + navigation
├── NotesViewModel.kt        # État UI (liste, recherche, filtres)
├── data/
│   ├── Note.kt              # Modèle + sérialisation JSON
│   └── NotesRepository.kt   # DataStore
└── ui/
    ├── glass/Glass.kt       # GlassSurface / GlassIconButton (effets lens+blur+vibrancy)
    ├── components/MeshBackground.kt
    ├── icons/AppIcons.kt
    ├── theme/Theme.kt
    ├── utils/TimeFormat.kt
    ├── notes/NotesListScreen.kt   # Liste + barre de recherche + barre de navigation
    └── editor/NoteEditorScreen.kt # Éditeur + barre d'actions + sélecteur de couleur
```

### Comment fonctionne l'effet verre

Un seul backdrop est créé à la racine (`rememberLayerBackdrop`). Le fond animé et la liste y sont capturés via `Modifier.layerBackdrop(backdrop)`. Les éléments en verre sont dessinés **au-dessus** du contenu capturé et appliquent :

```kotlin
Modifier.drawBackdrop(
    backdrop = backdrop,
    shape = { Capsule() },
    effects = {
        vibrancy()
        blur(10f.dp.toPx())
        lens(14f.dp.toPx(), 18f.dp.toPx())
    },
    onDrawSurface = { drawRect(containerColor) }
)
```

> Les effets `blur`/`lens` utilisent `RenderEffect` : flou à partir d'Android 12, lentille (shader) à partir d'Android 13. Sur les versions antérieures, seule la teinte de surface est affichée.

## Build local

Prérequis : JDK 17+, Android SDK (API 37).

```bash
./gradlew :app:assembleDebug
```

L'APK est généré dans `app/build/outputs/apk/debug/`.

## Build sur GitHub Actions

Le workflow `.github/workflows/android.yml` :

1. À chaque push sur `main` / PR → compile un **APK debug** (artefact téléchargeable).
2. Si les secrets de signature sont configurés → compile aussi **APK + AAB release signés**.
3. Sur un tag `v*` (ex. `v1.0.0`) → crée une **GitHub Release** avec l'APK et l'AAB.

### Configurer la signature (optionnel, recommandé)

```bash
# Générer un keystore (une seule fois)
keytool -genkeypair -v -keystore release.keystore -alias notes \
  -keyalg RSA -keysize 2048 -validity 10000

# Encoder en base64
base64 -w0 release.keystore   # Linux/macOS
certutil -encode release.keystore encoded.txt   # Windows (copier le contenu sans les lignes d'entête)
```

Ajouter ces secrets dans **Settings → Secrets and variables → Actions** :

| Secret | Valeur |
|---|---|
| `KEYSTORE_BASE64` | Contenu base64 du keystore |
| `KEYSTORE_PASSWORD` | Mot de passe du keystore |
| `KEY_ALIAS` | Alias de la clé (ex. `notes`) |
| `KEY_PASSWORD` | Mot de passe de la clé |

Sans secrets, seuls les builds debug sont produits.
