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

package energy.trolie.client.model.ratingproposals;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.InputValue;
import energy.trolie.client.model.common.RatingValue;

import java.util.List;

/**
 * Real-time rating value set for a given resource
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class RealTimeRating {

    @JsonProperty("resource-id")
    private String resourceId;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;

    @JsonProperty("inputs-used")
    private List<InputValue> inputsUsed;
}
