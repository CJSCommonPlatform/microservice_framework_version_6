package uk.gov.justice.services.event.buffer.notification;

public class Notification {

    private final String tableName;
    private final String action;
    private final String data;

    public Notification(final String tableName, final String action, final String data) {
        this.tableName = tableName;
        this.action = action;
        this.data = data;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "tableName='" + tableName + '\'' +
                ", action='" + action + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
