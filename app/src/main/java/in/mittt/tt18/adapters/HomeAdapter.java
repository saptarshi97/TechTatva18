package in.mittt.tt18.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.mittt.tt18.R;
import in.mittt.tt18.models.instagram.InstaFeedModel;
import in.mittt.tt18.models.instagram.InstagramFeed;


public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    String TAG = "HomeAdapter";
    private InstagramFeed feed;
    private Context context;

    public HomeAdapter(InstagramFeed feed) {
        this.feed = feed;
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insta_feed, parent, false);
        context = parent.getContext();
        return new HomeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {
        InstaFeedModel instaItem = feed.getFeed().get(position);
        holder.onBind(instaItem);
    }

    @Override
    public int getItemCount() {
        return feed.getFeed().size();
    }

    public void launchInstagramImage(InstaFeedModel instaItem) {
        try {
            Uri uri = Uri.parse(instaItem.getLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.instagram.android");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage() + "\n Perhaps user does not have Instagram installed ");
            //Launching in Browser
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(instaItem.getLink()));
                context.startActivity(browserIntent);
            } catch (ActivityNotFoundException e2) {
                Log.e(TAG, e2.getMessage() + "\n Perhaps user does not have Instagram installed ");
            }
        }
    }

    public void launchInstagramUser(InstaFeedModel instaItem) {
        String userURL = "https://instagram.com/_u/" + instaItem.getUser().getUsername().toString();
        try {
            Uri uri = Uri.parse(userURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.instagram.android");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage() + "\n Perhaps user does not have Instagram installed ");
            //Launching in Browser
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(userURL));
                context.startActivity(browserIntent);
            } catch (ActivityNotFoundException e2) {
                Log.e(TAG, e2.getMessage() + "\n Perhaps user does not have a Browser installed ");
            }
        }
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder {
        public ImageView instaImage;
        public ImageView instaDP;
        public TextView instaName;
        public TextView instaDescription;
        public TextView instaLikes;
        public TextView instaComments;
        public LinearLayout instaItem;

        public HomeViewHolder(View view) {
            super(view);
            initializeViews(view);
        }

        public void onBind(final InstaFeedModel instaItem) {
            instaName.setText(instaItem.getUser().getUsername());
            instaDescription.setText(instaItem.getCaption().getText());
//            Picasso.with(context).load(instaItem.getImages().getStandard_resolution().getUrl()).into(instaImage);
//            Picasso.with(context).load(instaItem.getUser().getProfile_picture()).into(instaDP);
            instaLikes.setText(Integer.toString(instaItem.getLikes().getCount()) + " likes");
            instaComments.setText(Integer.toString(instaItem.getComments().getCount()) + " comments");

            //Open Instagram App when user clicks on Image
            instaImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchInstagramImage(instaItem);
                }
            });
            //Open Instagram App when user clicks on username
            instaName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchInstagramUser(instaItem);
                }
            });
        }

        public void initializeViews(View view) {
            instaImage = view.findViewById(R.id.insta_feed_img_image_view);
            instaDP = view.findViewById(R.id.insta_feed_dp_image_view);
            instaName = view.findViewById(R.id.insta_feed_name_text_view);
            instaComments = view.findViewById(R.id.insta_feed_comments_text_view);
            instaDescription = view.findViewById(R.id.insta_feed_description_text_view);
            instaLikes = view.findViewById(R.id.insta_feed_likes_text_view);
            instaItem = view.findViewById(R.id.insta_feed_item_linear_layout);
        }
    }
}