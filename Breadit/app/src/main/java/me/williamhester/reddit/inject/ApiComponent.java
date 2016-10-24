package me.williamhester.reddit.inject;

import javax.inject.Singleton;

import dagger.Component;
import me.williamhester.reddit.ui.activities.BaseActivity;
import me.williamhester.reddit.ui.activities.SelectSubredditActivity;
import me.williamhester.reddit.ui.fragments.BaseFragment;
import me.williamhester.reddit.ui.views.VotableViewHolder;

/**
 * Created by william on 7/4/15.
 */
@Singleton
@Component(modules = ApiModule.class)
public interface ApiComponent {
  void inject(BaseFragment baseFragment);

  void inject(BaseActivity baseActivity);

  void inject(VotableViewHolder viewHolder);

  void inject(SelectSubredditActivity selectSubredditActivity);
}
