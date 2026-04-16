# Architektura Systemu Map (Data-Driven)

Dokument opisuje standard definiowania poziomów w silniku JGDX-Shooter. Każda mapa jest kompletnym pakietem zasobów zamkniętym w dedykowanym podkatalogu.

---

## 1. Struktura Katalogu Mapy

Każda mapa znajduje się w `assets/maps/[map_name]/`.

- **map.json**: Główny plik konfiguracyjny (definicja ECS, ustawienia, ścieżki).
- **Zasoby lokalne**: Tekstury podłoża, maski kolizji i pliki audio mogą znajdować się bezpośrednio w folderze mapy lub korzystać z zasobów globalnych silnika.

---

## 2. Struktura Pliku `map.json`

Przykład definicji poziomu:

```json
{
  "id": "testing_room",
  "name": "Pokój Testowy",
  "settings": {
    "width": 1600,
    "height": 1600,
    "backgroundTexture": "assets/maps/testing_room/floor.png",
    "ambientColor": { "r": 0.1, "g": 0.1, "b": 0.2, "a": 0.5 },
    "musicTrack": "assets/audio/music/ambient_loop.mp3"
  },
  "playerSpawn": { "x": 800, "y": 800 },
  "entities": [
    { "type": "crate_wood", "x": 400, "y": 400 },
    { "type": "zombie", "x": 1200, "y": 1200 }
  ]
}
```

---

## 3. Logika Wczytywania (MapService)

Silnik używa `MapService`, aby zainicjować poziom:

1. **Wczytanie Konfiguracji**: Parsowanie `map.json` do obiektu `MapConfig`.
2. **Ładowanie Zasobów**: `AssetService` i `AudioService` wczytują pliki wymienione w `settings`.
3. **Inicjalizacja Systemów**:
    - `MapSystem` ustawia granice świata (`width`, `height`).
    - `LightSystem` ustawia kolor otoczenia (`ambientColor`).
4. **Spawnowanie**:
    - Tworzona jest encja gracza w punkcie `playerSpawn`.
    - `EntityFactory` iteruje po tablicy `entities` i tworzy obiekty na mapie.

---

## 4. Zalety Rozwiązania

- **Modularność**: Łatwe dodawanie nowych map poprzez kopiowanie folderów.
- **Dedykowane Zasoby**: Każda mapa może mieć własne tekstury i dźwięki, które nie zaśmiecają globalnych folderów `assets`.
- **Łatwa Edycja**: Zmiana układu obiektów wymaga jedynie edycji pliku tekstowego JSON.
