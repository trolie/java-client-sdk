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
import org.trolie.client.model.common.PowerSystemResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Status of a forecast rating proposal
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class ForecastRatingProposalStatus {

    @JsonProperty("forecast-provider")
    private DataProvenance forecastProvider;

    @JsonProperty("begins")
    private String begins;

    @JsonProperty("incomplete-obligation-count")
    private int incompleteObligationCount;

    @JsonProperty("incomplete-obligations")
    private List<PowerSystemResource> incompleteObligations;

    @JsonProperty("invalid-proposal-count")
    private int invalidProposalCount;

    @JsonProperty("proposal-validation-errors")
    private List<ProposalValidationError> proposalValidationErrors;
}
