package prplegoo.regions.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.BoosterValue;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import prplegoo.regions.persistence.IDataPersistence;
import settlement.main.SETT;
import settlement.room.industry.module.FlatIndustries;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.dic.Dic;
import world.WORLD;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RDOutputs;
import world.region.pop.RDRace;

import java.util.HashMap;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class RDRecipeData {
    public int[][] enabledRecipeIndex;

    public RDRecipeData(int[][] enabledRecipeIndex) {
        this.enabledRecipeIndex = enabledRecipeIndex;
    }
}
