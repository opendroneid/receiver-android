package org.opendroneid.android.app.network.models.user;

import com.google.gson.annotations.SerializedName;

public class UserSensorsPostRequest {
    private String service;

    private String url;

    private String ref;

    @SerializedName("public")
    private int isPublic;

    @SerializedName("static")
    private int isStatic;

    private String icon;

    private String type;

    private int filter;

    private String ip;

    @SerializedName("master_name")
    private String masterName;

    public UserSensorsPostRequest(String service, String url, String ref, int isPublic, int isStatic, String icon, String type, int filter, String ip, String masterName) {
        this.service = service;
        this.url = url;
        this.ref = ref;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.icon = icon;
        this.type = type;
        this.filter = filter;
        this.ip = ip;
        this.masterName = masterName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public int getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(int isPublic) {
        this.isPublic = isPublic;
    }

    public int getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(int isStatic) {
        this.isStatic = isStatic;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
}
