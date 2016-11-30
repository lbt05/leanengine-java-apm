package cn.leancloud.leanengine_sniper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import cn.leancloud.leanengine_sniper.RequestRecord.RequestType;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public AppTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(AppTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  private static Pattern resourcePattern = Pattern
      .compile("^.*\\.(css|js|jpe?g|gif|png|woff2?|ico)$");

  public void testPattern() {
    String path = "/ajsdfaj/sadjfkajsdf/kjsdkfj.png";
    Matcher matcher = resourcePattern.matcher(path);
    Assert.assertTrue(matcher.matches());
    Assert.assertEquals("*.png", "*." + matcher.group(1));

    path = "/classes/_User";
    Assert.assertEquals(3, path.split("/").length);
  }

  public void testRequestRecord() {
    RequestRecord record = new RequestRecord("/1.1/classes/_User", "POST", RequestType.CLOUDAPI);
    record.end(200);
    RequestMetricItem item = record.metric();
    assertEquals(item.method + " " + "/1.1/classes/_User", item.getUrl());

    record = new RequestRecord("/1.1/classes/_User/123", "POST", RequestType.CLOUDAPI);
    record.end(200);
    item = record.metric();
    assertEquals(item.method + " " + "/1.1/classes/_User/:id", item.getUrl());

    RequestRecord record1 =
        new RequestRecord("/1.1/classes/_User/123", "POST", RequestType.CLOUDAPI);
    record1.end(200);
    RequestMetricItem item1 = record1.metric();
    item1.addRequestMetric(item);
    System.out.println(item1.getResponseTime());
  }

  public void testRequest() {
    RequestRecord record = new RequestRecord("/1.1/call/query", "POST", RequestType.REQUEST);
    record.end(200);
    RequestMetricItem item = record.metric();
    assertEquals(item.method + " " + "/1.1/call/query", item.getUrl());

    record = new RequestRecord("/1.1/functions/query", "GET", RequestType.REQUEST);
    record.end(200);
    item = record.metric();
    assertEquals(item.method + " " + "/1.1/functions/query", item.getUrl());
  }

}
