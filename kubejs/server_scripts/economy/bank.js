// /bank: the commands over 02_bank.js. A banker is any store keeper in a culture's town (temple vault from Age III, a lending bank
// from Age V). Money goes in and out of the player's purse in the market's own money; a loan makes new money, repaying burns it.

const bankKey = player => String(player.uuid)

// The temple vault block (Age III-IV banker): data/diaithir/vault.json names the block ids and the cultures whose pantheon is dead. A player who has
// used a vault block in the last minute may use /bank beside it, as beside a store keeper. Breaking one curses the thief when the culture's gods are dead.
let VAULT = { blocks: ['diaithir:temple_vault'], dead: {} }
try { VAULT = Object.assign(VAULT, econRead('kubejs/data/diaithir/vault.json') || {}) } catch (e) { }
const VAULT_SEEN = {}
const VAULT_WINDOW = 1200 // ticks
const bankAtVault = player => VAULT_SEEN[bankKey(player)] !== undefined && player.server.tickCount - VAULT_SEEN[bankKey(player)] < VAULT_WINDOW

BlockEvents.rightClicked(event => {
  if (VAULT.blocks.indexOf(String(event.block.id)) < 0) return
  VAULT_SEEN[bankKey(event.player)] = event.server.tickCount
  event.player.tell(Text.gold('The temple vault is open to you. Use /bank here: deposit, withdraw, or pledge goods for a loan.'))
})

BlockEvents.broken(event => {
  if (VAULT.blocks.indexOf(String(event.block.id)) < 0) return
  let culture = econMarketOf(event.player).culture
  if (!culture || !VAULT.dead[culture]) return // a living pantheon answers in its own way (the gods thread)
  event.server.runCommandSilent(`effect give ${event.player.username} minecraft:unluck 3600 3`)
  event.server.runCommandSilent(`effect give ${event.player.username} minecraft:weakness 600 1`)
  event.player.tell(Text.darkRed('The gods of this place are dead, and the vault was theirs. Something old marks you.'))
})

function bankDo(player, ctx, market, fn) {
  if (!market.culture) return player.tell(Text.red('There is no bank outside a culture\'s towns.'))
  if (!econAgeAtLeast('III')) return player.tell(Text.red('There is no bank in this Age.'))
  fn(econBank(ECON, ctx.culture), bankKey(player), econAge())
  econSave()
}

function bankShow(player, ctx, b, who, age) {
  let r = bankRates(b, age), l = b.loans[who]
  player.tell(Text.gold(`Bank of ${econCultureName(ctx.culture)}, in ${ctx.unit}${b.failed ? ' (failed)' : ''}:`))
  player.tell(Text.white(`Your deposit: ${Math.floor(b.dep[who] || 0)}${l ? `, locked ${Math.ceil(l.collateral)} against a debt of ${Math.ceil(l.owed)}` : ''}.`))
  let g = b.goods && b.goods[who]
  if (g) player.tell(Text.white(`You owe ${Math.ceil(g.owed)} against pledged goods worth ${Math.floor(g.pledge.value)}.`))
  player.tell(Text.white(bankReserve(age) >= 1 ? `A vault: it keeps your money safe for a small fee and lends only from its own fund (${Math.floor(b.fund || 0)}), against goods worth ${VAULT_COVER} times the loan.`
    : `Lends at ${(r.lend * 100).toFixed(1)}% a year, pays ${(r.save * 100).toFixed(1)}%, may lend ${Math.floor(bankCapacity(b, age))} more.`))
}

ServerEvents.commandRegistry(event => {
  let { commands: Commands, arguments: Arguments } = event
  let atBank = fn => cx => {
    let p = cx.source.getPlayerOrException()
    if (bankAtVault(p) || econNearStore(p)) { let m = econMarketOf(p), ctx = econCtx(m); bankDo(p, ctx, m, (b, who, age) => fn(p, cx, ctx, b, who, age)) }
    return 1
  }
  let amount = (name, fn) => Commands.literal(name).then(Commands.argument('amount', Arguments.INTEGER.create(event))
    .executes(atBank((p, cx, ctx, b, who, age) => fn(p, Math.max(1, Arguments.INTEGER.getResult(cx, 'amount')), ctx, b, who, age))))
  event.register(Commands.literal('bank').executes(atBank((p, cx, ctx, b, who, age) => bankShow(p, ctx, b, who, age)))
    .then(amount('deposit', (p, n, ctx, b, who) => {
      if (!econTake(p, n, ctx)) return p.tell(Text.red(`You have ${econPurse(p, ctx)} ${ctx.unit}.`))
      if (!bankDeposit(b, who, n)) { econGive(p, n, ctx); return p.tell(Text.red('The bank has failed.')) }
      p.tell(Text.green(`Deposited ${n} ${ctx.unit}.`))
    }))
    .then(amount('withdraw', (p, n, ctx, b, who) => {
      let got = bankWithdraw(b, who, n)
      econGive(p, got, ctx)
      p.tell(Text.green(`Withdrew ${Math.floor(got)} ${ctx.unit}.`))
    }))
    .then(amount('borrow', (p, n, ctx, b, who, age) => {
      let r = bankBorrow(b, who, n, age)
      if (!r.ok) return p.tell(Text.red(r.why))
      econGive(p, n, ctx)
      econMint(ECON, ctx.culture, n)
      p.tell(Text.green(`Borrowed ${n} ${ctx.unit} at ${(bankRates(b, age).lend * 100).toFixed(1)}% a year.`))
    }))
    .then(Commands.literal('pledge').then(Commands.argument('amount', Arguments.INTEGER.create(event)).executes(atBank((p, cx, ctx, b, who, age) => {
      // lend against the goods in the main hand: the vault keeps them until the loan is paid
      let n = Math.max(1, Arguments.INTEGER.getResult(cx, 'amount')), stack = p.mainHandItem, id = String(stack.id), cnt = stack.getCount()
      if (stack.isEmpty()) return p.tell(Text.red('Hold the goods you pledge.'))
      let town = econMarketOf(p).town
      let value = Math.floor(econQuote(ECON, ECON.regions, econGood, town, id, cnt, false, ctx.scale))
      vaultEndow(ECON, ctx.culture, b)
      let r = vaultBorrow(b, who, n, { value: value, items: [{ id: id, n: cnt }] }, age)
      if (!r.ok) return p.tell(Text.red(r.why))
      p.server.runCommandSilent(`clear ${p.username} ${id} ${cnt}`)
      econGive(p, n, ctx)
      p.tell(Text.green(`The vault lends ${n} ${ctx.unit} against ${cnt} ${stack.hoverName.string} (worth ${value}); pay it back with /bank repay to take them home.`))
    }))))
    .then(amount('repay', (p, n, ctx, b, who) => {
      if (b.goods[who]) {
        n = Math.min(n, econPurse(p, ctx))
        if (!n || !econTake(p, n, ctx)) return p.tell(Text.red('You have nothing to pay with.'))
        let r = vaultRepay(b, who, n)
        econGive(p, n - r.taken, ctx)
        r.items.forEach(i => p.server.runCommandSilent(`give ${p.username} ${i.id} ${i.n}`))
        return p.tell(Text.green(r.items.length ? 'The debt is paid and the goods are yours again.' : `Paid ${Math.floor(r.taken)} ${ctx.unit}; the vault still holds your goods.`))
      }
      n = Math.min(n, econPurse(p, ctx))
      if (!n || !econTake(p, n, ctx)) return p.tell(Text.red('You have nothing to pay with.'))
      let paid = bankRepay(b, who, n)
      econGive(p, n - paid, ctx) // anything over the debt comes back
      econMint(ECON, ctx.culture, -paid)
      p.tell(Text.green(`Paid ${Math.floor(paid)} ${ctx.unit}.`))
    })))
})

// /bond: the culture's treasury borrows from players (05_bonds.js), Age V on. Buy at a banker's store; coupons collect there too.
ServerEvents.commandRegistry(event => {
  let { commands: Commands, arguments: Arguments } = event
  let atBanker = fn => cx => {
    let p = cx.source.getPlayerOrException()
    if (!econNearStore(p)) return 1
    let m = econMarketOf(p), ctx = econCtx(m)
    if (!m.culture) p.tell(Text.red('There are no bonds outside a culture\'s towns.'))
    else if (!econAgeAtLeast('V')) p.tell(Text.red('No realm sells bonds in this Age.'))
    else { fn(p, cx, ctx, econBonds(ECON, ctx.culture), bankKey(p)); econSave() }
    return 1
  }
  event.register(Commands.literal('bond')
    .executes(atBanker((p, cx, ctx, b, who) => {
      let rate = bondRate(ECON, ctx.culture, bankRates(econBank(ECON, ctx.culture), econAge()).policy)
      let mine = b.list.filter(x => x.who === who)
      p.tell(Text.gold(`${econCultureName(ctx.culture)} bonds, in ${ctx.unit}: ${(rate * 100).toFixed(1)}% a year for ${BOND_WEEKS} weeks${b.blot > 0 ? ' (the realm has defaulted lately)' : ''}.`))
      p.tell(Text.white((mine.length ? `You hold ${mine.length}, face ${Math.floor(mine.reduce((t, x) => t + x.face, 0))}. ` : 'You hold none. ') + `Waiting to collect: ${Math.floor(b.due[who] || 0)}.`))
    }))
    .then(Commands.literal('buy').then(Commands.argument('amount', Arguments.INTEGER.create(event)).executes(atBanker((p, cx, ctx, b, who) => {
      let n = Math.max(1, Arguments.INTEGER.getResult(cx, 'amount'))
      if (!econTake(p, n, ctx)) return p.tell(Text.red(`You have ${econPurse(p, ctx)} ${ctx.unit}.`))
      let bond = bondIssue(ECON, ctx.culture, who, n, bankRates(econBank(ECON, ctx.culture), econAge()).policy)
      p.tell(Text.green(`Bought a bond of ${n} ${ctx.unit} at ${(bond.rate * 100).toFixed(1)}% a year.`))
    }))))
    .then(Commands.literal('collect').executes(atBanker((p, cx, ctx, b, who) => {
      let n = Math.floor(bondCollect(ECON, ctx.culture, who))
      if (n > 0) econGive(p, n, ctx)
      p.tell(Text.green(`Collected ${n} ${ctx.unit}.`))
    }))))
})
