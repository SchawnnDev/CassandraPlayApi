package utils;

import com.datastax.driver.core.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is a helper class for implementing a pagination function based on Cassandra Java Driver (<a href="https://datastax.github.io/java-driver/">...</a>)
 * <p>
 * The solution of skipping rows is that use page state rather than iterator rows one by one.
 */
public class CassandraPaging {

    private Session session;

    public CassandraPaging(Session session) {
        this.session = session;
    }

    /**
     * Retrieve rows for the specified page offset.
     *
     * @param statement
     * @param start     starting row (>1), inclusive
     * @param size      the maximum rows need to retrieve.
     * @return List<Row>
     */
    public List<Row> fetchRowsWithPage(Statement statement, int start, int size) {
        ResultSet result = skipRows(statement, start, size);
        return getRows(result, start, size);
    }

    private ResultSet skipRows(Statement statement, int start, int size) {
        ResultSet result = null;
        int skippingPages = getPageNumber(start, size);
        String savingPageState = null;
        statement.setFetchSize(size);
        boolean isEnd = false;
        for (int i = 0; i < skippingPages; i++) {
            if (null != savingPageState) {
                statement = statement.setPagingState(PagingState.fromString(savingPageState));
            }
            result = session.execute(statement);
            PagingState pagingState = result.getExecutionInfo().getPagingState();
            if (null != pagingState) {
                savingPageState = result.getExecutionInfo().getPagingState().toString();
            }

            if (result.isFullyFetched() && null == pagingState) {

                //if hit the end more than once, then nothing to return, otherwise, mark the isEnd to 'true'
                if (isEnd) {
                    return null;
                } else {
                    isEnd = true;
                }
            }
        }
        return result;
    }

    private int getPageNumber(int start, int size) {
        if (start < 1) {
            throw new IllegalArgumentException("Starting row need to be larger than 1");
        }
        int page = 1;
        if (start > size) {
            page = (start - 1) / size + 1;
        }
        return page;
    }

    private List<Row> getRows(ResultSet result, int start, int size) {
        List<Row> rows = new ArrayList<>(size);
        if (null == result) {
            return rows;
        }
        int skippingRows = (start - 1) % size;
        int index = 0;
        for (Iterator<Row> iter = result.iterator(); iter.hasNext() && rows.size() < size; ) {
            Row row = iter.next();
            if (index >= skippingRows) {
                rows.add(row);
            }
            index++;
        }
        return rows;
    }

}