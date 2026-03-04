package game.nobility;

import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;

class NobleOfficeUtil {

	private static CharSequence ¤¤Governing = "Governor";
	private static CharSequence ¤¤GoverningD = "Gives you {0} gov points to use to develop regions with. Each additional rank yields {1} more points.";
	private static CharSequence ¤¤rBoost = "Boosts {0} workers in your {1} with +2.0. Each additional rank boosts {2} more workers.";
	private static CharSequence ¤¤name = "Master of {0}";


	static {
		D.ts(NobleOfficeUtil.class);
	}

	public static LIST<NobleOffice> make(){

		int workers = 50;
		int gov = 20;

		ArrayListGrower<NobleOffice> all = new ArrayListGrower<>();
		for (INDUSTRY_HASER m : SETT.ROOMS().MINES)
			make(all, m, workers);
		make(all, SETT.ROOMS().WOOD_CUTTER, workers);
		for (INDUSTRY_HASER m : SETT.ROOMS().FARMS)
			make(all, m, workers);
		for (INDUSTRY_HASER m : SETT.ROOMS().ORCHARDS)
			make(all, m, workers);
		for (INDUSTRY_HASER m : SETT.ROOMS().PASTURES)
			make(all, m, workers);
		for (INDUSTRY_HASER m : SETT.ROOMS().FISHERIES)
			make(all, m, workers);
		for (INDUSTRY_HASER m : SETT.ROOMS().REFINERS)
			make(all, m, workers);
		for (INDUSTRY_HASER m : SETT.ROOMS().WORKSHOPS)
			make(all, m, workers);

//		CharSequence desc = "" + Str.TMP.clear().add(¤¤GoverningD).insert(0, gov).insert(1, NOBLES.RANK_INCREASE*gov);
//		new NobleOffice(all, gov*1000, BOOSTABLES.CIVICS().GOV, ¤¤Governing, desc, UI.icons().l.admin) {
//
//			@Override
//			public double value(int slots) {
//				return slots/(1000.0);
//			}
//
//
//			@Override
//			public boolean leavesMap() {
//				return true;
//			}
//
//
//			@Override
//			public void hoverValue(GBox b, int slots) {
//				b.add(target.icon);
//				b.textLL(target.name);
//				b.tab(6);
//				b.add(GFORMAT.iIncr(b.text(), slots*gov));
//				b.NL();
//
//			}
//
//
//			@Override
//			public int popBoosted(int slots) {
//				return -1;
//			}
//		};
//		all.get(all.size()-1).special = true;

		return all;
	}

	public static void make(ArrayListGrower<NobleOffice> all, INDUSTRY_HASER i, int workers) {
		make(all, i.industries().get(0).blue, i.industries().get(0).bonus(), workers);
	}

	public static void make(ArrayListGrower<NobleOffice> all, RoomBlueprintImp blue, Boostable bo, int workers) {

		CharSequence desc = "" + Str.TMP.clear().add(¤¤rBoost).insert(0, workers).insert(1, blue.info.names).insert(2, NOBLES.RANK_INCREASE*workers);
		CharSequence name = "" + Str.TMP.clear().add(¤¤name).insert(0, blue.info.names);
		new NobleOffice(all, 2.0, bo, name, desc, blue.icon) {

			@Override
			public double value(int slots) {
				if (blue.employment().employed() <= 0)
					return slots > 0 ? 1 : 0;
				return CLAMP.d((double)slots*workers/blue.employment().employed(), 0, 1);
			}

			@Override
			public RoomBlueprintIns<?> room() {
				if (blue instanceof RoomBlueprintIns<?>)
					return (RoomBlueprintIns<?>) blue;
				return null;
			}

			@Override
			public void hoverValue(GBox b, int slots) {
				b.add(target.icon);
				b.textLL(target.name);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), 2));
				b.add(UI.icons().s.human);
				b.add(GFORMAT.iofkInv(b.text(), slots*workers, blue.employment().employed()));
				b.add(GFORMAT.f0(b.text(), value(slots)*add));
				b.NL();
			}

			@Override
			public int popBoosted(int slots) {
				return slots*workers;
			}

		};

	}



}

