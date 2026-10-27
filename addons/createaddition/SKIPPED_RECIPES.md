# Skipped recipes

Reasons by code: `type createaddition:liquid_burning` is the diesel/liquid burner fuel registry, which depends on the 1.21 fluid registry the port does not ship (fluids are not ported yet); `fluid` is the bioethanol/seed_oil fluids, same cause; `type create:item_application` is a Create 1.21 recipe type absent from the Create port; `immersiveengineering:`, `mekanism:`, `jeed:`, `tconstruct`/`thermal` compat entries target mods that do not exist on 26.1; `createaddition:digital_adapter`, `portable_energy_interface`, `servo_motor`, `electric_pump` are items not ported (they need Create 1.21 internals the port replaced). The two tuff crushing recipes are now ported via create:copper_nugget -> minecraft:copper_nugget.

- `compacting/seed_oil.json`: fluid
- `compat/immersiveengineering/item_application/kiln_brick.json`: type create:item_application
- `compat/immersiveengineering/item_application/leaded_concrete.json`: type create:item_application
- `compat/immersiveengineering/item_application/reinforced_blast_brick.json`: type create:item_application
- `compat/immersiveengineering/sphalerite.json`: type immersiveengineering:mineral_mix
- `compat/jeed/shocking.json`: type jeed:effect_provider
- `compat/mekanism/rose_quartz_enriching.json`: type mekanism:enriching
- `compat/mekanism/rose_quartz_metallurgic_infusing.json`: type mekanism:metallurgic_infusing
- `compat/tconstruct/blaze_blood.json`: type createaddition:liquid_burning
- `compat/thermal/liquid_burning/heavy_oil.json`: type createaddition:liquid_burning
- `compat/thermal/liquid_burning/light_oil.json`: type createaddition:liquid_burning
- `compat/thermal/liquid_burning/refined_fuel.json`: type createaddition:liquid_burning
- `compat/thermal/liquid_burning/tree_oil.json`: type createaddition:liquid_burning
- `crafting/digital_adapter.json`: createaddition:digital_adapter
- `crafting/portable_energy_interface.json`: createaddition:portable_energy_interface
- `crafting/servo_motor.json`: createaddition:servo_motor
- `liquid_burning/biodiesel.json`: type createaddition:liquid_burning
- `liquid_burning/biofuel.json`: type createaddition:liquid_burning
- `liquid_burning/creosote.json`: type createaddition:liquid_burning
- `liquid_burning/crude_oil.json`: type createaddition:liquid_burning
- `liquid_burning/diesel.json`: type createaddition:liquid_burning
- `liquid_burning/ethanol.json`: type createaddition:liquid_burning
- `liquid_burning/gasoline.json`: type createaddition:liquid_burning
- `liquid_burning/lava.json`: type createaddition:liquid_burning
- `liquid_burning/plantoil.json`: type createaddition:liquid_burning
- `mechanical_crafting/electric_pump.json`: createaddition:electric_pump
- `mixing/bioethanol.json`: fluid
