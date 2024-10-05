package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.AbstractModSdkScript;
import com.github.argon.sos.mod.sdk.phase.Phase;
import com.github.argon.sos.mod.sdk.phase.PhaseManager;
import world.region.RD;

public class RDWorkersSaver extends AbstractModSdkScript {
    public CharSequence name() {
        return "RDWorkers.Saver";
    }

    @Override
    public CharSequence desc() {
        return "Saves RDWorkers.";
    }

    @Override
    protected void registerPhases(PhaseManager phaseManager) {
        phaseManager.register(Phase.INIT_SETTLEMENT_UI_PRESENT, RD.WORKERS());
        phaseManager.register(Phase.ON_GAME_SAVED, RD.WORKERS());
        phaseManager.register(Phase.ON_GAME_SAVE_LOADED, RD.WORKERS());
    }
}
