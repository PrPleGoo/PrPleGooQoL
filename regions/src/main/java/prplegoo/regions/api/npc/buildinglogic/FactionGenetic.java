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

    public void loadFitness() {
        fitnessRecords = loadDefault(faction);
    }

    public static FitnessRecord[] loadDefault(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[7];
        fitnessRecords[0] = new GovPoints(faction, 0);
        fitnessRecords[1] = new Health(faction, 1);
        fitnessRecords[2] = new Workforce(faction, 2);
        // Raiders;
        fitnessRecords[3] = new FitnessRecord(faction, 3) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return -GAME.raiders().entry.get(region).probabilityRaw();
            }
        };
        // Money;
        fitnessRecords[4] = new FitnessRecord(faction, 4) {
            @Override
            public double determineValue(FactionNPC faction) {
                double totalMoney = RD.OUTPUT().MONEY.boost.get(faction) * TIME.secondsPerDay * 2;

                for (RESOURCE resource : RESOURCES.ALL()) {
                    double price = faction.stockpile.price.get(resource);

                    double productionAmount = KingLevels.getInstance().getDailyProductionRate(faction, resource);
                    if (productionAmount < 0) totalMoney += productionAmount * price;
                    else if (productionAmount > 0) totalMoney += productionAmount * price;
                }

                return totalMoney;
            }
        };
        // Loyalty;
        fitnessRecords[5] = new Loyalty(faction, 5);
        // Religion;
        fitnessRecords[6] = new FitnessRecord(faction, 6) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                double amount;

                double tolerance = BOOSTABLES.NOBLE().TOLERANCE.get(faction.king().induvidual);
                StatsReligion.StatReligion religiousLikings = STATS.RELIGION().getter.get(faction.king().induvidual);

                amount = IntStream.range(0, RD.RACES().all.size())
                        .mapToDouble(i -> IntStream.range(0, RD.RELIGION().all().size()) // lookup all the religions
                                .mapToDouble(j -> religiousLikings.opposition(STATS.RELIGION().ALL.get(j)) * RD.RELIGION().all().get(j).target(region)) // collect religion's data
                                .sum())
                        .sum();

                return amount / tolerance;
            }
        };
        // TODO: add slaves to money

        return fitnessRecords;
    }

    public void calculateFitness(Boolean shouldLoadFitness) {
        if (shouldLoadFitness) loadFitness();

        Arrays.stream(fitnessRecords).forEach(fitnessRecord -> {
            fitnessRecord.addValue(faction);

            IntStream.range(0, faction.realm().all().size())
                    .forEach(i -> fitnessRecord.addValue(faction, i));
        });
    }

    public boolean shouldAdopt(FactionGenetic mutant) {
        // we don't have a deficit and neither does the mutant
        return !mutant.anyFitnessExceedsDeficit(faction) && !anyFitnessWillIncreaseDeficit(mutant) && anyFitnessIsMutationCandidate(mutant, GeneticVariables.random());
    }

    private boolean anyFitnessIsMutationCandidate(FactionGenetic mutant, double random) {
        return Arrays.stream(fitnessRecords).anyMatch(fitnessRecord -> fitnessRecord.tryMutation(faction, mutant, random));
    }

    private boolean anyFitnessWillIncreaseDeficit(FactionGenetic mutant) {
        return Arrays.stream(fitnessRecords).anyMatch(fitnessRecord -> fitnessRecord.willIncreaseDeficit(faction, mutant));
    }

    public void commit() {
        Arrays.stream(regionGenetics).forEach(RegionGenetic::commit);
    }

    public boolean anyFitnessExceedsDeficit(FactionNPC faction) {

        return Arrays.stream(fitnessRecords).anyMatch(fitnessRecord -> fitnessRecord.exceedsDeficit(faction));
    }
}

