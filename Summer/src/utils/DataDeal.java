package utils;

import java.net.Socket;
import java.util.HashMap;
import com.google.gson.Gson;

import bean.CommentBean;
import bean.Dynamic;
import bean.KnowledgeBean;
import bean.Message;
import bean.UserBean;
import server.Send;

public class DataDeal {

	private HashMap<Long, Socket> socketMap;
	private Gson gson = new Gson();
	private Socket socket;
	public UserUtils userUtils;
	private Send send;

	public DataDeal(Socket socket, HashMap<Long, Socket> socketMap) {
		this.socket = socket;
		this.socketMap = socketMap;
		send=new Send(socket);
	}

	public void deal(String str) {
		Message message = gson.fromJson(str, Message.class);
		String type = message.getType();

		switch (type) {
		case "UserBean":
			UserBean user = gson.fromJson(message.getContent(), UserBean.class);
			userOperate(user);
			break;
		case "Dynamic":
			Dynamic dynamic = gson.fromJson(message.getContent(), Dynamic.class);
			dynamicOperate(dynamic);
			break;
		case "CommentBean":
			CommentBean comment = gson.fromJson(message.getContent(), CommentBean.class);
			commentOperate(comment);
			break;
		case "Knowledge":
			KnowledgeBean knowledge=gson.fromJson(message.getContent(), KnowledgeBean.class);
			knowOperate(knowledge);
			break;
		case "心跳":
			break;
		default:
			break;
		}
	}

	public void userOperate(UserBean user) {
		String type = user.getType();
		userUtils = new UserUtils(user, socketMap, socket);
		Message message;
		switch (type) {
		case "登录":
			userUtils.signin();
			break;
		case "注册":
			message = new Message("注册", userUtils.register());
			send.send(gson.toJson(message));
			break;
		case "重置密码":
			message = new Message("重置密码", userUtils.expw());
			send.send(gson.toJson(message));
			break;
		case "上传信息":
			message = new Message("上传信息", userUtils.upLoadInfo());
			send.send(gson.toJson(message));
			break;
		case "用户信息":
			userUtils.downLoadinfo();
			break;
		case "注销":
			userUtils.logout();
			userUtils=null;
			break;
		default:
			break;
		}
	}

	public void dynamicOperate(Dynamic dynamic) {
		String type = dynamic.getType();
		Message message;
		DynamicUtils dynamicUtils = new DynamicUtils(dynamic,send);
		switch (type) {
		case "上传":
			message = new Message("Dynamic", dynamicUtils.save());
			send.send(gson.toJson(message));
			break;
		case "下载":
			dynamicUtils.download();
			break;
		default:
			break;
		}
	}
	
	public void knowOperate(KnowledgeBean knowledge) {
		String type = knowledge.getType();
		Message message;
		KnowledgeUtils knowUtils=new KnowledgeUtils(knowledge,send);
		switch(type) {
		case "百科":
			message=new Message("Knowledge",knowUtils.save());
			send.send(gson.toJson(message));
			break;
		case "下载":
			knowUtils.loadKnowledge();
			break;
		case "详细":
			knowUtils.loadKnowMore();
			break;
		case "修改":
			message=new Message("Knowledge",knowUtils.updateKnow());
			send.send(gson.toJson(message));
			break;
			default:
				break;
		}
	}

	public void commentOperate(CommentBean comment) {
		String type = comment.getType();
		Message message;
		CommentUtils commentUtils = new CommentUtils(comment,send);
		switch (type) {
		case "上传评论":
			message = new Message("CommentBean", commentUtils.saveComment());
			send.send(gson.toJson(message));
			break;
		case "下载评论":
			commentUtils.loadComment();
			break;
		case "上传赞":
			message = new Message("CommentBean", commentUtils.saveZan());
			send.send(gson.toJson(message));
			break;
		case "取消赞":
			message = new Message("CommentBean", commentUtils.deleteZan());
			send.send(gson.toJson(message));
			break;
		case "下载赞":
			commentUtils.loadZan();
			break;
		case "浏览":
			commentUtils.brow();
			break;
		case "反馈":
			message=new Message("CommentBean",commentUtils.f());
			send.send(gson.toJson(message));
		default:
			break;
		}
	}
}
