package net.soleheart.creditcardhelper;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.soleheart.creditcardhelper.greendao.CreditCard;
import net.soleheart.creditcardhelper.greendao.CreditCardDao;
import net.soleheart.creditcardhelper.greendao.DaoMaster;
import net.soleheart.creditcardhelper.logic.FreePriodHelper;

import java.util.List;

public class ListActivity extends AppCompatActivity {
    private DaoMaster.DevOpenHelper mDaoHelper;
    private SQLiteDatabase mDb;
    private CreditCardDao mDao;
    private Cursor mCursor;
    private SimpleCursorAdapter mCursorAdapter;

    private boolean mOrderByFreePeriodDesc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getSupportActionBar().setTitle(R.string.label_list);

        initDao();
        initListView();

        updateFreePeriod(mDao);
    }

    private void initDao() {
        mDaoHelper = new DaoMaster.DevOpenHelper(this, "creditcard-db", null);
        mDb = mDaoHelper.getReadableDatabase();
        mDao = new DaoMaster(mDb).newSession().getCreditCardDao();

        // 配置adapter
        String[] from = {CreditCardDao.Properties.BankName.columnName,
                CreditCardDao.Properties.LastDigits.columnName,
                CreditCardDao.Properties.DynamicFreePeriod.columnName};
        int[] to = {R.id.item_bank_name, R.id.item_last_digits, R.id.item_free_period};
        mCursorAdapter = new SimpleCursorAdapter(this, R.layout.list_item, mCursor, from, to, 0);
        mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                // 是否是免息期那一列
                int tempColumnIndex = cursor.getColumnIndex(CreditCardDao.Properties.DynamicFreePeriod.columnName);
                if (tempColumnIndex == columnIndex) {
                    int freePeriod = cursor.getInt(columnIndex);

                    mCursorAdapter.setViewText((TextView) view, "免息期: " + freePeriod + "天");
                    return true;
                }
                return false;
            }
        });
    }

    private void initListView() {
        ListView list = (ListView) findViewById(R.id.list_list);
        list.setAdapter(mCursorAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra("creditcard.id", id);
                startActivity(intent);
            }
        });
        TextView headerTip = new TextView(this);
        headerTip.setText("今天消费，免息期最长多少天？");
        list.addHeaderView(headerTip, null, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_list_add) {
            Intent intent = new Intent(ListActivity.this, AddOrEditActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_list_reorder_asc) {
            mOrderByFreePeriodDesc = false;
            refreshCursor();

            item.setChecked(true);

            return true;
        } else if (id == R.id.action_list_reorder_desc) {
            mOrderByFreePeriodDesc = true;
            refreshCursor();

            item.setChecked(true);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshCursor();
    }

    private void updateFreePeriod(CreditCardDao dao) {
        List<CreditCard> creditCards = dao.loadAll();
        for (CreditCard card : creditCards) {
            int freePeriod = FreePriodHelper.calcFreePeriod(card.getBillDate(), card.getPayDate());
            card.setDynamicFreePeriod(freePeriod);
        }

        dao.updateInTx(creditCards);
    }

    private void refreshCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        String orderSuffix = mOrderByFreePeriodDesc ? " DESC" : " ASC";
        String orderBy = CreditCardDao.Properties.DynamicFreePeriod.columnName + orderSuffix;
        mCursor = mDb.query(mDao.getTablename(), mDao.getAllColumns(), null, null, null, null, orderBy);
        if (mCursorAdapter != null) {
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD) {
                mCursorAdapter.swapCursor(mCursor);
            } else {
                mCursorAdapter.changeCursor(mCursor);
            }
            mCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        mDao = null;
        mDb = null;
        if (mDaoHelper != null) {
            mDaoHelper.close();
        }
    }
}
