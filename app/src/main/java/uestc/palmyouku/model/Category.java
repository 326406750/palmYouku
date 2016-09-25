package uestc.palmyouku.model;

/**
 * Created by Administrator on 2016/8/23.
 */
public class Category {
    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    private int mId;
    private String mName;
}
