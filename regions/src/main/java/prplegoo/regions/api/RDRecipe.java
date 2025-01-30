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
    private int[][] enabledRecipeIndex;

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

        LOG.ln("RDSlavery.onGameSaveLoaded: data found, writing");
        enabledRecipeIndex = data.enabledRecipeIndex;
    }

    private void initialize() {
        enabledRecipeIndex = new int[WORLD.REGIONS().all().size()][SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
    }

    public boolean isEnabled(Region region, RoomBlueprintImp blue, int industryIndexOnBlue){
        return enabledRecipeIndex[region.index()][blue.index()] == industryIndexOnBlue;
    }

    public void setRecipe(Region region, RoomBlueprintImp blue, int industryIndexOnBlue){
        enabledRecipeIndex[region.index()][blue.index()] = industryIndexOnBlue;
    }

    public static class RDEnabledRecipeBooster extends BoosterValue {
        private final RoomBlueprintImp blue;
        private final int recipeIndex;

        public RDEnabledRecipeBooster(BValue v, BSourceInfo info, double to, boolean isMul, RoomBlueprintImp blue, int recipeIndex) {
            super(v, info, to, isMul);

            this.blue = blue;
            this.recipeIndex = recipeIndex;
        }

        @Override
        public double getValue(double input){
            return input;
        }

        public double getIfRecipe(Region reg, int recipeIndex){
            return RD.RECIPES().isEnabled(reg, blue, recipeIndex) ? to() : 0;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            if(!(o instanceof Region)){
                return 0;
            }

            return getIfRecipe((Region) o, recipeIndex);
        }
    }
}

