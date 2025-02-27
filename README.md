# Economy Plugin

The Economy plugin is a Minecraft server plugin designed to manage currencies, shops, vendors, and transactions. It supports creating and managing multiple currencies, setting up shops with trade items (Player driven economy), and using vendors as trading posts.

## Requirements

- Minecraft Server: Spigot/Paper 1.21.1 or later

## Plugin directory
```
├── Economy
│   ├── config.yml
│   ├── hibernate.properties
│   └── database.db
├── Economy.jar
├── ...
```

## Configuration config.yml example
```
DailyVendorBuyLimit: 320 # Maximum amount of items that can be bought in the vendor per day.
DailyVendorResetDurationHrs: 24 # Reset timer for the vendor buy limit in hours.
```

## Configuration hibernate.properties example
```
hibernate.connection.driver_class=org.sqlite.JDBC
hibernate.connection.url=jdbc:sqlite:/home/admin/Repositories/Minecraft/DevServer/plugins/Economy/database.db
hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect
hibernate.hbm2ddl.auto=update
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.connection.autocommit=false
```

## Commands

### Admin Commands

#### `/deposit <currencyName> <amount>`
- **Description:** Deposit currency into a user's purse.
- **Permission:** `economy.admin`

#### `/withdraw <currencyName> <amount>`
- **Description:** Withdraw currency from a user's purse.
- **Permission:** `economy.admin`

#### `/currency`
- **Usage:** `/currency add <currencyName>`; `/currency remove <name>`
- **Description:** Add or remove a currency.
- **Permission:** `economy.admin`

#### `/shop`
- **Usage:** `/shop create <shopName>`; `/shop delete <shopName>`
- **Description:** Create or delete a shop. Spawns a villager acting as a trading post for that shop.
- **Permission:** `economy.admin`

#### `/shopsection`
- **Usage:** `/shopsection create <shopName> <sectionName>`; `/shopsection delete <shopName> <sectionName>`
- **Description:** Create or delete a section within a shop. The item in the player's main hand is used as the icon for the section.
- **Permission:** `economy.admin`

#### `/shopitem`
- **Usage:** `/shopitem add <shopName> <sectionName>`; `/shopitem remove <shopName> <sectionName>`
- **Description:** Add or remove the item in the player's main hand as an option in a shop section.
- **Permission:** `economy.admin`

#### `/vendor`
- **Usage:** `/vendor create <vendorName>`; `/vendor delete <vendorName>`
- **Description:** Create or delete a vendor. Spawns a villager acting as a trading post for that shop.
- **Permission:** `economy.admin`

#### `/vendorsection`
- **Usage:** `/vendorsection create <vendorName> <sectionName>`; `/vendorsection delete <vendorName> <sectionName>`
- **Description:** Create or delete a section within a vendor. The item in the player's main hand is used as the icon for the section.
- **Permission:** `economy.admin`

#### `/vendoritem`
- **Usage:** `/vendoritem add,update <vendorName> <sectionName> <currencyName1> <buyPrice1> <sellPrice1> <currencyName2> <buyPrice2> <sellPrice2> ...`; `/vendoritem remove <vendorName> <sectionName>`
- **Description:** Add or remove the item in the player's main hand as an option in a vendor section, specifying multiple currencies and prices.
- **Permission:** `economy.admin`

#### `/bank`
- **Usage:** `/bank create <bankName> <interestCooldown (hrs); /bank delete <bankName`
- **Description:** Create or delete a bank. Spawns a villager acting as the banker.
- **Permission:** `economy.admin`

#### `/account`
- **Usage:** `/account create <accountName> <bankName> <interestRate(%)> <unlockCurrencyName> <unlockCost>; /account delete <accountName> <bankName>`
- **Description:** Create or Delete a bank account option. Interest rate':' 10.25 -> 10.25%. Main hand item is used as icon
- **Permission:** `economy.admin`

#### `/ah`
- **Usage:** `/ah`
- **Description:** Opens the auction house GUI
- **Permission:** `economy.admin`

#### `/auction`
- **Usage:** `/auction create <startingBid> <currenyType> <startDelay(mins)> <duration(mins)>; /auction create <startingBid> <currencyType> <duration(mins)>`
- **Description:** Creates an auction for the item in your main hand
- **Permission:** `economy.admin`

#### `/auctionhouse`
- **Usage:** `/auctionhouse`
- **Description:** Spawns a villager acting as entry point for the auction house
- **Permission:** `economy.admin`

### User Commands

#### `/purse`
- **Description:** Opens the purse GUI for the user.
- **Permission:** `economy.user`

## Permissions

### `economy.admin`
- **Description:** Grants access to admin commands.
- **Default:** Operator (op)

### `economy.user`
- **Description:** Grants access to user commands.
- **Default:** True (available to all players)

## Installation

1. Ensure you have a Minecraft server running Spigot or Paper 1.21.1 or later.
3. Place the Economy plugin JAR file into the server's `plugins` directory.
4. Start the server to load the plugin.
