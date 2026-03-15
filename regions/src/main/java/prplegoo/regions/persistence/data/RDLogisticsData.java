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
public class RDLogisticsData {
    public int[][] electedResources;
    public double[][] logisticsCapacity;
    public int[] register;

    public RDLogisticsData(int[][] electedResources, double[][] logisticsCapacity, int[] register) {
        this.electedResources = electedResources;
        this.logisticsCapacity = logisticsCapacity;
        this.register = register;
    }
}
