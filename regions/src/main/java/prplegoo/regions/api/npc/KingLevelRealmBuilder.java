package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.strategy.*;
import snake2d.util.rnd.RND;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        double[] genocide = new double[RD.RACES().all.size()];
        double proclivity = BOOSTABLES.NOBLE().AGRESSION.get(faction.king().induvidual)
                / BOOSTABLES.NOBLE().TOLERANCE.get(faction.king().induvidual)
                / BOOSTABLES.NOBLE().MERCY.get(faction.king().induvidual);

        for (RDRace race : RD.RACES().all) {
            if (race.race.index == faction.king().induvidual.race().index) {
                genocide[race.index()] = 0;
                continue;
            }

            genocide[race.index()] = (1 - faction.king().induvidual.race().pref().race(race.race)) * proclivity;
        }

        for (Region region : faction.realm().all()) {
            for (RDRace race : RD.RACES().all) {
                if (genocide[race.index()] > 3.5) {
                    RD.RACES().edicts.massacre.toggled(race).set(region, 1);
                    RD.RACES().edicts.exile.toggled(race).set(region, 0);
                    RD.RACES().edicts.sanction.toggled(race).set(region, 0);
                } else {
                    RD.RACES().edicts.massacre.toggled(race).set(region, 0);
                    RD.RACES().edicts.exile.toggled(race).set(region, 0);
                    RD.RACES().edicts.sanction.toggled(race).set(region, 0);
                }
            }
        }

        // do genocide aggression, tolerance, mercy, rng on king name?
        // don't genocide own species, ever

        FactionGenetic original = new FactionGenetic(faction);
        original.loadFitness(faction).calculateFitness(faction);
        int totalMutations = GeneticVariables.mutationAttemptsPerTick;

//        LOG.ln("CHECKING original.anyFitnessExceedsDeficit IN KingLevelRealmBuilder;");
        if (original.anyFitnessExceedsDeficit(faction)) {
//            LOG.ln("DONE original.anyFitnessExceedsDeficit IN KingLevelRealmBuilder;");
            double govPoints = RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion());
            for (Region region : faction.realm().all()) {
                for (RDBuilding building : RD.BUILDINGS().all) {
                    double health = RD.HEALTH().boostablee.get(region);
                    if (GeneticVariables.isGrowthBuilding(building.index())
                            && govPoints >= 0
                            && health > 0.5) {
                        continue;
                    }
                    if (GeneticVariables.isHealthBuilding(building.index())
                            && health < 1) {
                        continue;
                    }

                    if (building.level.get(region) > 0) {
                        building.level.set(region, building.level.get(region) - 1);
                    }
                }
            }

            totalMutations += GeneticVariables.extraMutationsAfterReset;
        }

        for (int i = 0; i < totalMutations; i++) {
            MutationStrategy strategy = PickStrategy(faction);
            original = new FactionGeneticMutator(faction, strategy);

            KingLevels.getInstance().resetDailyProductionRateCache(faction);
            original.loadFitness(faction).calculateFitness(faction);

            FactionGeneticMutator mutator = new FactionGeneticMutator(faction, strategy);

            if (!mutator.tryMutate()) {
                continue;
            }

            KingLevels.getInstance().resetDailyProductionRateCache(faction);
            mutator.loadFitness(faction).calculateFitness(faction);

            if (!original.shouldAdopt(faction, mutator)) {
                original.commit();
            }
        }

        KingLevels.getInstance().resetDailyProductionRateCache(faction);
    }

    private MutationStrategy PickStrategy(FactionNPC faction) {
        if (RND.oneIn(4)) {
            return new RandomMutationStrategy();
        }

        if (GeneticVariables.growthBuildingIndex != -1) {
            RDBuilding building = RD.BUILDINGS().all.get(GeneticVariables.growthBuildingIndex);

            boolean canTryPopulationGrowthMutationStrategy = false;
            boolean canTryHealthMutationStrategy = false;
            for (Region region : faction.realm().all()) {
                int levelCurrent = building.level.get(region);
                if (building.canAfford(region, levelCurrent, levelCurrent + 1) == null && RND.oneIn(2)) {
                    if (RD.HEALTH().boostablee.get(region) < 5) {
                        canTryHealthMutationStrategy = true;
                    }

                    canTryPopulationGrowthMutationStrategy = true;
                }
            }

            if (canTryPopulationGrowthMutationStrategy && RND.oneIn(3)) {
                return new PopulationGrowthMutationStrategy();
            }
            if(canTryHealthMutationStrategy && RND.oneIn(3)) {
                return new HealthMutationStrategy();
            }
        }

        boolean canTryLoyaltyMutationStrategy = false;
        for (Region region : faction.realm().all()) {
            for (int i = 0; i < RD.RACES().all.size(); i++) {
                RDRace race = RD.RACES().all.get(i);

                if (RD.RACES().edicts.massacre.toggled(race).get(region) == 1){
                    continue;
                }

                if (race.loyalty.target.get(region) < 0) {
                    canTryLoyaltyMutationStrategy = true;
                    break;
                }
            }
        }
        if(canTryLoyaltyMutationStrategy && RND.oneIn(3)) {
            return new LoyaltyMutationStrategy();
        }

        boolean hasDeficits = false;
        for (RESOURCE resource : RESOURCES.ALL()) {
            if (faction.stockpile.amount(resource) < 0) {
                hasDeficits = true;
                break;
            }
        }

        if (hasDeficits && RND.oneIn(2)) {
            return new ReduceDeficitMutationStrategy();
        }

        if (RND.oneIn(3)) {
            return new PrimarySectorStrategy();
        } else if (RND.oneIn(2)){
            return new ReduceStorageMutationStrategy();
        }

        return new RandomMutationStrategy();
    }
}
