package tinker.sample.android.app;

import com.tencent.tinker.loader.app.TinkerApplication;

public class SampleApplication extends TinkerApplication {

    public SampleApplication() {
        super(7, "tinker.sample.android.app.SampleApplicationLike", "com.tencent.tinker.loader.TinkerLoader", false);
    }

}