package net.soleheart.creditcardhelper.greendao;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class CreditCardDaoGenerator {
    static final String OUTPUT_DIR = "app/src/greendao-gen";
    static final String OUTPUT_PKG_NAME = "net.soleheart.creditcardhelper.greendao";

    public static void main(String[] args) throws Exception {
        // Ensure the output dir existing
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Schema schema = new Schema(1000, OUTPUT_PKG_NAME);
        addItem(schema);
        new DaoGenerator().generateAll(schema, OUTPUT_DIR);
    }

    private static void addItem(Schema schema) {
        Entity creditCardItem = schema.addEntity("CreditCard");
        creditCardItem.implementsSerializable();
        creditCardItem.addIdProperty();

//        creditCardItem.addStringProperty("objectId").notNull().unique();
        creditCardItem.addStringProperty("bankName").notNull();
        creditCardItem.addIntProperty("billDate").notNull();
        creditCardItem.addIntProperty("payDate").notNull();
        creditCardItem.addStringProperty("lastDigits").notNull();
        creditCardItem.addIntProperty("dynamicFreePeriod");
    }
}
