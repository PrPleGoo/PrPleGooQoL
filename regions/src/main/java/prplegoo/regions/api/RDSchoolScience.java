package prplegoo.regions.api;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.sprite.UI.UI;

public class RDSchoolScience {
    public final Boostable booster;

    public RDSchoolScience() {
        booster = BOOSTING.push("SCHOOL_SCIENCE", 0, "Applied science", "How well the science from your capitol is applied across your realm.", UI.icons().s.vial, BoostableCat.ALL().WORLD);
    }
}
