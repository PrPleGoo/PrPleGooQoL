package prplegoo.regions.api.npc;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import lombok.Getter;
import snake2d.util.file.Json;
import world.region.RD;

public class KingLevels {
    @Getter
    private static KingLevels instance;

    public static boolean isActive() {
        return true;
    }

    private final PATHS.ResFolder prplegooResFolder = PATHS.STATS().folder("prplegoo");

    private final KingLevel[] kingLevels;
    private final int[] npcLevels;

    public KingLevels() {
        instance = this;

        Json json = new Json(prplegooResFolder.init.get("NOBLE_LEVELS"));

        Json[] kingLevelJsons = json.jsons("LEVELS");
        kingLevels = new KingLevel[kingLevelJsons.length];

        for (int i = 0; i < kingLevelJsons.length; i++) {
            kingLevels[i] = new KingLevel(kingLevelJsons[i]);
        }

        npcLevels = new int[FACTIONS.MAX];
    }

    public int getLevel(Faction faction) {
        if(!(faction instanceof FactionNPC)) {
            return -1;
        }

        return npcLevels[faction.index()];
    }

    public int getLevel(FactionNPC faction) {
        return npcLevels[faction.index()];
    }

    public KingLevel getKingLevel(Faction faction) {
        if(!(faction instanceof FactionNPC)) {
            return null;
        }

        return kingLevels[getLevel(faction)];
    }

    public KingLevel getKingLevel(FactionNPC faction) {
        return kingLevels[getLevel(faction)];
    }

    public void consumeResources(FactionNPC faction, NPCStockpile npcStockpile, double deltaDays) {
        if (!isActive()) {
            return;
        }

        KingLevel kingLevel = getKingLevel(faction);

        for(RESOURCE resource : RESOURCES.ALL()) {
            double amount = kingLevel.getConsumption()[resource.index()] * deltaDays;
            amount += kingLevel.getConsumptionCapitalPop()[resource.index()] * deltaDays * RD.RACES().population.get(faction.realm().capitol());
            // TODO: ConsumptionPreferredFood
            // TODO: ConsumptionFurniture
            // TODO: ConsumptionPreferredDrink
            // TODO: Consumption from military
            // TODO: Apply spoilage

            npcStockpile.inc(resource, -amount);
        }
    }
}
