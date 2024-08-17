package cn.yapeteam.yolbi.bindable;

public interface Bindable {
    int getKey();
    void setKey(int key);

    void onKey();

    String getName();
}
