package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.strategy.*;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDEdicts;
import world.region.pop.RDRace;
import world.region.pop.RDRaces;
import game.boosting.BOOSTABLES.Noble;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        RDRaces races = RD.RACES();

        Noble noble = BOOSTABLES.NOBLE();
        Royalty king = faction.king();
        double proclivity = noble.AGRESSION.get(king.induvidual)
                / noble.TOLERANCE.get(king.induvidual)
                / noble.MERCY.get(king.induvidual);

        double[] genocide = new double[races.all.size()];

        for (RDRace race : races.all) {
            genocide[race.index()] = (race.race.index == king.induvidual.race().index)
                    ? 0
                    : (1 - king.induvidual.race().pref().race(race.race)) * proclivity;
        }

        RDEdicts edicts = races.edicts;
        for (Region region : faction.realm().all())
            for (RDRace race : races.all) {
                if (genocide[race.index()] > 3.0) {
                    edicts.massacre.toggled(race).set(region, 1);
                } else {
                    edicts.massacre.toggled(race).set(region, 0);
                }

                edicts.exile.toggled(race).set(region, 0);
                edicts.sanction.toggled(race).set(region, 0);
            }

        // When we come into this function any values in the boosts are active so the cache is up-to-date
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

        KingLevels kingLevels = KingLevels.getInstance();
        WeightedBag<MutationStrategy> activeStrategies = alertMode
                ? alertStrategies
                : strategies;

        for (int i = 1; i <= GeneticVariables.mutationAttemptsPerTick; i++) {
            MutationStrategy strategy = activeStrategies.Pick();

            // The cached values are still valid on the first run, unless we're in alertMode
            if (i > 1 || alertMode) {
                kingLevels.resetDailyProductionRateCache(faction);
            }

            original = new FactionGeneticMutator(faction, strategy);
            original.loadFitness(faction).calculateFitness(faction);

            FactionGeneticMutator mutator = new FactionGeneticMutator(faction, strategy);

            if (!mutator.tryMutate()) {
                continue;
            }

            // Mutation succeeded, so we changed something that changes the values of boosts, so we need to clear the cache
            kingLevels.resetDailyProductionRateCache(faction);

            mutator.loadFitness(faction).calculateFitness(faction);

            if (!original.shouldAdopt(faction, mutator)) {
                original.commit();

                // If we exit the loop we don't want the cache to contain values from a failed mutation
                if (i == GeneticVariables.mutationAttemptsPerTick) {
                    kingLevels.resetDailyProductionRateCache(faction);
                }
            }
        }
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

