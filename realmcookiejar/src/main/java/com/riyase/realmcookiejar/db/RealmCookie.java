package com.riyase.realmcookiejar.db;

import android.text.TextUtils;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import okhttp3.Cookie;

/**
 * Created by riyase on 29/4/16.
 */
public class RealmCookie extends RealmObject{
    private String name;
    private String value;
    private long expiresAt;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    private boolean hostOnly;
    private boolean persistent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean isHostOnly() {
        return hostOnly;
    }

    public void setHostOnly(boolean hostOnly) {
        this.hostOnly = hostOnly;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public static RealmCookie createRealmCookie(Cookie cookie) {
        RealmCookie realmCookie = new RealmCookie();
        realmCookie.setName(cookie.name());
        realmCookie.setValue(cookie.value());
        realmCookie.setExpiresAt(cookie.expiresAt());
        realmCookie.setDomain(cookie.domain());
        realmCookie.setPath(cookie.path());
        realmCookie.setSecure(cookie.secure());
        realmCookie.setHttpOnly(cookie.httpOnly());
        realmCookie.setHostOnly(cookie.hostOnly());
        realmCookie.setPersistent(cookie.persistent());
        return realmCookie;
    }

    public static Cookie createOkCookie(RealmCookie  realmCookie) {
        Cookie.Builder builder = new Cookie.Builder();
        builder.name(realmCookie.getName())
                .value(realmCookie.getValue())
                .expiresAt(realmCookie.getExpiresAt())
                .domain(realmCookie.getDomain())
                .path(realmCookie.getPath());
        if (realmCookie.isSecure()) {
            builder.secure();
        }
        if (realmCookie.isHttpOnly()) {
            builder.httpOnly();
        }
        if (realmCookie.isHostOnly()) {
            builder.hostOnlyDomain(realmCookie.getDomain());
        }
        return builder.build();
    }

    public static RealmList<RealmCookie> createCookies(List<Cookie> cookies) {
        RealmList<RealmCookie> realmCookies = new RealmList<>();
        for (Cookie cookie : cookies) {
            realmCookies.add(createRealmCookie(cookie));
        }
        return realmCookies;
    }

    public static void update(RealmCookie realmCookie, Cookie cookie) {
        if (!TextUtils.isEmpty(cookie.value())) {
            realmCookie.setValue(cookie.value());
            realmCookie.setExpiresAt(cookie.expiresAt());
            realmCookie.setDomain(cookie.domain());
            realmCookie.setPath(cookie.path());
            realmCookie.setSecure(cookie.secure());
            realmCookie.setHttpOnly(cookie.httpOnly());
            realmCookie.setHostOnly(cookie.hostOnly());
            realmCookie.setPersistent(cookie.persistent());
        }
    }

    public static boolean equalsName(RealmCookie realmCookie, Cookie cookie) {
        return realmCookie.getName().equals(cookie.name());
    }

    public static boolean equals(RealmCookie realmCookie, Cookie cookie) {
        return realmCookie.getName().equals(cookie.name())
                && realmCookie.getName().equals(cookie.name())
                && realmCookie.getValue().equals(cookie.value())
                && realmCookie.getExpiresAt() == cookie.expiresAt()
                && realmCookie.getDomain().equals(cookie.name())
                && realmCookie.getPath().equals(cookie.name())
                && realmCookie.isSecure() == cookie.secure()
                && realmCookie.isHttpOnly() == cookie.httpOnly()
                && realmCookie.isHostOnly() == cookie.hostOnly()
                && realmCookie.isPersistent() == cookie.persistent();
    }
}
