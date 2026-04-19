# Architektura Systemu Map (Data-Driven & Isolated)

Dokument opisuje standard definiowania poziomów w silniku JGDX-Shooter. Każda mapa jest kompletnym, odizolowanym pakietem zasobów zamkniętym w dedykowanym podkatalogu w `assets/maps/[map_name]/`.

---

## 1. Struktura Katalogu Mapy (Asset Isolation)

Każda mapa jest niezależnym kontenerem. Dzięki temu ta sama nazwa pliku (np. `zombie.json`) może mieć zupełnie inne parametry na różnych mapach.

```text
assets/maps/[map_name]/
├── map.json                # Główna definicja warstw, gracza i statycznych encji
├── configs/
│   └── weapons/            # Lokalne nadpisania parametrów broni dla tej mapy
├── entities/
│   ├── enemies/            # Lokalne definicje przeciwników
│   ├── triggers/           # Lokalne definicje stref akcji (triggery)
│   └── objects/            # Lokalne definicje obiektów (beczki, skrzynie)
├── audio/
│   ├── ambience/           # Dźwięki tła specyficzne dla mapy
│   ├── music/              # Muzyka specyficzna dla mapy
│   └── sfx/                # Efekty dźwiękowe (np. taunty, dźwięki otoczenia)
└── graphics/
    └── textures/           # Tekstury, atlasy kafelków i sprite'y mapy
```

---

## 2. Logika ładowania (LoadingState)

Przełączanie map powinno odbywać się za pośrednictwem `LoadingState`, co zapewnia płynność UI i wyświetlenie paska postępu podczas pre-loadingu zasobów (tekstur, shaderów, audio).

Przykład wywołania zmiany mapy:
```java
gsm.setAbsoluteState(new LoadingState(gsm, new PlayState(gsm, mapPath), "LOADING LEVEL..."));
```

---

## 3. Struktura Pliku `map.json`

Silnik wspiera mapy oparte na kafelkach (Tilemaps) z obsługą atlasów.

```json
{
  "id": "testing_room",
  "name": "Pokój Testowy",
  "settings": {
    "width": 1600,
    "height": 1600,
    "ambientColor": { "r": 0.1, "g": 0.1, "b": 0.2, "a": 1.0 }
  },
  "tileLayer": {
    "tilesetPath": "graphics/textures/tileset_industrial.png",
    "tileSize": 32,
    "displaySize": 64,
    "tileData": [
      [0, 0, 1, 1],
      [2, 2, 0, 0]
    ]
  },
  "playerSpawn": { "x": 800, "y": 800 },
  "entities": [
    { "type": "barrel_red", "x": 450, "y": 300 },
    { "type": "trigger_ambient_forest", "x": 600, "y": 600 }
  ]
}
```

---

## 4. Logika Rozwiązywania Ścieżek (AssetService)

Silnik stosuje politykę **Map-First**:
1. Najpierw szuka zasobu w folderze aktualnie wczytanej mapy (`assets/maps/[current]/...`).
2. Jeśli nie znajdzie, szuka w folderze `assets/shared/` (lub `assets/core/`).
3. Ostatecznym fallbackiem są zasoby systemowe.

---

## 5. Konfiguracja Broni (Map-Specific Weapons)

Przy wczytywaniu mapy, `ConfigService` automatycznie ładuje pliki JSON z folderu `configs/weapons/` danej mapy. Pozwala to na pełne zbalansowanie rozgrywki per poziom.

---

## 6. System Triggerów na Mapie

Triggery są definiowane jako encje z komponentem `TriggerComponent`. Obsługiwane akcje:
- `AMBIENT_SOUND`: Uruchamia pętlę dźwiękową (parametry `isLooping`, `volume`).
- `STOP_AMBIENT`: Ucisza wszystkie pętle ambientowe.
- `MUSIC_CHANGE`: Zmienia ścieżkę muzyczną.
- `MESSAGE`: Wyświetla komunikat na ekranie (UI).
- `TRAP`: Aktywuje pułapkę.
