/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/AGPL-3.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kpay;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * Rest API
 *
 */
@Path("kpay")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class KPayResource {
    private static final Logger log = LoggerFactory.getLogger(KPayResource.class);

    private final KPayInstance instance;

    public KPayResource() {
        instance = KPayInstance.getInstance(null);
    }

    @GET
    @Produces("application/json")
    @Path("/info")
    public String info() {
        return "KPay Service";
    }

    @GET
    @Produces("application/json")
    @Path("/status")
    public String status() {
        return "{ running}";
    };


    @POST
    @Path("/generatePayments")
    @Operation(summary = "start processing some payments",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String generatePayments() {
        instance.getInstance().generatePayments();
        return "{ status: \"success\", message: \"payments is running\", title: \"Success\" }";
    }


    @POST
    @Path("/stopPayments")
    @Operation(summary = "stop processing some payments",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String stopPayments() {
        instance.getInstance().stopPayments();
        return "{ status: \"success\", message: \"payments stopped\", title: \"Success\" }";
    }


    @POST
    @Path("/viewMetrics")
    @Operation(summary = "used by panels to get data",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String viewMetrics(@Parameter(description = "query sent from the dashboard", required = true) String query) {
        return instance.getInstance().viewMetrics();
    }
}
