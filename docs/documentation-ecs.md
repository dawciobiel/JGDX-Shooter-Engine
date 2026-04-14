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
    - `WeaponComponent`: Parametry ataku, typy broni i system amunicji.
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
5. `CombatSystem`: Zarządzanie bronią, przeładowaniem i wczytywaniem parametrów z `weapons.json`.
6. `ProjectileSystem`: Czas życia pocisków.
7. `ParticleUpdateSystem`: Animacja cząsteczek.
8. `MovementSystem`: Fizyka ruchu z logiką kolizji z mapą.
9. `MapSystem`: Granice mapy.
10. `CollisionSystem`: Wykrywanie trafień, kolizji obszarowych (wybuchy) oraz podnoszenia przedmiotów (pickups).
11. `DamageSystem`: HP, efekty krwi, scoring i logika Friendly Fire.
12. `RenderSystem`: Rysowanie świata z korektą orientacji grafik i cieniowaniem (FBO).
13. `UISystem`: HUD.

## 3. Zaawansowane Funkcje

### System Broni (Data-Driven)
Parametry wszystkich broni są zdefiniowane w pliku `assets/config/weapons.json`.
- Pozwala na konfigurację obrażeń, szybkostrzelności, rozrzutu i zachowania pocisku.
- **Specjalne zachowania pocisków:**
    - `NORMAL`: Standardowy pocisk znikający po trafieniu.
    - `EXPLOSIVE`: Zadaje obrażenia obszarowe w zadanym promieniu (np. Rakieta).
    - `PIERCING`: Przebija wrogów i leci dalej, aż trafi w ścianę (np. RailGun).

### Dynamiczne Przeszkody
Silnik wspiera zniszczalne obiekty, które blokują ruch.
- `DestructibleComponent` pozwala na niszczenie elementów otoczenia.
- Jeśli obiekt posiada `ObstacleComponent`, `NavigationGraph` automatycznie usuwa te kafelki z dostępnych ścieżek AI. Po zniszczeniu obiektu, ścieżki są przeliczane ponownie.

### System Walki i Friendly Fire
- Pociski posiadają `ownerId`, co pozwala rozróżnić kto oddał strzał.
- `DamageSystem` zapobiega ranieniu sojuszników (np. zombie przez zombie).
- Pociski przelatują przez sojuszników bez ich ranienia i bez znikania, co pozwala na walkę w grupie.

## 4. Zasoby i Dane (Data-Driven)
- **Encje:** Definiowane w `assets/entities/`.
- **Bronie:** Definiowane w `assets/config/weapons.json`.
- **Konfiguracja Silnika:** `assets/config/default_config.json` oraz `user_config.json`.
- **Aliasy Komponentów:** `Transform`, `Render`, `Health`, `AI`, `Weapon`, `Velocity`, `Collider`, `Score`, `AmmoPickup`, `HealthPickup`.
