package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.phase.Phases;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.resources.RESOURCES;
import snake2d.LOG;
import snake2d.util.sets.ArrayList;
import util.dic.Dic;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

import java.nio.file.Path;

public class RDSlavery implements Phases {

    private ArrayList<RDSlave> all;

    public double boost(RDRace race, Region region){
        return all.get(race.index()).boost.get(region);
    }

    public RDSlavery(){
        ArrayList<RDSlavery.RDSlave> all = new ArrayList<>(RESOURCES.ALL().size());

        for (RDRace race : RD.RACES().all) {
            all.add(new RDSlavery.RDSlave(race));
        }
        this.all = all;
    }

    @Override
    public void onGameLoaded(Path saveFilePath) {
        LOG.ln("RDSlavery.onGameSaveLoaded " + saveFilePath);
    }

    @Override
    public void onGameSaved(Path saveFilePath) {
        LOG.ln("RDSlavery.onGameSaved " + saveFilePath);
    }

    private void initialize() {
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
