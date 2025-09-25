package prplegoo.regions.api.npc.buildinglogic;

import game.faction.npc.FactionNPC;
import lombok.Getter;
import prplegoo.regions.api.npc.buildinglogic.strategy.MutationStrategy;
import world.map.regions.Region;

import java.util.Arrays;
import java.util.stream.IntStream;

public class RegionGeneticMutator extends FactionGeneticMutator {
    public RegionGeneticMutator(FactionNPC faction, Region region, MutationStrategy mutationStrategy) {
        super(faction, region, mutationStrategy);
    }

    @Override
    public boolean tryMutate() {
        return mutationStrategy.tryMutateRegion(getRegionGenetics()[0]);
    }
}
