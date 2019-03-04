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

import io.confluent.kpay.metrics.model.ThroughputStats;
import io.confluent.kpay.payments.model.AccountBalance;
import io.confluent.kpay.payments.model.ConfirmedStats;
import io.confluent.kpay.payments.model.InflightStats;
import io.confluent.kpay.util.Pair;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

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

    /**
     * Data generation functions
     * @return
     */
    @POST
    @Path("/payments/start")
    @Operation(summary = "start processing some payments",
            tags = {"payment generation"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String generatePayments(@Parameter(description = "Submit task to the worker queues", required = true) String paymentsPerSecond) {
        instance.getInstance().generatePayments(Integer.parseInt(paymentsPerSecond));
        return "{ status: \"success\", message: \"payments is running\", title: \"Success\" }";
    }


    @POST
    @Path("/payments/stop")
    @Operation(summary = "stop processing some payments",
            tags = {"payment generation"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String stopPayments() {
        instance.getInstance().stopPayments();
        return "{ status: \"success\", message: \"payments stopped\", title: \"Success\" }";
    }

    @GET
    @Path("/payments/status")
    @Operation(summary = "check payment running status",
            tags = {"payment generation"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String paymentGenstatus() {
        return String.format("{ status: \"%b\"}", instance.getInstance().isGeneratingPayments());
    }




    /**
     * Trust plane: Instrumentation and observability, DLQ information
     * @return
     */
    @GET
    @Path("/metrics/throughput")
    @Operation(summary = "view throughput metrics",
            tags = {"metrics"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public ThroughputStats viewMetrics() {
        return instance.getInstance().viewMetrics();
    }
    @GET
    @Path("/metrics/pipeline")
    @Operation(summary = "view payment pipeline metrics",
            tags = {"metrics"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public Pair<InflightStats, ConfirmedStats> pipelineStats() {
        return instance.getInstance().getPaymentPipelineStats();
    }


//    @GET
//    @Path("/metrics/dlq")
//    public String dlqMetrics() {
//        return instance.getInstance().viewMetrics();
//    }


    /**
     * Control plane
     * @return
     * - pause, resume, status, scale[up,down]
     */
    @POST
    @Path("/pause")
    @Operation(summary = "pause processing",
            tags = {"control"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String pause() {
        return String.format("{ status: \"%s\", message: \"control pausing\", metrics: \"some metrics maybe?\" }", instance.getInstance().pause());
    }
    @POST
    @Path("/resume")
    @Operation(summary = "resume processing",
            tags = {"control"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public String resume() {
        return String.format("{ status: \"%s\", message: \"control resuming\", metrics: \"some metrics maybe?\" }", instance.getInstance().resume());
    }

    @GET
    @Produces("application/json")
    @Path("/status")
    public String status() {
        return String.format("{ status: \"%s\" }", instance.getInstance().status());
    };



    /**
     * Domain model
     * @return
     * - list accounts, show balance etc, show accounts over 'n', most active accounts
     */

    @GET
    @Produces("application/json")
    @Path("/listAccounts")
    @Operation(summary = "full set of accounts",
            tags = {"data"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    public List<AccountBalance> listAccounts() {
        return instance.getInstance().listAccounts();
    };


}
