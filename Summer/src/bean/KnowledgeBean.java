package bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class KnowledgeBean {

    private int id;
    private String type;
    private Long author;
    private String title;
    private String photo;
    private String content;
    private String dateTime;
    private ArrayList<byte[]> pictures;
    private ArrayList<String> name;
    private int browseCount;
    private int commentCount;
    private int zanCount;
    private int zanBool;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
//        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        this.dateTime = format.format(dateTime);
    	this.dateTime=dateTime;
    }

    public int getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(int browseCount) {
        this.browseCount = browseCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getZanCount() {
        return zanCount;
    }

    public void setZanCount(int zanCount) {
        this.zanCount = zanCount;
    }

    public int getZanBool() {
        return zanBool;
    }

    public void setZanBool(int zanBool) {
        this.zanBool = zanBool;
    }

    public ArrayList<byte[]> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<byte[]> pictures) {
        this.pictures = pictures;
    }

    public void setName(ArrayList<String> name) {
        this.name = name;
    }

    public ArrayList<String> getName() {
        return name;
    }

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}
