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
public class ShipperData {
    public double[] since;
    public double[][] resources;
    public int[][] slaves;

    public ShipperData(double[] since, double[][] resources, int[][] slaves) {
        this.since = since;
        this.resources = resources;
        this.slaves = slaves;
    }
}
