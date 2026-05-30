package settlement.entity.humanoid.ai.subject;
import static settlement.main.SETT.THINGS;

import game.GAME;
import game.time.TIME;
import init.type.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.spirit.dump.ROOM_DUMP;
import settlement.room.spirit.grave.GRAVE_JOB;
import settlement.room.spirit.grave.GraveData.GRAVE_DATA_HOLDER;
import settlement.room.spirit.grave.ROOM_GRAVEYARD;
import settlement.room.spirit.grave.ROOM_TOMB;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.text.D;

public final class PlanBuryCorpse extends PLANRES {

    private static CharSequence ¤¤verb = "burying corpse";

    private int timeout = -1;

    static {
        D.ts(PlanBuryCorpse.class);
    }

    PlanBuryCorpse() {
        super("SUBJECT_GRAVE");

    }



    public boolean shouldBury(Humanoid a, AIManager d) {

        if (timeout == TIME.hours().bitsSinceStart())
            return false;

        if (GAME.ARMIES().enemy().men() > 0)
            return false;

        if (!SETT.PATH().availability.is(a.tc()))
            return false;

        if (!SETT.PATH().reachability.is(a.tc()))
            return false;

        return true;
    }

    @Override
    protected AISubActivation init(Humanoid a, AIManager d) {

        if (GAME.ARMIES().enemy().men() > 0)
            return null;

        if (!SETT.PATH().reachability.is(a.tc()))
            return null;

        boolean has = false;

        if (!SETT.ROOMS().DUMP.service().finder.has(a.tc())) {
            for (GRAVE_DATA_HOLDER b : SETT.ROOMS().GRAVES) {
                if (b.graveData().available.get(null) > 0) {
                    has = true;
                    break;
                }
            }
        }else {
            has = true;
        }

        if (!has) {
            timeout = TIME.hours().bitsSinceStart();
            return null;
        }


        if (!SETT.PATH().finders.corpses.reserve(a.tc(), d.path, Integer.MAX_VALUE)) {
            timeout = TIME.hours().bitsSinceStart();
            return null;
        }
        Corpse c = SETT.PATH().finders.corpses.getResult();
        d.planObject = c.index();

        HCLASS cl = c.indu().hType().parentClass();
        for (StatGrave g : c.indu().race().service().GRAVES.get(cl.index())) {

            if (g.grave().permission().get(cl, c.indu().race())) {
                GRAVE_JOB grave = g.grave().requestAccessTile();
                if (grave != null) {
                    d.planTile.set(grave.jobCoo());
                    grave.jobReserve(null);
                    return fetchCorpse.set(a, d);
                }
            }
        }

        AISubActivation s = dumpStart.set(a, d);

        if (s == null)
            timeout = TIME.hours().bitsSinceStart();
        return s;

    }

    private final Resumer dumpStart = new Resumer(¤¤verb) {

        private ROOM_DUMP dump = SETT.ROOMS().DUMP;

        @Override
        public AISubActivation setAction(Humanoid a, AIManager d) {
            COORDINATE coo = dump.service().finder.reserve(a.tc(), Integer.MAX_VALUE);
            if (coo != null) {
                d.planTile.set(coo);
                Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
                AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
                if (s != null) {
                    return s;
                }
                can(a, d);
            }
            Corpse c =  SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (c != null)
                c.findableReserveCancel();
            return null;
        }

        @Override
        public AISubActivation res(Humanoid a, AIManager d) {

            return dumpRet.set(a, d);
        }

        @Override
        public boolean con(Humanoid a, AIManager d) {
            return SETT.THINGS().corpses.getByIndex((short) d.planObject) != null && SETT.THINGS().corpses.getByIndex((short) d.planObject).canBeDragged();
        }

        @Override
        public void can(Humanoid a, AIManager d) {
            FSERVICE s = dump.service().service(d.planTile.x(), d.planTile.y());
            if (s != null)
                s.findableReserveCancel();
            Corpse c =  SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (c != null)
                c.findableReserveCancel();
        }
    };

    private final Resumer dumpRet = new Resumer(¤¤verb) {

        private ROOM_DUMP dump = SETT.ROOMS().DUMP;

        @Override
        public AISubActivation setAction(Humanoid a, AIManager d) {
            Corpse c =  SETT.THINGS().corpses.getByIndex((short) d.planObject);
            return AI.SUBS().walkTo.drag(a, d, SETT.THINGS().corpses.draggable, c.index(), d.planTile);
        }

        @Override
        public AISubActivation res(Humanoid a, AIManager d) {
            Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (c != null) {
                dump.burry(c, d.planTile.x(), d.planTile.y());
                HCLASS cl = c.indu().hType().parentClass();
                for (StatGrave g : c.indu().race().service().GRAVES.get(cl.index())) {
                    if (g.grave().permission().get(cl, c.indu().race())) {
                        g.grave().get(cl).fail(c, 1);
                    }
                }
                c.remove();
            }else {
                can(a, d);
            }
            return null;
        }

        @Override
        public boolean con(Humanoid a, AIManager d) {
            return dump.service().service(d.planTile.x(), d.planTile.y()) != null;
        }

        @Override
        public void can(Humanoid a, AIManager d) {
            FSERVICE s = dump.service().service(d.planTile.x(), d.planTile.y());
            if (s != null)
                s.findableReserveCancel();
            Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (c != null)
                c.findableReserveCancel();
        }
    };

    private GRAVE_JOB job(AIManager d) {
        RoomBlueprintImp b = SETT.ROOMS().map.blueprintImp.get(d.planTile);
        if (b != null && b instanceof GRAVE_DATA_HOLDER) {
            return ((GRAVE_DATA_HOLDER)b).graveData().work(d.planTile.x(), d.planTile.y());
        }
        return null;
    }


    private final Resumer fetchCorpse = new Resumer(¤¤verb) {

        @Override
        protected AISubActivation setAction(Humanoid a, AIManager d) {
            return AI.SUBS().walkTo.path(a, d);
        }

        @Override
        protected AISubActivation res(Humanoid a, AIManager d) {
            return returnCorpse.set(a, d);
        }

        @Override
        public boolean con(Humanoid a, AIManager d) {
            Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (c == null || !c.canBeDragged())
                return false;
            GRAVE_JOB job = job(d);
            return job != null && job.jobReservedIs(null);
        }

        @Override
        public void can(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);
            if (job != null)
                job.jobReserveCancel(null);
            Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (c != null)
                c.findableReserveCancel();
        }
    };



    private final Resumer returnCorpse = new Resumer(¤¤verb) {

        @Override
        protected AISubActivation setAction(Humanoid a, AIManager d) {
            Corpse c = SETT.THINGS().corpses.getByIndex((short) d.planObject);
            if (SETT.ROOMS().map.blueprintImp.get(d.planTile) instanceof ROOM_GRAVEYARD) {
                ROOM_GRAVEYARD g = (ROOM_GRAVEYARD) SETT.ROOMS().map.blueprintImp.get(d.planTile);
                for (DIR dir : DIR.ORTHO) {
                    if (g.isGraveHead(d.planTile.x() + dir.x(), d.planTile.y() + dir.y())) {
                        AISubActivation s = AI.SUBS().walkTo.drag(a, d, THINGS().corpses.draggable, c.index(), d.planTile.x() + dir.x(), d.planTile.y() + dir.y());
                        if (s != null)
                            return s;
                    }
                }
            }
            if (SETT.ROOMS().map.blueprintImp.get(d.planTile) instanceof ROOM_TOMB) {
                ROOM_TOMB g = (ROOM_TOMB) SETT.ROOMS().map.blueprintImp.get(d.planTile);
                for (DIR dir : DIR.ORTHO) {
                    if (g.isGraveHead(d.planTile.x() + dir.x(), d.planTile.y() + dir.y())) {
                        AISubActivation s = AI.SUBS().walkTo.drag(a, d, THINGS().corpses.draggable, c.index(), d.planTile.x() + dir.x(), d.planTile.y() + dir.y());
                        if (s != null)
                            return s;
                    }
                }
            }


            AISubActivation s = AI.SUBS().walkTo.drag(a, d, THINGS().corpses.draggable, c.index(), d.planTile);
            if (s == null) {
                can(a, d);
                c.findableReserveCancel();
            }
            return s;
        }

        @Override
        protected AISubActivation res(Humanoid a, AIManager d) {
            return work.set(a, d);
        }

        @Override
        public boolean con(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);
            return job != null && job.jobReservedIs(null);
        }

        @Override
        public void can(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);
            if (job != null)
                job.jobReserveCancel(null);

        }
    };

    private final Resumer work = new Resumer(¤¤verb) {

        @Override
        protected AISubActivation res(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);
            job.buryAndPerform(SETT.THINGS().corpses.getByIndex((short) d.planObject));
            return null;
        }

        @Override
        public boolean con(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);
            return job != null && job.jobReservedIs(null);
        }

        @Override
        public void can(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);
            if (job != null)
                job.jobReserveCancel(null);
        }

        @Override
        protected AISubActivation setAction(Humanoid a, AIManager d) {
            GRAVE_JOB job = job(d);;
            job.jobStartPerforming();
            return AI.SUBS().WORK.activate(a, d, 25);
        }

    };



}