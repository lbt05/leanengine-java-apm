package cn.leancloud.leanengine_sniper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Interceptor;
import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;

public class APM {
  private static ConcurrentLinkedQueue<RequestRecord> requests =
      new ConcurrentLinkedQueue<RequestRecord>();
  private static String token;
  private static long interval = 60000;
  private static OkHttpClient client = new OkHttpClient();

  private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
  private final static LeanCloudInterceptor leanCloudInterceptor = new LeanCloudInterceptor();
  private final static Runnable reportTask = new Runnable() {

    public void run() {
      process();
    }
  };

  private static ScheduledFuture<?> reportTaskFuture;

  /**
   * 请在LeanEngine.init之后进行调用
   * 
   * @param token 每个应用所对应的 apm token
   */
  public static void init(String token) {
    APM.token = token;
    List<Interceptor> interceptors =
        InternalConfigurationController.globalInstance().getClientConfiguration()
            .getClientInterceptors();

    if (interceptors != null && !interceptors.contains(leanCloudInterceptor)) {
      interceptors.add(leanCloudInterceptor);
    }
    scheduleReportTask();
  }

  private static void scheduleReportTask() {
    if (reportTaskFuture != null) {
      reportTaskFuture.cancel(false);
    }
    reportTaskFuture =
        executor.scheduleAtFixedRate(reportTask, interval, interval, TimeUnit.MILLISECONDS);
  }

  protected static void addRequestInfo(RequestRecord request) {
    requests.add(request);
  }

  public static void setInterval(long intervalInSec) {
    interval = intervalInSec * 1000;
    scheduleReportTask();
  }

  private static void process() {
    // only process these and others will be processed in next round
    List<RequestRecord> currentExistsRequests = new LinkedList<RequestRecord>();
    currentExistsRequests.addAll(requests);
    requests.removeAll(currentExistsRequests);
    // let's aggregate data
    if (currentExistsRequests.size() > 0) {
      RequestMetrics metrics = new RequestMetrics(currentExistsRequests);
      sendMetrics(metrics);
    }
  }

  private static void sendMetrics(RequestMetrics metrics) {
    List<String> reports = metrics.getMetricsReport();

    for (String report : reports) {
      Request.Builder builder = new Request.Builder();
      builder.url("https://apm.leanapp.cn/metrics");
      builder.header("Authorization", token);
      builder.post(RequestBody.create(MediaType.parse("application/json"), report));
      InternalConfigurationController.globalInstance().getInternalLogger()
          .d(APM.class.getName(), "apm report -> \n" + report);
      try {
        Response response = client.newCall(builder.build()).execute();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}
