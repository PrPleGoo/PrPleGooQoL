package world.entity.army;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.trade.ITYPE;
import init.C;
import init.RES;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.army.AD;
import world.army.ADArmies;
import world.army.ADDivs;
import world.army.ADSupply;
import world.entity.WEntity;
import world.entity.WEntityConstructor;
import world.entity.caravan.Shipment;
import world.map.pathing.WPath;
import world.map.pathing.WRegFinder.Treaty;
import world.map.regions.Region;
import world.map.regions.centre.WCentre;

public final class WArmy extends WEntity{

    int iteration = 0;
    short index = -1;
    public final Str name = new Str(24);

    private final ADDivs army = new ADDivs(this);
    private final Treaty treaty = new Treaty() {

        @Override
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            if (to == null)
                return true;
            if (tx != to.cx() || ty != to.cy())
                return true;
            if (to.faction() == faction())
                return true;
            if (to.faction() == null || faction() == null)
                return false;
            if (DIP.get(to.faction(), faction()).ally)
                return true;
            return false;
        }
    };
    private final WPath path = new P(treaty);
    static double speed = C.TILE_SIZE*0.1;
    public static final int size = C.TILE_SIZE*2;
    private byte state = 0;
    short stateShort;
    float stateFloat;

    public static int reinforceTiles = 4;
    private float upD = 0;

    void init(int tx, int ty, Faction f) {
        body().moveCX(tx*C.TILE_SIZE + C.TILE_SIZEH);
        body().moveCY(ty*C.TILE_SIZE + C.TILE_SIZEH);
        state = 0;
        iteration ++;
        army.clear();
        path.clear();
        army.clear();
        add();
        if (!added())
            throw new RuntimeException();
        AD.addOnlyToBeCalledFromAnArmy(this, f);
    }

    public WArmy() {
        super(size, size);
    }

    @Override
    protected void save(FilePutter file) {

        file.i(iteration);
        file.s(index);
        name.save(file);
        army.save(file);
        path.save(file);
        file.f(stateFloat);
        file.s(stateShort);
        file.b(state);
        file.f(upD);
    }

    @Override
    protected WEntity load(FileGetter file) throws IOException {
        iteration = file.i();
        index = file.s();
        name.load(file);
        army.load(file);
        path.load(file);

        stateFloat = file.f();
        stateShort = file.s();
        state = file.b();
        upD = file.f();
        WORLD.ENTITIES().armies.load(this);
        return this;
    }

    @Override
    protected void removeAction() {
        for (int i = 0; i < divs().size(); i++) {
            divs().get(i).disband();
            i--;
        }
        checkForResources();
        AD.removeOnlyTobeCalledFromAnArmy(this);
        WORLD.ENTITIES().armies.ret(this);
    }

    public int iteration() {
        return iteration;
    }

//	@Override
//	protected void cl2earP() {
//		index = -1;
//		army.clear();
//		path.clear();
//		state = 0;
//		upD = 1;
//		hasBeenAskedforRegionAssistance = false;
//	}

    @Override
    protected void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
        if (faction() != FACTIONS.player()) {
            if (WORLD.FOW().is(ctx(), cty()))
                return;
        }
        WORLD.ENTITIES().armies.sprite.render(this, r, s, x, y, path.dir());

    }

    @Override
    protected void handleFow() {
        if (faction()== FACTIONS.player()) {
            WORLD.FOW().enlighten(ctx(), cty(), 5);
        }
    }

    @Override
    public Faction faction() {
        return AD.faction(this);
    }

    public ADArmies armies() {
        Faction f = faction();
        if (f != null)
            return f.armies();
        return null;
    }

    @Override
    protected void update(float ds) {

        if (AD.men(null).get(this) == 0) {
            if (region() == null || region().faction() != faction()) {
                remove();
                return;
            }
        }



        int ox = body().x1();
        int oy = body().y1();
        setState((byte) state().update(this, ds).index());
        upD -= ds;
        if (ox != body().x1() || oy != body().y1()) {
            upD = -1;
            WORLD.BATTLES().report(this);
        }else if (upD <= 0) {
            upD = 16;
            checkForResources();
            WORLD.BATTLES().report(this);
        }

    }

    void checkForResources() {
        if (faction() == FACTIONS.player()) {
            for (ADSupply s : AD.supplies().all) {
                if (s.current().get(this) > s.targetAmount(this)) {
                    returnResources();
                    return;
                }
            }
        }
    }

    private void setState(int state) {
        this.state = (byte) state;
    }

    private void returnResources() {
        Shipment ship = WORLD.ENTITIES().caravans.create(ctx(), cty(), FACTIONS.player().capitolRegion(), ITYPE.spoils);
        if (ship != null) {
            for (ADSupply ss : AD.supplies().all) {
                int am = (int) (ss.current().get(this)-ss.targetAmount(this));
                am = CLAMP.i(am, 0, Short.MAX_VALUE);
                if (am > 0) {
                    if (ss.baseHealth == 0)
                        ship.loadAndReserve(ss.res, am);
                    ss.current().inc(this, -am);
                }
            }
        }
    }

    @Override
    protected WEntityConstructor<? extends WEntity> constructor() {
        return WORLD.ENTITIES().armies;
    }

    public short armyIndex() {
        return index;
    }


    public void setDestination(int tx, int ty) {
        if (AD.men(null).get(this) == 0)
            return;
        stop();
        if (path.find(ctx(), cty(), tx, ty))
            setState((byte) WArmyState.moving.index());
    }

    public void besiege(Region reg) {
        if (AD.men(null).get(this) == 0)
            return;
        if (besieging(reg)) {
            WORLD.BATTLES().besige(this, reg);
        }else {
            stop();
            COORDINATE c = besigeTile(reg);
            if (c != null) {
                if (path.find(ctx(), cty(),c.x(), c.y())) {
                    setState((byte) WArmyState.besieging.index());
                    stateFloat = 0;
                    stateShort = (short) reg.index();
                    return;
                }
            }
        }

    }

    public void raid(Region reg) {
        if (AD.men(null).get(this) == 0)
            return;

        if (region() == reg) {
            raid(true);
            return;
        }

        stop();


        COORDINATE c = besigeTile(reg);
        if (c != null) {
            if (path.find(ctx(), cty(),c.x(), c.y())) {
                RES.coos().set(0);
                do {
                    if (reg.is(path.x(), path.y())) {
                        RES.coos().get().set(path.x(), path.y());
                        RES.coos().inc();
                    }
                    path.setNext();
                }while(!path.arrived());

                int am = RES.coos().getI() - 1;
                GAME.Notify(am);

                if (am > 0) {
                    RES.coos().shuffle(am);
                    RES.coos().set(0);
                    if (path.find(ctx(), cty(),RES.coos().get().x(), RES.coos().get().y())) {
                        setState((byte) WArmyState.movingRaid.index());
                        stateFloat = 0;
                        return;
                    }
                }
            }
        }

        besigeTile(reg);

    }

    public void raid(boolean raid) {
        if (raid && !canRaid())
            return;
        if (raid == raiding())
            return;

        stateFloat = (float) 0;

        if (raid) {

            setState((byte) WArmyState.raiding.index());
        }else {
            setState((byte) WArmyState.fortifying.index());
        }
    }

    public boolean canRaid() {
        return region() != null;
    }

    public boolean raiding() {
        return state() == WArmyState.raiding;
    }

    public COORDINATE besigeTile(Region reg) {


        if (WORLD.REGIONS().centre.get(ctx(), cty()) == reg) {
            Rec.TEMP.setDim(WCentre.TILE_DIM+2, WCentre.TILE_DIM+2);
            Rec.TEMP.moveC(reg.cx(), reg.cy());

            for (COORDINATE c : Rec.TEMP) {
                if (reg.isBesigeTile(c.x(), c.y())) {
                    return c;
                }
            }
        }

        if (reg.isBesigeTile(ctx(), cty())) {
            Coo.TMP.set(ctx(), cty());
            return Coo.TMP;
        }

        PathTile t = WORLD.PATH().path(ctx(), cty(), reg.cx(), reg.cy(), Treaty.DUMMY);
        while(t != null) {
            if (reg.isBesigeTile(t.x(), t.y())) {
                return t;
            }
            t = t.getParent();
        }
        return t;
    }


    public void teleport(int tx, int ty) {
        body().moveCX(tx*C.TILE_SIZE + C.TILE_SIZEH);
        body().moveCY(ty*C.TILE_SIZE + C.TILE_SIZEH);
        stop();
        WORLD.BATTLES().report(this);
    }

    public void intercept(WArmy other) {
        if (AD.men(null).get(this) == 0)
            return;
        stop();
        if (path.find(ctx(), cty(), other.ctx(), other.cty())) {
            stateShort = other.armyIndex();
            setState(WArmyState.intercepting.index());
        }
    }

    public WArmy intercepting() {
        if (state() == WArmyState.intercepting) {
            if (stateShort != -1) {
                WArmy aa = WORLD.ENTITIES().armies.get(stateShort);
                if (aa == null || !aa.added()) {
                    return null;
                }
                return aa;
            }

        }
        return null;
    }

    public void stop() {

        path.clear();
        if (state() != WArmyState.fortifying && state() != WArmyState.fortified) {

            stateFloat = 0;
            setState((byte) WArmyState.fortifying.index());
            upD = -1;
        }

    }

    public ADDivs divs() {
        return army;
    }

    public void disband() {
        if(added())
            super.remove();
    }



    @Override
    public WPath path() {
        return path;
    }

    public WArmyState state() {
        return WArmyState.all().get(state);
    }

    public Region region() {
        return WORLD.REGIONS().map.get(ctx(), cty());
    }

    public double supplyAmount() {
        if (WORLD.REGIONS().map.get(ctx(), cty()) != null && WORLD.REGIONS().map.get(ctx(), cty()).faction() == faction())
            return 1.0;
        return 1.0;
    }

    public boolean recruiting() {
        if (WORLD.REGIONS().map.get(ctx(), cty()) != null && WORLD.REGIONS().map.get(ctx(), cty()).faction() == faction())
            return state() == WArmyState.fortified;
        return false;
    }

    public double besigeTimer() {
        if (state() == WArmyState.besieging)
            return stateFloat;
        return 0;
    }

    public boolean besieging(Region reg) {
        return reg != null && reg == besieging();
    }

    public Region besieging() {
        if (state() != WArmyState.besieging)
            return null;
        Region reg = WORLD.REGIONS().getByIndex(stateShort);
        if (reg == null)
            return null;

        if (!DIP.WAR().is(faction(), reg.faction()))
            return null;
        if (AD.men(null).get(this) <= 0)
            return null;

        if (path.isValid())
            return reg.isBesigeTile(path.destX(), path.destY()) ? reg : null;
        return reg.isBesigeTile(ctx(), cty()) ? reg : null;
    }


    @Override
    public String toString() {
        return "[" + index + "]" + name + " (" + ctx() + "," + cty() + ")";
    }


    private static final class P extends WPath {

        private final Treaty t;

        P(Treaty t){
            this.t = t;
        }

        @Override
        public Treaty treaty() {
            return t;
        }


    };

}
