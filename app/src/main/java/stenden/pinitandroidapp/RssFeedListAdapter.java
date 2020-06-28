package stenden.pinitandroidapp;
/**
 * Created by Jeroen on 22-12-2016.
 */

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import stenden.pinitandroidapp.R;
import java.util.*;
import java.util.List;

// Is an adapter specially made for the RssFeedModel, this shows the text the way we want to.
public class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private List<RssFeedModel> mRssFeedModels;

    public static class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;

        public FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
        }
    }

    public RssFeedListAdapter(List<RssFeedModel> rssFeedModels) {
        mRssFeedModels = rssFeedModels;
    }

    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rss_feed, parent, false);
        FeedModelViewHolder holder = new FeedModelViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        ((TextView)holder.rssFeedView.findViewById(R.id.titleText)).setText(rssFeedModel.title);
        ((TextView)holder.rssFeedView.findViewById(R.id.descriptionText)).setText(rssFeedModel.description);
        TextView hyperLink = ((TextView)holder.rssFeedView.findViewById(R.id.linkText));
        Spanned text = Html.fromHtml("<a href=" + rssFeedModel.link + ">Click to see full article</a>");
        hyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        hyperLink.setText(text);
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }
}

