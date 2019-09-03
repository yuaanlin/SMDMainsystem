package mainsystem;

import static mainsystem.App.players;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;

class economy extends AbstractEconomy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "SMD economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return null;
    }

    @Override
    public String currencyNamePlural() {
        return null;
    }

    @Override
    public String currencyNameSingular() {
        return null;
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public double getBalance(String playerName) {
        if (players.containsKey(playerName)) {
            player p = players.get(playerName);
            return p.balance;
        } else {
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String datasource = "jdbc:mysql://localhost/mcserver?user=userdatadownloader&password=Ken3228009!&useSSL=false";
                conn = DriverManager.getConnection(datasource);
                Statement st = conn.createStatement();
                ResultSet rs = st.getResultSet();
                st.execute("SELECT * FROM userdata where `id`='" + playerName + "'");
                rs = st.getResultSet();
                while (rs.next()) {
                    return rs.getDouble("balance");
                }
                conn.close();
            } catch (Exception exc) {

            }
        }
        return 0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        player p = players.get(playerName);
        return p.balance;
    }

    @Override
    public boolean has(String playerName, double amount) {
        if (getBalance(playerName) > amount)
            return true;
        else
            return false;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        if (getBalance(playerName) > amount)
            return true;
        else
            return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }

}