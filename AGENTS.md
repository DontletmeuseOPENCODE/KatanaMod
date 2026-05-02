# AGENTS.md

Projekt ten został wygenerowany przez asystenta AI Antigravity na podstawie wymagań użytkownika.

## Kontekst projektu
Plugin to modyfikacja do gry Minecraft (silnik Paper, wersja 1.20.2), dodająca wsparcie dla niestandardowych Katan (np. Wakizashi). Oparto się na natywnym API Minecrafta bez modyfikacji klienta:
- `CustomModelData`: Pozwala na przypisanie niestandardowych tekstur przedmiotom bazowym (w tym przypadku `IRON_SWORD`).
- `PersistentDataContainer`: Mechanizm przechowujący ukryte klucze (tagi NBT) w przedmiotach, aby unikalnie identyfikować katany niezależnie od nazwy w grze.
- System komend (`/katana give`) oraz system rzemiosła (Crafting).

## Narzędzia i technologia
- Gradle: Użyto do zarządzania zależnościami i kompilacji.
- Paper API 1.20.2.
- Język: Java 17.

## Dodane komponenty
1. `KatanaManager`: Zarządzanie logiką przedmiotu i konfiguracja receptur.
2. `KatanaHitListener`: Obsługa nakładania efektów mikstur po uderzeniu bytu (np. trucizna).
3. `KatanaCommand`: Obsługa wydawania mieczy.
4. `KatanaModPlugin`: Główna klasa inicjalizacyjna.

Plugin jest gotowy do zbudowania przez użycie polecenia `./gradlew build` w głównym katalogu. W celu poprawnego wyświetlania, na kliencie musi znajdować się resource pack wykorzystujący podane konfiguracje `.json`.
