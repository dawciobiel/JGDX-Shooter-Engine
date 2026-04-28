# Dokumentacja Silnika: System Ekwipunku i Broni

Ten dokument opisuje architekturę oraz proces dodawania nowych broni, amunicji oraz powiązanych z nimi zasobów (grafika, dźwięki) do silnika.

## 1. Struktura Katalogów
System opiera się na spójnej strukturze plików w `assets/global/`.

- **Prefaby Broni**: `assets/global/prefabs/weapons/<nazwa_broni>.json`
- **Prefaby Amunicji**: `assets/global/prefabs/weapons/ammo/<nazwa_amunicji>.json`
- **Prefaby Pocisków**: `assets/global/prefabs/projectiles/<nazwa_pocisku>.json`
- **Ikony HUD**: `assets/global/textures/ui/icons/<ikona>.png`
- **Tekstury broni**: `assets/global/textures/weapons/<nazwa_broni>/<tekstura>.png`
- **Dźwięki**: `assets/global/audio/sfx/weapons/<nazwa_broni>/<akcja>.wav`

## 2. Pliki Konfiguracyjne (JSON)

Każdy plik konfiguracyjny (prefab) musi posiadać strukturę `version` oraz `data`.

### A. Broń (`WeaponPrefab`)
Definiuje statystyki, dźwięki i powiązania z amunicją.
- `id`: Unikalny identyfikator broni.
- `allowedAmmoCategories`: Lista kategorii amunicji obsługiwanych przez broń (np. `["9MM"]`).
- `stats`: FireRate, Spread, MagazineSize, Recoil.
- `audio`: Ścieżki do `shootSound`, `reloadSound`, `emptySound`.

### B. Amunicja (`AmmoPrefab`)
Łączy fizyczny pocisk z kategorią amunicji.
- `id`: Identyfikator amunicji (używany w ekwipunku).
- `category`: Klucz dopasowania do broni (np. `9MM`).
- `projectilePrefabPath`: Ścieżka do prefabu pocisku (`projectiles/bullet_9mm`).

### C. Pocisk (`ProjectilePrefab`)
Definiuje właściwości fizyczne pocisku w świecie gry.
- `baseDamage`: Wartość obrażeń.
- `speed`: Prędkość przemieszczania.
- `texturePath`: Ścieżka do grafiki pocisku.

## 3. Dodawanie broni do mapy
Aby broń była dostępna jako startowe wyposażenie na mapie, należy ją zdefiniować w `assets/maps/<mapa>/config.json` w sekcji `startingEquipment`:

```json
"startingEquipment": {
  "weapons": ["weapons/pistol", "weapons/shotgun"],
  "ammo": { 
      "9mm_regular": 100,
      "shells_regular": 50 
  }
}
```

## 4. Procedura dodawania nowej broni
1. **Pocisk**: Stwórz prefab pocisku w `assets/global/prefabs/projectiles/`.
2. **Amunicja**: Stwórz prefab amunicji w `assets/global/prefabs/weapons/ammo/`. Ustaw odpowiednią kategorię.
3. **Broń**: Stwórz prefab broni w `assets/global/prefabs/weapons/`. Upewnij się, że `allowedAmmoCategories` zgadza się z kategorią amunicji.
4. **Zasoby**: Dodaj pliki graficzne (ikona HUD, tekstura pocisku) oraz dźwięki (shoot, reload, empty).
5. **Konfiguracja mapy**: Dodaj identyfikator broni i amunicji do pliku `config.json` wybranej mapy.

*Uwaga: Brakujące pliki dźwiękowe lub tekstury nie spowodują awarii silnika (silnik zaloguje błąd), co pozwala na szybką identyfikację brakujących zasobów w konsoli.*
