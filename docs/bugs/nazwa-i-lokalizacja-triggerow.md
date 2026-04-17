Kolejna sprawa to konwencja nazewnicza oraz drzewo katalogów dla plików konfiguracyjnych `assets/configs/entities/`:

```log
trigger_fire.json
trigger_metal.json
trigger_mud.json
trigger_music_change.json
trigger_water.json
```

Każdy z definicji tych triggerów ma kod podobny do poniższego:
```json
{
  "components": {
    "Transform": {},
    "Collider": { "radius": 250 },
    "Trigger": { "type": "AMBIENT_SOUND", "value": "assets/audio/ambience/fire_loop.wav" }
  }
}
```

Czyli jest to trigger który ma typ "AMBIENT_SOUND".
Sama konstrukcja jest poprawna i słuszna. Jednak są to konkretne triggery dla konkretnej mapy. A więc wydaje mi się, że nie powinny znajdować się w katalogu silnika tylko w katalogu konkretnej mapy.


Być może już sama nazwa dla plików json które są konfigami postaci wroga powinny mieć w nazwie pliku jakiś prefix np. "enemy_"
lub wszystkie konfigi od wroga powinny być w odpowiednim podkatalogu. Chodzi tutaj o porządek, segregowanie i łatwość dostepu. Skąd programista po samej nazwie i położeniu obecnie ma wiedzieć od czego jest plik "zombie.json" i "door.json"?
Podobnie konwencja nazewnicza prefix lub podkatalog dla obiektów na mapie beczka i skrzynka. Albo wrzucić je do dedykowanego podkatalogu albo dodać do nazwy pliku prefix "map_object_" - albo jedno i drugie.

