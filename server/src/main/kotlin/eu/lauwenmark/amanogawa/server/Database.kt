package eu.lauwenmark.amanogawa.server.eu.lauwenmark.amanogawa.server

import eu.lauwenmark.amanogawa.server.eu.lauwenmark.amanogawa.server.auth.User
import mu.KLogging
import java.io.File
import java.sql.DriverManager

interface UserBackendStore {
    fun store(user: User)
    fun remove(user: User)
    fun retrieve(name: String) : User
}

interface GameBackendStore{
    fun store(game: Game)
    fun remove(game: Game)
    fun retrieve(name: String) : Game
}

class SQLiteUserBackend constructor(_filename: String) : UserBackendStore {

    companion object: KLogging()
    private val filename = _filename

    init {
        Class.forName("org.sqlite.JDBC")
        if (!File(filename).exists()) {
            val connection = DriverManager.getConnection("jdbc:sqlite:$filename")
            val statement = connection.createStatement()
            statement.executeUpdate("CREATE TABLE USERS (NICKNAME VARCHAR(64) PRIMARY KEY NOT NULL, EMAIL VARCHAR(64) NOT NULL, PASSWORDHASH VARCHAR(64) NOT NULL);")
            statement.close()
            connection.close()
            store(User("root", "root@root", "root"))
        } else {
            val connection = DriverManager.getConnection("jdbc:sqlite:$filename")
            connection.close()
        }

        logger.info{"Data file $filename opened successfully"}
    }
    override fun store(user: User) {
        val connection = DriverManager.getConnection("jdbc:sqlite:$filename")
        val statement = connection.createStatement()
        statement.executeUpdate("INSERT INTO USERS (NICKNAME, EMAIL, PASSWORDHASH) VALUES ('root', '', '')")
        statement.close()
        connection.close()
    }
    override fun remove(user: User) {
        val connection = DriverManager.getConnection("jdbc:sqlite:$filename")
        val statement = connection.createStatement()
        statement.executeUpdate("DELETE FROM USERS WHERE NICKNAME='${user.name}';")
        statement.close()
        connection.close()
    }
    override fun retrieve(name: String) : User {
        val connection = DriverManager.getConnection("jdbc:sqlite:$filename")
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM USERS WHERE NICKNAME='$name';")
        while(resultSet.next()) {
            if (name == resultSet.getString("NICKNAME")) {
                return User(name, resultSet.getString("EMAIL"), resultSet.getString("PASSWORDHASH"))
            }
        }
        resultSet.close()
        statement.close()
        connection.close()
        throw IllegalArgumentException("No user named $name could be found in the database.")
    }
}