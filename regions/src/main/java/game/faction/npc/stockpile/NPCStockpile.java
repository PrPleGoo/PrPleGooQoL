package game.faction.npc.stockpile;

import java.io.IOException;

import game.GAME;
import game.GameDisposable;
import game.VERSION;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.NPCResource;
import game.faction.npc.stockpile.UpdaterTree.ResIns;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.KingLevels;
import settlement.entity.ENTETIES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE;
import util.statistics.HistoryResource;
import view.interrupter.IDebugPanel;
import world.region.RD;
import world.region.pop.RDRace;

public class NPCStockpile extends NPCResource{

	public static final int AVERAGE_PRICE = 200;
	private static final double PRICE_MAX = 10.0;
	private static final double PRICE_MIN = 1.0/PRICE_MAX;
	private static final double PILE_SIZE = 25*ENTETIES.MAX/40000.0;

	private static final double PLAYER_AMOUNT = 0.025;

//	private static final double WORK_SPEED = 0.07*ENTETIES.MAX/40000.0;

	static Updater updater;

	static {
		new GameDisposable() {

			@Override
			protected void dispose() {
				updater = null;
			}
		};
	}

	private final SRes[] resses = new SRes[RESOURCES.ALL().size()];
	final FactionNPC f;
	private final DOUBLE credits;
	private double workforce = 1;

	public final HistoryResource price = new HistoryResource(16, TIME.seasons(), true);
	public final HistoryResource forSale = new HistoryResource(16, TIME.seasons(), true);

	public NPCStockpile(FactionNPC f, LISTE<NPCResource> all, DOUBLE credits){
		super(all);
		this.f = f;
		ACTION a = new ACTION() {

			@Override
			public void exe() {
				for (FactionNPC f : FACTIONS.NPCs()) {
					f.stockpile.saver().clear();
					f.stockpile.update(f, 0);
					f.credits().set(0);
					KingLevels.getInstance().pickMaxLevel(f, true);
				}
				GAME.factions().prime();
			}
		};

		if (updater == null) {
			updater = new Updater();
		}
		IDebugPanel.add("TRADE RESET", a);

		this.credits = credits;
		for (RESOURCE res : RESOURCES.ALL()) {
			resses[res.index()] = KingLevels.isActive() ? new KingLevelSRes(res.index()) : new SRes();
		}
	}

	public int amount(RESOURCE res) {
		if (res == null) {
			int tt = 0;
			for (int ri = 0; ri < RESOURCES.ALL().size(); ri++)
				tt += resses[ri].tradeAm();
			return tt;
		}
		return (int)resses[res.index()].tradeAm();
	}

	public int amount(int ri) {
		return (int)resses[ri].tradeAm();
	}

	public void inc(RESOURCE res, double amount) {
		resses[res.index()].offsetInc(amount);
	}

	public void incPlayer(RESOURCE res, double amount) {
		resses[res.index()].playerOffset += amount;
	}

	public double playerTarif(RESOURCE res) {
		SRes r = resses[res.index()];
		double a = playerTradeLimit(res);
		double am = Math.abs(r.playerOffset);
		if (am > a) {
			am -= a;
			return am/=r.amTarget();
		}
		return 0;
	}

	public double playerTraded(RESOURCE res) {
		return resses[res.index()].playerOffset;
	}

	public double playerTradeLimit(RESOURCE res) {
		return resses[res.index()].amTarget()*PLAYER_AMOUNT;
	}

	public double creditScore() {
		double aa = valueOfStockpile();
		aa = (aa + credits.getD())/(aa+1);
		aa = CLAMP.d(aa, PRICE_MIN, PRICE_MAX);
		return aa;
	}

	public double credit() {
		return (valueOfStockpile() + credits.getD());
	}

	public double valueOfStockpile() {
		if (!KingLevels.isActive()) {
			return workforce * AVERAGE_PRICE * RESOURCES.ALL().size();
		}

		double amount = 0;

		for (int i = 0; i < resses.length; i++) {
			amount += resses[i].offset * resses[i].price();
		}

		return amount;
	}

	public double price(int ri, double amount) {

		double mul = f.race().pref().priceMul(RESOURCES.ALL().get(ri));
		SRes r = resses[ri];
		double before = r.price();
		double after = r.priceAt(amount + (KingLevels.isActive() ? r.offset : 0));
		double price = before + (after-before)*0.5;

		price *= creditScore();

		return mul*price;
	}

	public double priceBuy(int ri, double amount) {

		double price = price(ri, amount);
		price *= f.race().pref().priceCap(RESOURCES.ALL().get(ri));


		return price*amount;
	}

	public double priceSell(int ri, double amount) {

		double price = price(ri, -amount);

		return price*amount;

	}

	public double prodRate(RESOURCE res) {
		return resses[res.index()].totRate;
	}

	public double rate(RESOURCE res) {
		return resses[res.index()].rate;
	}

	@Override
	protected SAVABLE saver() {
		return new SAVABLE() {

			@Override
			public void save(FilePutter file) {
				RESOURCES.map().saver().save(resses, file);
				file.d(workforce);
				price.save(file);
				forSale.save(file);
			}

			@Override
			public void load(FileGetter file) throws IOException {
				RESOURCES.map().loader().load(resses, file);
				workforce = file.d();
				price.load(file);
				forSale.load(file);
			}

			@Override
			public void clear() {
				for(SRes r : resses)
					r.clear();
				workforce = 1;
				price.clear();
				forSale.clear();
			}
		};
	}

	@Override
	public void update(FactionNPC faction, double seconds) {
		update(faction, seconds, RD.RACES().population.get(faction.capitolRegion())*0.25 + 0.15*RD.RACES().population.faction().get(faction));
	}

	public void update(FactionNPC faction, double seconds, double wf) {
		KingLevels.getInstance().pickMaxLevel(faction);
		if (!KingLevels.isActive()) {
			updater.tree.update(faction);

			//int wf =  RD.RACES().population.get(faction.capitolRegion());
			//wf *= 0.75 + 0.25*(BOOSTABLES.NOBLE().COMPETANCE.get(faction.court().king().roy().induvidual));


			workforce = wf * PILE_SIZE / RESOURCES.ALL().size();
			for (RESOURCE res : RESOURCES.ALL()) {
				game.faction.npc.stockpile.UpdaterTree.TreeRes o = updater.tree.o(res);
				double prod = 0;
				double prodTot = 0;
				double sp = 1;
				for (ResIns r : o.producers) {
					prod = Math.max(prod, 1.0 / r.prodSpeedBonus);
					double t = 1.0 / r.prodSpeedTot;
					if (t > prodTot) {
						sp = r.rateSpeed;
						prodTot = t;
					}
				}
				resses[res.index()].rateSpeed = sp;
				resses[res.index()].rate = prod;
				resses[res.index()].totRate = prodTot;
			}

			updater.update(this, seconds * TIME.secondsPerDayI);
		}

		KingLevels.getInstance().consumeResources(faction, this, seconds*TIME.secondsPerDayI);

		for (RESOURCE res : RESOURCES.ALL()) {
			price.set(res, (int) Math.round(res(res.index()).price()));
			forSale.set(res, (int) Math.round(res(res.index()).tradeAm()+res(res.index()).offset()));
		}

	}



	@Override
	protected void generate(RDRace race, FactionNPC faction, boolean init) {
		saver().clear();
		update(faction, 0);

	}

	SRes res(int index) {
		return resses[index];
	}

	class KingLevelSRes extends SRes {
		private final int resourceIndex;

		public KingLevelSRes(int resourceIndex){
			this.resourceIndex = resourceIndex;
		}

		@Override
		public double amTarget() {
			return KingLevels.getInstance().getDesiredStockpile(f, RESOURCES.ALL().get(resourceIndex));
		}

		@Override
		public double tradeAm() {
			return offset();
		}

		@Override
		public double offset() {
			return Math.max(offset, 0);
		}

		@Override
		public double price() {
			return super.priceAt(offset);
		}

		@Override
		public double priceAt(double amount) {
			return super.priceAt(amount);
		}

		@Override
		public double amMul(double amount) {
			if (amount <= 0)
				return PRICE_MAX;
			double tar = getNonZeroAmTarget();
			tar /= amount;
			tar = CLAMP.d(tar, PRICE_MIN, PRICE_MAX);
			return tar;
		}

//		@Override
//		public void clear() {
//			super.clear();
//			offset = getNonZeroAmTarget();
//		}

		private double getNonZeroAmTarget(){
			double amTarget = amTarget();
			if (amTarget == 0) {
				// TODO: TOLERANCE as a stand in for curiosity or hoarding or something;
				amTarget = BOOSTABLES.NOBLE().TOLERANCE.get(f.king().induvidual) * 0.9 * Math.pow(10, Math.sqrt(KingLevels.getInstance().getLevel(f))) + 5;
			}

			return amTarget;
		}
	}


	class SRes implements SAVABLE{

		protected double totRate = 1;
		protected double rate = 1;
		protected double rateSpeed = 1;
		protected double offset = 0;
		protected double playerOffset;

		public double rate() {
			return rate;
		}

		public double rateSpeed() {
			return rateSpeed;
		}

		public double rateTot() {
			return totRate;
		}



		public double amTarget() {
			return 1 + rate*workforce;
		}

		public double tradeAm() {
			return Math.max(totRate*workforce+offset, 0);
		}

		public double amMul(double amount) {
			if (amount <= 0)
				return PRICE_MAX;
			double tar = amTarget();
			tar /= amount;
			tar = CLAMP.d(tar, PRICE_MIN, PRICE_MAX);
			return tar;

		}

		public double priceBase() {
			if (totRate == 0)
				return AVERAGE_PRICE*10000;
			return AVERAGE_PRICE/totRate;
		}

		public double price() {
			return priceAt(amTarget()+offset);
		}

		public double priceAt(double amount) {
			return amMul(amount)*priceBase();

		}

		public double offset() {
			return offset;
		}

		void offsetInc(double am) {
			offset += am;
		}

		public double player() {
			return playerOffset;
		}

		void playerSet(double am) {
			playerOffset = am;
		}

		@Override
		public void save(FilePutter file) {
			file.d(totRate);
			file.d(rate);
			file.d(offset);
			file.d(playerOffset);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			totRate = file.d();
			rate = file.d();
			offset = file.d();
			if (!VERSION.versionIsBefore(68, 18))
				playerOffset = file.d();
		}

		@Override
		public void clear() {
			totRate = 1;
			rate = 1;
			offset = 0;
			playerOffset = 0;
			rateSpeed = 1;
		}

	}

	static double WV = 9.99;
	static double WVt = 10;





}
