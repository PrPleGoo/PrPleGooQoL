package world.entity.caravan;

import java.io.IOException;

import game.faction.trade.ITYPE;
import init.paths.PATHS;
import init.sprite.UI.Icon;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import view.main.VIEW;
import world.WORLD;
import world.entity.WEntityConstructor;
import world.map.regions.Region;

public final class Shipments extends WEntityConstructor<Shipment> {

    final Stack<Shipment> free = new Stack<>(1024);
    public final SPRITE icon;

    final TILE_SHEET caravan = (new ITileSheet(PATHS.SPRITE().getFolder("world").getFolder("entity").get("Tribute"), 100, 224) {
        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {

            s.singles.init(0, 0, 1, 12, 2, 1, d.s16);
            for (int i = 0; i < 12; i++) {
                s.singles.setVar(i);
                s.singles.paste(3, true);
            }

            return d.s16.saveGame();

        }
    }).get();

    public Shipments(LISTE<WEntityConstructor<?>> tot) throws IOException{
        super(tot, true);
        icon = new SPRITE.Imp(Icon.L, Icon.L) {

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                int i = (int) (VIEW.renderSecond()*2)%3;
                i*= 8;
                i+= 8*4;
                COLOR.WHITE150.bind();
                WORLD.ENTITIES().caravans.caravan.render(r, i+DIR.SE.id(), X1, X2, Y1, Y2);
                COLOR.unbind();

            }
        };
    }

    public Shipment create(Region start, Region dest, ITYPE type) {
        COORDINATE cc = WORLD.PATH().rnd(start);
        if (cc != null) {
            return create(cc.x(), cc.y(), dest, type);
        }
        return null;
    }

    public Shipment createActualDest(Region start, Region dest, ITYPE type) {
        COORDINATE cc = WORLD.PATH().rnd(start);
        if (cc != null) {
            Shipment c = create();
            c.add(cc.x(), cc.y(), dest, type);
            if (c.added())
                return c;

            free.push(c);
        }
        return null;
    }

    public Shipment create(int sx, int sy, Region dest, ITYPE type) {
        Shipment c = create();
        c.add(sx, sy, dest.faction(), type);
        if (c.added())
            return c;


        free.push(c);
        return null;
    }

    @Override
    protected Shipment create() {
        if (!free.isEmpty()) {
            return free.pop();
        }
        return new Shipment();
    }

    @Override
    protected void clear() {
        // TODO Auto-generated method stub

    }

}
