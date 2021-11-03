package com.example.welly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증 처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Welly");

        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mBtnRegister = findViewById(R.id.btn_register);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //회원가입 처리 시작
                 String strEmail = mEtEmail.getText().toString();
                 String strPwd = mEtPwd.getText().toString();

                 //파이어베이스 인증
                 mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()) {
                             FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                             UserAccount account = new UserAccount();
                             account.setIdToken(firebaseUser.getUid()); //고유값
                             account.setEmailId(firebaseUser.getEmail());
                             account.setPassword(strPwd);
                             //setValue  데이터베잇 삽입
                             mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                             Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                             startActivity(intent);

                             Toast.makeText(RegisterActivity.this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT);
                         } else {
                             Toast.makeText(RegisterActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT);
                         }
                     }
                 });

            }
        });
    }
}