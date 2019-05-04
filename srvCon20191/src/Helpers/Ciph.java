package Helpers;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@SuppressWarnings("deprecation")
public class Ciph {

	public static byte[] cifrar(byte[] aCifrar, Key llaveCifrar, String algoritmo) throws Exception
	{
		algoritmo = algoritmo + ((algoritmo.equals("Blowfish")) || (algoritmo.equals("AES")) ? "/ECB/PKCS5Padding" : "");
		System.out.println(algoritmo);
		Cipher localCipher = Cipher.getInstance(algoritmo);
		localCipher.init(Cipher.ENCRYPT_MODE, llaveCifrar);
		return localCipher.doFinal(aCifrar);
	}

	public static byte[] descifrar(byte[] aDescifrar, Key llaveDescifrar, String algoritmo) throws Exception
	{
		algoritmo = algoritmo + ((algoritmo.equals("Blowfish")) || (algoritmo.equals("AES")) ? "/ECB/PKCS5Padding" : "");
		Cipher localCipher = Cipher.getInstance(algoritmo);
		localCipher.init(Cipher.DECRYPT_MODE, llaveDescifrar);
		return localCipher.doFinal(aDescifrar);
	}

	public static byte[] macHash(byte[] aHash, Key llaveHash, String algoritmo) throws Exception
	{
		Mac localMac = Mac.getInstance(algoritmo);
		localMac.init(llaveHash);
		byte[] arrayOfByte = localMac.doFinal(aHash);
		return arrayOfByte;
	}

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
