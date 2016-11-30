package cn.leancloud.leanengine_sniper;

import java.io.IOException;
import java.net.SocketTimeoutException;

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
    try {
      Response response = chain.proceed(request);
      if (needRecord) {
        info.end(response.code());
        APM.addRequestInfo(info);
      }
      return response;
    } catch (IOException e) {
      if (e instanceof SocketTimeoutException && needRecord) {
        info.end(599);
        APM.addRequestInfo(info);
      }
      throw e;
    }
  }
}
