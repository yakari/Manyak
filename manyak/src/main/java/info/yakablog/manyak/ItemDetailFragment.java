package info.yakablog.manyak;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import info.yakablog.manyak.dummy.DummyContent;
import info.yakablog.manyak.fetchers.Amazon.AmazonFetcher;
import info.yakablog.manyak.item.GenericItem;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    private static final int PICK_CONTACT_REQUESTCODE =1337;
    private static Uri PICK_CONTACT_CONTENT_URI =null;
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    private View inflatedView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) inflatedView.findViewById(R.id.item_detail)).setText(mItem.content);
        }

        Button searchButton = (Button) inflatedView.findViewById(R.id.searchAmazonButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Manyak::ItemDetailFragment", "Hit the search button!");
                Activity host = (Activity) view.getContext();
                IntentIntegrator.initiateScan(host);
            }
        });

        Button lendButton = (Button) inflatedView.findViewById(R.id.lendButton);
        lendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Manyak::ItemDetailFragment", "Hit the lend button!");
                try {
                    Class<?> clazz = Class.forName("android.provider.ContactsContract$Contacts");
                    PICK_CONTACT_CONTENT_URI = (Uri)clazz.getField("CONTENT_URI").get(clazz);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if(PICK_CONTACT_CONTENT_URI != null) {
                    Intent i=new Intent(Intent.ACTION_PICK, PICK_CONTACT_CONTENT_URI);
                    getActivity().startActivityForResult(i, PICK_CONTACT_REQUESTCODE);
                }
            }
        });

        return inflatedView;
    }



    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                //View view = inflatedView.findViewById(R.layout.fragment_item_detail);
                String resultBarcode = scanResult.getContents();
                AmazonFetcher amazonFetcher = new AmazonFetcher();
                //amazonFetcher.setContext(this.getActivity());
                amazonFetcher.setParent((ItemDetailActivity) this.getActivity());
                String[] results = {resultBarcode};
                GenericItem itemFound = null;
                try {
                    itemFound = amazonFetcher.execute(results).get();
                    if (itemFound == null) {
                        Toast.makeText(this.getActivity(), "No result !", Toast.LENGTH_SHORT).show();
                    } else {
                        EditText title = (EditText) inflatedView.findViewById(R.id.itemTitle);
                        title.setText(itemFound.getName());

                        ImageView imageView = (ImageView) inflatedView.findViewById(R.id.productPicture);
                        Bitmap bmp = itemFound.getVignette();
                        if (bmp != null) {
                            imageView.setImageBitmap(bmp);
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //itemFound = amazonFetcher.fetchResult(resultBarcode);
                //EditText title = (EditText) inflatedView.findViewById(R.id.itemTitle);
                //title.setText(itemFound.getName());
            }
            // TODO : else continue with any other code you need in the method
        } else if (requestCode == PICK_CONTACT_REQUESTCODE) {
            if (resultCode==Activity.RESULT_OK) {
                Uri contactData = intent.getData();
                Cursor c =  this.getActivity().getContentResolver().query(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    EditText borrower = (EditText) inflatedView.findViewById(R.id.itemBorrower);
                    borrower.setText(name);
                    // TODO Whatever you want to do with the selected contact name.
                }
            }
        }
    }
}
