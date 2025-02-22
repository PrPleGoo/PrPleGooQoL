package game.nobility;

import java.io.IOException;
import java.util.Arrays;

import game.GAME.GameResource;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.BoosterValue;
import game.debug.Profiler;
import game.faction.npc.FactionNPC;
import game.faction.player.BoostCompound;
import init.sprite.UI.UI;
import init.text.D;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.updating.IUpdater;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import view.ui.message.MessageText;
import settlement.stats.STATS;

import java.io.IOException;
import java.util.Arrays;

public final class NOBLES extends GameResource{

	public static final int MAX_RANK = 8;
	public static final int RANK_INCREASE = 5;

	private final ArrayList<Noble> active = new ArrayList<Noble>(100);
	private final ArrayList<Noble> all = new ArrayList<Noble>(100);
	private final IUpdater upper;

	public final BoostSpecs boosters;
	public final Boostable MAX;
	public final Boostable MAX_RANKS;
	final BoostCompound<NobleOffice> bos;

	public final LIST<NobleOffice> OFFICES = NobleOfficeUtil.make();



	private int ranksAllocated = 0;
	private int[] allocations = new int[OFFICES.size()];
	private int ri = -1;

	public NOBLES() {
		super("NOBILITIES", false);
		D.t(this);
		MAX = BOOSTING.push("NOBLES_MAX", 0, HCLASSES.NOBLE().names, D.g("desc", "The amount of nobles you may appoint."), UI.icons().s.noble, BOOSTABLES.CIVICS());
		MAX_RANKS = BOOSTING.push("NOBLES_RANKS_MAX", 0, D.g("rname", "Noble Promotions"), D.g("rdesc", "The amount of promotions you can offer your nobles."), UI.icons().s.noble, BOOSTABLES.CIVICS());
		boosters = new BoostSpecs(HCLASSES.NOBLE().names, UI.icons().s.noble, true);

		while(all.hasRoom())
			new Noble(all);


		upper = new IUpdater(all.size(), 10) {

			@Override
			protected void update(int i, double timeSinceLast) {
				all.get(i).update(timeSinceLast);
			}
		};

		bos = new BoostCompound<NobleOffice>(boosters, OFFICES) {

			//double npc = CLAMP.d(all.size()/20.0, 0, 1);

			@Override
			protected BoostSpecs bos(NobleOffice t) {
				return t.boosts;
			}

			@Override
			protected double get(Boostable bo, FactionNPC f, boolean isMul) {
				return 0;
			}

			@Override
			protected double getValue(NobleOffice t) {
				return t.value(allocations(t));
			}




		};

		IDebugPanel.add("noble galore", new ACTION() {

			@Override
			public void exe() {
				new BoosterValue(BValue.VALUE1, new BSourceInfo("cheat", UI.icons().s.cancel), 10, false).add(MAX);
				new BoosterValue(BValue.VALUE1, new BSourceInfo("cheat", UI.icons().s.cancel), 10, false).add(MAX_RANKS);
			}
		});


	}

	@Override
	protected void save(FilePutter file) {
		for (Noble n : all)
			n.saver.save(file);
		upper.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Noble n : all)
			n.saver.load(file);
		upper.load(file);
		bos.clearChache();
		ri = -1;
		active.clear();
		for (Noble n : all) {
			if (n.subject() != null)
				active.add(n);
		}
	}

	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(NOBLES.class);
		upper.update(ds);
		prof.logEnd(NOBLES.class);
	}

	public LIST<Noble> ALL(){
		return all;
	}



	private void cache() {
		if (ri != active.size()) {
			ri = active.size();
			ranksAllocated = 0;
			Arrays.fill(allocations, 0);
			for (int ni = 0; ni < active.size(); ni++) {
				ranksAllocated += active.get(ni).rank();
				NobleOffice n = active.get(ni).office();
				if (n != null)
					allocations[n.index] += 1 + 5*active.get(ni).rank();
			}
		}
	}

	public int ranksAllocated() {
		cache();
		return ranksAllocated;
	}

	public int allocations(NobleOffice o) {
		cache();
		return allocations[o.index];
	}

	public void ranksAllocate(Noble n) {
		if (ranksAllocated() < (int)MAX_RANKS.get(POP_CL.clP())) {
			n.rankInc();
			ri = -1;
		}
	}

	public short assignOnlyCallFromHumanoid(Humanoid h) {
		if (!active.hasRoom())
			return -1;
		for (Noble n : all) {
			if (n.subject() == null) {
				n.assign(h);
				bos.clearChache();
				ri = -1;
				if (active.contains(n))
					throw new RuntimeException();
				active.add(n);
				return (short) n.index;
			}
		}
		throw new RuntimeException();
	}

	public void vacateOnlyCallFromHumanoid(Humanoid h, short pos) {
		Noble e = all.get(pos);
		if (e.subject() != h)
			throw new RuntimeException();
		active.remove(e);

		Humanoid first = null;
		Humanoid pick = null;
		for (ENTITY ent : SETT.ENTITIES().getAllEnts()) {
			if (ent instanceof Humanoid) {
				Humanoid a = (Humanoid) ent;
				if (!(a.indu().hType() == HTYPES.SUBJECT())
					|| a.isRemoved()) {
					continue;
				}

				if (first == null) {
					first = a;
				}

				if (a.indu().race().index == h.indu().race().index){
					pick = a;
					break;
				}
			}
		}

		if (pick == null) {
			pick = first;
		}

		ACTION no = new ACTION() {

			@Override
			public void exe() {
				e.saver.clear();
				ri = -1;
				bos.clearChache();
			}
		};

		if (pick != null) {
			Str pickText = new Str("{0}, {2}, {1} years old")
					.insert(0, STATS.APPEARANCE().name(pick.indu()))
					.insert(1, (int)Math.ceil(STATS.POP().age.years.getD(pick.indu())))
					.insert(2, pick.race().info.name);

			Humanoid finalPick = pick;
			ACTION yes = new ACTION() {
				@Override
				public void exe() {
					int rank = e.rank();
					int officeIndex = -1;
					if (e.office() != null) {
					    officeIndex = e.office().index;
					}

					no.exe();

					finalPick.nobleSet();
					Noble newNoble = finalPick.noble();

                    if (officeIndex != -1) {
                        newNoble.setOffice(OFFICES.get(officeIndex));
                    }
					for (int i = 0; i < rank; i++) {
						newNoble.rankInc();
					}
				}
			};

			MessageText message = new MessageText(¤¤title, new Str(¤¤mess).insert(0, e.title()));
			message.send();
			VIEW.messages().hide();

			VIEW.inters().yesNo.activate(new Str(¤¤mess)
					.insert(0, e.title())
					.add(new Str(" Thankfully their will mentions a successor {0}: {1}, assign them now?")
						.insert(0, pick.race().index == h.race().index ? "of the same race" : "of a different race")
						.insert(1, pickText)),
					yes, no, false);
		}
		else {
			new MessageText(¤¤title, new Str(¤¤mess).insert(0, e.title())).send();
			no.exe();
		}
	}

	public void setOffice(Noble n, NobleOffice office) {
		n.setOffice(office);
		ri = -1;
	}

	public Noble get(short index) {
		return all.get(index);
	}

	public LIST<Noble> active() {
		return active;
	}

	private static CharSequence ¤¤title = "Nobility passed!";
	private static CharSequence ¤¤mess = "It is a sad day. Our noble {0} passed today. We can now assign a new nobleman to this cause.";

	static {
		D.ts(NOBLES.class);
	}



}
