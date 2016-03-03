package net.soleheart.creditcardhelper;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.DecimalMin;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.Max;
import com.mobsandgeeks.saripaar.annotation.Min;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import net.soleheart.creditcardhelper.greendao.CreditCard;
import net.soleheart.creditcardhelper.greendao.CreditCardDao;
import net.soleheart.creditcardhelper.greendao.DaoMaster;

import java.util.List;

public class AddOrEditActivity extends AppCompatActivity implements Validator.ValidationListener {
    private Validator mValidator;

    private long mId;

    @NotEmpty(message = "请填入银行名称")
    private EditText mBankName;

    @Max(value = 31, message = "不能大于31")
    @Min(value = 1, message = "不能小于1")
    private EditText mBillDate;

    @Max(value = 31, message = "不能大于31")
    @Min(value = 1, message = "不能小于1")
    private EditText mPayDate;

    @Length(min = 4, max = 4, message = "请输入4位数字")
    private EditText mLastDigits;

    private DaoMaster.DevOpenHelper mDaoHelper;
    private CreditCardDao mDao;

    private boolean mEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mId = getIntent().getLongExtra("creditcard.id", -1);
        mEditMode = mId != -1;
        if (mEditMode) {
            setTitle(R.string.label_edit);
        }

        initDao();
        initForm();
    }

    private void initDao(){
        mDaoHelper = new DaoMaster.DevOpenHelper(this, "creditcard-db", null);
        SQLiteDatabase db = mDaoHelper.getWritableDatabase();
        mDao = new DaoMaster(db).newSession().getCreditCardDao();
    }

    private void initForm() {
        mBankName = (EditText) findViewById(R.id.add_bank_name);
        mBillDate = (EditText) findViewById(R.id.add_bill_date);
        mPayDate = (EditText) findViewById(R.id.add_pay_date);
        mLastDigits = (EditText) findViewById(R.id.add_last_digits);

        if (mEditMode) {
            CreditCard creditCard = mDao.load(mId);
            mBankName.setText(creditCard.getBankName());
            mBillDate.setText(String.valueOf(creditCard.getBillDate()));
            mPayDate.setText(String.valueOf(creditCard.getPayDate()));
            mLastDigits.setText(creditCard.getLastDigits());
        }

        findViewById(R.id.add_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidator.validate();
            }
        });

        // validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onSaveClicked() {
        String bankName = mBankName.getText().toString().trim();
        String billDate = mBillDate.getText().toString().trim();
        String payDate = mPayDate.getText().toString().trim();
        String lastDigits = mLastDigits.getText().toString().trim();

        CreditCard creditCard = new CreditCard(null);
        if (mEditMode) {
            creditCard.setId(mId);
        }
        creditCard.setBankName(bankName);
        creditCard.setBillDate(safeParseInt(billDate));
        creditCard.setPayDate(safeParseInt(payDate));
        creditCard.setLastDigits(lastDigits);

        try {
            if (mEditMode) {
                mDao.update(creditCard);
            } else {
                mDao.insert(creditCard);
            }

            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    static int safeParseInt(String value) {
        int ret = 0;
        try {
            ret = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }

        return ret;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDao = null;
        if (mDaoHelper != null) {
            mDaoHelper.close();
        }
    }

    @Override
    public void onValidationSucceeded() {
        onSaveClicked();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
