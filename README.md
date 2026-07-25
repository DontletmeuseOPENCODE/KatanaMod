# KatanaMod
A plugin for Paper 1.20.2 for adding custom Katanas with unique effects and models.

## Dostępne bronie:
- **Wakizashi** (Trucizna na 5 sekund przy uderzeniu)
- **Tantō** (Teleportacja za przeciwnika, 5 sek. cooldown)
- **Tachi** (Dash do przodu na PPM, 20 sek. cooldown)
- **Ōdachi** (Pajęczyna pod przeciwnikiem na PPM, 30 sek. cooldown)
- **Chisa-katana** (Zamrożenie 5s + Ślepota 10s przy uderzeniu)
- **Shuriken** (Rzucana broń, podpala na 1s, wraca przy chybieniu, max 16 w stosie)

## Jak pobrać i wgrać moda? (Tutorial)

### Część 1: Plugin (Serwer)
1. Pobierz lub zbuduj najnowszy plik `KatanaMod-<wersja>.jar` (komenda `./gradlew build` w głównym folderze). Plik znajdziesz w katalogu `build/libs`.
    2. Umieść plik `.jar` w folderze `plugins/` swojego serwera Minecraft (wymagany silnik Paper 1.20.2 oraz Java 17).
    3. Zrestartuj serwer (lub użyj komendy `/reload confirm`).
    4. Wszystkie wersje dostepne w /build/libs (opcjonalnie)

    ### Część 2: Resource Pack (Klient)
    Aby miecze miały customowe tekstury zamiast wyglądu zwykłego żelaznego miecza:
    1. Skopiuj folder paczki `KatanaModTexturePack` do katalogu `%appdata%/.minecraft/resourcepacks/` (Windows) lub `~/.minecraft/resourcepacks/` (Linux/Mac).
    2. Uruchom Minecrafta, wejdź w **Opcje** -> **Paczki Zasobów** i przerzuć paczkę z lewej na prawą stronę.
    3. Wciśnij **Gotowe**.

    ### Część 3: W grze
    Komendy administratorskie (wymagana permisja `katanamod.admin`, z automatu mają ją operatorzy):
    - `/katana give <TwójNick> <nazwa katany>

    Możesz też zdobyć je poprzez crafting:
    - Wakizashi: 2 żelazne sztabki na górze, 1 patyk na dole.
    - Tantō: 2 miedziane sztabki na górze, 1 patyk na dole.
