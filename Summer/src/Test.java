import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bean.CommentBean;
import bean.Dynamic;
import bean.KnowledgeBean;
import bean.UserBean;
import utils.DBCon;
import utils.DynamicUtils;
import utils.KnowledgeUtils;
import utils.PictureUtils;
import utils.UserUtils;

public class Test {

	public static void main(String[] args) {
//		UserBean userBean = new UserBean();
//		Map<Long,Socket> socketMap = new HashMap<>();
//		UserUtils userUtils = new UserUtils(userBean);
//		DBCon db = new DBCon();
		Gson json = new GsonBuilder().disableHtmlEscaping().create();
		KnowledgeBean k=new KnowledgeBean();
		k.setAuthor(15083498391L);
		k.setContent("<p>我是内容</p><img src=\"http://47.95.146.87:8080/pic/1508349839115672356709910.jpg\"/><p></p>");
		System.out.print(json.toJson(k));
	}
}
