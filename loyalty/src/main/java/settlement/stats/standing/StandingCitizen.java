package settlement.stats.standing;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.time.TIME;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.entry.Immigration;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.info.INFO;
import util.statistics.HistoryInt;
import util.text.D;
import util.text.Dic;
import util.updating.IUpdater;
import view.sett.IDebugPanelSett;
import world.army.AD;

public final class StandingCitizen extends Standing{



	private final HCLASS cl = HCLASSES.CITIZEN();

	{D.t(this);}

	public final CitizenThing expectation = new Expectation();
	public final CitizenThing fullfillment = new Fulfillment();
	public final CitizenThing happiness = new Happiness();
	public final CitizenThing loyaltyTarget = new MainTarget();
	public final CitizenThing loyalty = new Main();
	private double defs[] = new double[RACES.all().size()];
	private double maxes[] = new double[RACES.all().size()];

	private final double[] mains = new double[RACES.all().size()];

	public Boostable POW = BOOSTING.push("FULFILLMENT_EXPONENT", new Json(PATHS.CONFIG().get("Sett")).d("HAPPINESS_EXPONENT"), Dic.empty, Dic.empty, UI.icons().s.question, BoostableCat.ALL().WORLD_DUMP);
	public Boostable MAX = BOOSTING.push("MAX_CITY_POP", Immigration.MAX_POPULATION*new Json(PATHS.CONFIG().get("Sett")).d("MAX_POP", 0, 1), Dic.empty, Dic.empty, UI.icons().s.question, BoostableCat.ALL().WORLD_DUMP);
	public final StandingBuff buff = new StandingBuff(cl);


	@Override
	void save(FilePutter file) {
		happiness.save(file);
		fullfillment.save(file);
		expectation.save(file);
		loyalty.save(file);
		loyaltyTarget.save(file);
		file.ds(mains);
		buff.saver.save(file);
	}

	@Override
	void load(FileGetter file) throws IOException {
		happiness.load(file);
		fullfillment.load(file);
		expectation.load(file);
		loyalty.load(file);
		loyaltyTarget.load(file);
		file.ds(mains);
		setAll();
		buff.saver.load(file);

	}

	@Override
	void clear() {
		happiness.clear();
		fullfillment.clear();
		expectation.clear();
		loyalty.clear();
		loyaltyTarget.clear();
		Arrays.fill(mains, 0);
		buff.saver.clear();
	}

	StandingCitizen(){

		if (false) {
			//happiness should go above 100%
		}

		IDebugPanelSett.add("happiness++", new ACTION() {

			@Override
			public void exe() {
				for (Race race : RACES.all()) {
					happiness.set(race, happiness.getD(race)+0.1);
				}
			}
		});


	}

	private void setAll() {

		for (Race r : RACES.all()) {
			double max = 0;
			double def = 0;
			for (STAT ss : r.stats().standings(cl)) {
				if (!ss.standing().definition(r).get(cl).dismiss) {
					max += ss.standing().definition(r).get(cl).max;
					def += ss.standing().def(cl, r);
				}
			}
			maxes[r.index] = max;
			defs[r.index] = def;
		}
	}

	void update(double ds) {
		updater.update(ds);
		buff.update(ds);
	}


	void init() {
		setAll();
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			update(r, 0);
			mains[ri] = loyaltyTarget.getD(r);
			loyalty.set(r, mains[ri]);
		}
	}

	private final IUpdater updater = new IUpdater(RACES.all().size(), 4) {

		@Override
		protected void update(int i, double timeSinceLast) {

			Race r = RACES.all().get(i);
			StandingCitizen.this.update(r, timeSinceLast);

		}
	};

	private void update(Race race, double ds){
		fullfillment.up(race, ds);
		expectation.up(race, ds);
		happiness.up(race, ds);
		loyaltyTarget.up(race, ds);
		loyalty.up(race, ds);
	}






	final class Main extends CitizenThing {

		private final double inter = 1.0/(100.0*TIME.secondsPerDay());

		Main() {
			super(
					D.g("Loyalty"),
					D.g("loyalty_desc", "The overall loyalty of your citizens. The goal of any despot is to keep this high. Lower loyalty than 100% could lead to bloody riots. Happiness is a big factor, but other means exist...")
			);
		}

		@Override
		double update(Race race, double ds) {

			double now = mains[race.index];

			int t = (int) (loyaltyTarget.getD(race)*100);
			int c = (int) (now*100);
			double d = t-c;
			double mul = 1 + Math.abs(d)/25.0;
			d *= ds*inter*mul;
			double cur = now+d;
			if (d < 0 && cur < loyaltyTarget.getD(race))
				cur = loyaltyTarget.getD(race);
			else if(d > 0 && cur > loyaltyTarget.getD(race))
				cur = loyaltyTarget.getD(race);

			mains[race.index] = cur;
			return cur;
		}

	}

	final class MainTarget extends CitizenThing {

		MainTarget() {
			super(
					D.g("Submission-Target"),
					D.g("submissionTarget_desc", "What your submission will be in a few days.")
			);
		}

		@Override
		double update(Race race, double ds) {

			double h = happiness.getD(race)*BOOSTABLES.BEHAVIOUR().LOYALTY.get(POP_CL.clP(race, cl));
			return h;
		}

	}

	final class Happiness extends CitizenThing {

		Happiness() {
			super(
					D.g("Happiness"),
					D.g("happiness_desc", "Happiness is fulfillment in proportion to expectations. Fulfillment is gained by providing services and a just rule. Expectation is the amount of citizens in your city. Happiness boosts submission and promotes immigration.")
			);
		}

		@Override
		double update(Race r, double ds) {


			double sup = hap(r);
//			return (int)(100*CLAMP.d(sup, 0, 1))/100.0;
			return (int)(100*CLAMP.d(sup, 0, 2.5))/100.0;
		}

		public double hap(Race r) {
			if (STATS.POP().POP.data(cl).get(r, 0) + AD.cityDivs().total(r) == 0) {
				if (r == FACTIONS.player().race())
					return BOOSTABLES.BEHAVIOUR().HAPPI.get(POP_CL.clP(r, cl));
				else if (STATS.POP().POP.data(cl).get(FACTIONS.player().race(), 0) <= 0)
					return 0.5;
				return hap(FACTIONS.player().race())*BOOSTABLES.BEHAVIOUR().HAPPI.get(POP_CL.clP(r, cl));

			}

			double sup = fullfillment.getD(r);
			double exp = expectation.getD(r);
			if (sup <= 0)
				return 0;
			if (exp == 0)
				return 1;
			sup/=exp;


			sup *= BOOSTABLES.BEHAVIOUR().HAPPI.get(POP_CL.clP(r, cl));


			return sup;
		}

	}

	final class Fulfillment extends CitizenThing {



		Fulfillment() {
			super(
					D.g("Fulfillment"),
					D.g("full_desc", "A fulfillment modifier can be access to a tavern, or a road, or increased food servings. Each race have different weights tied to each modifier. Focus should be on the biggest modifiers first. Total Fulfillment is an exponential function of the sum of all your fulfillment modifiers divided by the sum of all max fulfillments.")
			);
		}

		@Override
		double update(Race r, double ds) {
			double d = fullfillment(r);
			return CLAMP.d(d, 0, 1);

		}

		public double fullfillment(Race r) {

			double current = 0;
			double max = maxes[r.index];
			double def = defs[r.index];
			for (STAT ss : r.stats().standings(cl)) {
				current += ss.standing().get(cl, r);
			}


			if (max <= 0)
				return 1;

			double d = 0;
			if (current < def) {
				d = -current/def;
			}else {
				current -= def;
				max -= def;
				d = Math.pow(current/max, fullPow(r));
			}

			if (GAME.player().race() == r) {
				double min = expectation(r, 6, 0);
				if (d < 0)
					return min*-d;
				return CLAMP.d(min + d, 0, 1);

			}else {
				double min = expectation(r, 2, 0);
				if (d < 0)
					return min*-d;
				return CLAMP.d(min + d, 0, 1);
			}

		}

	}



	public double fullPow(Race r) {
		return POW.get(POP_CL.clP(r));
	}



	final class Expectation extends CitizenThing {

		Expectation() {
			super(
					D.g("Expectations"),
					D.g("exp_desc", "As your population grows, so will your subjects' expectations. Expectation is tied to a species occurrence in the climate you've chosen to settle and grows linearly.")
			);
		}



		@Override
		double update(Race race, double ds) {
			return c(race);
		}

		private double c(Race race) {
			double pop = STATS.POP().POP.data(null).get(race, 0)+1 + AD.cityDivs().total(race);
			double popOther = STATS.POP().POP.data(null).get(null, 0)+1+AD.cityDivs().total() -pop;
			return expectation(race, pop, popOther);
		}



	}

	public double expectation(Race race, double amount, double other) {

		double bo = Math.sqrt(amount/(amount+other));
		double exp = (amount+other)/(MAX.get(POP_CL.clP(race)));
		double pe = 1.0/race.population().max;
		if (race != FACTIONS.player().race())
			bo*= 2.0;
		return bo*exp*pe;
	}

	@Override
	public double current(Induvidual a) {
		Race r = a.race();
		double current = 0;
		double max = maxes[r.index];
		double def = defs[r.index];
		for (STAT ss : r.stats().standings(cl)) {
			current += ss.standing().get(a);
		}

		double h = BOOSTABLES.BEHAVIOUR().HAPPI.get(a);

		if (max <= 0)
			return h;

		double d = 0;
		if (current < def) {
			d = -current/def;
		}else {
			current -= def;
			max -= def;
			d = Math.pow(current/max, fullPow(r));
		}

		if (GAME.player().race() == r) {
			double min = expectation(r, 10, 0);
			if (d < 0)
				h = min*-d;
			else
				h =  CLAMP.d(h*(min + d), 0, 1);

		}else {
			double min = expectation(r, 2, 0);
			if (d < 0)
				h = min*-d;
			else
				h = CLAMP.d(h*(min + d), 0, 1);
		}

		double pop = STATS.POP().POP.data(null).get(r, 0)+1 + AD.cityDivs().total(r);
		double popOther = STATS.POP().POP.data(null).get(null, 0)+1+AD.cityDivs().total() -pop;
		h/= expectation(r, pop, popOther);

		return h;
	}

	@Override
	public double current() {
		return loyalty.getD(null);
	}

	@Override
	public double target() {
		return loyaltyTarget.getD(null);
	}

	@Override
	public INFO info() {
		return loyalty.info();
	}


	public static abstract class CitizenThing {

		private final HistoryInt total = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		private final HistoryInt[] histories = new HistoryInt[RACES.all().size()];
		private final INFO info;
		private static final double dd = 10000000;

		CitizenThing(INFO info) {
			this.info = info;
			for (int i = 0; i < histories.length; i++)
				histories[i] = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		}

		CitizenThing(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}

		final void up(Race race, double ds) {
			set(race, update(race, ds));
		}

		abstract double update(Race race, double ds);


		public double getD(Race t) {
			return getD(t, 0);
		}

		public double getD(Race t, int daysBack) {
			HistoryInt h = t == null ? total : histories[t.index];
			double d = h.get(daysBack)/dd;
			return CLAMP.d(d, 0, d);
		}

		void save(FilePutter file) {
			total.save(file);
			for (HistoryInt i : histories)
				i.save(file);
		}

		void load(FileGetter file) throws IOException {
			total.load(file);
			for (HistoryInt i : histories)
				i.load(file);
		}

		void clear() {
			total.clear();
			for (HistoryInt i : histories)
				i.clear();
		}

		void set(Race race, double v) {

			histories[race.index].set((int) (v*dd));
			double total = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				total +=  CLAMP.d(getD(r), 0, 1)*STATS.POP().POP.data(STANDINGS.CITIZEN().cl).get(r, 0);
			}
			double p = STATS.POP().POP.data(STANDINGS.CITIZEN().cl).get(null, 0);
			if (p == 0)
				p = 1;
			total /= p;
			this.total.set((int) (total*dd));
		}

		public INFO info() {
			return info;
		}

	}

}
