package com.tvisha.trooponprime.ui.recent.story;

import static com.tvisha.trooponprime.MyApplication.troopClient;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.tvisha.trooponprime.R;
import com.tvisha.trooponprime.lib.clientModels.StoryModel;
import com.tvisha.trooponprime.lib.utils.Helper;
import com.tvisha.trooponprime.ui.recent.MainActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public
class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

   // on below line we are creating a int array
   // in which we are storing all our image ids.
   private final int[] resources = new int[]{
           /*R.drawable.logo1,
           R.drawable.logo2,
           R.drawable.logo1,
           R.drawable.logo2,
           R.drawable.logo1,
           R.drawable.logo2,*/
   };
   List<StoryModel> storyModels = new ArrayList<>();

   // on below line we are creating variable for
   // our press time and time limit to display a story.
   long pressTime = 0L;
   long limit = 500L;

   // on below line we are creating variables for
   // our progress bar view and image view .
   private StoriesProgressView storiesProgressView;
   private ImageView image;
   private ImageView ivback;
   private ImageView ivProfilePic;
   private ImageView videoButton;
   private VideoView videoView;
   private TextView textView;
   private TextView tvName;



   // on below line we are creating a counter
   // for keeping count of our stories.
   private int counter = 0;

   // on below line we are creating a new method for adding touch listener
   private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
         // inside on touch method we are
         // getting action on below line.
         switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

               // on action down when we press our screen
               // the story will pause for specific time.
               pressTime = System.currentTimeMillis();

               // on below line we are pausing our indicator.
               storiesProgressView.pause();
               return false;
            case MotionEvent.ACTION_UP:

               // in action up case when user do not touches
               // screen this method will skip to next image.
               long now = System.currentTimeMillis();

               // on below line we are resuming our progress bar for status.
               storiesProgressView.resume();

               // on below line we are returning if the limit < now - presstime
               return limit < now - pressTime;
         }
         return false;
      }
   };

   String userID = "";
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // inside in create method below line is use to make a full screen.
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      setContentView(R.layout.activity_stories);

      userID = getIntent().getStringExtra("user_id");
      troopClient.fetchStoriesByUserId(Long.parseLong(userID)).observe(this, new Observer<List<StoryModel>>() {
                 @Override
                 public void onChanged(List<StoryModel> list) {
                    storyModels = list;
                    troopClient.fetchStoriesByUserId(Long.parseLong(userID)).removeObserver(this);
                    Log.e("size==> ",""+storyModels.size());
                    if (storyModels.size()>0) {
                       updateStories();
                    }else {
                       finish();
                    }
                 }
              });


   }

   private void updateStories() {
      // on below line we are initializing our variables.
      storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);

      // on below line we are setting the total count for our stories.
      storiesProgressView.setStoriesCount(storyModels.size());

      // on below line we are setting story duration for each story.
      storiesProgressView.setStoryDuration(3000L);

      // on below line we are calling a method for set
      // on story listener and passing context to it.
      storiesProgressView.setStoriesListener(this);

      // below line is use to start stories progress bar.
      storiesProgressView.startStories(counter);

      // initializing our image view.
      image = findViewById(R.id.image);
      ivback = findViewById(R.id.ivback);
      ivProfilePic = findViewById(R.id.ivProfilePic);
      tvName = findViewById(R.id.tvName);
      videoView =  findViewById(R.id.video);
      videoButton =  findViewById(R.id.videoButton);
      textView = findViewById(R.id.tvText);
      ivback.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            onBackPressed();
         }
      });

      StoryModel storyModel = storyModels.get(0);
      tvName.setText(storyModel.getUser_name()+" - "+Helper.INSTANCE.getRecentListMessagesDateTime(storyModel.getCreated_at()));
      Glide.with(this).load(storyModel.getProfile_pic()).circleCrop().placeholder(R.drawable.default_user).into(ivProfilePic);
      // on below line we are setting image to our image view.
      //image.setImageResource(storyModels.get(counter).getData());



      setStories(counter);

      // below is the view for going to the previous story.
      // initializing our previous view.
      View reverse = findViewById(R.id.reverse);

      // adding on click listener for our reverse view.
      reverse.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // inside on click we are
            // reversing our progress view.
            storiesProgressView.reverse();
         }
      });

      // on below line we are calling a set on touch
      // listener method to move towards previous image.
      reverse.setOnTouchListener(onTouchListener);

      // on below line we are initializing
      // view to skip a specific story.
      View skip = findViewById(R.id.skip);
      skip.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // inside on click we are
            // skipping the story progress view.
            storiesProgressView.skip();
         }
      });
      // on below line we are calling a set on touch
      // listener method to move to next story.
      skip.setOnTouchListener(onTouchListener);
   }

   public void setStories(int position) {
      int type = storyModels.get(position).getType();
      if (storyModels != null && storyModels.get(position)!=null){
         troopClient.storyViewed(storyModels.get(position).getId());
      }
      switch (type){
         case 1:
            textView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            videoButton.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            JSONObject data = Helper.INSTANCE.stringToJsonObject(storyModels.get(position).getData());
            if (data!=null){
               textView.setText(data.optString("text"));
               if (data.optString("bgCode").contains("#")) {
                  textView.setBackgroundColor(Color.parseColor(data.optString("bgCode")));
               }else {
                  textView.setBackgroundColor(Color.parseColor("#"+data.optString("bgCode")));
               }
            }

            break;
         case 2:
            textView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            videoButton.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            Glide.with(this).load(Helper.INSTANCE.stringToJsonObject(storyModels.get(position).getData()).optString("url"))
                    .into(image);
            break;
         case 3:
            textView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            videoButton.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
            Glide.with(this).load(Helper.INSTANCE.stringToJsonObject(storyModels.get(position).getData()).optString("url"))
                    .into(image);
            break;
      }
   }
   @Override
   public void onNext() {
      // this method is called when we move
      // to next progress view of story.
      setStories(++counter);

      //image.setImageResource(resources[++counter]);
   }

   @Override
   public void onPrev() {

      // this method is called when we move to previous story.
      // on below line we are decreasing our counter
      if ((counter - 1) < 0) return;

      setStories(--counter);
      // on below line we are setting image to image view
      //image.setImageResource(resources[--counter]);
   }

   @Override
   public void onComplete() {
      // when the stories are completed this method is called.
      // in this method we are moving back to initial main activity.
      /*Intent i = new Intent(StoryActivity.this, MainActivity.class);
      startActivity(i);*/
      finish();
   }

   @Override
   protected void onDestroy() {
      // in on destroy method we are destroying
      // our stories progress view.
      if (storiesProgressView!=null) {
         storiesProgressView.destroy();
      }
      super.onDestroy();
   }
}
