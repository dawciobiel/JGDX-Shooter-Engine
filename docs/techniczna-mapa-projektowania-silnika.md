Poniżej techniczna mapa projektowania silnika dla gry zręcznościowej (2D, top-down / twin-stick).

---

# 1. Architektura bazowa

### ➤ ECS (Entity Component System)
* **Entity** – unikalne ID.
* **Component** – czyste dane (np. `TransformComponent`, `HealthComponent`).
* **System** – logika operująca na zestawach komponentów (np. `MovementSystem`).

---

# 2. Kluczowe Komponenty

### Ruch i Pozycja
* `TransformComponent`: {x, y, rotation, z (height)}
* `VelocityComponent`: {vx, vy}

### Walka i Statystyki
* `HealthComponent`: {hp, maxHp}
* `WeaponComponent`: {damage, fireRate, ammo}
* `ProjectileComponent`: {ownerId, damage, lifetime}

### Wizualizacja i Efekty
* `RenderComponent`: Proste kształty (koło, prostokąt).
* `TextureComponent`: Statyczne sprite'y.
* `AnimationComponent`: Animacje klatkowe.
* `LightComponent`: {radius, color, intensity} — **NOWE**

---

# 3. Systemy (Logika gry)

* `InputSystem`: Przetwarza wejście gracza na ruch i zdarzenia strzału.
* `AISystem`: Steruje przeciwnikami (obecnie proste podążanie, w planach Pathfinding).
* `CollisionSystem`: Wykrywanie kolizji kołowych (AABB w planach).
* `DamageSystem`: Obsługa obrażeń i usuwanie martwych jednostek.
* `RenderSystem`: Wielopasmowy proces renderowania (Scene Pass -> Light Pass -> Final Mix).
* `LightSystem`: Generowanie mapy świateł (LightMap) przy użyciu FBO i Additive Blending. — **NOWE**

---

# 4. System Stanów (GameState Management)

Silnik zarządza stosem stanów (`GameStateManager`):
* `MenuState`: Główne menu gry.
* `PlayState`: Główna pętla rozgrywki.
* `PauseState`: Nakładka pauzy (renderowana nad PlayState). — **NOWE**

---

# 5. Konfiguracja i Dane (Data-Driven)

### ➤ Game Configuration (GameConfig) — **NOWE**
Silnik używa plików JSON do zarządzania ustawieniami:
* **Lokalizacja**: `assets/config/default_config.json` (domyślne) oraz `user_config.json` (lokalne nadpisania).
* **Obszary**:
    * `graphics`: Rozdzielczość, FPS, jasność otoczenia (Ambient Light).
    * `audio`: Głośność master/sfx/music.
    * `debug`: Flagi dla hitboksów, FPS i trybu nieśmiertelności.
* **Technologia**: Jackson (ObjectMapper) do szybkiej serializacji POJO.

---

# 6. Oświetlenie (Advanced 2D Lighting) — **NOWE**

Silnik implementuje oświetlenie oparte na shaderach i Frame Buffer Objects (FBO):
1. **Pass 1**: Renderowanie całej sceny do `sceneFbo`.
2. **Pass 2**: Renderowanie świateł (z `LightComponent`) do `lightMapFbo` (Additive Blending).
3. **Pass 3**: Miksowanie obu buforów w shaderze `lighting.frag` (Multiply Blending).
4. **Ambient Light**: Możliwość sterowania jasnością i kolorem tła przez konfigurację.

---

# 7. Komunikacja (Event Bus)

Systemy komunikują się za pomocą zdarzeń:
* `ShootEvent`, `HitEvent`, `PickupEvent`, `DeathEvent`.
Pozwala to na całkowite odseparowanie np. systemu dźwięku od systemu walki.

---

# 8. Plany Rozwoju (Roadmap)

1. **Pathfinding (A*)**: Zaawansowane AI omijające przeszkody.
2. **System Zapisu**: Trwałość statystyk i postępu.
3. **Particle System Improvements**: Bardziej zaawansowane efekty wizualne.
4. **Pseudo-3D Support**: Wykorzystanie kanału `z` w oświetleniu i sortowanie warstw.
