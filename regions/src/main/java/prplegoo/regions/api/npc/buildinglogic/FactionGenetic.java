package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import lombok.Getter;
import prplegoo.regions.api.npc.KingLevels;
import world.map.regions.Region;
import world.region.RD;

public class FactionGenetic {
    private final RegionGenetic[] regionGenetics;
    @Getter
    private boolean isMutant = false;

    private double totalGovPoints;
    private double totalHealth;
    private double totalWorkforce;
    private double totalRaiders;
    private double totalMoney;

    public FactionGenetic(FactionNPC faction) {
        regionGenetics = new RegionGenetic[faction.realm().regions()];
        for(int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i] = new RegionGenetic(faction.realm().all().get(i).index());
        }
    }

    public void mutate(FactionNPC faction) {
        isMutant = true;
        for(RegionGenetic regionGenetic : regionGenetics) {
            regionGenetic.mutate();
        }
    }

    public void calculateFitness(FactionNPC faction, double[] buyPrice, double[] sellPrice) {
        // TODO: make record class
        // TODO: add loyalty and pop target count
        // TODO: add slaves to money
        totalGovPoints = 0;
        totalHealth = 0;
        totalWorkforce = 0;
        totalRaiders = 0;
        totalMoney = 0;

        for (Region region : faction.realm().all()) {
            totalGovPoints += RD.BUILDINGS().costs.GOV.bo.get(region);
            totalHealth += RD.HEALTH().get(region);
            totalWorkforce += RD.SLAVERY().getWorkforce().bo.get(region);
            totalRaiders += GAME.raiders().entry.get(region).probabilityRaw();

            totalMoney += RD.OUTPUT().MONEY.boost.get(faction);

            for (RESOURCE resource : RESOURCES.ALL()) {
                double amount = KingLevels.getInstance().getDailyConsumptionRate(faction, resource);
                if (amount > 0) {
                    totalMoney += amount * sellPrice[resource.index()];
                } else if (amount < 0) {
                    totalMoney += amount * buyPrice[resource.index()];
                }
            }
        }
    }

    public boolean shouldKill(FactionGenetic mutant) {
        if (totalGovPoints < 0) {
            if(totalGovPoints > mutant.totalGovPoints) {
                return true;
            }
        }
        if (totalHealth < 0) {
            if(totalHealth > mutant.totalHealth) {
                return true;
            }
        }
        if (totalWorkforce < 0) {
            if(totalWorkforce > mutant.totalWorkforce) {
                return true;
            }
        }
        if (totalRaiders < 0) {
            if(totalRaiders > mutant.totalRaiders) {
                return true;
            }
        }
        if (totalMoney < 0) {
            if(totalMoney > mutant.totalMoney) {
                return true;
            }
        }

        // we don't have a deficit and neither does the mutant
        double random = GeneticVariables.random();
        if ((mutant.totalGovPoints - totalGovPoints) / totalGovPoints < random) {
            return false;
        }
        if ((mutant.totalHealth - totalHealth) / totalHealth < random) {
            return false;
        }
        if ((mutant.totalWorkforce - totalWorkforce) / totalWorkforce < random) {
            return false;
        }
        if ((mutant.totalRaiders - totalRaiders) / totalRaiders < random) {
            return false;
        }
        if ((mutant.totalMoney - totalMoney) / totalMoney < random) {
            return false;
        }

        return true;
    }

    public void commit() {
        for (int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i].commit();
        }
    }
}

