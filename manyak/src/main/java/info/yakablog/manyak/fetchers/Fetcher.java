package info.yakablog.manyak.fetchers;

import info.yakablog.manyak.item.GenericItem;

/**
 * Created by krnl7365 on 03/03/14.
 */
public interface Fetcher {
    public GenericItem fetchResult(String queryValue);
}
