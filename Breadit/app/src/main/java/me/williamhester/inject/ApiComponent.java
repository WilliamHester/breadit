package me.williamhester.inject;

import javax.inject.Singleton;

import dagger.Component;
import me.williamhester.ui.activities.BaseActivity;
import me.williamhester.ui.fragments.BaseFragment;

/**
 * Created by william on 7/4/15.
 */
@Singleton
@Component(modules = {ParentApiModule.class, ApiModule.class})
public interface ApiComponent {
    void inject(BaseFragment baseFragment);
    void inject(BaseActivity baseActivity);
}
