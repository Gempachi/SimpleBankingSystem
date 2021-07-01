package banking;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BankSystem {
    final private String url;

    private String loginCardNumber;
    private String loginPinCode;

    Scanner scanner = new Scanner(System.in);

    BankSystem (String url) {
        this.url = url;
    }


    protected void createNewAccount(){

        String cardnumber;
        String pincode;

        String binNumber = "400000";

        //New pin code
        int num = ThreadLocalRandom.current().nextInt(9999);

        pincode = String.format("%04d", num);

        //New CardNumber

        DataBase dataBase = new DataBase(this.url);

        boolean success;
        boolean isValid = true;
        try {
            while(isValid) {
                StringBuilder number = new StringBuilder();
                for (int i = 0; i < 10 ; i++) {
                    Random r = new Random();
                    int low = 0;
                    int high = 9;
                    int random = r.nextInt(high-low) + low;
                    number.append(random);
                }
                cardnumber = binNumber + number;

                if (luhnValidator(cardnumber)){

                    success = dataBase.newAccountInDataBase(cardnumber,pincode);
                    if (success){
                        System.out.println("Your card has been created");
                        System.out.println("Your card number:\n" + cardnumber);
                        System.out.println("Your card PIN:\n" + pincode);
                    }
                    break;
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    static boolean luhnValidator(String number){

        //Convert input to int
        int[] cardNumber = new int[number.length()];

        for (int i = 0; i < number.length(); i++){
            cardNumber[i] = Integer.parseInt(number.substring(i, i + 1) );
        }
        //Starting from right, double each other digit, if greater than 9 mod 10 and +1 to the remainder
        for (int i = cardNumber.length - 2; i >= 0; i = i -2) {
            int tempValue = cardNumber[i];
            tempValue = tempValue * 2;
            if (tempValue > 9) {
                tempValue = tempValue % 10 + 1;
            }
            cardNumber[i] = tempValue;
        }

        //Add up all digits
        int total = 0;
        for (int j : cardNumber) {
            total += j;
        }
        //If number is a multiple of 10, it is valid
        return total % 10 == 0;

    }



    protected void mainMenu(){
        DataBase dataBase = new DataBase(url);
        int input;
        printMenu();

        dataBase.CreateTable();
        do {
            input = scanner.nextInt();
            switch (input){
                case 0:
                    System.out.println("Bye");
                    dataBase.closeConnection();
                    System.exit(0);
                    break;
                case 1:
                    createNewAccount();
                    break;
                case 2:
                    logInAccount();
                    break;
                default:
                    System.out.println("Unknown input");
                    break;
            }
        } while (true);

    }
    protected void logInAccount(){




        System.out.println("Enter your card number: ");
        loginCardNumber = scanner.next();
        System.out.println("Enter your PIN: ");
        loginPinCode = scanner.next();

        DataBase dataBase = new DataBase(url);

        Map<String, String> account = dataBase.getAllAccounts();

        for (Map.Entry<String,String> entry : account.entrySet()) {

            String number = entry.getKey();
            String pin = entry.getValue();


            if (loginCardNumber.equals(number) && pin.equals(loginPinCode)){

                System.out.println("You have successfully logged in!");
                loggedIn();
                break;

            }
        }

    }
    protected void loggedIn(){

        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");

        int input;
        DataBase dataBase = new DataBase(url);

        input = scanner.nextInt();
        switch (input){
            case 0:
                System.out.println("Bye");
                dataBase.closeConnection();
                System.exit(0);
                break;
            case 1:
                int tempBalance = dataBase.getBalance(loginCardNumber,loginPinCode);
                System.out.println(tempBalance);
                loggedIn();
                break;
            case 2:
                System.out.println("Add income: ");
                int inCome = scanner.nextInt();
                dataBase.addIncome(inCome,loginCardNumber,loginPinCode);
                loggedIn();
                break;
            case 3:
                doTransfer();
                break;
            case 4:
                dataBase.deleteAccount(loginCardNumber, loginPinCode);
            case 5:
                dataBase.closeConnection();
                System.out.println("You have successfully logged out!");
                mainMenu();
                break;
            default:
                System.out.println("Unknown input");
                break;
        }

    }

    private void doTransfer(){

        DataBase dataBase = new DataBase(url);
        int transferMoney;


        Map<String, String> account = dataBase.getAllAccounts();

        System.out.println("Enter card number for transfer: ");
        String accountToTransfer = scanner.next();

        //Ha nem ment át a luhm algoritmus validálásán
        if (!luhnValidator(accountToTransfer)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            loggedIn();
        }

        if (accountToTransfer.equals(loginCardNumber)){
            System.out.println("You can't transfer money to the same account!");
            loggedIn();
        }

        boolean isExist = false;
        for (Map.Entry<String,String> entry : account.entrySet()) {

            String number = entry.getKey();

            //Ha megtalálta kérheti a pénzt

            if (accountToTransfer.equals(number)) {
                System.out.println("Enter how much money you want to transfer:");
                transferMoney = scanner.nextInt();
                int baseMoney = dataBase.getBalance(loginCardNumber,loginPinCode);
                if (transferMoney > baseMoney){
                    System.out.println("Not enough money!");
                    loggedIn();
                }

                dataBase.transferMoney(transferMoney,loginCardNumber,accountToTransfer);

                isExist = true;
                loggedIn();
            }
        }
        if(!isExist) {
            System.out.println("Such a card does not exist.");
            loggedIn();
        }

    }

    private void printMenu(){
        System.out.println("1. Create a new account");
        System.out.println("2. Log in account");
        System.out.println("0. Exit");
    }


}
