package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import prplegoo.regions.persistence.IDataPersistence;
import settlement.main.SETT;
import snake2d.LOG;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class RDRecipe implements IDataPersistence<RDRecipeData> {
    private int[][] enabledRecipeIndex;

    public RDRecipe(){
    }

    @Override
    public String getKey() {
        return RDRecipeData.class.toString();
    }

    @Override
    public RDRecipeData getData() {
        return new RDRecipeData(enabledRecipeIndex);
    }

    @Override
    public void putData(RDRecipeData data) {
        if (data == null) {
            LOG.ln("RDSlavery.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDSlavery.onGameSaveLoaded: data found, writing");
        enabledRecipeIndex = data.enabledRecipeIndex;
    }

    private void initialize() {
        enabledRecipeIndex = new int[WORLD.REGIONS().all().size()][SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
    }

    public boolean isEnabled(Region region, int roomIndex, int recipeIndex){
        LOG.ln(region.index());
        LOG.ln(roomIndex);
        LOG.ln(recipeIndex);
        LOG.ln(enabledRecipeIndex[region.index()][roomIndex]);
        return enabledRecipeIndex[region.index()][roomIndex] == recipeIndex;
    }

    public static class RDEnabledRecipeBooster extends BValue.BValueSome {
        private final int roomIndex;
        private final int recipeIndex;

        public RDEnabledRecipeBooster(int roomIndex, int recipeIndex) {
            super(1.0);

            this.roomIndex = roomIndex;
            this.recipeIndex = recipeIndex;
        }

        @Override
        public double vGet(Region reg){
            return RD.RECIPES().isEnabled(reg, roomIndex, recipeIndex) ? 1 : 0;
        }
    }
}

