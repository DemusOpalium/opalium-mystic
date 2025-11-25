🌑 DasLoch – Legend & Mystic Item Framework

Opalium Haven • Paper 1.21.10 • Java 21 • Gradle Kotlin DSL

Ein vollständig modulares High-End-Item-System für Opalium Haven:
Mystic-Items, Legend-Items, Token-System, eigener Mystic-Well,
vollständiges Custom-Enchant-Framework, Life-System und Vault-Economy-Integration.

⚠️ Nostalgia-Top-System gehört NICHT zum Plugin.
Es bleibt 100 % ein Skript-System und ist bewusst getrennt.

📦 Features im Überblick
🗡 Mystic-Items

Mystic Sword, Mystic Bow, Mystic Armor, Mystic Pants

zufällige Enchants + Token

Life-System (Item verliert Leben beim Tod)

Prefix-Stufen basierend auf Token-Menge

voll konfigurierbar in items.yml

👑 Legend-Items

Spezialitems mit festen Stats

eigener Namensraum (#LEGEND-id)

perfekte Kompatibilität mit Economy & PDC

generierbar über /legendgive

✨ Custom-Enchant-System

45 moderne, servereigene Enchants

Kategorien: Universal, Sword, Bow, Pants, Legendaries

Rarities: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY

Jeder Enchant besitzt:

maxTier

Token-Kosten

Effekt-Maps (Percent-Werte pro Tier)

sichtbaren Namen & ID

Vollständig gesteuert über enchants.yml

🕳️ Mystic Well – Opalium Edition

Gold rein → Tokens + Rarity → Enchants würfeln.

Tier I, II, III mit jeweils eigenen Wahrscheinlichkeiten

Token-Spannen

Rarity-Gewichtung

Limits für RARE/EPIC/LEGENDARY

Perks/Modifier möglich (modifiers:)

Konfigurierbar in well.yml

❤️ Life-System

Jeder Mystic hat lives & max_lives

sinkt beim Sterben

verschwindet bei 0 Leben

alles via PDC gespeichert

🔧 Technische Basis

Vollständige Nutzung von PersistentDataContainer

Lore-Marker für Items:

#MYST-id: <id>
#LEGEND-id: <id>


Events: Combat, Death, BowHit, ArmorHit

Vault-Economy für alle Gold-Operationen

sauber kapsulierte Services (Factory, Parser, Well, Life)

🧩 Dateistruktur
src/main/resources/
 ├─ items.yml         # Definition aller Mystic/Legend Items
 ├─ enchants.yml      # 45 Enchants, Effekte & Token
 ├─ well.yml          # Mystic Well Tier-Logik & Rarity-Rolling
 └─ config.yml        # Plugin-Basis-Config

📚 API (für Entwickler / Skripte)
Item-Erstellung
createLegendItem(id, owner)
createMysticItem(id)

Werte lesen & schreiben
readEnchants(item)
getTokens(item)
getLives(item)
setLives(item, value)

Dienste abrufen
getMysticWellService()
getEnchantRegistry()
getItemService()

🧙 Commands
Command	Beschreibung
/legendgive <type> <player>	Gibt ein Legend-Item
/mysticgive <type> <player>	Gibt ein Mystic-Item
/mysticwell	Zugriff auf den Brunnen
/dasloch reload	Lädt alle YML-Configs neu
/dasloch debug	Debug-Infos für Entwickler
🧠 Funktionsablauf eines Mystic-Rerolls
1. Spieler zahlt Gold (Vault)
2. well.yml bestimmt:
     - Token-Menge (1–6)
     - Rarity (COMMON → LEGENDARY)
3. Token + Enchants werden berechnet
4. Lore wird aktualisiert (#MYST-id)
5. Item erhält Prefix abhängig von Token

🧪 Datenmodell

EnchantDefinition

id

displayName

rarity

applicable (SWORD/BOW/PANTS)

maxTier

tokenValues (pro Tier)

effects:

heal-percent

extra-gold

extra-xp-percent

streak-bonus-percent

extra-damage-percent

threshold-hearts

damage-reduction-percent

MysticWellTier

tokenMin/tokenMax

rareLimits

probability (weights)

modifiers

🎯 Aktueller Gesamtstatus

✔ 45 Enchants vollständig integriert
✔ well.yml optimiert (I–III mit perfektem Balancing)
✔ komplette Mystic-Pipeline läuft stabil
✔ Legend-Item-System voll funktionsfähig
✔ Combat-Listener implementiert
✔ Token-/Life-System zu 100 % funktionsbereit
✔ GitHub-Projekt sauber & build-fähig
✔ ReadMe vollständig
✔ Code modular & erweiterbar

🔮 Nächste Schritte (optional)

GUI für Mystic Well / Enchant Browser

Wiki-Panel im Web

/enchants Hilfe-Seite

Lore-Generator für alle Items

NFT-ähnliche „Signatures“ pro Drop

automatische Preisberechnung per Kills / XP

Wenn du willst, erstelle ich:

✔ Die Web-Wiki-Version
✔ Eine README-Variante mit Bildern
✔ Eine Developer-API-Dokumentation
✔ Eine Version mit Copy-Paste-Codeblöcken für jede Sektion
