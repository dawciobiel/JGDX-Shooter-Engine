# Architektura Zasobów i Konfiguracji (Wizja)

Niniejszy dokument opisuje nową, modułową strukturę katalogów oraz hierarchię konfiguracji silnika JGDX-Shooter-Engine. Celem jest całkowita separacja warstwy silnika, logiki gry oraz danych poziomów.

## 1. Struktura Katalogów Projektu

```text
project-root/
│
├── config/                 # KONFIGURACJA SILNIKA I UŻYTKOWNIKA (zewnętrzna względem assets/)
│   ├── engine.json         # Główne parametry silnika (ścieżki, limity encji, debug)
│   ├── rendering.json      # Parametry renderowania (FBO, shadery, oświetlenie globalne)
│   ├── physics.json        # Fizyka (kolizje, współczynniki tarcia, stabilność)
│   └── input.json          # Ustawienia sprzętowe użytkownika (czułość myszy, rebindy klawiszy)
│
├── assets/                 # ZASOBY I DANE GRY (runtime assets)
│   │
│   ├── global/             # Dane współdzielone przez wszystkie mapy
│   │   ├── textures/       # Tekstury bazowe, atlasy
│   │   ├── audio/          # Dźwięki sfx, muzyka
│   │   ├── fonts/          # Czcionki
│   │   ├── shaders/        # Kod GLSL shaderów
│   │   ├── ui/             # Skórki (skins), ikony HUD
│   │   ├── materials/      # Definicje materiałów (oświetlenie, właściwości powierzchni)
│   │   ├── particles/      # Systemy cząsteczek (eksplozje, dym, krew)
│   │   ├── prefabs/        # Szablony encji ECS (reusable game objects)
│   │   └── config/         # Konfiguracja Gameplayu i Gry (domyślne mapowania)
│   │       ├── game.json   # Zasady gry, progi punktowe, balans
│   │       └── input.json  # Domyślne mapowanie akcji (np. SHOOT -> LMB)
│   │
│   ├── maps/               # Dane poziomów
│   │   ├── map_name/
│   │   │   ├── map.json    # Dane kafelków, warstw i geometria (Tilemap)
│   │   │   ├── entities.json # Specyficzna dla mapy lista instancji encji i triggerów
│   │   │   ├── config.json # Parametry poziomu (oświetlenie, atmosfera, balans mapy)
│   │   │   └── local/      # Zasoby binarne unikalne dla mapy (nadpisują globalne)
│   │   │       ├── textures/
│   │   │       └── audio/
│   │   └── ...
│   │
│   └── dev/                # Zasoby testowe i deweloperskie (wykluczane z builda)
│
├── src/                    # KOD ŹRÓDŁOWY
│   ├── engine/             # Logika silnika (framework)
│   └── game/               # Logika konkretnej gry (content)
│
└── build.gradle.kts
```

## 2. Standard Plików JSON (Data Versioning)

Wszystkie pliki konfiguracyjne i dane (w tym mapy i prefaby) muszą stosować ujednolicony schemat wersjonowania.

**Schemat:**
```json
{
  "version": 1,
  "data": { 
    "param1": "value",
    "param2": 123
  }
}
```

*   `version`: Liczba całkowita określająca wersję schematu danych.
*   `data`: Obiekt zawierający właściwą konfigurację/dane.

## 3. Hierarchia Konfiguracji

System konfiguracji zostaje rozbity na trzy niezależne warstwy ładowania:

### A. Poziom Silnika i Użytkownika (Engine/User Level)
* **Lokalizacja**: `config/*.json`
* **Metoda ładowania**: `Gdx.files.local()` (zewnętrzne pliki systemu plików).

### B. Poziom Gry i Gameplayu (Game Level)
* **Lokalizacja**: `assets/global/config/*.json`
* **Metoda ładowania**: `Gdx.files.internal()`.

### C. Poziom Mapy (Map Level)
* **Lokalizacja**: `assets/maps/map_name/*.json`
* **Metoda ładowania**: `Gdx.files.internal()`.

## 4. System Rozwiązywania Zasobów (Asset Resolver)

Mechanizm wyszukiwania zasobów binarnych (tekstur, dźwięków) w `AssetService` działa kaskadowo:
1. **Lokalny folder mapy**: `assets/maps/map_name/local/textures/name.png`
2. **Globalny folder zasobów**: `assets/global/textures/name.png` (Fallback)

## 5. Korzyści

1. **Industry Standards**: Zastosowanie folderów `prefabs/` i `materials/` przybliża silnik do standardów rynkowych.
2. **Modular Content**: Mapy nie przechowują logiki, a jedynie dane o układzie świata.
3. **Robust Data**: Wersjonowanie pozwala na bezpieczną ewolucję formatów plików.
