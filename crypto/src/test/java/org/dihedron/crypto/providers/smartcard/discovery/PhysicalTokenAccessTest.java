/**
 * Copyright (c) 2012-2014, Andrea Funto'. All rights reserved.
 * 
 * This file is part of the Crypto library ("Crypto").
 *
 * Crypto is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 *
 * Crypto is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with Crypto. If not, see <http://www.gnu.org/licenses/>.
 */
package org.dihedron.crypto.providers.smartcard.discovery;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.dihedron.core.os.Platform;
import org.dihedron.core.os.files.FileFinder;
import org.dihedron.core.os.modules.ImageFile;
import org.dihedron.core.os.modules.ImageFile.Addressing;
import org.dihedron.core.os.modules.ImageFile.Format;
import org.dihedron.core.os.modules.ImageFile.OperatingSystem;
import org.dihedron.core.os.modules.ImageFileParser;
import org.dihedron.core.os.modules.ImageParseException;
import org.dihedron.core.streams.Streams;
import org.dihedron.core.url.URLFactory;
import org.dihedron.crypto.CryptoService;
import org.dihedron.crypto.KeyRing;
import org.dihedron.crypto.certificates.Certificates;
import org.dihedron.crypto.certificates.TrustAnchors;
import org.dihedron.crypto.constants.SignatureAlgorithm;
import org.dihedron.crypto.exceptions.CertificateVerificationException;
import org.dihedron.crypto.exceptions.ProviderException;
import org.dihedron.crypto.operations.sign.Signer;
import org.dihedron.crypto.operations.sign.SignerFactory;
import org.dihedron.crypto.operations.sign.SignerFactory.Type;
import org.dihedron.crypto.providers.AutoCloseableProvider;
import org.dihedron.crypto.providers.smartcard.SmartCardKeyRing;
import org.dihedron.crypto.providers.smartcard.SmartCardProviderFactory;
import org.dihedron.crypto.providers.smartcard.SmartCardTraits;
import org.dihedron.crypto.ui.PINDialog;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NOTE: this test should only be run if you have a physical token attached to your PC.
 * 
 * @author Andrea Funto'
 */

public class PhysicalTokenAccessTest {

	/**
	 * The logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
	
	@Before
	public void setUp() throws IOException {
		Security.addProvider(new BouncyCastleProvider());
		
		// if on Linux (what about MacOS-X?) I need to load the libpcsclite library 
		// otherwise the PKCS#11 support will throw an exception as soon as loaded
		ImageFileParser parser = null;
		
		// ... if only I had lambdas! 
		switch(Platform.getCurrent()) {
		case LINUX_32:
			parser = ImageFileParser.makeParser(Format.ELF);
			for(File file : FileFinder.findFile("libpcsclite.*", true, new String[]{ "/lib/i386-linux-gnu/", "/usr/local/lib/" })) {
				try {
					ImageFile module = parser.parse(file);
					logger.trace("module: {}", module.toJSON());
					if(module.getAddressing() == Addressing.SIZE_32 && (module.getOperatingSystem() == OperatingSystem.LINUX || module.getOperatingSystem() == OperatingSystem.SYSTEM_V)) {
						// make the library accessible to the JVM
						logger.info("making libpcsclite accessible from file at {}", file.getCanonicalPath());
						System.setProperty("sun.security.smartcardio.library", file.getCanonicalPath());
						break;
					}
				} catch(IOException | ImageParseException e) {
					logger.error("error parsing image at " + file.getCanonicalPath(), e);
				}
			}
			break;
		case LINUX_64:
			parser = ImageFileParser.makeParser(Format.ELF);
			for(File file : FileFinder.findFile("libpcsclite.*", true, new String[]{ "/lib/x86_64-linux-gnu/", "/usr/local/lib/" })) {
				try {
					ImageFile module = parser.parse(file);
					logger.trace("module: {}", module.toJSON());
					if(module.getAddressing() == Addressing.SIZE_64 && (module.getOperatingSystem() == OperatingSystem.LINUX || module.getOperatingSystem() == OperatingSystem.SYSTEM_V)) {
						// make the library accessible to the JVM
						logger.info("making libpcsclite accessible from file at {}", file.getCanonicalPath());
						System.setProperty("sun.security.smartcardio.library", file.getCanonicalPath());
						break;
					}
				} catch(IOException | ImageParseException e) {
					logger.error("error parsing image at " + file.getCanonicalPath(), e);
				}
			}
			break;
		default:
			logger.trace("no need to lookup libpcsclite");
			break;
		}		
	}
	
	@Test
	@Ignore
	public void testSmartCardTraits() throws ProviderException, IOException {
		assertTrue(getSmartCardTraits() != null);
	}
		
	@Test
	@Ignore
	public void testTrustAnchors() throws MalformedURLException {
		List<X509Certificate> certificates = new ArrayList<>();
		
		long start = System.currentTimeMillis();
//		certificates.addAll(TrustAnchors.fromTSL("https://applicazioni.cnipa.gov.it/TSL/IT_TSL_signed.xml"));
		certificates.addAll(TrustAnchors.fromTSL("classpath:org/dihedron/crypto/certificates/tsl/DIGITPA-20141015.xml"));
		logger.trace("size: {} (took {} ms)", certificates.size(), System.currentTimeMillis() - start);
	}
	
	@Test
	@Ignore
	public void testSmartCardProvider() throws Exception {
		SmartCardTraits traits = getSmartCardTraits();
		assertTrue(traits != null);
		try(AutoCloseableProvider provider = new SmartCardProviderFactory().getProvider(traits)) {
			assertTrue(provider != null);
			
			for(Provider p : Security.getProviders()) {
				logger.trace("installed provider: '{}'", p.getName());
			}
			
			logger.trace("name of the provider: '{}'", provider.getName());
			
			String password = new PINDialog("Please enter PIN", "SmartCard '" + traits.getSmartCard().getDescription() + "' in reader '" + traits.getReader().getDescription() + "'").getPIN();
			try(KeyRing keyring = new SmartCardKeyRing(provider, password)) {
				for(String alias : keyring.enumerateAliases()) {
					logger.trace("alias: '{}'", alias);
				}
				
				for(String alias : keyring.getSignatureKeyAliases()) {
					logger.trace("signature alias: '{}'", alias);
				}
				
			}
		}
	}
	
	/**
	 * @param args
	 */
	@Test
//	@Ignore
	public void testLoadCertificates() throws Exception {
		
		String password = new PINDialog("Please enter PIN", "SmartCard model unknown").getPIN();
		
		try (AutoCloseableProvider provider = new SmartCardProviderFactory().getProvider(getSmartCardTraits()); KeyRing keyring = new SmartCardKeyRing(provider, password)) {
			if(provider == null) {
				logger.warn("no smart card available, aborting test");
				return;
			}
			
			Collection<X509Certificate> trustAnchors = TrustAnchors.fromJavaRootCAs();
			
			trustAnchors.addAll(TrustAnchors.fromTSL("classpath:org/dihedron/crypto/certificates/tsl/DIGITPA-20141015.xml"));
//			trustAnchors.addAll(TrustAnchors.fromTSL("https://applicazioni.cnipa.gov.it/TSL/IT_TSL_signed.xml"));
						
			for(String alias : keyring.getSignatureKeyAliases()) {
				logger.info("signature alias: '{}'", alias);
				X509Certificate certificate = (X509Certificate)keyring.getCertificate(alias);
				
				List<X509Certificate> certificates = new ArrayList<>();
				certificates.addAll(trustAnchors);
				for(Certificate c : keyring.getCertificateChain(alias)) {
					certificates.add((X509Certificate)c);
				}				
			
				PKIXCertPathBuilderResult verified = Certificates.verifyCertificate(certificate, certificates);
				
				// dump certification path
				logger.info("certification path: ");
				for(Certificate step : verified.getCertPath().getCertificates()) {
					logger.info("step in certification path:\n{}", step);
				}
				// dump trust anchor
				logger.info("trust anchor: '{}'\n{}", verified.getTrustAnchor().getCAName(), verified.getTrustAnchor().getTrustedCert());
				// dump verified certificate
				logger.info("public key:\n{}", verified.getPublicKey());
				
				Signer signer = SignerFactory.makeSigner(Type.PKCS7, alias, keyring, provider, SignatureAlgorithm.SHA256_WITH_RSA);				
				try(InputStream input = URLFactory.makeURL("classpath:org/dihedron/crypto/data/tutorial.pdf").openStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
					signer.sign(input, output);
					
					logger.trace("written {} bytes to (signed output) output buffer", output.size());
					
					byte[] data = Arrays.clone(output.toByteArray());
					logger.trace("cloned byte array has a size of {} bytes", data.length);
					try(InputStream signed = new ByteArrayInputStream(data); OutputStream fos = new FileOutputStream("tutorial.pdf.p7e")) {
						Streams.copy(signed, fos);
					}
					
					try(InputStream signed = new ByteArrayInputStream(data)) {
						if(signer.verify(signed)) {
							logger.info("data verified");
						} else {
							logger.error("error verifying data");
						}
					}
				}
			}
		} catch(CertificateVerificationException e) {
			logger.warn("the certificate has expired or is not valid (CRL)");
		} 
	}	
	
	private SmartCardTraits getSmartCardTraits() throws IOException, ProviderException {
		DataBase database = DataBaseLoader.load();			
		
		List<Reader> readers = new ArrayList<Reader>();
		while(true) {
			readers.clear();
			for(Reader reader : Readers.enumerate()) {
				logger.trace("reader:\n{}", reader);
				if(reader.hasSmartCard()) {
					readers.add(reader);
				}
			}
			if(readers.size() == 1) {
				SmartCard smartcard = database.get(readers.get(0).getATR());
				logger.trace("selected smartcard:\n{}", smartcard);				
				return new SmartCardTraits(readers.get(0), smartcard);
			} else {
				logger.warn("no readers have a smart card available");
				break;
			}
		}
		return null;
	}	
}
