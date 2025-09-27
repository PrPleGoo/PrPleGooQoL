package world.overlay;

import game.boosting.BUtil;
import init.C;
import init.text.D;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

class OverlayMineral extends WorldOverlays.OverlayTileNormal{

	private static CharSequence ¤¤name = "¤Minerals";
	private static CharSequence ¤¤desc = "¤Shows the location of minerals.";
	static {
		D.ts(OverlayMineral.class);
	}
	
	
	OverlayMineral() {
		super(¤¤name, ¤¤desc, true, true);
	}
	
	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		super.renderAbove(r, s, data);
		
		int size = C.TILE_SIZE;
		
		for (Region reg : WORLD.REGIONS().active()) {
			
			for (DIR dir : DIR.NORTHO) {
				if (data.tBounds().holdsPoint(reg.cx()+dir.x()*5, reg.cy()+dir.y()*5)) {
					
					int am = 0;
					
					for (RDBuilding b : RD.BUILDINGS().all) {
						if (BUtil.value(b.baseFactors, reg) > 1) {
							am++;
						}
						
					}

					int x1 = data.transformGX((reg.cx()*C.TILE_SIZE+C.TILE_SIZEH)-am*size/2);
					int y1 = data.transformGY((reg.cy()+2)*C.TILE_SIZE);
					
					
					
					for (RDBuilding b : RD.BUILDINGS().all) {
						if (BUtil.value(b.baseFactors, reg) > 1) {
							int ss = (int) (C.TILE_SIZE);
							int d = (size-ss)/2;
							COLOR.BLACK.bind();
							b.icon().render(r, x1+d+8, x1+d+ss+8, y1+d+8, y1+d+ss+8);
							COLOR.unbind();
							b.icon().render(r, x1+d, x1+d+ss, y1+d, y1+d+ss);
							
							x1+=size;
						}
					}
					
					break;
				}
			}
			
			
			
		}
		
	}
	
	@Override
	public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		
//		Minable m = WORLD.MINERALS().get(it.tile());
//		if (m != null) {
//			
//			render(r, COLOR.WHITE100, it.x(), it.y(), 8);
//			render(r, COLOR.WHITE10, it.x()+2, it.y()+2, 6);
//			render(r, COLOR.WHITE30, it.x(), it.y(), 4);
//			m.resource.icon().render(r, it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE);
//		}
//		
	}

}
