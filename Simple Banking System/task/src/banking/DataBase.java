package banking;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class DataBase {
    ResultSet results;

    private String url;

    DataBase(String url){
        this.url = url;
    }

    //Adatbázis java kapcsolatot biztositja
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    //Ha nem lenne table létrehozza program inditásakor
    public void CreateTable(){
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	number TEXT,\n"
                + "	pin TEXT,\n"
                + "	balance INTEGER DEFAULT 0\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Új fiók létrehozása
    boolean newAccountInDataBase(String cardNumber, String pin){
        boolean success = true;
        String sql = "INSERT INTO card(number, pin, balance) VALUES(?, ?, ?)";
        try(Connection conn = this.connect();
            PreparedStatement prep = conn.prepareStatement(sql)){

            prep.setString(1,cardNumber);
            prep.setString(2,pin);
            prep.setInt(3,0);
            prep.executeUpdate();

        }catch (SQLException e){
            System.out.println(e.getMessage());
            success = false;
        }
        return success;
    }


    //Program lezárásakor megszakitja az adatbázissal a kapcsolatot
    public void closeConnection() {
        try(Connection conn = this.connect()) {
            if (conn != null){
                this.connect().close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    final private Map<String, String> accounts = new HashMap<>();

    //Map-be rak minden adatot az sqlből a kártyaszámot és a pinkódját
    public Map<String, String> getAllAccounts(){
        String sql = "SELECT number, pin FROM card";

        try(Connection conn = this.connect();
            Statement state = conn.createStatement()){

            results = state.executeQuery(sql);
            while (results.next()){
                accounts.put(results.getString("number"), results.getString("pin"));
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return accounts;
    }

    //A fiókon lévő pénzösszeget kéri le
    public int getBalance(String number, String pinCode){
        String sql = "SELECT balance FROM card WHERE number = ? AND pin = ?";

        int balance = 0;
        try(Connection conn = this.connect();
            PreparedStatement prep = conn.prepareStatement(sql)){

            prep.setString(1,number);
            prep.setString(2,pinCode);
            ResultSet result = prep.executeQuery();
            while (result.next()){
                balance = 0;
               balance = result.getInt("balance");
            }

        }catch (SQLException e){
            System.out.println(e.getMessage());

        }
        return balance;
    }

    //Összeget lehet feltölteni
    public void addIncome(int inCome, String number, String pin){
        String sql = "UPDATE card SET balance = balance + ? WHERE number = ? AND pin = ?";

        try(Connection conn = this.connect();
            PreparedStatement prep = conn.prepareStatement(sql)){

            prep.setInt(1, inCome);
            prep.setString(2,number);
            prep.setString(3,pin);

            prep.executeUpdate();

            System.out.println("Income was added");


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //Nem kivént fiókot lehet törölni
    public void deleteAccount(String number, String pin){
        String sql = "DELETE FROM card WHERE number = ? AND pin = ?";

        try(Connection conn = this.connect();
            PreparedStatement prep = conn.prepareStatement(sql)){

            prep.setString(1,number);
            prep.setString(2,pin);

            prep.executeUpdate();

            System.out.println("The account has been closed!");

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //Egyik fiókról a másikra lehet utalni pénzt
    public void transferMoney(int money, String fromNumber, String toNumber ) {
        String sqlAddMoney = "UPDATE card SET balance = balance + ? WHERE number = ?";

        String sqlMinusMoney = "UPDATE card SET balance = balance - ? WHERE number = ?";

        try(Connection conn = this.connect()) {
            conn.setAutoCommit(false);

            try(PreparedStatement addMoney = conn.prepareStatement(sqlAddMoney);
                PreparedStatement minusMoney = conn.prepareStatement(sqlMinusMoney)) {

                addMoney.setInt(1, money);
                addMoney.setString(2, toNumber);
                addMoney.executeUpdate();

                minusMoney.setInt(1, money);
                minusMoney.setString(2, fromNumber);
                minusMoney.executeUpdate();

                System.out.println("Success!");

                conn.commit();

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }


    }




}
