package prplegoo.regions.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("InnerClassMayBeStatic")
public class RDDeficitData {
    public int[] deficits;
    public int[] unresolvedDeficits;
    public double since;

    public RDDeficitData(int[] deficits, int[] unresolvedDeficits, double since){
        this.deficits = deficits;
        this.unresolvedDeficits = unresolvedDeficits;
        this.since = since;
    }
}
