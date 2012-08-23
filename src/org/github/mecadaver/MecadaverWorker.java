package org.github.mecadaver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

public class MecadaverWorker implements Runnable {

	/**
	 * Number of iterations of navigation.
	 * One iteration = going to one folder and doing a few file operations.
	 */
	private int nbIterations;
	
	/**
	 * Prefix for the WebDAV connection, for instance "http://localhost:8080"
	 */
	private String prefix;

	/**
	 * Start path for the WebDAV connection, for instance "/alfresco/webdav"
	 */
	private String startPath;

	/**
	 * Name of the WebDAV user, kept for debugging purposes.
	 */
	private String username;
	
	/**
	 * Time to wait between each WebDAV request.
	 * In milliseconds.
	 */
	private int pause;
	
	/**
	 * Object that handles the WebDAV connection, see https://code.google.com/p/sardine
	 */
	private Sardine sardine;

	/**
	 * Constructor.
	 */
	public MecadaverWorker(
			String username,
			String password,
			String prefix,
			String startPath,
			int nbIterations,
			int pause
			) throws IOException {
		this.prefix = prefix;
		this.startPath = startPath;
		this.nbIterations = nbIterations;
		this.username = username;
		this.pause = pause;
		
		// Log in to Kerberos.
//		LoginContext lc;
//		try {
//		    lc = new LoginContext(MecadaverWorker.class.getName(),
//		    		new LoginCallbackHandler( username, password ));
//
//		    // Attempt authentication
//		    // You might want to do this in a "for" loop to give
//		    // user more than one chance to enter correct username/password
//		    lc.login();
//
//		} catch (LoginException le) {
//		    System.err.println("Authentication attempt failed" + le);
//		    System.exit(-1);
//		}
		
//	    System.setProperty("java.security.auth.login.config", "/myDir/jaas.conf");
//	    System.setProperty("java.security.krb5.conf", "/etc/krb5/krb5.conf");
//	    System.setProperty("java.security.krb5.realm", "ENG.TEST.COM");
//        System.setProperty("java.security.krb5.kdc","winsvr2003r2.eng.test.com");
//
//	    boolean success = /*auth.*/KerberosAuthenticator.authenticate("testprincipal", "testpass");
//
//	    System.out.println(success);

		
		// Connect to WebDAV.
		//sardine = KerberosSardineFactory.begin();
		sardine = SardineFactory.begin(username, password);
	}

	/**
	 * Implements Runnable.
	 * @Override
	 */
	public void run() {
		// Navigate.
		String url = prefix + startPath;
		for (int i = 0; i < nbIterations; i++) {
			try {
				url = navigate(url, sardine);
				pause();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Perform a few operations, then navigate to another directory.
	 */
	public String navigate(String url, Sardine sardine) throws IOException {
		// TODO 1% of the time, upload a new document.
		
		List<DavResource> resources = sardine.list(url);
		resources.remove(0); // Remove the first element, which is the directory itself.
		Collections.shuffle(resources); // Shuffle so that we don't always navigate the same way.
		boolean atLeastOneDirectory = false;
		for (DavResource resource : resources) {
			// directory => go in
			// file => Sometimes download
			// no directory => go home
			if (resource.isDirectory()) {
				url = url + "/" + safeResourceName(resource);
				System.out.println("[" + username + "] Navigate to " + url);
				atLeastOneDirectory = true;
				break;
			}
			else {
				// 30% of the time, download the file.
				if (chance(30)) {
					System.out.println("[" + username + "] Download file " + resource);
					InputStream inputStream = sardine.get(url + "/" + safeResourceName(resource));
					// Consume the input stream.
					byte[] bytes = new byte[1024];
					while ((inputStream.read(bytes)) != -1);
					inputStream.close();
					pause();
				}
				// TODO 1% of the time, modify the file.
			}
		}
		if ( ! atLeastOneDirectory) {
			// If no directory here, then go up all the way to home.
			url = prefix + startPath;
			System.out.println("[" + username + "] Back to  " + url);
		}
		return url;
	}
	
	/**
	 * Wait for some time.
	 * Duration is the duration that has been defined in constructor.
	 */
	private void pause() {
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Dice.
	 * @param probability that the output is true, in percent.
	 */
	private final static boolean chance(int probability) {
		return new Random().nextInt(100) < probability;
	}

	/**
	 * Make a resource name URL-safe.
	 */
	private final static String safeResourceName(DavResource resource) throws UnsupportedEncodingException {
		return URLEncoder.encode(resource.getName(), "UTF-8");
	}
}