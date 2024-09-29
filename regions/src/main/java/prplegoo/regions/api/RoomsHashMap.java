package prplegoo.regions.api;

import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;

public class RoomsHashMap {
    public static INDUSTRY_HASER GetRoom(String key){
        switch(key){
            case "WOODCUTTER":
                return SETT.ROOMS().WOOD_CUTTER;
        }
        return null;
    }
}
