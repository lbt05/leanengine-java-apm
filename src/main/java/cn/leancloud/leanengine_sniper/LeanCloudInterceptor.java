package cn.leancloud.leanengine_sniper;

import java.io.IOException;

import cn.leancloud.leanengine_sniper.RequestRecord.RequestType;

import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Interceptor;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;

public class LeanCloudInterceptor implements Interceptor {

  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    boolean needRecord =
        InternalConfigurationController.globalInstance().getAppConfiguration()
            .getService(AVOSServices.STORAGE_SERVICE.toString()).endsWith(request.url().getHost())
            || InternalConfigurationController.globalInstance().getAppConfiguration()
                .getService(AVOSServices.FUNCTION_SERVICE.toString())
                .endsWith(request.url().getHost());
    RequestRecord info = null;
    if (needRecord) {
      info = new RequestRecord(request.url().getPath(), request.method(), RequestType.CLOUDAPI);
    }
    Response response = chain.proceed(request);
    // 这里存在一个疑虑就是所有本地的网络因素造成的问题，比如超时，断网都无法被统计到
    if (needRecord) {
      info.end(response.code());
      APM.addRequestInfo(info);
    }
    return response;
  }
}
