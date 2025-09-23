package settlement.army.ai.util;

import java.util.Arrays;

import init.config.Config;
import settlement.army.Army;
import settlement.army.ArmyManager;
import settlement.army.div.Div;
import settlement.army.formation.DivPositionAbs;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_OBJECT_ISSER;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LISTE;

public final class DivsTileMap {

	private final Tile[] tiles = new Tile[0x0FFFF];
	private int tileNewI = 1;
	private final short[] firstTiles = new short[SETT.TAREA];
	private final Bitsmap1D armies = new Bitsmap1D(0, 2, SETT.TAREA);
	private final DivStatus[] statuses;
	private static IntChecker checker = new IntChecker(Config.BATTLE.DIVISIONS_PER_ARMY*2);

	private final ArrayList<Div> tmp = new ArrayList<>(16);

	public DivsTileMap(DivStatus[] statuses) {
		this.statuses = statuses;
		for (int i = 1; i < tiles.length; i++) {
			tiles[i] = new Tile();
		}
	}

	void add() {

	}

	void add(short div, DivPositionAbs next) {

		checker.init();
		for (int i = 0; i < next.deployed(); i++) {
			int x = next.tile(i).x();
			int y = next.tile(i).y();
			int t = x + y*SETT.TWIDTH;
			add(t, div);
		}
	}

	private void add(int tileI, short currentI) {


		Tile first = tiles[firstTiles[tileI]&0x0FFFF];
		Div current = SETT.ARMIES().division(currentI);

		int aa = armies.get(tileI);
		aa |= current.army().bit;
		armies.set(tileI, aa);

		if (first == null) {
			firstTiles[tileI] = makeNewTile((short)0, current);
			tmp.clear();

			return;
		}

		{
			Tile t = first;
			while(t != null) {
				if (t.divI == currentI)
					return;
				t = tiles[t.next&0x0FFFF];
			}
		}

		Tile t = first;

		DivStatus currentOrder = statuses[currentI];

		while(t != null) {

			Div other = SETT.ARMIES().division(t.divI);
			if (!checker.isSetAndSet(t.divI)) {
				if (other.army() == current.army()) {
					currentOrder.friendlyCollisionSet(t.divI);
					statuses[t.divI].friendlyCollisionSet(currentI);
				}else {
					currentOrder.enemyCollisionSet(t.divI);
					statuses[t.divI].enemyCollisionSet(currentI);
				}
			}
			t = tiles[t.next&0x0FFFF];
		}

		firstTiles[tileI] = makeNewTile(firstTiles[tileI], current);



	}

	private short makeNewTile(short next, Div div) {
		short i = (short) tileNewI;
		Tile t = tiles[tileNewI];
		t.next = next;
		t.divI = div.index();
		tileNewI ++;
		return i;
	}

	public Iterable<Div> get(LISTE<Div> res, int tx, int ty){
		return get(res, tx, ty, ArmyManager.ARMIES_BITS);
	}

	public Iterable<Div> get(LISTE<Div> res, int tx, int ty, DIR d){
		tx+= d.x();
		ty += d.y();
		return get(res, tx, ty);
	}

	public Iterable<Div> getAlly(LISTE<Div> res, int tx, int ty, Army a){
		return get(res, tx, ty, a.bit);
	}

	public Iterable<Div> getEnemy(LISTE<Div> res, int tx, int ty, Army a){
		return get(res, tx, ty, ~a.bit);
	}

	public Div get(int tx, int ty, Army a) {
		return get(tx + ty*SETT.TWIDTH, a.bit);
	}

	public MAP_OBJECT_ISSER<Army> hasEnemy = new MAP_OBJECT_ISSER<Army>() {

		@Override
		public boolean is(int tile, Army value) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH, value);
		}

		@Override
		public boolean is(int tx, int ty, Army value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return (armies.get(tile) & ~value.bit) != 0;
		}

	};

	public MAP_OBJECT_ISSER<Army> hasAlly = new MAP_OBJECT_ISSER<Army>() {

		@Override
		public boolean is(int tile, Army value) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH, value);
		}

		@Override
		public boolean is(int tx, int ty, Army value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return (armies.get(tile) & value.bit) != 0;
		}

	};

	public MAP_OBJECT_ISSER<Div> hasOtherAlly = new MAP_OBJECT_ISSER<Div>() {

		@Override
		public boolean is(int tileI, Div value) {
			Tile t = tiles[firstTiles[tileI]&0x0FFFF];
			while(t != null) {
				Div d = SETT.ARMIES().division(t.divI);
				if (d != value && d.army() == value.army())
					return true;
				t = tiles[t.next&0x0FFFF];
			}
			return false;
		}

		@Override
		public boolean is(int tx, int ty, Div value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return is(tile, value);
		}

	};

	public MAP_OBJECT_ISSER<Div> isser = new MAP_OBJECT_ISSER<Div>() {

		@Override
		public boolean is(int tileI, Div value) {
			Tile t = tiles[firstTiles[tileI]&0x0FFFF];
			while(t != null) {
				Div d = SETT.ARMIES().division(t.divI);
				if (d == value)
					return true;
				t = tiles[t.next&0x0FFFF];
			}
			return false;
		}

		@Override
		public boolean is(int tx, int ty, Div value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			int tile = tx + ty*SETT.TWIDTH;
			return is(tile, value);
		}

	};

	private Iterable<Div> get(LISTE<Div> res, int tx, int ty, int aMask){
		if (!SETT.IN_BOUNDS(tx, ty))
			return res;
		int tileI = tx + ty*SETT.TWIDTH;
		return getMask(res, tileI, aMask);
	}

	public Iterable<Div> get(LISTE<Div> res, int tileI){
		return getMask(res, tileI, ArmyManager.ARMIES_BITS);
	}

	private Iterable<Div> getMask(LISTE<Div> res, int tileI, int aMask){
		Tile t = tiles[firstTiles[tileI]&0x0FFFF];
		while(t != null && res.hasRoom()) {
			Div d = SETT.ARMIES().division(t.divI);
			if ((d.army().bit & aMask) != 0)
				res.add(d);
			t = tiles[t.next&0x0FFFF];
		}
		return res;
	}

	private Div get(int tileI, int aMask){
		Tile t = tiles[firstTiles[tileI]&0x0FFFF];
		while(t != null) {
			Div d = SETT.ARMIES().division(t.divI);
			if ((d.army().bit & aMask) != 0)
				return d;
			t = tiles[t.next&0x0FFFF];
		}
		return null;
	}

	void clear() {
		Arrays.fill(firstTiles, (short)0);
		armies.clear();
		tileNewI = 1;
	}


	private static final class Tile {

		private short next;
		private short divI;

	}



}
