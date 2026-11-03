"""Real mass-produced semi tractor units and trailers, one iconic version per design. Data only; used by gen_trucks.py.
Tractor: (id, name, maker, year, country, layout, axles, kW, wiki page, gross kg)
layout: conv (long hood), cab (cab-over), tri (three-wheel mechanical horse). Power is the iconic engine's output, rounded.
Trailer: (id, name, year, kind, cargo, payload_kg, axles, length_m, wiki page)
kind picks the body model; cargo: crate (item slots), tank (litres), car (mounted vehicles), none (frame only)."""

TRACTORS = [
 ('scammell_scarab', 'Scammell Scarab', 'Scammell', 1933, 'UK', 'tri', 1, 22, 'Scammell_Scarab', 4500),
 ('diamond_t_980', 'Diamond T 980', 'Diamond T', 1939, 'US', 'conv', 3, 100, 'Diamond_T_980/981', 18000),
 ('foden_s21', 'Foden S21 Mickey Mouse', 'Foden', 1958, 'UK', 'cab', 3, 120, 'Foden_Trucks', 24000),
 ('mack_b61', 'Mack B-Series', 'Mack', 1953, 'US', 'conv', 2, 150, 'Mack_B_series', 30000),
 ('kenworth_w900', 'Kenworth W900', 'Kenworth', 1961, 'US', 'conv', 3, 370, 'Kenworth_W900', 36000),
 ('volvo_f88', 'Volvo F88', 'Volvo', 1965, 'SE', 'cab', 3, 260, 'Volvo_F88', 38000),
 ('maz_504', 'MAZ-504', 'MAZ', 1965, 'SU', 'cab', 2, 175, 'MAZ-504', 30000),
 ('mack_r600', 'Mack R-Series', 'Mack', 1966, 'US', 'conv', 3, 250, 'Mack_R_series', 36000),
 ('peterbilt_359', 'Peterbilt 359', 'Peterbilt', 1967, 'US', 'conv', 3, 300, 'Peterbilt_359', 36000),
 ('intl_transtar', 'International Transtar', 'International', 1969, 'US', 'cab', 3, 250, 'International_Transtar', 36000),
 ('mb_ng', 'Mercedes-Benz NG', 'Mercedes-Benz', 1973, 'DE', 'cab', 3, 240, 'Mercedes-Benz_NG', 38000),
 ('kenworth_k100', 'Kenworth K100', 'Kenworth', 1975, 'US', 'cab', 3, 300, 'Kenworth_K100', 36000),
 ('kamaz_5320', 'KamAZ-5320', 'KamAZ', 1976, 'SU', 'cab', 3, 154, 'KamAZ-5320', 22000),
 ('mack_superliner', 'Mack Super-Liner', 'Mack', 1977, 'US', 'conv', 3, 320, 'Mack_Super-Liner', 36000),
 ('tatra_815', 'Tatra 815', 'Tatra', 1982, 'CS', 'cab', 4, 210, 'Tatra_815', 40000),
 ('sinotruk_howo', 'Sinotruk Howo', 'Sinotruk', 1983, 'CN', 'cab', 3, 290, 'Sinotruk', 40000),
 ('scania_143', 'Scania 143', 'Scania', 1988, 'SE', 'cab', 3, 330, 'Scania_3-series', 40000),
 ('peterbilt_379', 'Peterbilt 379', 'Peterbilt', 1987, 'US', 'conv', 3, 335, 'Peterbilt_379', 36000),
 ('freightliner_fld120', 'Freightliner FLD120', 'Freightliner', 1989, 'US', 'conv', 3, 335, 'Freightliner_FLD', 36000),
 ('renault_magnum', 'Renault Magnum', 'Renault', 1990, 'FR', 'cab', 3, 340, 'Renault_Magnum', 40000),
 ('volvo_fh', 'Volvo FH', 'Volvo', 1993, 'SE', 'cab', 3, 340, 'Volvo_FH', 40000),
 ('isuzu_giga', 'Isuzu Giga', 'Isuzu', 1994, 'JP', 'cab', 3, 260, 'Isuzu_Giga', 25000),
 ('mercedes_actros', 'Mercedes-Benz Actros', 'Mercedes-Benz', 1996, 'DE', 'cab', 3, 290, 'Mercedes-Benz_Actros', 40000),
 ('fuso_super_great', 'Mitsubishi Fuso Super Great', 'Mitsubishi Fuso', 1996, 'JP', 'cab', 3, 280, 'Mitsubishi_Fuso_Super_Great', 25000),
 ('volvo_vnl', 'Volvo VN', 'Volvo', 1996, 'US', 'conv', 3, 335, 'Volvo_VN', 36000),
 ('daf_xf', 'DAF XF', 'DAF', 1997, 'NL', 'cab', 3, 340, 'DAF_XF', 40000),
 ('iveco_stralis', 'Iveco Stralis', 'Iveco', 2002, 'IT', 'cab', 3, 310, 'Iveco_Stralis', 40000),
 ('freightliner_cascadia', 'Freightliner Cascadia', 'Freightliner', 2007, 'US', 'conv', 3, 335, 'Freightliner_Cascadia', 36000),
 ('man_tgx', 'MAN TGX', 'MAN', 2007, 'DE', 'cab', 3, 350, 'MAN_TGX', 40000),
 ('tata_prima', 'Tata Prima', 'Tata', 2008, 'IN', 'cab', 3, 230, 'Tata_Prima', 40000),
 ('western_star_4900', 'Western Star 4900', 'Western Star', 2008, 'US', 'conv', 3, 370, 'Western_Star_Trucks', 36000),
 ('kenworth_t680', 'Kenworth T680', 'Kenworth', 2012, 'US', 'conv', 3, 335, 'Kenworth_T680', 36000),
]

TRAILERS = [
 ('dry_van', 'Dry Van Trailer', 1914, 'box', 'crate', 20000, 2, 16.2, 'Semi-trailer'),
 ('flatbed', 'Flatbed Trailer', 1925, 'flat', 'crate', 24000, 2, 14.6, 'Flatbed_trailer'),
 ('container_chassis', 'Container Chassis', 1956, 'chassis', 'crate', 26000, 3, 12.2, 'Intermodal_container'),
 ('tanker', 'Tank Trailer', 1930, 'tank', 'tank', 30000, 3, 13.7, 'Tank_truck'),
 ('reefer', 'Refrigerated Trailer', 1939, 'box', 'crate', 19000, 2, 16.2, 'Refrigerator_truck'),
 ('lowboy', 'Lowboy Trailer', 1940, 'lowboy', 'car', 40000, 3, 15.0, 'Lowboy_(trailer)'),
 ('car_carrier', 'Car Carrier Trailer', 1950, 'carrier', 'car', 17000, 2, 21.0, 'Car_carrier_trailer'),
 ('log_trailer', 'Log Trailer', 1930, 'log', 'crate', 24000, 2, 12.0, 'Log_truck'),
 ('curtainsider', 'Curtainsider Trailer', 1960, 'curtain', 'crate', 24000, 3, 13.6, 'Curtain-sided_trailer'),
 ('dump_trailer', 'Dump Trailer', 1930, 'dump', 'crate', 24000, 3, 9.0, 'Dump_truck'),
 ('livestock', 'Livestock Trailer', 1930, 'livestock', 'crate', 18000, 2, 16.2, 'Livestock_transporter'),
 ('step_deck', 'Step-Deck Trailer', 1940, 'step', 'crate', 22000, 2, 15.2, 'Flatbed_trailer'),
 ('modular_heavy', 'Modular Heavy-Haul Trailer', 1953, 'modular', 'crate', 100000, 6, 18.0, 'Modular_trailer'),
 ('tank_transporter', 'Heavy Equipment Transporter Semitrailer', 1977, 'lowboy', 'car', 55000, 4, 15.5, 'M1000_Heavy_Equipment_Transporter_Semitrailer'),
]

# Construction: (id, name, maker, year, country, kind, kW, mass kg, wiki page, road role)
CONSTRUCTION = [
 ('steam_roller', 'Aveling and Porter Road Roller', 'Aveling and Porter', 1867, 'UK', 'roller', 30, 12000, 'Road_roller', 'compact'),
 ('transit_mixer', 'Concrete Transit Mixer', 'Jaeger', 1916, 'US', 'mixer', 220, 14000, 'Concrete_mixer', 'mix'),
 ('d9_dozer', 'Caterpillar D9 Bulldozer', 'Caterpillar', 1954, 'US', 'dozer', 350, 49000, 'Caterpillar_D9', 'grade'),
 ('jcb_3cx', 'JCB 3CX Backhoe Loader', 'JCB', 1953, 'UK', 'backhoe', 55, 7000, 'Backhoe_loader', 'dig'),
 ('cat_140_grader', 'Caterpillar 140 Motor Grader', 'Caterpillar', 1961, 'US', 'grader', 180, 17000, 'Grader_(road_construction)', 'grade'),
 ('cat_966_loader', 'Caterpillar 966 Wheel Loader', 'Caterpillar', 1963, 'US', 'loader', 150, 17000, 'Loader_(equipment)', 'haul'),
 ('barber_greene_paver', 'Asphalt Paver', 'Barber-Greene', 1931, 'US', 'paver', 110, 15000, 'Paver_(vehicle)', 'pave'),
 ('bomag_roller', 'Vibratory Road Roller', 'Bomag', 1957, 'DE', 'roller', 100, 11000, 'Road_roller', 'compact'),
 ('bobcat_m440', 'Bobcat M-440 Skid-Steer Loader', 'Bobcat', 1960, 'US', 'skid', 14, 1200, 'Skid-steer_loader', 'dig'),
 ('cat_320_excavator', 'Caterpillar 320 Excavator', 'Caterpillar', 1987, 'US', 'excavator', 110, 20000, 'Excavator', 'dig'),
 ('wirtgen_miller', 'Road Milling Machine', 'Wirtgen', 1969, 'DE', 'miller', 400, 28000, 'Road_milling', 'mill'),
 ('putzmeister_pump', 'Concrete Pump Truck', 'Putzmeister', 1958, 'DE', 'pump', 300, 28000, 'Concrete_pump', 'mix'),
 ('cat_797_haul', 'Caterpillar 797 Haul Truck', 'Caterpillar', 1999, 'US', 'haul', 2983, 360000, 'Caterpillar_797', 'haul'),
]
