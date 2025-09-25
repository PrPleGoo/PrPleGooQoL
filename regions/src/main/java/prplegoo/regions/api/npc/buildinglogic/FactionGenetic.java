package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import lombok.Getter;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import prplegoo.regions.api.npc.buildinglogic.fitness.Workforce;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsReligion;
import world.map.regions.Region;
import world.region.RD;
import world.region.Realm;

import java.util.Arrays;
import java.util.stream.IntStream;

public class FactionGenetic {
    @Getter
    private final RegionGenetic[] regionGenetics;
    @Getter
    protected FitnessRecord[] fitnessRecords;
    @Getter
    protected FactionNPC faction;

    public FactionGenetic(FactionNPC faction) {
        this.faction = faction;

        Realm realm = faction.realm();
        regionGenetics = new RegionGenetic[realm.regions()];
        Arrays.setAll(regionGenetics, i -> new RegionGenetic(realm.all().get(i).index()));
    }

    public FactionGenetic(FactionNPC faction, Region region) {
        this.faction = faction;

        regionGenetics = new RegionGenetic[]{
                new RegionGenetic(region.index()),
        };
    }

    public void loadFitness(FactionGenetic faction) {
        fitnessRecords = loadDefault(faction);
    }

    public static FitnessRecord[] loadDefault(FactionGenetic faction) {
        return new FitnessRecord[]{
                new Health(faction, 0),
                new Loyalty(faction, 1),
                new Workforce(faction, 2),
                // TODO: add slaves to money
                // TODO: add religious ambition
                // TODO: add raider concerns (fix the regions actually being calculated for non-players 1st)
        };
    }

    public void calculateFitness() {
        if (fitnessRecords == null) {
            loadFitness(this);
        }

        Arrays.stream(fitnessRecords).forEach(fitnessRecord -> {
            fitnessRecord.addValue(faction);

            IntStream.range(0, regionGenetics.length)
                    .forEach(i -> fitnessRecord.addValue(faction, i, regionGenetics[i]));
        });
    }

    public boolean shouldAdopt(FactionGenetic mutant) {
        // we don't have a deficit and neither does the mutant
        return !mutant.anyFitnessExceedsDeficit(faction)
                && !anyFitnessWillIncreaseDeficit(mutant)
                && anyFitnessIsMutationCandidate(mutant, GeneticVariables.random());
    }

    private boolean anyFitnessIsMutationCandidate(FactionGenetic mutant, double random) {
        return Arrays.stream(fitnessRecords)
                .anyMatch(fitnessRecord -> fitnessRecord.tryMutation(faction, mutant, random));
    }

    private boolean anyFitnessWillIncreaseDeficit(FactionGenetic mutant) {
        return Arrays.stream(fitnessRecords)
                .anyMatch(fitnessRecord -> fitnessRecord.willIncreaseDeficit(mutant));
    }

    public void commit() {
        Arrays.stream(regionGenetics)
                .forEach(RegionGenetic::commit);
    }

    public boolean anyFitnessExceedsDeficit(FactionNPC faction) {
        return Arrays.stream(fitnessRecords)
                .anyMatch(fitnessRecord -> fitnessRecord.exceedsDeficit(faction));
    }
}