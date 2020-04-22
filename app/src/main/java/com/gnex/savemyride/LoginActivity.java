package com.gnex.savemyride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private  static final String TAG="LoginActivity";
    StateListDrawable states;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @BindView(R.id.edtTxtUserEmailLogin) EditText email;
    @BindView(R.id.edtTxtUserPasswordLogin) EditText password;
    @BindView(R.id.clErrorLogin) ConstraintLayout clErrorLogin;
    @BindView(R.id.imgEmailValidation) ImageView imgEmailValidation;
    @BindView(R.id.imgPasswordValidation) ImageView imgPasswordValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mAuth = FirebaseAuth.getInstance();
        states = new StateListDrawable();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
      //  updateUI(currentUser);
    }
    @OnClick(R.id.txtSignUp)
    public void openSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignUp.class);


        startActivity(intent);
    }


    @OnClick(R.id.btnLogin)
    public void login() {


    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                                imgEmailValidation.setImageResource(R.drawable.success_field);
                                imgEmailValidation.setVisibility(View.VISIBLE);
                                imgPasswordValidation.setImageResource(R.drawable.success_field);
                                imgPasswordValidation.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(LoginActivity.this, LobbyActivity.class);

                                intent.putExtra(EXTRA_MESSAGE, user.getUid());
                                startActivity(intent);
                    } else {

                                clErrorLogin.setVisibility(View.VISIBLE);
                                imgEmailValidation.setImageResource(R.drawable.error_field);
                                imgEmailValidation.setVisibility(View.VISIBLE);
                                imgPasswordValidation.setImageResource(R.drawable.error_field);
                                imgPasswordValidation.setVisibility(View.VISIBLE);
                                email.setActivated(true);
                                password.setActivated(true);


                    }

                }
            });
    }
}
