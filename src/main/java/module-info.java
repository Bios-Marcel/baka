module link.biosmarcel.baka {
    requires javafx.controls;
    requires org.eclipse.store.storage.embedded;
    requires org.apache.commons.csv;
    requires org.eclipse.store.storage.embedded.configuration;
    requires org.jspecify;

    exports link.biosmarcel.baka;
    exports link.biosmarcel.baka.bankimport;
}