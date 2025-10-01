package prplegoo.regions.api;

import game.boosting.*;
import game.faction.npc.FactionNPC;
import lombok.Getter;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.RDSlaveryData;
import snake2d.LOG;
import snake2d.util.sets.ArrayList;
import util.dic.Dic;
import world.WORLD;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuildPoints;
import world.region.pop.RDRace;

import java.util.HashMap;

public class RDSlavery implements IDataPersistence<RDSlaveryData> {
    @Getter
    private RDBuildPoints.RDBuildPoint workforce = null;
    private final ArrayList<RDSlave> all;
    private HashMap<Integer, HashMap<Integer, Boolean>> selectedSlaves;
    private double[][] slaveDelivery;

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
            new RBooster(new BSourceInfo(Dic.¤¤Population, slave.rdRace.race.appearance().icon), 0, 40000, true) {
                @Override
                public double get(Region t) {
                    return slave.rdRace.pop.get(t) / 40000.0 / 100.0;
                }

            }.add(slave.boost);
        }

        // Workforce is a type of slavery...
        for (RDBuildPoints.RDBuildPoint cost : RD.BUILDINGS().costs.ALL){
            if(MagicStringChecker.isWorkforceBoostableKey(cost.bo.key)){
                workforce = cost;
            }
        }

        if(workforce == null){
            throw new RuntimeException("Workforce boostable not found");
        }

        new RBooster(new BSourceInfo(Dic.¤¤Population, workforce.icon), 0, 40000, false) {
            @Override
            public double get(Region t) {
                return RD.RACES().population.get(t) / 15.0 / max();
            }

        }.add(workforce.bo);


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
    public String getKey() {
        return RDSlaveryData.class.toString();
    }

    @Override
    public RDSlaveryData getData() {
        return new RDSlaveryData(selectedSlaves, slaveDelivery);
    }

    @Override
    public void putData(RDSlaveryData data) {
        if (data == null) {
            LOG.ln("RDSlavery.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDSlavery.onGameSaveLoaded: data found");
        if (selectedSlaves.size() != data.data.size()
                || selectedSlaves.get(0).size() != data.data.get(0).size()
                || slaveDelivery.length != data.slaveDelivery.length
                || slaveDelivery[0].length != data.slaveDelivery[0].length)
        {
            LOG.ln("RDSlavery.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDSlavery.onGameSaveLoaded: data found, writing");
        selectedSlaves = data.data;
        slaveDelivery = data.slaveDelivery;
    }

    @Override
    public Class<RDSlaveryData> getDataClass() {
        return RDSlaveryData.class;
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

        slaveDelivery = new double[WORLD.REGIONS().all().size()][RD.RACES().all.size()];
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

    public int pushDelivery(Region reg, RDRace rdRace, double value){
        slaveDelivery[reg.index()][rdRace.index()] += value;
        int result = (int) Math.floor(slaveDelivery[reg.index()][rdRace.index()]);
        slaveDelivery[reg.index()][rdRace.index()] -= result;
        return result;
    }

    private double readDelivery(Region reg, RDRace rdRace) {
        return slaveDelivery[reg.index()][rdRace.index()];
    }

    public static class RDSlave {

        public final Boostable boost;
        public final RDRace rdRace;

        RDSlave(RDRace race) {
            boost = BOOSTING.push("SLAVE_PRODUCTION_" + race.race.key, 0, Dic.¤¤Law + ": " + race.race.info.name, race.race.info.desc, race.race.appearance().icon, BoostableCat.ALL().WORLD);
            this.rdRace = race;
        }

        public int getDelivery(Region reg, double days) {
            return RD.SLAVERY().pushDelivery(reg, this.rdRace, boost.get(reg) * days);
        }

        public int getDelivery(FactionNPC faction, double days) {
            int count = 0;
            for (int i = 0; i < faction.realm().regions(); i++) {
                Region region = faction.realm().region(i);
                count += RD.SLAVERY().pushDelivery(region, this.rdRace, boost.get(region) * days);
            }

            return count;
        }

        public int hasDelivery(Region reg, double days) {
            return (int) Math.floor(RD.SLAVERY().readDelivery(reg, this.rdRace) + boost.get(reg) * days);
        }
    }
}
