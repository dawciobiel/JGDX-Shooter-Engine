# Game Concept – 2D / pseudo-3D shooter

## 1. Założenia ogólne
Gra typu shooter, w której gracz porusza się po mapie i walczy z przeciwnikami.

## 2. Świat gry (Mapa)
- Mapa może być:
    - statyczna (zamknięta przestrzeń)
    - generowana dynamicznie
- Mapa zawiera obiekty:
    - gracz
    - przeciwnicy
    - obiekty neutralne

## 3. Encje (Entity)
Każda encja posiada parametry:
- zdrowie
- pancerz
- prędkość ruchu
- inne statystyki

Typy encji:
- Gracz
- Przeciwnik
- Obiekt neutralny

## 4. System walki

### Broń
Każda broń posiada:
- prędkość strzału
- czas przeładowania
- zasięg
- pojemność magazynka

### Pociski
- są niezależnymi encjami
- posiadają:
    - obrażenia
    - prędkość
- znikają po czasie lub kolizji

## 5. Sterowanie
- ruch: klawiatura
- celowanie: mysz
- celownik porusza się niezależnie od postaci