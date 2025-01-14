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

package org.trolie.client.model.ratingproposals;

import java.util.List;

import org.trolie.client.model.common.DataProvenance;
import org.trolie.client.model.common.EmergencyDuration;
import org.trolie.client.model.common.PowerSystemResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Header information for rating proposals submitted to TROLIE.  Users must enter at least the assumed emergency
 * durations and power system resources
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@EqualsAndHashCode
public class ProposalHeader {

    @JsonProperty("source")
    private DataProvenance source;

    @JsonProperty("default-emergency-durations")
    private List<EmergencyDuration> defaultEmergencyDurations;

    @JsonProperty("power-system-resources")
    private List<PowerSystemResource> powerSystemResources;


}
