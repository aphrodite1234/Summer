package server;

import utils.DBCon;

public class Service {
	
	public static void main(String[] args) throws Exception {
		DBCon db = new DBCon();
		String sql = "UPDATE usersocket SET userstate = 0";
		db.exercuteUpdate(sql);
		SerCon serCon=new SerCon();
		serCon.invoke();
	}
}
