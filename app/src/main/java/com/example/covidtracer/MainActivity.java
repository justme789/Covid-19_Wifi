package com.example.covidtracer;

import android.animation.Animator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.covidtracer.Model.UserModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    static String username;
    static String decrypted;
    static AES encDec = new AES();
    boolean safe = true;
    boolean doneContacts = false;
    boolean scanSuccess = false;
    boolean checkingContact = false;
    boolean animDone = false;
    boolean testing = false;
    boolean firstClick = false;
    boolean continuousBack = false;
    boolean background = false;
    BottomNavigationView navView;
    Bundle homeBundle = new Bundle();
    Bundle profileBundle = new Bundle();
    Fragment currentFragment;
    Map<String, Integer> dist;
    HomeFragment home = new HomeFragment();
    SearchFragment searchFragment = new SearchFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    SurveyFragment surveyFragment = new SurveyFragment();
    int people = 0;
    String tag;
    Stack<String> fragmentStack = new Stack<>();
    WifiManager wifiManager;

    //This method creates a field in every UserModel object in the datbase called positiveUser
    //this field will include the current if they ever submit a survey with covid symptoms
    public static void contactsToPositive() {
        //Realm API initialization
        Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
        //queries the contacts of the current user
        UserModel currentUser = realm.where(UserModel.class).equalTo("_id", username).findFirst();
        ArrayList<String> contacts = currentUser.getContacts();
        realm.executeTransaction(e -> {
            if (contacts != null) {
                //API call to create positive user field in realm with the current user
                for (String user : contacts) {
                    UserModel affectedUser = realm.where(UserModel.class).equalTo("_id", user).findFirst();
                    //check if this is the first positive user addition
                    if (affectedUser != null && !affectedUser.get_id().equals(username) && affectedUser.getContacts() != null && affectedUser.getContactsElapsed(username) != null) {
                        if (affectedUser.getContactsElapsed(username) > 900000) {
                            if (affectedUser.getPositive_contact() == null) {
                                affectedUser.setPositive_contact(username + "#" + affectedUser.getDateContacts(username));
                                affectedUser.setPositiveContactModified(new Date().getTime());
                                affectedUser.setPositive(true);
                            } else if (!affectedUser.getPositive_contact().contains(user)) {
                                affectedUser.setPositive_contact(affectedUser.getPositive_contact() + "," + username + "#" + affectedUser.getDateContacts(username));
                                affectedUser.setPositiveContactModified(new Date().getTime());
                                affectedUser.setPositive(true);
                            }
                        }
                    }
                }
            }
            currentUser.setPositive_contact(currentUser.getPositive_contact() + "," + username + "#" + new Date().getTime());
            currentUser.setPositiveContactModified(new Date().getTime());
            currentUser.setPositive(true);
        });
    }

    //This method when a user searches for a location. The method loops through BSSIDs and counts
    //the amount of user per wifi. Very similar to nearbyScan()
    public static int searchCrowd(ArrayList<String> bssids) {
        Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
        long highestCount = 0;
        for (String bssid : bssids) {
            long temp = 0;
            temp = realm.where(UserModel.class).contains("wifis", encDec.encrypt(bssid)).count();
            if (temp > highestCount) {
                highestCount = temp;
            }
        }
        return (int) highestCount;
    }

    public static void clean() {
        Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
        realm.executeTransaction(e -> {
            RealmResults<UserModel> oldUsers = realm.where(UserModel.class).lessThan("wifiLastModified", new Date().getTime() - (14 * 86400000)).findAll();
            RealmResults<UserModel> emptyUsers = realm.where(UserModel.class).lessThan("created", new Date().getTime() - (14 * 86400000)).equalTo("wifis", "null").findAll();
            oldUsers.deleteAllFromRealm();
            emptyUsers.deleteAllFromRealm();
        });
    }

    public void onPostResume() {
        super.onPostResume();
        safe = true;
        background = false;
    }

    public void onPause() {
        super.onPause();
        safe = false;
        background = true;
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        //Realm Database Initialization
        Realm.init(this);
        //Username from Register Activity
        username = getIntent().getStringExtra("username");
        decrypted = encDec.decrypt(username);
        getSupportFragmentManager().beginTransaction().replace(R.id.main, home).addToBackStack(null).commit();
        getSupportFragmentManager().beginTransaction().hide(home).commit();
        fragmentStack.push("home");
        //UI Setup
        //Setup bottom menu and colors
        navView = findViewById(R.id.bottomNav_view);
        navView.setBackgroundColor(0xFF1c1c1c);
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_selected},
                new int[]{android.R.attr.state_selected},
                new int[]{android.R.attr.state_focused}

        };

        int[] colors = new int[]{
                0xFF5E5E5E,
                0xFFEC5757,
                0xFF5E5E5E
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        navView.setItemIconTintList(colorStateList);
        navView.setItemTextColor(colorStateList);
        //fake(100);
        clean();
        //Welcom text and fade animation
        TextView welcome = findViewById(R.id.welcome);
        String welcomeText = "Welcome " + decrypted + "!";
        welcome.setText(welcomeText);
        welcome.animate().alpha(0f).setDuration(3000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                welcome.setVisibility(View.GONE);
                if (currentFragment.getClass() == HomeFragment.class && safe) {
                    getSupportFragmentManager().beginTransaction().show(home).commit();
                    firstClick = true;
                    currentFragment = home;
                }
                animDone = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        //current fragment is home by default
        currentFragment = home;
        //Check database for any positive contacts
        checkPositiveContacts();
        //Values for Homefragment to display crowdedness (currently 0)
        homeBundle.putString("param1", people + "");
        //Navigation bar user interaction
        navView.setOnNavigationItemSelectedListener(item -> {
            Realm checkRealm = Realm.getInstance(Realm.getDefaultConfiguration());
            continuousBack = false;
            if (!firstClick) {
                getSupportFragmentManager().beginTransaction().show(home).commit();
            }
            Fragment frag = null;
            tag = "";
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    frag = home;
                    tag = "home";
                    frag.setArguments(homeBundle);
                    break;
                case R.id.navigation_profile:
                    frag = profileFragment;
                    tag = "profile";
                    //Bundle with username to display user info (contacts and positive contacts)
                    checkPositiveContacts();
                    profileBundle.putString("param1", decrypted);
                    frag.setArguments(profileBundle);
                    break;
                case R.id.navigation_survey:
                    frag = surveyFragment;
                    tag = "survey";
                    break;

                case R.id.navigation_search:
                    frag = searchFragment;
                    tag = "search";
                    break;
            }

            //Survey Fragment cant be accessed if the current user didnt get in contact
            //with anyone(will have to wait for first scan)
            getSupportFragmentManager().beginTransaction().replace(R.id.main, frag, tag).addToBackStack(tag).commit();
            fragmentStack.push(tag);
            currentFragment = frag;
            return true;
        });

        //Wifi Manager Setup as provided from WiFiManager Documentation
        wifiManager = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        //Constant scans every 10 seconds
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean success = wifiManager.startScan();
                if (success) {
                    //Successful Scans
                    scanSuccess();
                } else {
                    //Failed Scans
                    scanFailure();
                }

                //Determining users with atleast one matching wifi and check for contacts
                nearbyScan();
                contact();
                checkMyself();
                checkPositiveContacts();
            }
        }, 0, 10000);
    }

    //This Method does the main part of the whole app: scanning wifis and measuring the distance
    //of the current user and every visible wifi. It uses the MongoDB Realm API to push that list
    //to the database.
    //This Method runs automatically in the background and stores wifis and distance.
    private void scanSuccess() {
        //wait for welcom animation to end and that the contact checking methind is not checking
        //for contacts so the users wifi doesnt change.
        if (!checkingContact && animDone && !testing) {
            //Default Configuration initialized in Register Class
            Realm scanRealm = Realm.getInstance(Realm.getDefaultConfiguration());
            List<ScanResult> results = wifiManager.getScanResults();
            //distance map where the string is the BSSID and the Integer is the distance in cm
            dist = new HashMap<>();
            for (ScanResult result : results) {
                //Free Space Path Loss equation which relies on the frequency and signal level to
                //provide an approximated distance to Access point
                double exp = (27.55 - (20 * Math.log10(result.frequency)) + Math.abs(result.level)) / 20.0;
                dist.put(encDec.encrypt(result.BSSID), (int) (Math.pow(10.0, exp) * 10));
            }
            //Realm Database execution to add the map of wifis and distances to the current user
            scanRealm.executeTransaction(e -> {
                UserModel userModel = scanRealm.where(UserModel.class).equalTo("_id", username).findFirst();
                userModel.setWifis(dist.toString());
                userModel.setWifiLastModified(new Date().getTime());
                //This is a check to make sure that the user's wifi list is not null and that
                //the map of distances is initialized and populated
                scanSuccess = true;
            });
        }
    }

    private void scanFailure() {
        //Does nothing
    }

    //This Method checks if the current area is crowded. It does so by using the list generated
    //from scanSuccess() and checking it with the Realm Database. The Realm API allows this by
    //querying searches and returns the count of every user that has matching wifis to the current
    //user. The highest count is then determined to be the amount of people around.
    private void nearbyScan() {
        //count of users
        long count = 0;
        //List of wifi BSSIDs
        assert Realm.getDefaultConfiguration() != null;
        Realm userChecking = Realm.getInstance(Realm.getDefaultConfiguration());
        //First scan check
        if (scanSuccess) {
            //Go through every scan and store the BSSID
            ArrayList<String> bssid = new ArrayList<>(dist.keySet());
            //RealmQuery of UserModels
            for (String wifi : bssid) {
                long temp = 0;
                temp = userChecking.where(UserModel.class).contains("wifis", wifi).count();
                if (count < temp) {
                    count = temp;
                }
            }
            //API request to count users whose wifi list contains one of the wifis that the
            //current user can see

            people = (int) count;
            //Refreshing the home fragment which has the UI that shows how crowded the current
            //spot is
            HomeFragment reload = new HomeFragment();
            homeBundle.putLong("param1", people);
            reload.setArguments(homeBundle);

            //Making sure that the app is not paused and that it is safe to replace fragments
            if (safe && currentFragment.getClass() == HomeFragment.class) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main, reload).commit();
            }
        }
    }

    //This Method checks how many users share the same wifis as the current user and stores the
    //BSSID and a list of said users in userPerBSSID then it checks how many wifis they share. So
    //there could be 15 users who share the same wifi but only one user who has the same wifis as
    //the current user.
    private void contact() {
        //Users per each wifi
        HashMap<String, ArrayList<String>> usersPerBSSID = new HashMap<>();
        //Nearest neighbors
        HashMap<String, ArrayList<Integer>> KNN = new HashMap<>();
        //distance per every pair of wifi
        ArrayList<Integer> distancePerNeighborPerWifi = new ArrayList<>();
        //API initialization
        Realm contact = Realm.getInstance(Realm.getDefaultConfiguration());
        if (scanSuccess) {
            //Starting contact check
            checkingContact = true;
            removeContacts();
            for (String scan : dist.keySet()) {
                //List of users that will be populated
                ArrayList<String> listOfUsers = new ArrayList<>();
                //List of UserModels returned from Realm API query
                RealmResults<UserModel> users = contact.where(UserModel.class).findAll();
                for (UserModel user : users) {
                    if (user.getWifis().contains(scan)) {
                        listOfUsers.add(user.get_id());
                    }
                }
                usersPerBSSID.put(scan, listOfUsers);
            }
            int distance = 0;
            //Map of users and how many wifis they share with the current user
            HashMap<String, Integer> usersRepeated = checker(usersPerBSSID);
            //every user in the list of users and how many wifis they share with current user
            for (String user : usersRepeated.keySet()) {
                //every wifi in the list of wifi and users that have that wifi
                for (String wifi : usersPerBSSID.keySet()) {
                    //every user in the wifi list
                    for (String usersPerWifi : usersPerBSSID.get(wifi)) {
                        if (user.equals(usersPerWifi)) {
                            //Realm call to get the UserModel of users that share wifis
                            UserModel userInArea = contact.where(UserModel.class).equalTo("_id", user).findFirst();
                            //Realm call to get the UserModel of the current user
                            UserModel currentUser = contact.where(UserModel.class).equalTo("_id", username).findFirst();
                            //Distance returned from UserModel
                            int currentUserDistance = currentUser.getDistance(wifi);
                            int userInAreaDistance = userInArea.getDistance(wifi);
                            //Check that user actually has said wifi in their wifi list (the wifi
                            //list could change if the user is moving around)
                            if (userInAreaDistance >= 0) {
                                distance = Math.abs(userInAreaDistance - currentUserDistance);
                                distancePerNeighborPerWifi.add(distance);
                            }
                        }
                    }
                }
                //Storing users and distance
                KNN.put(user, distancePerNeighborPerWifi);
            }
            doneContacts = true;
        }
        //copy to iterate over
        HashMap<String, ArrayList<Integer>> copy = new HashMap<>(KNN);
        for (Map.Entry<String, ArrayList<Integer>> entry : copy.entrySet()) {
            //removing any user from KNN wit a wifi distance that is greater than 100cm
            for (int i : entry.getValue()) {
                if (i > 200) {
                    KNN.remove(entry.getKey());
                }
            }
        }
        if (!KNN.isEmpty()) {
            KNN.remove(username);
            //Realm API call to store contacts
            contact.executeTransaction(e -> {
                UserModel userModel = contact.where(UserModel.class).equalTo("_id", username).findFirst();
                //first contact addition to user
                for (String key : KNN.keySet()) {
                    String previousContacts = userModel.getContactsMessy();
                    if (previousContacts == null) {
                        userModel.setContacts(key + "#" + new Date().getTime());
                        userModel.setContactLastModified(new Date().getTime());
                    } else if (!previousContacts.contains(key)) {
                        userModel.setContacts(previousContacts + ", " + key + "#" + new Date().getTime());
                        userModel.setContactLastModified(new Date().getTime());
                    } else {
                        int indexOfoldContact = userModel.getContactsMessy().indexOf(key);
                        Long elapsed = userModel.getContactsElapsed(key);
                        if (elapsed != null) {
                            Long date = userModel.getDateContacts(key);
                            elapsed = Math.abs(date - new Date().getTime()) + elapsed;
                            int indexOfComma = previousContacts.indexOf(",", indexOfoldContact);
                            if (indexOfComma < 0) {
                                indexOfComma = previousContacts.length();
                            }
                            userModel.setContacts(previousContacts.substring(0, indexOfoldContact + key.length()) + "#" + new Date().getTime() + "~" + elapsed + previousContacts.substring(indexOfComma));
                        } else {
                            int indexOfComma = previousContacts.indexOf(",", indexOfoldContact);
                            if (indexOfComma < 0) {
                                indexOfComma = previousContacts.length();
                            }
                            Long date = userModel.getDateContacts(key);
                            elapsed = Math.abs(date - new Date().getTime());
                            userModel.setContacts(previousContacts.substring(0, indexOfoldContact + key.length()) + "#" + new Date().getTime() + "~" + elapsed + previousContacts.substring(indexOfComma));
                        }
                    }
                }
                //switching from list to set to remove duplicates
                //adding values to profile bundle since the profile fragment handles UI for
                //contacts
                if (userModel.getContacts() != null) {
                    profileBundle.putString("param2", "" + userModel.getContacts().size());
                } else {
                    profileBundle.putString("param2", "" + 0);
                }
                profileFragment = new ProfileFragment();
                profileFragment.setArguments(profileBundle);
            });
        }
        //done checking contacts
        checkingContact = false;
    }

    //This method checks that the users share 3 or more wifis with the current user
    //which is very important for triangulation
    //@param map: HashMap of wifis BSSIDs and users per BSSID
    //@return HashMap of user and how many wifis they share with the current user
    private HashMap<String, Integer> checker(HashMap<String, ArrayList<String>> map) {
        ArrayList<String> allUsers = new ArrayList<>();
        HashMap<String, Integer> count = new HashMap<>();
        for (ArrayList<String> list : map.values()) {
            allUsers.addAll(list);
        }
        for (String users : allUsers) {
            //maps users with how many wifis they share with the current user
            count.put(users, Collections.frequency(allUsers, users));
        }
        //iterator to check tha no user shares less than 3 wifis
        Iterator<Map.Entry<String, Integer>> it = count.entrySet().iterator();
        while (it.hasNext()) {
            int number = it.next().getValue();
            //check that the current user is not in contact with themselves
            if (number < 3) {
                it.remove();
            }
        }
        return count;
    }

    private void removeContacts() {
        Realm contactRemover = Realm.getInstance(Realm.getDefaultConfiguration());
        UserModel currentUser = contactRemover.where(UserModel.class).equalTo("_id", username).findFirst();
        ArrayList<String> contacts = currentUser.getContacts();
        String messyContacts = currentUser.getContactsMessy();
        int millsPerMin = 60000;
        if (contacts != null) {
            for (String contact : contacts) {
                Long timeSinceLastScan = (new Date().getTime() - currentUser.getDateContacts(contact)) / millsPerMin;
                Long contactDuration = currentUser.getContactsElapsed(contact);
                if (contactDuration != null) {
                    if (contactDuration < 15 * millsPerMin) {
                        if (timeSinceLastScan > 5 * millsPerMin) {
                            int indexOfContact = messyContacts.indexOf(contact);
                            int indexOfComma = messyContacts.indexOf(",", indexOfContact);
                            //only contact in string
                            if (indexOfComma < 0 && contacts.size() == 1) {
                                currentUser.setContacts(null);
                            }
                            //last contact in string
                            else if (indexOfComma < 0 && contacts.size() > 1) {
                                currentUser.setContacts(messyContacts.substring(0, indexOfContact - 1));
                            }
                            //first contact in string
                            else if (indexOfComma > 0 && indexOfContact == 0) {
                                currentUser.setContacts(messyContacts.substring(indexOfComma + 1));
                            }
                            //center contact
                            else {
                                currentUser.setContacts(messyContacts.substring(0, indexOfContact) + messyContacts.substring(indexOfComma + 1));
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkMyself() {
        Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
        UserModel currentUser = realm.where(UserModel.class).equalTo("_id", username).findFirst();
        if (currentUser != null) {
            ArrayList<String> contacts = currentUser.getContacts();
            if (contacts != null) {
                realm.executeTransaction(e -> {
                    for (String contact : contacts) {
                        UserModel otherUser = realm.where(UserModel.class).equalTo("_id", contact).findFirst();
                        Long elapsed = currentUser.getContactsElapsed(contact);
                        if (elapsed != null) {
                            if (elapsed > 900000 && otherUser.isPositive()) {
                                if (currentUser.getPositive_contact() == null) {
                                    currentUser.setPositive_contact(contact + "#" + currentUser.getDateContacts(contact));
                                    currentUser.setPositiveContactModified(new Date().getTime());
                                } else if (!currentUser.getPositive_contact().contains(contact)) {
                                    currentUser.setPositive_contact(currentUser.getPositive_contact() + "," + contact + "#" + currentUser.getDateContacts(contact));
                                    currentUser.setPositiveContactModified(new Date().getTime());
                                }
                                currentUser.setPositive(true);
                            }
                        }
                    }
                });
            }
        }
    }

    //This method is called after UI initialization and it creates a badge over the profile icon
    //to bring the users attention that they've been in contact with a positive user
    private void checkPositiveContacts() {
        Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
        UserModel currentUser = realm.where(UserModel.class).equalTo("_id", username).findFirst();
        if (currentUser != null) {
            if (currentUser.getPositive_contact() != null) {
                Long dateOfPositive = currentUser.getDatePositives();
                if (dateOfPositive != null) {
                    //1,209,600,000
                    long fourteen = 1209600000;
                    //86,400,000
                    long daysInQuarantine = fourteen - Math.abs(new Date().getTime() - dateOfPositive);
                    navView.getOrCreateBadge(R.id.navigation_profile);
                    if (currentUser.getNotification() == 0) {
                        realm.executeTransaction(e -> {
                            currentUser.setNotification(1);
                        });
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "COVID19")
                                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                                .setContentTitle("Positive Contact")
                                .setContentText("Hey " + decrypted + ", youve been in contact with a positive user")
                                .setPriority(NotificationCompat.PRIORITY_HIGH);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                        notificationManager.notify(0, builder.build());
                        builder.setOngoing(true);
                    }
                    ProfileFragment reload = new ProfileFragment();
                    profileBundle.putLong("param3", daysInQuarantine);
                    reload.setArguments(profileBundle);
                    if (safe && currentFragment.getClass() == ProfileFragment.class) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main, profileFragment).commit();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!continuousBack) {
            fragmentStack.pop();
        }
        if (fragmentStack.isEmpty()) {
            moveTaskToBack(true);
        } else {
            getSupportFragmentManager().popBackStack();
            String nowFrag = fragmentStack.pop();
            int i = 0;
            if (nowFrag.equals("search")) {
                i = 1;
            } else if (nowFrag.equals("survey")) {
                i = 2;
            } else if (nowFrag.equals("profile")) {
                i = 3;
            }

            navView.getMenu().getItem(i).setChecked(true);
        }
        continuousBack = true;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Covid app";
            String description = "alarm user in case of contact";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("COVID19", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void deleteUser() {
        Realm realm = Realm.getInstance(Realm.getDefaultConfiguration());
        realm.executeTransaction(e -> {
            UserModel currentUser = realm.where(UserModel.class).equalTo("_id", username).findFirst();
            currentUser.deleteFromRealm();
        });
        File del = new File(MainActivity.this.getFilesDir() + "/text");
        if (del.delete()) {
            finishAndRemoveTask();
        }
    }

    private void fake(int tests) {
        Realm test = Realm.getInstance(Realm.getDefaultConfiguration());
        UserModel current = test.where(UserModel.class).equalTo("_id", username).findFirst();
        ArrayList<String> wifis = current.individualWifi();
        test.executeTransaction(e -> {
            for (int i = 0; i < tests; i++) {
                try {
                    UserModel testUser = test.createObject(UserModel.class, encDec.encrypt(i + ""));
                    for (String wifi : wifis) {
                        if (testUser.getWifis() == null) {
                            testUser.setWifis("{" + wifi + "=200");
                        } else {
                            testUser.setWifis(testUser.getWifis() + ", " + wifi);
                        }
                    }
                    testUser.setWifis(testUser.getWifis().substring(0, testUser.getWifis().length() - 1) + ", " + encDec.encrypt("00:02:8A:4D:7E:C6") + "=100}");
                    testUser.setContacts(current.getContactsMessy());
                } catch (io.realm.exceptions.RealmPrimaryKeyConstraintException x) {

                }
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
