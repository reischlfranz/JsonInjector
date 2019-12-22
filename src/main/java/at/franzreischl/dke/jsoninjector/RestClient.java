package at.franzreischl.dke.jsoninjector;

import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    public RestClient(String baseUrl, String resource) {
        try {
            if (!baseUrl.endsWith("/")) baseUrl += '/';
            url = new URL(baseUrl + resource);
        } catch (MalformedURLException ue) {

        }
    }

    public Response doGet(Map<String, String> params) {


        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }

//        System.out.println("  GET    " + webTarget.getUri().toString() + " ...");

        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
//        invocationBuilder.header("some-header", "true");

        Response response = invocationBuilder.get();

//        System.out.println("  > Status: " + response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase());
//        for (String s : response.getStringHeaders().keySet()) {
//            System.out.println("  > Header: " + s + " - " + response.getStringHeaders().get(s));
//        }
//        System.out.println("  > Data:   " + response.readEntity(String.class));
        return response;
    }

    public Response doPost(Map<String, String> params, Object data) {
//        Client client = ClientBuilder.newClient(new ClientConfig()
////                .register(MyClientResponseFilter.class)
////                .register(new AnotherClientFilter()
//        );

        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }

//        System.out.println("REST-Aufruf Middleware:");
//        System.out.println("  PUT    " + webTarget.getUri().toString() + " ...");

        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
//        invocationBuilder.header("some-header", "true");

        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response
                = invocationBuilder
                .post(entity);

//        System.out.println("  > Status: " + response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase());
//        for (String s : response.getStringHeaders().keySet()) {
//            System.out.println("  > Header: " + s + " - " + response.getStringHeaders().get(s));
//        }
//        System.out.println("  > Data:   " + response.readEntity(String.class));
        return response;
    }

    public Response doPut(Map<String, String> params, Object data) {
//        Client client = ClientBuilder.newClient(new ClientConfig()
////                .register(MyClientResponseFilter.class)
////                .register(new AnotherClientFilter()
//        );

        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }

//        System.out.println("REST-Aufruf Middleware:");
//        System.out.println("  POST   " + webTarget.getUri().toString() + " ...");

        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
//        invocationBuilder.header("some-header", "true");

        Entity entity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response
                = invocationBuilder
                .put(entity);

//        System.out.println("  > Status: " + response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase());
//        for (String s : response.getStringHeaders().keySet()) {
//            System.out.println("  > Header: " + s + " - " + response.getStringHeaders().get(s));
//        }
//        System.out.println("  > Data:   " + response.readEntity(String.class));
        return response;
    }

    public Response doDelete(Map<String, String> params) {
//        Client client = ClientBuilder.newClient(new ClientConfig()
////                .register(MyClientResponseFilter.class)
////                .register(new AnotherClientFilter()
//        );

        WebTarget webTarget = client.target(url.toString());
        if (params != null) {
            for (String s : params.keySet()) {
                webTarget = webTarget.queryParam(s, params.get(s));
            }
        }

//        System.out.println("  DELETE " + webTarget.getUri().toString() + " ...");

        Invocation.Builder invocationBuilder =
                webTarget.request(MediaType.APPLICATION_JSON);
//        invocationBuilder.header("some-header", "true");

        Response response = invocationBuilder.delete();

//        System.out.println("  > Status: " + response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase());
//        for (String s : response.getStringHeaders().keySet()) {
//            System.out.println("  > Header: " + s + " - " + response.getStringHeaders().get(s));
//        }
//        System.out.println("  > Data:   " + response.readEntity(String.class));
        return response;
    }

}
