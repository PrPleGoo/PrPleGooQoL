package view.sett.ui.room;

import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.WGROUP;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategories;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.employment.RoomEmployment;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GGaugeMutable;
import util.gui.slider.GSliderInt;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.ui.room.UIPanelUtil.RoomRow;

final class UIPanelWorkPrio extends ISidePanel {


	private static CharSequence ¤¤MasterPrio = "¤Master Priority";
	private static CharSequence ¤¤MasterPrioD = "¤When subjects are assigned to workplaces, this is the master priority, and the highest will be filled first. Each workplace will be filled according to the species priorities.";
	static CharSequence ¤¤Adjust = "¤Adjust all by 1.";

	static {
		D.ts(UIPanelWorkPrio.class);
	}


	private WGROUP group;

	public UIPanelWorkPrio() {
		D.t(this);
		titleSet(D.g("Work Priorities"));

		section.addRelBody(4, DIR.S, selector());
		section.addRelBody(2, DIR.S, new Details());

		{
			GuiSection filter = new GuiSection();

			final GETTER.GETTER_IMP<RoomCategoryMain> cat = new GETTER_IMP<RoomCategories.RoomCategoryMain>(SETT.ROOMS().CATS.MAIN_INFRA);


			for (RoomCategoryMain c : SETT.ROOMS().CATS.MAINS) {
				filter.addRightC(0, new CatButt(c, cat));
			}


			final GButt.ButtPanel unused = new GButt.ButtPanel(UI.icons().m.questionmark) {

				@Override
				protected void clickA() {
					selectedToggle();
				};
			};
			unused.selectedSet(true);
			unused.body().setHeight(filter.body().height());
			filter.addRightC(2, unused);

			section.addRelBody(8, DIR.S, filter);

			ArrayListGrower<RENDEROBJ> rows = new ArrayListGrower<>();
			ArrayListGrower<RoomBlueprintIns<?>> rs = new ArrayListGrower<>();

			for (RoomBlueprintIns<?> bb : SETT.ROOMS().ins()) {


				// non-employmentExtra is just jobs that don't make sense to prioritize, like training/retirement/university
				if (bb.employmentExtra() == null) {
					continue;
				}
				rs.add(bb);

				// We should cycle through the worktypes
				rows.add(new WorkButt(bb));
			}

			GScrollRows s = new GScrollRows(rows, HEIGHT - section.body().y2()-16) {
				@Override
				protected boolean passesFilter(int i, RENDEROBJ o) {
					RoomBlueprintIns<?> b = rs.get(i);
					if (b.cat.main() == cat.get()) {
						if (unused.selectedIs() || b.instancesSize() > 0)
							return true;
					}
					return false;
				}
			};

			section.addRelBody(8, DIR.S, s.view());
		}



	}

	void set(Race race, HCLASS cl) {
		group = WGROUP.get(cl == HCLASSES.CITIZEN() ? HTYPES.SUBJECT() : HTYPES.SLAVE(), race);
	}

	private class Details extends GuiSection {

		Details() {
			add(new GStat() {

				@Override
				public void update(GText text) {
					if (group == null)
						return;
					employed(group, text);
				}
			}.hh(STATS.WORK().EMPLOYED.stat().info()));

			addRightC(90, new GStat() {

				@Override
				public void update(GText text) {
					if (group == null)
						return;
					fullfillment(group, text);
				}
			}.hh(STATS.WORK().WORK_FULFILLMENT.info()));

			addRightC(90, new GStat() {

				@Override
				public void update(GText text) {
					if (group == null)
						return;
					skill(group, text);
				}
			}.hh(Dic.¤¤Skill));
		}



		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (group == null)
				return;
			super.render(r, ds);
		}

	}


	private static GText employed(WGROUP group, GText text) {
		int of = STATS.POP().pop(group.race, group.type);
		int c = SETT.ROOMS().employment.TARGET.get(group);
		GFORMAT.iofkInv(text, c, of);
		return text;
	}

	private static GText fullfillment(WGROUP group, GText text) {
		double f = 0;
		double am = 0;
		for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
			f += group.race.pref().getWork(p) * p.target.group(group);
			am += p.target.group(group);
		}
		f /= am;
		GFORMAT.percGood(text, f);
		return text;
	}

	private static GText skill(WGROUP group, GText text) {
		double f = 0;
		int am = 0;
		for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
			if (p.blueprint().bonus() != null) {
				f += group.race.bvalue(p.blueprint().bonus()) * p.target.group(group);
			}
			am += p.target.group(group);
		}
		f /= am;

		GFORMAT.perc(text, f);
		return text;
	}

	private GuiSection selector() {
		GuiSection butts = new GuiSection();
		int y1 = 0;

		for (HTYPE t : new HTYPE[] {HTYPES.SUBJECT(), HTYPES.SLAVE()}) {

			int x1 = 0;
			for (Race r : RACES.all()) {
				SPRITE s = new SPRITE.Imp(Icon.M) {

					@Override
					public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
						r.appearance().icon.render(rr, X1, X2, Y1, Y2);
						t.CLASS.iconSmall().render(rr, X1+8, X2+8, Y1+4, Y2+4);
					}
				};

				GButt b = new GButt.ButtPanel(s) {

					@Override
					protected void clickA() {
						group = WGROUP.get(t, r);
					}

					@Override
					protected void renAction() {
						selectedSet(group == WGROUP.get(t, r));
					}

					@Override
					protected void render(SPRITE_RENDERER rr, float ds, boolean isActive, boolean isSelected,
										  boolean isHovered) {

						super.render(rr, ds, isActive, isSelected, isHovered);
						if (STATS.WORK().workforce(WGROUP.get(t, r)) <= 0) {
							OPACITY.O50.bind();
							COLOR.BLACK.render(rr, body);
							OPACITY.unbind();
						}
					}

					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.textLL(r.info.names);
						b.NL();
						b.textL(t.names);
						b.NL(4);

						b.textL(Dic.¤¤Employees);
						b.tab(5);
						b.add(employed(WGROUP.get(t, r), b.text()));
						b.NL();
						b.textL(STATS.WORK().WORK_FULFILLMENT.info().name);
						b.tab(5);
						b.add(fullfillment(WGROUP.get(t, r), b.text()));
						b.NL();
						b.textL(Dic.¤¤Skill);
						b.tab(5);
						b.add(skill(WGROUP.get(t, r), b.text()));

						b.NL();


					}

				}.pad(3, 3);


				butts.add(b, x1, y1);
				x1 += b.body.width();
			}
			y1 = butts.getLastY2();

		}
		GButt b = new GButt.ButtPanel(SPRITES.icons().m.arrow_up) {

			@Override
			protected void clickA() {
				group = null;
			}

			@Override
			protected void renAction() {
				selectedSet(group == null);
			}

		};
		b.hoverTitleSet(¤¤MasterPrio).hoverInfoSet(¤¤MasterPrioD);
		b.body.setDim(butts.body().height());
		butts.addRelBody(0, DIR.W, b);

		butts.addRelBody(24, DIR.E, new GButt.ButtPanel(SPRITES.icons().m.repair) {
			@Override
			protected void clickA() {
				if (group == null)
					return;
				for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
					e.setPrioOnSkill(group);
				}
			}

			@Override
			protected void renAction() {
				activeSet(group != null);
			};

		}.hoverInfoSet(D.g("SortW", "Set all priorities based on work skill.")));
		butts.addRightC(2, new GButt.ButtPanel(SPRITES.icons().m.heart) {
			@Override
			protected void clickA() {
				if (group == null)
					return;
				for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
					e.setPrioOnFullfillment(group);
				}
			}

			@Override
			protected void renAction() {
				activeSet(group != null);
			};

		}.hoverInfoSet(D.g("SortF", "Set all priorities based on fulfillment.")));
		butts.addRightC(2, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
			@Override
			protected void clickA() {
				if (group == null) {
					for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
						e.priority.set(e.priority.max()/2);
					}
					return;
				}

				for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
					e.priorities.set(group, e.priorities.max(group)/2);
				}
			}

		}.hoverInfoSet(D.g("clear", "Set all priorities to default.")));

		{
			UIRoomRaceAssign a = new UIRoomRaceAssign();

			GButt.ButtPanel butt = new GButt.ButtPanel(UI.icons().m.descrimination) {
				@Override
				protected void clickA() {
					VIEW.s().tools.place(a);
				}
			};
			butt.hoverTitleSet(a.name());
			butt.hoverInfoSet(a.desc);
			butt.pad(8, 8);

			butts.addRelBody(16, DIR.W, butt);
		}

		return butts;

	}

	private SPRITE underline = new SPRITE.Imp(Icon.M) {

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			COLOR.WHITE100.render(r, X1, X2, Y1, Y2);

		}
	};

	// This is actually a JobRow
	private class WorkButt extends RoomRow {

		private final RoomEmployment p;

		WorkButt(RoomBlueprintIns<?> b) {
			// In the super we set the icon and name of the building
			super(b);
			this.p = b.employmentExtra();

			// This is the render for the actual job rows.
			addRelBody(2, DIR.E, new GStat() {

				@Override
				public void update(GText text) {
					if (group != null) {
						double wf = p.target.group(group);
						if (wf == 0)
							return;
						GFORMAT.perc(text, wf/STATS.WORK().workforce(group));
					}else {
						int n =  p.neededWorkers();
						if (n == 0)
							return;
						// Total worker count
						GFORMAT.iofkInv(text, p.target.get(), n);
					}
				}
			});


			addRelBody(80, DIR.E, new SPRITE.Imp(Icon.S*4, Icon.S) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (group == null)
						return;
					// rendering how much they like the job, based fully on the room
					double d = group.race.pref().getWork(p);
					GGaugeMutable.bad2Good(ColorImp.TMP, d);
					ColorImp.TMP.bind();
					int am = (int) Math.ceil(d*4);
					for (int i = 0; i < am; i++) {
						SPRITES.icons().s.arrowUp.render(r, X1+i*Icon.S/2, Y1);
					}
					COLOR.unbind();
				}
			});

			addRelBody(2, DIR.E, new SPRITE.Imp(Icon.S*4, Icon.S) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (group == null)
						return;
					// rendering how good they are at the job
					double d = RACES.boosts().getNorSkill(group.race, p);
					GGaugeMutable.bad2Good(ColorImp.TMP, d);
					ColorImp.TMP.bind();
					int am = (int) Math.ceil(d*4);
					for (int i = 0; i < am; i++) {
						SPRITES.icons().s.hammer.render(r, X1+i*Icon.S/2, Y1);

					}
					COLOR.unbind();
				}
			});



			INTE in = new INTE() {

				@Override
				public int min() {
					return p.priority.min();
				}

				@Override
				public int max() {
					return p.priority.max();
				}

				@Override
				public int get() {
					if (group != null)
						return p.priorities.get(group);
					return p.priority.get();
				}

				@Override
				public void set(int t) {
					if (group != null)
						p.priorities.set(group, t);
					else
						p.priority.set(t);
				}
			};

			// Priority slider
			GSliderInt t = new GSliderInt(in, 200, true);

			addRightC(8, t);
			body().incrW(6);

		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);

			if (p.priority.get() == 0 || (group != null && p.priorities.get(group) == 0)) {
				OPACITY.O35.bind();
				COLOR.RED100.render(r, body(), -2);
				OPACITY.unbind();
			}else if (p.neededWorkers() == 0){
				OPACITY.O25.bind();
				COLOR.BLACK.render(r, body(), -2);
				OPACITY.unbind();
			}

		}



		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(p.blueprint().info.names);

			b.textLL(¤¤MasterPrio);
			b.tab(4);
			b.add(GFORMAT.i(b.text(), p.priority.get()));
			b.NL();

			b.textLL(Dic.¤¤Employees);
			b.tab(4);
			b.add(GFORMAT.iofkInv(b.text(), p.target.get(), p.neededWorkers()));
			b.NL(4);


			b.tab(1).textL(HCLASSES.CITIZEN().names);
			b.tab(4).textL(HCLASSES.SLAVE().names);
			b.tab(7).textL(STATS.WORK().WORK_FULFILLMENT.info().name);
			b.tab(10).textL(Dic.¤¤Skill);
			b.NL();

			for (Race r :  RACES.all()) {

				if (group != null && r == group.race) {
					b.add(underline);
					b.rewind();
					b.add(r.appearance().icon);
				}else
					b.add(r.appearance().icon);
				b.tab(1);
				double wf = STATS.WORK().workforce(WGROUP.get(HTYPES.SUBJECT(), r));
				b.add(GFORMAT.i(b.text(), p.priorities.get(WGROUP.get(HTYPES.SUBJECT(), r))));
				b.tab(2);
				b.add(GFORMAT.perc(b.text(), wf == 0 ? 0 : p.target.group(WGROUP.get(HTYPES.SUBJECT(), r))/wf));
				b.tab(4);
				wf = STATS.WORK().workforce(WGROUP.get(HTYPES.SLAVE(), r));
				b.add(GFORMAT.i(b.text(), p.priorities.get(WGROUP.get(HTYPES.SLAVE(), r))));
				b.tab(5);
				b.add(GFORMAT.perc(b.text(), wf == 0 ? 0 : p.target.group(WGROUP.get(HTYPES.SLAVE(), r))/wf));
				b.tab(7);
				b.add(GFORMAT.perc(b.text(), r.pref().getWork(p)));
				b.tab(10);
				b.add(GFORMAT.perc(b.text(), RACES.boosts().skill(r, p)));
				b.NL(2);

			}
		}

	}

	private class CatButt extends GButt.BSection{

		int employees;
		int needed;
		int rooms;
		private final GETTER_IMP<RoomCategoryMain> cc;
		private final RoomCategoryMain c;

		CatButt(RoomCategoryMain c, GETTER_IMP<RoomCategoryMain> cc){
			this.cc = cc;
			this.c = c;

			add(c.icon, 0, 0);

			addDownC(0, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, employees);

				}
			}.r(DIR.N));


			addDownC(2, new SPRITE.Imp(40, 10) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (needed > 0)
						GMeter.render(r, GMeter.C_REDGREEN, (double)employees/needed, X1, X2, Y1, Y2);

				}
			});

			pad(6, 8);



			RENDEROBJ sss = new GStat() {

				@Override
				public void update(GText text) {
					int i = 0;
					for (int ri = 0; ri < c.all().size(); ri++) {
						RoomBlueprintImp rb = c.all().get(ri);

						if (rb instanceof RoomBlueprintIns<?>) {

							RoomBlueprintIns<?> rr = (RoomBlueprintIns<?>) rb;
							if (rr.employmentExtra() == null)
								continue;

							if (group == null)
								i += rr.employmentExtra().target.get();
							else
								i += rr.employmentExtra().target.group(group);
						}
					}

					if (group == null)
						GFORMAT.perc(text, (double)i/STATS.WORK().workforce());
					else if (STATS.WORK().workforce(group) > 0)
						GFORMAT.perc(text, (double)i/STATS.WORK().workforce(group));
				}
			}.r(DIR.N);
			sss.body().centerX(body());
			sss.body().moveY1(getLastY2());
			add(sss);

			RENDEROBJ r = new GButt.Glow(SPRITES.icons().s.magnifier) {

				@Override
				protected void clickA() {
					for (RoomBlueprintImp b : c.all()) {
						if (b instanceof RoomBlueprintIns<?>) {
							RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
							if (bb.employmentExtra() != null) {
								if (group == null)
									bb.employmentExtra().priority.inc(1);
								else
									bb.employmentExtra().priorities.inc(group, 1);
							}
						}
					}
				}

			}.hoverInfoSet(¤¤Adjust);

			r.body().moveX1(body().x2()+4);
			r.body().moveCY(body().cY()-16);
			add(r);

			r = new GButt.Glow(SPRITES.icons().s.minifier) {

				@Override
				protected void clickA() {
					for (RoomBlueprintImp b : c.all()) {
						if (b instanceof RoomBlueprintIns<?>) {
							RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
							if (bb.employmentExtra() != null) {
								if (group == null)
									bb.employmentExtra().priority.inc(-1);
								else
									bb.employmentExtra().priorities.inc(group, -1);
							}
						}
					}
				}

			}.hoverInfoSet(¤¤Adjust);

			addDownC(8, r);

			pad(4);
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {

			super.render(r, ds);
			if (rooms == 0) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body());
				OPACITY.unbind();
			}
		}

		@Override
		protected void clickA() {
			cc.set(c);
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(c.name);

			int i = 0;
			for (int ri = 0; ri < c.all().size(); ri++) {
				RoomBlueprintImp rb = c.all().get(ri);

				if (rb instanceof RoomBlueprintIns<?>) {

					RoomBlueprintIns<?> rr = (RoomBlueprintIns<?>) rb;
					if (rr.employmentExtra() == null)
						continue;

					if (group == null)
						i += rr.employmentExtra().target.get();
					else
						i += rr.employmentExtra().target.group(group);
				}
			}




			b.textLL(Dic.¤¤Employees);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), i));

			double p = i;
			if (group == null)
				p/=STATS.WORK().workforce();
			else if (STATS.WORK().workforce(group) > 0)
				p/=STATS.WORK().workforce(group);

			GText t = b.text();
			t.add('(').add((int)(100*p)).add('%').add(')');
		}

		@Override
		protected void renAction() {
			employees = 0;
			needed = 0;
			rooms = 0;

			for (int ri = 0; ri < c.all().size(); ri++) {
				RoomBlueprintImp rb = c.all().get(ri);

				if (rb instanceof RoomBlueprintIns<?>) {
					rooms ++;
					RoomBlueprintIns<?> rr = (RoomBlueprintIns<?>) rb;
					if (rr.employmentExtra() == null)
						continue;

					employees +=  rr.employmentExtra().employed();
					needed += rr.employment().neededWorkers();
				}
			}

			selectedSet(cc.get() == c);
		}

	}

}
