package com.riyase.realmcookiejar;

import android.util.Log;


import com.riyase.realmcookiejar.db.JarEntry;
import com.riyase.realmcookiejar.db.RealmCookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmResults;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by riyase on 29/4/16.
 */
public class RealmCookiejar implements CookieJar {

    public static final String TAG = "RealmCookieJar";
    //public static final String REALM_FILE_COOKIEJAR = "cookiejar.realm";
    private final HashMap<HttpUrl, List<Cookie>> cookieCache = new HashMap<>();
    private RealmConfiguration configuration;
    private boolean hostAsKey = false;

    public RealmCookiejar() {
        configuration = new RealmConfiguration.Builder()
                //.name(REALM_FILE_COOKIEJAR)
                .modules(new Module())
                //.encryptionKey()
                .schemaVersion(0)
                .build();
        Realm realm = Realm.getInstance(configuration);
        RealmResults<JarEntry> jarEntries = realm.where(JarEntry.class)
                .findAll();
        for (JarEntry entry : jarEntries) {
            List<Cookie> cookies = JarEntry.getOkCookies(entry);
            HttpUrl keyUrl = HttpUrl.parse(entry.getUrl());
            cookieCache.put(keyUrl,cookies);
        }
        realm.close();
    }

    public void hostAsKey(boolean hostAsKey) {
        this.hostAsKey = hostAsKey;
    }

    public boolean isHostAsKey() {
        return hostAsKey;
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String keyUrl;
        if (isHostAsKey()) {
            HttpUrl hostUrl = new HttpUrl.Builder()
                    .host(url.host())
                    .scheme(url.scheme())
                    .build();
            keyUrl = hostUrl.toString();
            cookieCache.put(hostUrl, cookies);
        } else {
            keyUrl = url.toString();
            cookieCache.put(url, cookies);
        }
        Thread thread = new Thread() {

            String url;
            List<Cookie> cookies;
            public Thread init(String url, List<Cookie> cookies) {
                this.url = url;
                this.cookies = cookies;
                return this;
            }

            public void run() {
                Realm realm = Realm.getInstance(configuration);
                try {
                    realm.beginTransaction();
                    JarEntry entry = realm.where(JarEntry.class).equalTo("url", url).findFirst();
                    if (entry == null) {
                        RealmList<RealmCookie> realmCookies = RealmCookie.createCookies(cookies);
                        entry = new JarEntry();
                        entry.setUrl(url);
                        entry.setCookies(realmCookies);
                        realm.copyToRealmOrUpdate(entry);
                    } else {
                        JarEntry.update(entry, cookies);
                    }
                    realm.commitTransaction();
                } catch (Exception e) {
                    if (realm.isInTransaction()) {
                        realm.cancelTransaction();
                    }
                } finally {
                    realm.close();
                }
            }
        }.init(keyUrl, cookies);
        thread.start();
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies;
        if (isHostAsKey()) {
            HttpUrl hostUrl = new HttpUrl.Builder()
                    .host(url.host())
                    .scheme(url.scheme())
                    .build();
            cookies = cookieCache.get(hostUrl);
        } else {
            cookies = cookieCache.get(url);
        }
        if (cookies != null) {
            return cookies;
        } else {
            return new ArrayList<>();
        }
    }

    public void clear() {
        cookieCache.clear();
        Thread thread = new Thread() {
            public void run() {
                Realm realm = Realm.getInstance(configuration);
                try {
                    realm.beginTransaction();
                    realm.deleteAll();
                    realm.commitTransaction();
                } catch (Exception e) {
                    if (realm.isInTransaction()) {
                        realm.cancelTransaction();
                    }
                } finally {
                    realm.close();
                }
            }
        };
        thread.start();
    }
}
