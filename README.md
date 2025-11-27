

---

<p align="center">
  <img src="https://raw.githubusercontent.com/edent/SuperTinyIcons/master/images/svg/minecraft.svg" width="120"/>
</p>

<h1 align="center">DasLoch – Mystic Item Framework</h1>
<p align="center"><strong>Mystic Items • Eigene Verzauberungen • Mystic-Brunnen • Token- & Life-System</strong></p>
<p align="center">PaperMC 1.21.10 • Java 21 • Vault Economy • Opalium Haven</p>

---

## ⭐ Überblick

**DasLoch** ist ein modulares Item-Framework für den Server **Opalium Haven**.
Es erweitert Minecraft um ein vollständig eigenes System für **Mystic Items**, **Custom Enchants**, **Token-Rolls**, ein **Life-System** und einen mehrstufigen **Mystic-Brunnen**.

Der gesamte Funktionsumfang wird über YML-Dateien gesteuert:

* `items.yml` – definiert alle Mystic-Rohlinge
* `enchants.yml` – definiert alle Custom-Verzauberungen
* `well.yml` – konfiguriert Tier I–III Rolls, Chancen & Kosten
* `config.yml` – globale Einstellungen

Das Plugin ist **einsatzbereit**, aber offiziell noch **ALPHA**, da das Legend-Item-System noch nicht fertiggestellt wurde.

---

## 🎯 Hauptfunktionen

### 🗡️ Mystic Items (Rohlinge & Upgrades)

Mystic-Items sind vollständig konfigurierbare Spezial-Items:

* besitzen **Token-Kosten**, **Tier**, **Lives** und **eigenen Enchant-Pool**
* starten immer **ungeprägt (0 Tokens)**
* werden durch den **Mystic-Brunnen** aufgewertet
* existieren für:

  * Schwerter
  * Äxte
  * Bögen / Armbrüste
  * Rüstungen (Helm, Brust, Legs, Boots)

Definition aller Items:
**`resources/items.yml`**

---

### ✨ Custom-Enchants

Das Plugin besitzt ein eigenes, serverseitiges Enchant-System:

* Enchants besitzen:

  * **Seltenheit (Rarity)**
  * **maxTier**
  * **Token-Kosten pro Stufe**
  * **Effekte & Lore**
* Mystic-Items können nur Enchants aus ihrem **Enchantment-Pool** erhalten
* Vanilla-Verzauberungen sind unabhängig davon

Config-Datei:
**`resources/enchants.yml`**


---

### ⚙️ Mystic-Brunnen (Tier I–III)

Der Mystic-Brunnen ist das Herzstück von *DasLoch*.

Er ermöglicht das **Roll-System**:

* **Tier I** – günstige Einstiegs-Rolls
* **Tier II** – bessere Enchants, teurer
* **Tier III** – Endgame-Rolls, höchste Rarity-Chancen

Jede Stufe hat:

* eigene Token-Kosten
* eigene Rarity-Verteilungen
* eigene Wahrscheinlichkeiten

Konfiguration:
**`resources/well.yml`**

Der Brunnen kann per **Menu (zMenu/DeluxeMenus)** oder **Command** genutzt werden.

---

### ❤️ Life-System

Mystic-Items besitzen:

* **maxLives** (z. B. 10)
* **currentLives** (nimmt durch Nutzung / Kämpfe ab)
* Lebensreparatur nur durch **Life Tokens**

Lives werden über PDC gespeichert und durch den `LifeTokenService` verwaltet.

---

### 📦 Shops & Menüs (zMenu, DeluxeMenus, NPC)

Das Plugin ist vollständig kompatibel mit:

* **zMenu**
* **DeluxeMenus**
* **Citizens NPCs**

Ein Shop-Eintrag für Mystic-Rohlinge:

```yml
actions:
  - type: player_command
    commands:
      - "dasloch mystic mystic_sword"
  - type: console_command
    commands:
      - "eco take %player% 550"
```

NPC-Beispiel:

```mc
/npc command add --console dasloch mystic emerald_forge_blade
```

---

### 👑 Legend Items (geplant)

Legend-Items sind bereits im Code vorbereitet, jedoch noch **nicht finalisiert**.

Sie sollen entstehen, wenn:

* ein Mystic-Item auf maximaler Stufe ist
* alle Enchants besitzt
* alle Token vollständig ausgeprägt sind

Aktuell deaktiviert – **ALPHA-Status**.

---

## 📂 Projektstruktur

```
src/
├── main/java/de/opalium/dasloch/
│   ├── command/       # Commands (mysticgive, legendgive, well, reload)
│   ├── config/        # Laden und Validieren der YML-Dateien
│   ├── enchant/       # Enchant-Registry, Wirkungen, Definition
│   ├── item/          # MysticItemService, ItemFactory, Token/Life-Handling
│   ├── listener/      # Kampf & Item Lifecycle Listener
│   ├── service/       # Parser, Token, Life, Verarbeitung
│   ├── well/          # MysticWellService + Roll-Logik (Tier I–III)
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

Das fertige Plugin befindet sich in:

```
build/libs/dasloch-plugin.jar
```

**Benötigt:**

* Java 21
* Paper 1.21.10
* Vault + Economy-Plugin (EssentialsX empfohlen)

---

## 🧱 Befehle

### Mystic Items

```
/mysticgive <id> <spieler>
```

Gibt einen Mystic-Rohling aus `items.yml`.

### Legend Items

```
/legendgive <id> <spieler>
```

(ALPHA – nur für Tests)

### Mystic-Brunnen

```
/dasloch well roll I
/dasloch well roll II
/dasloch well roll III
```

### Reload

```
/dasloch reload
```

---

## 🔧 Rechte (LuckPerms)

### Spieler / Default

```
dasloch.use
zmenu.open.mystic_brunnen
```

Damit kann jeder Spieler den Brunnen nutzen, wenn er genug Geld besitzt.

### Administratoren

```
dasloch.mystic.give
dasloch.legend.give
dasloch.reload
```

---

## ⚙️ Java-API (Kurz)

```java
MysticItem item = itemFactory.createMystic("mystic_sword");

int lives = lifeTokenService.getLives(stack);
Map<String, Integer> enchants = enchantParser.readEnchantLevels(stack);

MysticWellService.RollResult result =
    mysticWellService.roll(player, stack, MysticWellService.Tier.THREE);
```

Die Services können direkt über den Plugin-Context bezogen werden.

---

## 🧩 Erweiterbarkeit

Das Framework unterstützt:

* neue Mystic-Item-Kategorien
* neue Custom-Enchants
* neue Mystic-Well-Tiers
* Token-basiertes Balancing
* eigene Shops (NPC / Menü / GUI)
* benutzerdefinierte Drop-Tables
* serverexklusive Waffen & Rüstungen

---

## ⚠️ Alpha-Status

DasLoch ist:

* **einsatzfähig**
* **stabil**
* aber offiziell **ALPHA**

Folgende Systeme sind noch in Entwicklung:

* Legend-Item-Finalisierung
* automatische Lore-Generierung
* externe Plugin-API

---

## ❤️ Credits

**Projekt:** DasLoch
**Server:** Opalium Haven
**Entwicklung:** Demus
**Systemdesign & Dokumentation:** GPT-Opalium
**Symbole & Trenner:** interne Symboldateien (Symbol_Liste.json, Trenner_Symbole-Legende.txt)
 

---
