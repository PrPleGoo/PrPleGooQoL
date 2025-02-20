package prplegoo.regions.api.npc.buildinglogic;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.strategy.MutationStrategy;

public class FactionGeneticMutator extends FactionGenetic {
    private final MutationStrategy mutationStrategy;

    public FactionGeneticMutator(FactionNPC faction, MutationStrategy mutationStrategy) {
        super(faction);
        
        this.mutationStrategy = mutationStrategy;
    }

    public boolean tryMutate() {
        return mutationStrategy.tryMutate(this);
    }

    @Override
    public FactionGenetic loadFitness(FactionNPC faction) {
        fitnessRecords = mutationStrategy.loadFitness(faction);

        return this;
    }
}
