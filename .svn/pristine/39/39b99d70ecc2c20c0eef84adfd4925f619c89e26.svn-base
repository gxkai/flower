package com.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.dao.WLDao;



@ServerEndpoint("/ws/getsingle_pre/{url}")
public class ChatAnnotation_pre {
	private static final Set<ChatAnnotation_pre> connections = new CopyOnWriteArraySet<ChatAnnotation_pre>();
	private Session session;
	public static String xx;
	
	@OnOpen
	public void onOpen(Session session, @PathParam("url") String url) {
		String[] str = url.split("-");
    	String key = str[0];
		session.getUserProperties().put("key", key);
		this.session = session;
		connections.add(this);
    }
        
    /**  
     * 收到客户端消息时触发
     * @param relationId  
     * @param userCode  
     * @param message  
     * @return  
     * @throws InterruptedException 
    */  
    @OnMessage
    public void onMessage(Session session, String url) {
    	String[] str = url.split("-");
    	String key = str[0];//生成的4位随机码
    	WLDao.singleWL_pre(key);
    }
    
    /**  
     * 异常时触发
     * @param relationId
     * @param userCode
     * @param session
    */
    @OnError
    public void onError(Throwable throwable,Session session) {
    	
    	
    }
  
    /**  
     * 关闭连接时触发 
     * @param relationId  
     * @param userCode
     * @param session
    */
    @OnClose
    public void onClose(Session session) {
    	connections.remove(this);
    }
    
    public static void broadcast(String msg, String num) {
		for (ChatAnnotation_pre client : connections) {
			try {
				synchronized (client) {
					String code = client.session.getUserProperties().get("key").toString();
		            if (code.equals(msg)) {
		            	client.session.getBasicRemote().sendText(num);
		            }
				}
			} catch (IOException e) {
				connections.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {}
			}
		}
	}
    
}
