package com.example.covidtracer.Model;

import java.util.ArrayList;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * This class handles the object that will be stored in realm.
 * It contains all the necessary methods to get and set data for that realm object
 * Since objects are stored as strings, the getter methods will mainly focus on string
 * manipulation
 */

public class UserModel extends RealmObject {
    //realm object fields:
    //user's id which is also the primary key
    @PrimaryKey
    String _id;
    /**
     * "list" of wifis in form of a string which contains wifis and
     * distances to them
     */
    String wifis;
    /**
     * "list" of contacts in form of a string which contains name of
     * contact, when the contact was made, and time elapsed after contact
     */
    String contacts;
    //users that were in contact with this user and are positive
    String positive_contact;
    //when the user with created
    Long created;
    //when wifi list was last modified
    Long wifiLastModified;
    //when contacts were last modified
    Long contactLastModified;
    //when positives were last modified
    Long positiveContactModified;
    //current user positive check
    boolean isPositive;
    //UserModel constructor
    int notification;

    public UserModel() {
    }

    public String get_id() {
        return _id;
    }

    public String getWifis() {
        return wifis;
    }

    public void setWifis(String wifis) {
        this.wifis = wifis;
    }

    /**
     * Creates a list from wifis string
     *
     * @return arraylist of wifis from wifis string
     */

    public ArrayList<String> individualWifi() {
        ArrayList<String> wifisPerUser = new ArrayList<>();
        if (wifis != null) {
            boolean check = true;
            int firstEquals = wifis.indexOf("=");
            //make sure that the equal sign is not part of the wifis name
            while (check) {
                if (wifis.indexOf("=", firstEquals) - firstEquals == 1) {
                    firstEquals++;
                } else {
                    check = false;
                }
            }
            wifisPerUser.add(wifis.substring(1, wifis.indexOf("=")));
        }
        /**
         * loop over wifis string to get the wifis
         * each loop updates the location of the equal sign and comma
         * that encompass a wifi
         */
        for (int i = 0; i < wifis.length(); i++) {
            int indexOfComma = wifis.indexOf(",", i);
            int indexOfEquals = wifis.indexOf("=", indexOfComma);
            if (indexOfComma >= 0) {
                boolean equalCheck = true;
                while (equalCheck) {
                    if (wifis.indexOf("=", indexOfEquals) - indexOfEquals == 1) {
                        indexOfEquals++;
                    } else {
                        equalCheck = false;
                    }
                }
                wifisPerUser.add(wifis.substring(indexOfComma + 2, indexOfEquals));
                i = indexOfEquals;
            }
        }
        return wifisPerUser;
    }

    /**
     * Get the distance of the wifi to the user
     *
     * @param wifi the name of the wifi that the method will return the
     *             distance of
     * @return an int that represents the distance from the wifi to the user
     */
    public int getDistance(String wifi) {
        int wifiIndex = wifis.indexOf(wifi);
        int indexOfBreak = wifis.indexOf(",", wifiIndex);
        int distanceOfWifi = 0;
        if (wifiIndex > 0) {
            if (indexOfBreak > 0) {
                distanceOfWifi = Integer.parseInt(wifis.substring(wifiIndex + wifi.length() + 1, indexOfBreak));
            } else {
                distanceOfWifi = Integer.parseInt(wifis.substring(wifiIndex + wifi.length() + 1, wifis.length() - 1));
            }
        } else {
            distanceOfWifi = -1;
        }
        return distanceOfWifi;
    }


    public void setCreated(Long created) {
        this.created = created;
    }


    public void setWifiLastModified(Long wifiLastModified) {
        this.wifiLastModified = wifiLastModified;
    }


    public void setContactLastModified(Long contactLastModified) {
        this.contactLastModified = contactLastModified;
    }


    public void setPositiveContactModified(Long positiveContactModified) {
        this.positiveContactModified = positiveContactModified;
    }

    /**
     * Similar to individualWifi() but with contacts. This method returns the contacts
     * in the contacts string
     *
     * @return an arraylist containing contacts
     */
    public ArrayList<String> getContacts() {
        if (contacts != null) {
            ArrayList<String> contactsList = new ArrayList<>();
            contactsList.add(contacts.substring(0, contacts.indexOf("#")));
            for (int i = 0; i < contacts.length(); i++) {
                int indexOfComma = contacts.indexOf(",", i);
                int indexOfHash = contacts.indexOf("#", indexOfComma);
                if (indexOfComma >= 0 && indexOfHash >= 0) {
                    contactsList.add(contacts.substring(indexOfComma + 2, indexOfHash));
                    i = indexOfHash;
                }
            }
            return contactsList;
        }
        return null;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getContactsMessy() {
        return contacts;
    }

    public String getPositive_contact() {
        return positive_contact;
    }

    public void setPositive_contact(String positive_contact) {
        this.positive_contact = positive_contact;
    }

    /**
     * Gets the date a contact was made
     *
     * @param user the user in the contacts that this method will return
     *             the contact date of
     * @return a long that represents the time this contact was made in milliseconds
     */
    public Long getDateContacts(String user) {
        int indexOfUser = contacts.indexOf(user);
        int indexOfComma = contacts.indexOf(",", indexOfUser);
        int indexOfAprox = contacts.indexOf("~", indexOfUser);
        if (indexOfUser >= 0) {
            String dateString;
            if (indexOfComma > 0 && indexOfAprox > 0) {
                dateString = contacts.substring(indexOfUser + user.length() + 1, indexOfAprox);
            } else if (indexOfAprox < 0 && indexOfComma > 0) {
                dateString = contacts.substring(indexOfUser + user.length() + 1, indexOfComma);
            } else if (indexOfAprox > 0 && indexOfComma < 0) {
                dateString = contacts.substring(indexOfUser + user.length() + 1, indexOfAprox);
            } else {
                dateString = contacts.substring(indexOfUser + user.length() + 1);
            }
            return Long.parseLong(dateString);
        }
        return null;
    }

    /**
     * Similar to getDateContacts(user) but returns the time elapsed
     *
     * @param user the user that this method will return the time elapsed of
     * @return long representing the amount of time elapsed
     */
    public Long getContactsElapsed(String user) {
        if (contacts != null) {
            int indexOfUser = contacts.indexOf(user);
            int indexOfComma = contacts.indexOf(",", indexOfUser);
            if (indexOfUser >= 0) {
                String dateString;
                String dates;
                int indexOfAprox;
                if (indexOfComma > 0) {
                    dates = contacts.substring(indexOfUser + user.length() + 1, indexOfComma);
                    indexOfAprox = dates.indexOf("~");
                    dateString = dates.substring(indexOfAprox + 1);
                } else {
                    dates = contacts.substring(indexOfUser + user.length() + 1);
                    indexOfAprox = dates.indexOf("~");
                    dateString = dates.substring(indexOfAprox + 1);
                }
                if (indexOfAprox < 0) {
                    return null;
                } else return Long.parseLong(dateString);
            }
        }
        return null;
    }

    /**
     * Simillar to getContactsElapsed() but returns the date a positive contact
     * was made
     *
     * @return a long in the form of when a contact with a positive user was made
     */
    public Long getDatePositives() {
        ArrayList<Long> dates = new ArrayList<>();
        long highest = 0;
        if (positive_contact != null) {
            int indexOfHash = positive_contact.indexOf("#");
            int indexOfComma = positive_contact.indexOf(",", indexOfHash);
            String dateString = "";
            if (indexOfComma > 0) {
                boolean check = true;
                while (check) {
                    dateString = positive_contact.substring(indexOfHash + 1, indexOfComma);
                    dates.add(Long.parseLong(dateString));
                    indexOfHash = positive_contact.indexOf("#", indexOfComma);
                    indexOfComma = positive_contact.indexOf(",", indexOfHash);
                    if (indexOfComma < 0) {
                        dateString = positive_contact.substring(indexOfHash + 1);
                        dates.add(Long.parseLong(dateString));
                        check = false;
                    }
                }
            } else {
                dateString = positive_contact.substring(indexOfHash + 1);
                dates.add(Long.parseLong(dateString));
            }
            if (dates.size() == 1) {
                return dates.get(0);
            } else {
                for (Long date : dates) {
                    if (date > highest) {
                        highest = date;
                    }
                }
                return highest;
            }
        }
        return null;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    public int getNotification() {
        return notification;
    }

    public void setNotification(int notification) {
        this.notification = notification;
    }
}
