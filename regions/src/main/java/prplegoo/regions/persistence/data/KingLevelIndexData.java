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
public class KingLevelIndexData {
    public byte[] npcLevels;
    public int[] nextPickYear;

    public KingLevelIndexData(byte[] npcLevels, int[] nextPickYear) {
        this.npcLevels = npcLevels;
        this.nextPickYear = nextPickYear;
    }
}
