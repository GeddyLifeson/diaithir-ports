// English is the display name; the Diaithiris name sits under it in grey italics (written by tools/cuisine/build.py data).
ItemEvents.modifyTooltips(event => {
  const C = global.CUISINE
  const native = {}
  for (let [id, name] of C.items) native[id] = name
  for (let [id, name] of C.dishes) native[id] = name
  for (let s of C.stations) native['diaithir:st_' + s.id] = s.name
  for (let id in native) event.add(id, Text.gray(native[id]).italic())
})
