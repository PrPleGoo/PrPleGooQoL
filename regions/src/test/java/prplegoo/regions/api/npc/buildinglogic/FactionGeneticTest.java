package prplegoo.regions.api.npc.buildinglogic;

import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.UpdaterNPC;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import snake2d.util.sets.ArrayList;

import static game.faction.FACTIONS.MAX;

/**
 * I guess you can do some basic unit testing of your code when you mock the required game dependencies.
 * But running a test against the game itself might be tricky.
 */
public class FactionGeneticTest {
    @Test
    void FactionGenetic_loadFitness_Should_Load_fitnessRecords() {
        ArrayList<Faction> all = new ArrayList<>(MAX());
        UpdaterNPC ncpUpdater = new UpdaterNPC();
        FactionNPC factionNPC = new FactionNPC(all, ncpUpdater);
        FactionGenetic factionGenetic = new FactionGenetic(factionNPC);

        Assertions.assertThat(factionGenetic.getFitnessRecords()).isNull();

        factionGenetic.loadFitness();
        Assertions.assertThat(factionGenetic.getFitnessRecords()).isNotNull();
    }

    @Test
    void FactionGeneticMutator_loadFitness_Should_Load_fitnessRecords() {
        ArrayList<Faction> all = new ArrayList<>(MAX());
        UpdaterNPC ncpUpdater = new UpdaterNPC();
        FactionNPC factionNPC = new FactionNPC(all, ncpUpdater);
        FactionGeneticMutator factionGenetic = new FactionGeneticMutator(factionNPC, null);

        Assertions.assertThat(factionGenetic.getFitnessRecords()).isNull();

        factionGenetic.loadFitness();
        Assertions.assertThat(factionGenetic.getFitnessRecords()).isNotNull();
    }

    /*
    @Test
    void shouldFail(){
        Assertions.fail("failure message.");
    }

import org.mockito.Mockito;
    @Test
    void iDoNothing() {
        // there's Assertj for assertions
        Assertions.assertThat("text").isEqualTo("text");

        // and there's Mockito for mocking
        IAmUseless iAmUselessMock = Mockito.mock(IAmUseless.class);
        Mockito.when(iAmUselessMock.getOhLookZero()).thenReturn(0);
        int zeroMock = iAmUselessMock.getOhLookZero();
        Assertions.assertThat(zeroMock).isEqualTo(0);

        // lombok is there too for auto generated getter, setter, constructor, ...
        IAmUseless iAmUseless = new IAmUseless();
        int zero = iAmUseless.getOhLookZero();
        Assertions.assertThat(zero).isEqualTo(0);
    }

import lombok.Data;
    @Data
    static class IAmUseless {
        private final int ohLookZero = 0;
    }
    */
}