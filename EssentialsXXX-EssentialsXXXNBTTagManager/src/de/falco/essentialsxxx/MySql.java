package de.falco.essentialsxxx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySql {
	
  private Connection con;
  private String host;
  private String port;
  private String database;
  private String user;
  private String pw;
  
  public MySql(String host, String port, String database, String user, String pw) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.user = user;
    this.pw = pw;
  }
  
  public int connect() {
    if (!isconnect())
      try {
        this.con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.pw);
        return 0;
      } catch (SQLException e) {
        e.printStackTrace();
        return 1;
      }  
    return 0;
  }
  
  public int disconnect() {
    if (isconnect())
      try {
        this.con.close();
        return 0;
      } catch (SQLException e) {
        e.printStackTrace();
        return 1;
      }  
    return 1;
  }
  
  public boolean isconnect() {
    if (this.con == null)
      return false; 
    return true;
  }
  
  public void command(String sql) {
    try {
      PreparedStatement state = this.con.prepareStatement(sql);
      state.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    } 
  }
  
  public ResultSet getResult(String sql) {
    try {
      PreparedStatement state = this.con.prepareStatement(sql);
      return state.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public Connection getCon() {
    return this.con;
  }
}
