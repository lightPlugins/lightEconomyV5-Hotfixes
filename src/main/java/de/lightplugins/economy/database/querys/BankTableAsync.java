package de.lightplugins.economy.database.querys;

import de.lightplugins.economy.master.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankTableAsync {

    public Main plugin;
    private final String tableName = "BankTable";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     *  ReetrantLock is locking the current thread, befor a second call have access to this methodes
     *  This function is preventing duplication glitches and database write failures.
     */

    private final Lock playerBalanceLock = new ReentrantLock();
    private final Lock playerCurrentBankLevelLock = new ReentrantLock();
    private final Lock createBankAccountLock = new ReentrantLock();
    private final Lock updatePlayerNameLock = new ReentrantLock();
    private final Lock setBankMoney = new ReentrantLock();
    private final Lock getPlayersBalanceListLock = new ReentrantLock();
    private final Lock setBankLevelLock = new ReentrantLock();


    public BankTableAsync(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the balance of a player's bank account by their name.
     *
     * @param playerName The name of the player.
     * @return A CompletableFuture containing the player's bank balance, or null if not found.
     */
    public CompletableFuture<Double> playerBankBalance(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            playerBalanceLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankBalanceQuery(playerName, connection)) {

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("money");
                }

                return null;
            } catch (SQLException e) {
                logError("An error occurred while retrieving player's bank balance for " + playerName, e);
                return null;
            } finally {
                playerBalanceLock.unlock();
            }
        });
    }

    public CompletableFuture<Double> playerBankBalance(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            playerBalanceLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankBalanceQuery(uuid, connection)) {

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("money");
                }

                return null;
            } catch (SQLException e) {
                logError("An error occurred while retrieving player's bank balance for " + uuid, e);
                return null;
            } finally {
                playerBalanceLock.unlock();
            }
        });
    }

    /**
     * Retrieves the current bank level of a player by their name.
     *
     * @param playerName The name of the player.
     * @return A CompletableFuture containing the player's bank level, or null if not found.
     */
    public CompletableFuture<Integer> playerCurrentBankLevel(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            playerCurrentBankLevelLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankLevelQuery(playerName, connection)) {

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getInt("level");
                }

                return null;
            } catch (SQLException e) {
                logError("An error occurred while retrieving the bank level for player: " + playerName, e);
                return null;
            } finally {
                playerCurrentBankLevelLock.unlock();
            }
        });
    }

    /**
     * Creates a new bank account for the specified player.
     *
     * @param playerName The name of the new bank account holder.
     * @return A CompletableFuture indicating the success of the operation.
     */
    public CompletableFuture<Boolean> createBankAccount(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            createBankAccountLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareNewBankAccountInsert(playerName, connection)) {

                ps.execute();
                logInfo("Successfully created a new bank account for " + playerName);
                return true;
            } catch (SQLException e) {
                logError("An error occurred while creating a new bank account for " + playerName, e);
                return null;
            } finally {
                createBankAccountLock.unlock();
            }
        });
    }

    public CompletableFuture<Boolean> createBankAccount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            createBankAccountLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareNewBankAccountInsert(uuid, connection)) {

                ps.execute();
                logInfo("Successfully created a new bank account for " + uuid);
                return true;
            } catch (SQLException e) {
                logError("An error occurred while creating a new bank account for " + uuid, e);
                return null;
            } finally {
                createBankAccountLock.unlock();
            }
        });
    }

    /**
     * Updates the name of the bank account holder in the database.
     *
     * @param playerName The current name of the bank account holder.
     * @return A CompletableFuture indicating the success of the operation.
     */
    public CompletableFuture<Boolean> updatePlayerBankName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            updatePlayerNameLock.lock();
            OfflinePlayer offlinePlayer = Bukkit.getPlayer(playerName);
            if (offlinePlayer == null) {
                return false;
            }

            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankAccountNameUpdate(offlinePlayer, connection)) {

                ps.execute();
                logInfo("Successfully updated the name of the bank account holder: " + playerName);
                return true;
            } catch (SQLException e) {
                logError("An error occurred while updating the name of the bank account holder: " + playerName, e);
                return null;
            } finally {
                updatePlayerNameLock.unlock();
            }
        });
    }

    /**
     * Sets the bank level of a player in the database.
     *
     * @param playerName The name of the player.
     * @param level     The new bank level for the player.
     * @return A CompletableFuture indicating the success of the operation.
     */
    public CompletableFuture<Boolean> setBankLevel(String playerName, int level) {
        return CompletableFuture.supplyAsync(() -> {
            setBankLevelLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankLevelUpdate(playerName, level, connection)) {

                ps.execute();
                logInfo("Successfully set the bank level of player: " + playerName + " to " + level);
                return true;
            } catch (SQLException e) {
                logError("An error occurred while setting the bank level of player: " + playerName, e);
                return null;
            } finally {
                setBankLevelLock.unlock();
            }
        });
    }

    /**
     * Sets the bank balance of a player in the database.
     *
     * @param playerName The name of the player.
     * @param amount     The new bank balance for the player.
     * @return A CompletableFuture indicating the success of the operation.
     */
    public CompletableFuture<Boolean> setBankMoney(String playerName, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            setBankMoney.lock();
            double fixedAmount = Main.util.fixDouble(amount);

            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankBalanceUpdate(playerName, fixedAmount, connection)) {

                ps.execute();
                logInfo("Successfully set the bank balance of player: " + playerName + " to " + fixedAmount);
                return true;
            } catch (SQLException e) {
                logError("An error occurred while setting the bank balance of player: " + playerName, e);
                return null;
            } finally {
                setBankMoney.lock();
            }
        });
    }

    public CompletableFuture<Boolean> setBankMoney(UUID uuid, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            setBankMoney.lock();
            double fixedAmount = Main.util.fixDouble(amount);

            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = prepareBankBalanceUpdate(uuid, fixedAmount, connection)) {

                ps.execute();
                logInfo("Successfully set the bank balance of player: " + uuid + " to " + fixedAmount);
                return true;
            } catch (SQLException e) {
                logError("An error occurred while setting the bank balance of player: " + uuid, e);
                return null;
            } finally {
                setBankMoney.lock();
            }
        });
    }

    public CompletableFuture<HashMap<String, Double>> getPlayersBalanceList() {
        return CompletableFuture.supplyAsync(() -> {
            getPlayersBalanceListLock.lock();
            try (Connection connection = plugin.ds.getConnection();
                 PreparedStatement ps = preparePlayersBalanceListQuery(connection)) {

                ResultSet rs = ps.executeQuery();
                return extractPlayerBalances(rs);
            } catch (SQLException e) {
                logError("An error occurred while retrieving player balances.", e);
                return null;
            } finally {
                getPlayersBalanceListLock.unlock();
            }
        });
    }

    private PreparedStatement prepareBankBalanceQuery(String playerName, Connection connection) throws SQLException {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(playerName);
        PreparedStatement ps;
        if (offlinePlayer != null) {
            ps = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?");
            ps.setString(1, offlinePlayer.getUniqueId().toString());
        } else {
            ps = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE name = ?");
            ps.setString(1, playerName);
        }
        return ps;
    }

    private PreparedStatement prepareBankBalanceQuery(UUID uuid, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?");
        ps.setString(1, uuid.toString());
        return ps;
    }

    private PreparedStatement prepareBankLevelQuery(String playerName, Connection connection) throws SQLException {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(playerName);
        PreparedStatement ps;
        if (offlinePlayer != null) {
            ps = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?");
            ps.setString(1, offlinePlayer.getUniqueId().toString());
        } else {
            ps = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE name = ?");
            ps.setString(1, playerName);
        }
        return ps;
    }

    private PreparedStatement prepareNewBankAccountInsert(
            String playerName, Connection connection) throws SQLException {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(playerName);

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + tableName + " (uuid, name, money, level) VALUES (?, ?, ?, ?)");

        if (offlinePlayer != null) {
            ps.setString(1, offlinePlayer.getUniqueId().toString());
        } else {
            UUID uuid = UUID.randomUUID();
            ps.setString(1, uuid.toString());
        }
        ps.setString(2, playerName);
        ps.setDouble(3, 0.0);
        ps.setInt(4, 1);
        return ps;
    }

    private PreparedStatement prepareNewBankAccountInsert(
            UUID uuid, Connection connection) throws SQLException {

        OfflinePlayer offlinePlayer = Bukkit.getPlayer(uuid);

        if(offlinePlayer == null) {
            return null;
        }

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + tableName + " (uuid, name, money, level) VALUES (?, ?, ?, ?)");
        ps.setString(1, uuid.toString());
        ps.setString(2, offlinePlayer.getName());
        ps.setDouble(3, 0.0);
        ps.setInt(4, 1);
        return ps;
    }

    private PreparedStatement prepareBankAccountNameUpdate(
            OfflinePlayer offlinePlayer, Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("UPDATE " + tableName + " SET name = ? WHERE uuid = ?");
        ps.setString(1, offlinePlayer.getName());
        ps.setString(2, offlinePlayer.getUniqueId().toString());
        return ps;
    }

    private PreparedStatement preparePlayersBalanceListQuery(Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT * FROM " + tableName);
    }

    private HashMap<String, Double> extractPlayerBalances(ResultSet rs) throws SQLException {
        HashMap<String, Double> playerList = new HashMap<>();
        while (rs.next()) {
            playerList.put(rs.getString("name"), rs.getDouble("money"));
        }
        return playerList;
    }

    private PreparedStatement prepareBankLevelUpdate(
            String playerName, int level, Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("UPDATE " + tableName + " SET level = ? WHERE name = ?");
        ps.setInt(1, level);
        ps.setString(2, playerName);
        return ps;
    }

    private PreparedStatement prepareBankBalanceUpdate(
            String playerName, double amount, Connection connection) throws SQLException {
        OfflinePlayer offlinePlayer = Bukkit.getPlayer(playerName);
        PreparedStatement ps;

        if (offlinePlayer != null) {
            ps = connection.prepareStatement("UPDATE " + tableName + " SET money = ? WHERE uuid = ?");
            ps.setString(2, offlinePlayer.getUniqueId().toString());
        } else {
            ps = connection.prepareStatement("UPDATE " + tableName + " SET money = ? WHERE name = ?");
            ps.setString(2, playerName);
        }

        ps.setDouble(1, amount);
        return ps;
    }
    private PreparedStatement prepareBankBalanceUpdate(
            UUID uuid, double amount, Connection connection) throws SQLException {
        PreparedStatement ps;
        ps = connection.prepareStatement("UPDATE " + tableName + " SET money = ? WHERE uuid = ?");
        ps.setString(2, uuid.toString());
        ps.setDouble(1, amount);
        return ps;
    }


    private void logError(String message, Throwable e) {
        logger.error(message, e);
    }

    private void logInfo(String message) {
        if(Main.settings.getConfig().getBoolean("settings.debug")) {
            logger.info(message);
        }
    }
}
