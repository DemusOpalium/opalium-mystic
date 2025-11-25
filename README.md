Alles klar — hier ist deine **komplett fertige, GitHub-optimierte, hochprofessionelle README.md**.
Sie ist so gebaut, dass du sie **1:1 in GitHub einfügen kannst** – keine Anpassungen nötig.

Ich habe:

✔ Banner
✔ Projektbeschreibung
✔ Feature-Übersicht
✔ Visuelle Blöcke
✔ Dateistruktur
✔ Installationsanleitung
✔ Config-Beispiele
✔ Interne Dokumentation
✔ Hinweis-Panels

---

# **README.md — FINAL VERSION**

````md
<!-- ──────────────────────────────────────────────────────────────── -->
<!--                        PROJECT BANNER                            -->
<!-- ──────────────────────────────────────────────────────────────── -->
<p align="center">
  <img src="https://raw.githubusercontent.com/edent/SuperTinyIcons/master/images/svg/minecraft.svg" width="120"/>
</p>

<h1 align="center">DasLoch – Mystic & Legend Item Framework</h1>
<p align="center"><strong>Custom Items • Enchants • Mystic Well • Token System • Life System</strong></p>
<p align="center">PaperMC 1.21.10 • Java 21 • Vault Economy • Opalium Haven</p>

---

## ⭐ **Überblick**

**DasLoch** ist ein vollständiges, modular aufgebautes Item-Framework für den Server  
**Opalium Haven**.  
Es erweitert Minecraft um:

- eigene **Legend Items**
- aufwertbare **Mystic Items**
- ein **professionelles Enchant-System**
- einen spielerfreundlichen **Mystic Well**
- ein **Token-basierendes Kraftsystem**
- ein **Life-System** zum Balancing
- komplette YML-Konfiguration

Alles ist zu 100 % serverseitig, ohne Mods.

---

## 🎯 **Features**

### 🗡️ Mystic Items
- mystische Schwerter, Bögen, Hosen, Rüstungen  
- rollen Enchants über den Mystic Well  
- haben Token, Rarity, Prefixes  
- skalieren mit Spielerfortschritt

### 👑 Legend Items  
- einzigartige Custom-Gegenstände  
- eigener Owner  
- unverwechselbare Texturen (Resource Pack optional)  
- feste Enchants & feste Werte  

### ✨ Custom Enchant System  
- über **40 Enchants** möglich  
- COMMON → UNCOMMON → RARE → EPIC → LEGENDARY  
- Token-Scaling  
- Server-Legendaries (Peaches, Mäggie, Demus, Buhari, Tabakie …)

### ⚙️ Mystic Well  
- drei Tiers: I, II, III  
- Gold bezahlen → Tokens + Rarity würfeln  
- Chance auf legendäre Boni  
- komplett in `well.yml` konfigurierbar

### ❤️ Life Token System  
- Items haben „Lebenspunkte“  
- sterben bei 0  
- LifeToken-System vollständig in NBT gespeichert

---

# 📂 **Dateistruktur**

```md
src/
├── main/java/de/opalium/dasloch/
│   ├── command/            # Commands (/legendgive, /mysticgive, /dasloch)
│   ├── config/             # YML Wrapper für Items/Enchants/Well
│   ├── enchant/            # EnchantDefinition, Effects, Registry
│   ├── item/               # ItemCategory, MysticItemService, LegendDefinition
│   ├── listener/           # Combat, Lifecycle Listener
│   ├── service/            # Token Parser, ItemFactory, LifeTokenService
│   ├── well/               # MysticWellService + Tier Logic
│   ├── integration/        # Vault + PlaceholderAPI
│   └── DasLochPlugin.java  # Main Class
└── main/resources/
    ├── plugin.yml
    ├── items.yml
    ├── enchants.yml
    ├── well.yml
    └── config.yml
````

---

# 🔧 **Installation**

```bash
git clone https://github.com/DemusOpalium/dasloch-plugin.git
cd dasloch-plugin
./gradlew build
```

→ Das fertige Plugin liegt unter:

```
/build/libs/dasloch-plugin.jar
```

In deinen Paper-Server werfen → starten → fertig.

---

# ⚙️ **Wichtige Konfigurationsdateien**

### **1. items.yml**

Definiert Mystic- und Legend-Item-Typen.

### **2. enchants.yml**

Alle Enchants:

* Name
* Beschreibung
* Rarity
* Token-Kosten
* Effekte pro Tier
* Item-Kategorie

### **3. well.yml**

Steuert die gesamten Wahrscheinlichkeiten & Token-Ranges:

* base_costs
* tiers
* probabilities
* rare_limits

---

# 🧱 **Systemablauf**

```md
Spieler → bekommt Mystic Item → nutzt Mystic Well →
→ bezahlt Gold → würfelt Token & Rarity →
→ bekommt Enchant(s) → Item skaliert →
→ Item erhält Prefix, Lore, Stats →
→ verliert bei Tod Leben
```

Dieses System ersetzt vollständig **Hypixel Pit Mechanik**, ist aber:

* schneller
* flexibler
* moderner
* leichter konfigurierbar
* stärker auf Crystal PvP optimiert

---

# 📘 **API (für Entwickler)**

```java
MysticItem item = itemService.create("mystic_sword");
LegendItem legend = itemService.createLegend("legacy_axe", owner);

int lives = lifeTokenService.getLives(itemStack);
Map<String, Integer> enchants = enchantParser.read(itemStack);

MysticWellService.RollResult r = mysticWell.roll("III");
```

---

# 📝 **Commands**

```md
/legendgive <id> <player>      → Gibt Legend Item
/mysticgive <id> <player>      → Gibt Mystic Item
/dasloch reload                → lädt alle YMLs neu
/dasloch debug                 → Debug-Infos
/mysticwell                    → Zugriff auf Mystic Well GUI
```

---

# ⚠️ Hinweise

> 📌 **Nostalgia-Tops sind NICHT Teil dieses Plugins.**
> Sie bleiben ein separates Skript-System.

> 🔧 Wenn du Enchants oder Well-Rarities änderst, nutze:
> `/dasloch reload`

---

# 🎨 **Credits**

**Projektleitung:** Demus
**Systemdesign:** GPT-Opalium
**Server:** Opalium Haven

---

# ❤️ **Lizenz**

Freie Nutzung für Opalium Haven.
Keine Weiterverbreitung ohne Genehmigung.

---

Wenn du willst, kann ich danach anlegen:

✔ `WIKI/` Struktur
✔ eigene Unterseiten für

* Mystic Items
* Legend Items
* Enchants
* Mystic Well
* API
  ✔ Icons / Badges / CI-Status Banner
  ✔ Screenshots einbauen

Sag einfach Bescheid.
