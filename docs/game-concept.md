# Game Concept – Shooter Engine (Top-Down twin-stick)

## 1. Założenia ogólne
Szybka gra akcji typu Top-Down Shooter, skupiająca się na dynamicznej walce, przetrwaniu kolejnych fal przeciwników i eksploracji mrocznych poziomów. Silnik został zaprojektowany z myślą o modowalności i łatwym tworzeniu map przez JSON.

## 2. Świat gry (Mapa)
- **Struktura**: Poziomy budowane z kafelków (Tile-based) z dynamicznym oświetleniem 2D.
- **Interakcja**: Przesuwalne obiekty (beczki, skrzynie), niszczalne elementy otoczenia, interaktywne drzwi i przyciski.
- **Strefy**: Mapy zawierają triggery aktywujące dźwięki, muzykę lub komunikaty fabularne.

## 3. Encje (Entity)
- **Gracz**: Rozbudowany ekwipunek, wiele rodzajów broni, system zbierania amunicji i apteczek.
- **Przeciwnicy**: Zaawansowane AI wykorzystujące Pathfinding A*, Steering Behaviors oraz system fal (Waves).
- **Efekty**: System cząsteczek (iskry, łuski), system krwi (trwałe plamy na podłożu) oraz dynamiczne cienie.

## 4. System walki

### Arsenał
- **Bronie palne**: Od pistoletów po Rail-guny i wyrzutnie rakiet, każda z unikalnymi parametrami (spread, projectiles per shot, fire rate).
- **Walka wręcz**: Możliwość atakowania nożem (system kolizji stożkowej).
- **System Magazynków**: Realistyczne przeładowanie i ograniczona amunicja (z wyjątkami dla broni podstawowych).

### Pociski
- Niezależne encje z własną fizyką i zachowaniem (np. pociski wybuchowe, przenikające).

## 5. Sterowanie i Atmosfera
- **Ruch**: Klawisze WSAD.
- **Celowanie**: Mysz (niezależny kursor celownika).
- **Klimat**: Dynamiczna muzyka zmieniająca się w zależności od strefy mapy, bogate efekty dźwiękowe otoczenia.
