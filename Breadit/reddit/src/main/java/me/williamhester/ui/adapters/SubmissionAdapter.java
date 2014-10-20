package me.williamhester.ui.adapters;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.List;

import me.williamhester.models.AccountManager;
import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.Url;
import me.williamhester.ui.views.SubmissionViewHolder;
import me.williamhester.ui.views.VotableViewHolder;

/**
 * Created by William on 6/27/14.
 */
public class SubmissionAdapter extends ArrayAdapter<Submission> {

    private List<Submission> mSubmissions;
    private SubmissionViewHolder.SubmissionCallbacks mCallback;
    private Context mContext;

    public SubmissionAdapter(Context context, SubmissionViewHolder.SubmissionCallbacks callbacks, List<Submission> submissions) {
        super(context, R.layout.list_item_post, submissions);
        mContext = context;
        mSubmissions = submissions;
        mCallback = callbacks;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_post, parent, false);
            SubmissionViewHolder viewHolder = new SubmissionViewHolder(convertView, mCallback);
            convertView.setTag(viewHolder);
        }
        SubmissionViewHolder submissionViewHolder = (SubmissionViewHolder) convertView.getTag();
        submissionViewHolder.setContent(mSubmissions.get(position));
        return convertView;
    }

    @Override
    public int getCount() {
        return mSubmissions.size();
    }
}
