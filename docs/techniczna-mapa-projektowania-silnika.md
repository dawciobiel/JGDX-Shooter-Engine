Poniżej skrócona, techniczna mapa projektowania silnika dla takiej gry zręcznościowej (2D, top-down / twin-stick).

---

# 1. Architektura bazowa

Najważniejsza decyzja: **dziedziczenie vs kompozycja**

Twoja propozycja (drzewo klas typu `Przeciwnik -> Potwór -> Zombie`) szybko prowadzi do problemów. Lepsze podejście:

### ➤ ECS (Entity Component System) — rekomendowane

* **Entity** – tylko ID (np. `int id`)
* **Component** – dane (brak logiki)
* **System** – logika operująca na komponentach

Przykład:

* Entity: `Enemy#123`
* Components:

  * `Position`
  * `Velocity`
  * `Health`
  * `Weapon`
  * `AI`
* Systems:

  * `MovementSystem`
  * `CombatSystem`
  * `AISystem`

➡️ Dzięki temu:

* nie potrzebujesz dziesiątek klas typu `ZombieWithGunFast`
* łatwo dodajesz nowe zachowania przez komponenty

---

# 2. Podstawowe komponenty

Minimalny zestaw:

### Transform / ruch

```java
Position { float x, y }
Velocity { float vx, vy }
Rotation { float angle }
```

### Statystyki

```java
Health { float hp, maxHp }
Stats { speed, armor, ... }
```

### Walka

```java
Weapon {
    damage
    fireRate
    reloadTime
    projectileSpeed
}
```

### Pociski

```java
Projectile {
    damage
    lifetime
    speed
    ownerId
}
```

### AI

```java
AI {
    type (aggressive, passive, ranged)
}
```

---

# 3. Systemy (logika gry)

Każdy system działa co frame:

### MovementSystem

* aktualizuje pozycję na podstawie velocity

### InputSystem

* tylko dla gracza
* klawiatura → velocity
* mysz → rotation

### CombatSystem

* tworzy pociski
* obsługuje cooldown strzałów

### ProjectileSystem

* poruszanie pocisków
* usuwanie po czasie (`lifetime`)

### CollisionSystem

* wykrywanie kolizji:

  * pocisk vs obiekt
  * gracz vs przeciwnik
* zgłaszanie eventów

### DamageSystem

* odejmowanie HP
* śmierć → event

### AISystem

* sterowanie przeciwnikami:

  * podążanie za graczem
  * strzelanie

---

# 4. Eventy (bardzo ważne)

Zamiast bezpośrednich wywołań:

```java
EventBus.publish(new DamageEvent(targetId, damage));
```

Typowe eventy:

* `ShootEvent`
* `HitEvent`
* `DeathEvent`
* `SpawnEvent`

➡️ Zalety:

* brak silnych zależności między systemami
* łatwe debugowanie
* rozszerzalność

---

# 5. Mapa (dwa tryby)

Zaprojektuj wspólny interfejs:

```java
interface World {
    List<Entity> getEntitiesInArea(...)
}
```

### Tryb 1: mapa statyczna

* tilemap (np. grid)
* kolizje z mapą

### Tryb 2: mapa generowana

* chunki (np. 16x16)
* generacja on-demand
* cache + usuwanie dalekich chunków

➡️ Silnik powinien nie wiedzieć, jaki tryb jest używany.

---

# 6. Pociski jako byty

Masz rację — **pocisk to osobna encja**

Dlaczego:

* ma własny lifecycle
* kolizje
* różne typy (laser, rakieta, aoe)

---

# 7. Sterowanie

### Gracz:

* WASD → `Velocity`
* mysz → `Rotation`
* klik → `ShootEvent`

---

# 8. Menadżery vs Systemy

Zamiast „managerów”:

* używaj **systemów ECS**

Ale możesz mieć:

### Core services:

* `EventBus`
* `EntityManager`
* `ResourceManager` (assety)
* `WorldManager` (mapa)

---

# 9. Game loop

Klasyczny loop:

```java
while (running) {
    inputSystem.update()
    aiSystem.update()
    movementSystem.update()
    collisionSystem.update()
    combatSystem.update()
    projectileSystem.update()
    render()
}
```

➡️ Kolejność ma znaczenie (np. kolizje po ruchu)

---

# 10. Rozszerzalność

Zadbaj od początku o:

* data-driven (JSON/YAML na przeciwników, bronie)
* brak hardcodu typów (`Zombie`, `Smok` itd.)
* zamiast tego:

  ```json
  {
    "type": "enemy",
    "hp": 100,
    "weapon": "rifle",
    "ai": "aggressive"
  }
  ```

---

# 11. Najczęstsze błędy

* ❌ za dużo dziedziczenia
* ❌ logika w obiektach zamiast w systemach
* ❌ brak separacji danych i logiki
* ❌ brak eventów (tight coupling)
* ❌ jeden „God Manager”

---

# 12. Minimalny MVP (kolejność implementacji)

1. Entity + Component system
2. Movement
3. Rendering
4. Input (gracz)
5. Strzelanie (bez AI)
6. Pociski
7. Kolizje
8. HP / śmierć
9. AI przeciwników
10. Mapy

---
