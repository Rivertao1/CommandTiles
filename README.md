# CommandTiles

CommandTiles is a client-side Fabric mod that lets you create command buttons in game and run
them from a compact menu or an optional keyboard shortcut.

The first alpha release targets Minecraft 1.21.11.

## Features

- Open a configurable command-tile menu with `G` by default.
- Create, edit, and delete tiles without editing configuration files.
- Give each tile a name, description, Minecraft item icon, command or chat message, and delay.
- Run tiles from the menu or assign an optional keyboard, modifier-key, or mouse shortcut.
- Warn about vanilla key conflicts and prevent duplicate CommandTiles shortcuts.
- Configure the grid size, execution feedback, and whether the menu closes after an action.
- Recover safely from unreadable configuration files and keep a backup when saving.
- Use the Mod Menu configuration button when Mod Menu is installed.
- English and Simplified Chinese translations.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.4 or newer
- Fabric API 0.139.4+1.21.11 or a compatible newer build
- Java 21
- Mod Menu 17.0.0-alpha.1 or newer is optional

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Put Fabric API and the CommandTiles JAR in the Minecraft `mods` directory.
3. Optionally install Mod Menu.
4. Start the game and press `G` while in a world.

## Usage

1. Open CommandTiles with `G`.
2. Select **Edit**, then select the green **Add tile** card.
3. Enter a name and an action. Text beginning with `/` is sent as a command; other text is sent
   as a chat message.
4. Optionally choose an item ID, delay, and shortcut.
5. Save the tile and leave edit mode to run it.

When listening for a shortcut, press `Escape` to cancel or `Backspace`/`Delete` to clear the
binding. Shortcuts only run while playing with no other screen open.

Configuration is stored in `.minecraft/config/commandtiles/config.json`.

## Alpha Limitations

- The in-game editor currently exposes one action per tile. The configuration and executor
  already support ordered action lists, and a full list editor is planned.
- Profile and group data exists, but profile/group management screens are not implemented yet.
- Tile drag-and-drop reordering is not implemented.
- Only Fabric 1.21.11 is currently supported.

## Development

Build the project with Java 21:

```shell
./gradlew build
```

The distributable JAR is written to `build/libs/`.

Launch the development client with:

```shell
./gradlew runClient
```

## Credits

CommandTiles is independently implemented and inspired by the interaction and workflow ideas of
CommandKeys and Quick Menu. See [`CREDITS.md`](CREDITS.md) for project links and license details.

## License

Copyright (C) 2026 River_tao.

CommandTiles is licensed under GPL-3.0-only. See [`LICENSE`](LICENSE) for details.
