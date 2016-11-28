package cn.leancloud.leanengine_sniper;

import java.util.List;

import junit.framework.TestCase;
import cn.leancloud.LeanEngine;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUtils;

public class CloudAPITest extends TestCase {

  @Override
  public void setUp() {
    AVOSCloud.setDebugLogEnabled(true);
    LeanEngine.initialize("w1L4OCqPA4W5jE2cGiNJyiru", "sI1O7g5RV3YH7ssTlJSfddko",
        "wWehHS6hkrwMhoE6xfi4jdmS");
    APM.init("5d624dbb0fb4c886b731d21d95e69d116b5f7870", 10);
  }

  public void testObjectSave() throws Exception {
    for (int i = 0; i < 10; i++) {
      AVObject object = new AVObject("apmTest");
      object.put("shit", AVUtils.getRandomString(10));
      object.save();
    }

    AVQuery<AVObject> query = new AVQuery("apmTest");
    List<AVObject> objects = query.find();
    if (objects.size() > 0) {
      query = new AVQuery("apmTest");
      AVObject object = query.get(objects.get(0).getObjectId());
      object.put("shit", AVUtils.getRandomString(11));
      object.save();
      object.delete();
    }

    Thread.sleep(10000);
  }
}
