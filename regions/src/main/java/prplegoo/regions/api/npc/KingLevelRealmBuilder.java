package prplegoo.regions.api.npc;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;

public class KingLevelRealmBuilder {
    private final double[] lastUpdateByFactionIndex;

    public KingLevelRealmBuilder(){
        lastUpdateByFactionIndex = new double[FACTIONS.MAX];
    }

    public boolean build(FactionNPC faction) {
        if (!KingLevels.isActive()) {
            return false;
        }

        if (lastUpdateByFactionIndex[faction.index()] == TIME.currentSecond()) {
            return true;
        }

        // do genocide aggression, tolerance, mercy, rng on king name?
            // don't genocide own species, ever

        double[] buyPrice = new double[RESOURCES.ALL().size()];
        double[] sellPrice = new double[RESOURCES.ALL().size()];

        for (RESOURCE resource : RESOURCES.ALL()) {
            buyPrice[resource.index()] = faction.stockpile.priceBuy(resource.index(), 1);
            sellPrice[resource.index()] = faction.stockpile.priceSell(resource.index(), 1);
        }

        FactionGenetic original = new FactionGenetic(faction);
        original.calculateFitness(faction, buyPrice, sellPrice);

        for(int i = 0; i < GeneticVariables.mutationAttemptsPerTick; i ++) {
            FactionGenetic mutant = new FactionGenetic(faction);
            mutant.mutate(faction);
            mutant.calculateFitness(faction, buyPrice, sellPrice);

            if(!mutant.shouldKill(original)) {
                original = mutant;
                lastUpdateByFactionIndex[faction.index()] = TIME.currentSecond();
            }
        }

        if(!original.isMutant()) {
            original.commit();
        }

        return true;
    }
}
