# Lista zadań — Silnik Gry Shooter (Architektura ECS)

## Etap 11: Optymalizacja i Stabilizacja (W TRAKCIE)
- [x] **Refaktoryzacja kodu (Clean Code)** (Optymalizacja systemów Movement, UI, AI oraz fabryki encji)
- [x] **Ujednolicenie JSON/Serialization** (Zastąpienie wielokrotnych instancji `ObjectMapper` jednym współdzielonym singletonem)
- [x] **Izolacja zasobów Map (Asset Isolation)** (Każda mapa jest niezależnym kontenerem zasobów)
- [ ] **Zarządzanie pamięcią** (Analiza wycieków, optymalizacja SpriteBatch i ShapeRenderer)
- [ ] **Dostrajanie fizyki i kolizji** (Przyjrzeć się kolizjom wrogów z obiektami przesuwalnymi jak beczki/skrzynie pod kątem stabilności)
- [ ] **Profiler Wydajności** (Analiza czasu trwania update() poszczególnych systemów)
- [ ] **Usprawnienie ładowania zasobów** (Splash Screen i pasek postępu)

## Etap 10: Architektura map i jednostek na mapie (ZAKOŃCZONE)
- [x] **Architektura aktywowania jednostek na mapie** (Przeniesiono spawnowanie do JSON mapy przez `MapService`)
- [x] **Architektura map** (System wczytywania `map.json` z obsługą atlasów kafelków i skalowania)
- [x] **Wybór mapy przed rozpoczęciem rozgrywki** (Menu wyboru poziomu)

## Etap 9: Szlify i Rozwój (ZAKOŃCZONE)
... (reszta bez zmian)
