// Make Create mixing recipes (heated basin) from the drinks KubeJS scripts, running their real code.
//
// usage: node gen_create_bridge_kubejs.mjs <script dir with a_data.js + brewing.js> <out data dir> [--have items.txt]
//
// brewing.js already builds one hand-fed keg/barrel brew per age I/II vat drink, each with its own ingredient
// set (event.json of 'before_mods' generateData). This runs that script with KubeJS stubbed out and turns each
// of those brews into a heated-basin create:mixing recipe: one bucket of water plus the same items, 3 servings.
// --have lists the item ids the pack has (one per line); without it every item is taken to exist.
// A brew with more than 9 items (Create's limit) is skipped and reported.
import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';

const [dir, out] = process.argv.slice(2, 4);
const haveFile = process.argv.includes('--have') ? process.argv[process.argv.indexOf('--have') + 1] : null;
const have = haveFile ? new Set(fs.readFileSync(haveFile, 'utf8').split(/\s+/).filter(Boolean)) : null;
const BUCKET = 81000; // fluid units in the port: 20250 per 250 mb
const MAX_ITEMS = 9;

const noop = () => {};
let generate = null;
const sandbox = {
  console: { info: noop, warn: noop, log: noop },
  Item: { exists: id => !have || have.has(id) },
  ServerEvents: { generateData: (_stage, cb) => { generate = cb; }, loaded: noop, recipes: noop },
  BlockEvents: { rightClicked: noop, broken: noop },
};
vm.createContext(sandbox);
const src = ['a_data.js', 'brewing.js'].map(f => fs.readFileSync(path.join(dir, f), 'utf8')).join('\n');
vm.runInContext(src, sandbox);
if (!generate) throw new Error('brewing.js registered no generateData handler');

const brews = [];
generate({ json: (id, body) => brews.push([id, body]) });

const dest = path.join(out, 'create', 'recipe', 'mixing');
fs.mkdirSync(dest, { recursive: true });
let made = 0;
for (const [id, b] of brews) {
  const name = id.split('/').pop();
  const items = Object.entries(b.ingredients).flatMap(([item, n]) => Array(n).fill(item));
  if (items.length > MAX_ITEMS) { console.log(`skip ${name}: ${items.length} items, Create allows ${MAX_ITEMS}`); continue; }
  const rec = {
    'neoforge:conditions': [{ type: 'neoforge:mod_loaded', modid: 'diaithir_create' }],
    type: 'create:mixing',
    heat_requirement: 'heated',
    processing_time: Math.min(2400, Math.max(20, Math.floor(b.ticks / 20))),
    ingredients: items,
    fluid_ingredients: [{ type: 'fluid_stack', amount: BUCKET, fluid: 'minecraft:water' }],
    results: [{ id: b.drink, count: b.servings }],
  };
  fs.writeFileSync(path.join(dest, `brew_${name}.json`), JSON.stringify(rec, null, 1) + '\n');
  made++;
}
console.log(`${made} written, ${brews.length - made} skipped`);
