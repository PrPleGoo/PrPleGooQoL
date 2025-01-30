
package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import lombok.Getter;

public class RecipeBoosterValue extends BoosterValue {
    @Getter
    private final int buildingIndex;
    @Getter
    private final int recipeIndex;

    public RecipeBoosterValue(BValue v, BSourceInfo info, double to, boolean isMul, int buildingIndex, int recipeIndex) {
        super(v, info, to, isMul);

        this.buildingIndex = buildingIndex;
        this.recipeIndex = recipeIndex;
    }
}
