package cn.leancloud.leanengine_sniper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class SlowQueryRecords {
  final static ConcurrentHashMap<String, SlowQuery> cloudAPISlowRecords =
      new ConcurrentHashMap<String, SlowQuery>();
  final static ConcurrentHashMap<String, SlowQuery> requestsSlowRecords =
      new ConcurrentHashMap<String, SlowQuery>();

  public static void addRequest(final RequestMetricItem request, RequestRecord.RequestType type) {
    switch (type) {
      case REQUEST:
        addRequest(requestsSlowRecords, request);
        break;
      case CLOUDAPI:
        addRequest(cloudAPISlowRecords, request);
        break;
    }
  }

  private static void addRequest(ConcurrentHashMap<String, SlowQuery> records,
      final RequestMetricItem request) {
    String slowKey = request.getUrl();
    SlowQuery newQuery =
        records.computeIfPresent(slowKey, new BiFunction<String, SlowQuery, SlowQuery>() {

          public SlowQuery apply(String key, SlowQuery value) {
            if (value.record.getResponseTime() < request.getResponseTime()) {
              return new SlowQuery(request);
            }
            return value;
          }
        });
    if (newQuery != null) {
      records.put(slowKey, newQuery);
    } else {
      records.put(slowKey, new SlowQuery(request));
    }
  }

  public static List<RequestMetricItem> getCloudAPISlowQuerys() {
    List<RequestMetricItem> slowRequests = new LinkedList<RequestMetricItem>();
    for (Entry<String, SlowQuery> entry : cloudAPISlowRecords.entrySet()) {
      if (!entry.getValue().sent) {
        slowRequests.add(entry.getValue().record);
        entry.getValue().sent = true;
      }
    }
    return slowRequests;
  }

  public static List<RequestMetricItem> getRequestSlowQuerys() {
    List<RequestMetricItem> slowRequests = new LinkedList<RequestMetricItem>();
    for (Entry<String, SlowQuery> entry : requestsSlowRecords.entrySet()) {
      if (!entry.getValue().sent) {
        slowRequests.add(entry.getValue().record);
        entry.getValue().sent = true;
      }
    }
    return slowRequests;
  }

  public static class SlowQuery {
    RequestMetricItem record;
    boolean sent;

    public SlowQuery(RequestMetricItem record) {
      this.record = record;
      this.sent = false;
    }
  }
}
