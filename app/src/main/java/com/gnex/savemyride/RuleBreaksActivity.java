package com.gnex.savemyride;


import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gnex.savemyride.Models.RuleBreaks;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import butterknife.ButterKnife;

public class RuleBreaksActivity extends AppCompatActivity {

    private DatabaseReference myrefdatabase;
    RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_breaks);
        ButterKnife.bind(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        recyclerView = (RecyclerView) findViewById(R.id.simple_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myrefdatabase= FirebaseDatabase.getInstance().getReference().child("/RuleBreaks").child(currentUser.getUid());

        FirebaseRecyclerAdapter< RuleBreaks, BlogViewHolder> recyclerAdapter=new FirebaseRecyclerAdapter< RuleBreaks,RuleBreaksActivity.BlogViewHolder>(
                RuleBreaks.class,
                R.layout.rule_item,
                RuleBreaksActivity.BlogViewHolder.class,
                myrefdatabase
        ) {
            @Override
            protected void populateViewHolder(RuleBreaksActivity.BlogViewHolder viewHolder,  RuleBreaks model, int position) {
                viewHolder.setPlace(model.getPlace());
                viewHolder.setDateTime(model.getDateTile());
                viewHolder.setSpeed(model.getSpeed());
                viewHolder.setMaxSpeed(model.getMaxSpeed());
                viewHolder.setSign(model.getSign());
                final String promo_id=getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent detaisIntent=new Intent(RuleBreaksActivity.this,PromotionDetails.class);
//                        detaisIntent.putExtra("Promotion_ID",promo_id);
//                        startActivity(detaisIntent);
                    }
                });
            }
        };
        recyclerView.setAdapter(recyclerAdapter);

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.


    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView txtPlace;
        TextView txtMaxSpeed;
        TextView txtSpeed;
        TextView txtSign;
        TextView txtDateTime;

        ImageView imageView;

        public BlogViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            txtPlace = (TextView)itemView.findViewById(R.id.txtPlace);
            txtMaxSpeed = (TextView) itemView.findViewById(R.id.txtMaxSpeed);
            txtSpeed = (TextView)itemView.findViewById(R.id.txtSpeed);
            txtSign = (TextView) itemView.findViewById(R.id.txtSign);
            txtDateTime = (TextView)itemView.findViewById(R.id.txtDateTime);



        }

        public void setDateTime(String dateTile)
        {
            txtDateTime.setText("Date Time : "+dateTile);
        }
        public void setPlace(String discription)
        {
            txtPlace.setText("Place : "+discription);
        }
        public void setSpeed (Float speed)
        {
            txtSpeed.setText(" Your Speed : "+speed.toString());
        }
        public void setMaxSpeed (String maxSpeed)
        {
            txtMaxSpeed.setText(" Max Speed : "+maxSpeed);
        }
        public void  setSign(String sign)
        {
            txtSign.setText("Road Sign : "+sign);
        }


    }
}
