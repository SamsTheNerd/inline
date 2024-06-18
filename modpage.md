# Inline

Inline is a minecraft library for rendering in-line with text. It has some player facing features, mostly for fun, but also to serve as examples and helpers for future devs.

Check out the gallery for some examples !

## Player Facing Features

Inline comes with a couple of built-in Matchers, these can be used anywhere that displays text in-game:
- Items -- ex: `[item:diamond_sword]` -- supports any item id
- Entity -- ex: `[entity:pig]` -- supports any entity type id
- Player heads -- ex: `[face:samsthenerd]` -- supports player name or uuid.
- Mod Icon -- ex: `[mod:inline]`

Some other mods, such as emi, rei, and probably others, add the name of the mod that an item comes from to its tooltip. Inline will look for this and attach the mod's icon to it.

All of these are configurable from the in-game config screen.

## Devs

![A flowchart visualizing inline's core process](https://github.com/SamsTheNerd/inline/raw/main/assets/flowchart.png)

Check out the [readme](https://github.com/SamsTheNerd/inline) for more information on developing with Inline !