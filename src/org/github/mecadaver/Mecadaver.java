package org.github.mecadaver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Mecadaver: Automatic tool for WebDAV load testing.
 * @author Nicolas Raoul
 */
public class Mecadaver {
	
	/**
	 * Constructor.
	 */
	public Mecadaver(
			String prefix,
			String startPath,
			String credentialsFilename,
			int nbIterations,
			int pause
			) throws IOException {
		
		// Read users list.
		List<String> users = new ArrayList<String>();
		List<String> passwords = new ArrayList<String>();
		try{
			  FileInputStream fstream = new FileInputStream(credentialsFilename);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String line;
			  while ((line = br.readLine()) != null)   {
				  String[] tokens = line.split("\t");
				  users.add(tokens[0]);
				  passwords.add(tokens[1]);
			  }
			  in.close();
		} catch (Exception e){
			  e.printStackTrace();
		}

		// Start a thread for each user. 
		int i = 0;
		for (String user : users) {
			String password = passwords.get(i++);
			new Thread(new MecadaverWorker(user, password, prefix, startPath, nbIterations, pause)).start();
		}
	}
	
	/**
	 * Main application.
	 */
	public static void main(String[] args) throws IOException {
		String prefix = args.length > 0 ? args[0] : "http://localhost:8080";
		String startPath = args.length > 1 ? args[1] : "/alfresco/webdav";
		String credentialsFilename = args.length > 2 ? args[2] : "credentials.tsv";
		int nbIterations = args.length > 3 ? Integer.decode(args[3]) : 50;
		int pause = args.length > 4 ? Integer.decode(args[4]) : 20;
		new Mecadaver(prefix, startPath, credentialsFilename, nbIterations, pause);
	}
}
