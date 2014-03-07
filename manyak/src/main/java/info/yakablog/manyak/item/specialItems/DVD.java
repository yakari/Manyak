package info.yakablog.manyak.item.specialItems;

import java.util.List;

import info.yakablog.manyak.item.GenericItem;
import info.yakablog.manyak.item.entries.Actor;

/**
 * Created by krnl7365 on 04/03/14.
 */
public class DVD extends GenericItem {
    private int year;

    private List<Actor> actors;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public void addActor(Actor actor) {
        actors.add(actor);
    }
}
