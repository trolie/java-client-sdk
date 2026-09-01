package energy.trolie.client.model.operatingsnapshots;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link SnapshotHeader} for real-time rating snapshots
 */
@AllArgsConstructor
@SuperBuilder
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RealTimeSnapshotHeader extends SnapshotHeader {


}
