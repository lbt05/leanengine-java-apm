package cn.leancloud.leanengine_sniper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cn.leancloud.leanengine_sniper.RequestRecord.RequestType;

import com.alibaba.fastjson.JSONObject;

class RequestMetrics {
  HashMap<String, RequestMetricItem> requestsMetrics = new HashMap<String, RequestMetricItem>();
  HashMap<String, RequestMetricItem> cloudAPIMetrics = new HashMap<String, RequestMetricItem>();

  public RequestMetrics(List<RequestRecord> records) {
    for (RequestRecord record : records) {
      RequestMetricItem item = record.metric();
      switch (record.type) {
        case REQUEST:
          addRequestMetrics(requestsMetrics, item);
          break;
        case CLOUDAPI:
          addRequestMetrics(cloudAPIMetrics, item);
          break;
      }
    }
  }

  private void addRequestMetrics(HashMap<String, RequestMetricItem> metrics, RequestMetricItem item) {
    String key = item.getStatusCode() + ":" + item.getUrl();
    if (metrics.containsKey(key)) {
      metrics.get(key).addRequestMetric(item);
    } else {
      metrics.put(key, item);
    }
  }

  public List<String> getMetricsReport() {
    List<String> reports = new LinkedList<String>();

    if (!requestsMetrics.isEmpty()) {
      reports.add(getMetricString(RequestType.REQUEST, requestsMetrics.values()));
    }

    if (!cloudAPIMetrics.isEmpty()) {
      reports.add(getMetricString(RequestType.CLOUDAPI, cloudAPIMetrics.values()));
    }
    return reports;
  }

  private String getMetricString(RequestType type, Collection<RequestMetricItem> items) {
    String instance = getInstanceName();
    JSONObject object = new JSONObject();
    object.put("metric", type.toString());
    object.put("instance", instance);
    object.put("points", items);
    return object.toJSONString();
  }

  private String getInstanceName() {
    String instanceEnv = getEnvOrProperty("LEANCLOUD_APP_INSTANCE");
    if (instanceEnv == null) {

      String hostname = "Unknown";

      try {
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        hostname = addr.getHostName();
      } catch (UnknownHostException ex) {
      }
      return hostname;
    }

    return instanceEnv;
  }

  private String getEnvOrProperty(String key) {
    String value = System.getenv(key);
    if (value == null) {
      value = System.getProperty(key);
    }
    return value;
  }
}
