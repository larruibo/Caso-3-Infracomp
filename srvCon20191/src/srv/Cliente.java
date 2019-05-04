package srv;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import Helpers.Ciph;

public class Cliente {

	// Constantes
		public final static String PROVEEDOR = "BC";
		public final static String COMIENZO_CERTIFICADO = "-----BEGIN CERTIFICATE-----";
		public final static String FINAL_CERTIFICADO = "-----END CERTIFICATE-----";

		public X509Certificate generarCertificado(KeyPair parLlaves) throws Exception {
			X509Certificate certificado = Ciph.crearCertificado(parLlaves, PROVEEDOR);
			return certificado;
		}

		public X509Certificate leerCertificadoDeString(String certificado) throws Exception {
			StringReader lectorString = new StringReader(certificado);
			PemReader pemReader = new PemReader(lectorString);
			PemObject pemObject = pemReader.readPemObject();
			X509CertificateHolder holderCertificado = new X509CertificateHolder(pemObject.getContent());
			X509Certificate localX509Certificate = new JcaX509CertificateConverter().getCertificate(holderCertificado);
			pemReader.close();
			return localX509Certificate;
		}

		public String certificateToString(X509Certificate certificate) throws Exception {
			StringWriter escritorString = new StringWriter();
			JcaPEMWriter pemEscritor = new JcaPEMWriter(escritorString);
			pemEscritor.writeObject(certificate);
			pemEscritor.flush();
			pemEscritor.close();
			String certificadoString = escritorString.toString();
			return certificadoString;
		}
}
