package com.yingqi.flash.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yingqi.flash.dao.FlashUserDao;
import com.yingqi.flash.domain.FlashUser;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class UserUtil {

    private static void createUser(int count) throws Exception {
        List<FlashUser> users = new ArrayList<FlashUser>(count);
        //生成用户
        for (int i = 0; i < count; i++) {
            FlashUser user = new FlashUser();
            user.setId(13000000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c");
            user.setPassword(MD5Utils.inputPassToDbPass("123456", user.getSalt()));
            users.add(user);
        }
        System.out.println("create user");
//		//插入数据库
		/*Connection conn = DBUtil.getConn();
		String sql1 = "select * from flash_user";
		PreparedStatement statement = conn.prepareStatement(sql1);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			System.out.println(resultSet.getString(1));
		}

		String sql = "insert into flash_user(login_count, nickname, register_date, salt, password, id, head,last_login_date)values(?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for(int i=0;i<users.size();i++) {
			FlashUser user = users.get(i);
			pstmt.setInt(1, user.getLoginCount());
			pstmt.setString(2, user.getNickname());
			pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
			pstmt.setString(4, user.getSalt());
			pstmt.setString(5, user.getPassword());
			pstmt.setLong(6, user.getId());
			pstmt.setString(7,"00");
			pstmt.setTimestamp(8, new Timestamp(user.getRegisterDate().getTime()));
			pstmt.addBatch();
//			pstmt.executeUpdate();
		}
		pstmt.executeBatch();
		pstmt.close();
		conn.close();
		System.out.println("insert to db");*/
        //登录，生成token
        String urlString = "http://localhost:8080/login/do_login";
        File file = new File("D:/tokens.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        UserUtil userUtil = new UserUtil();
        for (int i = 0; i < users.size(); i++) {
            FlashUser user = users.get(i);
            String row = userUtil.login(urlString, user);
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + row);
        }
        raf.close();
        System.out.println("over");
        /*
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		file.createNewFile();
		raf.seek(0);
		for(int i=0;i<users.size();i++) {
			FlashUser user = users.get(i);
			URL url = new URL(urlString);
			HttpURLConnection co = (HttpURLConnection)url.openConnection();
			co.setRequestMethod("POST");
			co.setDoOutput(true);
			OutputStream out = co.getOutputStream();
			String params = "mobile="+user.getId()+"&password="+MD5Utils.inputPassFormPasss("123456");
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = co.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte buff[] = new byte[1024];
			int len = 0;
			while((len = inputStream.read(buff)) >= 0) {
				bout.write(buff, 0 ,len);
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());
			JSONObject jo = JSON.parseObject(response);
			String token = jo.getString("data");
			System.out.println("create token : " + user.getId());
			
			String row = user.getId()+","+token;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : " + user.getId());
		}
		raf.close();
		
		System.out.println("over");*/
    }

    public static void main(String[] args) throws Exception {
        createUser(5000);
    }

    public String login(String url, FlashUser user) throws IOException {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(manager).build();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("mobile",user.getId().toString()));
        list.add(new BasicNameValuePair("password",MD5Utils.inputPassFormPasss("123456")));
        httpPost.setEntity(new UrlEncodedFormEntity(list,"utf8"));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String row = null;
        if (response.getStatusLine().getStatusCode() == 200) {
            String content = EntityUtils.toString(response.getEntity(), "utf8");
            JSONObject jo = JSON.parseObject(content);
            String token = jo.getString("data");
            row = user.getId()+","+token;
        }
        return row;
    }
}
