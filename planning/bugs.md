adjacent matches of different types get attached incorrectly:
`<item:diamond> <card:base1-4>` goes to `<item:diamond> [diamond item]`
^^ seems like it was being caused by the item renderer not rendering, which seems,, bad

3 renderers in a row seems to still break ?

item renderer is dark for sidelit/block items

entity renderer is a mess

face renderer needs correct capitalization (kinda cringe)

sprite renderer too bright on signs and whatnot

dark block thing is back, probably flattened it too much? idk