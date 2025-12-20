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
    public int[] newDeficits;
    public int[] deficits;
    public int[] oldDeficits;
    public int[] supplies;
    public int[] unresolvedDeficits;
    public double since;

    public RDDeficitData(int[] newDeficits, int[] deficits, int[] oldDeficits, int[] supplies, int[] unresolvedDeficits, double since){
        this.newDeficits = newDeficits;
        this.deficits = deficits;
        this.oldDeficits = oldDeficits;
        this.supplies = supplies;
        this.unresolvedDeficits = unresolvedDeficits;
        this.since = since;
    }
}
