package com.example.vinay.epubscreenreader;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;


public class EpubReaderActivity extends ListActivity
{

    private static final String TAG = "EpubReader" ;
    private LayoutInflater inflater;
    private List<RowData> contentDetails;
    public static final String BOOK_NAME = "books/AgniGundam.epub";
    Book book;
    EditText editText;
    Button open_button,button_file,button_change;
    CustomAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        editText = (EditText) findViewById(R.id.editText);
        open_button = (Button)findViewById(R.id.button);
        button_file = (Button)findViewById(R.id.file_button);
        button_change = (Button)findViewById(R.id.button_change);
        contentDetails = new ArrayList<RowData>();
        AssetManager assetManager = getAssets();
        copyAssests();
        try {
            InputStream epubInputStream = assetManager.open(BOOK_NAME);
            book = (new EpubReader()).readEpub(epubInputStream);
            //logContentsTable(book.getTableOfContents().getTocReferences(), 0);
            logData();

        } catch (IOException e) {
            Log.e("epublib", e.getMessage());
        }

        adapter = new CustomAdapter(this, R.layout.list,
                R.id.title, contentDetails);
        setListAdapter(adapter);
        getListView().setTextFilterEnabled(true);
        open_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        load_book(editText.getText().toString());
                    }
                }
        );
        button_file.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFileChooser();  // make to choose files
                    }
                }
        );
        button_change.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        load_book2(editText.getText().toString());
                    }
                }
        );
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    0);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
         Load ebook from the path
     */
    private void load_book(String path){
        try {
            InputStream epubInputStream = new FileInputStream(path);
            book = (new EpubReader()).readEpub(epubInputStream);
            contentDetails.clear();
            logContentsTable(book.getTableOfContents().getTocReferences(), 0);

            //logData();

        } catch (IOException e) {
            Log.e("epublib", e.getMessage());
        }
        adapter = new CustomAdapter(this, R.layout.list,
                R.id.title, contentDetails);
        setListAdapter(adapter);
        getListView().setTextFilterEnabled(true);
    }
    /*
        Alternate loading strategy
     */
    private void load_book2(String path){
        try {
            InputStream epubInputStream = new FileInputStream(path);
            book = (new EpubReader()).readEpub(epubInputStream);
            contentDetails.clear();
            //logContentsTable(book.getTableOfContents().getTocReferences(), 0);

            logData();

        } catch (IOException e) {
            Log.e("epublib", e.getMessage());
        }
        adapter = new CustomAdapter(this, R.layout.list,
                R.id.title, contentDetails);
        setListAdapter(adapter);
        getListView().setTextFilterEnabled(true);
    }
    /*
        Description - Function to get all the resource files of the ebook
     */
    private void logData() {
        //adding one more

        List<Resource> list = book.getContents();
        for(Resource r:list){
            RowData rowData = new RowData();
            RowData rowData2 = new RowData();
            rowData.setTitle(r.getHref());
            rowData2.setTitle(r.getId());
            rowData.setResource(r);
            rowData2.setResource(r);
            contentDetails.add(rowData);
            //contentDetails.add(rowData2);
        }
    }
//  Custom Adapter to display the files of each chapter in ListView
    private class CustomAdapter extends ArrayAdapter<RowData> {

        public CustomAdapter(Context context, int resource,
                             int textViewResourceId, List<RowData> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        private class ViewHolder{
            private View row;
            private TextView titleHolder = null;

            public ViewHolder(View row) {
                super();
                this.row = row;
            }

            public TextView getTitle() {
                if(null == titleHolder)
                    titleHolder = (TextView) row.findViewById(R.id.title);
                return titleHolder;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            TextView title = null;
            RowData rowData = getItem(position);
            if(null == convertView){
                convertView = inflater.inflate(R.layout.list, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            title = holder.getTitle();
            title.setText(rowData.getTitle());
            return convertView;
        }

    }
// Utility to log the table of contents of the ebook
    private void logContentsTable(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }
        for (TOCReference tocReference:tocReferences) {
            StringBuilder tocString = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                tocString.append("\t");
            }
            tocString.append(tocReference.getTitle());
            RowData row = new RowData();
            row.setTitle(tocString.toString());
            row.setResource(tocReference.getResource());
            contentDetails.add(row);
            logContentsTable(tocReference.getChildren(), depth + 1);
        }


    }

    private class RowData{
        private String title;
        private Resource resource;

        public RowData() {
            super();
        }

        public String getTitle() {
            return title;
        }

        public Resource getResource() {
            return resource;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setResource(Resource resource) {
            this.resource = resource;
        }

    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        RowData rowData = contentDetails.get(position);
        Intent intent = new Intent(EpubReaderActivity.this, ContentViewActivity.class);
        try {
            intent.putExtra("display", new String(rowData.getResource().getData()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        startActivity(intent);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = getPath(this, uri);
                        editText.setText(path);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public void copyAssests()
    {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String filename : files)
        {
            Log.i(TAG,"In CopyAssets " + filename);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                String foldername= Environment.getExternalStorageDirectory().getPath()+"/Android/data/"+getPackageName().toString()+"/";
                File folder = new File(foldername);
                folder.mkdirs();
                File outfile = new File(foldername+filename);
                Log.i(TAG,foldername+filename);
                out = new FileOutputStream(outfile);
                copyFile(in, out);
                System.out.println("In copyAssets Entire Path"+foldername+filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}




