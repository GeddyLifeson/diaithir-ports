// The exchange: joint-stock companies, one chartered per culture's merchants, from Age V (the strangers' shares and the telegraph).
// Not before: Ages I-IV have debt and credit (02_bank.js) but no way to own a piece of a business.
// A company earns a cut of its culture's trade (2% of what changes hands in its markets), pays 60% of it out to shareholders as
// dividends once a week, and keeps the rest. Its share price is its fair value times exp(shares held by players / float): every share
// bought lifts the price along a smooth curve, every share sold lowers it, so a big holder moves the market against themselves, and
// the price you pay for n shares is the integral of the curve (a closed form, no loop). Fair value slides a fifth of the way a week
// toward the price at which the dividend would pay 6% a year, so a company in a busy culture is worth more over time and one in a
// dead market less. A broker takes 0.5% on each side; it leaves circulation. Dividends wait for the holder at the exchange.
// Prices are in tw and paid in the money of the market the player stands in, at its rate.

const EXCH_AGE = 'V'
const EXCH_PAR = 100
const EXCH_FLOAT = 1000
const EXCH_FEE = 0.005
const EXCH_PAYOUT = 0.6
const EXCH_YIELD = 0.06
const EXCH_CUT = 0.02

const exchLegacyCompanies = () => typeof DW_FACTIONS === 'undefined' ? {} : DW_FACTIONS

function exchCompany(co) {
  ECON.companies = ECON.companies || {}
  if (!ECON.companies[co] && exchLegacyCompanies()[co]) ECON.companies[co] = { name: exchLegacyCompanies()[co].name, culture: null, float: EXCH_FLOAT, fair: EXCH_PAR, z: 0 }
  return ECON.companies[co]
}

// A culture's merchants charter their company the first week the Age allows it.
function exchCharter(culture, name) {
  ECON.companies = ECON.companies || {}
  if (!ECON.companies[culture]) ECON.companies[culture] = { name: name, culture: culture, float: EXCH_FLOAT, fair: EXCH_PAR, z: 0 }
  return ECON.companies[culture]
}

// Price of the next share, in tw; and the cost in tw of moving n shares from z (n > 0 buying, n < 0 selling): the area under the curve.
const exchPrice = c => Math.max(0.01, c.fair * Math.exp(c.z / c.float))
const exchCost = (c, n) => c.fair * c.float * (Math.exp((c.z + n) / c.float) - Math.exp(c.z / c.float))

function exchHoldings(player) {
  let key = String(player.uuid)
  return ECON.holdings[key] = ECON.holdings[key] || {}
}

function exchTrade(player, co, n, buying, ctx) {
  let c = exchCompany(co), held = exchHoldings(player)
  if (!c) return player.tell(Text.red('No such company on the exchange.'))
  if (!buying) n = Math.min(n, held[co] || 0)
  if (n < 1) return player.tell(Text.red('You hold no shares in that company.'))
  ctx = ctx || econCtx({ town: 'wilds', culture: null })
  let tw = buying ? exchCost(c, n) : -exchCost(c, -n)
  let units = buying ? Math.ceil(tw * (1 + EXCH_FEE) * ctx.scale) : Math.floor(tw * (1 - EXCH_FEE) * ctx.scale)
  if (buying && !econTake(player, units, ctx)) return player.tell(Text.red(`That costs ${units} ${ctx.unit}.`))
  if (!buying) econGive(player, units, ctx)
  econMint(ECON, ctx.culture, buying ? -units : units)
  c.z += buying ? n : -n
  held[co] = (held[co] || 0) + (buying ? n : -n)
  ECON_PUB.dirty = true
  econSave()
  player.tell(Text.green(`${buying ? 'Bought' : 'Sold'} ${n} shares of ${c.name} for ${units} ${ctx.unit}.`))
}

// Dividends that came due while the holder was away, paid in the market's money.
function exchCollect(player, ctx) {
  let due = (ECON.div || {})[String(player.uuid)]
  if (!due) return
  let units = Math.floor(due * ctx.scale)
  if (units < 1) return
  ECON.div[String(player.uuid)] = due - units / ctx.scale
  econGive(player, units, ctx)
  econMint(ECON, ctx.culture, units)
  player.tell(Text.green(`Dividends: ${units} ${ctx.unit}.`))
}

function exchShow(player, ctx) {
  exchCollect(player, ctx)
  let held = exchHoldings(player)
  player.tell(Text.gold(`The exchange, in ${ctx.unit} per share (you have ${econPurse(player, ctx)}):`))
  Object.keys(ECON.companies || {}).forEach(co => {
    let c = ECON.companies[co]
    player.tell(econButtons(Text.white(`${c.name}: ${(exchPrice(c) * ctx.scale).toFixed(1)}, you hold ${held[co] || 0} `), '/exchange', co, 10))
  })
}

// Once a week: each company takes its cut of the trade in its culture, accrues dividends for its holders, and its fair value moves
// toward its yield. Charters any culture that has none once the Age allows.
function exchWeek() {
  if (!econAgeAtLeast(EXCH_AGE)) return
  ECON.div = ECON.div || {}
  ECON_CLAIMS.forEach(c => exchCharter(c.culture, 'Kompunaid ' + econCultureName(c.culture)))
  Object.keys(ECON.companies || {}).forEach(co => {
    let c = ECON.companies[co], s = c.culture ? econCur(ECON, c.culture) : null
    let earn = s ? (s.vol || 0) * EXCH_CUT : 0
    if (s) s.vol = 0
    let perShare = earn * EXCH_PAYOUT / c.float
    if (perShare > 0) Object.keys(ECON.holdings).forEach(u => { let n = ECON.holdings[u][co]; if (n) ECON.div[u] = (ECON.div[u] || 0) + n * perShare })
    c.fair = Math.max(5, c.fair * 0.8 + Math.max(5, 52 * perShare / EXCH_YIELD) * 0.2)
  })
}

ServerEvents.commandRegistry(event => {
  let { commands: Commands, arguments: Arguments } = event
  let open = fn => ctx => {
    let p = ctx.source.getPlayerOrException()
    if (!econAgeAtLeast(EXCH_AGE)) p.tell(Text.red('There is no exchange in this Age.'))
    else fn(p, ctx, econCtx(econMarketOf(p)))
    return 1
  }
  let trade = (name, buying) => Commands.literal(name).then(Commands.argument('company', Arguments.STRING.create(event))
    .then(Commands.argument('count', Arguments.INTEGER.create(event)).executes(open((p, ctx, mc) =>
      exchTrade(p, Arguments.STRING.getResult(ctx, 'company'), Math.max(1, Arguments.INTEGER.getResult(ctx, 'count')), buying, mc)))))
  event.register(Commands.literal('exchange').executes(open((p, ctx, mc) => exchShow(p, mc)))
    .then(trade('buy', true))
    .then(trade('sell', false)))
})
