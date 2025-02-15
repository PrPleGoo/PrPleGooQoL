package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import lombok.Getter;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class RegionGenetic {
    private final int regionIndex;
    private final BuildingGenetic[] buildingGenetics;
    private final RegionDeficits deficits;

    public RegionGenetic(int regionIndex) {
        this.regionIndex = regionIndex;
        buildingGenetics = new BuildingGenetic[RD.BUILDINGS().all.size()];
        for (int i = 0; i < buildingGenetics.length; i++) {
            buildingGenetics[i] = new BuildingGenetic(regionIndex, i);
        }

        Region region = WORLD.REGIONS().all().get(regionIndex);
        double govpointDeficit = GeneticVariables.clamp(RD.BUILDINGS().costs.GOV.bo.get(region) / GeneticVariables.govpointValue);
        double workforceDeficit = GeneticVariables.clamp(RD.SLAVERY().getWorkforce().bo.get(region) / GeneticVariables.workForceValue);
        double healthDeficit = GeneticVariables.clamp((RD.HEALTH().get(region) - 0.50) / GeneticVariables.healthValue);
        double raiderDeficit = GeneticVariables.clamp(GAME.raiders().entry.get(region).probabilityRaw());
        deficits = new RegionDeficits(govpointDeficit, workforceDeficit, healthDeficit, raiderDeficit);
    }

    public void mutate() {
        Region region = WORLD.REGIONS().all().get(regionIndex);


        for(BuildingGenetic buildingGenetic : buildingGenetics) {
            buildingGenetic.mutate(region, deficits);
        }
    }

    public void commit() {
        for (BuildingGenetic buildingGenetic : buildingGenetics) {
            buildingGenetic.commit(WORLD.REGIONS().all().get(regionIndex));
        }
    }

    public static class RegionDeficits {
        @Getter
        private final double govpointDeficit;
        private final double workforceDeficit;
        private final double healthDeficit;
        private final double raiderDeficit;

        public RegionDeficits(double govpointDeficit, double workforceDeficit, double healthDeficit, double raiderDeficit) {
            this.govpointDeficit = govpointDeficit;
            this.workforceDeficit = workforceDeficit;
            this.healthDeficit = healthDeficit;
            this.raiderDeficit = raiderDeficit;
        }

        public boolean govpointDeficit(double random) {
            return govpointDeficit < 0 && govpointDeficit > random;
        }
        public boolean workforceDeficit(double random) {
            return workforceDeficit < 0 && workforceDeficit > random;
        }
        public boolean healthDeficit(double random) {
            return healthDeficit < 0 && healthDeficit > random;
        }
        public boolean raiderDeficit(double random) {
            return raiderDeficit < 0 && raiderDeficit > random;
        }

        public boolean workforceAbundance(double random) {
            return workforceDeficit > 0 && workforceDeficit > random;
        }
    }
}
