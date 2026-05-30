package settlement.room.spirit.grave;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.misc.util.FSERVICE;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import settlement.stats.standing.StatStanding.StandingDef;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.DOUBLE;
import util.data.DOUBLE_O;
import util.data.INT_O;
import util.info.INFO;
import util.race.PERMISSION;
import util.text.D;

public abstract class GraveData {

    private final double disturbanceRemove;
    private int avai = 0;
    private int tot = 0;
    private int res;
    private int maxEmpl = 0;
    private double dist;
    private final RoomBlueprintIns<?> p;
    private final GraveDataClass[] classes = new GraveDataClass[HCLASSES.ALL().size()];
    private double upD;
    private final Grave grave;
    private final PERMISSION.Permission permission;

    private final StandingDef standingDef;

    private static CharSequence ¤¤Respect = "¤Respect";
    private static CharSequence ¤¤RespectD = "¤Respect is gained by furnishing your rooms properly and keeping it fully staffed.";
    private static CharSequence ¤¤Disturbance = "¤Grave Disturbance";
    private static CharSequence ¤¤DisturbanceD = "¤Disturbance happens when a occupied burial service is removed or broken. It will slowly decrease with time as subjects forget. Deactivate the room first and let the remains dissipate before removing them.";
    private static CharSequence ¤¤Burried = "¤Buried";
    private static CharSequence ¤¤BurriedD = "¤The amount of subjects successfully buried recently.";
    private static CharSequence ¤¤Corpses = "¤Failed";
    private static CharSequence ¤¤CorpsesD = "¤The amount of corpses that have failed to be buried recently.";
    private static CharSequence ¤¤Value = "¤Value";
    private static CharSequence ¤¤ValueD = "¤The value is the partition of successfully buried multiplied with respect and degrade.";
    private static CharSequence ¤¤Total = "¤Total Graves";
    private static CharSequence ¤¤TotalD = "¤Total amount of graves.";
    private static CharSequence ¤¤Available = "¤Available";
    private static CharSequence ¤¤AvailableD = "¤Available Graves.";


    static {
        D.ts(GraveData.class);
    }

    public final int composeTime;

    GraveData(RoomBlueprintIns<GraveInstance> p, RoomInitData init, int composeTime){
        this.disturbanceRemove = 1.0/(TIME.secondsPerDay()*16);
        this.p = p;
        standingDef = new StandingDef(init.data());
        grave = new Grave(p, this);
        for (HCLASS c : HCLASSES.ALL())
            classes[c.index()] = new GraveDataClass(c);
        permission = new PERMISSION.Permission(p.info);
        this.composeTime = composeTime;
    }

    public PERMISSION permission() {
        return permission;
    }


    protected void save(FilePutter file){
        file.i(avai);
        file.i(tot);
        file.i(res);
        file.d(dist);
        file.d(upD);
        for (GraveDataClass c : classes)
            c.saver.save(file);
        permission.save(file);
    }


    protected void load(FileGetter file) throws IOException{
        avai = file.i();
        tot = file.i();
        res = file.i();
        dist = file.d();
        upD = file.d();
        for (GraveDataClass c : classes)
            c.saver.load(file);
        permission.load(file);
        int res = 0;
        int tot = 0;
        for (int i = 0; i < p.instancesSize(); i++) {
            GraveInstance ins = (GraveInstance) p.getInstance(i);


            if (ins.active()) {
                double r = respect(ins);
                int t = ins.total();
                res += r*t;
                tot+=t;
                maxEmpl += ins.employees().max();
            }


        }
        this.res = res;
        this.tot = tot;

    }


    protected void clear() {
        avai = 0;
        tot = 0;
        res = 0;
        dist = 0;
        upD = 0;
        maxEmpl = 0;
        for (GraveDataClass c : classes)
            c.saver.clear();
    }

    public FSERVICE burrialService(int tx, int ty) {
        if (grave.get(tx, ty) != null)
            return grave.service;
        return null;
    }

    Grave grave(int tx, int ty) {
        return grave.get(tx, ty);
    }

    public GRAVE_JOB work() {

        if (avai <= 0)
            return null;

        int max = blueprint().instancesSize();
        int ri = RND.rInt(max);

        for (int i = 0; i < blueprint().instancesSize(); i++) {
            RoomInstance ins = blueprint().getInstance((i+ri)%max);

            GRAVE_JOB j = work(ins);
            if (j != null) {
                return j;
            }
        }
        GAME.Notify("strange!");

        return null;

    }

    public GRAVE_JOB work(RoomInstance room) {
        GraveInstance ins = (GraveInstance)room;
        if (ins.available() <= 0)
            return null;
        ins.jobs.searchAgain();
        GRAVE_JOB j = (GRAVE_JOB) ((GraveInstance) ins).jobs.getReservableJob(null);
        if (j == null) {
            GAME.Notify("strange!!");
            return null;
        }else {
            return j;
        }
    }

    public GRAVE_JOB work(int tx, int ty) {
        return grave.job(tx, ty);
    }

    void activate(GraveInstance i, int a, int tot) {
        this.avai += a;
        this.tot += tot;
        int res = (int)(respect(i)*tot);
        this.res += res;
        this.maxEmpl += i.employees().max();
    }

    void deactivate(GraveInstance i, int a, int tot) {
        this.avai -= a;
        this.tot -= tot;
        int res = (int)(respect(i)*tot);
        this.res -= res;
        this.maxEmpl -= i.employees().max();
    }

    void deactivate(GraveInstance i) {
        for (COORDINATE c : i.body()) {
            if (i.is(c)) {
                Grave g = grave.get(c.x(), c.y());
                if (g != null) {
                    g.deactivate();
                }
            }
        }
    }

    void dispose(GraveInstance i, int a, int tot) {
        dist += tot-a;
        for (COORDINATE c : i.body()) {
            if (i.is(c)) {
                Grave g = grave.get(c.x(), c.y());
                if (g != null) {
                    g.dispose();
                }
            }
        }
    }

    public GraveDataClass get(HCLASS c) {
        return classes[c.index()];
    }

    protected abstract double respect(GraveInstance grave);



    public final INT_O<Room> available = new INT_O<Room>() {

        private final INFO info = new INFO(¤¤Available, ¤¤AvailableD);

        @Override
        public int min(Room r) {
            return 0;
        }

        @Override
        public int max(Room r) {
            if (r == null)
                return tot;
            return ((GraveInstance)r).total();
        }

        @Override
        public int get(Room r) {
            if (r == null)
                return avai;
            return ((GraveInstance)r).available();
        }

        @Override
        public INFO info() {
            return info;
        };
    };

    public final INT_O<Room> total = new INT_O<Room>() {

        private final INFO info = new INFO(¤¤Total, ¤¤TotalD);

        @Override
        public int min(Room r) {
            return 0;
        }

        @Override
        public int max(Room r) {
            if (r == null)
                return tot;
            return ((GraveInstance)r).total();
        }

        @Override
        public int get(Room r) {
            if (r == null)
                return tot;
            return ((GraveInstance)r).total();
        }

        @Override
        public INFO info() {
            return info;
        };
    };

    public final DOUBLE_O<Room> respect = new DOUBLE_O<Room>() {

        private final INFO info = new INFO(¤¤Respect, ¤¤RespectD);

        @Override
        public double getD(Room r) {
            if (r == null) {
                double re = 1.0-blueprint().degradeAverage();
                if (tot > 0)
                    re *= (double)res/tot;
                if (maxEmpl > 0) {
                    re *= (double)blueprint().employment().employed()/maxEmpl;
                }

                return re;
            }
            GraveInstance ins = (GraveInstance) r;
            double resp = respect(ins);
            if (ins.active())
                resp *= (double)ins.employees().employed()/ins.employees().max();
            resp *= 1.0 - ins.getDegrade();
            return resp;
        }

        @Override
        public INFO info() {
            return info;
        };

    };

    public DOUBLE disturbance = new DOUBLE() {

        private final INFO info = new INFO(¤¤Disturbance, ¤¤DisturbanceD);

        @Override
        public double getD() {
            double p = STATS.POP().POP.data(null).get(null);
            if (p == 0)
                return dist > 0 ? 1 : 0;
            return CLAMP.d(100.0*dist/p, 0, 1);
        }

        @Override
        public INFO info() {
            return info;
        };
    };

//	public int requestAccessCorpse(Corpse corpse) {
//
//		if (false) {
//			//remove
//		}
//		if (avai == 0 || p.instancesSize() == 0)
//			return -1;
//
//		int i = RND.rInt(p.instancesSize());
//
//		for (int k = 0; k < p.instancesSize(); k++) {
//
//			int ii = (i+k)%p.instancesSize();
//			GraveInstance ins = (GraveInstance) p.getInstance(ii);
//			if (ins.active() && ins.available() > 0) {
//				for (COORDINATE c : ins.body()) {
//					if (ins.is(c)) {
//						Grave g = grave.get(c.x(), c.y());
//						if (g != null && g.isUsable()) {
//							g.setCorpse(corpse);
//							return c.x() + c.y()*SETT.TWIDTH;
//						}
//
//					}
//				}
//				GAME.Notify("nono");
//
//			}
//		}
//		GAME.Notify("nonono");
//		return -1;
//
//	}


    public GRAVE_JOB requestAccessTile() {
        if (avai == 0 || p.instancesSize() == 0)
            return null;

        int i = RND.rInt(p.instancesSize());

        for (int k = 0; k < p.instancesSize(); k++) {

            int ii = (i+k)%p.instancesSize();
            GraveInstance ins = (GraveInstance) p.getInstance(ii);
            if (ins.active() && ins.available() > 0) {
                for (COORDINATE c : ins.body()) {
                    if (ins.is(c)) {
                        GRAVE_JOB g = grave.job(c.x(), c.y());
                        if (g != null && g.jobReserveCanBe()) {

                            return g;
                        }

                    }
                }
                GAME.Notify("nono");

            }
        }
        GAME.Notify("nonono");
        return null;

    }

//	public boolean hasAccessCorpse(int tile, Corpse corpse) {
//
//		if (tile < 0)
//			return false;
//
//		int tx = tile % SETT.TWIDTH;
//		int ty = tile / SETT.TWIDTH;
//		Grave g = grave.get(tx, ty);
//		return g != null && g.corpse() == corpse;
//
//	}
//


    void update(double ds) {
        {
            double d = dist*disturbanceRemove;
            if (d < disturbanceRemove)
                d = disturbanceRemove;
            dist -= d*ds;
            if (dist < 0)
                dist = 0;
        }
        upD += ds;
        if (upD > TIME.secondsPerDay()) {
            upD -= TIME.secondsPerDay();
            for (HCLASS c : HCLASSES.ALL()) {
                classes[c.index()].update();
            }
        }
    }

    public StandingDef standingDef() {
        return standingDef;
    }

    public GraveInfo info(Room r, int i) {
        GraveInfo info = GraveInfo.get((GraveInstance)r, i);
        if (info.hasBody())
            return info;
        return null;
    }


    public interface GRAVE_DATA_HOLDER {

        public GraveData graveData();

    }

    public RoomBlueprintIns<?> blueprint(){
        return p;
    }

    public final class GraveDataClass {
        private final double[] burr = new double[RACES.all().size()];
        private final double[] fails = new double[RACES.all().size()];
        private final HCLASS cl;

        GraveDataClass(HCLASS cl) {
            this.cl = cl;
        }

        final SAVABLE saver = new SAVABLE() {

            @Override
            public void save(FilePutter file) {
                file.ds(burr);
                file.ds(fails);
            }

            @Override
            public void load(FileGetter file) throws IOException {
                file.ds(burr);
                file.ds(fails);
            }

            @Override
            public void clear() {
                Arrays.fill(burr, 0);
                Arrays.fill(fails, 0);
            }
        };

        public DOUBLE_O<Race> burried = new DOUBLE_O<Race>() {

            private final INFO info = new INFO(¤¤Burried, ¤¤BurriedD);

            @Override
            public double getD(Race t) {
                if (t == null) {
                    double m = 0;
                    for (Race r : RACES.all()) {
                        m += burr[r.index];
                    }
                    if (m == 0)
                        return 0;
                    return m;
                }
                return burr[t.index];
            }

            @Override
            public INFO info() {
                return info;
            };

        };

        public DOUBLE_O<Race> failed = new DOUBLE_O<Race>() {

            private final INFO info = new INFO(¤¤Corpses, ¤¤CorpsesD);

            @Override
            public double getD(Race t) {
                if (t == null) {
                    double m = 0;
                    for (Race r : RACES.all()) {
                        m += fails[r.index];
                    }
                    if (m == 0)
                        return 0;
                    return m;
                }
                return fails[t.index];
            }

            @Override
            public INFO info() {
                return info;
            };

        };

        public DOUBLE_O<Race> value = new DOUBLE_O<Race>() {

            private final INFO info = new INFO(¤¤Value, ¤¤ValueD);

            @Override
            public double getD(Race t) {
                if (t == null) {
                    double m = 0;
                    for (Race r : RACES.all()) {
                        m += burr[r.index]*STATS.POP().POP.data(cl).get(r);
                    }
                    if (m == 0)
                        return 0;
                    return m / STATS.POP().POP.data(cl).get(null);
                }

                double tot = burr[t.index] + fails[t.index];
                double res = 1;
                if (tot != 0)
                    res = 1.0 - fails[t.index]/tot;
                res *= 1.0 - 0.5*blueprint().degradeAverage();
                res *= 0.5 + 0.5*respect.getD(null);


                return CLAMP.d(res, 0, 1);
            }

            @Override
            public INFO info() {
                return info;
            };

        };

        public void fail(Corpse c, int delta) {
            fails[c.indu().race().index] += delta;
        }

        void burry(Corpse c) {
            burr[c.indu().race().index] ++;
        }

        void update() {
            for (Race r : RACES.all()) {
                fails[r.index] -= Math.max(fails[r.index]*0.10, 0.10);
                if (fails[r.index] < 0)
                    fails[r.index] = 0;

                burr[r.index] -= Math.max(burr[r.index]*0.10, 0.10);
                if (burr[r.index] < 0)
                    burr[r.index] = 0;

            }
        }

    }

}
