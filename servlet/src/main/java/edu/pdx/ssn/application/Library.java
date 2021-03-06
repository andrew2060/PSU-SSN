package edu.pdx.ssn.application;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface Library {

    Record getRecord(long barcode);

    List<Book> getCatalog(Long isbn, String title, String last, String first, String subj, Integer courseno);

    boolean hold(Long bookUid, long userUid, Date dueDate);

    Book getBook(long barcode);

    Book createNewBook(long isbn, String title, String last, String first, String profs, String subj, int num);

    Collection<Record> getRecords(long isbn);

    Record createRecord(long barcode, long isbn, Long donorUid, Long retDate);

    void checkin(long barcode);

    Book updateBook(long isbn, String title, String last, String first, String profs, String subj, int num);

    boolean checkout(long barcode, long date);

    List<Record> getBorrowedRecords(long uid);

    List<Record> getLoanedRecords(long uid);

    void removeRecord(long barcode, String uid);
}
