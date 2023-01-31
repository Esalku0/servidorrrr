package aev;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import static com.mongodb.client.model.Filters.*;

public class GestorHTTP implements HttpHandler {

	static String imagenString = "";
	static String alias = "";
	static String nombreCompleto = "";
	static String nacionalidad = "";
	static String fechaNacimiento = "";

	public static String mostrarTodosAlias() {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		MongoCollection<Document> coleccion = database.getCollection("Delincuente");
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
	
	
	public void guardarLog(HttpExchange httpExchange,String tipo) throws IOException {
		
		 DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		 Date date = new Date();

		
        File fichero = new File ("logs.txt");
        InetSocketAddress address = httpExchange.getRemoteAddress();
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(fichero.createNewFile()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fichero, true));
            bw.write("Ip: "+ address + "   Timestamp: " + 	 dateFormat.format(date) + "tipo:"+ tipo + "\n");
            bw.close();
        }else {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fichero, true));
            bw.write("Ip: "+ address + "   Timestamp: " + 	 dateFormat.format(date) + "tipo:"+ tipo + "\n");
            bw.close();
        }
    }
	
	

	public static void mostrarUnAlias(String alias1) {
		System.out.println("EL ALIAS ES: " + alias1);
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		MongoCollection<Document> coleccion = database.getCollection("Delincuente");

		MongoCursor<Document> cursor = coleccion.find(Filters.eq("alias", alias1)).iterator();

		while (cursor.hasNext()) {
			JSONObject obj = new JSONObject(cursor.next().toJson());
			nacionalidad = obj.getString("nacionalidad");
			fechaNacimiento = obj.getString("fechaNacimiento");
			nombreCompleto = obj.getString("nombreCompleto");
			alias = obj.getString("alias");
			imagenString = obj.getString("fotografia");
		}
		mongoClient.close();
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String requestParamValue = null;

		if ("GET".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handleGetRequest(httpExchange);
			guardarLog(httpExchange, "GET");
			handleGETResponse(httpExchange, requestParamValue);
		} else if ("POST".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handlePostRequest(httpExchange);
			guardarLog(httpExchange, "POST");
			handlePOSTResponse(httpExchange, requestParamValue);
	
		}
	}

	private String handleGetRequest(HttpExchange httpExchange) {
		System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
		if (httpExchange.getRequestURI().toString().contains("?")) {

			return httpExchange.getRequestURI().toString();
		} else {

			return httpExchange.getRequestURI().toString().split("\\/")[2];
		}
	}

	private String handlePostRequest(HttpExchange httpExchange) {
		System.out.println("Recibida URI tipo POST: " + httpExchange.getRequestBody().toString());
		InputStream is = httpExchange.getRequestBody();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line = "";
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

		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "<html><body><h1>Parametro no reconocido</h1></body></html>";
		if (requestParamValue.equals("mostrarTodos")) {
			String aliasString = mostrarTodosAlias();
			htmlResponse = "<html><body><h1>Todos los alias: </h1> <h3>" + aliasString + " </h3> </body></html>";

		}
		if (requestParamValue.contains("mostrarUno")) {

			String alias = requestParamValue.split("=")[1];


			mostrarUnAlias(alias);

			htmlResponse = "<html><body><h1>Info del alias en concreto: <br>Nombre Completo: " + nombreCompleto
					+ "<br>Fecha De Nacimiento: " + fechaNacimiento + "<br>Nacionalidad: " + nacionalidad + "</h1>"
					+ "<img src=" + imagenString + " width='200' height='200'> </body></html>";

		}
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
		System.out.println("Devuelve respuesta HTML: " + htmlResponse);

		nombreCompleto = "";
		fechaNacimiento = "";
		nacionalidad = "";
		alias = "";
		imagenString = "";

	}

	private void handlePOSTResponse(HttpExchange httpExchange, String ficheroJson) throws IOException {
		OutputStream outputStream = httpExchange.getResponseBody();

		JSONObject obj = new JSONObject(ficheroJson);
		nacionalidad = obj.getString("nacionalidad");
		fechaNacimiento = obj.getString("fechaNacimiento");
		nombreCompleto = obj.getString("nombreCompleto");
		alias = obj.getString("alias");
		imagenString = obj.getString("fotografia");

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		MongoCollection<Document> coleccion = database.getCollection("Delincuente");
		Document doc = new Document();
		doc.append("alias", alias);
		doc.append("nombreCompleto", nombreCompleto);
		doc.append("fechaNacimiento", fechaNacimiento);
		doc.append("nacionalidad", nacionalidad);
		doc.append("fotografia", imagenString);
		coleccion.insertOne(doc);
		mongoClient.close();

		String htmlResponse = "<html><body><h1>Info del delincuente guardado: <br>Nombre Completo: " + nombreCompleto
				+ "<br>Fecha De Nacimiento: " + fechaNacimiento + "<br>Nacionalidad: " + nacionalidad + "</h1>"
				+ "<img src=" + imagenString + " width='200' height='200'> </body></html>";

		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();
		String datosDelincuente = "Alias: " + alias + "\nNombre Completo: " + nombreCompleto + "\nFecha De Nacimiento: "
				+ fechaNacimiento + "\nNacionaliad: " + nacionalidad + "\n Fotogtafía: " + imagenString;
		mandaEmail("Nuevo Delincuente", datosDelincuente);
	}

	public static void mandaEmail(String asunto, String contenido) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("//// NUEVO DELINCUENTE DETECTADO ////");
		System.out.println("Introduce tu correo: ");
		String userID=sc.next();
		System.out.println("Introduce tu contraseña: ");
		String password=sc.next();

			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userID, password);
				}
			});

			try {

				Message message = new MimeMessage(session);

				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("estanisaliagak@gmail.com"));
				message.setSubject(asunto);
				message.setText(contenido);

				Transport.send(message);

				System.out.println("Correo enviado correctamente");

			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		

	}

}