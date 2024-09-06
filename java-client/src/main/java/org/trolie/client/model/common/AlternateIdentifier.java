/*
 *
 *  *  ===========================================================================
 *  *
 *  *  Copyright and Proprietary Information
 *  *
 *  *  Copyright (c) 1993, 2024 General Electric Technology GmbH. All Rights Reserved.
 *  *
 *  *  NOTE: CONTAINS CONFIDENTIAL AND PROPRIETARY INFORMATION OF GENERAL ELECTRIC
 *  *  TECHNOLOGY GMBH AND MAY NOT BE REPRODUCED, TRANSMITTED, STORED, OR COPIED IN
 *  *  WHOLE OR IN PART, OR USED TO FURNISH INFORMATION TO OTHERS, WITHOUT THE PRIOR
 *  *  WRITTEN PERMISSION OF GENERAL ELECTRIC TECHNOLOGY GMBH OR GRID SOLUTIONS.
 *  *
 *  *  ==========================================================================
 *
 */

package org.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AlternateIdentifier {

    @JsonProperty("name")
    private String name;

    @JsonProperty("authority")
    private String authority;

    @JsonProperty("mrid")
    private String mrid;
}
