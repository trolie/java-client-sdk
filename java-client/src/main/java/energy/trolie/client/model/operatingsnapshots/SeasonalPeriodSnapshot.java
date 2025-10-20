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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.RatingValue;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * For a given resource, represents a rating value set for a given forecast period.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class SeasonalPeriodSnapshot {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("period-start")
    private Instant periodStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("period-end")
    private Instant periodEnd;

    @JsonProperty("season-name")
    private String seasonName;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;
}
