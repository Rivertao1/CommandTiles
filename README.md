# CommandTiles

CommandTiles is a client-side Fabric mod for creating and running configurable command buttons
from an in-game menu.

> [!IMPORTANT]
> CommandTiles is in early development. The current `0.1.0` build provides the Fabric project
> foundation and placeholder screens; command-tile editing and execution are not implemented yet.

The initial target is Minecraft 1.21.11. Support for more Minecraft versions may be added later.

## Planned Features

- A configurable grid of command buttons opened with one key.
- Complete in-game editing, including item icons and ordered action lists.
- Multiple commands or chat messages per tile with individual delays.
- Profiles, groups, placeholders, and additional execution modes in later versions.
- Optional Mod Menu integration without making Mod Menu a required dependency.

## Development

- Java 21
- Gradle 9.2.1
- Fabric Loom 1.14.4
- Fabric Loader 0.18.4
- Fabric API 0.139.4+1.21.11
- Mod Menu 17.0.0-alpha.1 (optional at runtime)

Build with:

```shell
./gradlew build
```

The development client can be launched with:

```shell
./gradlew runClient
```

## Credits

CommandTiles is independently implemented and inspired by the interaction and workflow ideas of
CommandKeys and Quick Menu. See [`CREDITS.md`](CREDITS.md) for project links and license details.

## License

Copyright (C) 2026 River_tao.

CommandTiles is licensed under GPL-3.0-only. See [`LICENSE`](LICENSE) for details.
