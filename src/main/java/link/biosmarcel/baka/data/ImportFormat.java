package link.biosmarcel.baka.data;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

public enum ImportFormat {
    SparkasseCSV(link.biosmarcel.baka.bankimport.SparkasseCSV::parse),
    DKBCSV(link.biosmarcel.baka.bankimport.DKBCSV::parse),
    RevolutCSV(link.biosmarcel.baka.bankimport.RevolutCSV::parse),
    DKB2CSV(link.biosmarcel.baka.bankimport.DKB2CSV::parse),
    INGCSV(link.biosmarcel.baka.bankimport.INGCSV::parse),
    ;

    public final transient BiFunction<Account, File, List<Payment>> func;

    ImportFormat(final BiFunction<Account, File, List<Payment>> func) {
        this.func = func;
    }
}
