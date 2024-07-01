package link.biosmarcel.baka.bankimport;

public class Import {
    public static String prepareIBAN(final String iban) {
        return iban.replaceAll(" ", "");
    }
}
