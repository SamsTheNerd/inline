# Inline

Inline is a minecraft library for rendering in-line with text. It has some player facing features, mostly for fun, but also to serve as examples and helpers for future devs.

Check out the gallery for some examples !

## Player Facing Features

### Clientside

Inline comes with a couple of built-in matchers:
- Items -- ex: `[item:diamond_sword]` -- supports any item id
- Entity -- ex: `[entity:pig]` -- supports any entity type id
- Player heads -- ex: `[face:samsthenerd]` -- supports player name
- Mod Icon -- ex: `[mod:inline]`

You can use various characters in place of the `:`, some of which modify the size of the render, good for sign labels!
- `;` and `:` are normal size
- `,` is x0.75 scale
- `!` is x1.5 scale
- `+` is x2 scale

There's a clientside config for upper size limit of in-chat messages to prevent spam from blocking other messages.

You can also prefix any of these with `\ `to prevent it from matching.

Some other mods, such as emi, rei, and probably others, add the name of the mod that an item comes from to its tooltip. Inline will look for this and attach the mod's icon to it.

All of these are configurable from the in-game config screen.

#### Create Interop

Inline renders can be used on the create display boards. Due to some limitations, it can be a bit iffy with display links where you need a wider display board than you'd think, otherwise it gets cut off and won't match/render properly.

### Serverside

Server-side matchers match against `[show:hand]` or `[show:offhand]` to show off your currently held item in chat.

## Known Mod Conflicts
- Emojiful - due to them replacing the text renderer, preventing inline's mixins from applying.

If you find any others, please open an issue about it !


## Devs

![A flowchart visualizing inline's core process](https://github.com/SamsTheNerd/inline/raw/main/assets/flowchart.png)

Check out the [readme](https://github.com/SamsTheNerd/inline) for more information on developing with Inline !