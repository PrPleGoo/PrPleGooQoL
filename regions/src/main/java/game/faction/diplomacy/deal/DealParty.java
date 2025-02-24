package game.faction.diplomacy.deal;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.deal.DealRegs.DealReg;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.ITYPE;
import game.faction.trade.TradeManager;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.type.HTYPES;
import settlement.main.SETT;
import util.data.INT;
import util.data.INT.INTE;
import util.data.INT_O.INT_OE;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.region.RD;

public final class DealParty {

	private double selfWorth;
	private double offerableWorth;
	private Faction f;
	private Faction other;
	private FactionNPC npc;
	private double dist;

	public final INTE credits = new INT.IntImp() {

		@Override
		public int min() {
			return 0;
		};
		@Override
		public int max() {
			Faction fa = f;
			int cr = 0;
			if (fa instanceof FactionNPC) {
				cr = (int) ((FactionNPC) fa).stockpile.credit();
			}else
				cr = (int) f.credits().credits();
			if (cr < 0)
				return 0;
			return cr;
		};

	};
	public final DealRegs regs;
	private final int[] res = new int[RESOURCES.ALL().size()];
	private final int[] sla = new int[RACES.all().size()];

	public final INT_OE<RESOURCE> resources = new INT_OE<RESOURCE>() {

		@Override
		public int get(RESOURCE t) {
			return res[t.index()];
		}

		@Override
		public int min(RESOURCE t) {
			return 0;
		}

		@Override
		public int max(RESOURCE t) {
			if (f == FACTIONS.player())
				return f.res().getAvailable(t);
			return Math.max(f.res().getAvailable(t)-1, 0);
		}

		@Override
		public void set(RESOURCE t, int i) {
			res[t.index()] = i;
		}

	};

	public final INT_OE<Race> slaves = new INT_OE<Race>() {

		@Override
		public int get(Race t) {
			return sla[t.index()];
		}

		@Override
		public int min(Race t) {
			return 0;
		}

		@Override
		public int max(Race t) {
			return f.slaves().available(t);
		}

		@Override
		public void set(Race t, int i) {
			sla[t.index()] = i;
		}

	};

	DealParty(Deal deal, DealRegs.RegData rdata){
		regs = new DealRegs(deal, rdata);
	}




	void clear() {
		credits.set(0);
		Arrays.fill(res, 0);
		Arrays.fill(sla, 0);
		regs.clear();
	}

	void execute() {

		other.credits().inc(credits.get(), CTYPE.DIPLOMACY);
		f.credits().inc(-credits.get(), CTYPE.DIPLOMACY);


		for (DealReg reg : regs.all()) {
			if (reg.is()) {
				if (reg.reg().faction() == FACTIONS.player()) {
					ROPINIONS.STANCE().makeSpecialDeal(npc);
				}

				RD.setFaction(reg.reg(), other, true);
			}
		}

		boolean rr = false;
		for (RESOURCE r : RESOURCES.ALL()) {
			if (res[r.index()] > 0) {
				rr = true;
				break;
			}
		}
		for (Race r : RACES.all()) {
			if (sla[r.index] > 0) {
				rr = true;
				break;
			}
		}

		if (!rr)
			return;



		Shipment s = WORLD.ENTITIES().caravans.create(f.capitolRegion().cx(), f.capitolRegion().cy(), other.capitolRegion(), ITYPE.diplomacy);
		if (s != null) {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = resources.get(r);

				if (a > 0) {
					s.loadAndReserve(r, a);
				}
			}
			for (Race r : RACES.all()) {
				if (slaves.get(r) > 0) {
					s.load(r, slaves.get(r), HTYPES.SLAVE());
				}
			}

		}else {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = resources.get(r);
				if (a > 0) {
					other.buyer().deliver(r, a, ITYPE.diplomacy);
				}
			}
			for (Race r : RACES.all()) {
				if (slaves.get(r) > 0) {
					SETT.ENTRY().add(r, HTYPES.SLAVE(),slaves.get(r));
				}
			}

		}

		for (Race r : RACES.all()) {
			if (slaves.get(r) > 0) {
				other.slaves().trade(r,slaves.get(r), 0);
				f.slaves().trade(r,-slaves.get(r), 0);
			}
		}

		for (RESOURCE r : RESOURCES.ALL()) {
			int a =  resources.get(r);
			if (a > 0) {
				f.seller().remove(r, a, ITYPE.diplomacy);
			}
		}

		clear();

	}

	public double value() {

		double value = 0;
		value += credits.get();

		for (RESOURCE r : RESOURCES.ALL()) {

			if (res[r.index()] > 0)
				value += valueResource(r, res[r.index()]);

		}

		for (Race r : RACES.all()) {
			value += valueSlave(r, sla[r.index()]);
		}

		value += regs.worth();

		return value;


	}


	void init(Faction a, Faction b, FactionNPC evaluator) {

		f = a;
		other = b;
		npc = evaluator;
		regs.init(a, b, evaluator);

		credits.set(0);
		selfWorth = regs.selfWorth();
		offerableWorth = regs.offerableWorth();
		selfWorth += credits.max();
		offerableWorth += credits.max();
		dist = WORLD.PATH().distance(a.capitolRegion(), b.capitolRegion());
		for (RESOURCE r : RESOURCES.ALL()) {
			res[r.index()] = 0;

			double v = valueResource(r, (int)Math.ceil(resources.max(r)*0.75));
			selfWorth += v;
			offerableWorth += v;
		}
		for (Race r : RACES.all()) {
			sla[r.index()] = 0;
			double v = valueSlave(r, slaves.max(r));

			selfWorth += v;
			offerableWorth += v;
		}

		if (a != FACTIONS.player())
			offerableWorth *= 0.25;
		clear();
	}


	public int valueResource(RESOURCE res, int amount) {

		if (f == FACTIONS.player()) {
			int p = npc.seller().buyPrice(res, amount);
			p -= TradeManager.totalFee(FACTIONS.player(), npc, dist, res, amount);
			return Math.max(p, 0);
		}else {
			int p = npc.buyer().priceSell(res, amount);
			p += TradeManager.totalFee(f, other, dist, res, amount);
			if (!DIP.get(f, other).trades)
				p *= 1.5;
			return Math.max(p, 1);
		}

	}

	public static int manualPriceSell(FactionNPC f, RESOURCE res, int amount) {
		int p = f.buyer().priceSell(res, amount);
		p += TradeManager.totalFee(f, FACTIONS.player(), RD.DIST().distance(f), res, amount);
		if (!DIP.get(f).trades)
			p *= 1.5;
		return Math.max(p, 1);
	}

	public static int manualPriceBuy(FactionNPC f, RESOURCE res, int amount) {
		int p = f.seller().buyPrice(res, amount);
		p -= TradeManager.totalFee(FACTIONS.player(), f, RD.DIST().distance(f), res, amount);
		return Math.max(p, 0);
	}

	public int valueSlave(Race res, int amount) {

		if (f == FACTIONS.player()) {
			return npc.slaves().price(res, amount);
		}else {
			return npc.slaves().price(res, -amount);
		}

	}

	public double selfWorth() {
		return selfWorth;
	}

	public double offerableWorth() {
		return offerableWorth;
	}

	public Faction f() {
		return f;
	}

	public FactionNPC npc() {
		return npc;
	}

}
