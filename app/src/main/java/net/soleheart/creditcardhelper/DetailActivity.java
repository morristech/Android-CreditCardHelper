package net.soleheart.creditcardhelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.soleheart.creditcardhelper.greendao.CreditCard;
import net.soleheart.creditcardhelper.greendao.CreditCardDao;
import net.soleheart.creditcardhelper.greendao.DaoMaster;
import net.soleheart.creditcardhelper.logic.FreePriodHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {
    private DaoMaster.DevOpenHelper mDaoHelper;
    private DaoMaster mDaoMaster;

    private TextView mBankName;
    private TextView mBillDate;
    private TextView mPayDate;
    private TextView mLastDigits;

    private TextView mToday;
    private TextView mBillDaysLeft;
    private TextView mPayDaysLeft;
    private TextView mFreePeriod;

    private long mId;

    private SimpleDateFormat mDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mId = getIntent().getLongExtra("creditcard.id", -1);
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd");

        initDao();
        initViews();
    }

    private void initDao() {
        mDaoHelper = new DaoMaster.DevOpenHelper(this, "creditcard-db", null);
        SQLiteDatabase db = mDaoHelper.getReadableDatabase();
        mDaoMaster = new DaoMaster(db);
    }

    private void initViews() {
        mBankName = (TextView) findViewById(R.id.detail_bank_name);
        mBillDate = (TextView) findViewById(R.id.detail_bill_date);
        mPayDate = (TextView) findViewById(R.id.detail_pay_date);
        mLastDigits = (TextView) findViewById(R.id.detail_last_digits);

        mToday = (TextView) findViewById(R.id.detail_today);
        mBillDaysLeft = (TextView) findViewById(R.id.detail_bill_days_left);
        mPayDaysLeft = (TextView) findViewById(R.id.detail_pay_days_left);
        mFreePeriod = (TextView) findViewById(R.id.detail_free_period);

        findViewById(R.id.detail_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClicked();
            }
        });
    }

    private void onDeleteClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_card_title)
                .setMessage(R.string.delete_card_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDaoMaster.newSession().getCreditCardDao().deleteByKey(mId);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshData();
    }

    private void refreshData() {
        CreditCard creditCard = mDaoMaster.newSession().getCreditCardDao().load(mId);

        int billDay = creditCard.getBillDate();
        int payDay = creditCard.getPayDate();

        mBankName.setText(creditCard.getBankName());
        mBillDate.setText(creditCard.getBillDate() + "号");
        mPayDate.setText(creditCard.getPayDate() + "号");
        mLastDigits.setText(creditCard.getLastDigits());

        int billDaysLeft = FreePriodHelper.calcNextDateDaysLeft(billDay);
        int payDaysLeft = FreePriodHelper.calcNextDateDaysLeft(payDay);
        int freePeriod = FreePriodHelper.calcFreePeriod(billDay, payDay);

        mToday.setText(mDateFormat.format(new Date()));
        mBillDaysLeft.setText(billDaysLeft + "天");
        mPayDaysLeft.setText(payDaysLeft + "天");
        mFreePeriod.setText(freePeriod + "天");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_detail_edit) {
            Intent intent = new Intent(this, AddOrEditActivity.class);
            intent.putExtra("creditcard.id", mId);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDaoMaster = null;
        if (mDaoHelper != null) {
            mDaoHelper.close();
        }
    }
}
