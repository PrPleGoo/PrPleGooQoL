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
import world.region.pop.RDEdicts;
import world.region.pop.RDRace;
import world.region.pop.RDRaces;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        RDRaces races = RD.RACES();
        double[] genocide = new double[races.all.size()];
        Noble noble = BOOSTABLES.NOBLE();
        Induvidual king = faction.king().induvidual;
        double proclivity = noble.AGRESSION.get(king)
                / noble.TOLERANCE.get(king)
                / noble.MERCY.get(king);

        for (RDRace race : races.all) {
            genocide[race.index()] = (race.race.index == king.race().index)
                ? 0 // don't genocide own species, ever
                : ((1 - king.race().pref().race(race.race)) * proclivity); // weight genocide preferences on proclivity
        }

        LIST<Region> regions = faction.realm().all();
        for (Region region : regions)
            for (RDRace race : races.all) {
                RDEdicts edicts = races.edicts;
                edicts.massacre.toggled(race).set(region, genocide[race.index()] > 3.0 ? 1 : 0); //edicts genocide threshold
                edicts.exile.toggled(race).set(region, 0); // no exile edicts
                edicts.sanction.toggled(race).set(region, 0); // no sanction edicts
            }

        // do genocide aggression, tolerance, mercy, rng on king name?
        FactionGenetic original = new FactionGenetic(faction);
        calculateGeneticFitness(original);

        boolean alertMode = original.anyFitnessExceedsDeficit();
        if (alertMode)
            for (Region region : regions)
                for (RDBuilding building : RD.BUILDINGS().all) {
                    int buildingLevel = building.level.get(region);
                    if (buildingLevel > 0) building.level.set(region, buildingLevel - 1);
                }

        KingLevels kingLevelsInstance = KingLevels.getInstance();

        for (int i = 0; i < GeneticVariables.mutationAttemptsPerTick; i++) {
            MutationStrategy strategy = (alertMode
                    ? alertStrategies
                    : strategies).Pick();
            
            FactionGeneticMutator mutator = new FactionGeneticMutator(faction, strategy);
            if (!mutator.tryMutate()) continue;
            calculateGeneticFitness(mutator, kingLevelsInstance);

            original = new FactionGeneticMutator(faction, strategy);
            calculateGeneticFitness(original, kingLevelsInstance);
            if (!original.shouldAdopt(mutator)) original.commit();
        }

        kingLevelsInstance.resetDailyProductionRateCache(faction);
    }

    /**
     * Resets KingLevelsInstance's daily production rate cache then calculates genetic's fitness
     * @param genetic
     * @param faction
     * @param KingLevelsInstance
     */
    private void calculateGeneticFitness(FactionGenetic genetic, KingLevels KingLevelsInstance) {
        KingLevelsInstance.resetDailyProductionRateCache(genetic.faction);
        calculateGeneticFitness(genetic);
    }
    
    /**
     * Loads the genetic for the faction and calculates its fitness for later use
     * @param genetic
     * @param faction
     */
    private void calculateGeneticFitness(FactionGenetic genetic) {
        genetic.loadFitness().calculateFitness();
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

