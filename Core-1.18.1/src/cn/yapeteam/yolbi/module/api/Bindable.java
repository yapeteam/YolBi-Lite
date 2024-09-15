package cn.yapeteam.yolbi.module.api;

public interface Bindable {
    int getKey();
    void setKey(int key);

    void onKey();

    String getName();
}
