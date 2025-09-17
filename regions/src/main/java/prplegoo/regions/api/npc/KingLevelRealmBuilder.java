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
        var races = RD.RACES();
        double[] genocide = new double[races.all.size()];
        var noble = BOOSTABLES.NOBLE();
        var king = faction.king();
        double proclivity = noble.AGRESSION.get(king.induvidual)
                / noble.TOLERANCE.get(king.induvidual)
                / noble.MERCY.get(king.induvidual);
        
        for (RDRace race : races.all) {
            genocide[race.index()] = (race.race.index == king.induvidual.race().index)
                ? 0
                : (1 - king.induvidual.race().pref().race(race.race)) * proclivity;
        }

        for (Region region : faction.realm().all())
            for (RDRace race : races.all) {
                var edicts = races.edicts;
                edicts.massacre.toggled(race).set(region, genocide[race.index()] > 3.0 ? 1 : 0);
                edicts.exile.toggled(race).set(region, 0);
                edicts.sanction.toggled(race).set(region, 0);
            }

        // do genocide aggression, tolerance, mercy, rng on king name?
        // don't genocide own species, ever
        FactionGenetic original = new FactionGenetic(faction);
        calculateMutatorFitness(original, faction);

        boolean alertMode = original.anyFitnessExceedsDeficit(faction);
        if (alertMode)
            for (Region region : faction.realm().all())
                for (RDBuilding building : RD.BUILDINGS().all) {
                    int buildingLevel = building.level.get(region);
                    if (buildingLevel > 0) building.level.set(region, buildingLevel - 1);
                }

        KingLevel kingLevelsInstance = KingLevels.getInstance();

        for (int i = 0; i < GeneticVariables.mutationAttemptsPerTick; i++) {
            MutationStrategy strategy = (alertMode
                    ? alertStrategies
                    : strategies).Pick();
            
            FactionGeneticMutator mutator = new FactionGeneticMutator(faction, strategy);
            if (!mutator.tryMutate()) continue;
            calculateMutatorFitness(mutator, faction, kingLevelsInstance);

            original = new FactionGeneticMutator(faction, strategy);
            calculateMutatorFitness(original, faction, kingLevelsInstance);
            if (!original.shouldAdopt(faction, mutator)) original.commit();
        }

        kingLevelsInstance.resetDailyProductionRateCache(faction);
    }

    private void calculateMutatorFitness(FactionGeneticMutator mutant, FactionNPC faction, KingLevel KingLevelsInstance) {
        if (KingLevelsInstance != null) KingLevelsInstance.resetDailyProductionRateCache(faction);
        mutant.loadFitness(faction).calculateFitness(faction);
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

