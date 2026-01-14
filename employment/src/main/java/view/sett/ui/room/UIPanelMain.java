package view.sett.ui.room;

import static settlement.main.SETT.ROOMS;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.WGROUP;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategories;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.employment.RoomEmployment;
import settlement.room.main.employment.RoomEquip;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.statistics.HISTORY_INT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class UIPanelMain extends ISidePanel {

	final UIPanelWorkPrio work = new UIPanelWorkPrio();
	private final UIPanelWorkTools[] tools = new UIPanelWorkTools[SETT.ROOMS().employment.equip.ALL.size()];

	private static CharSequence ¤¤emp = "Employed";
	private static CharSequence ¤¤oddjobbers = "Oddjobbers";
	private static CharSequence ¤¤title = "workforce & rooms";

	static {
		D.ts(UIPanelMain.class);
	}

	public UIPanelMain(UIRoom[] rooms) {

		section.body().setWidth(C.SG * 270);

		titleSet(¤¤title);

		{
			GuiSection s = new GuiSection();

			GuiSection pop = new Emp();

			RENDEROBJ ss = new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text, ROOMS().employment.TARGET.get(null), ROOMS().employment.NEEDED.get());
				}
			}.hh(UI.icons().s.hammer);

			GButt.ButtPanel b = new GButt.ButtPanel(new SPRITE.Imp(200, 24) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					ss.body().moveX1Y1(X1, Y1+4);
					ss.render(r, 0);
				}


			}){
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(pop, this);
				}
			};

			s.add(b);


			s.addRightC(0, new GButt.ButtPanel(Dic.¤¤Priority) {
				@Override
				protected void clickA() {
					last().add(work, false);
				}
			}.icon(SPRITES.icons().m.arrow_up));

			section.addRelBody(0, DIR.S, s);
		}



		{
			GuiSection equip = new GuiSection();

			int k = 0;

			// This is the tool equip at the top level
			for (RoomEquip w : SETT.ROOMS().employment.equip.ALL) {
				tools[w.index()] = new UIPanelWorkTools(w);
				RENDEROBJ o = new GButt.ButtPanel(new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.i(text, w.currentTotal());
					}



				}) {
					@Override
					protected void clickA() {
						last().add(tools[w.index()], false);
					}


					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.add(w.info);
						b.NL();
						b.textLL(Dic.¤¤Equipped);
						b.tab(6);
						b.add(GFORMAT.iofkInv(b.text(), w.currentTotal(), w.neededTotal()));

						b.NL();
						b.add(b.text().add(Dic.¤¤Consumed).s().add('(').add(DicTime.¤¤Day).add(')'));
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), -w.currentTotal()*w.degradePerDay));

						b.sep();
						w.boosts.hover(text, 1.0, -1);

					}

					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
										  boolean isHovered) {
						// TODO Auto-generated method stub
						super.render(r, ds, isActive, isSelected, isHovered);
						double d = (double)w.currentTotal()/w.neededTotal();
						Rec.TEMP.setDim(body.width()-8, body.height()-8);
						Rec.TEMP.centerIn(body);

						if (w.neededTotal() > 0)
							GMeter.render(r, GMeter.C_GRAY, d, Rec.TEMP);

						super.render(r, ds, isActive, isSelected, isHovered);
					}

				} .setDim(124, 24+8).icon(w.resource.icon());

				equip.add(o, (k%2)*140, (k/2)*32);
				k++;
			}



			section.addRelBody(8, DIR.S, equip);
		}



		{

			GuiSection filter = new GuiSection();

			{
				SPRITE oo = new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.i(text, SETT.ROOMS().construction.instances());
					}
				}.hv(UI.icons().s.hammer.createColored(GCOLOR.UI().GREAT.hovered)).asSprite();
				ConstructionList li = new ConstructionList();
				filter.addRightC(0, new GButt.ButtPanel(oo) {
					@Override
					protected void clickA() {
						VIEW.s().panels.add(li, false);
					};
					@Override
					protected void renAction() {
						selectedSet(VIEW.s().panels.added(li));
					};

				}.setDim(48, 64));


			}

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
			unused.body().setHeight(filter.body().height()-16);
			filter.addRightC(2, unused);

			section.addRelBody(8, DIR.S, filter);

			ArrayListGrower<RENDEROBJ> rows = new ArrayListGrower<>();
			ArrayListGrower<RoomBlueprintIns<?>> rs = new ArrayListGrower<>();

			for (RoomBlueprintIns<?> b : SETT.ROOMS().ins()) {
				if (rooms[b.index()].clicker != null) {
					rows.add(rooms[b.index()].clicker);
					rs.add(b);
				}
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

	public void open(RoomEquip w) {
		VIEW.s().panels.add(tools[w.index()], true);
	}

	private static class CatButt extends GButt.BSection{

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


			addDownC(0, new SPRITE.Imp(40, 10) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

					if (needed > 0)
						GMeter.render(r, GMeter.C_REDGREEN, (double)employees/needed, X1, X2, Y1, Y2);

				}
			});
			pad(6, 8);
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
			b.textLL(Dic.¤¤Employees);
			b.tab(6);
			b.add(GFORMAT.iofkInv(b.text(), employees, needed));
			b.NL();
			b.textLL(Dic.¤¤Amount);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), rooms));
			b.NL();
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

					employees += rr.employmentExtra().employed(null);
					needed += rr.employment().neededWorkers();
				}
			}

			selectedSet(cc.get() == c);
		}

	}

	private static class Emp extends GuiSection{

		private final ArrayList<WGROUP> ll = new ArrayList<WGROUP>(WGROUP.all());

		Emp(){

			addRightC(0, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iBig(text, ROOMS().employment.NEEDED.get());

				}
			}.hh(Dic.¤¤Needed));

			addRightC(100, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iBig(text, ROOMS().employment.TARGET.get(null));

				}
			}.hh(Dic.¤¤Target));

			{
				HISTORY_INT em = SETT.ROOMS().employment.hEmployed();
				GStaples chart = new GStaples(em.historyRecords()) {

					@Override
					protected void hover(GBox box, int stapleI) {

						box.title(STATS.WORK().EMPLOYED.stat().info().name);

						int ii = em.historyRecords()-stapleI - 1;
						GText t = box.text();
						DicTime.setDaysAgo(t, ii);
						t.adjustWidth();
						box.add(t.lablify());
						box.NL();
						box.add(GFORMAT.i(box.text(), em.get(ii)));
						box.NL(8);


						if (stapleI > 0) {
							for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
								int now = e.history().get(ii);
								int delta = now -  e.history().get(ii+1);
								if (delta != 0) {
									box.add(e.blueprint().iconBig().small);
									box.textLL(e.blueprint().info.names);
									box.tab(7);
									box.add(GFORMAT.iIncr(box.text(), delta));
									box.NL();
								}
							}
						}

					}

					@Override
					protected double getValue(int stapleI) {
						return em.get(em.historyRecords()-stapleI - 1);
					}
				};
				chart.normalize(true);

				chart.body().setWidth(410).setHeight(80);

				addRelBody(8, DIR.S, chart);
			}


			GTableBuilder bb = new GTableBuilder() {

				@Override
				public int nrOFEntries() {
					return ll.size() + 1;
				}
			};

			bb.column("", 48, new GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new HOVERABLE.HoverableAbs(Icon.L) {

						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							int i = ier.get();
							if (i == ll.size()) {
								UI.icons().m.arrow_right.renderC(r, body.cX(), body.cY());
							}else {
								ll.get(i).race.appearance().icon.renderC(r, body.cX(), body.cY());
								ll.get(i).type.CLASS.iconSmall().renderC(r, body.cX()+8, body.cY()+4);
							}
						}

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;
							int i = ier.get();
							if (i == ll.size()) {
								b.title(Dic.¤¤Total);
							}else {
								Str.TMP.clear().add(ll.get(i).race.info.names);
								Str.TMP.s().add('(').add(ll.get(i).type.CLASS.names).add(')');
								b.title(Str.TMP);
							}
						}
					};


				}
			});

			int s = 120;

			bb.column(Dic.¤¤Workforce, s, new GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.i(text, STATS.WORK().workforce(ll.get(ier.get())));

						}
					}.r(DIR.NW);


				}
			});

			bb.column(STATS.WORK().incap.stat.info().name, s, new GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							int i = ier.get();
							if (i != ll.size()) {
								GFORMAT.i(text, -STATS.WORK().incap.get(ll.get(ier.get()).type, ll.get(ier.get()).race));
							}else {
								GFORMAT.i(text, -STATS.WORK().incap.get());
							}

						}
					}.r(DIR.NW);


				}
			});

			bb.column(¤¤emp, s, new GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							int i = ier.get();
							if (i != ll.size()) {
								GFORMAT.i(text, SETT.ROOMS().employment.TARGET.get(ll.get(ier.get())));
							}else {
								GFORMAT.i(text, SETT.ROOMS().employment.TARGET.get(ll.get(0)));
							}

						}
					}.r(DIR.NW);


				}
			});

			bb.column(Dic.¤¤Rate, s, new GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							double wf = STATS.WORK().workforce(ll.get(ier.get()));
							GFORMAT.perc(text, SETT.ROOMS().employment.TARGET.get(ll.get(ier.get()))/wf);

						}
					}.r(DIR.NW);


				}
			});


			bb.column(¤¤oddjobbers, s, new GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							int i = ier.get();
							if (i != ll.size()) {
								GFORMAT.i(text, STATS.WORK().workforce(ll.get(ier.get())) - SETT.ROOMS().employment.TARGET.get(ll.get(ier.get())));
							}else {
								GFORMAT.i(text, STATS.WORK().workforce() - SETT.ROOMS().employment.TARGET.get(null));
							}

						}
					}.r(DIR.NW);


				}
			});

			addRelBody(8, DIR.S, bb.create(8, true));



		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			ll.clearSloppy();

			for (WGROUP g : WGROUP.all()) {
				if (STATS.POP().POP.data(g.type.CLASS).get(g.race) > 0) {
					ll.add(g);
				}
			}
			super.render(r, ds);
		}


	}

}
