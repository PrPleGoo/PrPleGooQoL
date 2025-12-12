package init.race;

import java.io.IOException;

import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.tourism.TourismRace;
import init.race.appearence.RAppearence;
import init.race.bio.Bio;
import init.race.home.RaceHome;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.sprite.UI.Icon;
import lombok.Getter;
import settlement.stats.STATS;
import settlement.stats.equip.Equip;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.keymap.MAPPED;

public class Race implements MAPPED {

    public final BoostSpecs boosts;
    private KeyMap<LinkedList<BoostSpec>> bmap = null;
    //	private RaceBoosts bonuses;
    private RacePreferrence pref;
    public final RaceInfo info;
    public Physics physics;
    public final int index;
    public final boolean playable;
    public final String key;


    private KingMessages kmess;
    private RaceStats data;
    private RaceServiceSorter service;
    @Getter
    private RacePopulation population;
    private RaceHome home;
    private Bio bio;
    TourismRace tourism;

    private RAppearence appearance;

    private static final LIST<RES_AMOUNT> rNo = new ArrayList<RES_AMOUNT>(0);

    private LIST<RES_AMOUNT> resources = rNo;
    private LIST<RES_AMOUNT> resourceGroom = rNo;


    public Race(String key, Json data, Json text, ArrayList<Race> list){

        this.key = key;
        index = list.add(this);

        info = new RaceInfo(data, text);
        playable = data.bool("PLAYABLE");
        //population = new RacePopulation(data);
        boosts = new BoostSpecs(info.names, new SPRITE.Imp(Icon.S) {

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                appearance.iconBig.render(r, X1, X2, Y1, Y2);

            }
        }, false);

        boosts.read(data, null);
    }



    void expand(ExpandInit init) throws IOException {

        Json data = new Json(init.p.get(key));
        population = new RacePopulation(data);
        physics = new Physics(data);
        appearance = new RAppearence(this, data, init, physics.hitBoxsize());
        pref = new RacePreferrence(data, this);

        kmess = KingMessages.make(data, init);

        this.data = new RaceStats(this, data);
        service = new RaceServiceSorter(this);

        double[] ds = RESOURCES.map().readFill(data, 100000);
        ArrayList<RES_AMOUNT> resources = new ArrayList<>(ds.length);
        for (RESOURCE r : RESOURCES.ALL()) {
            if (ds[r.index()] > 0) {
                resources.add(new RES_AMOUNT.Imp(r, (int)ds[r.index()]));
            }
        }
        this.resources = resources;

        if (data.has("RESOURCE_GROOMING"))
            resourceGroom = RES_AMOUNT.make(data.json("RESOURCE_GROOMING"));
        this.home = new RaceHome(data.value("HOME"));
        bio = new Bio(data, this);
        tourism = new TourismRace(data, this);

        if (data.has("EQUIPMENT_NOT_ENABLED")) {

            for (Equip b : STATS.EQUIP(). collAll.readMany("EQUIPMENT_NOT_ENABLED", data)) {
                b.setAllowed(this, false);
            }
        }

        if (data.has("EQUIPMENT_ENABLED")) {

            for (Equip b : STATS.EQUIP().collAll.readMany("EQUIPMENT_ENABLED", data)) {
                b.setAllowed(this, true);
            }
        }

    }

    public RAppearence appearance() {
        return appearance;
    }

    public RacePreferrence pref() {
        return pref;
    }

//	public RaceBoosts bonus() {
//		return bonuses;
//	}

    public RaceStats stats() {
        return data;
    }

    public RaceServiceSorter service() {
        return service;
    }

    public RacePopulation population() {
        return population;
    }

    public RaceHome home() {
        return home;
    }

    public Bio bio() {
        return bio;
    }

    @Override
    public String toString() {
        return ""+info.name + "#" + index;
    }

    public LIST<RES_AMOUNT> resources(){
        return resources;
    }

    public LIST<RES_AMOUNT> resourcesGroom(){
        return resourceGroom;
    }

    @Override
    public int index() {
        return index;
    }

    public TourismRace tourism() {
        return tourism;
    }

    public KingMessages kingMessage() {
        return kmess;
    }

    @Override
    public String key() {
        return key;
    }

    private static ArrayList<BoostSpec> dummy = new ArrayList<>(0);

    public LIST<BoostSpec> all(Boostable bo){
        if (bmap == null) {
            bmap = new KeyMap<LinkedList<BoostSpec>>();
            for (BoostSpec boost : boosts.all()) {
                if (!bmap.containsKey(boost.boostable.key))
                    bmap.put(boost.boostable.key, new LinkedList<>());
                bmap.get(boost.boostable.key).add(boost);
            }
        }


        if (bmap.containsKey(bo.key))
            return bmap.get(bo.key);
        return dummy;
    }

    public double bvalue(Boostable bo, double input, double add, double mul){


        double padd = add > 0 ? add : 0;
        double sub = add < 0 ? add : 0;
        for (BoostSpec s : all(bo)) {
            if (s.booster.isMul)
                mul *= s.booster.getValue(input);
            else {
                double a = s.booster.getValue(input);
                if (a < 0)
                    sub += a;
                else
                    padd += a;
            }
        }
        return CLAMP.d(padd*mul + sub, bo.minValue, Double.MAX_VALUE);
    }

    public double bvalue(Boostable bo){
        return bvalue(bo, 1.0, bo.baseValue, 1.0);
    }
}
