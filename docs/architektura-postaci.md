# Architektura Postaci (Hybrid Rendering System)

Dokument opisuje system tworzenia i konfiguracji postaci (gracza, przeciwników, NPC) w silniku JGDX-Shooter. System ten opiera się na hybrydowym modelu renderowania, pozwalającym na dynamiczne przełączanie między grafiką proceduralną a sprite'ową.

---

## 1. Koncepcja Hybrydowa

Silnik wspiera dwa tryby wizualizacji postaci, definiowane na poziomie konfiguracji JSON (Prefab):

1.  **Tryb Proceduralny (`procedural`)**: Postać jest rysowana przy użyciu prymitywów geometrycznych (`ShapeRenderer`). Idealny do prototypowania, testów oraz gier o minimalistycznym stylu. Nie wymaga plików graficznych.
2.  **Tryb Sprite'owy (`sprite`)**: Tradycyjny system oparty na teksturach i animacjach klatkowych. Wymaga zdefiniowania atlasów lub pojedynczych plików PNG.

---

## 2. Struktura Pliku Prefaba (`.json`)

Każda postać jest definiowana przez plik JSON znajdujący się w `assets/global/prefabs/characters/` lub lokalnie w folderze mapy.

### Przykład: Wróg Proceduralny
```json
{
  "version": 1,
  "data": {
    "name": "Robo-Guard",
    "stats": {
      "health": 150,
      "speed": 120,
      "radius": 18
    },
    "visuals": {
      "style": "procedural",
      "procedural": {
        "primaryColor": "#8A2BE2",
        "accentColor": "#FFD700",
        "radius": 18,
        "useWeaponGraphic": true
      }
    }
  }
}
```

### Przykład: Wróg Sprite'owy
```json
{
  "version": 1,
  "data": {
    "name": "Zombie Runner",
    "stats": { "health": 50, "speed": 200, "radius": 16 },
    "visuals": {
      "style": "sprite",
      "texturePath": "characters/zombie/idle_0.png",
      "frameWidth": 64,
      "frameHeight": 64,
      "animations": {
        "WALK": { 
          "path": "characters/zombie/move", 
          "type": "FILES", 
          "count": 16, 
          "frameDuration": 0.05 
        }
      }
    }
  }
}
```

---

## 3. Parametry Wizualne

### Sekcja `visuals`
*   `style`: `procedural` lub `sprite`.
*   `texturePath`: Domyślna tekstura (używana jako placeholder lub ikona w UI).
*   `frameWidth` / `frameHeight`: Wymiary klatki (tylko dla `sprite`).
*   `animations`: Mapa animacji (IDLE, WALK, SHOOT, HIT, DIE).

### Sekcja `procedural` (tylko dla stylu proceduralnego)
*   `primaryColor`: Kolor główny ciała (format HEX).
*   `accentColor`: Kolor detali/oczu/broni (format HEX).
*   `radius`: Promień rysowania.
*   `useWeaponGraphic`: Czy rysować linię reprezentującą broń.

---

## 4. Implementacja Techniczna (ECS)

System korzysta ze wzorca **Strategii** poprzez komponenty:

### CharacterRendererComponent
Przechowuje instancję interfejsu `CharacterRenderer`. `EntityFactory` decyduje, którą implementację stworzyć:
*   `ProceduralCharacterRenderer`: Używa `ShapeRenderer` do rysowania kół i linii.
*   `SpriteCharacterRenderer`: Używa `SpriteBatch` do rysowania tekstur z `AnimationComponent`.

### AnimationComponent
Zarządza stanem animacji (`stateTime`, `currentState`). Nawet w trybie proceduralnym, komponent ten dostarcza informacji o stanie (np. czy postać idzie), co pozwala na dodanie efektów takich jak "bobbing" (kołysanie) figur geometrycznych.

---

## 5. Przepływ Tworzenia Postaci

1.  **Definicja**: Stwórz plik JSON w folderze prefabs.
2.  **Assety**: Dodaj grafiki (dla `sprite`) lub określ kolory (dla `procedural`).
3.  **Spawnowanie**: Dodaj wpis do `entities.json` mapy lub wywołaj `entityFactory.createEnemy(path, x, y)`.
4.  **Renderowanie**: `RenderSystem` automatycznie pobierze renderer z komponentu i wyświetli postać zgodnie z jej konfiguracją.

---

## 6. Rozszerzanie Systemu
Aby dodać nowy typ renderowania (np. szkieletowy Spine lub 3D), należy:
1. Stworzyć nową klasę implementującą `CharacterRenderer`.
2. Zaktualizować `CharacterRendererFactory`, aby rozpoznawał nową wartość pola `style`.
