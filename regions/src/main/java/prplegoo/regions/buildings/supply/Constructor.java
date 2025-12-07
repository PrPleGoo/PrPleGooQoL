package prplegoo.regions.buildings.supply;

import game.GAME;
import init.constant.C;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.room.main.TmpArea;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.furnisher.FurnisherItemTile;
import settlement.room.main.furnisher.FurnisherStat;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSprite1x1;
import settlement.room.sprite.RoomSpriteCombo;
import settlement.tilemap.floor.Floors;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

import java.io.IOException;

final class Constructor extends Furnisher{

	
	private final Floors.Floor floor2;
	
	final FurnisherStat workers = new FurnisherStat.FurnisherStatI(this) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};

	final FurnisherStat storage = new FurnisherStat(this, 0) {

		@Override
		public double get(AREA area, double fromItems) {
			return fromItems* ROOM_LOGISTICS.STORAGE;
		}

		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};

	protected Constructor(ROOM_LOGISTICS b, RoomInitData init)
			throws IOException {
		super(init, 1, 2, 88, 44);
	
		floor2 = SETT.FLOOR().map.read("FLOOR2", init.data());
		
		Json sp = init.data().json("SPRITES");
		
		RoomSprite sFence = new RoomSpriteCombo(sp, "FENCE_COMBO");
		
		RoomSprite sStone = new RoomSprite1x1(sp, "TORCH_1X1");
		Crate crate = new Crate(b);
		RoomSprite sCrate = new RoomSprite.Imp() {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it,
					double degrade, boolean isCandle) {
				
				if (crate.get(it.tx(), it.ty()) != null && !crate.away()) {
					SETT.HALFENTS().transports.sprite.renderBelow(r, s, data*2, it.x()+C.TILE_SIZEH, it.y()+C.TILE_SIZEH, 0, it.ran(), degrade, crate.realResource(), crate.resAmount());
				}
				return false;
			}

			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (crate.get(it.tx(), it.ty()) != null && !crate.away()) {
					SETT.HALFENTS().transports.sprite.render(r, s, data*2, it.x()+C.TILE_SIZEH, it.y()+C.TILE_SIZEH, degrade, true);
				}
			}
			
			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return (byte) item.rotation;
			}
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				SPRITES.cons().ICO.arrows.get(data).render(r, x, y);
			}
			
		};
		
		RoomSprite sAnimal = new RoomSprite.Imp() {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				DIR d = DIR.ORTHO.get(data).perpendicular();
				int tx = it.tx()+d.x();
				int ty = it.ty()+d.y();
				if (crate.get(tx, ty) != null && !crate.away() && crate.animalHas()) {
					double mov = (GAME.intervals().get05()+it.ran()) & 0x0FF;
					mov /= 0x0FF;
					SETT.ANIMALS().renderCaravan(r, s, mov, it.x()+C.TILE_SIZEH, it.y()+C.TILE_SIZEH, null, 0, false, data*2, it.ran());
				}
				
				return false;
			}
			
			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return (byte) item.rotation;
			}
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				SPRITES.cons().ICO.arrows.get(data).render(r, x, y);
			}
		};
		
		RoomSprite smarker = new RoomSprite1x1(sp, "WORKTABLE_1X1") {
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				OPACITY.O100.bind();
				super.render(r, s, data, it, degrade, false);
				OPACITY.unbind();
			};
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) == this;
			}
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				SPRITES.cons().ICO.arrows.get(data).render(r, x, y);
			}
			
		};
		
		FurnisherItemTile ff = new FurnisherItemTile(this, false, sFence, AVAILABILITY.ROOM_SOLID, false);
		FurnisherItemTile ss = new FurnisherItemTile(this, false, sStone, AVAILABILITY.ROOM_SOLID, true);
		FurnisherItemTile cc = new FurnisherItemTile(this, false, sCrate, AVAILABILITY.ROOM_SOLID, false).setData(1);
		FurnisherItemTile aa = new FurnisherItemTile(this, false, sAnimal, AVAILABILITY.ROOM_SOLID, false).setData(2);
		FurnisherItemTile oo = new FurnisherItemTile(this, true, smarker, AVAILABILITY.ROOM, false).setData(3);
		FurnisherItemTile __ = new FurnisherItemTile(this, false, null, AVAILABILITY.ROOM, false);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,oo,__,oo,__,oo,ss},
			{ff,aa,__,aa,__,aa,ff},
			{ff,cc,__,cc,__,cc,ff},
			{ff,ff,ff,ff,ff,ff,ff},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,oo,__,oo,__,oo,__,oo,ss},
			{ff,aa,__,aa,__,aa,__,aa,ff},
			{ff,cc,__,cc,__,cc,__,cc,ff},
			{ff,ff,ff,ff,ff,ff,ff,ff,ff},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,oo,__,oo,__,oo,__,oo,__,oo,ss},
			{ff,aa,__,aa,__,aa,__,aa,__,aa,ff},
			{ff,cc,__,cc,__,cc,__,cc,__,cc,ff},
			{ff,ff,ff,ff,ff,ff,ff,ff,ff,ff,ff},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,oo,__,oo,__,oo,__,oo,__,oo,ss},
			{ff,aa,__,aa,__,aa,__,aa,__,aa,ff},
			{ff,cc,__,cc,__,cc,__,cc,__,cc,ff},
			{ff,oo,__,oo,__,oo,__,oo,__,oo,ff},
			{ff,aa,__,aa,__,aa,__,aa,__,aa,ff},
			{ff,cc,__,cc,__,cc,__,cc,__,cc,ff},
			{ff,ff,ff,ff,ff,ff,ff,ff,ff,ff,ff},
		}, 10);
		
		
		flush(3);
		
	}

	@Override
	public boolean usesArea() {
		return false;
	}
	
	@Override
	public boolean mustBeIndoors() {
		return false;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return false;
	}

	@Override
	public void putFloor(int tx, int ty, int upgrade,  AREA area) {
		if (SETT.ROOMS().fData.tileData.get(tx, ty) != 0)
			super.putFloor(tx, ty, upgrade,  area);
		else
			floor2.placeFixed(tx, ty);
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return  new SupplyInstance(blue(), area, init);
	}

	@Override
	public ROOM_LOGISTICS blue() {
		return SETT.ROOMS().LOGISTICS;
	}

}
