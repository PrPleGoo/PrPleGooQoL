package prplegoo.regions.api.npc;

import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;

public class WeightedBag<T> {
    private int sum = 0;
    private final ArrayListGrower<Integer> weights = new ArrayListGrower<>();
    private final ArrayListGrower<T> items = new ArrayListGrower<>();

    public void Add(int weight, T item) {
        this.sum += weight;
        weights.add(weight);
        items.add(item);
    }

    public T Pick() {
        int random = RND.rInt(sum + 1);
        for (int i = 0; i < weights.size(); i++) {
            random -= weights.get(i);
            if (random <= 0) {
                return items.get(i);
            }
        }

        return null;
    }
}
