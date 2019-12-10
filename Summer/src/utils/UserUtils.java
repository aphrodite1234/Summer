package utils;

import java.net.Socket;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

import bean.Message;
import bean.UserBean;
import server.Send;

public class UserUtils {

	private UserBean userBean;
	private Map<Long, Socket> socketMap;
	private Socket socket;
	private String picurl = "http://www.wast.club:8080/pic/";
	String filePath = "/usr/_glucose/";
	Gson gson = new Gson();
	private Send send;

	public UserUtils(UserBean user) {
		userBean = user;
	}

	public UserUtils(UserBean user, HashMap<Long, Socket> socketMap, Socket socket) {
		userBean = user;
		this.socketMap = socketMap;
		this.socket = socket;
		send = new Send(socket);
	}

	public boolean reLogin() {// 更新登录信息
		DBCon db = new DBCon();
		Long phonenum;
		boolean boo;
		try {
			phonenum = userBean.getUserPhone();
			socketMap.put(phonenum, socket);
			Date date = new Date();// 登录时间
			String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

			String sql = "UPDATE usersocket SET userip='" + socket.getInetAddress().toString() + "',userport="
					+ socket.getPort() + ",logintime='" + datetime + "', userstate= 1 WHERE userphone = " + phonenum;
			db.exercuteUpdate(sql);
			boo = true;
		} catch (Exception e) {
			boo = false;
			e.printStackTrace();
		}
		db.close();
		return boo;
	}

	public void signin() {// 登录
		DBCon db = new DBCon();
		DBCon db1 = new DBCon();
		ResultSet rs,rs1;
		Long phonenum = userBean.getUserPhone();
		String password = null, sql = null,sql1=null;
		UserBean user=new UserBean();
		Message message;
		int state=0;
		try {
			sql = "SELECT password FROM user WHERE userphone = " + phonenum;
			sql1="SELECT userstate FROM usersocket WHERE userphone="+phonenum;
			rs = db.executeQuery(sql);
			rs1=db1.executeQuery(sql1);
			if(rs1.next()) {
				state=rs1.getInt("userstate");
			}
			if (rs.next()) {
				password = rs.getString("password");
			}
			if(password==null) {//用户不存在
				user.setType("null");
				message=new Message("UserBean",gson.toJson(user));
				send.send(gson.toJson(message));
			}else if(state==1){
				user.setType("异处登录");
				message=new Message("UserBean",gson.toJson(user));
				send.send(gson.toJson(message));
			}else if (password.equals(userBean.getPassWord())) {// 密码正确
				reLogin();
				downLoadinfo();
			} else {
				user.setType("false");
				message=new Message("UserBean",gson.toJson(user));
				send.send(gson.toJson(message));
			}
		} catch (Exception e) {
			user.setType("false");
			message=new Message("UserBean",gson.toJson(user));
			send.send(gson.toJson(message));
			e.printStackTrace();
		}
		db.close();
		db1.close();
	}

	public boolean logout() {// 退出登录
		DBCon db = new DBCon();
		boolean boo;
		System.out.println(socketMap);
		socketMap.remove(userBean.getUserPhone());
		System.out.println(userBean.getUserPhone() + "已下线~");
		System.out.println(socketMap);
		try {
			String sql = "UPDATE usersocket SET userstate = 0 WHERE userphone =" + userBean.getUserPhone();
			db.exercuteUpdate(sql);
			socket.close();
			Thread.currentThread().interrupt();
			boo = true;
		} catch (Exception e) {
			boo = false;
			e.printStackTrace();
		}
		db.close();
		return boo;
	}

	public String register() {// 注册
		DBCon db = new DBCon();
		ResultSet rs;
		String password = null, str = null;
		Long phonenum = userBean.getUserPhone();
		String username = "糖友-" + phonenum % 10000;
		SimpleDateFormat dateFormate=new SimpleDateFormat("yyyy-MM-dd");
		String birthDay=dateFormate.format(Calendar.getInstance().getTime());
		try {
			String sql1 = "SELECT password FROM user WHERE userphone = " + phonenum;
			rs = db.executeQuery(sql1);
			if (rs.next()) {
				password = rs.getString("password");
			}
			if (password != null) {
				str = "用户已存在";
			} else {
				String sql = "INSERT INTO user (userphone,username,password,blood,sex,birthday,photo,admin) VALUES ( " + phonenum + ",'"
						+ username + "','" + userBean.getPassWord() + "','未知','未知','"+birthDay+"','1',0)";
				String sql2 = "INSERT INTO usersocket (userphone) VALUES (" + phonenum + ")";
				db.exercuteUpdate(sql);
				db.exercuteUpdate(sql2);
				str = "true";
			}
		} catch (Exception e) {
			str = "false";
			e.printStackTrace();
		}
		db.close();
		return str;
	}

	public String expw() {// 重置密码
		DBCon db = new DBCon();
		String str;
		try {
			String sql = "UPDATE user SET password = '" + userBean.getPassWord() + "' WHERE userphone ="
					+ userBean.getUserPhone();
			db.exercuteUpdate(sql);
			str = "true";
		} catch (Exception e) {
			str = "false";
			e.printStackTrace();
		}
		db.close();
		return str;
	}

	public String upLoadInfo() {// 上传用户信息,admin==-1个人资料,admin==-2基本信息
		DBCon db = new DBCon();
		String str,sql = null;
		int state=userBean.getAdmin();
		try {
			switch(state) {
			case -1:
				if(userBean.getPhoto()!=null) {
					sql = "UPDATE user SET username = '" + userBean.getUserName()  + "',photo = '"
					+ savePic(userBean.getPhoto())+"',signature='"+userBean.getSignature()+"',location='"+userBean.getLocation() +
					"' WHERE userphone =" + userBean.getUserPhone();
				}else {
					sql = "UPDATE user SET username = '" + userBean.getUserName()  +"',signature='"+userBean.getSignature()+
							"',location='"+userBean.getLocation() +"' WHERE userphone =" + userBean.getUserPhone();
				}
				break;
			case -2:
				sql = "UPDATE user SET blood='" + userBean.getBloodType()
				+ "',sex = '" + userBean.getSex() + "',birthday ='" + userBean.getBirthday() + "',height = "
				+ userBean.getHeight() + ",weight = " + userBean.getWeight() + ",bmi = " + userBean.getBmi()  +
				" WHERE userphone =" + userBean.getUserPhone();
				break;
				default :
					break;
			}
			
			db.exercuteUpdate(sql);
			
			if(userBean.getUserName()!=null) {
				String	sql1="UPDATE dynamic SET sendername='"+userBean.getUserName()+"' WHERE senderphone="+userBean.getUserPhone(),
						sql2="UPDATE dynamic SET receivername='"+userBean.getUserName()+"' WHERE receiverphone="+userBean.getUserPhone(),
						sql3="UPDATE comment SET sendername='"+userBean.getUserName()+"' WHERE senderphone="+userBean.getUserPhone(),
						sql4="UPDATE comment SET receivername='"+userBean.getUserName()+"' WHERE receiverphone="+userBean.getUserPhone(),
						sql5="UPDATE attention SET sendername='"+userBean.getUserName()+"' WHERE senderphone="+userBean.getUserPhone(),
						sql6="UPDATE attention SET receivername='"+userBean.getUserName()+"' WHERE receiverphone="+userBean.getUserPhone();
				db.exercuteUpdate(sql1);
				db.exercuteUpdate(sql2);
				db.exercuteUpdate(sql3);
				db.exercuteUpdate(sql4);
				db.exercuteUpdate(sql5);
				db.exercuteUpdate(sql6);
			}
			
			str = "true";
		} catch (Exception e) {
			str = "false";
			e.printStackTrace();
		}
		db.close();
		return str;
	}

	private String savePic(byte[] data) {
		String filename = String.valueOf(userBean.getUserPhone());
		PictureUtils.byte2file(data, filePath + filename + ".jpg");
		return filename;
	}

	public void downLoadinfo() {// 下载用户信息,admin(0)登录者信息,admin(-2)查看别人信息
		DBCon db = new DBCon();
		ResultSet rs;
		UserBean user=new UserBean();
		user.setUserPhone(userBean.getUserPhone());
		int admin=userBean.getAdmin();
		Message message;
		String sql = "SELECT * FROM user WHERE userphone = " + userBean.getUserPhone();
		try {
			rs = db.executeQuery(sql);
			if (rs.next()) {
				if(admin==0) {
					user.setBirthday(rs.getString("birthday"));
					user.setHeight(rs.getFloat("height"));
					user.setWeight(rs.getFloat("weight"));
					user.setBmi(rs.getFloat("bmi"));
					user.setAdmin(rs.getInt("admin"));
					user.setLocation(rs.getString("location"));
				}
				user.setUserName(rs.getString("username"));
				user.setBloodType(rs.getString("blood"));
				user.setSex(rs.getString("sex"));
				user.setSignature(rs.getString("signature"));
				user.setUrl(picurl + rs.getString("photo")+".jpg");
				user.setType("用户信息");
				message=new Message("UserBean",gson.toJson(user));
				send.send(gson.toJson(message));
			}
		} catch (Exception e) {
			user.setType("false");
			message=new Message("UserBean",gson.toJson(user));
			send.send(gson.toJson(message));
			e.printStackTrace();
		}
		db.close();
	}
}
