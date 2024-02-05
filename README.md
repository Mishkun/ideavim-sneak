<img src="src/main/resources/META-INF/pluginIcon.svg" width="80" height="80" alt="icon" align="left"/>

# IdeaVim-Sneak (no longer supported)

---
# !!Archive Mode Notice!!

This plugin was bruteforcefully hacked for my personal use several years ago. It uses some dirty tricks and is not maintainable. I am very grateful for all of the users of this plugin and also people who contributed to make it less messy. I do not use it daily for more than a year and can't justify maintaining it anymore. As of IdeaVim version 2.8.0 it stopped working.

IdeaVim authors offered to integrate the functionality of this plugin to the IdeaVim itself. Track progress in the [issue](https://github.com/JetBrains/ideavim/discussions/818) in project repo

Also check out my much more useful plugin for supporting leader-key bindings in Intellij products: [Ataman](https://github.com/Mishkun/ataman-intellij)

It was a great journey, cheers!

Mikhail Levchenko, creator of ideavim-sneak

---

[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/15348-ideavim-sneak?label=dowload%20plugin)](https://plugins.jetbrains.com/plugin/15348-ideavim-sneak) 

IdeaVim-Sneak is a port of [vim-sneak](https://github.com/justinmk/vim-sneak) for [IdeaVim](https://github.com/JetBrains/ideavim).

## Setup

Install plugin from Intellij Idea Marketplace and add the following option on top of your `./ideavimrc`:

```
set sneak
```

After IdeaVim reboot you can use this plugin

## Usage

- Type `s` and two chars to start sneaking in forward direction
- Type `S` and two chars to start sneaking in backward direction
- Type `;` or `,` to proceed with sneaking just as if you were using `f` or `t` commands

## License

Just as IdeaVim, this plugin is licensed under the terms of the MIT License.

## Credits

Plugin icon is merged icons of IdeaVim plugin and a random sneaker by FreePic from flaticon.com
