package me.kodokenshi.kodocore.file

import com.github.kittinunf.fuel.httpDownload
import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.extras.log
import me.kodokenshi.kodocore.plugin.KPlugin
import java.io.File
import java.net.URLClassLoader
import java.sql.*

inline fun sqliteFile(fileName: String, block: SQLiteFile.() -> Unit) = SQLiteFile("plugins/${javaPlugin<KPlugin>().name}", fileName).apply(block).closeConnection()
inline fun sqliteFile(filePath: String, fileName: String, block: SQLiteFile.() -> Unit) = SQLiteFile(filePath, fileName).apply(block).closeConnection()

class SQLiteFile {

    val filePath: String
    val name: String
    private var connection: Connection? = null
    var isConnected = false; private set

    constructor(filePath: String, fileName: String) {

        this.filePath = filePath
        name = "$fileName.db"

        try {

            File(filePath).apply { if (!exists()) mkdirs() }

            if (!loadSQLite()) {
                "&9${javaPlugin<KPlugin>().name}> &7Couldn't connect to SQLite for file \"$name\" in folder \"${filePath}\".".log()
                return
            }

            connection = DriverManager.getConnection("jdbc:sqlite:${filePath}/$name")

            isConnected = true

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't connect to SQLite for file \"$name\" in folder \"${filePath}\".".log()
            e.printStackTrace()
        }

    }
    constructor(fileName: String): this("plugins/${javaPlugin<KPlugin>().name}", fileName)

    fun createTable(
        name: String,
        keys: Map<String, ValueType>,
        primaryKey: String = ""
    ): Boolean {

        if (!isConnected) return false

        if (name.isBlank()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't create table for file \"${this@SQLiteFile.name}\" because table is blank.".log()
            return false
        }
        if (keys.isEmpty()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't create table \"$name\" for file \"${this.name}\" because contents map is empty.".log()
            return false
        }

        return try {

            val statement = connection!!.createStatement()

            statement.executeUpdate(buildString {

                append("CREATE TABLE IF NOT EXISTS $name (")

                val lastKey = keys.keys.last()
                for ((key, type) in keys) {

                    append("$key ${type.sql}")
                    if (primaryKey.isNotBlank() && key == primaryKey) append(" PRIMARY KEY")
                    if (key != lastKey) append(", ")

                }
                append(")")

            })

            statement.close()

            true

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't create table \"$name\" for file \"${this.name}\".".log()
            e.printStackTrace()
            false
        }

    }
    fun insertInto(table: String, key: String, value: Value<out Any>) = insertInto(table, arrayOf(key), arrayOf(value))
    fun insertInto(
        table: String,
        keys: Array<String>,
        values: Array<Value<out Any>>
    ): Boolean {

        if (!isConnected) return false

        if (table.isBlank()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't insert into file \"${this.name}\" because table is blank.".log()
            return false
        }
        if (keys.isEmpty()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't insert into table \"$name\" for file \"${this.name}\" because keys list is empty.".log()
            return false
        }
        if (values.isEmpty()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't insert into table \"$name\" for file \"${this.name}\" because values map is empty.".log()
            return false
        }

        return try {

            val statement = connection!!.prepareStatement(buildString {

                append("INSERT OR REPLACE INTO $table (")

                val lastKey = keys.last()
                for (key in keys) {

                    append(key)
                    if (key != lastKey) append(", ")

                }
                append(") VALUES (")

                val size = keys.size - 1
                repeat(size + 1) {

                    append("?")
                    if (it != size) append(", ")

                }
                append(")")

            })

            for ((index, content) in values.withIndex())
                content.set(statement, index + 1)

            statement.executeUpdate()
            statement.close()

            true

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't insert into table \"$name\" for file \"${this.name}\".".log()
            e.printStackTrace()
            false
        }

    }
    fun getFrom(table: String, key: String, type: ValueType, where: String = "") = buildList {
        for (map in getFrom(table, mapOf(key to type), where))
            addAll(map.values)
    }
    fun getFrom(
        table: String,
        keys: Map<String, ValueType>,
        where: String = ""
    ): List<Map<String, Any>> = buildList {

        if (!isConnected) return@buildList

        if (table.isBlank()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't get from file \"${this@SQLiteFile.name}\" because table is blank.".log()
            return@buildList
        }
        if (keys.isEmpty()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't get from table \"$name\" for file \"${this@SQLiteFile.name}\" because keys list is empty.".log()
            return@buildList
        }

        try {

            val statement = connection!!.prepareStatement(buildString {

                append("SELECT ")
                val lastKey = keys.keys.last()
                for (key in keys.keys) {

                    append(key)
                    if (key != lastKey) append(", ")

                }
                append(" FROM $table")
                if (where.isNotBlank()) append(" WHERE $where")

            })

            val result = statement.executeQuery()

            while (result.next())
                add(buildMap {

                    for (key in keys)
                        this[key.key] = key.value.get(result, key.key)

                })

            result.close()
            statement.close()

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't get from table \"$name\" for file \"${this@SQLiteFile.name}\".".log()
            e.printStackTrace()
        }

    }
    fun deleteFrom(
        table: String,
        key: String,
        value: Value<out Any>
    ): Boolean {

        if (!isConnected) return false

        if (table.isBlank()) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't delete from file \"${this@SQLiteFile.name}\" because table is blank.".log()
            return false
        }

        return try {

            val statement = connection!!.prepareStatement("DELETE FROM $table WHERE $key = ?")

            value.set(statement, 1)

            val result = statement.executeUpdate()

            statement.close()
            result > 0

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't delete from table \"$name\" for file \"${this@SQLiteFile.name}\".".log()
            e.printStackTrace()
            false
        }

    }

    fun closeConnection() {
        isConnected = false
        connection?.close()
    }

    fun stringValue(string: String) = Value(string) { statement, index -> statement.setString(index, string) }
    fun byteValue(byte: Byte) = Value(byte) { statement, index -> statement.setByte(index, byte) }
    fun shortValue(short: Short) = Value(short) { statement, index -> statement.setShort(index, short) }
    fun intValue(int: Int) = Value(int) { statement, index -> statement.setInt(index, int) }
    fun longValue(long: Long) = Value(long) { statement, index -> statement.setLong(index, long) }
    fun floatValue(float: Float) = Value(float) { statement, index -> statement.setFloat(index, float) }
    fun doubleValue(double: Double) = Value(double) { statement, index -> statement.setDouble(index, double) }
    fun dateValue(date: Date) = Value(date) { statement, index -> statement.setDate(index, date) }
    fun timeValue(time: Time) = Value(time) { statement, index -> statement.setTime(index, time) }
    fun timestampValue(timestamp: Timestamp) = Value(timestamp) { statement, index -> statement.setTimestamp(index, timestamp) }
    fun booleanValue(boolean: Boolean) = Value(boolean) { statement, index -> statement.setBoolean(index, boolean) }
    fun blobValue(blob: Blob) = Value(blob) { statement, index -> statement.setBlob(index, blob) }
    fun xmlValue(sqlxml: SQLXML) = Value(sqlxml) { statement, index -> statement.setSQLXML(index, sqlxml) }
    class Value<T> internal constructor(internal inline val value: T, internal inline val set: (PreparedStatement, Int) -> Unit)

    enum class ValueType(internal inline val sql: String, internal inline val get: (ResultSet, String) -> Any) {

        STRING("TEXT", { result, key -> result.getString(key) }),
        BYTE("TINYINT", { result, key -> result.getByte(key) }),
        SHORT("SMALLINT", { result, key -> result.getShort(key) }),
        INT("INTEGER", { result, key -> result.getInt(key) }),
        LONG("BIGINT", { result, key -> result.getLong(key) }),
        FLOAT("FLOAT", { result, key -> result.getFloat(key) }),
        DOUBLE("FLOAT", { result, key -> result.getDouble(key) }),
        DATE("DATE", { result, key -> result.getDate(key) }),
        TIME("TIME", { result, key -> result.getTime(key) }),
        TIMESTAMP("TIMESTAMP", { result, key -> result.getTimestamp(key) }),
        BOOLEAN("BOOLEAN", { result, key -> result.getBoolean(key) }),
        BLOB("BLOB", { result, key -> result.getBlob(key) }),
        XML("XML", { result, key -> result.getSQLXML(key) })

    }

    companion object {
        private var sqliteLoaded = false
    }

    private fun loadSQLite(): Boolean {

        if (sqliteLoaded) return true

        return try {

            var canReturn = true
            var loaded = true

            try { Class.forName("org.sqlite.JDBC"); } catch (_: Exception) {

                val sqlite = File("plugins/KodoCore/SQLite-3.43.0.0.jar")

                if (!sqlite.exists()) {
                    
                    canReturn = false
                    "&9${javaPlugin<KPlugin>().name}> &7Downloading SQLite...".log()
                    "https://github.com/xerial/sqlite-jdbc/releases/download/3.43.0.0/sqlite-jdbc-3.43.0.0.jar"
                        .httpDownload()
                        .fileDestination { _, _ ->
                            File("plugins/KodoCore").mkdirs()
                            sqlite
                        }.response { _, _, result ->

                            result.fold(
                                success = {

                                    "&9${javaPlugin<KPlugin>().name}> &aSQLite downloaded.".log()
                                    loaded = true
                                    canReturn = true

                                },
                                failure = {

                                    "&9${javaPlugin<KPlugin>().name}> &cCouldn't download SQLite. No data will be saved.".log()
                                    it.exception.printStackTrace()
                                    loaded = false
                                    canReturn = true

                                }
                            )

                        }

                }

                while (!canReturn) { Thread.sleep(100) }

                URLClassLoader(arrayOf(sqlite.toURI().toURL())).loadClass("org.sqlite.JDBC")

            }

            sqliteLoaded = loaded
            loaded

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

}