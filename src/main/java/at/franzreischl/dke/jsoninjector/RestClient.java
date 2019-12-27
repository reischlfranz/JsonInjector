package at.franzreischl.dke.jsoninjector;

import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RestClient {
    private static final int TIMEOUT_MILLIS = 5000;

    URL url;
    Client client = ClientBuilder.newClient(new ClientConfig()
//                .register(MyClientResponseFilter.class)
//                .register(new AnotherClientFilter()
    );

    public RestClient(String baseUrl, String resource) throws MalformedURLException {
        try {
            if (!baseUrl.endsWith("/")) baseUrl += '/';
            url = new URL(baseUrl + resource);
        } catch (MalformedURLException ue) {

        }
        if(url == null) throw new MalformedURLException();
    }

    public Response doGet(Map<String, String> params) {
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        return response;
    }

    public Response doPost(Map<String, String> params, Object data) {
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(entity);
        return response;
    }

    public Response doPostJsonObject(Map<String, String> params, JSONObject data) {
//        System.err.println("POSTING json object: \n"+data.toString());
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response = invocationBuilder.post(Entity.json(data.toString()));
        return response;
    }

    public Response doPostMap(Map<String, String> params, Map<String, Object> data) {
        return doPostJsonObject(params, new JSONObject(data));
    }

    public Response doPostJsonObject(Map<String, String> params, JSONArray data) {
//        System.err.println("POSTING json array: \n"+data.toString());
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response = invocationBuilder.post(Entity.json(data.toString()));
        return response;
    }

    public Response doPut(Map<String, String> params, Object data) {
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response
                = invocationBuilder
                .put(entity);
        return response;
    }



    public Response doPutJsonObject(Map<String, String> params, JSONObject data) {
//        System.err.println("PUTING json object: \n"+data.toString());
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response = invocationBuilder.post(Entity.json(data.toString()));
        return response;
    }
    public Response doPutMap(Map<String, String> params, Map<String, Object> data) {
        return doPutJsonObject(params, new JSONObject(data));
    }

    public Response doPutJsonObject(Map<String, String> params, JSONArray data) {
//        System.err.println("PUTING json array: \n"+data.toString());
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response = invocationBuilder.post(Entity.json(data.toString()));
        return response;
    }



    public Response doDelete(Map<String, String> params) {
        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }
        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.delete();
        return response;
    }

}
