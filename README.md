# Economy Plugin

The Economy plugin is a Minecraft server plugin designed to manage currencies, shops, vendors, and transactions. It supports creating and managing multiple currencies, setting up shops with trade items (Player driven economy), and using vendors as trading posts.

## Requirements

- Minecraft Server: Spigot/Paper 1.21.1 or later

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
- **Usage:** `/vendoritem add <vendorName> <sectionName> <currencyName1> <buyPrice1> <sellPrice1> <currencyName2> <buyPrice2> <sellPrice2> ...`; `/vendoritem remove <vendorName> <sectionName>`
- **Description:** Add or remove the item in the player's main hand as an option in a vendor section, specifying multiple currencies and prices.
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

## Support

For support, please open an issue in the [GitHub repository](https://github.com/Yu-Qing-Liu/Economy).