package io.confluent.kpay.util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Path("ktable")
public interface ReadOnlyMapResource<K,V> {

    int size();
    @GET()
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON)
    Set<K> keySet();


    @POST
    @Path("/get")
    @Operation(summary = "singular get operation",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    V get(K k);

    @POST
    @Path("/getQuery")
    @Operation(summary = "query get operation",
            tags = {"query"},
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "405", description = "Invalid input")
            })
    List<Pair<K,V>> get(List<K> query);
}
