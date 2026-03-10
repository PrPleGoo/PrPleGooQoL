package prplegoo.regions.api.region.rd;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.player.Player;
import init.sprite.UI.UI;
import prplegoo.regions.api.MagicStringChecker;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class RDEstates {
    public Boostable maxEstates;

    private int estateBuildingIndex = -1;
    public RDEstates(){
        maxEstates = BOOSTING.push("MAX_ESTATE_COUNT", 3, "Max estate count", "The amount of estates you can manage before incurring increased costs.", UI.icons().s.admin, BOOSTABLES.CIVICS());
    }

    private void ensureEstateBuildingIndex() {
        if (estateBuildingIndex != -1) {
            return;
        }

        for (int i = 0; i < RD.BUILDINGS().all.size(); i++) {
            if (MagicStringChecker.isEstateBuilding(RD.BUILDINGS().all.get(i).key())) {
                estateBuildingIndex = i;
                break;
            }
        }
    }

    public int getCurrentEstateCount() {
        ensureEstateBuildingIndex();
        if (estateBuildingIndex == -1) {
            return 0;
        }

        int currentEstateCount = 0;
        for (Region region : FACTIONS.player().realm().all()) {
            if (region.capitol()) {
                continue;
            }

            int level = RD.BUILDINGS().all.get(estateBuildingIndex).level.get(region);
            if (level > 0) {
                currentEstateCount++;
            }
        }

        return currentEstateCount;
    }

    public double getConsumptionMultiplier() {
        Player player = FACTIONS.player();

        double estatesOverCap = getCurrentEstateCount() - maxEstates.get(player);

        if (estatesOverCap <= 0) {
            return 1.0;
        }

        estatesOverCap /= 10;

        return 1.0 + estatesOverCap;
    }

    public static class RDEstateBooster extends BoosterValue {
        public RDEstateBooster(BValue v, BSourceInfo info, double to) {
            super(v, info, to, false);
        }

        @Override
        public double getValue(double input){
            return input;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            if(!(o instanceof Region)){
                return 0;
            }

            return to() * RD.ESTATES().getConsumptionMultiplier();
        }
    }
}
