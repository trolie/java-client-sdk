/*
 *  ===========================================================================
 *
 *  Copyright and Proprietary Information
 *
 *  Copyright (c) 1993, 2024 General Electric Technology GmbH. All Rights Reserved.
 *
 *  NOTE: CONTAINS CONFIDENTIAL AND PROPRIETARY INFORMATION OF GENERAL ELECTRIC
 *  TECHNOLOGY GMBH AND MAY NOT BE REPRODUCED, TRANSMITTED, STORED, OR COPIED IN
 *  WHOLE OR IN PART, OR USED TO FURNISH INFORMATION TO OTHERS, WITHOUT THE PRIOR
 *  WRITTEN PERMISSION OF GENERAL ELECTRIC TECHNOLOGY GMBH OR GRID SOLUTIONS.
 *
 *  ==========================================================================
 */

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
