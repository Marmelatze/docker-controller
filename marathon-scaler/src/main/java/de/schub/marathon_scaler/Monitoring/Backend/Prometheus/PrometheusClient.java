package de.schub.marathon_scaler.Monitoring.Backend.Prometheus;

import com.google.gson.*;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Exception.PrometheusException;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Exception.PrometheusQueryException;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model.PrometheusError;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model.PrometheusResponse;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model.Value;
import de.schub.marathon_scaler.Monitoring.Backend.Prometheus.Model.VectorResponse;
import org.glassfish.jersey.uri.UriComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Client to make queries to prometheus
 */
public class PrometheusClient
{
    Logger logger = LoggerFactory.getLogger(PrometheusClient.class);

    WebTarget target;
    Gson gson;

    /**
     * @param endpoint e.g. http://localhost:9090
     */
    public PrometheusClient(URI endpoint)
    {
        Client client = ClientBuilder.newBuilder().build();
        target = client.target(endpoint).path("/api/query");
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(PrometheusResponse.class, new PrometheusResponseAdapter());
        gson = builder.create();
    }

    /**
     * Query prometheus and return the result
     *
     * @param expression
     * @return
     * @throws PrometheusException
     */
    public PrometheusResponse query(String expression) throws PrometheusException
    {
        WebTarget queryTarget = target.queryParam(
            "expr",
            UriComponent.encode(expression, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED)
        );
        logger.debug(expression);
        String json = queryTarget.request().get(String.class);

        PrometheusResponse response = gson.fromJson(json, PrometheusResponse.class);
        if (response instanceof PrometheusError) {
            throw new PrometheusQueryException(expression, ((PrometheusError) response).getError());
        }

        return response;
    }

    /**
     * Like query, but gets a single float value from the first result, when the query results in a vector
     *
     * @param expression
     * @return
     */
    public Optional<Float> querySingleValue(String expression)
    {
        try {
            VectorResponse vector = (VectorResponse) query(expression);
            List<Value> values = vector.getValue();
            if (values.size() > 0) {
                return Optional.of(values.get(0).getValue());
            }
        } catch (PrometheusException e) {
            logger.error("Failed to get single value for query " + expression, e);
        }

        return Optional.empty();
    }

    /**
     * deserializer for GSON to decode different prometheus response types ("type" field).
     */
    private class PrometheusResponseAdapter implements JsonDeserializer<PrometheusResponse>
    {
        @Override
        public PrometheusResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
        {
            String type = json.getAsJsonObject().getAsJsonPrimitive("type").getAsString();
            switch (type) {
                case "error":
                    return context.deserialize(json, PrometheusError.class);
                case "vector":
                    return context.deserialize(json, VectorResponse.class);
                // @TODO other types
            }
            return null;
        }
    }


}
