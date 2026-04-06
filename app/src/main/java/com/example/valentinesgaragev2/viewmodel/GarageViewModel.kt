// viewmodel/GarageViewModel.kt
package com.example.valentinesgaragev2.viewmodel

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.valentinesgaragev2.data.GarageDatabase

class GarageViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GarageDatabase(application)

    private val _trucks = MutableLiveData<Cursor?>()
    val trucks: LiveData<Cursor?> = _trucks

    private val _activeCheckIns = MutableLiveData<Cursor?>()
    val activeCheckIns: LiveData<Cursor?> = _activeCheckIns

    private val _repairs = MutableLiveData<Cursor?>()
    val repairs: LiveData<Cursor?> = _repairs

    private val _employeeReport = MutableLiveData<Cursor?>()
    val employeeReport: LiveData<Cursor?> = _employeeReport

    private val _outstandingTasks = MutableLiveData<Cursor?>()
    val outstandingTasks: LiveData<Cursor?> = _outstandingTasks

    private val _vehicleHistory = MutableLiveData<Cursor?>()
    val vehicleHistory: LiveData<Cursor?> = _vehicleHistory

    private val _employees = MutableLiveData<Cursor?>()
    val employees: LiveData<Cursor?> = _employees

    private val _currentCheckInDetails = MutableLiveData<Cursor?>()
    val currentCheckInDetails: LiveData<Cursor?> = _currentCheckInDetails

    private val _checkInResult = MutableLiveData<Boolean?>()
    val checkInResult: MutableLiveData<Boolean?> = _checkInResult

    init {
        loadTrucks()
        loadActiveCheckIns()
        loadEmployees()
    }

    fun addTruck(registration: String, make: String, model: String, year: Int): Long {
        val result = db.insertTruck(registration, make, model, year)
        loadTrucks()
        return result
    }

    private fun loadTrucks() {
        _trucks.value = db.getAllTrucks()
    }

    fun getTruckByRegistration(registration: String): Cursor {
        return db.getTruckByRegistration(registration)
    }

    fun addCheckIn(
        registration: String,
        make: String,
        model: String,
        year: Int,
        employeeId: String,
        km: Int,
        exterior: String,
        interior: String,
        engine: String,
        tires: String,
        notes: String
    ) {
        try {
            var truckId: Long? = null
            val truckCursor = db.getTruckByRegistration(registration)

            truckCursor.use {
                if (it.moveToFirst()) {
                    truckId = it.getLong(it.getColumnIndexOrThrow("id"))
                }
            }

            if (truckId == null) {
                truckId = db.insertTruck(registration, make, model, year)
            }

            val checkInId = db.insertCheckIn(truckId!!, employeeId, km, exterior, interior, engine, tires, notes)
            _checkInResult.value = checkInId > 0
            loadActiveCheckIns()
        } catch (e: Exception) {
            _checkInResult.value = false
        }
    }

    fun loadActiveCheckIns() {
        _activeCheckIns.value = db.getAllActiveCheckIns()
    }

    fun loadCheckInDetails(checkInId: Long) {
        _currentCheckInDetails.value = db.getCheckInDetails(checkInId)
    }

    fun completeCheckIn(checkInId: Long) {
        db.completeCheckIn(checkInId)
        loadActiveCheckIns()
        _repairs.value = null
        loadOutstandingTasks()
    }

    fun loadRepairsForCheckIn(checkInId: Long) {
        _repairs.value = db.getRepairsForCheckIn(checkInId)
    }

    fun updateRepair(repairId: Long, mechanicName: String, status: String, notes: String) {
        db.updateRepair(repairId, mechanicName, status, notes)
        val repairsCursor = db.getRepairsForCheckIn(getCheckInIdFromRepair(repairId))
        _repairs.value = repairsCursor
        loadOutstandingTasks()
    }

    private fun getCheckInIdFromRepair(repairId: Long): Long {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT check_in_id FROM ${GarageDatabase.TABLE_REPAIRS} WHERE id = ?",
            arrayOf(repairId.toString())
        )
        var checkInId = 0L
        cursor.use {
            if (it.moveToFirst()) {
                checkInId = it.getLong(0)
            }
        }
        return checkInId
    }

    fun loadEmployeeReport(mechanicName: String? = null) {
        _employeeReport.value = db.getEmployeeReport(mechanicName)
    }

    fun loadVehicleHistory(registration: String) {
        _vehicleHistory.value = db.getVehicleHistory(registration)
    }

    fun loadOutstandingTasks() {
        _outstandingTasks.value = db.getOutstandingTasks()
    }

    private fun loadEmployees() {
        _employees.value = db.getAllEmployees()
    }

    fun clearCheckInResult() {
        _checkInResult.value = null
    }
}