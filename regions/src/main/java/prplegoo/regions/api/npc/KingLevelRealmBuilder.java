package prplegoo.regions.api.npc;

import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class KingLevelRealmBuilder {
    public void build(FactionNPC faction) {
        // do genocide aggression, tolerance, mercy, rng on king name?
            // don't genocide own species, ever

        FactionGenetic original = new FactionGenetic(faction);
        original.calculateFitness(faction, buyPrices(faction), sellPrices(faction));
        int totalMutations = GeneticVariables.mutationAttemptsPerTick;

//        LOG.ln("CHECKING original.anyFitnessExceedsDeficit IN KingLevelRealmBuilder;");
        if (original.anyFitnessExceedsDeficit(faction)) {
//            LOG.ln("DONE original.anyFitnessExceedsDeficit IN KingLevelRealmBuilder;");
            for (Region region : faction.realm().all()) {
                for (RDBuilding building : RD.BUILDINGS().all) {
                    if (building.level.get(region) > 0) {
                        building.level.set(region, building.level.get(region) - 1);
                    }
                }
            }

            original = new FactionGenetic(faction);
            KingLevels.getInstance().resetDailyProductionRateCache(faction);
            original.calculateFitness(faction, buyPrices(faction), sellPrices(faction));

            totalMutations += GeneticVariables.extraMutationsAfterReset;
        }

        totalMutations = Math.max(1, totalMutations * GeneticVariables.regionMutationsPerMutation / faction.realm().regions());
        totalMutations = Math.min(GeneticVariables.maxMutations, totalMutations);

        for(int i = 0; i < totalMutations; i ++) {
            FactionGenetic mutant = new FactionGenetic(faction);

            if (!mutant.mutate()) {
                continue;
            }

            KingLevels.getInstance().resetDailyProductionRateCache(faction);

            mutant.calculateFitness(faction, buyPrices(faction), sellPrices(faction));

            if(!original.shouldKill(faction, mutant)) {
                original = mutant;
            }
        }

        original.commit();
        KingLevels.getInstance().resetDailyProductionRateCache(faction);
    }

    private double[] buyPrices(FactionNPC faction) {
        double[] prices = new double[RESOURCES.ALL().size()];
        for (RESOURCE resource : RESOURCES.ALL()) {
            prices[resource.index()] = faction.stockpile.priceBuy(resource.index(), 1);
        }
        return prices;
    }
    private double[] sellPrices(FactionNPC faction) {
        double[] prices = new double[RESOURCES.ALL().size()];
        for (RESOURCE resource : RESOURCES.ALL()) {
            prices[resource.index()] = faction.stockpile.priceSell(resource.index(), 1);
        }
        return prices;
    }
}
