package prplegoo.regions.api.npc;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGEat;
import lombok.Getter;
import snake2d.util.file.Json;

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
}
