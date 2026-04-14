# Lista zadań - Silnik Gry Shooter (Architektura ECS)

## Etap 9: Szlify i Rozwój (W TRAKCIE)
- [x] **Refaktoryzacja struktury assets** (Podział na audio/sfx, graphics/textures, shaders)
- [x] **System Fallback dla zasobów** (Automatyczne ładowanie domyślnych dźwięków i ikon broni)
- [x] **Broń Melee: Nóż (Knife)** (Implementacja walki wręcz, zasięgu ataku i ikony w HUD)
- [x] **Orientacja wyświetlanej grafiki** (Poprawne wyświetlanie tekstur postaci i broni)
- [x] **Rozbudowany System Broni** (9 typów broni, inwentarz, brak resetu amunicji)
- [x] **Dynamiczna Zmiana Broni** (Klawisze 1-9, Q/E, konfigurowalne)
- [x] **Obsługa Fullscreen i Ustawień Ekranu** (Wczytywanie szerokości, wysokości i trybu pełnoekranowego z plików konfiguracyjnych)
- [ ] **System Animacji Śmierci** (Wrogowie nie znikają natychmiast)
- [ ] **Różne typy przeciwników** (Szybsi biegacze, pancerni wrogowie)
- [ ] **System Drzwi i Przełączników** (Prosta interakcja ze światem)
- [x] **Wyświetlanie HitBox** (W konfigu gry jest opcja "showHitboxes")
- [x] **Wyświetlanie FPS** (W konfigu gry jest opcja "showFps")
- [ ] **Zapisywanie Wyników** (Highscores do pliku)

## Etap 8: Rozgrywka i Detale (ZAKOŃCZONE)
- [x] **System Przeszkód i Terenu** (Spowolnienie w wodzie/błocie, kolorowanie mapy)
- [x] **Dropy przedmiotów** (Apteczki, Amunicja z wrogów)
- [x] **Logika Fal** (Automatyczny spawn, progresja trudności)
- [x] **Efekty cząsteczkowe** (Krew przy trafieniu, łuski pocisków)
- [x] **Przeszkody możliwe do zniszczenia** (Zniszczalne skrzynie i lekkie zarośla)
- [x] **Brak Friendly Fire** (Pociski przelatują przez sojuszników)
- [x] **Poprawa obliczania punktów** (Punkty tylko za własne zabójstwa)

## Etap 7: Rozszerzona Mechanika i UI (ZAKOŃCZONE)
- [x] Różne rodzaje broni (Pistol, Shotgun, Machine Gun)
- [x] Paski zdrowia (Health Bars) nad głowami encji i w HUD
- [x] System ładowania Assetów (`AssetService` i `AudioService`)
- [x] **Menu główne i stany gry** (Scene2D buttons, Game Over, Restart)
- [x] **UI/HUD** (Licznik punktów, Kills, Wave, Ammo, Reload Bar, Ikony broni)
- [x] **System Animacji** (Dynamiczny, Data-Driven, Sprite Sheets & Files)
- [x] **Dźwięki** (Integracja audio z walką, amunicją i śmiercią)

## Etap 6: Świat i Mapa (ZAKOŃCZONE)
- [x] Interfejs `GameMap` i `StaticMap`
- [x] `ProceduralMap`: system chunków
- [x] Kamera podążająca za graczem
- [x] `MapSystem`: ograniczanie ruchu do granic mapy

## Etap 5: AI, Efekty i Dane (ZAKOŃCZONE)
- [x] Komponent `AI` (CHASE, STATIONARY)
- [x] `AISystem`: logika wrogów
- [x] `ParticleSystem`: efekty wybuchów i iskrzenia
- [x] System ładowania danych z JSON (EntityFactory z aliasami)

## Etap 4: Kolizje i Statystyki (ZAKOŃCZONE)
- [x] Komponent `Collider` i `Health`
- [x] `CollisionSystem`: wykrywanie trafień
- [x] `DamageSystem`: HP, śmierć, eventy

## Etap 3: Walka i System Pocisków (ZAKOŃCZONE)
- [x] Komponent `Weapon`: cooldown, prędkość pocisku
- [x] Komponent `Projectile`: czas życia
- [x] `CombatSystem`: tworzenie pocisków
- [x] `ProjectileSystem`: czyszczenie pocisków
- [x] `InputSystem`: celowanie 360° i LPM

## Etap 2: Podstawowe Komponenty i Ruch (ZAKOŃCZONE)
- [x] Komponent: `TransformComponent`, `VelocityComponent`
- [x] `MovementSystem`: fizyka ruchu
- [x] `InputSystem`: sterowanie WASD
- [x] `RenderSystem`: podstawowe rysowanie

## Etap 1: Fundamenty ECS i Core (ZAKOŃCZONE)
- [x] Implementacja `EntityManager`
- [x] Implementacja bazowej klasy `Component` i `GameSystem`
- [x] Implementacja `EventBus`
- [x] Główna klasa `Engine`
