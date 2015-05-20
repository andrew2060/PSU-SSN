package edu.pdx.ssn.application;

import java.util.List;
import java.util.UUID;

public interface Library {

    List<Book> getCatalog(Long uid, Long isbn, String title, String last, String first, String subj, Integer courseno);

    boolean checkout(Long bookUid, UUID userUid, String dueDate);

    Book getBook(long barcode);

    Book createNew(long barcode, long isbn, String title, String last, String first, String profs, String subj, int num, long donor, long ret);

}
