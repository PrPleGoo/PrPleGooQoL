package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import lombok.Getter;
import prplegoo.regions.api.npc.KingLevels;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class FactionGenetic {
    private final RegionGenetic[] regionGenetics;
    @Getter
    private boolean isMutant = false;

    private FitnessRecord[] fitnessRecords;

    public FactionGenetic(FactionNPC faction) {
        regionGenetics = new RegionGenetic[faction.realm().regions()];
        for (int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i] = new RegionGenetic(faction.realm().all().get(i).index());
        }
    }

    public void mutate() {
        isMutant = true;
        for (RegionGenetic regionGenetic : regionGenetics) {
            regionGenetic.mutate();
        }
    }

    public void calculateFitness(FactionNPC faction, double[] buyPrice, double[] sellPrice) {
        fitnessRecords = new FitnessRecord[5];
        // GovPoints, needs to be index 0;
        fitnessRecords[0] = new FitnessRecord(faction, 0) {
            @Override
            public double determineValue(FactionNPC faction) {
                return RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion());
            }
        };
        // Health;
        fitnessRecords[1] = new FitnessRecord(faction, 1) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return RD.HEALTH().get(region) - 0.5;
            }
        };
        // Work force;
        fitnessRecords[2] = new FitnessRecord(faction,2) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return RD.SLAVERY().getWorkforce().bo.get(region);
            }
        };
        // Raiders;
        fitnessRecords[3] = new FitnessRecord(faction,3) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return -GAME.raiders().entry.get(region).probabilityRaw();
            }
        };
        // Money;
        fitnessRecords[4] = new FitnessRecord(faction,4) {
            @Override
            public double determineValue(FactionNPC faction) {
                double totalMoney = RD.OUTPUT().MONEY.boost.get(faction);

                for (RESOURCE resource : RESOURCES.ALL()) {
                    double amount = KingLevels.getInstance().getDailyConsumptionRate(faction, resource);
                    if (amount > 0) {
                        totalMoney -= amount * buyPrice[resource.index()];
                    } else if (amount < 0) {
                        totalMoney += amount * sellPrice[resource.index()];
                    }
                }

                return totalMoney;
            }
        };
        // TODO: add loyalty and pop target count
        // TODO: add slaves to money


        for (FitnessRecord fitnessRecord : fitnessRecords) {
            fitnessRecord.addValue(faction);

            for (int i = 0; i < faction.realm().all().size(); i++) {
                fitnessRecord.addValue(faction, i);
            }
        }
    }

    public boolean hasGovPointDeficitGreaterThan(double target) {
        if (fitnessRecords[0].factionValue >= 0) {
            return false;
        }

        return target > fitnessRecords[0].factionValue;
    }

    public boolean shouldKill(FactionNPC faction, FactionGenetic mutant) {
        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.willIncreaseDeficit(faction, mutant)) {
                return true;
            }
        }

        // we don't have a deficit and neither does the mutant
        double random = GeneticVariables.random();
        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.tryMutation(faction, mutant, random)) {
                return false;
            }
        }

        return true;
    }

    public void commit() {
        for (int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i].commit();
        }
    }

    private abstract class FitnessRecord {
        private final int index;
        private double factionValue = 0;
        private final double[] regionValues;

        public FitnessRecord(FactionNPC faction, int index) {
            this.index = index;
            regionValues = new double[faction.realm().all().size()];
        }

        public double determineValue(FactionNPC faction, Region region) {
            return 0;
        }

        public double determineValue(FactionNPC faction) {
            return 0;
        }

        public void addValue(FactionNPC faction, int regionIndex) {
            regionValues[regionIndex] += determineValue(faction, faction.realm().all().get(regionIndex));
        }

        public void addValue(FactionNPC faction) {
            factionValue += determineValue(faction);
        }

        public boolean willIncreaseDeficit(FactionNPC faction, FactionGenetic mutant) {
            if (factionValue < 0) {
                return factionValue > mutant.fitnessRecords[index].factionValue;
            }
            if (mutant.fitnessRecords[index].factionValue < 0) {
                return true;
            }

            for (int i = 0; i < faction.realm().all().size(); i++) {
                if (regionValues[i] < 0) {
                    return regionValues[i] > mutant.fitnessRecords[index].regionValues[i];
                }
                if (mutant.fitnessRecords[index].regionValues[i] < 0) {
                    return true;
                }
            }

            return false;
        }

        public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
            if (mutant.fitnessRecords[index].factionValue != factionValue) {
                return (mutant.fitnessRecords[index].factionValue - factionValue) / factionValue > random;
            }
            for (int i = 0; i < faction.realm().all().size(); i++) {
                if (mutant.fitnessRecords[index].regionValues[i] != regionValues[i]
                        && (mutant.fitnessRecords[index].regionValues[i] - regionValues[i]) / regionValues[i] > random) {
                    return true;
                }
            }

            return false;
        }
    }
}
