package prplegoo.regions.api;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGEat;
import init.sprite.UI.UI;
import prplegoo.regions.persistence.IDataPersistence;
import snake2d.LOG;
import util.dic.Dic;
import world.WORLD;
import world.map.regions.Region;

import java.nio.file.Path;
import java.util.HashMap;

public class RDFoodConsumption implements IDataPersistence<RDFoodConsumptionData> {
    public final Boostable booster;
    private boolean[][] selectedFoods;

    public RDFoodConsumption() {
        booster = BOOSTING.push("FOOD_CONSUMPTION", 0, Dic.¤¤Food, Dic.¤¤Food, UI.icons().s.sprout, BoostableCat.ALL().WORLD);
        initialize();
    }

    @Override
    public String getKey() {
        return RDFoodConsumption.class.toString();
    }

    @Override
    public RDFoodConsumptionData getData() {
        return new RDFoodConsumptionData(selectedFoods);
    }

    @Override
    public void putData(RDFoodConsumptionData data) {
        initialize();
        if (data == null) {
            LOG.ln("RDFoodConsumption.onGameSaveLoaded: data null, initializing");
            return;
        }

        LOG.ln("RDFoodConsumption.onGameSaveLoaded: data found");
        if (selectedFoods.length != data.data.length
                || selectedFoods[0].length != data.data[0].length)
        {
            LOG.ln("RDFoodConsumption.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDFoodConsumption.onGameSaveLoaded: data found, writing");
        selectedFoods = data.data;
    }

    @Override
    public Class<RDFoodConsumptionData> getDataClass() {
        return RDFoodConsumptionData.class;
    }

    private void initialize() {
        selectedFoods = new boolean[WORLD.REGIONS().all().size()][RESOURCES.ALL().size()];
        for (int r = 0; r <WORLD.REGIONS().all().size(); r++) {
            for (ResGEat food : RESOURCES.EDI().all()) {
                selectedFoods[r][food.resource.index()] = food.serve;
            }
        }
    }

    public void toggleFood(Region region, RESOURCE resource) {
        selectedFoods[region.index()][resource.index()] = !selectedFoods[region.index()][resource.index()];

        // Check if at least something is selected
        for (RESOURCE food : RESOURCES.EDI().res()) {
            if (selectedFoods[region.index()][food.index()]) {
                return;
            }
        }

        // If not, then undo the change
        selectedFoods[region.index()][resource.index()] = !selectedFoods[region.index()][resource.index()];
    }

    public boolean has(Region t, RESOURCE food) {
        if (!selectedFoods[t.index()][food.index()]) {
            return false;
        }

        return selectedFoods[t.index()][food.index()];
    }

    public int getFoodTypeCount(Region t) {
        int foodTypeCount = 0;
        for (RESOURCE food : RESOURCES.EDI().res()) {
            foodTypeCount += selectedFoods[t.index()][food.index()] ? 1 : 0;
        }

        return foodTypeCount;
    }
}
