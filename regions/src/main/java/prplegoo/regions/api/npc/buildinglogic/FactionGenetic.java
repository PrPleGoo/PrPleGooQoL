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
    private RegionGenetic[] regionGenetics;
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
        return new FitnessRecord[]{
                new GovPoints(faction, 0),
                new Health(faction, 1),
                new Workforce(faction, 2),
                // Raiders;
                new FitnessRecord(faction, 3) {
                    @Override
                    public double determineValue(FactionNPC faction1, Region region) {
                        return -GAME.raiders().entry.get(region).probabilityRaw();
                    }
                },
                // Money;
                new FitnessRecord(faction, 4) {
                    @Override
                    public double determineValue(FactionNPC faction1) {
                        double totalMoney = RD.OUTPUT().MONEY.boost.get(faction1) * TIME.secondsPerDay * 2;

                        for (RESOURCE resource : RESOURCES.ALL()) {
                            double productionAmount = KingLevels.getInstance().getDailyProductionRate(faction1, resource);
                            if (productionAmount != 0) totalMoney += productionAmount * faction1.stockpile.price.get(resource);
                        }

                        return totalMoney;
                    }
                },
                // Loyalty;
                new Loyalty(faction, 5),
                // Religion;
                new FitnessRecord(faction, 6) {
                    @Override
                    public double determineValue(FactionNPC faction1, Region region) {

                        Induvidual king = faction1.king().induvidual;
                        double tolerance = BOOSTABLES.NOBLE().TOLERANCE.get(king);
                        StatsReligion religionStats = STATS.RELIGION();
                        StatsReligion.StatReligion religiousLikings = religionStats.getter.get(king);

                        double amount = IntStream.range(0, RD.RACES().all.size())
                                .mapToObj(i -> RD.RELIGION().all()) // collect religion list as "religions"
                                .mapToDouble(religions -> IntStream.range(0, religions.size()) // lookup all the religions
                                        .mapToDouble(j -> religiousLikings.opposition(religionStats.ALL.get(j)) * religions.get(j).target(region)) // collect religion's data
                                        .sum())
                                .sum();

                        return amount / tolerance;
                    }
                },
                // TODO: add slaves to money
        };
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
                .anyMatch(fitnessRecord -> fitnessRecord.willIncreaseDeficit(faction, mutant));
    }

    public FactionGenetic adopt(FactionGenetic genetic){
        regionGenetics = genetic.regionGenetics;
        fitnessRecords = genetic.fitnessRecords;
        faction = genetic.faction;

        return this;
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