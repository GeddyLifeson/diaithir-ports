Per-item gear art for the parts benches (ports #178 guns, #182 melee, #185 tools). 884 blueprints x their slots = grayscale 64 px layers.

Format (same as #180, just per blueprint): textures/item/workshop/<blueprint_id>_<slot>.png (slots as in the blueprint: head/guard/handle/binding, barrel/lock/stock, pot/handle), models/item/workshop/<id>.json, items/workshop/<id>.json (tints per layer, same defaults as gen_workshop_assets.py). To use: set each blueprint's "model" field to its own id (model_map.json lists id -> id) and copy assets/ into diaithir-gear resources. Ranged and siege weapons are not included yet.

Silhouettes come from API-written shape specs (tools/mkjobs.py prompts) drawn by tools/{guns,melee,tools}.py. First pass, not seen in a client; 17 items used a generic fallback shape (specs missing) and will be redrawn.
