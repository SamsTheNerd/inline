# Inline

Inline is a minecraft library for rendering in-line with text. It has some player facing features, mostly for fun, but also to serve as examples and helpers for future devs.

See the [Modrinth Gallery](https://modrinth.com/project/inline/gallery) for images.

## Basic Ideas

Inline has 3 core components: Matchers, Data, and Renderers.

Matchers are responsible for parsing any and all text before it's rendered in-game and attaching appropriate Data to that text. For most cases you'll want to use a RegexMatcher. Matchers usually exist on the client.

Data is primarily just a standardized box used to pass arbitrary data to the renderer. It gets passed around by being stuck to a text's Style. Notably, data exists on the server as well as the client. This is so that mods can attach data to text directly instead of going through a matcher.

Finally, Renderers take in some data and render *something* in place of a character. Inline comes with a variety of built in renderers, in some cases it may be beneficial to re-use these either by extending its data or by composition.

![A flowchart visualizing how the core components interact.](https://github.com/SamsTheNerd/inline/raw/main/assets/flowchart.png)

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

## Developing

Inline is hosted on the [BlameJared Maven](https://maven.blamejared.com/com/samsthenerd/inline/)

You can depend on it using gradle:

```groovy
// add it to your maven repos
repositories {
    maven { url "https://maven.blamejared.com" }
}

// depend on it in common if multiloader:
modApi "com.samsthenerd.inline:inline-common:${project.inline_version}"

// and in fabric
modApi "com.samsthenerd.inline:inline-fabric:${project.inline_version}"

// and forge
modApi "com.samsthenerd.inline:inline-forge:${project.inline_version}"
```

The library is documented using javadoc, with an online version available at https://inline.samsthenerd.com/. If you have any trouble using the library feel free to open an issue or find me in one of the larger modding discords.