package de.falco.essentialsxxx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/*
 * server for table id's hopefully efficient and usable :)
 * 
 * @author FalcoW
 * @copyright Phoenixgames.net
 * @version 1.0
 */
public class Main extends Thread{
	
	//prefix
	private final static String prefix = "[essentialsxxx-IdManager]";
	
	//console types
	private static ServerSocket server;
	
	private static BufferedReader reader;
	private static PrintWriter writer;
	
	//tablename where the data will save after server-stopping
	private static String tablename = null;
	
	//mysql connection where to save data after server-stopping
	private static MySql mysql;
	
	//objects to save data and load data dynamicly (ids)
	private static Map<String,Integer> lastnumbers = new LinkedHashMap<>();
	private static Map<String,ArrayList<Integer>> oldnumbers = new LinkedHashMap<>();
	
	
	/*
	 * start method
	 */
	public static void main(String args[]){
		
		//check if the class is available you need for the mysql connection
		try {
			Class.forName( "com.mysql.cj.jdbc.Driver" );
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		
		reader = new BufferedReader(new InputStreamReader(System.in));
		writer = new PrintWriter(System.out);
		
		
		writer.write(prefix + " start programm... \n");
		writer.flush();
		
		//sleep
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
			Runtime.getRuntime().halt(250);
		}
		
		mysql = loadmysql();//load mysql to load old data later
		
		//sleep
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
			Runtime.getRuntime().halt(250);
		}
		
		writer.write(prefix + " in which table is our data? \n");
		writer.flush();
		
		
		try {
			tablename = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Main.createtableDefault(tablename, mysql);//create a table for the data you have to save after a crash
		
		Main.loadolddata();//load old data
		
		writer.write(prefix + " which port you want to use for the server? \n");
		writer.flush();
		
		int port = 0;//port for the serversocket
		
		try {
			port = Integer.parseInt(reader.readLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		//sleep
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			Runtime.getRuntime().halt(250);
		}
		
		
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		writer.write(prefix + " server started port " + port + " wait for response \n");
		writer.flush();
		
		Thread t = new Main();
		Runtime.getRuntime().addShutdownHook(t);//add shutdownhook for server-crash
		
		
		while(true) {
			
			
			Socket so = null;
			
			try {
				so = server.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			long t3 = System.currentTimeMillis();
			
			BufferedReader soreader = null;
			PrintWriter sowriter = null;
			try {
				soreader = new BufferedReader(new InputStreamReader(so.getInputStream()));
				sowriter = new PrintWriter(so.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			writer.write(prefix + " client connected\n");
			writer.flush();
			
			
			String command = null;
			try {
				command = soreader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println(prefix + " command " + command);
			
			if(command.equals("wantnumbers")) {
				wantnumbers(so,soreader,sowriter);
			}else if(command.equals("registertable")) {
				registertable(so,soreader);
			}else if(command.equals("addoldnumbers")) {
				addoldnumbers(so,soreader);
			}else {
				System.out.println(prefix + " unknown command? This could be a hacker attacke. I recommend to close all connections and this server");
				
			}
			
			writer.write(prefix + " try to close the connection\n");
			
			long t5 = System.currentTimeMillis();
			
			try {
				so.close();
				soreader.close();
				sowriter.close();
				
				long t6 = System.currentTimeMillis();
				
				System.out.println(prefix + " successfully close the connection (" + (t6-t5) + ")");
				
			} catch (IOException e) {
				
				
				e.printStackTrace();
			
				long t6 = System.currentTimeMillis();
				System.out.println(prefix + " error while try to close the connection (" + (t6-t5) + ")");
			}
			
			
			long t4 = System.currentTimeMillis();
			
			writer.write(prefix + " client dissconnect succesful Time (" + (t4-t3) + ") command: " + command + " \n");
			writer.flush();
			
			
			/*
			writer.write(prefix + " exit?");
			writer.flush();
			
			String exit = reader.readLine();
			
			if(exit.equals("exit")) {
				System.exit(0);
			}
			*/
			
			
			
		
			
		}
			
			
			
			
		
		
	}
	
	
	/*
	 * add old numbers with you want to get later 
	 * 
	 * @param Socket of the connection
	 * @param soreader
	 */
	public static void addoldnumbers(Socket so, BufferedReader soreader) {
		
		long t1 = System.currentTimeMillis();
		
		String tablename = null;
		
		try {
			tablename = soreader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(prefix + " error while reading the tablename. This could be a hacker attack. I recommend to close all connections!");
			return;
		}
		
		if(!Main.oldnumbers.containsKey(tablename)) {
			ArrayList<Integer> tmp = new ArrayList<>();
			Main.oldnumbers.put(tablename, tmp);
		}
		
		String numbers = null;
		try {
			numbers = soreader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(prefix + " error while reading the tablename. This could be a hacker attack. I recommend to close all connections!");
			return;
		}
		
		
		System.out.println(prefix + " tablename: " + tablename + " numbers: " + numbers);
		
		try {
			if(numbers != null) {
				for(String i : numbers.split("§")) {
					int number = Integer.parseInt(i);
					if(number < Main.lastnumbers.get(tablename)) {
						Main.oldnumbers.get(tablename).add(number);	
					}
				}	
			}	
		}catch(NumberFormatException ex) {
			ex.printStackTrace();
		}
		
		long t2 = System.currentTimeMillis();
		
		System.out.println(prefix + " successfully add old numbers (" + (t2-t1) + ")");
		
		/*
		for(Object line : soreader.lines().toArray()) {
			System.out.println(line);
			
			String numbers = (String) line;
			
			for(String i : numbers.split("§")) {
				
				try {
					int number = Integer.parseInt(i);
					Main.oldnumbers.get(tablename).add(number);
					
				}catch(NumberFormatException ex) {
					ex.printStackTrace();
				}
				
			}
		}
		*/
		
		/*
		*/
		
	}
	
	/*
	 * command execute in main when a mc-plugin want new ids
	 * 
	 * @param Socket of the connection
	 * @param soreader BufferdReader of the connection
	 * @param sowriter PrintWriter of the connection
	 */
	public static void wantnumbers(Socket so, BufferedReader soreader, PrintWriter sowriter) {
		
		long t1 = System.currentTimeMillis();
		
		String tablename = null;
		int length = 0;
		
		try {
			tablename = soreader.readLine();
			length = Integer.parseInt(soreader.readLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println(prefix + " error while reading the length of the numbers. This could be a hacker attack. I recommend to close all connections!");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(prefix + " error while reading the length of the numbers. This could be a hacker attack. I recommend to close all connections!");
			return;
		}//
		
		System.out.println(prefix + " tablename: " + tablename + " length: " + length);
		
		ArrayList<Integer> builder = new ArrayList<>();
		
		for(int x = 0; x < oldnumbers.get(tablename).size(); x++) {
			
			if(!(x+1 ==  length)) {
				break;
			}
			
			builder.add(oldnumbers.get(tablename).get(x));
			
			
			oldnumbers.get(tablename).remove(x);
			
		}
		
		int tmp = builder.size();
		
		
		if(tmp != length) {
			
			int rest = length - tmp;
			
			int aktuell = Main.lastnumbers.get(tablename);
			
			
			for(int x = aktuell + 1; x < (aktuell + rest) + 1; x++) {
				//System.out.print(x + "/");
				builder.add(x);
			}
			
			Main.lastnumbers.put(tablename,Main.lastnumbers.get(tablename) + rest);
			
		}
		
		StringBuilder end = new StringBuilder();
		
		for(int i : builder) {
			end.append("§" + i);
		}
		
		System.out.println(prefix + " result: " + end.toString().replaceFirst("§", ""));
		
		sowriter.write(end.toString().replaceFirst("§", ""));
		sowriter.flush();
		
		long t2 = System.currentTimeMillis();
		
		System.out.println(prefix + " successfully execute command wantnumbers (" + (t2-t1) + ")");
		
		
	}
	
	/*
	 * register table so there is a entry in the field
	 * 
	 * @param Socket of the connection
	 * @param BufferedReader of the Socket
	 */
	public static void registertable(Socket so, BufferedReader soreader) {
		
		long t1 = System.currentTimeMillis();
		
		String host = null;
		String port = null;
		String database = null;
		String user = null;
		String pw = null;
		String tablename = null;
		try {
			host = soreader.readLine();
			port = soreader.readLine();
			database = soreader.readLine();
			user = soreader.readLine();
			pw = soreader.readLine();
			
			tablename = soreader.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println(prefix + " error while reading the length of the numbers. This could be a hacker attack. I recommend to close all connections!");
			return;
		}
		
		System.out.println(prefix + " host: " + host + " port: " + port + " database: " + database + " user: " + user + " pw: " + pw + " tablename: " + tablename);
		
		MySql mysql = new MySql(host, port, database, user, pw);
		
		if(mysql.isconnect() == false) {
			mysql.connect();
		}
		
		if(mysql.getCon() == null) {
			System.out.println(prefix + " couldnt connect to database ");
			return;
		}else {
			System.out.println(prefix + " connect to database ");
		}
		
			
		ResultSet result = mysql.getResult("SELECT id FROM `" + tablename + "` ORDER BY `id`");
			
		int last = 0;
			
		try {
			if(result.last()) {
				last = result.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		lastnumbers.put(tablename, last);
		
		
		if(oldnumbers.containsKey(tablename) == false) {
			ArrayList<Integer> tmp = new ArrayList<>();
			oldnumbers.put(tablename, tmp);
		}
		
		long t2 = System.currentTimeMillis();
		
		System.out.println(prefix + " successfully register table " + tablename + " last index: " + last + " (" + (t2-t1) + ")");
			
			
		
		
		
	}
	
	
	/*
	 * save the current data so you can load it back later
	 */
	public void run() {
		
		long t1 = System.currentTimeMillis();
		
		System.out.println(prefix + " try to close all connections");
		
		Main.writer.close();
		try {
			Main.server.close();
			Main.reader.close();
			
			long t2 = System.currentTimeMillis();
			
			System.out.println(prefix + " successfully close all connections! (" + (t2-t1) + ")");
		} catch (IOException e2) {
			e2.printStackTrace();
			long t2 = System.currentTimeMillis();
			System.out.println(prefix + " error while trying to close all connections? (" + (t2-t1) + ")");
		}
		
		long t3 = System.currentTimeMillis();
		
		System.out.println(prefix + " try to save data");
		
		for(String key : lastnumbers.keySet()) {
			
			long t5 = System.currentTimeMillis();
			
			System.out.println(prefix + " try to save data from " + key);
			
			int last = lastnumbers.get(key);
			ArrayList<Integer> old = oldnumbers.get(key);
			
			
			String oldend = null;
			
			if(old.isEmpty() == false) {
				
				StringBuilder builder = new StringBuilder();
				for(int x : old) {
					builder.append("§" + x);
				}
				
				oldend = "'" + builder.toString().replaceFirst("§", "") + "'";
				
			}
			
			String command = "SELECT id FROM " + tablename + " WHERE tablename='" + key + "';";
			
			ResultSet result = mysql.getResult(command);
			
			try {
				if(result.next()) {
					
					int id = result.getInt(1);
					
					command = "UPDATE " + tablename + " "
							+ "SET number=" + last + ", old=" + oldend + " "
							+ "WHERE id=" + id;
					mysql.command(command);
					
					long t6 = System.currentTimeMillis();
					
					System.out.println(prefix + " succesfully saved " + key + " (" + (t6-t5) + ")");
					
				}else {
					
					command = "SELECT id FROM " + tablename + " ORDER BY `id`;";
					
					ResultSet re = mysql.getResult(command);
						
					int number = 0;
						
					try {
						if(re.last()) {
							number = re.getInt(1) + 1;
						}
					} catch (SQLException | NullPointerException e) {
						e.printStackTrace();
					}
						
						
					command = "INSERT INTO " + tablename + " "
							+ "(id,tablename,number,old) VALUES ("
							+ "" + number
							+ ",'" + key + "'"
							+ "," + last
							+ "," + oldend
							+ ");";
					mysql.command(command);
					
					long t6 = System.currentTimeMillis();
					
					System.out.println(prefix + " succesfully saved " + key + " (" + (t6-t5) + ")");
					
				}
				
				
			} catch (SQLException | NullPointerException e1) {
				e1.printStackTrace();
				
				long t6 = System.currentTimeMillis();
				
				System.out.println(prefix + " error while try to save " + key + " (" + (t6-t5) + ")");
			}
			
			
		}//for
		
		long t4 = System.currentTimeMillis();
		
		System.out.println(prefix + " saved all tables (" + (t4-t3) + ")");
		
	}
	
	/*
	 * mysql setup
	 * 
	 * @return mysql connection
	 */
	public static MySql loadmysql() {
		
		String host = null;
		String user = null;
		String pw = null;
		String port = null;
		String database = null;
		
		
		writer.write(prefix + " mysql-setup started \n");
		writer.flush();
		
		File file = new File("mysql.properties");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		Properties pro = new Properties();
		try {
			pro.load(new FileInputStream(file));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//host
		if(pro.containsKey("host")) {
			host = pro.getProperty("host");
		}else {
			writer.write(prefix + " your host (z.B. localhost) \n");
			writer.flush();
			
			try {
				host = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				throw new NullPointerException(prefix + " unknown error while reading hostname");
			}
			
			
			writer.write(prefix + " host => " + host + "\n");
			writer.flush();	
		}
		
		//port
		if(pro.containsKey("port")) {
			port = pro.getProperty("port");
		}else {
			writer.write(prefix + " you port (z.B. 3306) \n");
			writer.flush();
			
			try {
				port = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				throw new NullPointerException(prefix + " unknown error while reading port");
			}
			
			
			writer.write(prefix + " port => " + port + "\n");
			writer.flush();	
		}
		
		//database
		if(pro.containsKey("database")) {
			database = pro.getProperty("database");
		}else {
			writer.write(prefix + " your database \n");
			writer.flush();
			
			try {
				database = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				throw new NullPointerException(prefix + " unknown error while reading database");
			}
			
			
			writer.write(prefix + " database => " + database + "\n");
			writer.flush();	
		}
		
		//user
		if(pro.containsKey("user")) {
			user = pro.getProperty("user");
		}else {
			writer.write(prefix + " your user \n");
			writer.flush();
			
			try {
				user = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				throw new NullPointerException(prefix + " unknown error while reading user");
			}
			
			
			writer.write(prefix + " user=> " + user + " \n");
			writer.flush();	
		}
		
		//pw
		if(pro.containsKey("pw")) {
			pw = pro.getProperty("pw");
		}else {
			writer.write(prefix + " you pw \n");
			writer.flush();
			
			try {
				pw = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			writer.write(prefix + " pw => " + pw + "\n\n");	
		}
		
		
		//connect
		writer.write(prefix + " try to connect to " + host + ":" + port + "/" + database + "\n" );
		writer.write(prefix + " user:" + user + " pw:'" + pw + "'\n");
		writer.flush();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Runtime.getRuntime().halt(500);
		}
		
		MySql re = new MySql(host, port, database, user, pw);
		
		if(re.isconnect() == false) {
			re.connect();
		}
		
		if(re.getCon() != null) {
			System.out.println(prefix + " connected to database");
			pro.setProperty("host", host);
			pro.setProperty("port", port);
			pro.setProperty("user", user);
			pro.setProperty("pw", pw);
			pro.setProperty("database", database);
			
			try {
				pro.store(new PrintWriter(file), "data");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else {
			System.out.println(prefix + " couldnt connect to database");
		}
		
		
		
		return re;
		
	}
	
	/*
	 * load the old data
	 * 
	 * @return time it takes
	 */
	public static long loadolddata() {
		
		long t1 = System.currentTimeMillis();
		
		System.out.println(prefix + " load the old data from table: " + tablename);
		
		String command = "SELECT * FROM `" + tablename + "`;";
		
		ResultSet result = mysql.getResult(command);
		try {
			while(result.next()) {
				
				
				String tablename = result.getString(2);
				int number = result.getInt(3);
				String oldnumbers = result.getString(4);
				
				
				Main.lastnumbers.put(tablename, number);
				
				ArrayList<Integer> old = new ArrayList<Integer>();
				
				if(oldnumbers != null) {
					
					
					for(String i : oldnumbers.split("§")) {
						try {
							old.add(Integer.parseInt(i));
						}catch(NumberFormatException ex) {
							ex.printStackTrace();
						}
					}
					
				}
				
				Main.oldnumbers.put(tablename, old);
				
				System.out.println(prefix + " load " + tablename + " last: " + number + " old:" + old);
				
			}
			
			
			System.out.println(prefix + " success!");
			
			long t2 = System.currentTimeMillis();
			
			return (t2-t1);
			
		} catch (SQLException | NullPointerException e) {
			e.printStackTrace();
		}
		
		System.out.println(prefix + " couldnt load the data maybe empty?");
		
		long t2 = System.currentTimeMillis();
		
		return (t2-t1);
		
	}

	/*
	 * create the default table, where you could save data after a server crash
	 * 
	 * @param tablename
	 * @param mysql connection
	 * @return time it takes
	 */
	public static long createtableDefault(String tablename, MySql mysql) {
		
		long t1 = System.currentTimeMillis();
		
		String command = "CREATE TABLE IF NOT EXISTS `" + tablename + "` "
				+ "("
				+ "id int Primary Key"
				+ ",tablename TEXT"
				+ ",number INT"
				+ ",old TEXT"
				+ ");";
		mysql.command(command);
		
		long t2 = System.currentTimeMillis();
		
		return (t2-t1);
		
	}
	
	/*
	 * old
	 */
	
	/*
	public static void save() {
		
		for(String tablename : lastnumbers.keySet()) {
			
			int last = lastnumbers.get(tablename);
			
			StringBuilder builder = new StringBuilder();
			
			for(int all : oldnumbers.get(tablename)) {
				builder.append("§" + all);
			}
			
			String old = builder.toString().replaceFirst("§", "");
			
			ResultSet result = mysql.getResult("SELECT id FROM " + Main.tablename + " WHERE `tablename`=`" + tablename + "`");
			
			try {
				if(result.next()) {
					
					int id = result.getInt(1);
					
					mysql.command("UPDATE `" + Main.tablename + "` SET `number`=" + last + " , `old`=`" + old + "` WHERE `id`=" + id);
					
					return;
					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			int id = 0;
			
			ResultSet re = mysql.getResult("SELECT id FROM " + tablename + " ORDER BY `id`");
			try {
				if(re.last()) {
					id = re.getInt(1) + 1;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			mysql.command("INSERT INTO " + Main.tablename + " "
					+ "(id,tablename,number,old) VALUES ("
					+ "" + id
					+ "," + tablename
					+ "," + last
					+ "," + old
					+ ");");
			
		}
		
	}
	
	public static void load() {
		
		ResultSet result = mysql.getResult("SELECT tablename,number,old FROM " + tablename);
		try {
			while(result.next()) {
				String t = result.getString(2);
				int number = result.getInt(3);
				String old = result.getString(4);
				
				
				lastnumbers.put(t, number);
				
				ArrayList<Integer> oldnumberlist = new ArrayList();
				
				for(String i : old.split("§")) {
					int tmp = Integer.parseInt(i);
					oldnumberlist.add(tmp);
				}
				
				//Map<String,ArrayList<Integer>> tt = new LinkedHashMap<>();
				
				oldnumbers.put(t, oldnumberlist);
				
				
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	*/
	
}
