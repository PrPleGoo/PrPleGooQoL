package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.Faction;
import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.*;

public class PopulationGrowthMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutate(FactionGenetic factionGenetic) {
        if (GeneticVariables.growthBuildingIndex == -1) {
            for (RDBuilding building : RD.BUILDINGS().all) {
                if (GeneticVariables.isGrowthBuilding(building.index())) {
                    break;
                }
            }
        }

        Faction faction = WORLD.REGIONS().all().get(factionGenetic.getRegionGenetics()[0].regionIndex).faction();
        double govPointsBefore = RD.BUILDINGS().costs.GOV.bo.get(faction);

        for(int i = 0; i < factionGenetic.getRegionGenetics().length; i++) {
            Region region = WORLD.REGIONS().all().get(factionGenetic.getRegionGenetics()[i].regionIndex);
            INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

            tryDestroyBuilding(levelInt, factionGenetic.getRegionGenetics()[i].buildingGenetics[GeneticVariables.growthBuildingIndex], region);
        }

        List<RegionSizeTuple> regionsWithSize = new ArrayList<>();
        for (int i = 0; i < factionGenetic.getRegionGenetics().length; i++){
            Region region = WORLD.REGIONS().all().get(factionGenetic.getRegionGenetics()[i].regionIndex);
            double size = RD.RACES().capacity.get(region);

            if (region.capitol()) {
                size = Double.POSITIVE_INFINITY;
            }

            regionsWithSize.add(new RegionSizeTuple(i, size));
        }

        Collections.sort(regionsWithSize);
        for(int i = 0; i < factionGenetic.getRegionGenetics().length; i++) {
            int regionIndex = regionsWithSize.get(factionGenetic.getRegionGenetics().length - i - 1).RegionIndex;
            while (mutateRegion(factionGenetic.getRegionGenetics()[regionIndex])) {
                if (RD.BUILDINGS().costs.GOV.bo.get(faction) < govPointsBefore) {
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        return tryMutateBuilding(regionGenetic.buildingGenetics[GeneticVariables.growthBuildingIndex], region);
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

        return tryLevelUpgrade(levelInt, buildingGenetic, region);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[2];
        fitnessRecords[0] = new GovPoints(faction, 0);
        // PopTarget;
        fitnessRecords[1] = new FitnessRecord(faction, 1) {
            @Override
            public boolean willIncreaseDeficit(FactionNPC faction, FactionGenetic mutant) {
                return factionValue > mutant.getFitnessRecords()[index].factionValue;
            }

            @Override
            public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
                return factionValue < mutant.getFitnessRecords()[index].factionValue;
            }

            @Override
            public double determineValue(FactionNPC faction) {
                double popTarget = 0;

                for (Region region : faction.realm().all()) {
                    popTarget += RD.RACES().capacity.get(region);
                }

                return popTarget;
            }
        };

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