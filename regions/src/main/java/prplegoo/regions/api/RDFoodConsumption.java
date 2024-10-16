package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.phase.Phases;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.LOG;
import util.dic.Dic;
import world.WORLD;
import world.map.regions.Region;
import world.region.RDOutput;

import java.nio.file.Path;
import java.util.HashMap;

public class RDFoodConsumption implements Phases {
    public final Boostable booster;
    private HashMap<Integer, HashMap<Integer, Boolean>> selectedFoods;

    public RDFoodConsumption() {
        booster = BOOSTING.push("FOOD_CONSUMPTION", 0, Dic.¤¤Food, Dic.¤¤Food, UI.icons().s.sprout, BoostableCat.WORLD_PRODUCTION);

        selectedFoods = new HashMap<>();
        for (Region region : WORLD.REGIONS().all()) {
            HashMap<Integer, Boolean> selectedFood = new HashMap<>();
            for (RESOURCE food : RESOURCES.EDI().res()) {
                selectedFood.put(food.index(), true);
            }

            selectedFoods.put(region.index(), selectedFood);
        }
    }

    public void toggleFood(Region region, RESOURCE resource) {
        if (!selectedFoods.get(region.index()).containsKey(resource.index())) {
            return;
        }

        HashMap<Integer, Boolean> selectedFood = selectedFoods.get(region.index());
        selectedFood.replace(resource.index(), !selectedFood.get(resource.index()));

        boolean anySet = false;
        for (Integer key : selectedFood.keySet()) {
            anySet = anySet || selectedFood.get(key);
        }

        if (anySet) {
            return;
        }

        selectedFood.replace(resource.index(), !selectedFood.get(resource.index()));
    }

    public boolean has(Region t, RESOURCE food) {
        if (!selectedFoods.get(t.index()).containsKey(food.index())) {
            return false;
        }

        return selectedFoods.get(t.index()).get(food.index());
    }

    public int getFoodTypeCount(Region t) {
        HashMap<Integer, Boolean> selectedFood = selectedFoods.get(t.index());

        int foodTypeCount = 0;
        for (Integer key : selectedFood.keySet()) {
            foodTypeCount += selectedFood.get(key) ? 1 : 0;
        }

        return foodTypeCount;
    }

    @Override
    public void onGameLoaded(Path saveFilePath) {
        LOG.ln("RDFoodConsumption.onGameSaveLoaded " + saveFilePath);
    }

    @Override
    public void onGameSaved(Path saveFilePath) {
        LOG.ln("RDFoodConsumption.onGameSaved " + saveFilePath);
    }

}
