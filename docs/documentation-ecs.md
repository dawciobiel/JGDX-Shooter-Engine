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
    - `AIComponent`: Definicja zachowania bota (CHASE, STATIONARY).
    - `WeaponComponent`: Parametry ataku i typy broni (PISTOL, SHOTGUN, MACHINE_GUN).
    - `ProjectileComponent`: Cykl życia pocisku.
    - `ParticleComponent`: Efekty wizualne (fading/scaling).
    - `ScoreComponent`: Wynik punktowy gracza.

### Systemy (`pl.shooter.engine.ecs.systems`)
Kolejność wywołań w pętli gry:
1. `InputSystem`: Czyta WASD, przelicza pozycję myszy na świat (unproject) i publikuje `ShootEvent`.
2. `AISystem`: Steruje wrogami (pogoń za graczem, automatyczny strzał).
3. `CombatSystem`: Reaguje na `ShootEvent`, sprawdza cooldown i tworzy encje pocisków.
4. `ProjectileSystem`: Odpowiada za czas życia pocisków.
5. `ParticleUpdateSystem`: Animuje (zanikanie) i usuwa cząsteczki.
6. `MovementSystem`: Fizyka ruchu (poz = poz + vel * dt).
7. `MapSystem`: Pilnuje, aby obiekty nie opuszczały granic mapy.
8. `CollisionSystem`: Wykrywa kolizje kołowe, publikuje `HitEvent`.
9. `DamageSystem`: Reaguje na `HitEvent` (odejmowanie HP, śmierć, spawn efektów cząsteczkowych).
10. `RenderSystem`: Kamera śledząca gracza, rysowanie mapy i wszystkich obiektów (prymitywy lub tekstury).
11. `UISystem`: Wyświetla HUD (HP, Wynik) na stałej pozycji ekranu.

## 3. Komunikacja (EventBus)
Systemy komunikują się asynchronicznie.
- `ShootEvent`: Publikowany przez `InputSystem` lub `AISystem`.
- `HitEvent`: Publikowany przez `CollisionSystem`.
- `ScoreEvent`: Publikowany przez `DamageSystem` po eliminacji wroga.

## 4. Zasoby i Dane (Data-Driven)
Encje są definiowane w plikach JSON w folderze `assets/entities/`.
- `EntityFactory.loadFromJson("assets/entities/zombie.json", x, y)`: Automatycznie tworzy encję i przypisuje jej komponenty na podstawie kluczy (aliasów) zdefiniowanych w JSON.
- **Aliasy:** Zamiast pełnych nazw klas, w JSON używamy skrótów: `Transform`, `Render`, `Health`, `AI`, `Weapon`, `Velocity`, `Collider`.

## 5. System Mapy
- **GameMap / StaticMap**: Definiuje świat gry.
- **ProceduralMap**: System oparty na chunkach (wycinkach 16x16), generujący świat w czasie rzeczywistym.
- **MapSystem**: Zapewnia, że obiekty pozostają wewnątrz granic `GameMap`.
