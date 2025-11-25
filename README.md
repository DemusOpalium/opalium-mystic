
---

<p align="center">
  <img src="https://raw.githubusercontent.com/edent/SuperTinyIcons/master/images/svg/minecraft.svg" width="120"/>
</p>

<h1 align="center">DasLoch – Mystic & Legend Item Framework</h1>
<p align="center"><strong>Custom Items • Enchants • Mystic Well • Token System • Life System</strong></p>
<p align="center">PaperMC 1.21.10 • Java 21 • Vault Economy • Opalium Haven</p>

---

## ⭐ Überblick

**DasLoch** ist ein vollständiges, modular aufgebautes Item-Framework für den Server
**Opalium Haven**.

Es erweitert Minecraft um:

* Legend Items
* Mystic Items
* ein eigenes Enchant-System
* einen Token-basierten Mystic Well
* ein Life-System
* komplett konfigurierbare YMLs

---

## 🎯 Features

### 🗡️ Mystic Items

* Upgrades, Rarity, Token, Prefixes
* rollen Enchants über den Mystic Well

### 👑 Legend Items

* feste Werte
* eigener Besitzer
* serverexklusive Designs

### ✨ Custom Enchants

* COMMON → LEGENDARY
* 40+ mögliche Enchants
* Balancing über Token

### ⚙️ Mystic Well

* Tier I–III
* Goldzahlung → Rarity-Roll
* voll konfigurierbar in `well.yml`

### ❤️ Life System

* Items verlieren Leben
* Life Tokens reparieren

---

## 📂 Dateistruktur

```
src/
├── main/java/de/opalium/dasloch/
│   ├── command/
│   ├── config/
│   ├── enchant/
│   ├── item/
│   ├── listener/
│   ├── service/
│   ├── well/
│   └── DasLochPlugin.java
└── main/resources/
    ├── plugin.yml
    ├── items.yml
    ├── enchants.yml
    ├── well.yml
    └── config.yml
```

---

## 🔧 Installation

```
git clone https://github.com/DemusOpalium/dasloch-plugin.git
cd dasloch-plugin
./gradlew build
```

Das fertige JAR liegt in:

```
build/libs/dasloch-plugin.jar
```

---

## 🧱 Commands

```
/legendgive <id> <player>
/mysticgive <id> <player>
/dasloch reload
/mysticwell
```

---

## ⚙️ API (kurz)

```java
MysticItem item = itemService.create("mystic_sword");
LegendItem legend = itemService.createLegend("legacy_axe", owner);

int lives = lifeTokenService.getLives(stack);
Map<String, Integer> ench = enchantParser.read(stack);

MysticWellService.RollResult roll = mysticWell.roll("III");
```

---

## ⚠️ Hinweise

> Nostalgia-Tops sind NICHT Teil dieses Plugins.
> Das bleibt ein separates Skript-System.

---

## ❤️ Credits

**Projekt:** DasLoch
**Server:** Opalium Haven
**Lead:** Demus
**Systemdesign:** GPT-Opalium

---
