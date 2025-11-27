
---

<p align="center">
  <img src="https://raw.githubusercontent.com/edent/SuperTinyIcons/master/images/svg/minecraft.svg" width="120"/>
</p>

<h1 align="center">DasLoch – Mystic Item Framework</h1>
<p align="center"><strong>Mystic Items • Custom Enchants • Mystic Well • Token & Life System</strong></p>
<p align="center">PaperMC 1.21.10 • Java 21 • Vault Economy • Opalium Haven</p>

---

## ⭐ Überblick

**DasLoch** ist ein modulares, vollständig konfigurierbares Item-Framework für
**Opalium Haven (Paper 1.21.10)**.

Das Plugin erweitert Minecraft um ein vollständiges **Mystic-Item-Ökosystem**, bestehend aus:

* **Mystic Items** (Rohlinge, Upgrades, Token)
* **Custom-Enchant-System**
* **Token-basierter Mystic Well (Tier I–III)**
* **Life-System pro Item**
* **komplett konfigurierbare Items, Enchants und Well-Tiers**
* **Integration mit NPC-Shops und DeluxeMenus / zMenu**

Die gesamte Item-Logik, Enchants, Token-Werte und Rollen sind vollständig in YML-Dateien steuerbar
(`items.yml`, `enchants.yml`, `well.yml`).


---

## 🎯 Features

### 🗡️ Mystic Items (Rohlinge & Upgrades)

Mystic-Items sind vollständig konfigurierbare Spezialwaffen:

* besitzen **Token-Kosten**, **Tier**, **Lives**, **Enchant-Pools**
* kommen **ungeprägt (0 Tokens)** aus Shops / Commands
* können über den **Mystic Well** hochgerollt werden
* funktionieren in allen Item-Slots (Swords, Axe, Bow, Armor)

Mystic-Items werden in `items.yml` definiert.

---

### ✨ Custom-Enchants

DasLoch besitzt ein eigenes Enchant-Framework:

* jede Mystic-Waffe nutzt einen individuellen **Enchant-Pool**
* Enchants besitzen:

  * **Rarity**
  * **maxTier**
  * **Token-Costs pro Upgrade**
  * **Lore-Effekte**
* alle Enchants werden in `enchants.yml` definiert


Das Enchant-System ist **serverseitig** und unabhängig von Vanilla-Enchants.

---

### ⚙️ Mystic Well (Tier I–III)

Das Herzstück des Plugins.

Der Mystic Well:

* rollt Mystic-Items auf Basis ihres Enchant-Pools
* besitzt **3 Tiers** (I, II, III)
* Tier bestimmt:

  * Wahrscheinlichkeit
  * Token-Cost
  * Rarity-Chance
* alle Werte werden in `well.yml` konfiguriert

Jede Roll-Stufe entspricht einem echten Progressions-Step.

**Tiers:**

| Tier    | Beschreibung                                   |
| ------- | ---------------------------------------------- |
| **I**   | Start-Rolls, günstiger, niedrige Rarity-Chance |
| **II**  | Mid-Rolls, höhere Qualität, teurer             |
| **III** | Endgame-Rolls, maximale Rarity-Chancen         |

---

### ❤️ Life-System

Jedes Mystic-Item besitzt:

* **maxLives**
* **currentLives**
* **Life Tokens** zum Reparieren

Lives werden in den **PDC-Tags** gespeichert und über das `LifeTokenService` verwaltet.

---

### 📦 Rohling-Shops (zMenu / DeluxeMenus / NPC)

DasLoch ist so designed, dass Shops extrem einfach eingebunden werden können:

#### Shop per zMenu / DeluxeMenus

```yml
actions:
  - type: player_command
    commands:
      - "dasloch mystic mystic_sword"
  - type: console_command
    commands:
      - "eco take %player% 550"
```

#### Shop per Citizens-NPC

```mc
/npc command add --console dasloch mystic mystic_axe_sunder
```

Das Plugin ist 100% menu-kompatibel.

---

### 👑 Legend Items (geplant)

Legend-Items werden erst verfügbar, wenn ein Mystic-Item vollständig:

* **maximales Tier**
* **alle Enchants**
* **komplett gelevelt**

… erreicht hat.
Das System ist **implementiert aber noch nicht aktiviert** (ALPHA-Status).

---

## 📂 Dateistruktur

```
src/
├── main/java/de/opalium/dasloch/
│   ├── command/       # Commands (legendgive, mysticgive, well, reload)
│   ├── config/        # Items, Enchants, Well Config Loader
│   ├── enchant/       # Enchant registry + definition system
│   ├── item/          # MysticItemService, ItemFactory, PDC handler
│   ├── listener/      # Combat, lifecycle, item interactions
│   ├── service/       # Token, Life, EnchantParser
│   ├── well/          # MysticWellService, Roll logic
│   └── DasLochPlugin.java
└── main/resources/
    ├── items.yml
    ├── enchants.yml
    ├── well.yml
    ├── config.yml
    └── plugin.yml
```

---

## 🔧 Installation & Build

```bash
git clone https://github.com/DemusOpalium/dasloch-plugin.git
cd dasloch-plugin
./gradlew build
```

Das fertige JAR findest du unter:

```
build/libs/dasloch-plugin.jar
```

Voraussetzungen:

* **Java 21**
* **Paper 1.21.10**
* **Vault + Economy Plugin**

---

## 🧱 Commands

### 📌 Mystic Items

```
/mysticgive <id> <player>
```

Gibt einen Mystic-Rohling aus `items.yml`.

### 📌 Legend Items

```
/legendgive <id> <player>
```

Noch ALPHA – nur intern für Dev-Tests.

### 📌 Mystic Well

```
/dasloch well roll I
/dasloch well roll II
/dasloch well roll III
```

Rollt das Item in der Hand.

### 📌 Reload (Config)

```
/dasloch reload
```

---

## 🔧 Rechte (LuckPerms)

### Für Spieler (Default)

```
dasloch.use
zmenu.open.mystic_brunnen
```

Damit kann jeder den Brunnen nutzen (Economy-Check kommt aus `well.yml`).

### Für Admins

```
dasloch.mystic.give
dasloch.legend.give
dasloch.reload
```

---

## ⚙️ API (Java)

```java
MysticItem item = itemFactory.createMystic("mystic_sword");
mysticItemService.applyTokenCost(item);

int lives = lifeTokenService.getLives(stack);
Map<String, Integer> ench = enchantParser.readEnchantLevels(stack);

MysticWellService.RollResult roll =
        mysticWellService.roll(player, stack, MysticWellService.Tier.THREE);
```

Alle Services sind sauber im Plugin registriert und über DI abrufbar.

---

## 🧩 Erweiterbarkeit

Das Framework ist modular und erlaubt:

* neue Mystic-Item-Typen
* neue Enchants
* neue Well-Tiers
* neue Roll-Wahrscheinlichkeiten
* Item-Prefixe, Token-Skalierung
* integration mit:

  * NPC Shops
  * DeluxeMenus / zMenu
  * Loot-Tables
  * Custom Boss Drops

---

## ⚠️ Alpha-Status

Das Plugin ist:

* **einsatzbereit**
* **stabil**
* aber offiziell noch **ALPHA**

Folgende Systeme sind geplant aber noch nicht fertig:

* Legend-Item-Finalisierung
* Lore-Automatisierung
* Integration für externe API-Plugins

---

## ❤️ Credits

**Projekt:** DasLoch
**Server:** Opalium Haven
**Lead:** Demus
**Systemdesign:** GPT-Opalium
**Enchants & Symbolsystem:**
 

---
