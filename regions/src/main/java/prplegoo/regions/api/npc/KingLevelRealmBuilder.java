package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.strategy.*;
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
                if (genocide[race.index()] > 3.0) {
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

        boolean alertMode = original.anyFitnessExceedsDeficit(faction);
        if (alertMode) {
            for (Region region : faction.realm().all()) {
                for (RDBuilding building : RD.BUILDINGS().all) {
                    if (GeneticVariables.isGrowthBuilding(building.index())) {
                        continue;
                    }

                    if (GeneticVariables.isHealthBuilding(building.index())
                            && RD.SLAVERY().getWorkforce().bo.get(region) >= 0) {
                        continue;
                    }

                    if (building.level.get(region) > 0) {
                        building.level.set(region, building.level.get(region) - 1);
                    }
                }
            }
        }

        for (int i = 0; i < GeneticVariables.mutationAttemptsPerTick; i++) {
            MutationStrategy strategy = alertMode
                    ? PickAlertStrategy()
                    : PickStrategy();
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

    private MutationStrategy PickStrategy() {
        return strategies.Pick();
    }

    private MutationStrategy PickAlertStrategy() {
        return alertStrategies.Pick();
    }

    public KingLevelRealmBuilder() {
        strategies.Add(1, ReduceWorkforceDeficitMutationStrategy);
        strategies.Add(1, PopulationGrowthMutationStrategy);
        strategies.Add(1, HealthMutationStrategy);
        strategies.Add(1, LoyaltyMutationStrategy);
        strategies.Add(1, LoyaltyPruningMutationStrategy);
        strategies.Add(1, ReduceDeficitMutationStrategy);
        strategies.Add(3, PrimarySectorStrategy);
        strategies.Add(4, ReduceStorageMutationStrategy);

        alertStrategies.Add(1, PopulationGrowthMutationStrategy);
        alertStrategies.Add(1, HealthImprovementStrategy);
        alertStrategies.Add(1, LoyaltyMutationStrategy);
        alertStrategies.Add(1, ReduceWorkforceDeficitMutationStrategy);
    }

    private static final WeightedBag<MutationStrategy> strategies = new WeightedBag<>();
    private static final WeightedBag<MutationStrategy> alertStrategies = new WeightedBag<>();
    private static final ReduceWorkforceDeficitMutationStrategy ReduceWorkforceDeficitMutationStrategy = new ReduceWorkforceDeficitMutationStrategy();
    private static final PopulationGrowthMutationStrategy PopulationGrowthMutationStrategy = new PopulationGrowthMutationStrategy();
    private static final HealthMutationStrategy HealthMutationStrategy = new HealthMutationStrategy();
    private static final HealthImprovementStrategy HealthImprovementStrategy = new HealthImprovementStrategy();
    private static final LoyaltyMutationStrategy LoyaltyMutationStrategy = new LoyaltyMutationStrategy();
    private static final LoyaltyPruningMutationStrategy LoyaltyPruningMutationStrategy = new LoyaltyPruningMutationStrategy();
    private static final ReduceDeficitMutationStrategy ReduceDeficitMutationStrategy = new ReduceDeficitMutationStrategy();
    private static final PrimarySectorStrategy PrimarySectorStrategy = new PrimarySectorStrategy();
    private static final ReduceStorageMutationStrategy ReduceStorageMutationStrategy = new ReduceStorageMutationStrategy();
}

