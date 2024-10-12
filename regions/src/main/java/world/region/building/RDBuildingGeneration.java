package world.region.building;

import game.boosting.*;
import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lockable;
import init.paths.PATHS.ResFolder;
import init.race.RACES;
import init.type.CLIMATE;
import init.type.CLIMATES;
import prplegoo.regions.api.PrPleGooEfficiencies;
import settlement.main.SETT;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.room.food.orchard.ROOM_ORCHARD;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.woodcutter.ROOM_WOODCUTTER;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tuple;
import snake2d.util.sprite.text.Str;
import util.dic.Dic;
import util.info.GFORMAT;
import world.map.regions.Region;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.pop.RDRace;

class RDBuildingGeneration {

    private final KeyMap<LISTE<Gen>> gens = new KeyMap<>();

    public void generate(LISTE<RDBuilding> all, RDInit init, ResFolder p, String folder, RDBuildingCat cat) {
        if (gens.get(folder) != null)
            for (Gen g : gens.get(folder))
                g.generate(all, init, p, cat);

    }


    public RDBuildingGeneration() {

        {
            ArrayListGrower<RoomBlueprintImp> all = new ArrayListGrower<>();
            for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().FARMS).join(SETT.ROOMS().ORCHARDS)) {
                if (b.constructor().mustBeIndoors())
                    all.add(b);
            }

            new GenIndustry("agriculture", "_GENERATE_INDOORS", all) {
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());

                    PrPleGooEfficiencies.POP_SCALING(bu);

                    super.connect(bu, blue, local, global);
                }
            };
            all.clear();
            for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().FARMS).join(SETT.ROOMS().ORCHARDS)) {
                if (!b.constructor().mustBeIndoors())
                    all.add(b);
            }
            new GenIndustry("agriculture", "_GENERATE_OUTDOORS", all){
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
                    double mi = 0.1;
                    double ma = 1.0;
                    if (blue instanceof ROOM_ORCHARD) {
                        mi = 0.5;
                    }

                    BoostSpec bo = Efficiencies.FERTILITY(mi, ma, true).add(bu.efficiency);

                    bu.baseFactors.add(bo);

                    PrPleGooEfficiencies.POP_SCALING(bu);

                    super.connect(bu, blue, local, global);
                }
            };
        }
        {
            ArrayListGrower<RoomBlueprintImp> all = new ArrayListGrower<>();
            for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().PASTURES).join(SETT.ROOMS().FISHERIES)) {
                if (b.constructor().mustBeIndoors())
                    all.add(b);
            }

            new GenIndustry("pasture", "_GENERATE_INDOORS", all) {
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
                    if (blue instanceof ROOM_FISHERY) {
                        BoostSpec bo = Efficiencies.WATER(0.1, 2.0, true).add(bu.efficiency);
                        bu.baseFactors.add(bo);
                    }

                    PrPleGooEfficiencies.POP_SCALING(bu);

                    super.connect(bu, blue, local, global);
                }
            };
            all.clear();
            for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().PASTURES).join(SETT.ROOMS().FISHERIES)) {
                if (!b.constructor().mustBeIndoors())
                    all.add(b);
            }
            new GenIndustry("pasture", "_GENERATE_OUTDOORS", all){
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
                    if (blue instanceof ROOM_FISHERY) {
                        BoostSpec bo = Efficiencies.WATER(0.1, 2.0, true).add(bu.efficiency);
                        bu.baseFactors.add(bo);
                    }
                    else {
                        BoostSpec bo = Efficiencies.FERTILITY(0.1, 1.0, true).add(bu.efficiency);
                        bu.baseFactors.add(bo);

                        bo = Efficiencies.WATER(1.00, 1.25, true).add(bu.efficiency);
                        bu.baseFactors.add(bo);
                    }

                    PrPleGooEfficiencies.POP_SCALING(bu);

                    super.connect(bu, blue, local, global);
                }
            };



        }

        {
            new GenIndustry("mine", "_GENERATE", new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().WOOD_CUTTER).join(SETT.ROOMS().MINES)) {
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
                    if (blue instanceof ROOM_WOODCUTTER) {
                        PrPleGooEfficiencies.POP_SCALING_WOOD(bu);
                    }
                    else {
                        ROOM_MINE f = (ROOM_MINE) blue;

                        PrPleGooEfficiencies.MINABLE(bu, f.minable, 0.3, 1.5);
                        PrPleGooEfficiencies.POP_SCALING_MINABLE(bu, f.minable);
                    }


                    super.connect(bu, blue, local, global);
                }
            };
        }

        {
            new GenIndustryWithRecipes("refinery", "_GENERATE", new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().REFINERS)) {
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());

                    PrPleGooEfficiencies.POP_SCALING(bu);

                    connect(bu, blue);
                }
            };
        }

        {
            new GenIndustryWithRecipes("workshop", "_GENERATE", new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().WORKSHOPS)) {
                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());

                    PrPleGooEfficiencies.POP_SCALING(bu);

                    connect(bu, blue);
                }
            };
        }

        {
            new Gen("religion", "_GENERATE", new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().TEMPLES.ALL)) {

                @Override
                void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
                    ROOM_TEMPLE t = (ROOM_TEMPLE) blue;
                    consume(bu, local, t.religion.conversionRealm, false, false);
                    consume(bu, global, t.religion.conversionRealm, false, true);
                }
            };
        }


    }

    private abstract class Gen {

        public final String file;
        private final LIST<RoomBlueprintImp> rooms;
        private final ArrayListGrower<Tuple<RDBuilding, RoomBlueprintImp>> tmps = new ArrayListGrower<>();

        protected boolean isPopScaler(){
            return false;
        }

        Gen(String folder, String file, LIST<RoomBlueprintImp> rooms){
            this.file = file;
            this.rooms = new ArrayList<>(rooms);
            if (!gens.containsKey(folder))
                gens.put(folder, new ArrayListGrower<>());
            gens.get(folder).add(this);

        }

        void generate(LISTE<RDBuilding> all, RDInit init, ResFolder p, RDBuildingCat cat) {

            if (!p.init.exists(file))
                return;

            Json json = new Json(p.init.get(file));

            boolean aibuild = json.bool("AI_BUILDS", true);
            double[] local = json.ds("LOCAL_LEVELS");
            double[] global = json.ds("GLOBAL_LEVELS");
            Json[] levels = json.jsons("LEVELS");

            LIST<RoomBlueprint> omitt = SETT.ROOMS().collection.readMany("OMITT_ROOMS", json);
            String order = "";
            if (json.has("ORDER"))
                order = json.value("ORDER");
            for (RoomBlueprintImp room : rooms) {
                if (omitt.contains(room))
                    continue;
                RDBuilding bu = generate(all, init, levels, cat, room, aibuild, order);
                tmps.add(new Tuple.TupleImp<>(bu, room));
            }

            BOOSTING.connecter(new ACTION() {

                @Override
                public void exe() {
                    for (Tuple<RDBuilding, RoomBlueprintImp> t : tmps) {
                        Gen.this.connect(t.a(), t.b(), local, global);
                    }
                }
            });

        }

        RDBuilding generate(LISTE<RDBuilding> all, RDInit init, Json[] jlevels, RDBuildingCat cat, RoomBlueprintImp blue, boolean aiBuild, String order) {


            ArrayListGrower<RDBuildingLevel> levels = new ArrayListGrower<>();

            for (int i = 0; i < jlevels.length; i++) {
                String n = blue.info.name + ": " + GFORMAT.toNumeral(new Str(4), i+1);
                Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + blue.key + "_"+(i+1), n, blue.info.desc, blue.iconBig());

                RDBuildingLevel b = new RDBuildingLevel(n, blue.iconBig(), needs);
                levels.add(b);

            }

            RDBuilding bu = new RDBuilding(all, init, cat, blue.key, blue.info, levels, aiBuild, false, order, isPopScaler());

            for (int i = 0; i < jlevels.length; i++) {
                RDBuildingLevel l = bu.levels.get(i+1);
                Json j = jlevels[i];
                l.local.read("BOOST", j, RDBuildingCat.lValue);
                l.global.read("BOOST_GLOBAL", j, RDBuildingCat.lGlobal, Dic.¤¤global, false);

                l.cost = j.i("CREDITS", 0, 1000000, 0);
            }

            return bu;
        }


        abstract void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global);

        void consume(RDBuilding bu, double[] values, Boostable bo, boolean mul, boolean global) {
            consume(bu, values, 1.0, bo, mul, global);
        }

        protected void consume(RDBuilding bu, double[] values, double dv, Boostable bo, boolean mul, boolean global) {
            for (int i = 0; i < values.length; i++) {
                RDBuildingLevel b = bu.levels.get(i+1);
                double v = values[i]*dv;
                if (v == 0 && !mul)
                    continue;
                if (v == 1 && mul)
                    continue;
                BoostSpecs ss = global ? b.global : b.local;
                ss.push(new LBoost(bu, v, mul), bo);
            }
        }
    }

    private class GenIndustry extends Gen {
        @Override
        protected boolean isPopScaler(){
            return true;
        }

        GenIndustry(String folder, String file, LIST<RoomBlueprintImp> rooms) {
            super(folder, file, rooms);
        }

        @Override
        void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
            INDUSTRY_HASER h = (INDUSTRY_HASER) blue;

            for (IndustryResource r : h.industries().get(0).outs()) {
                consume(bu, local, r.rate, RD.OUTPUT().get(r.resource).boost, false, false);
                consume(bu, global, r.rate, RD.OUTPUT().get(r.resource).boost, false, true);
            }
        }
    }

    private class GenIndustryWithRecipes extends Gen {

        GenIndustryWithRecipes(String folder, String file, LIST<RoomBlueprintImp> rooms) {
            super(folder, file, rooms);
        }

        @Override
        RDBuilding generate(LISTE<RDBuilding> all, RDInit init, Json[] jlevels, RDBuildingCat cat, RoomBlueprintImp blue, boolean aiBuild, String order)
        {
            INDUSTRY_HASER h = (INDUSTRY_HASER) blue;
            ArrayListGrower<RDBuildingLevel> levels = new ArrayListGrower<>();

            for (int i = 0; i < h.industries().size(); i++) {
                String n = blue.info.name + ": " + GFORMAT.toNumeral(new Str(4), i+1);
                Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + blue.key + "_"+(i+1), n, blue.info.desc, blue.iconBig());

                RDBuildingLevel b = new RDBuildingLevel(n, blue.iconBig(), needs);
                levels.add(b);
            }

            RDBuilding bu = new RDBuilding(all, init, cat, blue.key, blue.info, levels, aiBuild, false, order, true);

            for (int i = 0; i < jlevels.length; i++) {
                RDBuildingLevel l = bu.levels.get(i+1);
                Json j = jlevels[0];
                l.local.read("BOOST", j, RDBuildingCat.lValue);
                l.cost = j.i("CREDITS", 0, 1000000, 0);
            }

            return bu;
        }

        @Override
        void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) { }

        void connect(RDBuilding bu, RoomBlueprintImp blue) {
            INDUSTRY_HASER h = (INDUSTRY_HASER) blue;

            for(int i = 0; i < h.industries().size(); i++)
            {
                Industry recipe = h.industries().get(i);

                for (IndustryResource r : recipe.outs()) {
                    consume(bu, i, r.rate, RD.OUTPUT().get(r.resource).boost);
                }

                for (IndustryResource r : recipe.ins()) {
                    consume(bu, i, -r.rate, RD.OUTPUT().get(r.resource).boost);
                }
            }
        }

        private void consume(RDBuilding bu, int i, double dv, Boostable bo) {
            RDBuildingLevel b = bu.levels.get(i + 1);

            b.local.push(new LBoost(bu, dv, false), bo);
        }
    }

    protected static void mimic(RDBuilding bu, Boostable bo) {

        for (CLIMATE c : CLIMATES.ALL()) {
            for (int si = 0; si < c.boosters.all().size(); si++) {
                BoostSpec s = c.boosters.all().get(si);
                if (s.boostable == bo) {
                    BoostSpec sp = CLIMATES.pushIfDoesntExist(c, s.booster.to(), bu.efficiency, s.booster.isMul);
                    if (sp != null && !bu.baseFactors.contains(sp))
                        bu.baseFactors.add(sp);
                }
            }
        }

        for (RDRace c : RD.RACES().all) {
            for (int si = 0; si < c.race.boosts.all().size(); si++) {
                BoostSpec s = c.race.boosts.all().get(si);
                if (s.boostable == bo) {
                    BoostSpec sp = RACES.boosts().pushIfDoesntExist(c.race, s.booster.to(), bu.efficiency, s.booster.isMul);
                    if (sp != null && !bu.baseFactors.contains(sp))
                        bu.baseFactors.add(sp);
                }
            }
        }

    }
    
    private static class LBoost extends BoosterImp {

        private final RDBuilding bu;

        public LBoost(RDBuilding bu, double value, boolean isMul) {
            super(new BSourceInfo(bu.info.name, bu.levels.get(1).icon), value, isMul);
            this.bu = bu;
        }

        @Override
        public double vGet(Region reg) {
            if (isPositive(1.0)) {
                return getValue(bu.efficiency.get(reg));
            }
            else
            {
                return getValue(1.0);
            }
        }

        @Override
        public boolean has(Class<?> b) {
            return b == Region.class;
        };

        @Override
        public double vGet(Faction f) {
            return 0;
        }
    }
}
