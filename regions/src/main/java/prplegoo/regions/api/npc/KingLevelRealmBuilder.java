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

import java.util.Arrays;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        LIST<RDRace> races = RD.RACES().all;
        double[] genocide = new double[races.size()];
        Noble noble = BOOSTABLES.NOBLE();
        Induvidual king = faction.king().induvidual;

        // do genocide aggression, tolerance, mercy, rng on king name?
        double proclivity = noble.AGRESSION.get(king)
                / noble.TOLERANCE.get(king)
                / noble.MERCY.get(king);

        Race kingRace = king.race();
        Arrays.setAll(genocide, i -> (races.get(i).race.index == kingRace.index)
                ? 0 // don't genocide own species, ever
                : ((1 - kingRace.pref().race(races.get(i).race) * proclivity)));

        RDEdicts edicts = RD.RACES().edicts;
        LIST<Region> regions = faction.realm().all();
        for (Region region : regions)
            for (RDRace race : races) {
                edicts.massacre.toggled(race).set(region, genocide[race.index()] > 3.0 ? 1 : 0);
                edicts.exile.toggled(race).set(region, 0);
                edicts.sanction.toggled(race).set(region, 0);
            }

        FactionGenetic original = new FactionGenetic(faction);
        original.calculateFitness(true);

        boolean alertMode = original.anyFitnessExceedsDeficit(faction);
        if (alertMode) for (Region region : regions)
            for (RDBuilding building : RD.BUILDINGS().all) {
                int buildingLevel = building.level.get(region);
                if (buildingLevel > 0) building.level.set(region, buildingLevel - 1);
            }

        int i = 0;
        KingLevels kingLevelsInstance = KingLevels.getInstance();
        while (i++ < GeneticVariables.mutationAttemptsPerTick) {
            MutationStrategy strategy = (alertMode ? alertStrategies : strategies).Pick();
            FactionGenetic originalWithStrategy = new FactionGeneticMutator(faction, strategy);

            kingLevelsInstance.resetDailyProductionRateCache(faction);
            originalWithStrategy.calculateFitness(true);

            FactionGeneticMutator mutator = new FactionGeneticMutator(faction, strategy);

            kingLevelsInstance.resetDailyProductionRateCache(faction);
            mutator.calculateFitness(true);

            if (mutator.tryMutate() && originalWithStrategy.shouldAdopt(mutator)) original.adopt(mutator).commit();
        }

        kingLevelsInstance.resetDailyProductionRateCache(faction);
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

