package cp.wordbox;

import java.util.ArrayList;

/**
 * Created by Chantal on 05.11.2017.
 */

class Word {

    private String id;
    private String yourLang;
    private String yourLang2;
    private String yourLang3;
    private String otherLang;
    private String otherLang2;
    private String otherLang3;
    private String degree;

    public Word(String id,  String yourLang, String yourLang2, String yourLang3, String otherLang, String otherLang2, String otherLang3, String degree) {
        this.id = id;
        this.yourLang = yourLang;
        this.yourLang2 = yourLang2;
        this.yourLang3 = yourLang3;
        this.otherLang = otherLang;
        this.otherLang2 = otherLang2;
        this.otherLang3 = otherLang3;
        this.degree = degree;
    }

    public Word(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getYourLang() {
        return yourLang;
    }

    public void setYourLang(String yourLang) {
        this.yourLang = yourLang;
    }

    public String getOtherLang() {
        return otherLang;
    }

    public void setOtherLang(String otherLang) {
        this.otherLang = otherLang;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getYourLang2() {
        return yourLang2;
    }

    public void setYourLang2(String yourLang2) {
        this.yourLang2 = yourLang2;
    }

    public String getYourLang3() {
        return yourLang3;
    }

    public void setYourLang3(String yourLang3) {
        this.yourLang3 = yourLang3;
    }

    public String getOtherLang2() {
        return otherLang2;
    }

    public void setOtherLang2(String otherLang2) {
        this.otherLang2 = otherLang2;
    }

    public String getOtherLang3() {
        return otherLang3;
    }

    public void setOtherLang3(String otherLang3) {
        this.otherLang3 = otherLang3;
    }
}
