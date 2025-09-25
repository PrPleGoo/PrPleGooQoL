package prplegoo.regions.api.npc.buildinglogic;

import game.faction.npc.FactionNPC;
import lombok.Getter;
import prplegoo.regions.api.npc.buildinglogic.strategy.MutationStrategy;
import world.map.regions.Region;

import java.util.Arrays;
import java.util.stream.IntStream;

public class FactionGeneticMutator extends FactionGenetic {
    protected final MutationStrategy mutationStrategy;

    public FactionGeneticMutator(FactionNPC faction, MutationStrategy mutationStrategy) {
        super(faction);

        this.mutationStrategy = mutationStrategy;
    }

    public FactionGeneticMutator(FactionNPC faction, Region region, MutationStrategy mutationStrategy) {
        super(faction, region);

        this.mutationStrategy = mutationStrategy;
    }

    public boolean tryMutate() {
        return mutationStrategy.tryMutate(this);
    }

    @Override
    public void loadFitness(FactionGenetic faction) {
        fitnessRecords = mutationStrategy.loadFitness(faction);
    }
}

