package bff.model;

public class User {
    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String vehicleNumber;

    public User() {}

    public User(String loginId, String password, String name, String vehicleNumber) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.vehicleNumber = vehicleNumber;
    }

    public User(Long id, String loginId, String password, String name, String vehicleNumber) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.vehicleNumber = vehicleNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}
