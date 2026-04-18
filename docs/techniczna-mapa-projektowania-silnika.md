Poniżej techniczna mapa projektowania silnika dla gry zręcznościowej (2D, top-down / twin-stick).

---

# 1. Architektura bazowa

### ➤ ECS (Entity Component System)
* **Entity** – unikalne ID.
* **Component** – czyste dane (np. `TransformComponent`, `HealthComponent`).
* **System** – logika operująca na zestawach komponentów (np. `MovementSystem`).

### ➤ Centralna Komunikacja (Event Bus)
Systemy komunikują się asynchronicznie za pomocą zdarzeń (np. `ShootEvent`, `HitEvent`, `DeathEvent`, `TriggerEvent`). Pozwala to na całkowite odseparowanie logiki (np. walki) od efektów (np. dźwięku czy cząsteczek).

---

# 2. Kluczowe Komponenty

### Ruch i Pozycja
* `TransformComponent`: {x, y, rotation}
* `VelocityComponent`: {vx, vy}
* `SteeringComponent`: Parametry dla algorytmów Steering Behaviors.

### Walka i Statystyki
* `HealthComponent`: {hp, maxHp, isDead, corpseDuration, hasBlood}
* `WeaponComponent`: {damage, fireRate, ammo, spread, projectilesPerShot, type}
* `ProjectileComponent`: {ownerId, damage, lifetime, behavior, explosionRadius}
* `InventoryComponent`: Zarządzanie listą broni i amunicją.

### Wizualizacja i Efekty
* `RenderComponent`: Proste kształty (koło, prostokąt, kolor, alpha).
* `TextureComponent`: Statyczne sprite'y z automatycznym rozwiązywaniem ścieżek.
* `AnimationComponent`: Animacje klatkowe (obsługa arkuszy SHEET lub pojedynczych plików).
* `LightComponent`: {radius, color, intensity} — Dynamiczne oświetlenie 2D.
* `ParticleComponent`: Czas życia i zachowanie cząsteczek.

### Logika i Interakcja
* `AIComponent`: Stan maszyny stanów przeciwnika i ścieżka (Pathfinding).
* `TriggerComponent`: Definiowanie stref akcji (dźwięki, wiadomości, teleporty) z parametrami `isLooping` i `volume`.
* `ColliderComponent`: Promień kolizji fizycznej.
* `NameComponent`: Wyświetlana nazwa jednostki.
* `SoundComponent`: Mapowanie akcji (HIT, DIE, SHOOT) na konkretne pliki dźwiękowe.

---

# 3. Systemy (Logika gry)

* `InputSystem`: Przetwarza wejście gracza na zdarzenia ruchu i walki.
* `AISystem` & `PathfindingSystem`: Zaawansowane AI oparte na grafie węzłów i algorytmie A*.
* `CollisionSystem`: Wykrywanie i reagowanie na kolizje (fizyka kołowa).
* `DamageSystem`: Obsługa obrażeń, punktacji, generowania przedmiotów (drop) i systemu zwłok.
* `RenderSystem`: Wielopasmowy proces renderowania (Scene Pass -> Light Pass -> Final Mix z shaderem).
* `LightSystem`: Dynamiczne generowanie mapy świateł (LightMap).
* `AmbientSoundSystem`: Zarządzanie muzyką i pętlami tła w zależności od terenu i triggerów.
* `WaveSystem`: Dynamiczne spawnowanie fal przeciwników wokół gracza.
* `TriggerSystem`: Obsługa interakcji gracza ze strefami akcji na mapie.

---

# 4. System Stanów (GameState Management)

Silnik zarządza stosem stanów (`GameStateManager`):
* `MenuState`: Główne menu gry z dynamicznym odkrywaniem map.
* `PlayState`: Główna pętla rozgrywki (izolacja zasobów mapy).
* `PauseState`: Nakładka pauzy z możliwością powrotu do menu (czyści cały stos stanów).

---

# 5. Konfiguracja i Dane (Data-Driven)

### ➤ Engine Configuration (`engine_config.json`)
Silnik używa centralnego pliku konfiguracyjnego do zarządzania parametrami bez konieczności rekompilacji:
* **Paths**: Definicje wszystkich ścieżek do zasobów (maps, configs, core, shared, textures, shaders).
* **Graphics**: Rozdzielczość, Fullscreen, Target FPS, Ambient Brightness.
* **Audio**: Głośność Master/SFX/Music.
* **Debug**: Flagi widoczności hitboksów, FPS i ścieżek AI.
* **Controls**: Mapowanie klawiszy (moveUp, weaponSwitch, etc.).

### ➤ Architektura Map (Asset Isolation)
Każda mapa jest niezależną jednostką przechowywaną w `assets/maps/[map_name]/`:
* `map.json`: Definicja kafelków, punktów startowych i statycznych encji.
* `configs/weapons/`: Lokalne parametry broni (np. inne obrażenia na danej mapie).
* `entities/`: Lokalne definicje jednostek i triggerów.
* `audio/` & `graphics/`: Tekstury i dźwięki specyficzne dla mapy.

### ➤ Współdzielone zasoby (`assets/shared/`)
Encje i zasoby używane na wielu mapach (np. standardowi przeciwnicy) znajdują się w folderze `shared`.

---

# 6. Oświetlenie i Shadery

Silnik implementuje oświetlenie oparte na shaderach i Frame Buffer Objects (FBO):
1. **Pass 1**: Renderowanie całej sceny do `sceneFbo`.
2. **Pass 2**: Renderowanie świateł (z `LightComponent`) do `lightMapFbo`.
3. **Pass 3**: Miksowanie obu buforów w shaderze `lighting.frag` (Multiply Blending).
Ścieżki do shaderów są konfigurowalne w `engine_config.json`.

---

# 7. Zarządzanie Zasobami (Asset Handling)

* **AssetService**: Inteligentny resolver ścieżek. Priorytetyzuje zasoby lokalne mapy przed zasobami `core` i `shared`.
* **AudioService**: Zarządza dźwiękami i muzyką. Każda mapa posiada własną instancję serwisu, która jest całkowicie zwalniana (`dispose()`) przy zmianie poziomu, co zapobiega wyciekom dźwięku ("ghost audio").
* **JsonService**: Udostępnia ujednolicony singleton `ObjectMapper` dla całego silnika.

---

# 8. Plany Rozwoju (Roadmap)

1. **Zarządzanie Pamięcią**: Optymalizacja rendererów i analiza wycieków tekstur.
2. **Dostrajanie Fizyki**: Stabilizacja kolizji obiektów przesuwalnych.
3. **Profiler Wydajności**: Wbudowane narzędzie do pomiaru obciążenia systemów ECS.
4. **Splash Screen**: Profesjonalny ekran ładowania z paskiem postępu.
5. **System Zapisu**: Trwałość postępów gracza i statystyk.
