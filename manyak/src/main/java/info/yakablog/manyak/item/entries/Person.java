package info.yakablog.manyak.item.entries;

import java.util.Date;

/**
 * Created by krnl7365 on 03/03/14.
 */
public class Person {
    private String firstName;

    private String lastName;

    private Date birth;

    private String urlWikipedia;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirth() {
        return birth;
    }

    public void setBirth(Date birth) {
        this.birth = birth;
    }

    public String getUrlWikipedia() {
        return urlWikipedia;
    }

    public void setUrlWikipedia(String urlWikipedia) {
        this.urlWikipedia = urlWikipedia;
    }
}
