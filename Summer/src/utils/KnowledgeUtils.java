package utils;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import bean.KnowledgeBean;
import bean.Message;
import server.Send;

public class KnowledgeUtils {

	private KnowledgeBean knowledge;
	private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private String filePath = "/usr/_glucose/";
	private String picurl = "http://www.wast.club:8080/pic/";
	private Type type = new TypeToken<ArrayList<String>>() {
	}.getType();
	private Send send;

	public KnowledgeUtils(KnowledgeBean know,Send send) {
		knowledge = know;
		this.send=send;
	}

	public String save() {// 保存百科知识，成功后返回id,id==-1错误
		DBCon db = new DBCon();
		KnowledgeBean know = new KnowledgeBean();
		know.setType(knowledge.getType());
		ResultSet rs;
		String sql = "INSERT INTO knowledge (author, title, picture, content, sendtime,brocount)VALUES(" + knowledge.getAuthor()
				+ ",'" + knowledge.getTitle() + "','" + gson.toJson(knowledge.getName()) + "','" + knowledge.getContent()
				+ "','" + knowledge.getDateTime() +"'," +knowledge.getBrowseCount() + ")";
		try {
			db.exercuteUpdate(sql);
			String sql1 = "SELECT id FROM knowledge WHERE author=" + knowledge.getAuthor() + " AND sendtime='"
					+ knowledge.getDateTime() + "'";
			rs = db.executeQuery(sql1);
			if (rs.next() && savePic()) {
				know.setId(rs.getInt("id"));
			} else {
				know.setId(-1);
			}
		} catch (SQLException e) {
			know.setId(-1);
			e.printStackTrace();
		}
		db.close();
		return gson.toJson(know);
	}

	public boolean savePic() {
		ArrayList<String> picname = knowledge.getName();
		ArrayList<byte[]> data = knowledge.getPictures();
		for (int i = 0; i < data.size(); i++) {
			PictureUtils.byte2file(data.get(i), filePath + picname.get(i) + ".jpg");
		}
		return true;
	}

	public void loadKnowledge() {// 加载百科知识简略信息,返回id为-1错误,id(-1)按浏览次数,id(-2)按发表时间
		DBCon db = new DBCon();
		Message message;
		ResultSet rs;
		String sql = null ;
		int state = knowledge.getId();
		if(state==-1) {
			sql= "SELECT id,picture,title,brocount FROM knowledge ORDER BY brocount DESC";
		}else if(state==-2) {
			sql= "SELECT id,picture,title,brocount FROM knowledge ORDER BY sendtime DESC LIMIT 15";
		}
		try {
			rs = db.executeQuery(sql);
			while(rs.next()) {
				KnowledgeBean know = new KnowledgeBean();
				know.setType(knowledge.getType());
				ArrayList<String> s=gson.fromJson(rs.getString("picture"), type);
				know.setTitle(rs.getString("title"));
				know.setId(rs.getInt("id"));
				if(s.size()>0) {
					know.setPhoto(picurl + s.get(0) + ".jpg");
				}
				know.setBrowseCount(rs.getInt("brocount"));
				message = new Message("Knowledge", gson.toJson(know));
				send.send(gson.toJson(message));
			}
		} catch (SQLException e) {
			KnowledgeBean know = new KnowledgeBean();
			know.setType(knowledge.getType());
			know.setId(-1);
			message = new Message("Knowledge", gson.toJson(know));
			send.send(gson.toJson(message));
			e.printStackTrace();
		}
		db.close();
	}

	public void loadKnowMore() {// 加载百科详细信息,id==-1错误
		DBCon db = new DBCon(), db1 = new DBCon(), db2 = new DBCon(), db3 = new DBCon();
		ResultSet rs, zancount, comcount, zanbool;
		Message message;
		KnowledgeBean know = new KnowledgeBean();
		know.setType(knowledge.getType());
		String sql = "SELECT * FROM knowledge WHERE id=" + knowledge.getId();
		String sql1 = "SELECT * FROM zan WHERE knid=" + knowledge.getId(),
				sql2 = "SELECT * FROM comment WHERE knid=" + knowledge.getId(),
				sql3 = "SELECT * FROM zan WHERE userphone=" + knowledge.getAuthor() + " AND knid=" + knowledge.getId();
		try {
			rs = db.executeQuery(sql);
			zancount = db1.executeQuery(sql1);
			zancount.last();
			comcount = db2.executeQuery(sql2);
			comcount.last();
			zanbool = db3.executeQuery(sql3);
			zanbool.last();
			if(rs.next()) {
				know.setId(rs.getInt("id"));
				know.setTitle(rs.getString("title"));
				know.setContent(rs.getString("content"));
				know.setDateTime(rs.getString("sendtime"));
				know.setBrowseCount(rs.getInt("brocount"));
			}
			know.setZanCount(zancount.getRow());
			know.setZanBool(zanbool.getRow());
			know.setCommentCount(comcount.getRow());
		} catch (SQLException e) {
			know.setId(-1);
			e.printStackTrace();
		}
		db.close();
		message=new Message("Knowledge",gson.toJson(know));
		send.send(gson.toJson(message));
	}

	public String updateKnow() {// 更改百科,id==-1错误,id==1正确
		DBCon db = new DBCon();
		KnowledgeBean know=new KnowledgeBean();
		know.setType(knowledge.getType());
		ResultSet rs;
		String sql = "UPDATE knowledge SET title='" + knowledge.getTitle() + "',picture='" + gson.toJson(knowledge.getName())
				+ "',content='" + knowledge.getContent() + "',sendtime='" + knowledge.getDateTime() + "' WHERE id="
				+ knowledge.getId() + " AND author=" + knowledge.getAuthor(),
				sql1="SELECT picture FROM knowledge WHERE id="+knowledge.getId();
		try {
			rs=db.executeQuery(sql1);
			if(rs.next()) {
				ArrayList<String> s=gson.fromJson(rs.getString("picture"), type);
				for(String path:s) {
					PictureUtils.deleteFile(path);
				}
			}
			db.exercuteUpdate(sql);
			know.setId(1);
		} catch (SQLException e) {
			know.setId(-1);
			e.printStackTrace();
		}
		db.close();
		return gson.toJson(know);
	}
}
