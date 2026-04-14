# Dokumentacja Techniczna: Silnik Shooter (Architektura & API)

Ten dokument stanowi kompendium wiedzy o architekturze silnika, skierowane do programistów open-source rozwijających projekt.

## 1. Architektura Core
Silnik opiera się na trzech filarach: **ECS (Entity Component System)**, **EventBus** oraz **Centralnym Orkiestratorze**.

### Engine (`pl.shooter.engine.Engine`)
Główna klasa zarządzająca cyklem życia gry.
- `update(float deltaTime)`: Wywołuje wszystkie systemy w zdefiniowanej kolejności.
- `getEntityManager()`: Dostęp do bazy danych encji.
- `getEventBus()`: Dostęp do systemu zdarzeń.

## 2. System ECS

### Encje i Komponenty
- **Entity**: Tylko unikalne ID.
- **Component**: Klasy POJO przechowujące czyste dane.
    - `TransformComponent`: Pozycja (x, y) i rotacja.
    - `VelocityComponent`: Prędkość wektorowa.
    - `HealthComponent`: Statystyki żywotności.
    - `RenderComponent`: Dane o wyglądzie (kolor, rozmiar).
    - `TextureComponent`: Ścieżka do grafiki PNG.
    - `AIComponent`: Definicja zachowania bota z parametrami taktycznymi.
    - `WeaponComponent`: Parametry ataku, typy broni, system amunicji oraz **zasięg walki wręcz (`range`)**.
    - `ProjectileComponent`: Cykl życia pocisku, obrażenia i specjalne zachowania (Explosive/Piercing).
    - `ParticleComponent`: Efekty wizualne (zanikanie).
    - `ScoreComponent`: Wynik punktowy gracza.
    - `SteeringComponent`: Integracja z `gdx-ai`.
    - `DestructibleComponent`: Obiekt możliwy do zniszczenia (skrzynie, krzaki).
    - `ObstacleComponent`: Encja blokująca ruch i ścieżki AI.
    - `AmmoPickupComponent` / `HealthPickupComponent`: Przedmioty do podniesienia.

### Systemy (`pl.shooter.engine.ecs.systems`)
Kolejność wywołań w pętli gry:
1. `InputSystem`: Sterowanie graczem i celowanie.
2. `PathfindingSystem`: Dynamiczne wyznaczanie ścieżek A*.
3. `AISystem`: Decyzje przeciwników.
4. `SteeringSystem`: Kinematyczny ruch na podstawie decyzji AI.
5. **`CombatSystem`**: Zarządzanie bronią palną i **walką wręcz (Melee)**. Obsługuje przeładowanie i wczytywanie parametrów z `weapons.json`.
6. `ProjectileSystem`: Czas życia pocisków.
7. `ParticleUpdateSystem`: Animacja cząsteczek.
8. `MovementSystem`: Fizyka ruchu z logiką kolizji z mapą.
9. `MapSystem`: Granice mapy.
10. `CollisionSystem`: Wykrywanie trafień, kolizji obszarowych (wybuchy) oraz podnoszenia przedmiotów (pickups).
11. `DamageSystem`: HP, efekty krwi, scoring i logika Friendly Fire.
12. `RenderSystem`: Rysowanie świata z korektą orientacji grafik i cieniowaniem (FBO) przy użyciu shaderów.
13. **`UISystem`**: HUD (Paski zdrowia, punkty, wave, **ikony broni**).

## 3. Zaawansowane Funkcje

### System Broni i Melee
Parametry wszystkich broni są zdefiniowane w pliku `assets/config/weapons.json`.
- **System Fallback:** Silnik automatycznie szuka dźwięków i ikon w dedykowanych folderach broni. Jeśli ich nie znajdzie, używa zasobów z folderu `assets/audio/sfx/weapons/default/`.
- **Walka Wręcz (Melee):** Bronie typu `KNIFE` nie generują pocisków. `CombatSystem` sprawdza obecność wrogów w zasięgu `range` i w kącie ~90 stopni przed atakującym.

### System Ładowania Zasobów (Asset Pipeline)
- **AudioService:** Zarządza dźwiękami z mechanizmem fallback.
- **AssetService:** Wykorzystuje `AssetManager` do asynchronicznego ładowania tekstur.

## 4. Zasoby i Dane (Data-Driven)
- **Struktura Katalogów:**
    - `assets/audio/sfx/`: Efekty dźwiękowe (characters, weapons, ui).
    - `assets/graphics/textures/`: Tekstury postaci, broni, UI.
    - `assets/graphics/shaders/`: Pliki `.vert` i `.frag`.
- **Encje:** Definiowane w `assets/entities/`.
- **Bronie:** Definiowane w `assets/config/weapons.json`.
- **Konfiguracja Silnika:** `assets/config/default_config.json` oraz `user_config.json`.
- **Aliasy Komponentów:** `Transform`, `Render`, `Health`, `AI`, `Weapon`, `Velocity`, `Collider`, `Score`, `AmmoPickup`, `HealthPickup`.
