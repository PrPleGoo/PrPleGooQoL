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
        double govPointsBefore = RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion());

        for(int i = 0; i < factionGenetic.getRegionGenetics().length; i++) {
            Region region = WORLD.REGIONS().all().get(factionGenetic.getRegionGenetics()[i].regionIndex);
            INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

            tryDestroyBuilding(levelInt, factionGenetic.getRegionGenetics()[i].buildingGenetics[GeneticVariables.growthBuildingIndex], region);
        }

        List<RegionSizeTuple> regionsWithSize = new ArrayList<>();
        for (int i = 0; i < factionGenetic.getRegionGenetics().length; i++){
            Region region = WORLD.REGIONS().all().get(factionGenetic.getRegionGenetics()[i].regionIndex);
            double potentialSize = RD.RACES().popTarget.getValue(region);

            if (region.capitol()) {
                potentialSize = Double.POSITIVE_INFINITY;
            }

            regionsWithSize.add(new RegionSizeTuple(i, potentialSize));
        }

        Collections.sort(regionsWithSize);

        double capForRegion = RD.BUILDINGS().costs.GOV.bo.added(faction.capitolRegion()) / 2;

        for(int i = 0; i < factionGenetic.getRegionGenetics().length; i++) {
            int regionIndex = regionsWithSize.get(factionGenetic.getRegionGenetics().length - i - 1).RegionIndex;
            while (mutateRegion(factionGenetic.getRegionGenetics()[regionIndex], capForRegion)) {
                if (RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion()) < govPointsBefore) {
                    break;
                }
            }

            capForRegion = Math.max(5, capForRegion / 2);
        }

        return true;
    }

    public boolean mutateRegion(RegionGenetic regionGenetic, double capForRegion) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        return tryMutateBuilding(regionGenetic.buildingGenetics[GeneticVariables.growthBuildingIndex], region, capForRegion);
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region, double capForRegion) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

        if (region.faction().realm().regions() > 2
                && capForRegion > RD.BUILDINGS().costs.GOV.bo.get(region.faction().capitolRegion())) {
            return false;
        }

        double currentPop = RD.RACES().population.get(region);
        double regionCapacity = RD.RACES().popTarget.getValue(region);
        return !(regionCapacity * 0.75 > currentPop) && tryLevelUpgrade(levelInt, buildingGenetic, region);

    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
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