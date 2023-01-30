package aev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import static com.mongodb.client.model.Filters.eq;

public class GestorHTTP implements HttpHandler {

	static String imagenString = "";
	static String alias = "";
	static String nombreCompleto = "";
	static int fechaNacimiento = 0;

	public static String mostrarTodosAlias() {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		MongoCollection<Document> coleccion = database.getCollection("delincuente");
		String todosLosAliaString = "";
		MongoCursor<Document> cursor = coleccion.find().iterator();
		while (cursor.hasNext()) {
			JSONObject obj = new JSONObject(cursor.next().toJson());
			todosLosAliaString += obj.getString("alias") + "\n";
			System.out.println(todosLosAliaString);

		}
		mongoClient.close();
		return todosLosAliaString;
	}

	public static void mostrarUnAlias(String alias1) {
		System.out.println(alias1);
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		MongoCollection<Document> coleccion = database.getCollection("delincuente");

		Bson query = eq("alias", alias1);
		MongoCursor<Document> cursor = coleccion.find(query).iterator();
		while (cursor.hasNext()) {
			JSONObject obj = new JSONObject(cursor.next().toJson());

			imagenString = obj.getString("imagen");
			fechaNacimiento = obj.getInt("fechaNacimiento");
			nombreCompleto = obj.getString("nombreCompleto");
			alias = obj.getString("alias");
		}

		mongoClient.close();
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		String requestParamValue = null;
		if ("GET".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handleGetRequest(httpExchange);
			handleGETResponse(httpExchange, requestParamValue);
		} else if ("POST".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handlePostRequest(httpExchange);
			handlePOSTResponse(httpExchange, requestParamValue);
		}

	}

	private String handleGetRequest(HttpExchange httpExchange) {
		System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
		if (httpExchange.getRequestURI().toString().contains("?")) {
			return httpExchange.getRequestURI().toString().split("\\?")[1];
		}else {
			return httpExchange.getRequestURI().toString().split("\\/")[2];
		}
	}
	

	private String handlePostRequest(HttpExchange httpExchange) {
		System.out.println("Recibida URI tipo POST: " + httpExchange.getRequestBody().toString());
		InputStream is = httpExchange.getRequestBody();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private void handleGETResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
		System.out.println("COSITA: " + requestParamValue);
		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "<html><body><h1>Parametro no reconocido</h1></body></html>";
		if (requestParamValue.equals("mostrarTodos")) {
			System.out.println("hola");
			String aliasString = mostrarTodosAlias();
			htmlResponse = "<html><body><h1>Todos los alias: </h1> <h3>" + aliasString + " </h3> </body></html>";
		}
		if (requestParamValue.equals("mostrarUno")) {
			System.out.println("AAAAAAAAAAAAAAAAAAAA");
			String alias = requestParamValue.split("=")[1];
			String aliasGodString= removefirstChar(alias);
			System.out.println("cont" + aliasGodString);
			httpExchange.sendResponseHeaders(204, -1);
			mostrarUnAlias(aliasGodString);
			htmlResponse = "<html><body><h1>Todos los alias: </h1> <h3>" + nombreCompleto + " </h3> </body></html>";
		}
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
		System.out.println("Devuelve respuesta HTML: " + htmlResponse);
	}

	
    public static String removefirstChar(String str)
    {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(1);
    }
	
	
	
	private void handlePOSTResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
		OutputStream outputStream = httpExchange.getResponseBody();
//		String htmlResponse = "<html><body><h1>Par&aacutemetro no reconocido</h1></body></html>";
		System.out.println(requestParamValue);
		if (requestParamValue.contains("mostrarUno")) {
			temperaturaTermostato = requestParamValue.split("=")[1];
			httpExchange.sendResponseHeaders(204, -1);
			System.out.println("El servidor devuelve codigo 204");
			regularTemperatura();
		}
		outputStream.flush();
		outputStream.close();
		System.out.println("Devuelve respuesta HTML: vacia");
	}

	
	
	
}