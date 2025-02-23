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

package org.trolie.client.model.operatingsnapshots;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.trolie.client.model.common.EmergencyRatingValue;
import org.trolie.client.model.common.RatingValue;

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
public class ForecastPeriodSnapshot {

    @JsonProperty("period-start")
    private Instant periodStart;

    @JsonProperty("period-end")
    private Instant periodEnd;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    // Getters for each rating type.

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;
}
