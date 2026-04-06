package com.example.valentinesgaragev2.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GarageDatabase(context: Context) : SQLiteOpenHelper(context, "GarageDB", null, 2) {

    companion object {
        const val TABLE_TRUCKS = "trucks"
        const val TABLE_CHECK_INS = "check_ins"
        const val TABLE_REPAIRS = "repaijghrs"
        const val TABLE_EMPLOYEES = "employees"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Trucks table
        db.execSQL("""
            CREATE TABLE $TABLE_TRUCKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                registration TEXT UNIQUE,
                make TEXT,
                model TEXT,
                year INTEGER
            )
        """)

        // Check-ins table with condition tracking
        db.execSQL("""
            CREATE TABLE $TABLE_CHECK_INS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                truck_id INTEGER,
                employee_id TEXT,
                date TEXT,
                km INTEGER,
                exterior_condition TEXT,
                interior_condition TEXT,
                engine_condition TEXT,
                tires_condition TEXT,
                notes TEXT,
                status TEXT DEFAULT 'Active',
                FOREIGN KEY(truck_id) REFERENCES trucks(id)
            )
        """)

        // Repairs table with collaborative tracking
        db.execSQL("""
            CREATE TABLE $TABLE_REPAIRS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                check_in_id INTEGER,
                task_name TEXT,
                category TEXT,
                mechanic_name TEXT,
                status TEXT DEFAULT 'Pending',
                notes TEXT,
                last_updated TEXT,
                FOREIGN KEY(check_in_id) REFERENCES $TABLE_CHECK_INS(id)
            )
        """)

        // Employees table
        db.execSQL("""
            CREATE TABLE $TABLE_EMPLOYEES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                employee_id TEXT UNIQUE,
                name TEXT,
                role TEXT
            )
        """)

        // Insert sample employees
        insertSampleEmployees(db)
    }

    private fun insertSampleEmployees(db: SQLiteDatabase) {
        val employees = listOf(
            arrayOf("M001", "John Smith", "Senior Mechanic"),
            arrayOf("M002", "Mike Johnson", "Mechanic"),
            arrayOf("M003", "Sarah Williams", "Mechanic"),
            arrayOf("S001", "Valentine", "Manager")
        )

        for (emp in employees) {
            val values = ContentValues()
            values.put("employee_id", emp[0])
            values.put("name", emp[1])
            values.put("role", emp[2])
            db.insert(TABLE_EMPLOYEES, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPAIRS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHECK_INS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRUCKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPLOYEES")
        onCreate(db)
    }

    fun insertTruck(registration: String, make: String, model: String, year: Int): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("registration", registration)
        values.put("make", make)
        values.put("model", model)
        values.put("year", year)
        return db.insert(TABLE_TRUCKS, null, values)
    }

    fun getTruckByRegistration(reg: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_TRUCKS WHERE registration = ?", arrayOf(reg))
    }

    fun getAllTrucks(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_TRUCKS ORDER BY registration", null)
    }

    fun insertCheckIn(
        truckId: Long,
        employeeId: String,
        km: Int,
        exterior: String,
        interior: String,
        engine: String,
        tires: String,
        notes: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("truck_id", truckId)
        values.put("employee_id", employeeId)
        values.put("date", getCurrentDateTime())
        values.put("km", km)
        values.put("exterior_condition", exterior)
        values.put("interior_condition", interior)
        values.put("engine_condition", engine)
        values.put("tires_condition", tires)
        values.put("notes", notes)
        values.put("status", "Active")

        val checkInId = db.insert(TABLE_CHECK_INS, null, values)
        createDefaultRepairTasks(checkInId)
        return checkInId
    }

    private fun createDefaultRepairTasks(checkInId: Long) {
        val db = writableDatabase
        val defaultTasks = listOf(
            "Oil Change" to "Engine",
            "Brake Inspection" to "Brakes",
            "Tire Rotation" to "Tires",
            "Engine Diagnostic" to "Engine"
        )

        for ((task, category) in defaultTasks) {
            val values = ContentValues()
            values.put("check_in_id", checkInId)
            values.put("task_name", task)
            values.put("category", category)
            values.put("status", "Pending")
            values.put("mechanic_name", "")
            values.put("notes", "")
            values.put("last_updated", getCurrentDateTime())
            db.insert(TABLE_REPAIRS, null, values)
        }
    }

    fun getAllActiveCheckIns(): Cursor {
        val db = readableDatabase
        return db.rawQuery("""
            SELECT c.id, t.registration, t.make, t.model, c.date, c.km, c.status
            FROM $TABLE_CHECK_INS c
            JOIN $TABLE_TRUCKS t ON c.truck_id = t.id
            WHERE c.status != 'Completed'
            ORDER BY c.date DESC
        """, null)
    }

    fun getCheckInDetails(checkInId: Long): Cursor {
        val db = readableDatabase
        return db.rawQuery("""
            SELECT c.*, t.registration, t.make, t.model, t.year
            FROM $TABLE_CHECK_INS c
            JOIN $TABLE_TRUCKS t ON c.truck_id = t.id
            WHERE c.id = ?
        """, arrayOf(checkInId.toString()))
    }

    fun getRepairsForCheckIn(checkInId: Long): Cursor {
        val db = readableDatabase
        return db.rawQuery("""
            SELECT * FROM $TABLE_REPAIRS 
            WHERE check_in_id = ?
            ORDER BY category, task_name
        """, arrayOf(checkInId.toString()))
    }

    fun updateRepair(repairId: Long, mechanicName: String, status: String, notes: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("mechanic_name", mechanicName)
        values.put("status", status)
        values.put("notes", notes)
        values.put("last_updated", getCurrentDateTime())
        db.update(TABLE_REPAIRS, values, "id = ?", arrayOf(repairId.toString()))
    }

    fun getEmployeeReport(mechanicName: String? = null): Cursor {
        val db = readableDatabase
        return if (mechanicName == null) {
            db.rawQuery("""
                SELECT mechanic_name, 
                       COUNT(*) as tasks_completed,
                       GROUP_CONCAT(task_name) as tasks
                FROM $TABLE_REPAIRS
                WHERE mechanic_name != '' AND status = 'Completed'
                GROUP BY mechanic_name
                ORDER BY mechanic_name
            """, null)
        } else {
            db.rawQuery("""
                SELECT t.registration, r.task_name, r.status, r.notes, r.last_updated
                FROM $TABLE_REPAIRS r
                JOIN $TABLE_CHECK_INS c ON r.check_in_id = c.id
                JOIN $TABLE_TRUCKS t ON c.truck_id = t.id
                WHERE r.mechanic_name = ? AND r.status = 'Completed'
                ORDER BY r.last_updated DESC
            """, arrayOf(mechanicName))
        }
    }

    fun getVehicleHistory(registration: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("""
            SELECT c.date, c.km, c.exterior_condition, c.interior_condition,
                   c.engine_condition, c.tires_condition, c.notes
            FROM $TABLE_CHECK_INS c
            JOIN $TABLE_TRUCKS t ON c.truck_id = t.id
            WHERE t.registration = ?
            ORDER BY c.date DESC
        """, arrayOf(registration))
    }

    fun getOutstandingTasks(): Cursor {
        val db = readableDatabase
        return db.rawQuery("""
            SELECT t.registration, r.task_name, r.category, r.status, r.mechanic_name
            FROM $TABLE_REPAIRS r
            JOIN $TABLE_CHECK_INS c ON r.check_in_id = c.id
            JOIN $TABLE_TRUCKS t ON c.truck_id = t.id
            WHERE r.status != 'Completed'
            ORDER BY c.date DESC, r.category
        """, null)
    }

    fun getAllEmployees(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_EMPLOYEES ORDER BY name", null)
    }

    fun completeCheckIn(checkInId: Long) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("status", "Completed")
        db.update(TABLE_CHECK_INS, values, "id = ?", arrayOf(checkInId.toString()))
    }

    private fun getCurrentDateTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}