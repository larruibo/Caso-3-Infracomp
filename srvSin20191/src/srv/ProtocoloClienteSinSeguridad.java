package srv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class ProtocoloClienteSinSeguridad {
	
	//Flujos del cliente y del servidor
	static BufferedReader stdIn;
	static BufferedReader lector;
	static PrintWriter escritor;
	
	
	// Constantes
	public final static String OK = "OK";
	public final static String ERROR = "ERROR";
	public final static String HOLA = "HOLA";
	public final static String ALGORITMOS = "ALGORITMOS";
	public final static String SEPARADOR = ":";
	public final static String SEPARADOR_ID = ";";
	public final static String ESPACIO = " ";
	public final static String CERTIFICADO_CLIENTE = "CERTCLNT";
	public final static String SEPARADOR_USUARIO = ",";
	public final static String PROVEEDOR = "BC";
	public final static String COMIENZO_CERTIFICADO = "-----BEGIN CERTIFICATE-----";
	public final static String FINAL_CERTIFICADO = "-----END CERTIFICATE-----";
	
	// Algoritmos
	private final static String[] simetricos = { "AES", "Blowfish"};
	private final static String[] asimetricos = { "RSA" };
	private final static String[] hmacs = { "HMACSHA1", "HMACSHA256", "HMACSHA384", "HMACSHA512" };
	public static String simetrico = "";
	public static String asimetrico = "";
	public static String hmac = "";
	
	// Certificados
	static X509Certificate cert = null;
	static PublicKey serverPublicKey = null;
	static SecretKey llaveSimetrica = null;
	private static KeyPair parLlaves;
		
		
	//Método que se encarga de procesar la comunicación entre el cliente y el servidor.
	public static void procesar(BufferedReader stdIn2, BufferedReader lector2, PrintWriter escritor2) throws IOException
	{
		String fromServer = "";
		String fromUser = "";
		
		stdIn = stdIn2;
		lector = lector2;
		escritor = escritor2;
		
		try {
			comenzarComunicacionEnviarAlgoritmos(stdIn);
			manejarCertificados(fromServer);
			manejarMensajes(stdIn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Método que se encarga de entablar la primera parte del protocolo con el servidor, encargado de
	//la selección de los algoritmos.
	public static void comenzarComunicacionEnviarAlgoritmos(BufferedReader stdIn) throws Exception {
		
		boolean continuar = false;
		String ans;
		int opcion;
		
		//Cadena para iniciar el protocolo
		escritor.println(HOLA);
		String respuesta = lector.readLine();

		if(!respuesta.equals(OK)) {
			throw new Exception("No se pudo conectar con el servidor");
		}

		System.out.println();
		System.out.println("Se entabló la comunicación con el servidor.");
		//Escoge el algoritmo simétrico, verifica que escoja un algoritmo válido
		while(!continuar)
		{
			// Escoger simetrico
			System.out.println("Por favor escoja el número del algoritmo simetrico que desea: ");

			for(int i = 0; i < simetricos.length; i++) 
			{
				System.out.println((i+1) +" " +simetricos[i]);
			}

			ans = stdIn.readLine();
			try
			{
				opcion = Integer.parseInt(ans);
				if(opcion > simetricos.length || opcion  < 1) 
				{
					System.out.println("Opcion no existe");
				} else {
					simetrico = simetricos[opcion-1];
					continuar = true;
				}
			}
			catch(Exception e)
			{
				System.out.println("No ingresó un número");
			}
			
		}
		continuar = false;
		
		//Escoge el algoritmo asimetrico, verifica que escoja un algoritmo válido
		while(!continuar)
		{
			// Escoger asimetrico
			System.out.println("Por favor escoja el número del algoritmo asimetrico que desea: ");

			for(int i = 0; i < asimetricos.length; i++)
			{
				System.out.println((i+1) +" " +asimetricos[i]);
			}

			ans = stdIn.readLine();
			try
			{
				opcion = Integer.parseInt(ans);
				if(opcion > asimetricos.length || opcion  < 1) 
				{
					System.out.println("Opcion no existe");
				} else {
					asimetrico = asimetricos[opcion-1];
					continuar = true;
				}
			}
			catch(Exception e)
			{
				System.out.println("No ingresó un número");
			}
			
		}

		continuar = false;
		
		//Escoge el HMAC, verifica que escoja un algoritmo válido
		while(!continuar)
		{
			// Escoger hmac
			System.out.println("Por favor escoja el número del algoritmo hmac que desea: ");

			for(int i = 0; i < hmacs.length; i++) 
			{
				System.out.println((i+1) +" " +hmacs[i]);
			}

			try
			{
				ans = stdIn.readLine();
				opcion = Integer.parseInt(ans);
				if(opcion > hmacs.length || opcion  < 1) 
				{
					System.out.println("Opcion no existe");
				} else {
					hmac = hmacs[opcion-1];
					continuar = true;
				}
			}
			catch(Exception e)
			{
				System.out.println("No ingresó un número");
			}
				
		}
		continuar = false;

			

		System.out.println("Escogió: " +simetrico +SEPARADOR +asimetrico +SEPARADOR +hmac);
		escritor.println(ALGORITMOS +SEPARADOR +simetrico +SEPARADOR +asimetrico +SEPARADOR +hmac);
		System.out.println("Respuesta del servidor a los algoritmos: " + lector.readLine());
	}
	
	//Método que se encarga del envío y validación de los certificados con el servidor, siguiendo
	//la segunda parte del protocolo.
	public static void manejarCertificados(String fromServer) throws Exception {
		
		System.out.println();
		System.out.println("Envío de certificados.");
		KeyPairGenerator localKeyPairGenerator = KeyPairGenerator.getInstance(asimetrico);
		localKeyPairGenerator.initialize(1024, new SecureRandom());
		parLlaves = localKeyPairGenerator.generateKeyPair();

		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator localObject2 = KeyPairGenerator.getInstance(asimetrico, PROVEEDOR);
		localObject2.initialize(1024);
		parLlaves = localObject2.generateKeyPair();

		//Certificado del cliente
		cert = generarCertificado(parLlaves);
		
		//Conversión a bytes y a String, se envía el string al servidor
		byte[] certificadoEnBytes = cert.getEncoded();
		String certificadoString = DatatypeConverter.printHexBinary(certificadoEnBytes);
		escritor.println(certificadoString);
		System.out.println("Se envió certificado al servidor");

		//Lee el certificado del servidor y lo decodifica. Obtiene la Llave pública
		fromServer = lector.readLine();
		byte[] decoded = DatatypeConverter.parseHexBinary(fromServer);
		X509Certificate certificadoServidor= (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
		serverPublicKey = certificadoServidor.getPublicKey();
		
		//Convierte la llave pública a bytes y a String y la envía al servidor.
		byte[] llaveEnBytes = serverPublicKey.getEncoded();
		String llaveString = DatatypeConverter.printHexBinary(llaveEnBytes);
		System.out.println("Se aceptó el certificado y se recibió el del servidor");
		
		//Envía la llave pública al servidor y espera la devolución de la misma
		escritor.println(llaveString);
		
		fromServer = lector.readLine();
		System.out.println("Se recibió la llave del servidor.");
		
		if(fromServer.equalsIgnoreCase(llaveString))
		{
			System.out.println("Llaves coinciden.");
		}
		else
		{
			throw new Exception("La llave que retornó el servidor no coincide.");
		}
		
		
	}
	
	//Método que se encarga de manejar los mensajes entre el cliente y el servidor
	//para la tercera parte del protocolo. Se encarga del envío de los datos.
	public static void manejarMensajes(BufferedReader stdIn) throws IOException 
	{
		String fromServer;
		String id;
		String gradosLatitud;
		String minutosLatitud;
		String gradosLongitud;
		String minutosLongitud;
		int datosEnviados = 0;
		
		System.out.println();
		System.out.println("Envío de la información al servidor.");
		escritor.println("OK");
		System.out.println("Por favor sea cuidadoso con los datos que va a ingresar. Deberá hacerlo 2 veces.");
		while(datosEnviados<2)
		{
			//Ingresar el número de identificacion
			System.out.println("Por favor ingrese el número de identificación");
			id = stdIn.readLine();
			
			//Ingresar los grados de la latitud
			System.out.println("Por favor ingrese el número de grados de la latitud");
			gradosLatitud = stdIn.readLine();
			
			//Ingresar los minutos decimales de la latitud
			System.out.println("Por favor ingrese los minutos decimales de la latitud. Recuerde que el separador es punto (.)");
			minutosLatitud = stdIn.readLine();
			
			//Ingresar los grados de la longitud
			System.out.println("Por favor ingrese el número de grados de la longitud");
			gradosLongitud = stdIn.readLine();
			
			//Ingresar los minutos decimales de la latitud
			System.out.println("Por favor ingrese los minutos decimales de la longitud. Recuerde que el separador es punto (.)");
			minutosLongitud = stdIn.readLine();
			
			//Crea la cadena completa y la envía al servidor. Realiza el paso dos veces.
			String datos = id + SEPARADOR_ID + gradosLatitud + ESPACIO + minutosLatitud + SEPARADOR_USUARIO + gradosLongitud + ESPACIO + minutosLongitud;
			System.out.println("Datos a enviar al servidor: " + datos);
			escritor.println(datos);
			datosEnviados++;
			
		}
		
		//Espera la respuesta del servidor, bien sea la confirmación de los datos recibidos o ERROR
		fromServer = lector.readLine();
		
		if(fromServer.equalsIgnoreCase(ERROR))
		{
			System.out.println(fromServer + ": Error al recibir los datos. Por favor intente de nuevo.");
		}
		else
		{
			System.out.println("Confirmación: " + fromServer + " :Datos recibidos correctamente. ");
		}
	}
	
	
	
	//Métodos que generan los certificados
	public static X509Certificate generarCertificado(KeyPair parLlaves) throws Exception
	{
		X509Certificate certificado = crearCertificado(parLlaves, PROVEEDOR);
		return certificado;
	}
	
	@SuppressWarnings("deprecation")
	public static X509Certificate crearCertificado(KeyPair parLlaves, String proveedor) throws Exception
	{
		PublicKey publica1 = parLlaves.getPublic();
		PrivateKey privada = parLlaves.getPrivate();
		PublicKey publica2 = parLlaves.getPublic();
		JcaX509ExtensionUtils localJcaX509ExtensionUtils = new JcaX509ExtensionUtils();
		JcaX509v3CertificateBuilder localJcaX509v3CertificateBuilder = new JcaX509v3CertificateBuilder(new X500Name("CN=0.0.0.0, OU=None, O=None, L=None, C=None"), new BigInteger(128, new SecureRandom()), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + 8640000000L), new X500Name("CN=0.0.0.0, OU=None, O=None, L=None, C=None"), publica1);
		localJcaX509v3CertificateBuilder.addExtension(X509Extension.subjectKeyIdentifier, false, localJcaX509ExtensionUtils.createSubjectKeyIdentifier(publica1));
		localJcaX509v3CertificateBuilder.addExtension(X509Extension.authorityKeyIdentifier, false, localJcaX509ExtensionUtils.createAuthorityKeyIdentifier(publica2));
		return new JcaX509CertificateConverter().setProvider(proveedor).getCertificate(localJcaX509v3CertificateBuilder.build(new JcaContentSignerBuilder("MD5withRSA").setProvider(proveedor).build(privada)));
	}
	
}
