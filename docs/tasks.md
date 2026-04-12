# Lista zadań - Silnik Gry Shooter (Architektura ECS)

## Etap 1: Fundamenty ECS i Core (ZAKOŃCZONE)
- [x] Implementacja `EntityManager`
- [x] Implementacja bazowej klasy `Component` i `GameSystem`
- [x] Implementacja `EventBus`
- [x] Główna klasa `Engine`

## Etap 2: Podstawowe Komponenty i Ruch (ZAKOŃCZONE)
- [x] Komponenty: `TransformComponent`, `VelocityComponent`
- [x] `MovementSystem`: fizyka ruchu
- [x] `InputSystem`: sterowanie WASD
- [x] `RenderSystem`: podstawowe rysowanie

## Etap 3: Walka i System Pocisków (ZAKOŃCZONE)
- [x] Komponent `Weapon`: cooldown, prędkość pocisku
- [x] Komponent `Projectile`: czas życia
- [x] `CombatSystem`: tworzenie pocisków
- [x] `ProjectileSystem`: czyszczenie pocisków
- [x] `InputSystem`: celowanie 360° i LPM

## Etap 4: Kolizje i Statystyki (ZAKOŃCZONE)
- [x] Komponent `Collider` i `Health`
- [x] `CollisionSystem`: wykrywanie trafień
- [x] `DamageSystem`: HP, śmierć, eventy

## Etap 5: AI, Efekty i Dane (ZAKOŃCZONE)
- [x] Komponent `AI` (CHASE, STATIONARY)
- [x] `AISystem`: logika wrogów
- [x] `ParticleSystem`: efekty wybuchów i iskrzenia
- [x] System ładowania danych z JSON (EntityFactory z aliasami)

## Etap 6: Świat i Mapa (ZAKOŃCZONE)
- [x] Interfejs `GameMap` i `StaticMap`
- [x] `ProceduralMap`: system chunków
- [x] Kamera podążająca za graczem
- [x] `MapSystem`: ograniczanie ruchu do granic mapy

## Etap 7: Nowe Funkcjonalności (W TRAKCIE)
- [x] Różne rodzaje broni (Pistol, Shotgun, Machine Gun)
- [x] Paski zdrowia (Health Bars) nad głowami encji
- [x] System ładowania Assetów (`AssetService`)
- [ ] **Menu główne i stany gry** (Scene2D buttons)
- [ ] **UI/HUD** (Licznik punktów na stałe na ekranie)
- [ ] **System Animacji** (Obracanie tekstur, klatki animacji)
- [ ] **Dźwięki** (Integracja `AudioService` z systemami)
