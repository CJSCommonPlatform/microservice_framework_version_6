package uk.gov.justice.services.event.buffer.api;

import static java.lang.Thread.currentThread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class DataInserter {

    private final DataSource dataSource;

    public DataInserter(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private DataInserter cleanData() throws SQLException {
        try(final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM dm_queue")) {
                preparedStatement.executeUpdate();
            }
        }

        return this;
    }

    private void insertData() throws SQLException {

        int counter = 1;

        while(true) {

            try(final Connection connection = dataSource.getConnection()) {
                try(final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO dm_queue values (?, ?, ?)")) {

                    preparedStatement.setInt(1, counter);
                    preparedStatement.setInt(2, counter);
                    preparedStatement.setString(3, "command " + counter);

                    preparedStatement.executeUpdate();

                    System.out.print(".");
                }
                
                counter++;

                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    connection.close();
                    currentThread().interrupt();
                }
            }
        }
    }

    public static void main(final String[] args) throws Exception {


        final DataSource dataSource = new DataSourceFactory().createDataSource();

        final DataInserter dataInserter = new DataInserter(dataSource);

        dataInserter.cleanData().insertData();
    }
}
