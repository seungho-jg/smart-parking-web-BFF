package bff.model;

public class User {
    private final int id;
    private final String login_id;
    private final String password;
    private final String name;
    private final String vehicle_number;

    public User(int id, String login_id, String password, String name, String vehicle_number) {
        this.id = id;
        this.login_id = login_id;
        this.password = password;
        this.name = name;
        this.vehicle_number = vehicle_number;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.login_id;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNickname() {
        return this.name;
    }

    public String getVehicle_number() { return this.vehicle_number;}
}
