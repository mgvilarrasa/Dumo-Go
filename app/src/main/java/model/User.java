package model;

/**
 * User class
 */
public class User {
    //Atributs
    private String memberNum;
    private String userName;
    private String name;
    private String lastName;
    private String id;
    private String birthDate;
    private String address;
    private String country;
    private String createDate;
    private String phoneNum;
    private String mail;
    private boolean isAdmin;

    /**
     * Constructor
     * @param memberNum
     * @param userName
     * @param name
     * @param lastName
     * @param id
     * @param birthDate
     * @param address
     * @param country
     * @param createDate
     * @param phoneNum
     * @param mail
     */
    public User(String memberNum, String userName, String name, String lastName, String id, String birthDate,
                String address, String country, String createDate, String phoneNum, String mail) {
        this.memberNum = memberNum;
        this.userName = userName;
        this.name = name;
        this.lastName = lastName;
        this.id = id;
        this.birthDate = birthDate;
        this.address = address;
        this.country = country;
        this.createDate = createDate;
        this.phoneNum = phoneNum;
        this.mail = mail;
    }

    public User(String memberNum, String userName, String name, String lastName, String id, String birthDate,
                String address, String country, String phoneNum, String mail) {
        this.memberNum = memberNum;
        this.userName = userName;
        this.name = name;
        this.lastName = lastName;
        this.id = id;
        this.birthDate = birthDate;
        this.address = address;
        this.country = country;
        this.phoneNum = phoneNum;
        this.mail = mail;
    }

    //Getters
    public String getMemberNum() {
        return memberNum;
    }

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getId() {
        return id;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public String getCountry() {
        return country;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getMail() {
        return mail;
    }

    public boolean isAdmin(){
        return isAdmin;
    }

    //Setters
    public void setMemberNum(String memberNum) {
        this.memberNum = memberNum;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setAdmin(boolean isAdmin){
        this.isAdmin = isAdmin;
    }
}
