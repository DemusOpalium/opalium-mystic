# DasLoch – Legend & Mystic Item Framework
PaperMC 1.21.10 • Java 21 • Gradle (Kotlin DSL)

Ziel:
Ein modular aufgebautes Plugin für den Server „Opalium Haven“, das Legend- und Mystic-Items bereitstellt. 
Die Items besitzen Enchants, Token, Prefixes und ein Life-System. 
Kompatibel mit Vault/Economy.

Nostalgia-Tops sind NICHT Teil des Plugins – das bleibt weiterhin ein Skript-System.

Kernsystem:
- Legend-Items (eigene Namen & Designs)
- Mystic-Items (Sword, Bow, Armor, Pants)
- Enchant-System (I–III, COMMON/RARE)
- Token-Berechnung (Prefix je nach Token-Summe)
- Life-System (Items verlieren Leben beim Tod)
- Mystic Well (Gold investieren → Enchants würfeln)
- Konfiguration in YML: items.yml, enchants.yml, well.yml

API:
- createLegendItem(id, owner)
- createMysticItem(id)
- readEnchants(Item)
- getLives / setLives
- getTokens / recalcTokens

Commands:
- /legendgive <type> <player>
- /mysticgive <type> <player>
- /dasloch reload
- /dasloch debug

Technik:
- PersistentDataContainer für NBT
- Lore-Tags (#LEGEND-id, #MYST-id)
- Event-Hooks für Combat, Death, Bow, Armor
- Vault-Anbindung für Gold/Coins
