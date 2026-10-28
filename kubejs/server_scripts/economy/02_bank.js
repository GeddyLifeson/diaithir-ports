// Banks, credit and interest. Pure functions over the ECON state, one bank per culture (state.bank[culture]).
//
// Age III-IV: a temple vault. It keeps money safe for a fee and lends nothing out (reserve ratio 1), so it creates no money.
// Age V: a fractional-reserve bank. It keeps 20% of deposits and may lend the rest; a loan creates money (the borrower's purse
//   fills and the circulation grows) and repaying destroys it. Age VI-VII: 10% reserve and a central rate that follows inflation.
// The rate. Policy rate per year = expected inflation + 2% (floor 0.5%): borrowers pay it plus a premium that grows as the bank
//   lends out more of what it may; depositors earn the policy rate less the bank's spread, only once the bank lends at all.
//   Expected inflation is last week's fall in the money's rate, annualised.
// A loan is secured: half of it stays locked in the borrower's deposit. Eight weeks without a payment is default: the locked
//   deposit is taken, and any loss comes out of the bank's equity. If equity goes below zero the bank fails: depositors get back
//   what the bank's reserves can pay, pro rata, and no one can borrow from it again.

const BANK_RESERVE = { III: 1, IV: 1, V: 0.2, VI: 0.1, VII: 0.1, VIII: 0.1, RODOS: 0.1 }
const BANK_FEE_WEEK = 0.0005 // storage fee on a temple vault deposit
const BANK_COLLATERAL = 0.5
const BANK_DEFAULT_WEEKS = 8
const BANK_SPREAD = 0.015
const VAULT_RATE = 0.06 // yearly interest on a loan against goods
const VAULT_COVER = 1.5 // the pledge must be worth this much of the loan
const VAULT_SALE = 0.8 // what a forfeited pledge fetches, of its pledged value
const BANK_LOAN_FEE = 0.01 // arranging a loan costs the borrower this much extra, owed with the rest

function econBank(state, culture) {
  state.bank = state.bank || {}
  return state.bank[culture] = state.bank[culture] || { dep: {}, loans: {}, equity: 0, failed: false, infl: 0, lastRate: 0, fund: 0, goods: {} }
}

const bankReserve = age => BANK_RESERVE[age] || 0
const bankDeposits = b => Object.keys(b.dep).reduce((t, k) => t + b.dep[k], 0)
const bankLoans = b => Object.keys(b.loans).reduce((t, k) => t + b.loans[k].owed, 0)
const bankLocked = (b, who) => b.loans[who] ? b.loans[who].collateral : 0

// How much more the bank may lend: deposits / reserve ratio is the most it can have out in all, less what it has.
function bankCapacity(b, age) {
  let rr = bankReserve(age)
  if (!rr || b.failed) return 0
  return Math.max(0, bankDeposits(b) * (1 / rr - 1) - bankLoans(b))
}

// Rates per year. Utilisation is loans over what it may lend at most.
function bankRates(b, age) {
  let rr = bankReserve(age)
  let policy = Math.max(0.005, b.infl + 0.02)
  let use = rr && bankDeposits(b) ? Math.min(1, bankLoans(b) / (bankDeposits(b) * (1 / rr - 1))) : 0
  let lend = policy + 0.01 + 0.08 * use * use
  return { policy: policy, lend: lend, save: rr < 1 && bankLoans(b) > 0 ? Math.max(0, policy - BANK_SPREAD) : 0 }
}

// Put money in (the caller takes it from the purse). Returns false at a failed bank.
function bankDeposit(b, who, amount) {
  if (b.failed || amount <= 0) return false
  b.dep[who] = (b.dep[who] || 0) + amount
  return true
}

// Take money out: what is not locked as collateral. Returns the amount handed over (the caller gives it to the purse).
function bankWithdraw(b, who, amount) {
  if (amount <= 0) return 0
  let free = Math.max(0, (b.dep[who] || 0) - bankLocked(b, who))
  let n = Math.min(amount, free)
  b.dep[who] = (b.dep[who] || 0) - n
  return n
}

// A loan: needs room in the bank and collateral in the borrower's own free deposit. Money is created: the caller adds the
// returned amount to the purse and to the culture's circulation (econMint). Returns { ok, amount, why }.
function bankBorrow(b, who, amount, age) {
  if (b.failed) return { ok: false, why: 'The bank has failed.' }
  if (bankReserve(age) >= 1 || !bankReserve(age)) return { ok: false, why: 'There is no lender here.' }
  if (b.loans[who]) return { ok: false, why: 'Pay what you owe first.' }
  let need = amount * BANK_COLLATERAL
  if ((b.dep[who] || 0) < need) return { ok: false, why: `A loan of ${amount} needs ${need} kept in your deposit.` }
  if (amount > bankCapacity(b, age)) return { ok: false, why: 'The bank has no more to lend.' }
  b.loans[who] = { owed: amount * (1 + BANK_LOAN_FEE), collateral: need, late: 0, paid: 0 }
  return { ok: true, amount: amount }
}

// A payment on a loan from the purse. Returns the amount taken; the caller burns that much from circulation. The loan, interest
// and all, ends when it is paid off and the collateral is freed.
function bankRepay(b, who, amount) {
  let l = b.loans[who]
  if (!l) return 0
  let n = Math.min(amount, l.owed)
  l.owed -= n
  l.paid += n
  l.late = 0
  if (l.owed <= 1e-9) delete b.loans[who]
  return n
}

// Once a week. mintFn(amount) adds to circulation (negative burns). rateNow and the previous week's set the expected inflation.
// Returns the notes: ['defaults: ...', 'The bank fails'].
function bankWeek(b, age, rateNow) {
  let notes = []
  if (b.lastRate) b.infl = Math.max(0, Math.min(1, (b.lastRate / rateNow - 1) * 52))
  b.lastRate = rateNow
  if (b.failed) return notes
  let r = bankRates(b, age)
  if (bankReserve(age) >= 1) {
    // a temple vault: the storage fee goes to the temple's fund (the money stays in the vault, none is made or lost); goods loans accrue and may be forfeited
    Object.keys(b.dep).forEach(k => { let fee = b.dep[k] * BANK_FEE_WEEK; b.dep[k] -= fee; b.fund += fee })
    Object.keys(b.goods).forEach(k => {
      let l = b.goods[k]
      l.owed += l.owed * VAULT_RATE / 52
      l.late = l.paid > 0 ? 0 : l.late + 1
      l.paid = 0
      if (l.late >= BANK_DEFAULT_WEEKS) { // the temple keeps the pledge and sells it for most of its worth
        b.fund += l.pledge.value * VAULT_SALE
        delete b.goods[k]
        notes.push('forfeit: ' + k)
      }
    })
    return notes
  }
  let income = 0
  Object.keys(b.loans).forEach(k => {
    let l = b.loans[k], interest = l.owed * r.lend / 52
    l.owed += interest
    income += interest
    l.late = l.paid > 0 ? 0 : l.late + 1
    l.paid = 0
    if (l.late >= BANK_DEFAULT_WEEKS) {
      let taken = Math.min(l.collateral, b.dep[k] || 0)
      b.dep[k] = (b.dep[k] || 0) - taken
      b.equity += taken - l.owed
      delete b.loans[k]
      notes.push('default: ' + k)
    }
  })
  let paid = 0
  Object.keys(b.dep).forEach(k => { let i = b.dep[k] * r.save / 52; b.dep[k] += i; paid += i })
  b.equity += income - paid
  if (b.equity < 0 && !b.failed) {
    b.failed = true
    let owed = bankDeposits(b) // the reserves held every deposit; the lost equity comes out of them
    let share = owed ? Math.max(0, Math.min(1, (owed + b.equity) / owed)) : 1
    Object.keys(b.dep).forEach(k => { b.dep[k] *= share })
    b.loans = {}
    notes.push('The bank fails; depositors get back ' + Math.round(share * 100) + '%.')
  }
  return notes
}

// ---- The temple vault's loan against goods (Age III-IV) ----------------------------------------------------------------------------
// The vault lends only from its own fund (storage fees and forfeited pledges), never from deposits and never by making money, and only
// against goods worth VAULT_COVER times the loan, which it keeps until the loan is paid. pledge = { value, items: [{ id, n }] }; value is
// the goods' worth in the culture's money at the market's prices, found by the caller. Returns { ok, why }.
// A temple vault starts with an endowment, once: the treasury gives what it has, then the wage pool, and the ruler makes up the rest
// (the one place new money enters, counted in circulation, held by the vault until it is lent). Without it a fresh vault has no fund to lend.
const VAULT_ENDOWMENT = 200
function vaultEndow(state, culture, b) {
  if (b.endowed) return
  b.endowed = true
  let s = econCur(state, culture), want = VAULT_ENDOWMENT
  let t = Math.min(want, s.treasury); s.treasury -= t; want -= t
  let w = Math.min(want, s.wagePool); s.wagePool -= w; want -= w
  if (want > 0) s.money += want
  b.fund += VAULT_ENDOWMENT
}

function vaultBorrow(b, who, amount, pledge, age) {
  if (bankReserve(age) < 1) return { ok: false, why: 'Only a temple vault lends against goods.' }
  if (b.failed) return { ok: false, why: 'The vault has been emptied.' }
  if (b.goods[who]) return { ok: false, why: 'Redeem your pledge first.' }
  if (amount < 1) return { ok: false, why: 'Ask for at least one coin.' }
  if (amount > b.fund) return { ok: false, why: `The temple can lend only ${Math.floor(b.fund)}.` }
  if (pledge.value < amount * VAULT_COVER) return { ok: false, why: `The goods must be worth ${Math.ceil(amount * VAULT_COVER)}; these are worth ${Math.floor(pledge.value)}.` }
  b.fund -= amount
  b.goods[who] = { owed: amount, pledge: pledge, late: 0, paid: 0 }
  return { ok: true }
}

// A payment on a goods loan. Returns { taken, items }: the money taken (the caller removes it from the purse) and, once the debt is
// cleared, the pledged items to hand back.
function vaultRepay(b, who, amount) {
  let l = b.goods[who]
  if (!l) return { taken: 0, items: [] }
  let n = Math.min(amount, l.owed)
  l.owed -= n; l.paid += n; l.late = 0
  b.fund += n
  if (l.owed > 1e-9) return { taken: n, items: [] }
  delete b.goods[who]
  return { taken: n, items: l.pledge.items }
}
