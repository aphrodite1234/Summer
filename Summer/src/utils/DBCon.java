package utils;

import java.sql.*;
public class DBCon {

	// JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/glucose?useSSL=false&serverTimezone=UTC";
    // 数据库的用户名与密码
    static final String USER = "root";
    static final String PASS = "Lv1003@@";
    public Connection con =null;
    Statement stmt = null;
    ResultSet rs = null;
    public PreparedStatement ps;
    
    public DBCon() {
    	try {
    		Class.forName(JDBC_DRIVER);
    		con=DriverManager.getConnection(DB_URL,USER,PASS);
    		stmt=con.createStatement();
    	}catch(Exception e) {
    		e.getStackTrace();
    	}
    }
    
    //查询
    public ResultSet executeQuery(String sql) throws SQLException {
    	rs=stmt.executeQuery(sql);
    	return rs;
    }
    
    //增、删、改
    public int exercuteUpdate(String sql) throws SQLException {
    	int rowCount = 0;
    	rowCount=stmt.executeUpdate(sql);
    	return rowCount;
    }
    //关闭
    public void close() {
    	try {
    		con.close();
    		con=null;
    	}catch(Exception e) {
    		e.getStackTrace();
    	}
    }
}
