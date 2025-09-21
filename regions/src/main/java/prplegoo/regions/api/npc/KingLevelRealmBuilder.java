package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.Noble;
import game.faction.npc.FactionNPC;
import init.race.Race;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.strategy.*;
import settlement.stats.Induvidual;
import snake2d.util.sets.LIST;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDEdicts;
import world.region.pop.RDRace;
import world.region.pop.RDRaces;

import java.util.Arrays;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        RDRaces races = RD.RACES();
        LIST<RDRace> racesAll = races.all;
        Noble noble = BOOSTABLES.NOBLE();
        Induvidual king = faction.king().induvidual;
        // do genocide aggression, tolerance, mercy, rng on king name?
        double proclivity = noble.AGRESSION.get(king)
                / noble.TOLERANCE.get(king)
                / noble.MERCY.get(king);

        double[] genocide = new double[racesAll.size()];

        Race kingRace = king.race();
        Arrays.setAll(genocide, i -> {
            Race race = racesAll.get(i).race;
            return (race.index == kingRace.index)
                    ? 0 // don't genocide own species, ever
                    : ((1 - kingRace.pref().race(race) * proclivity));
        });

        RDEdicts edicts = races.edicts;
        LIST<Region> regions = faction.realm().all();
        for (Region region : regions) {
            for (RDRace race : racesAll) {
                edicts.massacre.toggled(race).set(region, genocide[race.index()] > 3.0 ? 1 : 0);
                edicts.exile.toggled(race).set(region, 0);
                edicts.sanction.toggled(race).set(region, 0);
            }
        }

        // When we come into this function any values in the boosts are active so the cache is up-to-date
        FactionGenetic original = new FactionGenetic(faction);
        original.calculateFitness(true);

        boolean alertMode = original.anyFitnessExceedsDeficit(faction);
        if (alertMode) {
            for (Region region : regions) {
                for (RDBuilding building : RD.BUILDINGS().all) {
                    if (GeneticVariables.isGrowthBuilding(building.index())) {
                        continue;
                    }

                    if (GeneticVariables.isHealthBuilding(building.index())
                            && RD.SLAVERY().getWorkforce().bo.get(region) >= 0) {
                        continue;
                    }

                    int buildingLevel = building.level.get(region);
                    if (buildingLevel > 0) {
                        building.level.set(region, buildingLevel - 1);
                    }
                }
            }
        }

        KingLevels kingLevelsInstance = KingLevels.getInstance();
        WeightedBag<MutationStrategy> activeStrategies = alertMode
                ? alertStrategies
                : strategies;
        int i = 0;
        while (i++ < GeneticVariables.mutationAttemptsPerTick) {
            MutationStrategy strategy = activeStrategies.Pick();

            // The cached values are still valid on the first run, unless we're in alertMode
            if (i > 1 || alertMode) {
                kingLevelsInstance.resetDailyProductionRateCache(faction);
            }

            original = new FactionGeneticMutator(faction, strategy);
            original.calculateFitness(true);

            FactionGeneticMutator mutator = new FactionGeneticMutator(faction, strategy);

            if (!mutator.tryMutate()) {
                continue;
            }

            // Mutation succeeded, so we changed something that changes the values of boosts, so we need to clear the cache
            kingLevelsInstance.resetDailyProductionRateCache(faction);

            mutator.calculateFitness(true);

            if (!original.shouldAdopt(mutator)) {
                original.commit();

                // If we exit the loop we don't want the cache to contain values from a failed mutation
                if (i == GeneticVariables.mutationAttemptsPerTick) {
                    kingLevelsInstance.resetDailyProductionRateCache(faction);
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
        strategies.Add(1, GlobalBuildingStrategy);
        strategies.Add(4, PrimarySectorStrategy);
        strategies.Add(5, IndustrializeMutationStrategy);
        strategies.Add(1, RemoveBadRecipeMutationStrategy);

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
    private static final IndustrializeMutationStrategy IndustrializeMutationStrategy = new IndustrializeMutationStrategy();
    private static final RemoveBadRecipeMutationStrategy RemoveBadRecipeMutationStrategy = new RemoveBadRecipeMutationStrategy();
    private static final GlobalBuildingStrategy GlobalBuildingStrategy = new GlobalBuildingStrategy();
}