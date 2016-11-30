package cn.leancloud.leanengine_sniper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import cn.leancloud.CloudCodeServlet;
import cn.leancloud.EngineSessionCookie;
import cn.leancloud.HttpsRequestRedirectFilter;
import cn.leancloud.LeanEngine;
import cn.leancloud.LeanEngineHealthCheckServlet;
import cn.leancloud.RequestUserAuthFilter;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;

public class FunctionRequestTest extends TestCase {
  String secret = "05XgTktKPMkU";
  private static Server server;
  private static int port = 3000;

  @Override
  public void setUp() throws Exception {
    System.setProperty("LEANCLOUD_APP_PORT", "3000");
    System.setProperty("LEANCLOUD_API_SERVER", "https://api.leancloud.cn");
    LeanEngine.initialize("w1L4OCqPA4W5jE2cGiNJyiru", "sI1O7g5RV3YH7ssTlJSfddko",
        "wWehHS6hkrwMhoE6xfi4jdmS");
    LeanEngine.setLocalEngineCallEnabled(true);
    LeanEngine.setUseMasterKey(true);
    LeanEngine.addSessionCookie(new EngineSessionCookie(secret, 160000, true));
    LeanEngine.register(AllEngineFunctions.class);
    AVOSCloud.setDebugLogEnabled(true);
    APM.init("5d624dbb0fb4c886b731d21d95e69d116b5f7870");
    APM.setInterval(10);

    server = new Server(port);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(LeanEngineHealthCheckServlet.class, "/__engine/1/ping");
    handler.addServletWithMapping(CloudCodeServlet.class, "/1.1/functions/*");
    handler.addServletWithMapping(CloudCodeServlet.class, "/1.1/call/*");
    handler.addServletWithMapping(CustomServlet.class, "/tags/*");

    handler.addFilterWithMapping(HttpsRequestRedirectFilter.class, "/*",
        EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));

    handler.addFilterWithMapping(RequestFilter.class, "/*",
        EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));

    handler.addFilterWithMapping(RequestUserAuthFilter.class, "/*",
        EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
    server.start();
  }

  @Override
  public void tearDown() throws Exception {
    Thread.sleep(10000);
    server.stop();
  }


  public void test_ping() throws IOException {
    Request.Builder builder = new Request.Builder();
    builder.url("http://localhost:3000/__engine/1/ping");
    builder.get();
    OkHttpClient client = new OkHttpClient();
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    String body = new String(response.body().bytes());
    assertTrue(body.indexOf("runtime") > 0);
    assertTrue(body.indexOf("version") > 0);
  }

  public void testHello() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", "张三");
    Object result = AVCloud.callFunction("hello", params);
    assertEquals("hello 张三", result);
  }

  public void testAVCloudFunction() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("ts", 123);
    AVUser.logOut();
    Map<String, Object> result = AVCloud.callFunction("ping", params);
    assertTrue(result == null);
  }

  public void testRPCCall() throws Exception {
    AVUser registerUser = new AVUser();
    registerUser.setUsername(AVUtils.getRandomString(10) + System.currentTimeMillis());
    registerUser.setPassword(AVUtils.getRandomString(10));
    registerUser.signUp();
    AVUser u = AVCloud.rpcFunction("ping", 123);
    assertEquals(registerUser.getObjectId(), (u.getObjectId()));
  }

  public void testSimpleObject() throws Exception {
    AVObject obj = new AVObject("rpcTest");
    obj.put("int", 12);
    obj.save();

    String result = AVCloud.rpcFunction("simpleObject", obj);
    assertEquals("success", result);

    obj.put("int", 3000);
    obj.save();
    result = AVCloud.rpcFunction("simpleObject", obj);
    assertEquals("failure", result);
  }

  public void testComplexObject() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    int[] array = new int[3];
    for (int i = 0; i < array.length; i++) {
      array[i] = i + 123;
    }
    AVObject hello = new AVObject("hello");
    hello.put("int", 123);
    hello.save();

    List<AVObject> list = new ArrayList<AVObject>(2);
    list.add(hello);
    list.add(hello);
    params.put("array", array);
    params.put("avobject", hello);
    params.put("foo", "bar");
    params.put("list", list);

    Map<String, Object> result = AVCloud.rpcFunction("complexObject", params);
    assertEquals("bar", result.get("foo"));
    assertEquals(hello, result.get("avobject"));
    assertEquals(list, result.get("list"));
    for (int i = 0; i < array.length; i++) {
      assertEquals(array[i], ((List) result.get("array")).get(i));
    }
  }

  public void testQueryResult() throws AVException {
    List<Map> result = AVCloud.callFunction("query", null);
    for (Map m : result) {
      assertNotNull(m.get("username"));
    }
    List<AVUser> userResults = AVCloud.rpcFunction("query", null);
    for (AVUser u : userResults) {
      assertNotNull(u.getUsername());
    }
  }

  public void testCustomServlet() throws Exception {
    Request.Builder builder = new Request.Builder();
    builder.url("http://localhost:3000/tags/data");
    builder.get();
    OkHttpClient client = new OkHttpClient();
    Response response = client.newCall(builder.build()).execute();
  }
}
