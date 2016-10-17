package com.riyase.realmcookiejar.db;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import okhttp3.Cookie;

/**
 * Created by riyase on 29/4/16.
 */
public class JarEntry extends RealmObject {

    @PrimaryKey
    private String url;
    private RealmList<RealmCookie> cookies;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RealmList<RealmCookie> getCookies() {
        return cookies;
    }

    public void setCookies(RealmList<RealmCookie> cookies) {
        this.cookies = cookies;
    }

    public static void update(JarEntry entry, List<Cookie> cookies ) {
        for (int i=0; i<cookies.size(); i++) {
            int pos = -1;
            for (int j=0; j<entry.getCookies().size(); j++) {
                if (RealmCookie.equalsName(entry.getCookies().get(j), cookies.get(i))) {
                    pos = j;
                    break;
                }
            }
            if (pos == -1) {
                entry.getCookies().add(RealmCookie.createRealmCookie(cookies.get(i)));
            } else {
                RealmCookie.update(entry.getCookies().get(pos), cookies.get(i));
            }
        }
    }

    public static List<Cookie> getOkCookies(JarEntry jarEntry) {
        if (jarEntry.getCookies().size() == 0) {
            return new ArrayList<>();
        } else {
            return getOkCookies(jarEntry.getCookies());
        }
    }

    private static List<Cookie> getOkCookies( RealmList<RealmCookie> realmCookies ) {
        List<Cookie> cookies = new ArrayList<>();
        for ( RealmCookie realmCookie : realmCookies ) {
            cookies.add( RealmCookie.createOkCookie( realmCookie ));
        }
        return cookies;
    }
}