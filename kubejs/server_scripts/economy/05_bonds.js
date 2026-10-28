// Government bonds (Age V on, with the banks). A culture's treasury borrows from players: the buyer pays the face value now, the treasury
// pays a coupon every week and the face value back at maturity. The coupon is the policy rate plus a premium that grows with the
// culture's arrears, so a realm that cannot pay its wage bill borrows dearer. A bond whose coupons go unpaid for eight weeks is written
// off and the realm's name suffers (its premium stays high for a year). Pure functions over state.bonds[culture].

const BOND_WEEKS = 26
const BOND_DEFAULT_LATE = 8
const BOND_MIN_RATE = 0.005

const econBonds = (state, culture) => { state.bonds = state.bonds || {}; return state.bonds[culture] = state.bonds[culture] || { list: [], due: {}, blot: 0 } }

// Yearly coupon the culture must offer now.
function bondRate(state, culture, policy) {
  let s = econCur(state, culture), b = econBonds(state, culture)
  let premium = Math.min(0.25, 4 * s.arrears / Math.max(1, s.ref)) + (b.blot > 0 ? 0.05 : 0)
  return Math.max(BOND_MIN_RATE, policy) + 0.01 + premium
}

// Buy a bond of `face` units. The caller takes the money from the purse. Returns the bond, or null when the buyer's offer is too small.
function bondIssue(state, culture, who, face, policy) {
  if (face < 1) return null
  let s = econCur(state, culture), b = econBonds(state, culture)
  econMint(state, culture, -face) // out of circulation, into the treasury
  s.treasury += face
  let bond = { who: who, face: face, rate: bondRate(state, culture, policy), left: BOND_WEEKS, late: 0 }
  b.list.push(bond)
  return bond
}

// Once a week: coupons and redemptions come out of the treasury. Returns notes.
function bondWeek(state, culture) {
  let s = econCur(state, culture), b = econBonds(state, culture), notes = []
  if (b.blot > 0) b.blot--
  b.list = b.list.filter(bond => {
    let coupon = bond.face * bond.rate / 52
    if (s.treasury >= coupon) { s.treasury -= coupon; b.due[bond.who] = (b.due[bond.who] || 0) + coupon; bond.late = 0 }
    else { s.arrears += coupon; bond.late++ }
    if (bond.late >= BOND_DEFAULT_LATE) { b.blot = 52; notes.push('default: ' + bond.who); return false }
    if (--bond.left > 0) return true
    if (s.treasury >= bond.face) { s.treasury -= bond.face; b.due[bond.who] = (b.due[bond.who] || 0) + bond.face; return false }
    s.arrears += bond.face
    bond.left = 1 // matured but unpaid: it stays on the books and counts late
    bond.late++
    return true
  })
  return notes
}

// Coupons and redemptions waiting for a holder; the caller adds them to the purse.
function bondCollect(state, culture, who) {
  let b = econBonds(state, culture), n = b.due[who] || 0
  b.due[who] = 0
  econMint(state, culture, n)
  return n
}
