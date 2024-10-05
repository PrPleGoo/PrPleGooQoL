package world.region;

import static world.WORLD.REGIONS;

import java.io.IOException;
import java.util.Arrays;

import game.GameDisposable;
import game.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.race.Race;
import init.sprite.UI.UI;
import init.text.D;
import prplegoo.regions.api.RDWorkers;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.data.DataO;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.building.RDBuilding;
import world.region.building.RDBuildings;
import world.region.pop.RDRace;
import world.region.pop.RDRaces;
import world.region.updating.RDUpdater;

public class RD extends WorldResource {

    private static RD self;


    private final RDBuildings buildings;
    private final RDOutput resources;
    private final RDRandom random;
    private final RDRaces races;
    private final RDAdmin admin;
    private final RDMilitary military;
    private final RDTax tax;
    private final RDHealth health;
    private final RDDistance distance;
    private final RDReligions religion;
    private final RDOwner owner;
    private final RDDevastation deva;
    private final RDOrg org;
    private final RDWorkers workers;

    private RDUpdater updater;
    private boolean distDirty = true;

    private final long[][] regionData;
    private final long[][] factionData;
    private final int[] factionI = new int[WREGIONS.MAX];

    //	final RegData[] dreg = new RegData[WREGIONS.MAX];
    final Realm[] drea = new Realm[FACTIONS.MAX];
    private final RDInit init = new RDInit();

    private static CharSequence ¤¤regChange = "{0} changes master from {1} to {2}.";

    static {
        D.ts(RD.class);
    }

    public RD(WREGIONS regions) throws IOException {
        super("region Data", "RD");
        self = this;

        admin = new RDAdmin(init);
        distance = new RDDistance(init);
        random = new RDRandom(init);
        health = new RDHealth(init);
        resources = new RDOutput(init);
        military = new RDMilitary(init);
        races = new RDRaces(init);
        tax = new RDTax(init);
        religion = new RDReligions(init);
        buildings = new RDBuildings(init);
        owner = new RDOwner(init);
        deva = new RDDevastation(init);
        org = new RDOrg(init);
        workers = new RDWorkers();

        Arrays.fill(factionI, -1);

        regionData = new long[WREGIONS.MAX][init.count.longCount()];
        factionData = new long[FACTIONS.MAX][init.rCount.longCount()];
        for (int i = 0; i < drea.length; i++)
            drea[i] = new Realm(i);

    }

    private final WorldResourceManager saver = new WorldResourceManager() {

        @Override
        public void save(FilePutter file) {

            init.count.saver().save(WORLD.REGIONS().all(), file);
            init.rCount.saver().save(FACTIONS.all(), file);

            file.isE(factionI);

            for (Realm r : drea) {
                r.saver.save(file);
            }
            for (SAVABLE s : init.savable)
                s.save(file);

            updater.saver.save(file);
        }

        @Override
        public void load(FileGetter file) throws IOException {

            init.count.loader().load(WORLD.REGIONS().all(), file);
            init.rCount.loader().load(FACTIONS.all(), file);

            file.isE(factionI);

            for (Realm r : drea) {
                r.saver.load(file);
            }

            for (SAVABLE s : init.savable)
                s.load(file);

            updater.saver.load(file);
            distDirty = true;
            RD.ADMIN().change(FACTIONS.player().capitolRegion());
        }

        @Override
        public void clear() {
            for (long[] r : regionData) {
                Arrays.fill(r, 0);
            }
            for (long[] r : factionData) {
                Arrays.fill(r, 0);
            }

            for (Realm r : drea) {
                r.saver.clear();
            }

            Arrays.fill(factionI, -1);

            for (SAVABLE s : init.savable)
                s.clear();
            ;
            updater.saver.clear();
            distDirty = false;
            WORLD.MINIMAP().repaint();
        }

        @Override
        public LIST<PLACABLE> makePlacers(ToolManager tm) {
            return new Placers();
        }

        ;

        @Override
        public void generate(ACTION loadPrint) {
            clear();
            new Gen(init, loadPrint);
            loadPrint.exe();
            prime();
            loadPrint.exe();
        }

        ;

        @Override
        public void validateInit(WorldError error) {
            if (!WORLD.REGIONS().player.active()) {
                error.problem = "Player region is missing";
                error.coo.set(-1, -1);
                return;
            }

            WORLD.REGIONS().player.fationSet(FACTIONS.player(), false);
            WORLD.REGIONS().player.setCapitol();
            WORLD.REGIONS().player.info.name().clear().add(FACTIONS.player().name);

            if (FACTIONS.NPCs().size() == 0)
                error.warning = "No factions have been set";


        }

        ;
    };

    public void prime() {

        for (Region r : REGIONS().active()) {
            random.randomize(r);
            for (int i = 0; i < 3; i++) {
                RD.UPDATER().BUILD(r);
                for (RDUpdatable up : init.upers) {
                    up.init(r);
                }
            }

        }

        for (FactionNPC ff : FACTIONS.NPCs()) {
            if (ff.realm().capitol() == null)
                continue;
            RDRace race = null;
            double br = 0;

            Region r = ff.capitolRegion();

            for (RDRace rrr : RD.RACES().all) {
                if (rrr.pop.get(r) >= br) {
                    br = rrr.pop.get(r);
                    race = rrr;
                }
            }

            boolean sa = ff.sanctified;
            ff.generate(race, true);
            ff.sanctified = sa;
        }

        for (Region r : REGIONS().active()) {
            random.randomize(r);
            for (int i = 0; i < 2; i++) {
                RD.UPDATER().BUILD(r);
                for (RDUpdatable up : init.upers) {
                    up.init(r);
                }
            }

        }
    }

    @Override
    public WorldResourceManager saver() {
        return saver;
    }

    @Override
    public void update(float ds, Profiler prof) {
        prof.logStart(this);
        if (distDirty) {
            distDirty = false;
            distance.init();
        }

        updater.update(ds);
        prof.logEnd(this);
    }

    @Override
    protected void afterTick() {
        buildings.update();
    }

    @Override
    public void initAfterGameSetup() {
        buildings.init(init);
        races.init();

        updater = new RDUpdater(init);
        distDirty = true;
    }

    public final class RDInit {

        public final DataO<Region> count = new DataO<Region>("RDR") {

            @Override
            protected long[] data(Region t) {
                return regionData[t.index()];
            }

        };

        public final DataO<Faction> rCount = new DataO<Faction>("RDF") {

            @Override
            protected long[] data(Faction t) {
                return factionData[t.index()];
            }

        };

        public final LinkedList<RDUpdatable> upers = new LinkedList<>();

        public final LinkedList<SAVABLE> savable = new LinkedList<>();
        public final RDDefis deficiencies = new RDDefis();

    }

    public interface RDUpdatable {
        void update(Region reg, double time);

        void init(Region reg);
    }

    public interface RDGeneratable {

        void generate(Region r);

    }


    public static RDBuildings BUILDINGS() {
        return self.buildings;
    }

    public static RDOutput OUTPUT() {
        return self.resources;
    }

    public static RDRandom RAN() {
        return self.random;
    }

    public static RDRaces RACES() {
        return self.races;
    }

    public static RDAdmin ADMIN() {
        return self.admin;
    }

    public static RDMilitary MILITARY() {
        return self.military;
    }

    public static RDTax TAX() {
        return self.tax;
    }

    public static RDHealth HEALTH() {
        return self.health;
    }

    public static RDDistance DIST() {
        return self.distance;
    }

    public static RDReligions RELIGION() {
        return self.religion;
    }

    public static RDOwner OWNER() {
        return self.owner;
    }

    public static RDOrg ORG() {
        return self.org;
    }

    public static RDWorkers WORKERS() {
        return self.workers;
    }

    public static RDUpdater UPDATER() {
        return self.updater;
    }

    public static RDDevastation DEVASTATION() {
        return self.deva;
    }

    public static RDDefis DEFS() {
        return self.init.deficiencies;
    }

    public static Realm REALM(Region reg) {
        if (self.factionI[reg.index()] != -1)
            return self.drea[self.factionI[reg.index()]];
        return null;
    }

    public static Realm REALM(Faction f) {
        return self.drea[f.index()];
    }

    private static void removeFaction(Region region) {

        Realm rr = REALM(region);

        if (rr == null)
            return;

        self.factionI[region.index()] = -1;

        rr.regions.removeShort((short) region.index());
        if (rr.capitolI == region.index()) {
            if (rr.regions.size() > 0)
                rr.capitolI = (short) rr.regions.get(rr.regions.size() - 1);
            else
                rr.capitolI = -1;
        }

    }

    public static void setFaction(final Region region, final Faction f, boolean log) {

        Realm oldRealm = REALM(region);

        if (f != null && REALM(f) == oldRealm)
            return;

        RD.OWNER().ownerI.set(region, (RD.OWNER().ownerI.get(region) + 1) % RD.OWNER().ownerI.max(region));

        final Faction fold = region.faction();

        removeFaction(region);

        if (f != null) {
            Realm rr = f.realm();
            if (rr.regions.hasRoom()) {
                self.factionI[region.index()] = f.index();

                rr.regions.add((short) region.index());

                if (rr.capitolI == -1)
                    rr.capitolI = (short) region.index();

            }
            f.realm().ferArea = 0;
            for (int ri = 0; ri < f.realm().regions(); ri++) {
                Region r = WORLD.REGIONS().all().get(ri);
                f.realm().ferArea += r.info.area() * r.info.fertility();
            }
        }

        if (fold != null) {
            fold.realm().ferArea = 0;
            for (int ri = 0; ri < fold.realm().regions(); ri++) {
                Region r = WORLD.REGIONS().all().get(ri);
                fold.realm().ferArea += r.info.area() * r.info.fertility();
            }
        }

        self.distDirty = true;

        WORLD.MINIMAP().updateRegion(region);


        RDOwnerChanger.changeI++;
        for (RDOwnerChanger ch : RDOwnerChanger.ownerChanges) {
            ch.change(region, fold, f);
        }

        Str.TMP.clear().add(¤¤regChange);
        Str.TMP.insert(0, region.info.name());
        Str.TMP.insert(1, FACTIONS.name(fold));
        Str.TMP.insert(2, FACTIONS.name(f));
        WORLD.LOG().log(fold, f, UI.icons().s.crown, Str.TMP, region.cx(), region.cy());

        if (f == FACTIONS.player()) {
            for (RDBuilding bu : RD.BUILDINGS().all) {
                if (bu.level.get(region) > 0 && !bu.levels.get(bu.level.get(region)).reqs.passes(region)) {
                    bu.level.set(region, 0);
                }
            }
        }

    }

    public static void clearFaction(FactionNPC faction) {
        while (faction.realm().regions() > 0)
            setFaction(faction.realm().region(0), null, false);
    }

    public static void setCapitol(Region region) {
        Realm rr = REALM(region);
        if (rr == null)
            throw new RuntimeException("Can't set a rebel region as a capitol");

        Region old = region.faction().capitolRegion();

        rr.capitolI = (short) region.index();
        self.distDirty = true;

        for (RDOwnerChanger ch : RDOwnerChanger.ownerChanges) {
            ch.change(region, region.faction(), region.faction());
        }

        rr.regions.swap(0, rr.regions.indexOf((short) region.index()));
        WORLD.MINIMAP().updateRegion(region);
        if (old != null)
            WORLD.MINIMAP().updateRegion(old);
    }

    public static RDRace RACE(Race r) {
        return RACES().get(r);
    }

    public static abstract class RDOwnerChanger {

        public static int changeI;
        static final ArrayListGrower<RDOwnerChanger> ownerChanges = new ArrayListGrower<>();

        static {
            new GameDisposable() {

                @Override
                protected void dispose() {
                    ownerChanges.clear();
                }
            };
        }

        public RDOwnerChanger() {
            ownerChanges.add(this);
        }

        public abstract void change(Region reg, Faction oldOwner, Faction newOwner);
    }


}
