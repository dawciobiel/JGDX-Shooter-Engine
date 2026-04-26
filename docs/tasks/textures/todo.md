# Textures — todo list

## Zastanowić się jak konkretnie mają być prezentowane i zbudowane modele w grze.

a) Model 2D
  - Jeżeli tak to czy jedna klatka ma przedstawiać gotowy obraz postaci przedstawiony z konkretnej perspektywy?
  - czy może jednak klatka ma być połączeniem nałożonych na siebie warstw postaci: 
    - stopy
    - nogi
    - tułów
    - ramiona + ręce + broń
    - głowa
    - hełm

  Jeżeli tak, to każdą z tych warstw można byłoby obracać w kierunku dowolnego punktu na ekranie 2D. Czyli w kierunku położenia celownika.

b) Model 3D.
  W takim przypadku dochodzi problem generowania poszczególne klatki animacji postaci. Trzeba by było implementować przesunięcia/transformacje modelu.
  Ewentualnie na podstawie modelu wygenerować kilka klatek postaci, które można by wyświetlać w formie animacji.
  
  
## W jaki sposób generować tekstury, modele?

a) ręcznie pobierać z internetu
b) generator postaci
   Bez sensu, bo i tak trzeba używać assetów. Więc co najwyżej można próbować używać generatora do tworzenia assetów na podstawie warstw postaci.
   
   Wymagania:
   - trzbea mieć sensowne tekstury dla wielu modelu w tym samym stylu.
   
c) na podstawie warstwowych sprittów generować postać podczas gry
   Ale w takim wypadku i tak wydajniej jest generować ręcznie assety, a potem je używać do animacji.
