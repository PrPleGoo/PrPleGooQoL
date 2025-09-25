package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.Faction;
import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.*;

public class PopulationGrowthMutationStrategy extends MutationStrategy {
    private static final double regionCapDivider = 2.0;
    @Override
    public boolean tryMutate(FactionGenetic factionGenetic) {
        if (GeneticVariables.growthBuildingIndex == -1) {
            for (RDBuilding building : RD.BUILDINGS().all) {
                if (GeneticVariables.isGrowthBuilding(building.index())) {
                    break;
                }
            }
        }

        RegionGenetic[] regionGenetics = factionGenetic.getRegionGenetics();
        FactionNPC faction = (FactionNPC) WORLD.REGIONS().all().get(regionGenetics[0].regionIndex).faction();
        double govPointsBefore = RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion());

        for (RegionGenetic regionGenetic : regionGenetics) {
            Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
            INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

            tryDestroyBuilding(levelInt, regionGenetic.buildingGenetics[GeneticVariables.growthBuildingIndex], region);
        }

        List<RegionSizeTuple> regionsWithSize = new ArrayList<>();
        for (int i = 0; i < regionGenetics.length; i++){
            Region region = WORLD.REGIONS().all().get(regionGenetics[i].regionIndex);
            double potentialSize = RD.RACES().popTarget.getValue(region);

            if (region.capitol()) {
                potentialSize = Double.POSITIVE_INFINITY;
            }

            regionsWithSize.add(new RegionSizeTuple(i, potentialSize));
        }

        Collections.sort(regionsWithSize);

        double capForRegion = RD.BUILDINGS().costs.GOV.bo.added(faction.capitolRegion()) / regionCapDivider;

        for(int i = 0; i < regionGenetics.length; i++) {
            int regionIndex = regionsWithSize.get(regionGenetics.length - i - 1).RegionIndex;
            while (mutateRegion(regionGenetics[regionIndex], capForRegion)) {
                if (RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion()) < govPointsBefore) {
                    break;
                }
            }

            capForRegion = capForRegion / regionCapDivider;
        }

        for(int i = 0; i < regionGenetics.length; i++) {
            int regionIndex = regionsWithSize.get(regionGenetics.length - i - 1).RegionIndex;
            mutateRegion(regionGenetics[regionIndex], Double.NEGATIVE_INFINITY);
        }

        return true;
    }

    public boolean mutateRegion(RegionGenetic regionGenetic, double capForRegion) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        return tryMutateBuilding(regionGenetic.buildingGenetics[GeneticVariables.growthBuildingIndex], region, capForRegion);
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region, double capForRegion) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

        if (region.faction().realm().regions() > 1
                && capForRegion > RD.BUILDINGS().costs.GOV.bo.get(region.faction().capitolRegion())) {
            return false;
        }

        double currentPop = RD.RACES().population.get(region);
        double regionCapacity = RD.RACES().popTarget.getValue(region);
        if (regionCapacity * 0.75 > currentPop) {
            return false;
        }

        return tryLevelUpgrade(levelInt, buildingGenetic, region);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new GovPoints(faction, 0);

        return fitnessRecords;
    }

    private static class RegionSizeTuple implements Comparable<RegionSizeTuple> {
        public int RegionIndex;

        public double Size;

        public RegionSizeTuple(int regionIndex, double size) {
            this.RegionIndex = regionIndex;
            this.Size = size;
        }

        @Override
        public int compareTo(RegionSizeTuple o) {
            return Double.compare(this.Size, o.Size);
        }
    }
}