
2. Nie możemy mieć w kodzie Java wpisanych na sztywno ścieżek i nazw plików .json do wczytania. Wyjątkiem są konfigi `default_config.json`, `user_config.json`.

3. Plik `assets/config/weapons.json` to powinien być indywidualnym plikiem wewnątrz katalogu mapy. A nie plikiem ogólnym dla całego silnika.
   Użycie tej ściezki występuje w ConfigService.java:17

5. Ściezki do katalogów i plików które są w dokumentacji README.md i docs/*.md są nieaktualne. Należy te ścieżki w tej dokumentacji poprawić.