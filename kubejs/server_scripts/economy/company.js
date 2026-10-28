// /company: found a company, fund it, run lines between the towns of your culture, borrow from the bank. The rules and the weekly run are in 07_company.js.
// Money is in the home culture's units; funding, withdrawing and founding happen in that culture's towns.

// Companies are always on, and they come as quests: a worker asks for safe transport (caravan), and finishing that opens "Found a company". Both are misc; ignore either.
// Admin-only escape hatch, not the opt-in: kubejs/config/diaithir_company.json {"enabled": false, "requireOffice": false}.
let COMPANY_ON = true, COMPANY_OFFICE_ON = true
try { let c = econRead('kubejs/config/diaithir_company.json'); if (c && c.enabled === false) COMPANY_ON = false; if (c && c.requireOffice === false) COMPANY_OFFICE_ON = false } catch (e) { }
const companyBlocks = level => (x, y, z) => {
  let id = String(level.getBlock(x, y, z).id)
  return { id: id, passable: /:(air|cave_air|void_air)$/.test(id) || /(torch|lantern|carpet|button|sign|banner|flower|rail|pressure_plate|grass|vine|candle)/.test(id) && !/_door$/.test(id) }
}
const COMPANY_QUEST = 'found_company'
function companyQuest(p, set) {
  ECON.quests = ECON.quests || {}
  let q = ECON.quests[String(p.uuid)] = ECON.quests[String(p.uuid)] || {}
  if (set) q[COMPANY_QUEST] = set
  return q[COMPANY_QUEST]
}
// The way in is a person, not a menu. After a few talks with working people, one of them asks for safe transport: they hand over a
// load and name a town. Deliver it to a keeper there and the carrying fee is paid from that town's wage pool. Doing it well opens
// the company quest. Any profession can ask; CARAVAN_JOBS has the goods and the complaint for each.
const CARAVAN_QTY = 12, CARAVAN_WEEKS = 4
function caravanState(p) {
  companyQuest(p)
  return ECON.quests[String(p.uuid)]
}
function caravanTalk(p, keeper) {
  if (!COMPANY_ON) return
  let q = caravanState(p), m = econMarketOf(p)
  if (!m.culture || q.caravan === 'done') return
  let job = q.job
  if (job && q.caravan === 'active') {
    if (m.town !== job.to) return
    let have = econCount(p, job.id)
    if (have < job.n) return p.tell(Text.gray(`${job.name} is expecting ${job.n} ${Item.of(job.id).hoverName.string} from ${job.fromName}. You carry ${have}.`))
    p.server.runCommandSilent(`clear ${p.username} ${job.id} ${job.n}`)
    let ctx = econCtx(m), late = (ECON.week || 0) - job.week > CARAVAN_WEEKS
    let tw = caravanReward((ECON_PRICES[job.id] || [1])[0], job.n, late)
    let paid = econPayWage(ECON, m.culture, tw)
    if (paid) econGive(p, Math.ceil(paid * ctx.scale), ctx)
    q.caravan = 'done'; q.job = null
    p.tell(Text.green(late ? 'The load arrives late but whole.' : 'The load arrives safe and on time.').append(Text.gray(paid ? ` You are paid ${Math.ceil(paid * ctx.scale)} ${ctx.unit}.` : ' The town\'s till is empty, so it owes you.')))
    p.tell(Text.gold('"You do this well. You could do it for a living, with an office, a ledger and a few carts."'))
    p.tell(Text.gold('New misc quest: Found a company.').append(Text.gray(' See /company quest.')))
    companyQuest(p, 'offered')
    try { p.server.runCommandSilent('execute as ' + p.username + ' run ages quest report caravan delivered') } catch (e) { }
    return econSave()
  }
  q.talks = (q.talks || 0) + 1
  if (q.talks < 3) return
  let to = caravanDest(ECON_CLAIMS, m.town)
  if (!to) return
  let j = caravanJob(econKeeperProfession(keeper), econLocalAge(m.culture))
  q.talks = 0; q.caravan = 'active'
  q.job = { id: j.id, n: CARAVAN_QTY, from: m.town, fromName: m.name, to: to.town, name: to.name, week: ECON.week || 0 }
  p.tell(Text.of('"' + j.line + '"').color('white'))
  p.tell(Text.gold('New misc quest: Safe passage.').append(Text.gray(` Take ${CARAVAN_QTY} ${Item.of(j.id).hoverName.string} to ${to.name}, within ${CARAVAN_WEEKS} weeks. Talk to anyone there who works.`)))
  p.give(Item.of(j.id, CARAVAN_QTY))
  try { p.server.runCommandSilent('execute as ' + p.username + ' run ages quest report caravan accepted') } catch (e) { }
  econSave()
}
const companyKey = p => String(p.uuid)
const companyEnv = () => ({
  state: ECON, regions: ECON.regions, good: econGood,
  idsOf: culture => { let a = econLocalAge(culture); if (!ECON_OPEN_CACHE[a]) ECON_OPEN_CACHE[a] = econOpenGoods(a); return ECON_OPEN_CACHE[a] },
  scaleOf: culture => { let c = ECON_CLAIMS.filter(x => x.culture === culture)[0]; return c ? econCtx({ town: c.town, culture: culture }).scale : 1 },
  cur: culture => econCur(ECON, culture)
})
const companyKnown = culture => tech => {
  try { if (typeof global !== 'undefined' && typeof global.TECH_KNOWN === 'function') return !!global.TECH_KNOWN(culture, tech) } catch (e) { }
  return true
}
const companyTowns = culture => ECON_CLAIMS.filter(c => c.culture === culture)

function companyShow(p, co) {
  let unit = econCtx({ town: (companyTowns(co.home)[0] || {}).town, culture: co.home }).unit
  p.tell(Text.gold(`${co.name}${co.closed ? ' (bankrupt)' : ''}: ${Math.floor(co.money)} ${unit}, owes ${Math.floor(co.loan)}.`))
  if (!co.lines.length) return p.tell(Text.gray('No lines. /company towns, /company vehicles, then /company line <from> <to> <vehicle>.'))
  co.lines.forEach((l, i) => {
    let v = COMPANY_VEHICLES.filter(x => x.id === l.vehicle)[0]
    p.tell(Text.white(`${i + 1}. ${l.from} to ${l.to}, ${l.n} x ${v ? v.name : l.vehicle}: ${l.units || 0} units hauled over ${l.weeks || 0} weeks, profit ${Math.floor(companyProfit(l))}${l.cargo ? ', now ' + Item.of(l.cargo).hoverName.string : ''}.`))
  })
}

ServerEvents.commandRegistry(event => {
  let { commands: Commands, arguments: Arguments } = event
  let run = fn => cx => { let p = cx.source.getPlayerOrException(); if (!COMPANY_ON) { p.tell(Text.gray('Companies are switched off on this server.')); return 1 } fn(p, cx); econSave(); return 1 }
  let mine = (p, then) => { let co = companyOf(ECON, companyKey(p)); if (!co) return p.tell(Text.red('You have no company. /company found <name> in a built office.')); if (co.closed) return p.tell(Text.red('Your company is bankrupt.')); then(co) }
  let num = (cx, name) => Math.max(1, Arguments.INTEGER.getResult(cx, name))
  let str = (cx, name) => String(Arguments.STRING.getResult(cx, name))
  let amount = (name, fn) => Commands.literal(name).then(Commands.argument('amount', Arguments.INTEGER.create(event)).executes(run((p, cx) => mine(p, co => fn(p, co, num(cx, 'amount'), econMarketOf(p))))))
  event.register(Commands.literal('company')
    .executes(run((p) => { let co = companyOf(ECON, companyKey(p)); if (co) companyShow(p, co); else p.tell(Text.gray('No company yet. /company found <name> in a built office.')) }))
    .then(Commands.literal('quest').executes(run((p) => {
      let m = econMarketOf(p), done = !!companyOf(ECON, companyKey(p))
      if (done) return p.tell(Text.green('Found a company: done.'))
      let cs = caravanState(p)
      if (cs.caravan === 'active') return p.tell(Text.gold('Safe passage (misc quest). ').append(Text.gray(`Take ${cs.job.n} ${Item.of(cs.job.id).hoverName.string} from ${cs.job.fromName} to ${cs.job.name}.`)))
      if (companyQuest(p) !== 'offered') return p.tell(Text.gray('No quest yet. Talk to the working people in the towns.'))
      if (!m.culture) return p.tell(Text.gray('Found a company (misc quest). Stand in the office you build, in a culture\'s town, to see what it still needs.'))
      let ctx0 = econCtx(m), age = econLocalAge(m.culture)
      let r = companyOfficeCheck(companyBlocks(p.level), [Math.floor(p.x), Math.floor(p.y), Math.floor(p.z)], age, companyKnown(m.culture))
      p.tell(Text.gold('Found a company (misc quest). Build an office, stand in it, then /company found <name>.'))
      p.tell(Text.gray(` - starting capital ${r.fee} ${ctx0.unit} (you have ${econPurse(p, ctx0)})`))
      if (r.ok) p.tell(Text.green(' - the office is ready'))
      else r.missing.forEach(x => p.tell(Text.red(' - ' + x)))
    })))
    .then(Commands.literal('found').then(Commands.argument('name', Arguments.STRING.create(event)).executes(run((p, cx) => {
      let m = econMarketOf(p)
      if (!m.culture) return p.tell(Text.red('A company is founded in a culture\'s town.'))
      if (companyOf(ECON, companyKey(p))) return p.tell(Text.red('You have a company already.'))
      if (COMPANY_OFFICE_ON && companyQuest(p) !== 'offered') return p.tell(Text.red('No one has asked you to found a company yet. Help the people in the towns first: talk to the working people.'))
      let fee = 0
      if (COMPANY_OFFICE_ON) {
        let ctx0 = econCtx(m)
        let r = companyOfficeCheck(companyBlocks(p.level), [Math.floor(p.x), Math.floor(p.y), Math.floor(p.z)], econLocalAge(m.culture), companyKnown(m.culture))
        if (!r.ok) { p.tell(Text.red('You cannot found a company here yet. Build an office and stand in it. It still needs:')); r.missing.forEach(x => p.tell(Text.gray(' - ' + x))); return }
        fee = r.fee
        if (!econTake(p, fee, ctx0)) return p.tell(Text.red(`The charter needs ${fee} ${ctx0.unit} as starting capital. You have ${econPurse(p, ctx0)}.`))
      }
      let co = companyFound(ECON, companyKey(p), str(cx, 'name'), m.culture)
      co.money += fee
      companyQuest(p, 'done')
      try { p.server.runCommandSilent('execute as ' + p.username + ' run ages quest report company found') } catch (e) { }
      p.tell(Text.green(`${co.name} is founded in ${m.name}${fee ? ' with ' + fee + ' in the till' : ''}. Fund it with /company fund <amount>.`))
    }))))
    .then(amount('fund', (p, co, n, m) => {
      let ctx = econCtx(m)
      if (m.culture !== co.home) return p.tell(Text.red('Fund the company in its home culture.'))
      if (!econTake(p, n, ctx)) return p.tell(Text.red(`You have ${econPurse(p, ctx)} ${ctx.unit}.`))
      co.money += n
      p.tell(Text.green(`Paid in ${n} ${ctx.unit}.`))
    }))
    .then(amount('withdraw', (p, co, n, m) => {
      let ctx = econCtx(m)
      if (m.culture !== co.home) return p.tell(Text.red('Take money out in the home culture.'))
      n = Math.min(n, Math.floor(co.money))
      if (n < 1) return p.tell(Text.red('The company has no money to spare.'))
      co.money -= n
      econGive(p, n, ctx)
      p.tell(Text.green(`Took out ${n} ${ctx.unit}.`))
    }))
    .then(Commands.literal('towns').executes(run((p) => {
      let co = companyOf(ECON, companyKey(p)), culture = co ? co.home : econMarketOf(p).culture
      if (!culture) return p.tell(Text.red('Stand in a culture\'s town or found a company.'))
      let ids = companyEnv().idsOf(culture)
      companyTowns(culture).forEach(c => {
        let gaps = ids.map(id => [id, companyGap(ECON, econGood, c.town, id)])
        let want = gaps.filter(g => g[1] < 0).sort((a, b) => a[1] - b[1]).slice(0, 3).map(g => `${Item.of(g[0]).hoverName.string} ${Math.floor(-g[1])}`).join(', ')
        let have = gaps.filter(g => g[1] > 0).sort((a, b) => b[1] - a[1]).slice(0, 3).map(g => `${Item.of(g[0]).hoverName.string} ${Math.floor(g[1])}`).join(', ')
        p.tell(Text.white(`${c.town} (${c.name}): wants ${want || 'nothing'}; has spare ${have || 'nothing'}.`))
      })
    })))
    .then(Commands.literal('vehicles').executes(run((p) => {
      let co = companyOf(ECON, companyKey(p)), culture = co ? co.home : econMarketOf(p).culture
      if (!culture) return p.tell(Text.red('Stand in a culture\'s town or found a company.'))
      companyVehicles(econLocalAge(culture), companyKnown(culture), companyYear()).forEach(v =>
        p.tell(Text.white(`${v.id}: ${v.name}, carries ${v.cap}, ${v.speed} blocks a day, costs ${v.cost} tw, upkeep ${v.upkeep} tw a week.`)))
    })))
    .then(Commands.literal('line').then(Commands.argument('from', Arguments.STRING.create(event)).then(Commands.argument('to', Arguments.STRING.create(event))
      .then(Commands.argument('vehicle', Arguments.STRING.create(event)).executes(run((p, cx) => mine(p, co => {
        let from = str(cx, 'from'), to = str(cx, 'to'), vid = str(cx, 'vehicle'), towns = companyTowns(co.home).map(c => c.town)
        if (from === to || towns.indexOf(from) < 0 || towns.indexOf(to) < 0) return p.tell(Text.red('A line joins two different towns of your culture (/company towns).'))
        let v = companyVehicles(econLocalAge(co.home), companyKnown(co.home), companyYear()).filter(x => x.id === vid)[0]
        if (!v) return p.tell(Text.red('That vehicle is not open to your culture yet (/company vehicles).'))
        let price = Math.ceil(v.cost * companyEnv().scaleOf(co.home))
        if (co.money < price) return p.tell(Text.red(`A ${v.name} costs ${price}; the company has ${Math.floor(co.money)}.`))
        co.money -= price
        econCur(ECON, co.home).treasury += price
        co.lines.push({ from: from, to: to, vehicle: vid, n: 1 })
        p.tell(Text.green(`Line ${co.lines.length} opened: ${from} to ${to} with a ${v.name}.`))
      })))))))
    .then(amount('add', (p, co, n) => {
      let l = co.lines[n - 1], v = l && COMPANY_VEHICLES.filter(x => x.id === l.vehicle)[0]
      if (!l) return p.tell(Text.red('No such line.'))
      let price = Math.ceil(v.cost * companyEnv().scaleOf(co.home))
      if (co.money < price) return p.tell(Text.red(`Another ${v.name} costs ${price}; the company has ${Math.floor(co.money)}.`))
      co.money -= price
      econCur(ECON, co.home).treasury += price
      l.n++
      p.tell(Text.green(`Line ${n} now runs ${l.n} ${v.name}s.`))
    }))
    .then(amount('borrow', (p, co, n, m) => {
      if (m.culture !== co.home) return p.tell(Text.red('Borrow in the home culture.'))
      let b = econBank(ECON, co.home), who = 'co:' + companyKey(p), age = econLocalAge(co.home)
      if (bankReserve(age) >= 1) { // a temple vault lends from its own fund against the company's vehicles, and makes no money
        vaultEndow(ECON, co.home, b)
        let r = vaultBorrow(b, who, n, { value: companyAssets(co), items: [] }, age)
        if (!r.ok) return p.tell(Text.red(r.why))
        co.money += n; co.loan = b.goods[who].owed
        return p.tell(Text.green(`The temple lends ${n} against your carts; ${Math.floor(co.loan)} to repay.`))
      }
      let need = Math.ceil(n * BANK_COLLATERAL)
      if (co.money < need) return p.tell(Text.red(`A loan of ${n} needs ${need} kept in the bank; the company has ${Math.floor(co.money)}.`))
      bankDeposit(b, who, need)
      let r = bankBorrow(b, who, n, age)
      if (!r.ok) { bankWithdraw(b, who, need); return p.tell(Text.red(r.why)) }
      co.money += n - need // the collateral stays with the bank
      co.loan = b.loans[who].owed
      econMint(ECON, co.home, n)
      p.tell(Text.green(`The bank lends ${n}, keeping ${need} as collateral; ${Math.floor(co.loan)} to repay.`))
    }))
    .then(amount('repay', (p, co, n, m) => {
      let b = econBank(ECON, co.home), who = 'co:' + companyKey(p)
      if (b.goods[who]) {
        let r = vaultRepay(b, who, Math.min(n, Math.floor(co.money)))
        co.money -= r.taken; co.loan = b.goods[who] ? b.goods[who].owed : 0
        return p.tell(Text.green(`Repaid ${Math.floor(r.taken)}; ${Math.floor(co.loan)} still owed.`))
      }
      let paid = bankRepay(b, who, Math.min(n, Math.floor(co.money)))
      co.money -= paid
      econMint(ECON, co.home, -paid)
      if (!b.loans[who]) co.money += bankWithdraw(b, who, b.dep[who] || 0)
      co.loan = b.loans[who] ? b.loans[who].owed : 0
      p.tell(Text.green(`Repaid ${Math.floor(paid)}; ${Math.floor(co.loan)} still owed.`))
    }))
  )
})
