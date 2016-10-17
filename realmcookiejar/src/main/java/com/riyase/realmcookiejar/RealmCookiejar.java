package com.riyase.realmcookiejar;

import android.util.Log;


import com.riyase.realmcookiejar.db.JarEntry;
import com.riyase.realmcookiejar.db.RealmCookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by riyase on 29/4/16.
 */
public class RealmCookiejar implements CookieJar {

    public static final String TAG = "RealmCookieJar";
    private final HashMap<HttpUrl, List<Cookie>> cookieCache = new HashMap<>();
    private RealmConfiguration configuration;
    private boolean hostAsKey = false;

    public RealmCookiejar(RealmConfiguration configuration) {
        this.configuration = configuration;
        Realm realm = Realm.getInstance( configuration );
        RealmResults<JarEntry> jarEntries = realm.where(JarEntry.class)
                .findAll();
        for ( JarEntry entry : jarEntries ) {
            List<Cookie> cookies = JarEntry.getOkCookies(entry);
            HttpUrl keyUrl = HttpUrl.parse(entry.getUrl());
            cookieCache.put(keyUrl,cookies);
        }
        realm.close();
    }

    public void hostAsKey( boolean hostAsKey ) {
        this.hostAsKey = hostAsKey;
    }

    public boolean isHostAsKey() {
        return hostAsKey;
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

        String keyUrl;
        if (isHostAsKey()) {
            Log.d(TAG,"save url:"+url.host());
            keyUrl = url.host();
            HttpUrl httpUrl = HttpUrl.parse(keyUrl);
            cookieCache.put(httpUrl, cookies);
        } else {
            Log.d(TAG,"save url:"+url.toString());
            keyUrl = url.toString();
            cookieCache.put(url, cookies);
        }
        Log.d(TAG,"save cookies:"+cookies.toString());

        Realm realm = Realm.getInstance(configuration);
        realm.executeTransactionAsync(new Realm.Transaction() {

            String url;
            List<Cookie> cookies;

            public Realm.Transaction init(String url, List<Cookie> cookies) {
                this.url = url;
                this.cookies = cookies;
                return this;
            }

            @Override
            public void execute(Realm realm) {
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
            }
        }.init(keyUrl, cookies));
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {

        List<Cookie> cookies;
        if ( isHostAsKey() ) {
            Log.d(TAG, "load url:" + url.host());
            cookies = cookieCache.get(HttpUrl.parse(url.host()));
        } else {
            Log.d(TAG, "load url:" + url.toString());
            cookies = cookieCache.get(url);
        }

        if ( cookies != null ) {
            Log.d(TAG, "load cookies:" + cookies.toString());
            return cookies;
        } else {
            return new ArrayList<>();
        }
    }

    public void clear() {
        cookieCache.clear();
        Realm realm = Realm.getInstance(configuration);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(JarEntry.class).findAll().deleteAllFromRealm();
                realm.where(RealmCookie.class).findAll().deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {

            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {

            }
        });
    }
}
