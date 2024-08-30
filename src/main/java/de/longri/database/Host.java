package de.longri.database;

public class Host {

    final String host;
    final String port;

    public Host(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

}
