// Converts recipe JSON written for 1.21.1 mods to their 26.1 formats. Anything else is returned untouched.
// Mekanism 10.8: item and fluid inputs become {count|amount, ingredient}, item outputs use "id"; chemical inputs are unchanged.
// Modern Industrialization 26.1: inputs become {ingredient, amount}, outputs {id, amount}.
// Used by the ATM11 server scripts as allthemods.custom(global.port261({...})).

const MEK108_ITEM_INPUTS = ["input", "item_input", "main_input", "extra_input"]
const MEK108_OUTPUTS = ["output", "main_output", "secondary_output", "item_output"]

function mek108Plain(v) {
  return Object.prototype.toString.call(v) === "[object Object]" // a JS object literal, not a wrapped Java object such as an ItemStack
}

function mek108ItemInput(v) {
  if (v == null) return v
  if (!mek108Plain(v)) return { count: 1, ingredient: v } // an id or tag string, or a wrapped Ingredient
  if (v.ingredient !== undefined || v.amount !== undefined) return v
  if (v.item !== undefined) return { count: v.count || 1, ingredient: v.item }
  if (v.tag !== undefined) return { count: v.count || 1, ingredient: `#${v.tag}` }
  return v
}

function mek108FluidInput(v) {
  if (!mek108Plain(v) || v.ingredient !== undefined) return v
  if (v.tag !== undefined) return { amount: v.amount, ingredient: `#${v.tag}` }
  if (v.fluid !== undefined) return { amount: v.amount, ingredient: v.fluid }
  return v
}

function mi261Input(v) {
  if (!mek108Plain(v) || v.ingredient !== undefined) return v
  const ingredient = v.item !== undefined ? v.item : v.fluid !== undefined ? v.fluid : v.tag !== undefined ? `#${v.tag}` : undefined
  if (ingredient === undefined) return v
  const out = { ingredient: ingredient, amount: v.amount !== undefined ? v.amount : v.count !== undefined ? v.count : 1 }
  if (v.probability !== undefined) out.probability = v.probability
  return out
}

function mi261Output(v) {
  if (!mek108Plain(v) || v.id !== undefined) return v
  const id = v.item !== undefined ? v.item : v.fluid
  if (id === undefined) return v
  const out = { id: id, amount: v.amount !== undefined ? v.amount : v.count !== undefined ? v.count : 1 }
  if (v.probability !== undefined) out.probability = v.probability
  return out
}

function mi261(json) {
  const map = (v, f) => (Array.isArray(v) ? v.map(f) : f(v))
  for (const k of ["item_inputs", "fluid_inputs"]) {
    if (json[k] !== undefined) json[k] = map(json[k], mi261Input)
  }
  for (const k of ["item_outputs", "fluid_outputs"]) {
    if (json[k] !== undefined) json[k] = map(json[k], mi261Output)
  }
  return json
}

global.port261 = (json) => {
  if (!mek108Plain(json) || typeof json.type !== "string") return json
  if (json.type.indexOf("modern_industrialization:") === 0) return mi261(json)
  if (json.type.indexOf("mekanism:") !== 0) return json
  MEK108_ITEM_INPUTS.forEach((k) => {
    if (json[k] !== undefined) json[k] = mek108ItemInput(json[k])
  })
  if (json.fluid_input !== undefined) json.fluid_input = mek108FluidInput(json.fluid_input)
  MEK108_OUTPUTS.forEach((k) => {
    const out = json[k]
    if (mek108Plain(out) && out.item !== undefined && out.id === undefined) {
      out.id = out.item
      delete out.item
    }
  })
  return json
}
