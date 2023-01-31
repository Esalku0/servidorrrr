package aev;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.HostnameVerifier;

import com.sun.net.httpserver.HttpServer;

public class Servidor {
	
	
	public static void anyadirConexionLogs(String host,int puerto) throws IOException {
		
		 DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		 Date date = new Date();
		  System.out.println("Hora actual: " + dateFormat.format(date));
		
		  FileWriter fw = new FileWriter("logs.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			FileReader fr = new FileReader("logs.txt");
			BufferedReader br = new BufferedReader(fr);
			
			String linea = br.readLine();
			
			while(linea!=null){
				bw.write(linea);
				System.out.println( "Fecha de conexion: coco");	
				linea = br.readLine();
			}
			bw.write("Fecha de conexion: " + dateFormat.format(date) + " host: " + host + " - " + " puerto: " + puerto);
			bw.newLine();
			br.close();
			fr.close();
			bw.close();
			fw.close();
	}
	

	public static void main(String[] args) {
		try {
			FileReader fr = new FileReader("config.txt");
			BufferedReader br = new BufferedReader(fr);
			String host = br.readLine();
			int puerto = Integer.parseInt(br.readLine());
			br.close();
			InetSocketAddress direccionTCPIP = new InetSocketAddress(host, puerto);
			anyadirConexionLogs(host, puerto);
			int backlog = 0; // Numero de conexiones pendientes que el servidor puede mantener en cola
			HttpServer servidor = HttpServer.create(direccionTCPIP, backlog);

			GestorHTTP gestorHTTP = new GestorHTTP(); // Clase que gestionara los GETs, POSTs, etc.
			String rutaRespuesta = "/servidor"; // Ruta (a partir de localhost en este ejemplo) en la que el servidor dara
												// respuesta
			servidor.createContext(rutaRespuesta, gestorHTTP); // Crea un contexto, asocia la ruta al gestor HTTP

			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
			servidor.setExecutor(threadPoolExecutor);

			servidor.start();
			System.out.println("Servidor HTTP arranca en " + host + ":" + puerto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}