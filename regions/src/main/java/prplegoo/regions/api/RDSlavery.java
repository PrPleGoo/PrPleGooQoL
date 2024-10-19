package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.ModSdkModule;
import com.github.argon.sos.mod.sdk.config.json.JsonConfigStore;
import com.github.argon.sos.mod.sdk.phase.Phases;
import game.boosting.*;
import game.faction.FACTIONS;
import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.dic.Dic;
import world.WORLD;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RDOutput;
import world.region.pop.RDRace;

import java.nio.file.Path;
import java.util.HashMap;

public class RDSlavery implements Phases {
    private final JsonConfigStore jsonConfigStore = ModSdkModule.jsonConfigStore();

    private ArrayList<RDSlave> all;
    private HashMap<Integer, HashMap<Integer, Boolean>> selectedSlaves;

    public double boost(RDRace race, Region region){
        return all.get(race.index()).boost.get(region);
    }

    public RDSlavery(){
        ArrayList<RDSlavery.RDSlave> all = new ArrayList<>(RD.RACES().all.size());

        for (RDRace race : RD.RACES().all) {
            all.add(new RDSlavery.RDSlave(race));
        }
        this.all = all;

        for(RDSlave slave : all){
            new RBooster(new BSourceInfo(Dic.¤¤Filter, slave.rdRace.race.appearance().icon), 0, 1, true) {
                @Override
                public double get(Region t) {
                    return RD.SLAVERY().has(t, slave.rdRace) ? 1 : 0;
                }

            }.add(slave.boost);
        }

        initialize();
    }

    public Boostable boostable(RDRace rdRace) {
        return all.get(rdRace.index()).boost;
    }

    public int size() {
        return all.size();
    }

    public RDSlave get(int i) {
        return all.get(i);
    }

    public ArrayList<RDSlave> all() {
        return all;
    }

    @Override
    public void onGameLoaded(Path saveFilePath) {
        LOG.ln("RDSlavery.onGameSaveLoaded " + saveFilePath);
        jsonConfigStore.bindToSave(RDSlaveryData.class, "RDSlavery", PATHS.local().SAVE.get().resolve("PrPleGoo"), false);

        RDSlaveryData data = jsonConfigStore.get(RDSlaveryData.class).orElse(null);
        if (data == null) {
            LOG.ln("RDSlavery.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDSlavery.onGameSaveLoaded: data found, writing");
        selectedSlaves = data.data;
    }

    @Override
    public void onGameSaved(Path saveFilePath) {
        LOG.ln("RDSlavery.onGameSaved " + saveFilePath);
        jsonConfigStore.save(new RDSlaveryData(selectedSlaves));
    }

    private void initialize() {
        selectedSlaves = new HashMap<>();
        for (Region region : WORLD.REGIONS().all()) {
            HashMap<Integer, Boolean> selectedSlave = new HashMap<>();
            for (RDRace race : RD.RACES().all) {
                selectedSlave.put(race.index(), true);
            }

            selectedSlaves.put(region.index(), selectedSlave);
        }
    }

    public boolean has(Region t, RDRace e) {
        if (!selectedSlaves.get(t.index()).containsKey(e.index())) {
            return false;
        }

        return selectedSlaves.get(t.index()).get(e.index());
    }

    public void toggleSlave(Region region, RDRace race) {
        if (!selectedSlaves.get(region.index()).containsKey(race.index())) {
            return;
        }

        HashMap<Integer, Boolean> selectedSlave = selectedSlaves.get(region.index());
        selectedSlave.replace(race.index(), !selectedSlave.get(race.index()));
    }

    public static class RDSlave {

        public final Boostable boost;
        public final RDRace rdRace;

        RDSlave(RDRace race) {
            boost = BOOSTING.push("SLAVE_PRODUCTION_" + race.race.key, 0, Dic.¤¤Law + ": " + race.race.info.name, race.race.info.desc, race.race.appearance().icon, BoostableCat.WORLD_PRODUCTION);
            this.rdRace = race;
        }

        public int getDelivery(Region reg, double days) {
            return (int) Math.ceil(boost.get(reg) * days);
        }
    }
}
