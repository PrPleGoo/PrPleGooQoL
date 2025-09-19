package prplegoo.regions.api.npc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class StockpileSmoothingData {
    public double[][] currentTarget;

    public StockpileSmoothingData(double[][] currentTarget) {
        this.currentTarget = currentTarget;
    }
}

