package world.region.building;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import game.boosting.BOOSTING;
import game.boosting.BValue;
import game.boosting.BValue.BValueSimple;
import game.boosting.BoostSpec;
import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lockable;
import init.paths.PATHS.ResFolder;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.type.CLIMATES;
import prplegoo.regions.api.PrPleGooEfficiencies;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.dic.Dic;
import util.info.GFORMAT;
import util.info.INFO;
import util.keymap.RMAP;
import world.map.regions.Region;
import world.region.RD.RDInit;

public class RDBuildingCat {

    final ArrayListGrower<RDBuilding> all = new ArrayListGrower<>();
    public final COLOR color;
    public final String key;
    public final int order;


    RDBuildingCat(LISTE<RDBuilding> all, RDInit init, String folder, ResFolder p, RDBuildingGeneration gen) throws IOException{
        this.key = folder.toUpperCase(Locale.ENGLISH);
        Json json = new Json(p.init.get("_CAT"));
        this.color = new ColorImp(json);
        order = json.i("ORDER", 0, 10000000, 0);
        addJsons(all, init, p);

        gen.generate(all, init, p, folder, this);

        RDBuilding[] bus = new RDBuilding[this.all.size()];
        for (int i = 0; i <  this.all.size(); i++)
            bus[i] = this.all.get(i);
        this.all.clear();
        Arrays.sort(bus, new Comparator<RDBuilding>() {

            @Override
            public int compare(RDBuilding o1, RDBuilding o2) {
                return o1.order.compareTo(o2.order);
            }

        });
        this.all.add(bus);

    }

//	void generate(RDInit init, Json json, RoomBlueprintImp blue) {
//		addJson(init, json, blue.key, blue.info, null);
//	}

    public LIST<RDBuilding> all(){
        return all;
    }

    private void addJsons(LISTE<RDBuilding> all, RDInit init, ResFolder p) throws IOException{

        for (String f : p.init.getFiles()) {

            Json json = new Json(p.init.get(f));
            Json text = new Json(p.text.get(f));
            INFO info = new INFO(text);
            addJson(all, init, json, f, info, text);
        }

    }

    private void addJson(LISTE<RDBuilding> all, RDInit init, Json json, String key, INFO info, Json text) throws IOException{


        ArrayListGrower<RDBuildingLevel> levels = new ArrayListGrower<>();

        Json[] jsons = json.jsons("LEVELS", 1, 14);

        int li = 0;
        String[] names = new String[0];
        if (text != null && text.has("LEVELS"))
            names = text.texts("LEVELS");
        boolean aibuild = json.bool("AI_BUILDS", true);
        boolean noti = json.bool("NOTIFY_WHEN_UPGRADABLE", false);
        String order = "";
        if (json.has("ORDER"))
            order = json.value("ORDER");
        for (Json j : jsons) {
            CharSequence name = li < names.length ? names[li] : info.name + ": " + GFORMAT.toNumeral(new Str(4), li+1);
            Icon icon = SPRITES.icons().get(j);

            Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + this.key + "_" + key + "_"+(li+1), name, info.desc, icon);
            needs.push(j);
            RDBuildingLevel l = new RDBuildingLevel(name, icon, needs);
            levels.add(l);
            li++;
        }

        RDBuilding b = new RDBuilding(all, init, this, key, info, levels, aibuild, noti, order);


        for (int i = 0; i < jsons.length; i++) {
            RDBuildingLevel l = b.levels.get(i+1);
            Json j = jsons[i];
            l.local.read("BOOST", j, lValue);
            l.global.read("BOOST_GLOBAL", j, lGlobal, Dic.¤¤global, true, "ADMIN");
            l.cost = j.i("CREDITS", 0, 1000000, 0);
        }

        if (json.bool("SLAVERY", false)) {
            BOOSTING.connecter(() -> PrPleGooEfficiencies.SLAVERY(b, 0, 1));
        }

        if (json.bool("FOOD_CONSUMER", false)) {
            BOOSTING.connecter(() -> PrPleGooEfficiencies.FOOD_CONSUMER(b));
        }

        new RMAP.MAPJson<Efficiency>("EFFICIENCY", json, emap, error) {

            @Override
            protected void process(Efficiency s, Json j, String key, boolean isWeak) {
                s.apply(b, j);
            }

        };

    }

    static final BValue lValue = new BValueSimple() {
        @Override
        public double vGet(Region reg) {
            return 1.0;
        }

        @Override
        public double vGet(Faction f) {
            return 0;
        }

        @Override
        public boolean has(Class<?> b) {
            return b == Region.class;
        };
    };

    static final BValue lGlobal = new BValueSimple() {
        @Override
        public double vGet(Region reg) {
            return 1.0;
        }

        @Override
        public double vGet(Faction f) {
            return 1.0;
        }

        @Override
        public boolean has(Class<?> b) {
            return true;
        };

    };



    private static boolean error = false;
    private final KeyMap<Efficiency> emap = new KeyMap<>();
//	private final ResColl<Efficiency> efficiencies = new ResColl<Efficiency>("EFFICIENCY");

    abstract class Efficiency{

        public final String key;

        private Efficiency(String key) {
            this.key = key;
            emap.put(key, this);
        }

        abstract void apply(RDBuilding bu, Json json);

    }

    abstract class EfficiencyValue extends Efficiency {

        public EfficiencyValue(String key) {
            super(key);
        }

        @Override
        void apply(RDBuilding bu, Json json) {
            json = json.json(key);
            double from = json.d("FROM");
            double to = json.d("TO");
            boolean mul = json.bool("IS_MUL", true);
            apply(bu, from, to, mul);
        }

        abstract void apply(RDBuilding bu, double from, double to, boolean isMul);

    }

    abstract class EfficiencyClimate extends Efficiency {

        public EfficiencyClimate(String key) {
            super("CLIMATE");
        }

        @Override
        void apply(RDBuilding bu, Json json) {
            json = json.json(key);
            double from = json.d("FROM");
            double to = json.d("TO");
            boolean mul = json.bool("IS_MUL", true);
            apply(bu, from, to, mul);
        }

        abstract void apply(RDBuilding bu, double from, double to, boolean isMul);

    }


    {
        new EfficiencyValue("RAN_PROSPECT") {
            @Override
            void apply(RDBuilding bu, double from, double to, boolean isMul) {

                BoostSpec bo = Efficiencies.RAN_PROSPECT(bu, from, to, isMul).add(bu.efficiency);
                bu.efficiency.addFactor(bo);
                bu.baseFactors.add(bo);
            }
        };

        new EfficiencyValue("FERTILITY") {

            @Override
            void apply(RDBuilding bu, double from, double to, boolean isMul) {
                BoostSpec bo = Efficiencies.FERTILITY(from, to, isMul).add(bu.efficiency);


                bu.baseFactors.add(bo);

            }
        };

        new EfficiencyValue("WATER") {

            @Override
            void apply(RDBuilding bu, double from, double to, boolean isMul) {

                BoostSpec bo = Efficiencies.WATER(from, to, isMul).add(bu.efficiency);
                bu.baseFactors.add(bo);
            }
        };

        new EfficiencyValue("FOREST") {

            @Override
            void apply(RDBuilding bu, double from, double to, boolean isMul) {

                BoostSpec bo = Efficiencies.FOREST(from, to, isMul).add(bu.efficiency);
                bu.baseFactors.add(bo);

            }
        };

        new Efficiency("CLIMATE") {

            @Override
            void apply(RDBuilding bu, Json json) {
                double[] clim = new double[CLIMATES.ALL().size()];
                CLIMATES.MAP().readFill("CLIMATE", clim, json, 0, 5);
                BoostSpec bo = Efficiencies.CLIMATE(clim, 0, 1, true).add(bu.efficiency);
                bu.baseFactors.add(bo);

            }
        };

        new Efficiency("MINABLES") {

            @Override
            void apply(RDBuilding bu, Json json) {
                Minable m = RESOURCES.minables().read("MINABLE", json);
                BoostSpec bo = 	Efficiencies.MINABLE(m, 0, 1, true).add(bu.efficiency);
                bu.baseFactors.add(bo);

            }
        };

    }



}