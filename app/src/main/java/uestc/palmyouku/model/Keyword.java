package uestc.palmyouku.model;

/**
 * Created by Administrator on 2016/8/25.
 */
public class Keyword {
    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public String getKeyord() {
        return mKeyord;
    }

    public void setKeyord(String keyord) {
        mKeyord = keyord;
    }

    public int getSearchCount() {
        return mSearchCount;
    }

    public void setSearchCount(int searchCount) {
        mSearchCount = searchCount;
    }

    private String mKeyord;
    private String mLink;
    private int mSearchCount;
}

