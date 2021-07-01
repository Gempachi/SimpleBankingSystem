package banking;

public class Main {
    public static void main(String[] args) {

        String filename = "";
        try {
            filename = args[1];
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        String url ="jdbc:sqlite:"+filename;

        BankSystem bankSystem = new BankSystem(url);

        bankSystem.mainMenu();
    }
}