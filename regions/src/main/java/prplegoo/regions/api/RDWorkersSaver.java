package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.AbstractModSdkScript;
import com.github.argon.sos.mod.sdk.game.error.ErrorHandler;
import com.github.argon.sos.mod.sdk.phase.Phase;
import com.github.argon.sos.mod.sdk.phase.PhaseManager;
import com.github.argon.sos.mod.sdk.phase.Phases;
import snake2d.Errors;
import snake2d.LOG;
import world.region.RD;

import java.nio.file.Path;

public class RDWorkersSaver extends AbstractModSdkScript {
    @Override
    public boolean forceInit() {
        LOG.ln("RDWorkersSaver.forceInit");
        return true;
    }

    public CharSequence name() {
        LOG.ln("RDWorkersSaver.name");
        return "RDWorkers.Saver";
    }

    @Override
    public CharSequence desc() {
        LOG.ln("RDWorkersSaver.desc");
        return "Saves RDWorkers.";
    }

    @Override
    protected void registerPhases(PhaseManager phaseManager) {
        LOG.ln("RDWorkersSaver.registerPhases");
        phaseManager.register(Phase.ON_GAME_SAVE_LOADED, new Phases() {
            @Override
            public void onGameSaveLoaded(Path saveFilePath) {
                RD.WORKERS().onGameSaveLoaded(saveFilePath);
            }
        });
        phaseManager.register(Phase.ON_GAME_SAVED, new Phases() {
            @Override
            public void onGameSaved(Path saveFilePath) {
                RD.WORKERS().onGameSaved(saveFilePath);
            }
        });
    }
}
