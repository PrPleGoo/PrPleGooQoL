package prplegoo.regions.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import snake2d.util.sets.Tuple;

import java.util.HashMap;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class RDOptionalConsumptionData {
    public boolean[][][] enabled;

    public RDOptionalConsumptionData(boolean[][][] enabled) {
        this.enabled = enabled;
    }
}


