package srv;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import Helpers.Ciph;
import Helpers.HexConverter;



public class ClienteCifrado extends Cliente {

	// Constantes
	public final static int PORT = 2000;
	public final static String HOST = "192.168.0.35";
	public final static String OK = "OK";
	public final static String ERROR = "ERROR";
	public final static String HOLA = "HOLA";
	public final static String ALGORITMOS = "ALGORITMOS";
	public final static String SEPARADOR = ":";
	public final static String CERTIFICADO_CLIENTE = "CERTCLNT";
	public final static String SEPARADOR_USUARIO = ",";

	// Lectores
	boolean ejecutar = true;
	Socket socket = null;
	PrintWriter escritor = null;
	BufferedReader lector = null;

	// Certificados
	X509Certificate cert = null;
	X509Certificate certServidor = null;
	PublicKey serverPublicKey = null;
	SecretKey llaveSimetrica = null;
	private KeyPair parLlaves;
	SecretKey serverSecretKey=null;

	// Algoritmos
	private final String[] simetricos = {  "AES", "Blowfish"};
	private final String[] asimetricos = { "RSA" };
	private final String[] hmacs = { "HMACSHA256", "HMACSHA1", "HMACSHA384", "HMACSHA512" };
	public String simetrico = "";
	public String asimetrico = "";
	public String hmac = "";
	
	// Tiempos
	private static long tiempoLecturaLlave;
	private static long tiempoEscrituraOk;	
		
	public  long darTiempoLecturaLlave()
	{
		return tiempoLecturaLlave;
	}
			
	public  long darTiempoEscrituraOk()
	{
		return tiempoEscrituraOk;
	}

	// Constructor
	public ClienteCifrado() {
		realizarConexion();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer = "";

		try {	
			comenzarComunicacionEnviarAlgoritmos(stdIn);
			manejarCertificados(fromServer);
			manejarEnvioMensajes(stdIn);
			escritor.close();
			lector.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// Methods
	public void realizarConexion() {
		try {
			socket = new Socket(HOST, PORT);
			escritor = new PrintWriter(socket.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(Exception e) {
			System.err.println("Exception: " +e.getMessage());
			System.exit(1);
		}
	}

	public void comenzarComunicacionEnviarAlgoritmos(BufferedReader cliente) throws Exception {
		escritor.println(HOLA);
		String respuesta = lector.readLine();

		if(!respuesta.equals(OK)) {
			throw new Exception("No se pudo conectar con el servidor");
		}

		// Escoger simetrico
		simetrico = simetricos[0];

		// Escoger asimetrico
		asimetrico = asimetricos[0];
		
		// Escoger hmac
		hmac = hmacs[0];	

		System.out.println("Escogio: " +simetrico +SEPARADOR +asimetrico +SEPARADOR +hmac);
		escritor.println(ALGORITMOS +SEPARADOR +simetrico +SEPARADOR +asimetrico +SEPARADOR +hmac);
	}

	public void manejarCertificados(String fromServer) throws Exception {
		KeyPairGenerator localKeyPairGenerator = KeyPairGenerator.getInstance(asimetrico);
		localKeyPairGenerator.initialize(1024, new SecureRandom());
		parLlaves = localKeyPairGenerator.generateKeyPair();

		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator localObject2 = KeyPairGenerator.getInstance(asimetrico, PROVEEDOR);
		localObject2.initialize(1024);
		parLlaves = localObject2.generateKeyPair();
		// Mi certificado
		
//		cert = generarCertificado(parLlaves);
//		escritor.println(certificateToString(cert).replaceAll("\\s+",""));
//		System.out.println("Se envio certificado al servidor");
		
		
		//byte[] mybyte=cert.getEncoded();
		//escritor.println(mybyte);
		//socket.getOutputStream().write(mybyte);
		//socket.getOutputStream().flush();
		
		cert = generarCertificado(parLlaves);
		byte[] certificadoEnBytes = cert.getEncoded();
		String certificadoString = DatatypeConverter.printHexBinary(certificadoEnBytes);
		escritor.println(certificadoString);
		System.out.println("Se envio certificado al servidor");
		
		String respuesta=lector.readLine();
		if(!respuesta.equals(OK)) {
			throw new Exception(ERROR);
		}
		System.out.println("llego1");
		//recibir comando del protocolo del servidor
		System.out.println("llego22");
		System.out.println("llego33");
		//String cmd=lector.readLine();
		System.out.println("llego44");
		// Saltarse linea
		String cmd=lector.readLine();
		//lol mother of machete
		
		System.out.println(cmd);
	
		// Leer certificado Servidor
		String pem = COMIENZO_CERTIFICADO +System.lineSeparator();
		byte[] decoded = DatatypeConverter.parseHexBinary(cmd);



		
		X509Certificate certificadoServidor= (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
		System.out.println("Se acepto certificado");
		Ciph x = new Ciph();
		SecretKey llaveSimetrica = this.keyGenGenerator(simetrico);
		serverPublicKey = certificadoServidor.getPublicKey();
		byte[] cyph = Ciph.cifrar(llaveSimetrica.getEncoded(), serverPublicKey, asimetrico);
		String llaveString = DatatypeConverter.printHexBinary(cyph);
//		
//		
//		byte[] llaveEnBytes = (serverPublicKey.getEncoded());
//		String llaveString = DatatypeConverter.printHexBinary(llaveEnBytes);
		tiempoLecturaLlave = System.currentTimeMillis();
		escritor.println(llaveString);
	}
	
    public  SecretKey keyGenGenerator(String algoritmo) throws NoSuchAlgorithmException, NoSuchProviderException {
        int tamLlave = 0;
        if (algoritmo.equals("DES")) {
            tamLlave = 56;
        } else if (algoritmo.equals("AES")) {
            tamLlave = 128;
        } else if (algoritmo.equals("Blowfish")) {
            tamLlave = 128;
        } else if (algoritmo.equals("RC4")) {
            tamLlave = 128;
        }
        if (tamLlave == 0) {
            throw new NoSuchAlgorithmException();
        }
        KeyGenerator keyGen = KeyGenerator.getInstance(algoritmo);
        keyGen.init(tamLlave);
        SecretKey key = keyGen.generateKey();
        return key;
    }
	
	
	
    public static byte[] hD(byte[] msg, Key key, String algo) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        Mac mac = Mac.getInstance(algo);
        mac.init(key);
        byte[] bytes = mac.doFinal(msg);
        return bytes;
    }
	public boolean manejarEnvioMensajes(BufferedReader cliente) throws Exception {
		
		
		
		String servidorLlaveSimetrica = lector.readLine();
		byte[] servidor = HexConverter.fromHEX(servidorLlaveSimetrica);
		byte[] sim = Ciph.descifrar(servidor, parLlaves.getPrivate() , asimetrico);
		
		escritor.println(OK);

		SecretKeySpec sk = new SecretKeySpec(sim, 0, sim.length, asimetrico);
		serverSecretKey = sk;
		// Mandar posici�n
		manejarPosicion(cliente);

		// Respuesta al mandar posici�n;
		String resultadoActualizacion= lector.readLine();

		
		if(!resultadoActualizacion.equals(OK)) {
			throw new Exception("No se acepto la posici�n en el servidor");
		} else {
			System.out.println("Se acepto posici�n");
			return true;
		}
	}

	public void manejarPosicion(BufferedReader cliente) throws Exception {
		System.out.println("Ingrese su id seguido de ; seguuido de su posici�n como dos parejas de n�meros \n (grados y minutos en decimal), separados por una coma �,� \n : Por ejemplo: 41 24.2028, 2 10.4418");
		String pos = " 1; 10 10, 10 10";
		byte[] byPosicion = Ciph.cifrar(pos.getBytes(), serverSecretKey, simetrico);

		// Digest
		byte[] hashPosicion = Ciph.macHash(pos.getBytes(), serverSecretKey, hmac);
		byte[] chashPosicion = Ciph.cifrar(hashPosicion, serverPublicKey, asimetrico);

		String cpos = HexConverter.toHEX(byPosicion);
		String hpos = HexConverter.toHEX(chashPosicion);
		escritor.println(cpos);
		escritor.println(hpos);
		tiempoEscrituraOk = System.currentTimeMillis();
		System.out.println("Se envio la posici�n e id");
		String lin = lector.readLine();
		if (!lin.equals("ERROR")) {
			System.out.println("Recibio exitosamente");
		}
		
	}

	// Inner Class Helpers
	public String leerDelServidor(BufferedReader lector) {
		String fromServer = "";
		try {
			if((fromServer = lector.readLine()) != null) {
				System.out.println("Servidor: " +fromServer);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return fromServer;
	}
}
