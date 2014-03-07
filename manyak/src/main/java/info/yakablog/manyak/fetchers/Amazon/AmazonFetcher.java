package info.yakablog.manyak.fetchers.Amazon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import info.yakablog.manyak.ItemDetailActivity;
import info.yakablog.manyak.item.GenericItem;
import info.yakablog.manyak.item.specialItems.Book;
import info.yakablog.manyak.item.specialItems.DVD;

/**
 * Created by krnl7365 on 03/03/14.
 */
public class AmazonFetcher extends AsyncTask<String, Void, GenericItem> {

    /*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    private static final String AWS_ACCESS_KEY_ID = "XXXXXXXXXXXXXXXXXXXX";

    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXX/XXXXXXXXXXXXXX";

    private static final String AWS_ASSOCIATE_TAG = "XXXXXXXXX";

    /*
     * Use one of the following end-points, according to the region you are
     * interested in:
     *
     *      US: ecs.amazonaws.com
     *      CA: ecs.amazonaws.ca
     *      UK: ecs.amazonaws.co.uk
     *      DE: ecs.amazonaws.de
     *      FR: ecs.amazonaws.fr
     *      JP: ecs.amazonaws.jp
     *
     */
    private static final String ENDPOINT = "ecs.amazonaws.fr";

    private static Context context = null;

    private static ItemDetailActivity parent;

    @Override
    protected GenericItem doInBackground(String... strings) {
        String queryValue = strings[0];
        GenericItem itemToFetch = null;
        // Books
        itemToFetch = fetchResult(queryValue, "Books");
        if(itemToFetch == null) {
            itemToFetch = fetchResult(queryValue, "DVD");
            if(itemToFetch == null) {
                itemToFetch = fetchResult(queryValue, "VHS");
                if(itemToFetch == null) {
                    itemToFetch = fetchResult(queryValue, "Video");
                    if(itemToFetch == null) {
                        itemToFetch = fetchResult(queryValue, "Music");
                        if(itemToFetch == null) {
                            itemToFetch = fetchResult(queryValue, "VideoGames");
                            // TODO : tenter de fetcher un autre type...
                            if(itemToFetch == null) {
                                displayLongToast("You're doomed: this type of product is not supported\nor this product does not exist on Amazon!");
                            } else {
                                displayShortToast("It's a Video Game!");
                            }
                        } else {
                            displayShortToast("It's an Audio CD (or assimilated to Music)!");
                        }
                    } else {
                        displayShortToast("It's a Video (other than DVD or VHS)!");
                    }
                } else {
                    displayShortToast("It's a VHS (Okay, Amazon still sells that kind of stuff)!");
                }
            } else {
                displayShortToast("It's a DVD!");
            }
        } else {
            displayShortToast("It's a Book!");
        }
        return itemToFetch;
    }

    /*
     * The Item ID to lookup. The value below was selected for the US locale.
     * You can choose a different value if this value does not work in the
     * locale of your choice.
     */
    //private static final String ITEM_ID = "0545010225";
    public GenericItem fetchResult(String queryValue, String searchIndex) {
        /*
         * Set up the signed requests helper
         */
        SignedRequestsHelper helper;
        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String requestUrl = null;
        String title = null;
        int year = -1;
        String image = null;
        String productGroup = null;

        GenericItem itemFound = null;

        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("AssociateTag", AWS_ASSOCIATE_TAG);
        params.put("Version", "2011-08-01");
        params.put("Operation", "ItemLookup");
        params.put("SearchIndex", searchIndex);
        params.put("IdType", "EAN");
        params.put("ItemId", queryValue);
        params.put("ResponseGroup", "Medium");

        requestUrl = helper.sign(params);
        Log.d("Manyak::AmazonFetcher", "Signed Request is \"" + requestUrl + "\"");

        try {
            productGroup = fetchProductGroup(requestUrl);
            Log.d("Manyak::AmazonFetcher", "Signed productGroup is \"" + productGroup + "\"");
        } catch (RuntimeException e) {
            Log.d("Manyak::AmazonFetcher", "Failed to fetch ProductGroup: " + e.getMessage());
            e.printStackTrace();
        }

        Log.d("Manyak::AmazonFetcher", "ProductGroup = \"" + productGroup + "\", SearchIndex = \"" + searchIndex + "\"");
        displayLongToast("ProductGroup = \"" + productGroup + "\", SearchIndex = \"" + searchIndex + "\"");

        if(productGroup == null) {
            return null;
        } else if(productGroup.toLowerCase().equals("dvd")) {
            itemFound = new DVD();

            year = fetchYear(requestUrl);
            Log.d("Manyak::AmazonFetcher", "Signed year is \"" + year + "\"");

            ((DVD) itemFound).setYear(year);

        } else if(productGroup.toLowerCase().equals("book")) {
            itemFound = new Book();
            //((Book) itemFound).setIsbn(isbn);
        } else {
            itemFound = new GenericItem();
        }

        try {
            title = fetchTitle(requestUrl);
            Log.d("Manyak::AmazonFetcher", "Signed Title is \"" + title + "\"");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        try {
            image = fetchImage(requestUrl);
            Log.d("Manyak::AmazonFetcher", "Signed image is \"" + image + "\"");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        itemFound.setName(title);
        itemFound.setVignetteURL(image);

        try {
            URL url = new URL(image);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            itemFound.setVignette(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // TODO : return a GenericItem !
        if (productGroup == null) {
            return null;
        }
        return itemFound;
    }



    /*
     * Utility function to fetch the response from the service and extract the
     * title from the XML.
     */
    private static String fetchTitle(String requestUrl) {
        String title = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);
            Node titleNode = doc.getElementsByTagName("Title").item(0);
            title = titleNode.getTextContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return title;
    }

    /*
     * Utility function to fetch the response from the service and extract the
     * year from the XML.
     */
    private static int fetchYear(String requestUrl) {
        int year = -1;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);
            Node releaseDate = doc.getElementsByTagName("ReleaseDate").item(0);
            year = Integer.parseInt(releaseDate.getTextContent().substring(0, releaseDate.getTextContent().indexOf("-")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return year;
    }

    /*
     * Utility function to fetch the response from the service and extract the
     * image from the XML.
     */
    private static String fetchImage(String requestUrl) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);
            Node imageNode = doc.getElementsByTagName("Image").item(0);
            if(imageNode == null) {
                imageNode = doc.getElementsByTagName("MediumImage").item(0);
                if(imageNode == null) {
                    imageNode = doc.getElementsByTagName("SmallImage").item(0);
                    if(imageNode == null) {
                        imageNode = doc.getElementsByTagName("LargeImage").item(0);
                    }
                }
            }
            if(imageNode != null) {
                NodeList nodelist = imageNode.getChildNodes();
                for(int i = 0; i < nodelist.getLength(); i++) {
                    Node imgNode = nodelist.item(i);
                    String localName = imgNode.getLocalName();
                    if((localName != null && localName.toLowerCase().equals("url")) || imgNode.getTextContent().toLowerCase().startsWith("http://")) {
                        return imgNode.getTextContent();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /*
     * Utility function to fetch the response from the service and extract the
     * image from the XML.
     */
    private static String fetchProductGroup(String requestUrl) {
        String productGroup = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);
            Node productGroupNode = doc.getElementsByTagName("ProductGroup").item(0);
            productGroup = productGroupNode.getTextContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return productGroup;
    }

    private void displayShortToast(final String message) {
        parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parent.getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayLongToast(final String message) {
        parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parent.getBaseContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void setContext(Context context) {
        AmazonFetcher.context = context;
    }

    public static void setParent(ItemDetailActivity parent) {
        AmazonFetcher.parent = parent;
    }
}
