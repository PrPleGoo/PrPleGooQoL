package prplegoo.regions.api.gen;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.trade.TradeManager;
import game.time.TIME;
import init.RES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.pathing.WRegFinder;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD;

public class FactionGenerator {
    private final WRegFinder finder = new WRegFinder();
    private final WRegFinder.Treaty neighbors = new WRegFinder.Treaty() {

        @Override
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            if (to == null)
                return true;
            if (to.faction() == null)
                return true;
            if (to != null && to.faction() != origin.faction())
                return false;
            return true;
        }
    };

    private final WRegFinder.Treaty factionFinder = new WRegFinder.Treaty() {
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            return dist < 128;
        }
    };

    private static final FactionGenerator _instance = new FactionGenerator();

    public static FactionGenerator getInstance() {
        return _instance;
    }

    public void generateKingdoms() {
        Region playerCapitol = FACTIONS.player().capitolRegion();
        LIST<WRegFinder.RegDist> neighbors = finder.all(playerCapitol, WRegFinder.Treaty.FACTION_BORDERS, WRegSel.DUMMY(playerCapitol));
        forbiddenIndex = neighbors.get(RND.rInt(neighbors.size())).reg.index();

        AddNpcCluster(FACTIONS.player().capitolRegion(), -1);

        int attempt = 0;
        while (FACTIONS.NPCs().size() < FACTIONS.NPC_MAX() - 10) {
            Region reg = WORLD.REGIONS().active().get(RND.rInt(WORLD.REGIONS().active().size()));
            if (reg.faction() == null) {
                attempt = AddNpcCluster(reg, attempt);
            }

            if (attempt >= 50) {
                break;
            }
        }
    }

    private static int forbiddenIndex = -1;

    public int AddNpcCluster(Region region, int attemptCount) {
        if (attemptCount != -1) {
            boolean emptyArea = finder.all(region, this.factionFinder, new WRegSel() {
                @Override
                public boolean is(Region t) {
                    return t.faction() != null;
                }
            }).isEmpty();

            if (!emptyArea) {
                return attemptCount + 1;
            }
        }

        ArrayListGrower<Region> relevantRegions = new ArrayListGrower<>();
        relevantRegions.add(region);

        int npcCount = RND.rInt(2) + 3;
        while (npcCount > 0 && !relevantRegions.isEmpty()) {
            Region attempt = relevantRegions.get(0);
            relevantRegions.remove(0);

            LIST<WRegFinder.RegDist> neighbors = finder.all(attempt, this.neighbors, WRegSel.DUMMY(attempt));

            if (attempt.faction() == FACTIONS.player()
                    && neighbors.size() == 1) {
                attempt = WORLD.REGIONS().active().get(forbiddenIndex);
                neighbors = finder.all(attempt, this.neighbors, WRegSel.DUMMY(attempt));
            } else if (attempt.index() == forbiddenIndex) {
                continue;
            }

            for (WRegFinder.RegDist regDist : neighbors) {
                if (regDist.reg.index() == forbiddenIndex
                        || regDist.reg.faction() == FACTIONS.player()) {
                    continue;
                }

                relevantRegions.add(regDist.reg);
            }

            if (attempt.index() == forbiddenIndex
                || attempt.faction() != null) {
                continue;
            }

            FACTIONS.activateNext(attempt, null, false);
            npcCount--;
        }

        return 0;
    }
}
