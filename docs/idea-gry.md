Kodować grę zaczniemy moze jutro. Póki co napisz mi proszę w skrócie jak projektować silnik gry zręcznośćiowej. Pomysł na silnik do gry typu: 
- Obiekt (gracz) się porusza po mapie.
- po mapie poruszają się obiekty "przeciwnik". "przeciwnik" może poruszać się w dowolnym kierunku oraz strzelać w kierunku gracza.
- Mapa. Powiem szczerze że nie wymyśliłem jeszcze czy mapa będzie zamkniętą przestrzenią (z góry ustalonym kształcie) czy może będzie generowana na bierząco w dowolnym kierunku. Załóżmy, że oba tryby mogą być możliwe do realizacji.
- Każdy obiekt na mapie ("gracz", "przeciwnik", "obiekt neutralny") powinien mieć swoje parametry. Na przykład: życie, prędkość poruszania się, pancerz, jakieś dodatkowe parametry które będzie można zdefiniować później. 
- Wydaje mi się, że silnik będzie udostępniał różne klasy obiektów. Na przykład: "gracz", "przeciwnik", "obiekt neutralny". Przy czym na przykład "przeciwnik" powinien być posiadać podklasy np. "potwór", "człowiek", "pojazd" i tym podobne. Tak samo podklasy mogą mieć kolejne pod-klasy np. "potwór zombie", "potwór  smok", "człowiek biały", "człowiek czarny" itp.
- broń. Wydaje mi się, że każdy obiekt ("gracz", "przeciwnik", "obiekt neutralny") powinien móc posiadać broń. Broń powinna mieć swoje parametry. Na przykład "prędkość strzału", "prędkość przeładowania", "ilość zadawanych obrażeń", "pojemność magazynka", "maksymalna odległość strzału" itp.
- pocisk wystrzelony z broni. Wydaje mi się, że chyba również to powinien być oddzielny byt w grze. Pojawia się i ostatecznie znika w odpowiednim czasie. Więc może to pocisk powinien mieć pewne swoje parametry typu "ilość zadawnych obrażeń", "prędkość poruszania się".

Sterownanie obiektu "gracz" klawiszami. Celowanie kursorem za pomocą myszy. Celwnik może się poruszać dowolnie po ekranie. Tam gdzie jest celownik to w tym kierunku będzie oddany strzał.

