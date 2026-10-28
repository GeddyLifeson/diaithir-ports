// global.CALENDAR_YEAR(): the server calendar year, negative before the common era; NaN until the engine has sent a year.
// The engine's 'year' event carries the timeline year LC, saved by earth_timeline (flood.js) and cities.js; LC = 14873 - cal BP and cal BP = 1950 - year.
(() => {
  const LC_TO_YEAR = -12923
  global.CALENDAR_YEAR = () => {
    let server = Utils.server
    if (!server) return NaN
    let lc = server.persistentData.getIntOr('earth_timeline_lc', server.persistentData.getIntOr('cities_lc', -1))
    return lc > 0 ? lc + LC_TO_YEAR : NaN
  }
})()
