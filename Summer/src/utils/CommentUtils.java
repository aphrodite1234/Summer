package utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;

import bean.CommentBean;
import bean.Message;
import server.Send;

public class CommentUtils {

	private CommentBean comment;
	private Gson gson = new Gson();
	private String picurl = "http://www.wast.club:8080/pic/";
	private Send send;
	
	public CommentUtils(CommentBean commentBean,Send send) {
		comment=commentBean;
		this.send=send;
	}
	
	public String saveComment() {// 保存用户的评论以及回复，用id属性表示返回结果1成功，-1失败
		DBCon db = new DBCon();
		CommentBean com = new CommentBean();
		String sql = "INSERT INTO comment(dyid,knid,senderphone,sendername,receiverphone,receivername,content,comtime,state)"
				+ "VALUES(" + comment.getDyId()+","+comment.getKnId() + "," + comment.getSenderPhone() + ",'" + comment.getSenderName() + "',"
				+ comment.getReceiverPhone() + ",'" + comment.getReceiverName() + "','" + comment.getContent() + "','"
				+ comment.getDateTime() + "'," + comment.getState() + ")";
		try {
			db.exercuteUpdate(sql);
			com.setId(1);
		} catch (SQLException e) {
			com.setId(-1);
			e.printStackTrace();
		}
		com.setType(comment.getType());
		db.close();
		return gson.toJson(com);
	}

	public void loadComment() {// 加载评论,comment为请求消息,dyid区分动态,id==-1为错误,state(-1)动态评论,state(-2)百科评论
		DBCon db = new DBCon();
		DBCon db1 = new DBCon();
		DBCon db2 = new DBCon();
		DBCon db3 = new DBCon();
		DBCon db4 = new DBCon();
		ResultSet rs, zancount, zanbool,comzan,comcount;
		Message message;
		String sql = null,sql3 = null,sql4 = null;
		switch(comment.getState()) {
		case -1:
			sql = "SELECT * FROM comment WHERE dyid =" + comment.getDyId() + " ORDER BY comtime ";
			sql3="SELECT * FROM zan WHERE dyid="+ comment.getDyId();
			sql4="SELECT * FROM comment WHERE dyid =" + comment.getDyId();
			break;
		case -2:
			sql = "SELECT * FROM comment WHERE knid =" + comment.getKnId() + " ORDER BY comtime ";
			sql3="SELECT * FROM zan WHERE knid="+ comment.getKnId();
			sql4="SELECT * FROM comment WHERE knid =" + comment.getKnId();
			break;
			default:
				break;
		}
		
		try {
			rs = db.executeQuery(sql);
			comzan=db3.executeQuery(sql3);
			comzan.last();
			comcount=db4.executeQuery(sql4);
			comcount.last();
			while (rs.next()) {
				String sql1 = "SELECT * FROM zan WHERE comid =" + rs.getInt("id"),
						sql2 = "SELECT * FROM zan WHERE userphone =" + comment.getSenderPhone() + " AND comid="+ rs.getInt("id");
				zancount = db1.executeQuery(sql1);
				zancount.last();
				zanbool = db2.executeQuery(sql2);
				zanbool.last();
				CommentBean com = new CommentBean();
				com.setState(comzan.getRow());//主贴的赞数
				com.setDyId(comcount.getRow());//主贴的评论数
				com.setId(rs.getInt("id"));
				com.setType(comment.getType());
				com.setSenderPhone(rs.getLong("senderphone"));
				com.setSenderName(rs.getString("sendername"));
				com.setReceiverPhone(rs.getLong("receiverphone"));
				com.setReceiverName(rs.getString("receivername"));
				com.setContent(rs.getString("content"));
				com.setZanCount(zancount.getRow());
				com.setZanBool(zanbool.getRow());
				message = new Message("CommentBean", gson.toJson(com));
				send.send(gson.toJson(message));
			}
		} catch (SQLException e) {
			CommentBean com = new CommentBean();
			com.setType(comment.getType());
			com.setId(-1);
			message = new Message("CommentBean", gson.toJson(com));
			send.send(gson.toJson(message));
			e.printStackTrace();
		}
		db.close();
		db1.close();
		db2.close();
		db3.close();
		db4.close();
	}

	public String saveZan() {// 保存点赞记录,state(-1)动态赞,state(-2)评论赞,state(-3)百科赞,返回消息用id属性表示返回结果1成功，-1失败
		DBCon db = new DBCon();
		CommentBean com = new CommentBean();
		String sql = "INSERT INTO zan(userphone,dyid,comid,knid,zantime)VALUES(" + comment.getSenderPhone() + ","
				+ comment.getDyId() + "," + comment.getId()+","+comment.getKnId() + ",'" + comment.getDateTime() + "')";
		try {
			db.exercuteUpdate(sql);
			com.setId(1);
		} catch (SQLException e) {
			com.setId(-1);
			e.printStackTrace();
		}
		com.setType(comment.getType());
		com.setState(comment.getState());
		db.close();
		return gson.toJson(com);
	}

	public String deleteZan() {// 取消点赞,state(-1)动态赞,state(-2)评论赞,state(-3)百科赞,返回消息用id属性表示返回结果1成功，-1失败
		DBCon db = new DBCon();
		CommentBean com = new CommentBean();
		String sql = "DELETE FROM zan WHERE (dyid=" + comment.getDyId() + " OR comid=" + comment.getId()+" OR knid="+comment.getKnId()
				+ ") AND userphone=" + comment.getSenderPhone();
		try {
			db.exercuteUpdate(sql);
			com.setId(1);
		} catch (SQLException e) {
			com.setId(-1);
			e.printStackTrace();
		}
		com.setType(comment.getType());
		com.setState(comment.getState());
		db.close();
		return gson.toJson(com);
	}

	public void loadZan() {// 加载赞列表，comment为请求消息,dyid表示动态,id==-1错误
		DBCon db = new DBCon();
		ResultSet rs;
		Message message;
		String sql = "SELECT zan.userphone,user.username,user.photo FROM zan,user WHERE zan.dyid="
				+ comment.getDyId() + " AND zan.userphone=user.userphone";
		try {
			rs = db.executeQuery(sql);
			while (rs.next()) {
				CommentBean com = new CommentBean();
				com.setSenderPhone(rs.getLong("zan.userphone"));
				com.setSenderName(rs.getString("user.username"));
				com.setType(comment.getType());
				String photo = picurl + rs.getString("user.photo") + ".jpg";
				com.setPhoto(photo);
				message = new Message("CommentBean", gson.toJson(com));
				send.send(gson.toJson(message));
			}
		} catch (SQLException e) {
			CommentBean com = new CommentBean();
			com.setType(comment.getType());
			com.setId(-1);
			message = new Message("CommentBean", gson.toJson(com));
			send.send(gson.toJson(message));
			e.printStackTrace();
		}
		db.close();
	}

	public void brow() {//每有一个用户浏览一次帖子/百科，各brocount+1
		DBCon db = new DBCon();
		String sql =null;
		if(comment.getDyId()>0) {
			sql = "UPDATE dynamic SET brocount=brocount+1 WHERE dyid="+comment.getDyId();	
		}else if(comment.getKnId()>0) {
			sql = "UPDATE knowledge SET brocount=brocount+1 WHERE id="+comment.getKnId();
		}
		try {
			db.exercuteUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
	
	public String f() {//保存反馈信息,id==1成功,id==-1错误
		DBCon db=new DBCon();
		CommentBean com=new CommentBean();
		com.setType(comment.getType());
		String sql="INSERT INTO feedback(userphone,content,ftime,state)VALUES("+comment.getSenderPhone()+",'"+comment.getContent()+"','"
				+comment.getDateTime()+"',0)";
		try {
			db.exercuteUpdate(sql);
			com.setId(1);
		} catch (SQLException e) {
			com.setId(-1);
			e.printStackTrace();
		}
		return gson.toJson(com);
	}
}
