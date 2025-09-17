package prplegoo.regions.api;

import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import prplegoo.regions.persistence.IDataPersistence;
import settlement.main.SETT;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprintImp;
import snake2d.LOG;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class RDRecipe implements IDataPersistence<RDRecipeData> {
    private int[][][] enabledRecipeIndex;

    public RDRecipe(){
        initialize();
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

        LOG.ln("RDSlavery.onGameSaveLoaded: data found");
        if (enabledRecipeIndex.length != data.enabledRecipeIndex.length
                || enabledRecipeIndex[0].length != data.enabledRecipeIndex[0].length
                || enabledRecipeIndex[0][0].length != data.enabledRecipeIndex[0][0].length)
        {
            LOG.ln("RDSlavery.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDSlavery.onGameSaveLoaded: data found, writing");
        enabledRecipeIndex = data.enabledRecipeIndex;
    }

    @Override
    public Class<RDRecipeData> getDataClass() {
        return RDRecipeData.class;
    }

    private void initialize() {
        enabledRecipeIndex = new int[WORLD.REGIONS().all().size()][SETT.ROOMS().AMOUNT_OF_BLUEPRINTS*2][SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
    }

    public boolean isEnabled(Region region, int buildingIndex, RoomBlueprintImp blue, int industryIndexOnBlue){
        return enabledRecipeIndex[region.index()][buildingIndex][blue.index()] == industryIndexOnBlue;
    }

    public void setRecipe(Region region, int buildingIndex, RoomBlueprintImp blue, int industryIndexOnBlue){
        enabledRecipeIndex[region.index()][buildingIndex][blue.index()] = industryIndexOnBlue;
    }

    public int getRecipeIndex(Region region, int buildingIndex, RoomBlueprintImp blue) {
        return enabledRecipeIndex[region.index()][buildingIndex][blue.index()];
    }

    public static class RDEnabledRecipeBooster extends BoosterValue {
        private final RoomBlueprintImp blue;
        private final int recipeIndex;
        private final int buildingIndex;

        public RDEnabledRecipeBooster(BValue v, BSourceInfo info, double to, boolean isMul, RoomBlueprintImp blue, int recipeIndex, int buildingIndex) {
            super(v, info, to, isMul);

            this.blue = blue;
            this.recipeIndex = recipeIndex;
            this.buildingIndex = buildingIndex;
        }

        @Override
        public double getValue(double input){
            return input;
        }

        public double getIfRecipe(Region reg, int buildingIndex, int recipeIndex){
            return RD.RECIPES().isEnabled(reg, buildingIndex, blue, recipeIndex) ? to() : 0;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            if(!(o instanceof Region)){
                return 0;
            }

            return getIfRecipe((Region) o, buildingIndex, recipeIndex);
        }
    }
}

