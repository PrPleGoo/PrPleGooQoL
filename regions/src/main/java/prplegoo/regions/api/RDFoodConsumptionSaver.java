package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.AbstractModSdkScript;
import com.github.argon.sos.mod.sdk.phase.Phase;
import com.github.argon.sos.mod.sdk.phase.PhaseManager;
import com.github.argon.sos.mod.sdk.phase.Phases;
import snake2d.LOG;
import world.region.RD;

import java.nio.file.Path;

public class RDFoodConsumptionSaver extends AbstractModSdkScript {
    @Override
    public boolean forceInit() {
        LOG.ln("RDFoodConsumptionSaver.forceInit");
        return true;
    }

    public CharSequence name() {
        return "RDFoodConsumptionSaver.Saver";
    }

    @Override
    public CharSequence desc() {
        return "Saves RDFoodConsumptionSaver.";
    }

    @Override
    protected void registerPhases(PhaseManager phaseManager) {
        LOG.ln("RDFoodConsumptionSaver.registerPhases");
        phaseManager.register(Phase.ON_GAME_SAVE_LOADED, new Phases() {
            @Override
            public void onGameLoaded(Path saveFilePath) {
                RD.FOOD_CONSUMPTION().onGameLoaded(saveFilePath);
            }
        });
        phaseManager.register(Phase.ON_GAME_SAVED, new Phases() {
            @Override
            public void onGameSaved(Path saveFilePath) {
                RD.FOOD_CONSUMPTION().onGameSaved(saveFilePath);
            }
        });
    }
}
