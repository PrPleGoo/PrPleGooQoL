package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
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

        Faction faction = WORLD.REGIONS().all().get(factionGenetic.regionGenetics[0].regionIndex).faction();
        double govPointsBefore = RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion());
        double govPointsTotal = RD.BUILDINGS().costs.GOV.bo.added(faction.capitolRegion());

        for(int i = 0; i < factionGenetic.regionGenetics.length; i++) {
            Region region = WORLD.REGIONS().all().get(factionGenetic.regionGenetics[i].regionIndex);
            INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

            tryDestroyBuilding(levelInt, factionGenetic.regionGenetics[i].buildingGenetics[GeneticVariables.growthBuildingIndex], region);
        }

        List<RegionSizeTuple> regionsWithSize = new ArrayList<>();
        for (int i = 0; i < factionGenetic.regionGenetics.length; i++){
            Region region = WORLD.REGIONS().all().get(factionGenetic.regionGenetics[i].regionIndex);
            double potentialSize = RD.RACES().popTarget.getValue(region);

            if (region.capitol()) {
                potentialSize = Double.POSITIVE_INFINITY;
            }

            regionsWithSize.add(new RegionSizeTuple(i, potentialSize));
        }

        Collections.sort(regionsWithSize);

        for(int i = 0; i < factionGenetic.regionGenetics.length; i++) {
            int regionIndex = regionsWithSize.get(factionGenetic.regionGenetics.length - i - 1).RegionIndex;
            while (mutateRegion(factionGenetic.regionGenetics[regionIndex], govPointsTotal)) {
                if (RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion()) < govPointsBefore) {
                    break;
                }
            }
        }

        return true;
    }

    public boolean mutateRegion(RegionGenetic regionGenetic, double govPointsTotal) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        return tryMutateBuilding(regionGenetic.buildingGenetics[GeneticVariables.growthBuildingIndex], region, govPointsTotal);
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region, double govPointsTotal) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex).level;

        double currentPop = RD.RACES().population.get(region);
        double regionCapacity = RD.RACES().popTarget.getValue(region);
        if (regionCapacity * 0.75 > currentPop) {
            return false;
        }

        if (region.capitol()
                && region.faction().realm().regions() > 2
                && govPointsTotal / 2 > RD.BUILDINGS().costs.GOV.bo.get(region.faction().capitolRegion())){
            return false;
        }

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
                return factionValue > mutant.fitnessRecords[index].factionValue;
            }

            @Override
            public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
                return factionValue < mutant.fitnessRecords[index].factionValue;
            }

            @Override
            public double determineValue(FactionNPC faction) {
                double popTarget = 0;

                for (Region region : faction.realm().all()) {
                    popTarget += RD.RACES().popTarget.getValue(region);
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