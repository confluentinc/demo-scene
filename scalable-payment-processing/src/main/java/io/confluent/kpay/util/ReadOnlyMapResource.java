package io.confluent.kpay.util;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Path("map")
public interface ReadOnlyMapResource<K,V> {

    int size();
    @GET()
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON)
    Set<K> keySet();

    @GET()
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    V get(K k);

    @GET()
    @Path("/getQuery")
    @Produces(MediaType.APPLICATION_JSON)
    List<Pair<K,V>> get(List<K> query);
}
