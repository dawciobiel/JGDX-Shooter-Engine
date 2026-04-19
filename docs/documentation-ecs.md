# Dokumentacja Techniczna: Silnik Shooter (Architektura & API)

Ten dokument stanowi kompendium wiedzy o architekturze silnika, skierowane do programistów rozwijających projekt.

## 1. Architektura Core
Silnik opiera się na trzech filarach: **ECS (Entity Component System)**, **EventBus** oraz **Warstwie Usług (Services)**.

### Engine (`pl.shooter.engine.Engine`)
Główna klasa zarządzająca cyklem życia gry.
- `update(float deltaTime)`: Wywołuje wszystkie systemy w zdefiniowanej kolejności.
- `dispose()`: Gwarantuje poprawne zwolnienie zasobów wszystkich systemów (np. uciszenie audio).
- `getEntityManager()`: Zarządzanie bazą danych encji.
- `getEventBus()`: Centralny system zdarzeń.

## 2. System Stanów (State Management)
Silnik zarządza logiką poprzez stos stanów (`GameStateManager`).

- **MenuState**: Główne menu.
- **PlayState**: Aktywna rozgrywka na mapie.
- **PauseState**: Nakładka pauzy (renderowana nad PlayState).
- **LoadingState**: Generyczny ekran ładowania z paskiem postępu. Przyjmuje `targetState` i automatycznie przełącza się na niego po zakończeniu ładowania zasobów przez `AssetService`.

## 3. System ECS

### Komponenty (`pl.shooter.engine.ecs.components`)
Klasy POJO przechowujące czyste dane:
- `TransformComponent`: Pozycja (x, y) i rotacja.
- `VelocityComponent`: Prędkość wektorowa.
- `HealthComponent`: Statystyki żywotności, system zwłok i kolor krwi.
- `RenderComponent`: Dane o wyglądzie prymitywnym (kolor, rozmiar, alpha).
- `TextureComponent`: Ścieżka do grafiki PNG (rozwiązywana przez AssetService).
- `AnimationComponent`: Zarządzanie klatkami animacji (Sheet/Files).
- `AIComponent`: Maszyna stanów bota i parametry taktyczne.
- `WeaponComponent`: Parametry ataku, typy broni, system amunicji i dźwięki.
- `ProjectileComponent`: Cykl życia pocisku i specjalne zachowania (Explosive/Piercing).
- `SoundComponent`: Mapowanie akcji (HIT, DIE, SHOOT) na pliki audio.
- `TriggerComponent`: Definiowanie stref akcji z parametrami `isLooping` i `volume`.
- `ColliderComponent`: Fizyczny promień kolizji.
- `NameComponent`: Nazwa wyświetlana nad jednostką.
- `InventoryComponent`: Zarządzanie posiadanym uzbrojeniem.

### Systemy (`pl.shooter.engine.ecs.systems`)
Kolejność wywołań w pętli gry (kluczowa dla spójności):
1. `InputSystem`: Sterowanie graczem.
2. `PathfindingSystem`: Wyznaczanie ścieżek A*.
3. `AISystem`: Decyzje botów.
4. `SteeringSystem`: Płynny ruch AI.
5. `CombatSystem`: Logika strzału i przeładowania.
6. `ProjectileSystem`: Fizyka pocisków.
7. `ParticleUpdateSystem`: Efekty wizualne.
8. `PushingSystem`: Fizyka przesuwania obiektów.
9. `InteractionSystem`: Obsługa interakcji gracza z obiektami.
10. `MapSystem`: Granice mapy.
11. `MovementSystem`: Integracja prędkości z pozycją i kolizjami z mapą.
12. `TriggerSystem`: Wykrywanie wejścia/wyjścia ze stref akcji.
13. `CollisionSystem`: Wykrywanie trafień i podnoszenia przedmiotów.
14. `DamageSystem`: HP, efekty krwi i logika śmierci (`DeathEvent`).
15. `SoundSystem`: Odtwarzanie efektów SFX na podstawie zdarzeń.
16. `AmbientSoundSystem`: Zarządzanie muzyką i pętlami tła.
17. `WaveSystem`: Dynamiczne fale przeciwników.
18. `RenderSystem`: Renderowanie świata (Scene -> Lights -> Mix).
19. `UISystem`: HUD, menu i Profiler wydajności.

## 4. Usługi (Services)

- **ConfigService**: Zarządza ładowaniem `engine_config.json` oraz lokalnych konfiguracji broni.
- **AssetService**: Inteligentny resolver ścieżek (Map-First) z asynchronicznym ładowaniem. Wspiera metodę `getProgress()` dla ekranów ładowania.
- **AudioService**: Zarządza dźwiękami i muzyką. Posiada unikalny ID instancji dla bezpiecznego czyszczenia zasobów.
- **JsonService**: Udostępnia globalny singleton `ObjectMapper` (Jackson).
- **MapService**: Orkiestrator wczytywania map i pre-loadingu zasobów encji.

## 5. System Zdarzeń (Event Bus)
Systemy komunikują się asynchronicznie:
- `ShootEvent`, `HitEvent`, `DeathEvent`, `TriggerEvent`, `PickupEvent`, `BulletFiredEvent`, `EmptyWeaponEvent`, `TerrainChangeEvent`, `ScoreEvent`.

## 6. Dane i Konfiguracja (Data-Driven)

### Struktura Zasobów:
- `assets/core/`: Zasoby krytyczne (shadery, bazowe tekstury).
- `assets/maps/[name]/`: Odizolowane zasoby specyficzne dla poziomu.
- `assets/configs/engine_config.json`: Centralne ustawienia silnika i ścieżek.

### Aliasy Komponentów w JSON:
Silnik używa uproszczonych nazw w plikach encji:
`Transform`, `Velocity`, `Render`, `Health`, `AI`, `Weapon`, `Player`, `Texture`, `Score`, `Particle`, `Projectile`, `Collider`, `AmmoPickup`, `HealthPickup`, `Name`, `Trigger`, `Pushable`, `Door`, `Inventory`.
