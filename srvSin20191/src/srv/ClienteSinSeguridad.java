package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteSinSeguridad {
	
	public static final int PUERTO = 2000;
	public static final String SERVIDOR = "localhost";
	
	public static void main(String args[]) throws IOException 
	{
		Socket socket = null;
		PrintWriter escritor = null;
		BufferedReader lector = null;
		
		System.out.println("Cliente...");
		
		try
		{
			System.out.println("Se está utilizando el puerto: " + PUERTO + " y el servidor: " + SERVIDOR );
			
			//Crea el socket del lado del cliente
			socket = new Socket(SERVIDOR, PUERTO);
			
			//Se conectan los flujos
			escritor = new PrintWriter(socket.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		//Crea un flujo para leer lo que escribe el cliente por el teclado
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		//Se ejecuta el protocolo del lado del cliente
		ProtocoloClienteSinSeguridad.procesar(stdIn, lector, escritor);
		
		//Se cierran los flujos y el socket
		stdIn.close();
		lector.close();
		escritor.close();
		socket.close();
	}

}
