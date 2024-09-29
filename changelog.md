# Changelog

## 1.0.1

### Fixed

- Player heads crashing.

## 1.0.0

### Added

- Create display board interop
- More separators - can do `[item;diamond]` or `[item:diamond]` for example.
  - Indirectly helps with figura incompatability where `[item:modid:somemoddeditem]` would not get matched due to figura matching the `:modid:` 
- Size Modifiers - ex `[item!diamond]` will render a larger diamond. good for signs and whatnot
  - `;` and `:` are normal size
  - `,` is x0.75 scale
  - `!` is x1.5 scale
  - `+` is x2 scale
  - Clientside config for upper size limit of in-chat messages.
- Serverside item showoff. `[show:hand]` will show the item in your hand when you send the message. `[show:offhand]` will show your offhand item.
- Serverside chat matchers - uses the same text matcher backend as other matchers but runs serverside against chat messages with the additional context of the `ServerPlayerEntity` that sent the message.
- Better sign glow effects using framebuffers. 
- Chinese & Korean config translations (thanks ChuijkYahus & skye !)

### Changed
- Moved matcher backend to common. Main universal matchers are still on the client.
- VertexConsumer given to the renderer is now the same one passed into the text renderer, fixes compatability with mods like glowcase that use odd vertex consumers.
- Emojiful is now explicitly incompatible due to it replacing the text renderer.
- Inline Style now uses a component-ish (not item components) system for storing data in styles.

### Fixed
- caxton incompatability (thanks skye !)
- Various lighting and fading issues.

## 0.0.1

Initial Release
