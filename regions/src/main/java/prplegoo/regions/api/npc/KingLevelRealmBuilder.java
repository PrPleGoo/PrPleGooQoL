package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.Noble;
import game.faction.npc.FactionNPC;
import init.race.Race;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.RegionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.strategy.*;
import settlement.stats.Induvidual;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDEdicts;
import world.region.pop.RDRace;
import world.region.pop.RDRaces;

import java.util.Arrays;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        build(faction, faction.capitolRegion());
    }

    public void build(FactionNPC faction, Region region) {
        if (region.capitol()) {
            handleGenocide(faction);
            optimizeGrowth(faction);
        }

        FactionGenetic original = new FactionGenetic(faction, region);
        original.calculateFitness();
// TODO: AI leaves conscription in free lands, fucking over their economy into equipment
// TODO: AI expands more than it should, increasing their conscription level and fucking over their economy into equipment
// Conscripts are killed after a fight
        boolean alertMode = original.anyFitnessExceedsDeficit(faction);
        if (alertMode) {
            for (RDBuilding building : RD.BUILDINGS().all) {
                RD.OUTPUT().taxRate.set(region, 0);

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

        KingLevels kingLevels = KingLevels.getInstance();

        int i = 0;
        while (++i < GeneticVariables.mutationAttemptsPerTick) {
            WeightedBag<MutationStrategy> activeStrategies = alertMode && i < 6
                    ? alertStrategies
                    : strategies;

            MutationStrategy strategy = activeStrategies.Pick();

            // The cached values are still valid on the first run, unless we're in alertMode
            if (i > 1 || alertMode) {
                kingLevels.resetDailyProductionRateCache(faction);
            }

            original = new RegionGeneticMutator(faction, region, strategy);
            original.calculateFitness();

            RegionGeneticMutator mutator = new RegionGeneticMutator(faction, region, strategy);

            if (!mutator.tryMutate()) {
                continue;
            }

            // Mutation succeeded, so we changed something that changes the values of boosts, so we need to clear the cache
            kingLevels.resetDailyProductionRateCache(faction);

            mutator.calculateFitness();

            if (!original.shouldAdopt(mutator)) {
                original.commit();

                // If we exit the loop we don't want the cache to contain values from a failed mutation
                if (i == GeneticVariables.mutationAttemptsPerTick) {
                    kingLevels.resetDailyProductionRateCache(faction);
                }
            }
        }
    }

    private void handleGenocide(FactionNPC faction) {
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
                    : ((1 - kingRace.pref().race(race)) * proclivity);
        });

        RDEdicts edicts = races.edicts;
        LIST<Region> regions = faction.realm().all();
        for (Region region : regions) {
            for (RDRace race : racesAll) {
                if (genocide[race.index()] > 2.75) {
                    edicts.massacre.toggled(race).set(region, 1);
                } else {
                    edicts.massacre.toggled(race).set(region, 0);
                }

                edicts.exile.toggled(race).set(region, 0);
                edicts.sanction.toggled(race).set(region, 0);
            }
        }
    }

    private void optimizeGrowth(FactionNPC faction) {
        new FactionGeneticMutator(faction, PopulationGrowthMutationStrategy).tryMutate();
    }

    public KingLevelRealmBuilder() {
        strategies.Add(1, ReduceWorkforceDeficitMutationStrategy);
        strategies.Add(1, HealthMutationStrategy);
        strategies.Add(1, LoyaltyMutationStrategy);
        strategies.Add(2, LoyaltyPruningMutationStrategy);
        strategies.Add(1, ReduceDeficitMutationStrategy);
        strategies.Add(1, GlobalBuildingStrategy);
        strategies.Add(4, PrimarySectorStrategy);
        strategies.Add(5, IndustrializeMutationStrategy);
        strategies.Add(2, MoneyStrategy);
        strategies.Add(1, RemoveBadRecipeMutationStrategy);

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
    private static final MoneyStrategy MoneyStrategy = new MoneyStrategy();
    private static final RemoveBadRecipeMutationStrategy RemoveBadRecipeMutationStrategy = new RemoveBadRecipeMutationStrategy();
    private static final GlobalBuildingStrategy GlobalBuildingStrategy = new GlobalBuildingStrategy();
}