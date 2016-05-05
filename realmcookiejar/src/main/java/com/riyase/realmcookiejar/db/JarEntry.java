package com.riyase.realmcookiejar.db;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import okhttp3.Cookie;

/**
 * Created by voris on 29/4/16.
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

    public static void update(Realm realm, JarEntry entry, List<Cookie> cookies ) {
        for ( Cookie cookie : cookies ) {
            for (RealmCookie realmCookie : entry.getCookies()) {
                if ( RealmCookie.equalsName( realmCookie, cookie ) ) {
                    //Log.d(YaraCookiejar.TAG,"update  "+cookie.name()+":"+cookie.value());
                    RealmCookie.update(realmCookie, cookie);
                } else {
                    RealmCookie savedCookie = realm.copyToRealm( RealmCookie.createRealmCookie( cookie ));
                    //Log.d(YaraCookiejar.TAG,"put  "+savedCookie.getName()+":"+savedCookie.getValue());
                    entry.getCookies().add( savedCookie );
                }
            }
        }
    }

    public static List<Cookie> getOkCookies( JarEntry jarEntry ) {
        if ( jarEntry.getCookies().size() == 0 ) {
            return new ArrayList<>();
        } else {
            return getOkCookies( jarEntry.getCookies() );
        }
    }

    private static List<Cookie> getOkCookies( RealmList<RealmCookie> realmCookies ) {
        List<Cookie> cookies = new ArrayList<>();
        for ( RealmCookie realmCookie : realmCookies ) {
            cookies.add( RealmCookie.createOkCookie( realmCookie ));
            //Log.d(YaraCookiejar.TAG,"get  "+realmCookie.getName()+":"+realmCookie.getValue());
        }
        return cookies;
    }
}
