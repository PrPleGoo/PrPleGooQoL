package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.Noble;
import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGeneticMutator;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.strategy.*;
import settlement.stats.Induvidual;
import snake2d.util.sets.LIST;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        LIST<RDRace> races = RD.RACES().all;
        double[] genocide = new double[races.size()];
        Noble noble = BOOSTABLES.NOBLE();
        Induvidual king = faction.king().induvidual;
        double proclivity = noble.AGRESSION.get(king)
                / noble.TOLERANCE.get(king)
                / noble.MERCY.get(king);

        for (RDRace race : races) {
            if (race.race.index == king.race().index) {
                genocide[race.index()] = 0;
                continue;
            }

            genocide[race.index()] = (1 - king.race().pref().race(race.race)) * proclivity;
        }

        for (Region region : faction.realm().all()) {
            for (RDRace race : races) {
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
        strategies.Add(1, ReduceDeficitMutationStrategy);
        strategies.Add(3, PrimarySectorStrategy);
        strategies.Add(4, ReduceStorageMutationStrategy);

        alertStrategies.Add(1, PopulationGrowthMutationStrategy);
        alertStrategies.Add(4, HealthMutationStrategy);
        alertStrategies.Add(4, LoyaltyMutationStrategy);
        alertStrategies.Add(2, ReduceWorkforceDeficitMutationStrategy);
    }

    private static final WeightedBag<MutationStrategy> strategies = new WeightedBag<>();
    private static final WeightedBag<MutationStrategy> alertStrategies = new WeightedBag<>();
    private static final ReduceWorkforceDeficitMutationStrategy ReduceWorkforceDeficitMutationStrategy = new ReduceWorkforceDeficitMutationStrategy();
    private static final PopulationGrowthMutationStrategy PopulationGrowthMutationStrategy = new PopulationGrowthMutationStrategy();
    private static final HealthMutationStrategy HealthMutationStrategy = new HealthMutationStrategy();
    private static final LoyaltyMutationStrategy LoyaltyMutationStrategy = new LoyaltyMutationStrategy();
    private static final ReduceDeficitMutationStrategy ReduceDeficitMutationStrategy = new ReduceDeficitMutationStrategy();
    private static final PrimarySectorStrategy PrimarySectorStrategy = new PrimarySectorStrategy();
    private static final ReduceStorageMutationStrategy ReduceStorageMutationStrategy = new ReduceStorageMutationStrategy();
}

