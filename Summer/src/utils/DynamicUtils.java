package utils;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bean.Dynamic;
import bean.Message;
import server.Send;

public class DynamicUtils {

	private Dynamic dynamic;
	private Gson gson = new Gson();
	private String picurl = "http://www.wast.club:8080/pic/";
	private String filePath = "/usr/_glucose/";
	private Send send;
	private Type type = new TypeToken<ArrayList<String>>() {
	}.getType();
	
	public DynamicUtils() {

	}

	public DynamicUtils(Dynamic dynamic,Send send) {
		this.dynamic = dynamic;
		this.send=send;
	}

	public String save() {// 保存用户新发表的动态
		DBCon db = new DBCon();
		Dynamic dy = new Dynamic();
		try {
			String sql = "INSERT INTO dynamic (senderphone, sendername, receiverphone, receivername, content, picture, dytime, state,brocount) VALUES (" 
					+ dynamic.getSenderPhone() + ",'"+ dynamic.getSenderName() + "'," + dynamic.getReceiverPhone()+ ",'"
					+ dynamic.getReceiverName()+ "','"+dynamic.getContent() + "','"+savePicture() + "','" + dynamic.getDytime() + "',0,0)";
			db.exercuteUpdate(sql);
			dy.setType("true");
		} catch (SQLException e) {
			dy.setType("false");
			e.printStackTrace();
		}
		db.close();
		return gson.toJson(dy);
	}

	private String savePicture() {
		ArrayList<String> pic = new ArrayList<>();
		ArrayList<byte[]> picture = dynamic.getPicture();
		for (int i = 0; i < picture.size(); i++) {
			Calendar ca = Calendar.getInstance();
			String fileName = String.valueOf(dynamic.getSenderPhone()) + ca.getTimeInMillis() + i;
			PictureUtils.byte2file(picture.get(i), filePath + fileName + ".jpg");
			pic.add(fileName);
		}
		return gson.toJson(pic);
	}

	public void download() {// 查看动态,dynamic为请求消息,使用type，id，senderPhone属性保存用户信息，复用state区分请求消息类型
		DBCon db = new DBCon();
		ResultSet rs;
		Message message;
		int state = dynamic.getState();// <0,分广场刷新(-1),广场加载全部(-2),关注刷新(-3),关注全部(-4),个人刷新(-5),个人全部(-6)，主页推荐(-7)
		try {
			String sql = null;
			switch (state) {
			case -1:
				sql = "SELECT * FROM dynamic WHERE dyid >" + dynamic.getDyid() + " ORDER BY dytime ASC ";
				break;
			case -2:
				sql = "SELECT * FROM dynamic ORDER BY dytime DESC";
				break;
//			case -3:
//				break;
//			case -4:
//				break;
			case -5:
				sql = "SELECT * FROM dynamic WHERE dyid >" + dynamic.getDyid() + " AND senderphone="
						+ dynamic.getSenderPhone() + " ORDER BY dytime DESC ";
				break;
			case -6:
				sql = "SELECT * FROM dynamic WHERE  senderphone=" + dynamic.getSenderPhone() + " ORDER BY dytime DESC ";
				break;
			case -7:
				sql = "SELECT * FROM dynamic WHERE brocount>0 ORDER BY brocount DESC,dytime DESC limit 10 ";
				break;
			default:
				break;
			}

			rs = db.executeQuery(sql);
			while (rs.next()) {
				ArrayList<String> url = new ArrayList<>();
				ArrayList<String> filename;
				DBCon db1 = new DBCon(), db2 = new DBCon(), db3 = new DBCon(), db4 = new DBCon();
				ResultSet zancount, comcount, zanbool, pic;
				String sql1 = "SELECT * FROM zan WHERE dyid=" + rs.getInt("dyid"),
						sql2 = "SELECT * FROM comment WHERE dyid=" + rs.getInt("dyid"),
						sql3 = "SELECT * FROM zan WHERE userphone=" + dynamic.getReceiverPhone() + " AND dyid="+ rs.getInt("dyid"),
						sql4 = "SELECT blood,photo FROM user WHERE userphone=" + rs.getLong("senderphone");
				zancount = db1.executeQuery(sql1);
				zancount.last();
				comcount = db2.executeQuery(sql2);
				comcount.last();
				zanbool = db3.executeQuery(sql3);
				zanbool.last();
				pic = db4.executeQuery(sql4);
				Dynamic dynamic = new Dynamic();
				if (pic.next()) {
					url.add(picurl + pic.getString("photo") + ".jpg");
					dynamic.setB_type(pic.getString("blood"));
				}
				dynamic.setDyid(rs.getInt("dyid"));
				dynamic.setType(this.dynamic.getType());
				dynamic.setSenderPhone(rs.getLong("senderphone"));
				dynamic.setSenderName(rs.getString("sendername"));
				dynamic.setReceiverPhone(rs.getLong("receiverphone"));
				dynamic.setReceiverName(rs.getString("receivername"));
				dynamic.setContent(rs.getString("content"));
				filename = gson.fromJson(rs.getString("picture"), type);
				for (String s : filename) {
					url.add(picurl + s + ".jpg");
				}
				dynamic.setUrl(url);
				dynamic.setComment_count(comcount.getRow());
				dynamic.setZan_count(zancount.getRow());
				dynamic.setZan_bool(zanbool.getRow());
				dynamic.setDytime(rs.getString("dytime"));
				dynamic.setBrocount(rs.getInt("brocount"));
				dynamic.setState(state);
				message = new Message("Dynamic", gson.toJson(dynamic));
				send.send(gson.toJson(message));
				db1.close();
				db2.close();
				db3.close();
				db4.close();
			}
		} catch (SQLException e) {
			Dynamic dynamic = new Dynamic();
			dynamic.setType("false");
			message = new Message("Dynamic", gson.toJson(dynamic));
			send.send(gson.toJson(message));
			e.printStackTrace();
		}
		db.close();
	}
}
