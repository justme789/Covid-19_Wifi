package com.example.covidtracer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.covidtracer.Model.UserModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Register extends AppCompatActivity {
    /**
     * this class handles Realm initialization, registeration GUI,
     * and the first realm object storage.
     */
    Button register;
    Realm realm;
    String username;
    boolean grantedPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Realm initialization and user authentication
        super.onCreate(savedInstanceState);
        boolean exists = false;
        if (Realm.getDefaultConfiguration() == null) {
            Realm.init(this);
            String appID = "application-0-cvrjc";
            App app = new App(new AppConfiguration.Builder(appID)
                    .build());
            if (app.currentUser() == null) {
                Credentials credentials = Credentials.anonymous();
                app.loginAsync(credentials, result -> {
                    User user = app.currentUser();
                    String partitionValue = "_partition_key_none";
                    SyncConfiguration config = new SyncConfiguration.Builder(
                            user,
                            partitionValue)
                            .allowWritesOnUiThread(true)
                            .allowQueriesOnUiThread(true)
                            .build();
                    realm = Realm.getInstance(config);
                    //Setting this configuration as the default one that
                    //will be used later
                    Realm.setDefaultConfiguration(config);
                });
            } else {
                User user = app.currentUser();
                String partitionValue = "_partition_key_none";
                SyncConfiguration config = new SyncConfiguration.Builder(
                        user,
                        partitionValue)
                        .allowWritesOnUiThread(true)
                        .allowQueriesOnUiThread(true)
                        .build();
                realm = Realm.getInstance(config);
                Realm.setDefaultConfiguration(config);
            }
        }
        //Existing user check
        if (read().length() > 1 && read() != null) {
            UserModel user = realm.where(UserModel.class).equalTo("_id", read()).findFirst();
            if (user != null) {
                switchActivities(read());
                exists = true;
            }
        }
        //Permissions check
        boolean location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean readWrite = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        //Permissions request
        if (!location && !readWrite) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
        } else {
            grantedPermissions = true;
        }

        if (!exists) {
            setContentView(R.layout.register);
            register = findViewById(R.id.register_butotn);
            EditText usernameField = findViewById(R.id.username_field);
            usernameField.setOnClickListener(e -> {
                usernameField.setText("");
            });
            usernameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (grantedPermissions) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            userAndRealm(usernameField);
                        }
                        return false;
                    } else {
                        Toast.makeText(Register.this, "Permissions have not been granted", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
            register.setOnClickListener(e -> {
                if (grantedPermissions) {
                    userAndRealm(usernameField);
                } else {
                    Toast.makeText(Register.this, "Permissions have not been granted", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Creating a UserModel in realm
     *
     * @param usernameField the registration field
     */
    private void userAndRealm(EditText usernameField) {
        //User creation
        username = usernameField.getText().toString();
        if (username.length() < 5) {
            Toast.makeText(this, "Username has to be more than 5 letters", Toast.LENGTH_SHORT).show();
        } else if (username.length() > 24) {
            Toast.makeText(this, "Username has to be less than 24 letters", Toast.LENGTH_SHORT).show();
        } else {
            try {
                realm.executeTransaction(a -> {
                    AES encryptor = new AES();
                    Long creation = new Date().getTime();
                    //Username encryption
                    String encryptedUser = encryptor.encrypt(username);
                    UserModel user1 = realm.createObject(UserModel.class, encryptedUser);
                    usernameField.setText("");
                    user1.setWifis("null");
                    user1.setPositive(false);
                    user1.setCreated(creation);
                    user1.setNotification(0);
                    Toast.makeText(getApplicationContext(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                    write(encryptedUser);
                    switchActivities(encryptedUser);
                });
            } catch (io.realm.exceptions.RealmPrimaryKeyConstraintException x) {
                Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * This method switches from this Actvity to the MainActivity
     *
     * @param username the username that will be sent to MainActivity
     */
    private void switchActivities(String username) {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        switchActivityIntent.putExtra("username", username);
        startActivity(switchActivityIntent);
        finish();
    }

    //Reading saved username
    private String read() {
        BufferedReader br = null;
        String s = "";
        try {
            br = new BufferedReader(new FileReader(Register.this.getFilesDir() + "/text"));
            s = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    //Writing username
    private void write(String s) {
        File file = new File(Register.this.getFilesDir(), "text");
        try {
            FileWriter usernameSave = new FileWriter(file);
            usernameSave.append(s);
            usernameSave.flush();
            usernameSave.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //This method creats a pop up that requests the user for location and storage permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == 1) {
                grantedPermissions = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
        }
    }
}
