package com.codepath.gogreen.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.codepath.gogreen.CommentAdapter;
import com.codepath.gogreen.DividerItemDecoration;
import com.codepath.gogreen.R;
import com.codepath.gogreen.models.Action;
import com.codepath.gogreen.models.Comment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.facebook.login.widget.ProfilePictureView.TAG;


public class DetailFragment extends Fragment {

    LayoutInflater inflater;
    View v;
    ImageView ivProfilePicDet;
    TextView tvAction;
    TextView tvPoints;
    TextView tvTimeStamp;
    TextView tvLikes;
    ImageButton ivFavorite;
    ImageButton ivReply;
    Context context;
    EditText etWriteComment;
    Button btComment;
    PieChart pieChart;
    String actionId;
    ArrayList<Comment> comments;
    RecyclerView rvComments;
    CommentAdapter commentAdapter;
    MaterialDialog modal;
    ParseUser user;

    String fuel, water, trees, emissions, numberOf, body;

    Action action;

    public static DetailFragment newInstance() {

        Bundle args = new Bundle();

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.comment_fragment, null);
        user = ParseUser.getCurrentUser();

        actionId = getArguments().getString("objectID");
        String relativeTime = getArguments().getString("relativeTime");
        body = getArguments().getString("body");
        context = getActivity();
        String points = getArguments().getString("points");

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(getArguments().getString("action"));
            action = Action.fromJSON(jsonObject);
            action.setObjectId(actionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ivProfilePicDet = (ImageView) v.findViewById(R.id.ivProfilePicDet);
        tvAction = (TextView) v.findViewById(R.id.tvAction);
        tvPoints = (TextView) v.findViewById(R.id.tvPoints);
        tvTimeStamp = (TextView) v.findViewById(R.id.tvTimeStamp);
        tvLikes = (TextView) v.findViewById(R.id.tvLikes);
        ivFavorite = (ImageButton) v.findViewById(R.id.ivFavorite);
        ivReply = (ImageButton) v.findViewById(R.id.ivReply);
        etWriteComment = (EditText) v.findViewById(R.id.etWriteComment);
        btComment = (Button) v.findViewById(R.id.btComment);
        rvComments = (RecyclerView) v.findViewById(R.id.rvComments);
        tvPoints.setText(points);
        tvTimeStamp.setText(relativeTime);

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("fbId", action.getUid());
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser author, ParseException e) {
                if (e == null) {
                    // load propic
                    String imgUrl = author.getString("profileImgUrl");
                    Log.d("imageeee", imgUrl);
                    Glide.with(context)
                            .load(imgUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .bitmapTransform(new CropCircleTransformation(context))
                            .into(ivProfilePicDet);
//
//                    load action body: bold name, compose rest of body using function below
                    String name = author.getString("name");
                    SpannableString str = new SpannableString(name + body);
                    str.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvAction.setText(str);
                } else if (e != null) {
                    Log.e("action", "Error: " + e.getMessage());
                }
            }
        });


        ParseQuery<Action> actionQuery = ParseQuery.getQuery("Action");
        actionQuery.whereEqualTo("objectId", actionId);
        actionQuery.getFirstInBackground(new GetCallback<Action>() {
            @Override
            public void done(Action object, ParseException e) {
                if (e == null) {
                    Log.d("actionComments", "reloading");

                    // load original comments
                    JSONArray commentJSON = object.getJSONArray("comments");
                    if (commentJSON == null) {
                        commentJSON = new JSONArray();
                        object.put("comments", commentJSON);
                    }
                    for (int i = 0; i < commentJSON.length(); i++) {
                        try {
                            Log.d("actionComments", "adding comment");
                            addComment(Comment.fromJSON(commentJSON.getJSONObject(i)));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                } else if (e != null) {
                    Log.e("points", "Error: " + e.getMessage());
                }
            }
        });


        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(comments);
        rvComments.setAdapter(commentAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setReverseLayout(true);
        rvComments.setLayoutManager(linearLayoutManager);
        rvComments.addItemDecoration(new DividerItemDecoration(getContext()));
        // set the adapter
        //  rvComments.setAdapter(commentAdapter);

        // add new comments from reply field
        btComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etWriteComment.getText().toString().equals("")) {
                    Comment comment = new Comment();
                    comment.setUid(user.getString("fbId"));
                    comment.setBody(etWriteComment.getText().toString());
                    Date date = new Date();
                    comment.setDate(date);
                    Log.d("timestamp created", date.toString());

                    addComment(comment);
                    JSONArray commentArray = action.getJSONArray("comments");
                    commentArray.put(comment.toJSON());
                    action.put("comments", commentArray);

                    action.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("actionComments", "new: " + String.valueOf(comments.size()));
                            etWriteComment.setText("");
                        }
                    });
                }
            }
        });
        pieChart = (PieChart) v.findViewById(R.id.pieChart);

        pieChart.setCenterText(points);
        try {
            drawPieChart(pieChart);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // load initial favorite state
        JSONArray favoritedBy = action.getFavorited();
        boolean isFaved = false;
        if(favoritedBy != null) {
            for (int a = 0; a < favoritedBy.length(); a++) {
                try {
                    if (String.valueOf(favoritedBy.get(a)).equals(user.getString("fbId"))) {
                        isFaved = true;
                        ivFavorite.setImageResource(R.drawable.ic_faved);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tvLikes.setText(String.valueOf(favoritedBy.length()));
        }
        if(!isFaved){
            ivFavorite.setImageResource(R.drawable.ic_fave);
        }


        modal = new MaterialDialog.Builder(getContext())
                .customView(v, false)
                .show();


        // Set favorite on click listener
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                boolean isFaved = false;
                ParseUser current = ParseUser.getCurrentUser();
                JSONArray favoritedBy = action.getFavorited();
                //   Log.d("hopeee", String.valueOf(ids));
                if (favoritedBy == null) {
                    favoritedBy = new JSONArray();
                }
                for (int i = 0; i < favoritedBy.length(); i++) {
                    try {
                        if (favoritedBy.get(i).equals(current.getString("fbId"))) {
                            isFaved = true;
                            favoritedBy.remove(i);
                            ivFavorite.setImageResource(R.drawable.ic_fave);
                            int likes = Integer.valueOf(tvLikes.getText().toString());
                            tvLikes.setText(String.valueOf(likes - 1));
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (!isFaved) {
                    favoritedBy.put(current.getString("fbId"));
                    ivFavorite.setImageResource(R.drawable.ic_faved);
                    int likes = Integer.valueOf(tvLikes.getText().toString());
                    tvLikes.setText(String.valueOf(likes + 1));
                }

                action.setFavorited(favoritedBy);
                action.saveInBackground();

            }
        });

    }

    public void drawPieChart(PieChart pChart) throws JSONException {
        final double[] yData = {action.getMagnitude() * action.getResourceData().getDouble("fuel"), action.getMagnitude() * action.getResourceData().getDouble("water"),
                action.getMagnitude() * action.getResourceData().getDouble("trees"), action.getMagnitude() * action.getResourceData().getDouble("emissions")};
        final String[] xData = {"Fuel", "Water", "Trees", "Emissions"};

        ArrayList<PieEntry> yEntry = new ArrayList<>();
        ArrayList<String> xEntry = new ArrayList<>();

        for (int i = 0; i < yData.length; i++) {
            double aYData = yData[i];
            yEntry.add(new PieEntry((float) aYData));
        }
        for (String aXData : xData) {
            xEntry.add(aXData);
        }
        PieDataSet dataSet = new PieDataSet(yEntry, "Environmental Impact");
        dataSet.setSliceSpace(3);
        dataSet.setValueTextSize(12);
        dataSet.setSelectionShift(7);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        //colors.add(Color.GREEN);
        colors.add(Color.rgb(153, 255, 153));
        //colors.add(Color.YELLOW);
        colors.add(Color.rgb(229, 255, 204));

        dataSet.setColors(colors);

        pChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: " + e.toString());
                Log.d(TAG, "onValueSelected: " + h.toString());

                int pos = e.toString().indexOf("E");
                String envValues = e.toString().substring(pos + 16);

                int vos = 0;
                for (int i = 0; i < yData.length; i++) {
                    if (yData[i] == Double.parseDouble(envValues)) {

                        String str = h.toString().substring(15,16);
                        Log.d("onValueSelected", str);
                        vos = Integer.parseInt(str);
                        break;
                    }
                }
                String str = h.toString().substring(14,15);
                Log.d("onValueSelected", str);
                vos = Integer.parseInt(str);


                String env = xData[vos];
                Log.d("MessagesF", String.valueOf(vos));

                if (env.equals("Emissions")) {
                    Toast.makeText(getContext(), env + " reduced by: " + envValues + "lbs", Toast.LENGTH_SHORT).show();
                } else if (env.equals("Water")){
                    Toast.makeText(getContext(), env + " saved: " + envValues + "L" , Toast.LENGTH_SHORT).show();
                }
                else if (env.equals("Fuel")){
                    Toast.makeText(getContext(), env + " saved: " + envValues + "L", Toast.LENGTH_SHORT).show();
                }
                else if (env.equals("Trees")){
                    Toast.makeText(getContext(), env + " saved: " + envValues + " " + env, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });

        PieData data = new PieData(dataSet);
        pChart.setData(data);
        pChart.invalidate();
        pChart.getLegend().setEnabled(false);
        pChart.getDescription().setEnabled(false);
        data.setDrawValues(false);
        pChart.setHoleRadius(50);
        pChart.setTransparentCircleAlpha(1);
        pChart.setRotationEnabled(true);
    }





    public void addComment(Comment comment) {
        // iterate through JSON array
        // for each entry, deserialize the JSON object
        Log.d("comment", comment.getUid());
        comments.add(comments.size(), comment);
        commentAdapter.notifyItemInserted(comments.size());
    }

}
